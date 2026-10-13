package com.dukilab.launcher

import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dukilab.launcher.core.AppEntry
import com.dukilab.launcher.ui.LauncherActions
import com.dukilab.launcher.ui.LauncherRoot
import com.dukilab.launcher.ui.LocalIconProvider
import com.dukilab.launcher.ui.Screen
import com.dukilab.launcher.ui.theme.DukiTheme
import com.dukilab.launcher.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    private val roleRequest = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        viewModel.setDefaultHome(isDefaultHome())
    }

    private val actions = object : LauncherActions {
        override fun open(screen: Screen) = viewModel.open(screen)
        override fun setQuery(text: String) = viewModel.setQuery(text)
        override fun launch(app: AppEntry) = viewModel.launch(app)
        override fun launchTopResult() = viewModel.launchTopResult()
        override fun openAppInfo(app: AppEntry) = viewModel.openAppInfo(app)
        override fun uninstall(app: AppEntry) = viewModel.uninstall(app)
        override fun addToDock(app: AppEntry) = viewModel.addToDock(app)
        override fun removeFromDock(app: AppEntry) = viewModel.removeFromDock(app)
        override fun moveInDock(app: AppEntry, offset: Int) = viewModel.moveInDock(app, offset)
        override fun requestDefaultHome() = this@MainActivity.requestDefaultHome()
        override fun setThemeMode(mode: ThemeMode) = viewModel.setThemeMode(mode)
        override fun setMonochromeIcons(enabled: Boolean) = viewModel.setMonochromeIcons(enabled)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            DukiTheme(mode = state.themeMode) {
                val dark = when (state.themeMode) {
                    ThemeMode.System -> isSystemInDarkTheme()
                    ThemeMode.Light -> false
                    ThemeMode.Dark -> true
                }
                SideEffect {
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = !dark
                        isAppearanceLightNavigationBars = !dark
                    }
                }
                CompositionLocalProvider(LocalIconProvider provides viewModel.iconProvider) {
                    LauncherRoot(state, actions)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.setDefaultHome(isDefaultHome())
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // ホーム画面を表示中にホームボタンが押されたら、一覧や設定を閉じる。
        if (intent.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_HOME)) {
            viewModel.onHomePressed()
        }
    }

    private fun isDefaultHome(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roles = getSystemService(RoleManager::class.java)
            if (roles != null && roles.isRoleAvailable(RoleManager.ROLE_HOME)) {
                return roles.isRoleHeld(RoleManager.ROLE_HOME)
            }
        }
        val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolved = packageManager.resolveActivity(home, PackageManager.MATCH_DEFAULT_ONLY)
        return resolved?.activityInfo?.packageName == packageName
    }

    private fun requestDefaultHome() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roles = getSystemService(RoleManager::class.java)
            if (roles != null && roles.isRoleAvailable(RoleManager.ROLE_HOME) && !roles.isRoleHeld(RoleManager.ROLE_HOME)) {
                roleRequest.launch(roles.createRequestRoleIntent(RoleManager.ROLE_HOME))
                return
            }
        }
        runCatching { startActivity(Intent(Settings.ACTION_HOME_SETTINGS)) }
    }
}
