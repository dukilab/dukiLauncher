package com.dukilab.launcher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dukilab.launcher.ui.components.rememberReduceMotion
import com.dukilab.launcher.ui.drawer.AppDrawer
import com.dukilab.launcher.ui.home.HomeScreen
import com.dukilab.launcher.ui.settings.SettingsScreen

/** ホームの上に、アプリ一覧と設定を重ねて出す。戻るボタンでホームに戻る。 */
@Composable
fun LauncherRoot(state: LauncherUiState, actions: LauncherActions) {
    BackHandler(enabled = state.screen != Screen.Home) { actions.open(Screen.Home) }
    val reduceMotion = rememberReduceMotion()
    Box(Modifier.fillMaxSize()) {
        HomeScreen(state, actions)
        AnimatedVisibility(
            visible = state.screen == Screen.Drawer,
            enter = if (reduceMotion) EnterTransition.None else slideInVertically { it / 3 } + fadeIn(),
            exit = if (reduceMotion) ExitTransition.None else slideOutVertically { it / 3 } + fadeOut(),
        ) {
            AppDrawer(state, actions)
        }
        AnimatedVisibility(
            visible = state.screen == Screen.Settings,
            enter = if (reduceMotion) EnterTransition.None else fadeIn(),
            exit = if (reduceMotion) ExitTransition.None else fadeOut(),
        ) {
            SettingsScreen(state, actions)
        }
    }
}
