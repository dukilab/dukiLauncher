package com.dukilab.launcher.ui

import androidx.compose.runtime.Immutable
import com.dukilab.launcher.core.AppEntry
import com.dukilab.launcher.core.Cell
import com.dukilab.launcher.core.LauncherSettings
import com.dukilab.launcher.core.SavedState

/** 画面に渡す状態。 */
@Immutable
data class LauncherUiState(
    /** 入っているアプリ（非表示にしたものも含む）。キーで引ける。 */
    val apps: Map<String, AppEntry> = emptyMap(),
    val saved: SavedState = SavedState(),
    val settings: LauncherSettings = LauncherSettings(),
    /** アプリ一覧を読み込み終えたか。 */
    val loaded: Boolean = false,
    /** このアプリが既定のホームアプリか。 */
    val isDefaultHome: Boolean = true,
    /** 画面オフ用のユーザー補助サービスが有効か。 */
    val screenOffReady: Boolean = false,
) {
    /** アプリ一覧に出すもの（非表示を除く）。 */
    val drawerApps: List<AppEntry>
        get() = apps.values.filter { it.key !in saved.hidden }

    val hiddenApps: List<AppEntry>
        get() = saved.hidden.mapNotNull { apps[it] }

    fun app(key: String): AppEntry? = apps[key]
}

/** 画面から呼ぶ操作。本物は [com.dukilab.launcher.LauncherViewModel]、テストでは偽物を使う。 */
interface LauncherActions {
    fun launch(appKey: String)
    fun openAppInfo(appKey: String)
    fun uninstall(appKey: String)

    fun addToHome(appKey: String, page: Int)
    fun removeFromHome(appKey: String)
    fun removeHomeCell(cell: Cell)
    fun moveHomeItem(from: Cell, to: Cell)
    fun moveToDock(from: Cell)
    fun renameFolder(folderId: String, name: String)
    fun takeOutOfFolder(folderId: String, appKey: String)
    fun dissolveFolder(folderId: String)

    fun addToDock(appKey: String)
    fun removeFromDock(appKey: String)
    fun moveInDock(from: Int, to: Int)

    fun hide(appKey: String)
    fun unhide(appKey: String)

    fun updateSettings(transform: (LauncherSettings) -> LauncherSettings)
    fun setGrid(columns: Int, rows: Int)

    fun expandNotifications()
    fun lockScreen()
    fun openHomeSettings()
    fun openAccessibilitySettings()
}
