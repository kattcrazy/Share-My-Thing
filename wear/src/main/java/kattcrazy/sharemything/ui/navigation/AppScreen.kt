package kattcrazy.sharemything.ui.navigation

import kattcrazy.sharemything.data.SurfaceSlot

sealed interface AppScreen {
    data object List : AppScreen

    data class QrDetail(val itemId: Long) : AppScreen

    data class QrTips(val itemId: Long) : AppScreen

    data class TextDetail(val itemId: Long) : AppScreen

    data class Edit(val itemId: Long? = null) : AppScreen

    data object TilesComplications : AppScreen

    data class PickSlotItem(val slot: SurfaceSlot) : AppScreen
}
