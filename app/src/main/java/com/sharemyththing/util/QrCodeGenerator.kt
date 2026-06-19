package com.sharemyththing.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.util.concurrent.ConcurrentHashMap

object QrCodeGenerator {
    private val cache = ConcurrentHashMap<String, Bitmap>()

    fun generate(content: String, sizePx: Int = 280): Bitmap {
        val key = "$sizePx:$content"
        return cache.getOrPut(key) {
            createBitmap(content, sizePx)
        }
    }

    fun invalidateCache() {
        cache.clear()
    }

    private fun createBitmap(content: String, sizePx: Int): Bitmap {
        val hints = mapOf(
            EncodeHintType.MARGIN to 1,
            EncodeHintType.CHARACTER_SET to "UTF-8",
        )
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
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
