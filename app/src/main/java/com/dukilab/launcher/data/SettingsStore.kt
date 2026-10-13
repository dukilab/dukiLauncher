package com.dukilab.launcher.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dukilab.launcher.core.Dock
import com.dukilab.launcher.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** 保存する設定。 */
data class LauncherSettings(
    val dock: Dock = Dock(),
    val themeMode: ThemeMode = ThemeMode.System,
    val monochromeIcons: Boolean = false,
)

private val Context.dataStore by preferencesDataStore(name = "launcher")

/** 設定の読み書き（DataStore）。 */
class SettingsStore(private val context: Context) {

    private object Keys {
        val dock = stringPreferencesKey("dock")
        val theme = stringPreferencesKey("theme")
        val monochrome = booleanPreferencesKey("monochrome_icons")
    }

    val settings: Flow<LauncherSettings> = context.dataStore.data.map { p ->
        LauncherSettings(
            dock = Dock.deserialize(p[Keys.dock]),
            themeMode = p[Keys.theme]?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } } ?: ThemeMode.System,
            monochromeIcons = p[Keys.monochrome] ?: false,
        )
    }

    suspend fun updateDock(transform: (Dock) -> Dock) {
        context.dataStore.edit { p ->
            p[Keys.dock] = transform(Dock.deserialize(p[Keys.dock])).serialize()
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.theme] = mode.name }
    }

    suspend fun setMonochromeIcons(enabled: Boolean) {
        context.dataStore.edit { it[Keys.monochrome] = enabled }
    }
}
