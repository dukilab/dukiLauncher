package com.dukilab.launcher.data

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import android.os.UserManager
import com.dukilab.launcher.core.AppEntry
import com.dukilab.launcher.core.AppSearch
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

/** 端末に入っているアプリ（ランチャーに出るもの）1件。 */
data class InstalledApp(
    val entry: AppEntry,
    val component: ComponentName,
    val user: UserHandle,
)

/** LauncherApps からアプリ一覧を読み、追加・削除・更新のたびに流し直す。 */
class InstalledApps(context: Context) {

    private val launcherApps = context.getSystemService(LauncherApps::class.java)
    private val userManager = context.getSystemService(UserManager::class.java)
    private val ownPackage = context.packageName

    /** 今の一覧（名前順）。自分自身は出さない。 */
    fun load(): List<InstalledApp> {
        val apps = launcherApps.profiles.flatMap { user ->
            launcherApps.getActivityList(null, user).map { it.toInstalledApp(user) }
        }.filter { it.component.packageName != ownPackage }
        val byKey = apps.associateBy { it.entry.key }
        return AppSearch.sort(apps.map { it.entry }).mapNotNull { byKey[it.key] }
    }

    /** アイコン読み込み用に、キーから LauncherActivityInfo を引く。 */
    fun resolve(app: InstalledApp): LauncherActivityInfo? =
        launcherApps.getActivityList(app.component.packageName, app.user)
            .firstOrNull { it.componentName == app.component }

    fun changes(): Flow<List<InstalledApp>> = callbackFlow {
        val callback = object : LauncherApps.Callback() {
            private fun reload() { trySend(load()) }
            override fun onPackageRemoved(packageName: String?, user: UserHandle?) = reload()
            override fun onPackageAdded(packageName: String?, user: UserHandle?) = reload()
            override fun onPackageChanged(packageName: String?, user: UserHandle?) = reload()
            override fun onPackagesAvailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) = reload()
            override fun onPackagesUnavailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) = reload()
        }
        trySend(load())
        launcherApps.registerCallback(callback, Handler(Looper.getMainLooper()))
        awaitClose { launcherApps.unregisterCallback(callback) }
    }.conflate()

    private fun LauncherActivityInfo.toInstalledApp(user: UserHandle): InstalledApp {
        val serial = userManager.getSerialNumberForUser(user)
        val key = componentName.flattenToShortString() + "#" + serial
        return InstalledApp(
            entry = AppEntry(key = key, label = label?.toString().orEmpty(), packageName = componentName.packageName),
            component = componentName,
            user = user,
        )
    }
}
