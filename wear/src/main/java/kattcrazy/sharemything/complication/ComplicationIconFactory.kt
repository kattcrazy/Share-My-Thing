package kattcrazy.sharemything.complication

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.wear.watchface.complications.data.MonochromaticImage
import kattcrazy.sharemything.data.ItemIcon
import kattcrazy.sharemything.ui.ItemIconBitmap

internal object ComplicationIconFactory {
    private const val ICON_DP = 24f

    fun monochromaticImage(context: Context, icon: ItemIcon): MonochromaticImage =
        monochromaticImage(context, icon.drawableRes())

    fun monochromaticImage(context: Context, @DrawableRes drawableRes: Int): MonochromaticImage {
        val bitmap = ItemIconBitmap.renderMonochromeBitmap(context, drawableRes, ICON_DP)
        return MonochromaticImage.Builder(
            android.graphics.drawable.Icon.createWithBitmap(bitmap),
        ).build()
    }

    fun smallImage(context: Context, icon: ItemIcon) =
        smallImage(context, icon.drawableRes())

    fun smallImage(context: Context, @DrawableRes drawableRes: Int): androidx.wear.watchface.complications.data.SmallImage {
        val bitmap = ItemIconBitmap.renderMonochromeBitmap(context, drawableRes, ICON_DP)
        return androidx.wear.watchface.complications.data.SmallImage.Builder(
            image = android.graphics.drawable.Icon.createWithBitmap(bitmap),
            type = androidx.wear.watchface.complications.data.SmallImageType.ICON,
        ).build()
    }
}
