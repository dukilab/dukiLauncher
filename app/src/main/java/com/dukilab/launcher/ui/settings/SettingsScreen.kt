package com.dukilab.launcher.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.dukilab.launcher.BuildConfig
import com.dukilab.launcher.R
import com.dukilab.launcher.core.Tags
import com.dukilab.launcher.ui.LauncherActions
import com.dukilab.launcher.ui.LauncherUiState
import com.dukilab.launcher.ui.Screen
import com.dukilab.launcher.ui.components.DukiButton
import com.dukilab.launcher.ui.components.DukiButtonStyle
import com.dukilab.launcher.ui.components.DukiCard
import com.dukilab.launcher.ui.components.DukiHeader
import com.dukilab.launcher.ui.components.DukiHeaderAction
import com.dukilab.launcher.ui.components.DukiTag
import com.dukilab.launcher.ui.theme.Duki
import com.dukilab.launcher.ui.theme.DukiDimens
import com.dukilab.launcher.ui.theme.ThemeMode

/** 設定画面。テーマ・アイコンの見せ方・既定のホームアプリ。 */
@Composable
fun SettingsScreen(state: LauncherUiState, actions: LauncherActions) {
    val c = Duki.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.surface)
            .safeDrawingPadding()
            .testTag("settings"),
    ) {
        DukiHeader {
            DukiHeaderAction("閉じる") { actions.open(Screen.Home) }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(DukiDimens.space4),
            verticalArrangement = Arrangement.spacedBy(DukiDimens.space5),
        ) {
            Column {
                DukiTag(Tags.of("settings"))
                Text("設定", style = Duki.type.h1, color = c.ink)
            }

            DukiCard(modifier = Modifier.fillMaxWidth()) {
                DukiTag(Tags.of("theme", 1))
                Text("テーマ", style = Duki.type.h3, color = c.ink, modifier = Modifier.padding(bottom = DukiDimens.space2))
                ThemeOption("端末の設定に合わせる", ThemeMode.System, state.themeMode, actions)
                ThemeOption("ライト", ThemeMode.Light, state.themeMode, actions)
                ThemeOption("ダーク", ThemeMode.Dark, state.themeMode, actions)
            }

            DukiCard(modifier = Modifier.fillMaxWidth()) {
                DukiTag(Tags.of("icon", 2))
                Text("アイコン", style = Duki.type.h3, color = c.ink, modifier = Modifier.padding(bottom = DukiDimens.space2))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = state.monochromeIcons,
                            role = Role.Checkbox,
                            onValueChange = actions::setMonochromeIcons,
                        )
                        .padding(vertical = DukiDimens.space2)
                        .testTag("monochrome"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DukiDimens.space3),
                ) {
                    SquareMark(selected = state.monochromeIcons)
                    Column {
                        Text("アイコンをモノクロにする", style = Duki.type.body, color = c.ink)
                        Text("アプリのアイコンを白黒で表示します。", style = Duki.type.bodySm, color = c.inkMuted)
                    }
                }
            }

            DukiCard(modifier = Modifier.fillMaxWidth()) {
                DukiTag(Tags.of("home", 3))
                Text("ホームアプリ", style = Duki.type.h3, color = c.ink)
                Text(
                    text = if (state.isDefaultHome) "dukiLauncherが既定のホームアプリになっています。" else "まだ既定のホームアプリになっていません。",
                    style = Duki.type.bodySm,
                    color = c.inkMuted,
                    modifier = Modifier.padding(vertical = DukiDimens.space2),
                )
                DukiButton(text = "ホームアプリを選ぶ", onClick = actions::requestDefaultHome, style = DukiButtonStyle.Outline)
            }

            Column(verticalArrangement = Arrangement.spacedBy(DukiDimens.space2)) {
                Image(
                    painter = painterResource(R.drawable.dukilab_logo),
                    contentDescription = "dukiLab.",
                    colorFilter = ColorFilter.tint(c.logoInk),
                    modifier = Modifier.height(24.dp),
                )
                DukiTag("dukiLauncher · v" + BuildConfig.VERSION_NAME)
            }
        }
    }
}

@Composable
private fun ThemeOption(label: String, mode: ThemeMode, current: ThemeMode, actions: LauncherActions) {
    val c = Duki.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = mode == current, role = Role.RadioButton) { actions.setThemeMode(mode) }
            .padding(vertical = DukiDimens.space2)
            .testTag("theme:" + mode.name),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DukiDimens.space3),
    ) {
        SquareMark(selected = mode == current)
        Text(label, style = Duki.type.body, color = c.ink)
    }
}

/** 選択の印。角丸を使わないので、ラジオもチェックも四角で表す。 */
@Composable
private fun SquareMark(selected: Boolean) {
    val c = Duki.colors
    Box(
        modifier = Modifier
            .size(20.dp)
            .border(DukiDimens.borderStrong, c.lineStrong)
            .padding(5.dp),
    ) {
        if (selected) Box(Modifier.fillMaxSize().background(c.ink))
    }
}
