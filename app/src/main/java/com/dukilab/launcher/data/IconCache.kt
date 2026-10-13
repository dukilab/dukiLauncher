package com.dukilab.launcher.data

import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** アプリアイコンを画像にして覚えておく。読み込みは裏のスレッドで行う。 */
class IconCache(private val installed: InstalledApps, private val sizePx: Int) {

    private val cache = LruCache<String, ImageBitmap>(256)

    fun peek(key: String): ImageBitmap? = cache.get(key)

    suspend fun load(app: InstalledApp): ImageBitmap? {
        cache.get(app.entry.key)?.let { return it }
        return withContext(Dispatchers.IO) {
            val info = installed.resolve(app) ?: return@withContext null
            val drawable = info.getBadgedIcon(0) ?: return@withContext null
            drawable.toBitmap(sizePx, sizePx).asImageBitmap().also { cache.put(app.entry.key, it) }
        }
    }

    /** アプリが更新されたらアイコンも変わることがあるので捨てる。 */
    fun clear() = cache.evictAll()
}
