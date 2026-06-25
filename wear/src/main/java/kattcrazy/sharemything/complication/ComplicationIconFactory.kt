package kattcrazy.sharemything.complication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.wear.watchface.complications.data.MonochromaticImage
import kattcrazy.sharemything.data.ItemIcon

internal object ComplicationIconFactory {
    private const val ICON_DP = 24f

    fun monochromaticImage(context: Context, icon: ItemIcon): MonochromaticImage =
        monochromaticImage(context, icon.drawableRes())

    fun monochromaticImage(context: Context, @DrawableRes drawableRes: Int): MonochromaticImage {
        val drawable = checkNotNull(ContextCompat.getDrawable(context, drawableRes)).mutate()
        DrawableCompat.setTint(drawable, Color.WHITE)
        val sizePx = (ICON_DP * context.resources.displayMetrics.density).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, sizePx, sizePx)
        drawable.draw(canvas)
        return MonochromaticImage.Builder(
            android.graphics.drawable.Icon.createWithBitmap(bitmap),
        ).build()
    }
}
