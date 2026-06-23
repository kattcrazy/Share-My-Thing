package kattcrazy.sharemything.data

object SurfaceDisplayConstants {
    const val QR_TILE_SIZE_PX = 280
    const val QR_IN_APP_WIDTH_FRACTION = 0.72f
    const val BOTH_QR_WIDTH_FRACTION = 0.52f
    const val QR_IMAGE_INNER_PADDING_DP = 8f
    const val QR_WHITE_TOP_PADDING_DP = 28f
    const val QR_WHITE_HORIZONTAL_PADDING_DP = 24f
    const val QR_WHITE_BOTTOM_PADDING_DP = 10f
    const val TITLE_TOP_PADDING_DP = 8f
    const val TEXT_CONTENT_TOP_PADDING_DP = 8f
    const val TILE_TEXT_HORIZONTAL_PADDING_DP = 12f

    fun textContentMaxLines(screenHeightDp: Float): Int {
        val reservedDp = 72f
        val lineHeightDp = 20f
        return ((screenHeightDp - reservedDp) / lineHeightDp).toInt().coerceIn(4, 10)
    }

    fun bothTileContentMaxLines(screenHeightDp: Float): Int {
        val reservedDp = 140f
        val lineHeightDp = 20f
        return ((screenHeightDp - reservedDp) / lineHeightDp).toInt().coerceIn(2, 5)
    }
}
