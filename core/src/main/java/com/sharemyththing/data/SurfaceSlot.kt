package com.sharemyththing.data

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
    ;

    val isTile: Boolean get() = name.startsWith("TILE_")
    val isComplication: Boolean get() = !isTile

    companion object {
        val all = entries.toList()
        val tiles = all.filter { it.isTile }
        val complications = all.filter { it.isComplication }
        const val maxTiles = 5
        const val maxComplications = 5

        fun fromName(name: String?): SurfaceSlot? =
            name?.let { value -> entries.firstOrNull { it.name == value } }
    }
}
