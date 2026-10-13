package com.dukilab.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import com.dukilab.launcher.ui.theme.Duki
import com.dukilab.launcher.ui.theme.DukiDimens

/** 入力欄（surface-sunken の面、line-strong の2px枠。フォーカス中は ink の枠）。 */
@Composable
fun DukiSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onSearch: () -> Unit = {},
) {
    val c = Duki.colors
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = Duki.type.body.copy(color = c.ink),
        cursorBrush = SolidColor(c.ink),
        interactionSource = interaction,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        modifier = modifier.testTag("search"),
        decorationBox = { inner ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(c.surfaceSunken)
                    .border(DukiDimens.borderStrong, if (focused) c.focusRing else c.lineStrong)
                    .padding(horizontal = DukiDimens.space4, vertical = DukiDimens.space3),
            ) {
                if (value.isEmpty()) {
                    Text(placeholder, style = Duki.type.body, color = c.inkMuted, maxLines = 1)
                }
                inner()
            }
        },
    )
}
