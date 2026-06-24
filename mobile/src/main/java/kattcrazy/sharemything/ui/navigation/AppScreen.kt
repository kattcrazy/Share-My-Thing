package kattcrazy.sharemything.ui.navigation

import kattcrazy.sharemything.data.SurfaceSlot

sealed interface AppScreen {
    data object List : AppScreen

    data object About : AppScreen

    data class QrDetail(val itemId: Long) : AppScreen

    data object QrTips : AppScreen

    data class TextDetail(val itemId: Long) : AppScreen

    data class Edit(val itemId: Long? = null) : AppScreen

    data object PhoneWidgets : AppScreen

    data object AppShortcuts : AppScreen

    data object WatchTiles : AppScreen

    data object WatchComplications : AppScreen

    data class PickSlotItem(val slot: SurfaceSlot) : AppScreen
}
