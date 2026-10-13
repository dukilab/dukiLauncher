package com.dukilab.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dukilab.launcher.ui.theme.Duki
import com.dukilab.launcher.ui.theme.DukiDimens

/** ロゴ面の形：左端は45°の右上がり（／）、右端は垂直。 */
private val LogoPlaneShape = GenericShape { size, _ ->
    moveTo(size.height, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height)
    lineTo(0f, size.height)
    close()
}

/**
 * dukiLab.のヘッダー（スマホ幅版）。
 * 黒 ▶ 斜め境界 ▶ 白いロゴ面 ▶ 垂直境界 ▶ 黒い帯。色はテーマに関係なく固定。
 * ロゴ面の左の余白は「高さ÷2＋12dp」（先頭の d の45°カットと斜め境界のすき間を12dpにそろえる）。
 */
@Composable
fun DukiHeader(
    modifier: Modifier = Modifier,
    logo: String = "dukiLauncher.",
    height: Dp = 56.dp,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val c = Duki.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(c.headerBar)
            .padding(start = DukiDimens.space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .clip(LogoPlaneShape)
                .background(c.headerLogoBg)
                .padding(start = height / 2 + 12.dp, end = DukiDimens.space4),
            contentAlignment = Alignment.CenterStart,
        ) {
            // ロゴ面は常に白なので、ロゴは常に黒（logo-ink のライト値）。
            Text(text = logo, style = Duki.type.logotypeSm, color = Color.Black, maxLines = 1)
        }
        Row(
            modifier = Modifier.weight(1f).padding(end = DukiDimens.space4),
            horizontalArrangement = Arrangement.spacedBy(DukiDimens.space4, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
            content = actions,
        )
    }
}

/** ヘッダー帯の上の文字リンク（白・等幅）。 */
@Composable
fun DukiHeaderAction(text: String, onClick: () -> Unit) {
    val c = Duki.colors
    Text(
        text = text,
        style = Duki.type.spec,
        color = c.headerBarInk,
        modifier = Modifier
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = DukiDimens.space2),
    )
}
