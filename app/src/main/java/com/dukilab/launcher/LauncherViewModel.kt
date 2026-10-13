package com.dukilab.launcher

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.content.pm.LauncherApps
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dukilab.launcher.core.AppEntry
import com.dukilab.launcher.core.AppSearch
import com.dukilab.launcher.core.Dock
import com.dukilab.launcher.data.IconCache
import com.dukilab.launcher.data.InstalledApp
import com.dukilab.launcher.data.InstalledApps
import com.dukilab.launcher.data.LauncherSettings
import com.dukilab.launcher.data.SettingsStore
import com.dukilab.launcher.ui.IconProvider
import com.dukilab.launcher.ui.LauncherUiState
import com.dukilab.launcher.ui.Screen
import com.dukilab.launcher.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** ランチャー全体の状態と操作。 */
class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val installed = InstalledApps(application)
    private val settingsStore = SettingsStore(application)
    private val launcherApps = application.getSystemService(LauncherApps::class.java)
    private val icons = IconCache(
        installed,
        sizePx = (application.resources.displayMetrics.density * 48).toInt().coerceAtLeast(48),
    )

    private val apps = MutableStateFlow<List<InstalledApp>>(emptyList())
    private val query = MutableStateFlow("")
    private val screen = MutableStateFlow(Screen.Home)
    private val isDefaultHome = MutableStateFlow(true)
    private val settings = MutableStateFlow(LauncherSettings())

    init {
        viewModelScope.launch {
            installed.changes().collect { list ->
                icons.clear()
                apps.value = list
                // アンインストールされたアプリはドックからも外す。
                val keys = list.mapTo(HashSet()) { it.entry.key }
                if (list.isNotEmpty() && settings.value.dock.keys.any { it !in keys }) {
                    settingsStore.updateDock { it.retainInstalled(keys) }
                }
            }
        }
        viewModelScope.launch { settingsStore.settings.collect { settings.value = it } }
    }

    val uiState: StateFlow<LauncherUiState> = combine(apps, query, screen, isDefaultHome, settings) { list, q, s, home, st ->
        val entries = list.map { it.entry }
        LauncherUiState(
            apps = entries,
            results = AppSearch.filter(entries, q),
            query = q,
            dock = st.dock.resolve(entries),
            dockFull = st.dock.isFull,
            dockKeys = st.dock.keys.toSet(),
            screen = s,
            isDefaultHome = home,
            themeMode = st.themeMode,
            monochromeIcons = st.monochromeIcons,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, LauncherUiState())

    val iconProvider: IconProvider = object : IconProvider {
        override fun peek(key: String): ImageBitmap? = icons.peek(key)
        override suspend fun load(key: String): ImageBitmap? = find(key)?.let { icons.load(it) }
    }

    private fun find(key: String): InstalledApp? = apps.value.firstOrNull { it.entry.key == key }

    fun setQuery(text: String) { query.value = text }

    fun open(target: Screen) {
        if (target != Screen.Drawer) query.value = ""
        screen.value = target
    }

    /** ホームボタンが押されたとき：開いている一覧や設定を閉じてホームへ戻る。 */
    fun onHomePressed() = open(Screen.Home)

    fun setDefaultHome(held: Boolean) { isDefaultHome.value = held }

    fun launch(entry: AppEntry) {
        val app = find(entry.key) ?: return
        runCatching { launcherApps.startMainActivity(app.component, app.user, null, null) }
        open(Screen.Home)
    }

    /** 検索欄で確定したら、一番上の候補を開く。 */
    fun launchTopResult() {
        uiState.value.results.firstOrNull()?.let(::launch)
    }

    fun openAppInfo(entry: AppEntry) {
        val app = find(entry.key) ?: return
        runCatching { launcherApps.startAppDetailsActivity(app.component, app.user, null, null) }
    }

    fun uninstall(entry: AppEntry) {
        val intent = Intent(Intent.ACTION_DELETE, Uri.fromParts("package", entry.packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { getApplication<Application>().startActivity(intent) }
    }

    fun addToDock(entry: AppEntry) = editDock { it.add(entry.key) }
    fun removeFromDock(entry: AppEntry) = editDock { it.remove(entry.key) }
    fun moveInDock(entry: AppEntry, offset: Int) = editDock { it.move(entry.key, offset) }

    private fun editDock(transform: (Dock) -> Dock) {
        viewModelScope.launch { settingsStore.updateDock(transform) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsStore.setThemeMode(mode) }
    }

    fun setMonochromeIcons(enabled: Boolean) {
        viewModelScope.launch { settingsStore.setMonochromeIcons(enabled) }
    }
}
