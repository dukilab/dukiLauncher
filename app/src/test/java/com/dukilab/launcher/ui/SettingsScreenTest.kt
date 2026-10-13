package com.dukilab.launcher.ui

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dukilab.launcher.ui.settings.SettingsScreen
import com.dukilab.launcher.ui.theme.DukiTheme
import com.dukilab.launcher.ui.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class SettingsScreenTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun currentThemeIsSelectedAndChangeIsSent() {
        val actions = FakeActions()
        rule.setContent { DukiTheme { SettingsScreen(LauncherUiState(themeMode = ThemeMode.Light), actions) } }
        rule.onNodeWithTag("theme:Light").assertIsSelected()
        rule.onNodeWithTag("theme:Dark").performClick()
        assertEquals(listOf("theme:Dark"), actions.calls)
    }

    @Test
    fun monochromeToggleIsSent() {
        val actions = FakeActions()
        rule.setContent { DukiTheme { SettingsScreen(LauncherUiState(monochromeIcons = false), actions) } }
        rule.onNodeWithTag("monochrome").performScrollTo().performClick()
        assertEquals(listOf("mono:true"), actions.calls)
    }

    @Test
    fun closeReturnsHome() {
        val actions = FakeActions()
        rule.setContent { DukiTheme { SettingsScreen(LauncherUiState(), actions) } }
        rule.onNodeWithText("閉じる").performClick()
        assertEquals(listOf("open:Home"), actions.calls)
    }
}
