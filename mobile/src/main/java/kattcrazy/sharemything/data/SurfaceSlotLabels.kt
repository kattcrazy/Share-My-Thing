package kattcrazy.sharemything.data

import androidx.annotation.StringRes
import kattcrazy.sharemything.R

val SurfaceSlot.labelRes: Int
    @StringRes get() = when (this) {
        SurfaceSlot.TILE_1 -> R.string.tile_1
        SurfaceSlot.TILE_2 -> R.string.tile_2
        SurfaceSlot.TILE_3 -> R.string.tile_3
        SurfaceSlot.TILE_4 -> R.string.tile_4
        SurfaceSlot.TILE_5 -> R.string.tile_5
        SurfaceSlot.COMPLICATION_1 -> R.string.complication_1
        SurfaceSlot.COMPLICATION_2 -> R.string.complication_2
        SurfaceSlot.COMPLICATION_3 -> R.string.complication_3
        SurfaceSlot.COMPLICATION_4 -> R.string.complication_4
        SurfaceSlot.COMPLICATION_5 -> R.string.complication_5
        SurfaceSlot.PHONE_WIDGET_1 -> R.string.phone_widget_1
        SurfaceSlot.PHONE_WIDGET_2 -> R.string.phone_widget_2
        SurfaceSlot.PHONE_WIDGET_3 -> R.string.phone_widget_3
        SurfaceSlot.PHONE_WIDGET_4 -> R.string.phone_widget_4
        SurfaceSlot.PHONE_WIDGET_5 -> R.string.phone_widget_5
        SurfaceSlot.SHORTCUT_1 -> R.string.shortcut_1
        SurfaceSlot.SHORTCUT_2 -> R.string.shortcut_2
        SurfaceSlot.SHORTCUT_3 -> R.string.shortcut_3
        SurfaceSlot.SHORTCUT_4 -> R.string.shortcut_4
        SurfaceSlot.SHORTCUT_5 -> R.string.shortcut_5
    }
