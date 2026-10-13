package com.dukilab.launcher.ui.home

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.dukilab.launcher.core.ClockText
import com.dukilab.launcher.core.Dock
import com.dukilab.launcher.core.Tags
import com.dukilab.launcher.ui.LauncherActions
import com.dukilab.launcher.ui.LauncherUiState
import com.dukilab.launcher.ui.Screen
import com.dukilab.launcher.ui.components.AppMenuItem
import com.dukilab.launcher.ui.components.AppTile
import com.dukilab.launcher.ui.components.DukiButton
import com.dukilab.launcher.ui.components.DukiButtonStyle
import com.dukilab.launcher.ui.components.DukiCard
import com.dukilab.launcher.ui.components.DukiTag
import com.dukilab.launcher.ui.components.gridBackground
import com.dukilab.launcher.ui.theme.Duki
import com.dukilab.launcher.ui.theme.DukiDimens
import kotlinx.coroutines.delay
import java.time.LocalDateTime

/** 今の時刻。分が変わるたびに更新する。 */
@Composable
fun rememberNow(clock: () -> LocalDateTime = LocalDateTime::now): LocalDateTime {
    val now by produceState(initialValue = clock()) {
        while (true) {
            delay(ClockText.millisUntilNextMinute(value))
            value = clock()
        }
    }
    return now
}

/**
 * ホーム画面。方眼の地に、時計のカード・ドック・アプリ一覧ボタン。
 * 上にスワイプしてもアプリ一覧が開く。
 */
@Composable
fun HomeScreen(
    state: LauncherUiState,
    actions: LauncherActions,
    now: LocalDateTime = rememberNow(),
) {
    val c = Duki.colors
    val threshold = with(LocalDensity.current) { 64.dp.toPx() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .gridBackground(c.surface, c.grid, DukiDimens.gridCell)
            .pointerInput(Unit) {
                var total = 0f
                detectVerticalDragGestures(
                    onDragStart = { total = 0f },
                    onVerticalDrag = { _, dy -> total += dy },
                    onDragEnd = { if (total < -threshold) actions.open(Screen.Drawer) },
                )
            }
            .safeDrawingPadding()
            .padding(DukiDimens.space4)
            .testTag("home"),
        verticalArrangement = Arrangement.spacedBy(DukiDimens.space5),
    ) {
        ClockCard(now)
        if (!state.isDefaultHome) DefaultHomeCard(onClick = actions::requestDefaultHome)
        Spacer(Modifier.weight(1f))
        DockCard(state, actions)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DukiDimens.space4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DukiButton(text = "アプリ一覧", onClick = { actions.open(Screen.Drawer) })
            DukiButton(text = "設定", onClick = { actions.open(Screen.Settings) }, style = DukiButtonStyle.Outline)
        }
    }
}

@Composable
private fun ClockCard(now: LocalDateTime) {
    val c = Duki.colors
    DukiCard(modifier = Modifier.fillMaxWidth()) {
        DukiTag(Tags.of("home"))
        Text(
            text = ClockText.time(now),
            style = Duki.type.display,
            color = c.ink,
            modifier = Modifier.padding(top = DukiDimens.space2).testTag("clock"),
        )
        Text(text = ClockText.date(now), style = Duki.type.spec, color = c.inkMuted)
    }
}

@Composable
private fun DefaultHomeCard(onClick: () -> Unit) {
    val c = Duki.colors
    DukiCard(modifier = Modifier.fillMaxWidth()) {
        DukiTag(Tags.of("setup"))
        Text(
            text = "既定のホームアプリにすると、ホームボタンでこの画面が開くようになります。",
            style = Duki.type.bodySm,
            color = c.ink,
            modifier = Modifier.padding(vertical = DukiDimens.space2),
        )
        DukiButton(text = "既定のホームアプリにする", onClick = onClick, style = DukiButtonStyle.Outline)
    }
}

@Composable
private fun DockCard(state: LauncherUiState, actions: LauncherActions) {
    val c = Duki.colors
    DukiCard(
        modifier = Modifier.fillMaxWidth().testTag("dock"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = DukiDimens.space4, end = DukiDimens.space4,
            top = DukiDimens.space4, bottom = DukiDimens.space3,
        ),
    ) {
        DukiTag(Tags.of("dock", state.dock.size) + " / " + Dock.MAX_SIZE.toString().padStart(3, '0'))
        if (state.dock.isEmpty()) {
            Text(
                text = "アプリ一覧でアプリを長押しすると、ここに追加できます。",
                style = Duki.type.bodySm,
                color = c.inkMuted,
                modifier = Modifier.padding(vertical = DukiDimens.space3),
            )
        } else {
            Row(modifier = Modifier.fillMaxWidth().padding(top = DukiDimens.space2)) {
                state.dock.forEachIndexed { index, app ->
                    AppTile(
                        app = app,
                        monochrome = state.monochromeIcons,
                        onClick = { actions.launch(app) },
                        modifier = Modifier.weight(1f),
                        menu = {
                            listOf(
                                AppMenuItem("左へ動かす", enabled = index > 0) { actions.moveInDock(app, -1) },
                                AppMenuItem("右へ動かす", enabled = index < state.dock.lastIndex) { actions.moveInDock(app, 1) },
                                AppMenuItem("ドックから外す") { actions.removeFromDock(app) },
                                AppMenuItem("アプリ情報") { actions.openAppInfo(app) },
                            )
                        },
                    )
                }
                // 空きの分も幅をとって、アイコンの位置をそろえる。
                repeat(Dock.MAX_SIZE - state.dock.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}
