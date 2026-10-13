package com.dukilab.launcher.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.dukilab.launcher.ui.theme.Duki
import java.util.Locale

/** 分類と通し番号の控えめなタグ（例：`APPS · 042`）。枠・塗り・角マークは付けない。 */
@Composable
fun DukiTag(text: String, modifier: Modifier = Modifier, color: Color = Duki.colors.inkMuted) {
    Text(
        text = text.uppercase(Locale.ROOT),
        style = Duki.type.tag,
        color = color,
        modifier = modifier,
        maxLines = 1,
    )
}
