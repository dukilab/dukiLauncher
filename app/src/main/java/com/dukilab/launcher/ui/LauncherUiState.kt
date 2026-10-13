package com.dukilab.launcher.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.ImageBitmap
import com.dukilab.launcher.core.AppEntry
import com.dukilab.launcher.ui.theme.ThemeMode

/** いま表示している画面。 */
enum class Screen { Home, Drawer, Settings }

/** 画面に渡す状態。Androidの型を含めず、テストでそのまま作れるようにしている。 */
@Immutable
data class LauncherUiState(
    val apps: List<AppEntry> = emptyList(),
    val results: List<AppEntry> = emptyList(),
    val query: String = "",
    val dock: List<AppEntry> = emptyList(),
    val dockFull: Boolean = false,
    val dockKeys: Set<String> = emptySet(),
    val screen: Screen = Screen.Home,
    val isDefaultHome: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.System,
    val monochromeIcons: Boolean = false,
)

/** 画面からの操作。 */
interface LauncherActions {
    fun open(screen: Screen)
    fun setQuery(text: String)
    fun launch(app: AppEntry)
    fun launchTopResult()
    fun openAppInfo(app: AppEntry)
    fun uninstall(app: AppEntry)
    fun addToDock(app: AppEntry)
    fun removeFromDock(app: AppEntry)
    fun moveInDock(app: AppEntry, offset: Int)
    fun requestDefaultHome()
    fun setThemeMode(mode: ThemeMode)
    fun setMonochromeIcons(enabled: Boolean)
}

/** アイコン画像の取り出し口。テストでは何も返さない（頭文字の四角で代わりに表示）。 */
interface IconProvider {
    fun peek(key: String): ImageBitmap?
    suspend fun load(key: String): ImageBitmap?

    object None : IconProvider {
        override fun peek(key: String): ImageBitmap? = null
        override suspend fun load(key: String): ImageBitmap? = null
    }
}

val LocalIconProvider = staticCompositionLocalOf<IconProvider> { IconProvider.None }
