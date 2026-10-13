package com.dukilab.launcher.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dukilab.launcher.core.AppEntry
import com.dukilab.launcher.ui.LocalIconProvider
import com.dukilab.launcher.ui.theme.Duki
import com.dukilab.launcher.ui.theme.DukiDimens

private val Grayscale = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

/** アプリのアイコン。読み込めないときは頭文字の四角を出す。 */
@Composable
fun AppIcon(app: AppEntry, size: Dp, monochrome: Boolean, modifier: Modifier = Modifier) {
    val provider = LocalIconProvider.current
    val icon: ImageBitmap? by produceState(initialValue = provider.peek(app.key), app.key, provider) {
        if (value == null) value = provider.load(app.key)
    }
    val bitmap = icon
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            colorFilter = if (monochrome) Grayscale else null,
            modifier = modifier.size(size),
        )
    } else {
        val c = Duki.colors
        Box(
            modifier = modifier
                .size(size)
                .background(c.surfaceSunken)
                .border(DukiDimens.borderHair, c.line),
            contentAlignment = Alignment.Center,
        ) {
            Text(app.label.take(1), style = Duki.type.h3, color = c.ink)
        }
    }
}

/** 長押しメニューの1項目。 */
data class AppMenuItem(val label: String, val enabled: Boolean = true, val onClick: () -> Unit)

/** アイコン＋名前のタイル。タップで開き、長押しでメニューを出す。 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppTile(
    app: AppEntry,
    monochrome: Boolean,
    onClick: () -> Unit,
    menu: () -> List<AppMenuItem>,
    modifier: Modifier = Modifier,
    iconSize: Dp = 48.dp,
    showLabel: Boolean = true,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    val c = Duki.colors
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClickLabel = "開く",
                    onLongClickLabel = "メニュー",
                    onClick = onClick,
                    onLongClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        menuOpen = true
                    },
                )
                .padding(vertical = DukiDimens.space2, horizontal = DukiDimens.space1)
                .testTag("app:" + app.key),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DukiDimens.space1),
        ) {
            AppIcon(app, iconSize, monochrome)
            if (showLabel) {
                Text(
                    text = app.label,
                    style = Duki.type.caption,
                    color = c.ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
        DukiMenu(expanded = menuOpen, onDismiss = { menuOpen = false }, title = app.label, items = if (menuOpen) menu() else emptyList())
    }
}

/** dukiLab.の形（直角・ヘアラインの枠・影なし）のメニュー。 */
@Composable
fun DukiMenu(expanded: Boolean, onDismiss: () -> Unit, title: String, items: List<AppMenuItem>) {
    val c = Duki.colors
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        shape = RectangleShape,
        containerColor = c.surfaceRaised,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(DukiDimens.borderHair, c.lineStrong),
        modifier = Modifier.cornerMark(DukiDimens.cutSm, c.cornerMark),
    ) {
        DukiTag(title, modifier = Modifier.padding(horizontal = DukiDimens.space4, vertical = DukiDimens.space2))
        items.forEach { item ->
            DropdownMenuItem(
                text = { Text(item.label, style = Duki.type.bodySm) },
                enabled = item.enabled,
                onClick = {
                    onDismiss()
                    item.onClick()
                },
            )
        }
    }
}
