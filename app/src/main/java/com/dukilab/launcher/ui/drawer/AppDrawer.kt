package com.dukilab.launcher.ui.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.dukilab.launcher.core.AppEntry
import com.dukilab.launcher.core.Tags
import com.dukilab.launcher.ui.LauncherActions
import com.dukilab.launcher.ui.LauncherUiState
import com.dukilab.launcher.ui.Screen
import com.dukilab.launcher.ui.components.AppMenuItem
import com.dukilab.launcher.ui.components.AppTile
import com.dukilab.launcher.ui.components.DukiSearchField
import com.dukilab.launcher.ui.components.DukiTag
import com.dukilab.launcher.ui.theme.Duki
import com.dukilab.launcher.ui.theme.DukiDimens

/** アプリ一覧。上に検索欄、下にアイコンのグリッド。見出しを下にスワイプすると閉じる。 */
@Composable
fun AppDrawer(state: LauncherUiState, actions: LauncherActions) {
    val c = Duki.colors
    val threshold = with(LocalDensity.current) { 64.dp.toPx() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.surface)
            .safeDrawingPadding()
            .testTag("drawer"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    var total = 0f
                    detectVerticalDragGestures(
                        onDragStart = { total = 0f },
                        onVerticalDrag = { _, dy -> total += dy },
                        onDragEnd = { if (total > threshold) actions.open(Screen.Home) },
                    )
                }
                .padding(start = DukiDimens.space4, end = DukiDimens.space4, top = DukiDimens.space4),
            verticalArrangement = Arrangement.spacedBy(DukiDimens.space3),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    DukiTag(Tags.of("apps", state.apps.size))
                    Text("アプリ一覧", style = Duki.type.h3, color = c.ink)
                }
                Text(
                    text = "閉じる",
                    style = Duki.type.spec,
                    color = c.ink,
                    modifier = Modifier
                        .clickable(role = Role.Button) { actions.open(Screen.Home) }
                        .padding(DukiDimens.space2),
                )
            }
            DukiSearchField(
                value = state.query,
                onValueChange = actions::setQuery,
                placeholder = "アプリを検索",
                onSearch = actions::launchTopResult,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (state.results.isEmpty()) {
            Text(
                text = if (state.query.isBlank()) "アプリを読み込んでいます。" else "「${state.query.trim()}」に一致するアプリが見つかりませんでした。",
                style = Duki.type.bodySm,
                color = c.inkMuted,
                modifier = Modifier.padding(DukiDimens.space4).testTag("empty"),
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 76.dp),
                contentPadding = PaddingValues(DukiDimens.space3),
                modifier = Modifier.fillMaxSize().testTag("grid"),
            ) {
                items(state.results, key = { it.key }) { app ->
                    AppTile(
                        app = app,
                        monochrome = state.monochromeIcons,
                        onClick = { actions.launch(app) },
                        menu = { drawerMenu(app, state, actions) },
                    )
                }
            }
        }
    }
}

private fun drawerMenu(app: AppEntry, state: LauncherUiState, actions: LauncherActions): List<AppMenuItem> {
    val inDock = app.key in state.dockKeys
    val dockItem = when {
        inDock -> AppMenuItem("ドックから外す") { actions.removeFromDock(app) }
        state.dockFull -> AppMenuItem("ドックがいっぱいです（最大5個）", enabled = false) {}
        else -> AppMenuItem("ドックに追加") { actions.addToDock(app) }
    }
    return listOf(
        dockItem,
        AppMenuItem("アプリ情報") { actions.openAppInfo(app) },
        AppMenuItem("アンインストール") { actions.uninstall(app) },
    )
}
