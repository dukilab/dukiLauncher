package com.dukilab.launcher.ui.components

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import com.dukilab.launcher.ui.theme.Duki
import com.dukilab.launcher.ui.theme.DukiDimens

/** ボタンの種類。主ボタン（塗り）は1画面に1つまで。 */
enum class DukiButtonStyle { Primary, Outline }

/**
 * 直角・太字のボタン。左上に cut-sm の三角が付く。
 * 押すと三角が45°の斜めのまま右下へ広がってボタン全体を覆い、白黒が反転する（0.24秒）。
 * 離すと0.32秒で左上へ戻る。端末のアニメーションがオフのときは切り替えだけにする。
 */
@Composable
fun DukiButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: DukiButtonStyle = DukiButtonStyle.Primary,
    enabled: Boolean = true,
    leading: (@Composable RowScope.() -> Unit)? = null,
) {
    val c = Duki.colors
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val density = LocalDensity.current
    val restPx = with(density) { DukiDimens.cutSm.toPx() }
    val wipe = remember { Animatable(restPx) }
    val reduceMotion = rememberReduceMotion()

    // 押している間は「幅＋高さ」まで広げると、斜めの辺がちょうど右下の角を越えて全面を覆う。
    var coverPx by remember { mutableFloatStateOf(restPx) }
    LaunchedEffect(pressed, enabled) {
        val target = if (pressed && enabled) coverPx else restPx
        if (reduceMotion) {
            wipe.snapTo(target)
        } else if (pressed) {
            wipe.animateTo(target, tween(durationMillis = 240, easing = CubicBezierEasing(.4f, 0f, .2f, 1f)))
        } else {
            wipe.animateTo(target, tween(durationMillis = 320, easing = CubicBezierEasing(.2f, .8f, .2f, 1f)))
        }
    }

    val (bg, fg, border) = when {
        !enabled -> Triple(c.surfaceSunken, c.inkMuted, c.line)
        style == DukiButtonStyle.Primary -> Triple(c.inverseSurface, c.inverseInk, c.inverseSurface)
        else -> Triple(c.surface, c.ink, c.lineStrong)
    }

    CompositionLocalProvider(LocalContentColor provides fg) {
        Row(
            modifier = modifier
                .onSizeChanged { coverPx = (it.width + it.height).toFloat().coerceAtLeast(restPx) }
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .drawWithContent {
                    drawContent()
                    val s = wipe.value
                    val path = Path().apply {
                        moveTo(0f, 0f); lineTo(s, 0f); lineTo(0f, s); close()
                    }
                    if (enabled) {
                        // 下の色と文字を反転して見せる（bundle.css の mix-blend-mode: difference）
                        drawPath(path, Color.White, blendMode = BlendMode.Difference)
                    } else {
                        drawPath(path, c.line)
                    }
                }
                .background(bg)
                .border(DukiDimens.borderStrong, border)
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick,
                )
                .padding(horizontal = DukiDimens.space5, vertical = DukiDimens.space3),
            horizontalArrangement = Arrangement.spacedBy(DukiDimens.space2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leading?.invoke(this)
            Text(text = text, style = Duki.type.button, color = fg, maxLines = 1)
        }
    }
}

/** 端末の「アニメーションを無効にする」（アニメーター再生時間 0）がオンか。 */
@Composable
fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}
