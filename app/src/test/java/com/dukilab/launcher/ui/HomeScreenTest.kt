package com.dukilab.launcher.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dukilab.launcher.ui.home.HomeScreen
import com.dukilab.launcher.ui.theme.DukiTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class HomeScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val now = LocalDateTime.of(2026, 10, 13, 6, 5)

    @Test
    fun showsClockAndDate() {
        rule.setContent { DukiTheme { HomeScreen(LauncherUiState(), FakeActions(), now) } }
        rule.onNodeWithTag("clock").assertIsDisplayed()
        rule.onNodeWithText("06:05").assertIsDisplayed()
        rule.onNodeWithText("2026.10.13 TUE").assertIsDisplayed()
    }

    @Test
    fun emptyDockShowsHint() {
        rule.setContent { DukiTheme { HomeScreen(LauncherUiState(), FakeActions(), now) } }
        rule.onNodeWithText("アプリ一覧でアプリを長押しすると、ここに追加できます。").assertIsDisplayed()
    }

    @Test
    fun dockShowsAppsAndLaunchesOnTap() {
        val actions = FakeActions()
        val dock = sampleApps.take(2)
        rule.setContent {
            DukiTheme { HomeScreen(LauncherUiState(dock = dock, dockKeys = dock.map { it.key }.toSet()), actions, now) }
        }
        rule.onNodeWithText("Chrome").performClick()
        assertEquals(listOf("launch:" + sampleApps[1].key), actions.calls)
    }

    @Test
    fun appsButtonOpensDrawer() {
        val actions = FakeActions()
        rule.setContent { DukiTheme { HomeScreen(LauncherUiState(), actions, now) } }
        rule.onNodeWithText("アプリ一覧").performClick()
        assertEquals(listOf("open:Drawer"), actions.calls)
    }

    @Test
    fun notDefaultHomeShowsSetupCard() {
        val actions = FakeActions()
        rule.setContent { DukiTheme { HomeScreen(LauncherUiState(isDefaultHome = false), actions, now) } }
        rule.onNodeWithText("既定のホームアプリにする").performClick()
        assertEquals(listOf("requestHome"), actions.calls)
    }

    @Test
    fun defaultHomeHidesSetupCard() {
        rule.setContent { DukiTheme { HomeScreen(LauncherUiState(isDefaultHome = true), FakeActions(), now) } }
        rule.onNodeWithText("既定のホームアプリにする").assertDoesNotExist()
    }
}
