package kattcrazy.sharemything.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Icon
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import kattcrazy.sharemything.data.ItemIcon
import kattcrazy.sharemything.theme.ShareMyThingColorSchemes

object ItemIconBitmap {
    private const val SHORTCUT_SIZE_DP = 48f
    private const val ICON_INSET_FRACTION = 0.22f
    private const val CORNER_RADIUS_FRACTION = 0.2f

    fun shortcutIcon(context: Context, icon: ItemIcon): Icon =
        Icon.createWithAdaptiveBitmap(renderShortcutBitmap(context, icon.drawableRes()))

    fun renderShortcutBitmap(context: Context, icon: ItemIcon): Bitmap =
        renderShortcutBitmap(context, icon.drawableRes())

    fun renderShortcutBitmap(context: Context, @DrawableRes drawableRes: Int): Bitmap {
        val density = context.resources.displayMetrics.density
        val sizePx = (SHORTCUT_SIZE_DP * density).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ShareMyThingColorSchemes.BrandBlue.toArgb()
        }
        val cornerRadius = sizePx * CORNER_RADIUS_FRACTION
        canvas.drawRoundRect(
            0f,
            0f,
            sizePx.toFloat(),
            sizePx.toFloat(),
            cornerRadius,
            cornerRadius,
            backgroundPaint,
        )

        val drawable = checkNotNull(ContextCompat.getDrawable(context, drawableRes)).mutate()
        DrawableCompat.setTint(drawable, Color.WHITE)
        val inset = (sizePx * ICON_INSET_FRACTION).toInt()
        drawable.setBounds(inset, inset, sizePx - inset, sizePx - inset)
        drawable.draw(canvas)
        return bitmap
    }

    fun renderMonochromeBitmap(context: Context, @DrawableRes drawableRes: Int, sizeDp: Float = 24f): Bitmap {
        val drawable = checkNotNull(ContextCompat.getDrawable(context, drawableRes)).mutate()
        DrawableCompat.setTint(drawable, Color.WHITE)
        val sizePx = (sizeDp * context.resources.displayMetrics.density).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, sizePx, sizePx)
        drawable.draw(canvas)
        return bitmap
    }

    private fun androidx.compose.ui.graphics.Color.toArgb(): Int =
        android.graphics.Color.argb(
            (alpha * 255f).toInt(),
            (red * 255f).toInt(),
            (green * 255f).toInt(),
            (blue * 255f).toInt(),
        )
}
