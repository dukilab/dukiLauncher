package com.dukilab.launcher.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.dukilab.launcher.BuildConfig
import com.dukilab.launcher.core.AppEntry
import com.dukilab.launcher.core.AppSearch
import com.dukilab.launcher.core.DoubleTapAction
import com.dukilab.launcher.core.LauncherSettings
import com.dukilab.launcher.core.SwipeDownAction
import com.dukilab.launcher.core.Tags
import com.dukilab.launcher.core.ThemeMode
import com.dukilab.launcher.ui.LauncherActions
import com.dukilab.launcher.ui.LauncherUiState
import com.dukilab.launcher.ui.components.AppIcon
import com.dukilab.launcher.ui.components.DukiButton
import com.dukilab.launcher.ui.components.DukiButtonStyle
import com.dukilab.launcher.ui.components.DukiCard
import com.dukilab.launcher.ui.components.DukiChoiceRow
import com.dukilab.launcher.ui.components.DukiHeader
import com.dukilab.launcher.ui.components.DukiTag
import com.dukilab.launcher.ui.components.DukiText
import com.dukilab.launcher.ui.components.DukiToggleRow
import com.dukilab.launcher.ui.components.gridBackground
import com.dukilab.launcher.ui.components.itemGestures
import com.dukilab.launcher.ui.theme.DukiDimens
import com.dukilab.launcher.ui.theme.DukiTheme

/** 設定画面とその下の画面で共通の枠（黒いヘッダー＋方眼の地＋縦スクロール）。 */
@Composable
fun SubScreen(
    title: String,
    onBack: () -> Unit,
    testTag: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = DukiTheme.colors
    Column(
        Modifier
            .fillMaxSize()
            .gridBackground(c.surface, c.grid)
            .testTag(testTag),
    ) {
        Column(Modifier.background(c.headerBar)) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            DukiHeader {
                DukiText(title, DukiTheme.type.h3, color = c.headerBarInk, maxLines = 1, modifier = Modifier.weight(1f))
                DukiTag(
                    "戻る",
                    color = c.headerBarInk,
                    modifier = Modifier
                        .itemGestures(Unit, onTap = onBack, onLongPress = {})
                        .semantics { contentDescription = "戻る" }
                        .padding(DukiDimens.space2),
                )
            }
        }
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(DukiDimens.space4),
            verticalArrangement = Arrangement.spacedBy(DukiDimens.space5),
        ) {
            content()
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@Composable
private fun SettingLabel(text: String) {
    Spacer(Modifier.height(DukiDimens.space3))
    DukiText(text, DukiTheme.type.bodySm.copy(fontWeight = DukiTheme.type.button.fontWeight))
    Spacer(Modifier.height(DukiDimens.space2))
}

/** 設定画面。 */
@Composable
fun SettingsScreen(
    state: LauncherUiState,
    actions: LauncherActions,
    onOpenHiddenApps: () -> Unit,
    onOpenCatalog: () -> Unit,
    onBack: () -> Unit,
) {
    val s = state.settings
    val c = DukiTheme.colors
    SubScreen(title = "設定", onBack = onBack, testTag = "settings") {
        if (!state.isDefaultHome) {
            DukiCard(inverse = true, modifier = Modifier.fillMaxWidth()) {
                DukiTag(Tags.of("home"), color = c.inverseInk)
                DukiText("既定のホームアプリにしましょう", DukiTheme.type.h3, color = c.inverseInk)
                DukiText(
                    "ホームボタンで dukiLauncher が開くように、端末の設定で選んでください。",
                    DukiTheme.type.bodySm,
                    color = c.inverseInk,
                )
                Spacer(Modifier.height(DukiDimens.space3))
                DukiButton("ホームアプリを選ぶ", onClick = actions::openHomeSettings, style = DukiButtonStyle.Outline)
            }
        }

        DukiCard(Modifier.fillMaxWidth()) {
            DukiTag(Tags.of("display", 1))
            DukiText("表示", DukiTheme.type.h3)
            SettingLabel("テーマ")
            DukiChoiceRow(
                options = listOf(ThemeMode.SYSTEM to "端末に合わせる", ThemeMode.LIGHT to "ライト", ThemeMode.DARK to "ダーク"),
                selected = s.themeMode,
                onSelect = { mode -> actions.updateSettings { it.copy(themeMode = mode) } },
            )
            SettingLabel("アイコンの色")
            DukiChoiceRow(
                options = listOf(true to "モノクロ", false to "元の色"),
                selected = s.monochromeIcons,
                onSelect = { mono -> actions.updateSettings { it.copy(monochromeIcons = mono) } },
            )
            SettingLabel("ホーム画面の列数")
            DukiChoiceRow(
                options = LauncherSettings.COLUMN_CHOICES.map { it to "${it}列" },
                selected = s.gridColumns,
                onSelect = { actions.setGrid(it, s.gridRows) },
            )
            SettingLabel("ホーム画面の段数")
            DukiChoiceRow(
                options = LauncherSettings.ROW_CHOICES.map { it to "${it}段" },
                selected = s.gridRows,
                onSelect = { actions.setGrid(s.gridColumns, it) },
            )
            Spacer(Modifier.height(DukiDimens.space2))
            DukiToggleRow(
                title = "アプリ名を表示する",
                checked = s.showLabels,
                onCheckedChange = { on -> actions.updateSettings { it.copy(showLabels = on) } },
            )
        }

        DukiCard(Modifier.fillMaxWidth()) {
            DukiTag(Tags.of("gesture", 2))
            DukiText("操作", DukiTheme.type.h3)
            SettingLabel("下にスワイプしたとき")
            DukiChoiceRow(
                options = listOf(
                    SwipeDownAction.NOTIFICATIONS to "通知",
                    SwipeDownAction.SEARCH to "検索",
                    SwipeDownAction.NONE to "なし",
                ),
                selected = s.swipeDown,
                onSelect = { a -> actions.updateSettings { it.copy(swipeDown = a) } },
            )
            SettingLabel("ダブルタップしたとき")
            DukiChoiceRow(
                options = listOf(DoubleTapAction.SCREEN_OFF to "画面オフ", DoubleTapAction.NONE to "なし"),
                selected = s.doubleTap,
                onSelect = { a -> actions.updateSettings { it.copy(doubleTap = a) } },
            )
            if (s.doubleTap == DoubleTapAction.SCREEN_OFF && !state.screenOffReady) {
                Spacer(Modifier.height(DukiDimens.space3))
                DukiText(
                    "画面を消すには、ユーザー補助で「dukiLauncher」を有効にしてください。画面の内容は読み取りません。",
                    DukiTheme.type.bodySm,
                    color = c.inkMuted,
                )
                Spacer(Modifier.height(DukiDimens.space2))
                DukiButton("ユーザー補助を開く", onClick = actions::openAccessibilitySettings, style = DukiButtonStyle.Outline)
            }
        }

        DukiCard(Modifier.fillMaxWidth()) {
            DukiTag(Tags.of("apps", 3))
            DukiText("アプリ", DukiTheme.type.h3)
            Spacer(Modifier.height(DukiDimens.space2))
            DukiToggleRow(
                title = "新しいアプリをホーム画面に置く",
                note = "インストールしたアプリを、ホーム画面の空いているところに自動で置きます。",
                checked = s.autoAddNewApps,
                onCheckedChange = { on -> actions.updateSettings { it.copy(autoAddNewApps = on) } },
            )
            Spacer(Modifier.height(DukiDimens.space3))
            DukiButton(
                text = "非表示のアプリ（${state.saved.hidden.size}）",
                onClick = onOpenHiddenApps,
                style = DukiButtonStyle.Outline,
            )
        }

        DukiCard(Modifier.fillMaxWidth()) {
            DukiTag(Tags.of("about", 4))
            DukiText("このアプリについて", DukiTheme.type.h3)
            Spacer(Modifier.height(DukiDimens.space1))
            DukiText("VERSION ${BuildConfig.VERSION_NAME}", DukiTheme.type.spec, color = c.inkMuted)
            DukiText(
                "書体：M PLUS 2・M PLUS 1 Code（SIL Open Font License 1.1）、dukiLab Display。",
                DukiTheme.type.caption,
                color = c.inkMuted,
            )
            Spacer(Modifier.height(DukiDimens.space3))
            Row(horizontalArrangement = Arrangement.spacedBy(DukiDimens.space2)) {
                DukiButton("デザイン部品の一覧", onClick = onOpenCatalog, style = DukiButtonStyle.Outline)
            }
            if (state.isDefaultHome) {
                Spacer(Modifier.height(DukiDimens.space3))
                DukiButton("ホームアプリを選び直す", onClick = actions::openHomeSettings, style = DukiButtonStyle.Outline)
            }
        }
    }
}

/** 非表示にしたアプリの一覧。ここから表示に戻せる。 */
@Composable
fun HiddenAppsScreen(state: LauncherUiState, actions: LauncherActions, onBack: () -> Unit) {
    val c = DukiTheme.colors
    val hidden = AppSearch.sorted(state.hiddenApps)
    SubScreen(title = "非表示のアプリ", onBack = onBack, testTag = "hidden") {
        DukiCard(Modifier.fillMaxWidth()) {
            DukiTag(Tags.of("hidden", hidden.size))
            if (hidden.isEmpty()) {
                DukiText("非表示のアプリはありません", DukiTheme.type.body, color = c.inkMuted)
            }
            hidden.forEach { app -> HiddenRow(app) { actions.unhide(app.key) } }
        }
    }
}

@Composable
private fun HiddenRow(app: AppEntry, onUnhide: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = DukiDimens.space2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DukiDimens.space3),
    ) {
        AppIcon(app, DukiDimens.space7)
        DukiText(app.label, DukiTheme.type.body, maxLines = 1, modifier = Modifier.weight(1f))
        DukiButton("表示する", onClick = onUnhide, style = DukiButtonStyle.Outline)
    }
}
