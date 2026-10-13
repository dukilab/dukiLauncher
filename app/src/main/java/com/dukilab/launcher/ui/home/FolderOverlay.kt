package com.dukilab.launcher.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.dukilab.launcher.core.AppEntry
import com.dukilab.launcher.core.HomeItem
import com.dukilab.launcher.core.Tags
import com.dukilab.launcher.ui.components.AppIcon
import com.dukilab.launcher.ui.components.DukiCard
import com.dukilab.launcher.ui.components.DukiTag
import com.dukilab.launcher.ui.components.DukiText
import com.dukilab.launcher.ui.components.bottomLine
import com.dukilab.launcher.ui.components.itemGestures
import com.dukilab.launcher.ui.theme.DukiDimens
import com.dukilab.launcher.ui.theme.DukiTheme

/**
 * 開いたフォルダ。中央のカードに名前（その場で書き換えられる）とアプリを並べる。
 * 外側を押すと閉じる。
 */
@Composable
fun FolderOverlay(
    folder: HomeItem.Folder,
    apps: (String) -> AppEntry?,
    editName: Boolean,
    onRename: (String) -> Unit,
    onLaunch: (String) -> Unit,
    onLongPress: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val c = DukiTheme.colors
    var name by remember(folder.id) { mutableStateOf(folder.name) }
    val focus = remember { FocusRequester() }
    val rename by rememberUpdatedState(onRename)

    LaunchedEffect(editName) {
        if (editName) runCatching { focus.requestFocus() }
    }

    fun commit() {
        if (name.trim() != folder.name) rename(name)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClickLabel = "閉じる",
            ) {
                commit()
                onDismiss()
            }
            .testTag("folder"),
        contentAlignment = Alignment.Center,
    ) {
        DukiCard(
            modifier = Modifier
                .padding(DukiDimens.space5)
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
        ) {
            DukiTag(Tags.of("folder", folder.appKeys.size))
            Spacer(Modifier.height(DukiDimens.space1))
            BasicTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                textStyle = DukiTheme.type.h3.copy(color = c.ink),
                cursorBrush = SolidColor(c.ink),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { commit() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focus)
                    .onFocusChanged { if (!it.isFocused) commit() }
                    .bottomLine(c.lineStrong)
                    .padding(vertical = DukiDimens.space1)
                    .semantics { contentDescription = "フォルダ名" },
            )
            Spacer(Modifier.height(DukiDimens.space4))
            folder.appKeys.chunked(4).forEach { rowKeys ->
                Row(Modifier.fillMaxWidth()) {
                    rowKeys.forEach { key ->
                        FolderApp(
                            app = apps(key),
                            onTap = { onLaunch(key) },
                            onLongPress = { onLongPress(key) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(4 - rowKeys.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun FolderApp(app: AppEntry?, onTap: () -> Unit, onLongPress: () -> Unit, modifier: Modifier = Modifier) {
    val tap by rememberUpdatedState(onTap)
    val long by rememberUpdatedState(onLongPress)
    Column(
        modifier = modifier
            .itemGestures(app?.key, onTap = { tap() }, onLongPress = { long() })
            .semantics { contentDescription = app?.label ?: "" }
            .padding(vertical = DukiDimens.space2),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppIcon(app, 48.dp)
        Spacer(Modifier.height(DukiDimens.space1))
        DukiText(app?.label.orEmpty(), DukiTheme.type.appLabel, maxLines = 1)
    }
}
