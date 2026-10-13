package com.dukilab.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.dukilab.launcher.ui.theme.Duki
import com.dukilab.launcher.ui.theme.DukiDimens

/**
 * dukiLab.の基本の入れ物。直角の面の左上に cut-md の三角。
 * [inverse] で反転版（塗り面。三角は地の色で抜く）。
 */
@Composable
fun DukiCard(
    modifier: Modifier = Modifier,
    inverse: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(
        start = DukiDimens.space5, end = DukiDimens.space5,
        top = DukiDimens.space4, bottom = DukiDimens.space5,
    ),
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = Duki.colors
    val bg = if (inverse) c.inverseSurface else c.surfaceRaised
    val border = if (inverse) c.inverseSurface else c.line
    val fg = if (inverse) c.inverseInk else c.ink
    val mark = if (inverse) c.surface else c.cornerMark
    CompositionLocalProvider(LocalContentColor provides fg) {
        Column(
            modifier = modifier
                .background(bg)
                .border(DukiDimens.borderHair, border)
                .cornerMark(DukiDimens.cutMd, mark)
                .padding(contentPadding),
            content = content,
        )
    }
}
