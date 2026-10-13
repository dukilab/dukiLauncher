package com.dukilab.launcher.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dukilab.launcher.core.AppSearch
import com.dukilab.launcher.ui.drawer.AppDrawer
import com.dukilab.launcher.ui.theme.DukiTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class AppDrawerTest {

    @get:Rule
    val rule = createComposeRule()

    private val all = AppSearch.sort(sampleApps)
    private val base = LauncherUiState(apps = all, results = all, screen = Screen.Drawer)

    @Test
    fun showsAllAppsAndCount() {
        rule.setContent { DukiTheme { AppDrawer(base, FakeActions()) } }
        rule.onNodeWithText("APPS · 004").assertIsDisplayed()
        rule.onNodeWithText("カメラ").assertIsDisplayed()
        rule.onNodeWithText("Chrome").assertIsDisplayed()
    }

    @Test
    fun searchFiltersWithHiragana() {
        val actions = FakeActions()
        rule.setContent {
            var state by remember { mutableStateOf(base) }
            actions.onQuery = { q -> state = state.copy(query = q, results = AppSearch.filter(all, q)) }
            DukiTheme { AppDrawer(state, actions) }
        }
        rule.onNodeWithTag("search").performTextInput("かめ")
        rule.onNodeWithText("カメラ").assertIsDisplayed()
        rule.onNodeWithText("Chrome").assertDoesNotExist()
    }

    @Test
    fun noResultShowsMessage() {
        val state = base.copy(query = "zzz", results = emptyList())
        rule.setContent { DukiTheme { AppDrawer(state, FakeActions()) } }
        rule.onNodeWithText("「zzz」に一致するアプリが見つかりませんでした。").assertIsDisplayed()
    }

    @Test
    fun tapLaunchesApp() {
        val actions = FakeActions()
        rule.setContent { DukiTheme { AppDrawer(base, actions) } }
        rule.onNodeWithText("時計").performClick()
        assertEquals(listOf("launch:" + sampleApps[2].key), actions.calls)
    }

    @Test
    fun longPressMenuAddsToDock() {
        val actions = FakeActions()
        val chrome = sampleApps[1]
        rule.setContent { DukiTheme { AppDrawer(base, actions) } }
        rule.onNodeWithTag("app:" + chrome.key).performTouchInput { longClick() }
        rule.onNodeWithText("アプリ情報").assertIsDisplayed()
        rule.onNodeWithText("アンインストール").assertIsDisplayed()
        rule.onNodeWithText("ドックに追加").performClick()
        assertEquals(listOf("dockAdd:" + chrome.key), actions.calls)
    }

    @Test
    fun longPressMenuOffersRemoveWhenInDock() {
        val actions = FakeActions()
        val chrome = sampleApps[1]
        rule.setContent { DukiTheme { AppDrawer(base.copy(dockKeys = setOf(chrome.key)), actions) } }
        rule.onNodeWithTag("app:" + chrome.key).performTouchInput { longClick() }
        rule.onNodeWithText("ドックから外す").performClick()
        assertTrue(actions.calls.contains("dockRemove:" + chrome.key))
    }

    @Test
    fun fullDockDisablesAdd() {
        rule.setContent { DukiTheme { AppDrawer(base.copy(dockFull = true), FakeActions()) } }
        rule.onNodeWithTag("app:" + sampleApps[0].key).performTouchInput { longClick() }
        rule.onNodeWithText("ドックがいっぱいです（最大5個）").assertIsNotEnabled()
    }

    @Test
    fun closeReturnsHome() {
        val actions = FakeActions()
        rule.setContent { DukiTheme { AppDrawer(base, actions) } }
        rule.onNodeWithText("閉じる").performClick()
        assertEquals(listOf("open:Home"), actions.calls)
    }
}
