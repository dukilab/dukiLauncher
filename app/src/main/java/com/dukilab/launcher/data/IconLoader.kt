package com.dukilab.launcher.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.LruCache
import androidx.compose.ui.graphics.asImageBitmap
import com.dukilab.launcher.ui.components.AppIconImage
import com.dukilab.launcher.ui.components.IconSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * アプリアイコンを正方形のビットマップにする。角丸やマスクは付けない（dukiLab. は直角）。
 *
 * モノクロ表示では、Android 13 以降のテーマアイコン（monochrome レイヤー）があれば
 * それを型抜き用に使い、なければ元のアイコンを渡して画面側で白黒にする。
 */
class IconLoader(context: Context, private val repo: AppRepository) : IconSource {
    private val appContext = context.applicationContext
    private val cache = LruCache<String, AppIconImage>(256)

    override suspend fun icon(key: String, sizePx: Int, monochrome: Boolean): AppIconImage? {
        val cacheKey = "$key|$sizePx|$monochrome"
        cache.get(cacheKey)?.let { return it }
        return withContext(Dispatchers.IO) {
            val info = repo.info(key) ?: return@withContext null
            val density = appContext.resources.displayMetrics.densityDpi
            val drawable = runCatching { info.getIcon(density) }.getOrNull() ?: return@withContext null
            val image = render(drawable, sizePx, monochrome, repo.isMainUser(key), info.user)
            cache.put(cacheKey, image)
            image
        }
    }

    /** パッケージが更新されたときに、そのアプリのアイコンを読み直させる。 */
    fun clear() = cache.evictAll()

    private fun render(
        drawable: Drawable,
        size: Int,
        monochrome: Boolean,
        mainUser: Boolean,
        user: android.os.UserHandle,
    ): AppIconImage {
        if (monochrome && drawable is AdaptiveIconDrawable && Build.VERSION.SDK_INT >= 33) {
            val mono = drawable.monochrome
            if (mono != null) {
                return AppIconImage(drawLayers(size, listOf(mono)).asImageBitmap(), isMask = true)
            }
        }
        var bitmap = if (drawable is AdaptiveIconDrawable) {
            drawLayers(size, listOfNotNull(drawable.background, drawable.foreground))
        } else {
            drawPlain(drawable, size)
        }
        if (!mainUser) {
            val badged = appContext.packageManager.getUserBadgedIcon(BitmapDrawable(appContext.resources, bitmap), user)
            bitmap = drawPlain(badged, size)
        }
        return AppIconImage(bitmap.asImageBitmap(), isMask = false)
    }

    /** アダプティブアイコンの層を、見える範囲（中央72/108）がちょうど正方形に収まるよう描く。 */
    private fun drawLayers(size: Int, layers: List<Drawable>): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val inset = size / 4
        for (layer in layers) {
            layer.setBounds(-inset, -inset, size + inset, size + inset)
            layer.draw(canvas)
        }
        return bitmap
    }

    private fun drawPlain(drawable: Drawable, size: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, size, size)
        drawable.draw(canvas)
        return bitmap
    }

}
