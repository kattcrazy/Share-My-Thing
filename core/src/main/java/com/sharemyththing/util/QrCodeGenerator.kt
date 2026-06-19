package com.sharemyththing.util

import android.graphics.Bitmap
import android.graphics.Color
import android.util.LruCache
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

object QrCodeGenerator {
    private const val MAX_CACHE_ENTRIES = 6

    private val cache = object : LruCache<String, Bitmap>(MAX_CACHE_ENTRIES) {
        override fun entryRemoved(
            evicted: Boolean,
            key: String,
            oldValue: Bitmap,
            newValue: Bitmap?,
        ) {
            if (evicted && !oldValue.isRecycled) {
                oldValue.recycle()
            }
        }
    }

    fun generate(content: String, sizePx: Int = 280): Bitmap {
        val key = cacheKey(content, sizePx)
        synchronized(cache) {
            cache.get(key)?.takeUnless { it.isRecycled }?.let { return it }
            val bitmap = createBitmap(content, sizePx)
            cache.put(key, bitmap)
            return bitmap
        }
    }

    fun invalidateCacheForContent(content: String) {
        synchronized(cache) {
            val suffix = ":$content"
            cache.snapshot().keys.filter { it.endsWith(suffix) }.forEach { cache.remove(it) }
        }
    }

    fun invalidateCache() {
        synchronized(cache) {
            cache.evictAll()
        }
    }

    private fun cacheKey(content: String, sizePx: Int) = "$sizePx:$content"

    private fun createBitmap(content: String, sizePx: Int): Bitmap {
        val hints = mapOf(
            EncodeHintType.MARGIN to 1,
            EncodeHintType.CHARACTER_SET to "UTF-8",
        )
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.RGB_565)
        val pixels = IntArray(sizePx * sizePx)
        for (y in 0 until sizePx) {
            for (x in 0 until sizePx) {
                pixels[y * sizePx + x] = if (matrix[x, y]) Color.BLACK else Color.WHITE
            }
        }
        bitmap.setPixels(pixels, 0, sizePx, 0, 0, sizePx, sizePx)
        return bitmap
    }
}
