package kattcrazy.sharemything.data

enum class SurfaceSlot(
    val prefKeySuffix: String,
) {
    TILE_1("tile_1"),
    TILE_2("tile_2"),
    TILE_3("tile_3"),
    TILE_4("tile_4"),
    TILE_5("tile_5"),
    COMPLICATION_1("complication_1"),
    COMPLICATION_2("complication_2"),
    COMPLICATION_3("complication_3"),
    COMPLICATION_4("complication_4"),
    COMPLICATION_5("complication_5"),
    PHONE_WIDGET_1("phone_widget_1"),
    PHONE_WIDGET_2("phone_widget_2"),
    PHONE_WIDGET_3("phone_widget_3"),
    PHONE_WIDGET_4("phone_widget_4"),
    PHONE_WIDGET_5("phone_widget_5"),
    SHORTCUT_1("shortcut_1"),
    SHORTCUT_2("shortcut_2"),
    SHORTCUT_3("shortcut_3"),
    SHORTCUT_4("shortcut_4"),
    SHORTCUT_5("shortcut_5"),
    ;

    val isTile: Boolean get() = name.startsWith("TILE_")
    val isComplication: Boolean get() = name.startsWith("COMPLICATION_")
    val isPhoneWidget: Boolean get() = name.startsWith("PHONE_WIDGET_")
    val isShortcut: Boolean get() = name.startsWith("SHORTCUT_")
    val isWatchSurface: Boolean get() = isTile || isComplication
    val isPhoneOnlySurface: Boolean get() = isPhoneWidget || isShortcut

    companion object {
        val all = entries.toList()
        val tiles = all.filter { it.isTile }
        val complications = all.filter { it.isComplication }
        val phoneWidgets = all.filter { it.isPhoneWidget }
        val phoneShortcuts = all.filter { it.isShortcut }
        val watchSurfaces = all.filter { it.isWatchSurface }
        val syncableSlots = all.filter { !it.isShortcut }
        const val maxTiles = 5
        const val maxComplications = 5
        const val maxPhoneWidgets = 5
        const val maxShortcuts = 5

        fun fromName(name: String?): SurfaceSlot? =
            name?.let { value -> entries.firstOrNull { it.name == value } }
    }
}
