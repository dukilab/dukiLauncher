package com.dukilab.launcher.ui

import com.dukilab.launcher.core.AppEntry
import com.dukilab.launcher.ui.theme.ThemeMode

/** 呼ばれた操作を記録するだけの LauncherActions。 */
class FakeActions : LauncherActions {
    val calls = mutableListOf<String>()
    var onQuery: (String) -> Unit = {}

    override fun open(screen: Screen) { calls += "open:$screen" }
    override fun setQuery(text: String) { calls += "query:$text"; onQuery(text) }
    override fun launch(app: AppEntry) { calls += "launch:${app.key}" }
    override fun launchTopResult() { calls += "launchTop" }
    override fun openAppInfo(app: AppEntry) { calls += "info:${app.key}" }
    override fun uninstall(app: AppEntry) { calls += "uninstall:${app.key}" }
    override fun addToDock(app: AppEntry) { calls += "dockAdd:${app.key}" }
    override fun removeFromDock(app: AppEntry) { calls += "dockRemove:${app.key}" }
    override fun moveInDock(app: AppEntry, offset: Int) { calls += "dockMove:${app.key}:$offset" }
    override fun requestDefaultHome() { calls += "requestHome" }
    override fun setThemeMode(mode: ThemeMode) { calls += "theme:$mode" }
    override fun setMonochromeIcons(enabled: Boolean) { calls += "mono:$enabled" }
}

val sampleApps = listOf(
    AppEntry("com.android.camera/.Camera#0", "カメラ", "com.android.camera"),
    AppEntry("com.android.chrome/.Main#0", "Chrome", "com.android.chrome"),
    AppEntry("com.google.android.deskclock/.Clock#0", "時計", "com.google.android.deskclock"),
    AppEntry("com.android.settings/.Settings#0", "設定", "com.android.settings"),
)
