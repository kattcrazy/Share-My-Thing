package com.sharemyththing.tile

import android.content.Context
import android.graphics.Bitmap
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ProtoLayoutScope
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.Typography.BODY_LARGE
import androidx.wear.protolayout.material3.Typography.TITLE_MEDIUM
import androidx.wear.protolayout.material3.materialScopeWithResources
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.types.layoutString
import com.sharemyththing.data.DisplayItem
import com.sharemyththing.data.ItemType
import com.sharemyththing.data.SurfaceSlot
import com.sharemyththing.data.usesQr
import com.sharemyththing.presentation.MainActivity
import com.sharemyththing.theme.ShareMyThingColorSchemes
import com.sharemyththing.theme.toProtolayoutColorScheme
import com.sharemyththing.util.QrCodeGenerator

private fun expandSpacer() = LayoutElementBuilders.Spacer.Builder()
    .setWidth(DimensionBuilders.expand())
    .setHeight(DimensionBuilders.expand())
    .build()

internal fun tileResourcesVersion(item: DisplayItem?): String =
    when (item) {
        null -> "empty"
        else -> "${item.id}-${item.type}-${item.title.hashCode()}-${item.content.hashCode()}"
    }

internal fun buildTileResources(item: DisplayItem?): ResourceBuilders.Resources =
    ResourceBuilders.Resources.Builder()
        .setVersion(tileResourcesVersion(item))
        .build()

internal fun buildTileLayout(
    context: Context,
    deviceConfiguration: DeviceParametersBuilders.DeviceParameters,
    protoLayoutScope: ProtoLayoutScope,
    slot: SurfaceSlot,
    item: DisplayItem?,
    configureText: String,
) = materialScopeWithResources(
    context = context,
    protoLayoutScope = protoLayoutScope,
    deviceConfiguration = deviceConfiguration,
    allowDynamicTheme = false,
    defaultColorScheme = ShareMyThingColorSchemes.watchDark.toProtolayoutColorScheme(),
) {
    when (item?.type) {
        ItemType.QR_CODE -> {
            if (item.content.isNotBlank()) {
                qrTileLayout(context, deviceConfiguration, item)
            } else {
                textTileLayout(context, deviceConfiguration, item)
            }
        }

        ItemType.BOTH -> {
            if (item.content.isNotBlank()) {
                bothTileLayout(context, deviceConfiguration, item)
            } else {
                textTileLayout(context, deviceConfiguration, item)
            }
        }

        null -> configureTileLayout(context, slot, configureText)

        ItemType.TEXT -> textTileLayout(context, deviceConfiguration, item)
    }
}

/** Matches [com.sharemyththing.ui.detail.QrDetailScreen]: white QR block + centered title. No content or edit button. */
private fun MaterialScope.qrTileLayout(
    context: Context,
    deviceConfiguration: DeviceParametersBuilders.DeviceParameters,
    item: DisplayItem,
) = LayoutElementBuilders.Column.Builder()
    .setWidth(DimensionBuilders.expand())
    .setHeight(DimensionBuilders.expand())
    .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
    .setModifiers(openItemModifiers(context, item))
    .addContent(qrWhiteSection(deviceConfiguration, item))
    .addContent(titleSection(item.title))
    .build()

/** QR code, title, and truncated content for [ItemType.BOTH] tiles. */
private fun MaterialScope.bothTileLayout(
    context: Context,
    deviceConfiguration: DeviceParametersBuilders.DeviceParameters,
    item: DisplayItem,
) = LayoutElementBuilders.Column.Builder()
    .setWidth(DimensionBuilders.expand())
    .setHeight(DimensionBuilders.expand())
    .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
    .setModifiers(openItemModifiers(context, item))
    .addContent(qrWhiteSection(deviceConfiguration, item, BOTH_QR_WIDTH_FRACTION))
    .addContent(titleSection(item.title))
    .addContent(
        LayoutElementBuilders.Column.Builder()
            .setWidth(DimensionBuilders.expand())
            .setHeight(DimensionBuilders.wrap())
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
            .setModifiers(
                ModifiersBuilders.Modifiers.Builder()
                    .setPadding(
                        ModifiersBuilders.Padding.Builder()
                            .setTop(DimensionBuilders.dp(TEXT_CONTENT_TOP_PADDING_DP))
                            .setStart(DimensionBuilders.dp(TILE_TEXT_HORIZONTAL_PADDING_DP))
                            .setEnd(DimensionBuilders.dp(TILE_TEXT_HORIZONTAL_PADDING_DP))
                            .build(),
                    )
                    .build(),
            )
            .addContent(
                tileBodyText(
                    value = item.content,
                    maxLines = bothTileContentMaxLines(deviceConfiguration.screenHeightDp.toFloat()),
                ),
            )
            .build(),
    )
    .build()

/** Matches [com.sharemyththing.ui.detail.TextDetailScreen]: centered title + content. No edit button. */
private fun MaterialScope.textTileLayout(
    context: Context,
    deviceConfiguration: DeviceParametersBuilders.DeviceParameters,
    item: DisplayItem,
) = LayoutElementBuilders.Column.Builder()
    .setWidth(DimensionBuilders.expand())
    .setHeight(DimensionBuilders.expand())
    .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
    .setModifiers(openItemModifiers(context, item))
    .addContent(
        LayoutElementBuilders.Box.Builder()
            .setWidth(DimensionBuilders.expand())
            .setHeight(DimensionBuilders.expand())
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
            .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
            .addContent(textTileContent(deviceConfiguration, item))
            .build(),
    )
    .build()

private fun MaterialScope.textTileContent(
    deviceConfiguration: DeviceParametersBuilders.DeviceParameters,
    item: DisplayItem,
) = LayoutElementBuilders.Column.Builder()
    .setWidth(DimensionBuilders.expand())
    .setHeight(DimensionBuilders.wrap())
    .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
    .setModifiers(horizontalTextPadding())
    .addContent(tileTitleText(item.title))
    .addContent(
        LayoutElementBuilders.Column.Builder()
            .setWidth(DimensionBuilders.expand())
            .setHeight(DimensionBuilders.wrap())
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
            .setModifiers(
                ModifiersBuilders.Modifiers.Builder()
                    .setPadding(
                        ModifiersBuilders.Padding.Builder()
                            .setTop(DimensionBuilders.dp(TEXT_CONTENT_TOP_PADDING_DP))
                            .build(),
                    )
                    .build(),
            )
            .addContent(
                tileBodyText(
                    value = item.content,
                    maxLines = textContentMaxLines(deviceConfiguration.screenHeightDp.toFloat()),
                ),
            )
            .build(),
    )
    .build()

private fun MaterialScope.configureTileLayout(
    context: Context,
    slot: SurfaceSlot,
    configureText: String,
) = LayoutElementBuilders.Column.Builder()
    .setWidth(DimensionBuilders.expand())
    .setHeight(DimensionBuilders.expand())
    .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
    .setModifiers(openSlotModifiers(context, slot))
    .addContent(expandSpacer())
    .addContent(
        text(
            text = configureText.layoutString,
            typography = BODY_LARGE,
            maxLines = 4,
            overflow = LayoutElementBuilders.TEXT_OVERFLOW_TRUNCATE,
        ),
    )
    .addContent(expandSpacer())
    .build()

private fun MaterialScope.qrWhiteSection(
    deviceConfiguration: DeviceParametersBuilders.DeviceParameters,
    item: DisplayItem,
    widthFraction: Float = QR_IN_APP_WIDTH_FRACTION,
): LayoutElementBuilders.Column {
    val qrSizeDp = deviceConfiguration.screenWidthDp * widthFraction
    val imageSizeDp = (qrSizeDp - QR_IMAGE_INNER_PADDING_DP * 2f).coerceAtLeast(48f)
    val columnBuilder = LayoutElementBuilders.Column.Builder()
        .setWidth(DimensionBuilders.expand())
        .setHeight(DimensionBuilders.wrap())
        .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
        .setModifiers(
            ModifiersBuilders.Modifiers.Builder()
                .setBackground(
                    ModifiersBuilders.Background.Builder()
                        .setColor(ColorBuilders.argb(0xFFFFFFFF.toInt()))
                        .build(),
                )
                .setPadding(
                    ModifiersBuilders.Padding.Builder()
                        .setTop(DimensionBuilders.dp(QR_WHITE_TOP_PADDING_DP))
                        .setStart(DimensionBuilders.dp(QR_WHITE_HORIZONTAL_PADDING_DP))
                        .setEnd(DimensionBuilders.dp(QR_WHITE_HORIZONTAL_PADDING_DP))
                        .setBottom(DimensionBuilders.dp(QR_WHITE_BOTTOM_PADDING_DP))
                        .build(),
                )
                .build(),
        )

    if (item.type.usesQr && item.content.isNotBlank()) {
        val imageResource = runCatching {
            QrCodeGenerator.generate(item.content, QR_TILE_SIZE_PX)?.toInlineImageResource()
        }.getOrNull()
        if (imageResource != null) {
            columnBuilder.addContent(
                LayoutElementBuilders.Image.Builder(protoLayoutScope)
                    .setWidth(DimensionBuilders.dp(imageSizeDp))
                    .setHeight(DimensionBuilders.dp(imageSizeDp))
                    .setContentScaleMode(LayoutElementBuilders.CONTENT_SCALE_MODE_FIT)
                    .setImageResource(imageResource)
                    .build(),
            )
        }
    }

    return columnBuilder.build()
}

private fun MaterialScope.titleSection(title: String) =
    LayoutElementBuilders.Column.Builder()
        .setWidth(DimensionBuilders.expand())
        .setHeight(DimensionBuilders.wrap())
        .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
        .setModifiers(
            ModifiersBuilders.Modifiers.Builder()
                .setPadding(
                    ModifiersBuilders.Padding.Builder()
                        .setTop(DimensionBuilders.dp(TITLE_TOP_PADDING_DP))
                        .setStart(DimensionBuilders.dp(TILE_TEXT_HORIZONTAL_PADDING_DP))
                        .setEnd(DimensionBuilders.dp(TILE_TEXT_HORIZONTAL_PADDING_DP))
                        .build(),
                )
                .build(),
        )
        .addContent(tileTitleText(title))
        .build()

private fun MaterialScope.tileTitleText(title: String) = text(
    text = title.layoutString,
    typography = TITLE_MEDIUM,
    maxLines = 2,
    overflow = LayoutElementBuilders.TEXT_OVERFLOW_TRUNCATE,
)

private fun MaterialScope.tileBodyText(value: String, maxLines: Int) = text(
    text = value.layoutString,
    typography = BODY_LARGE,
    maxLines = maxLines,
    overflow = LayoutElementBuilders.TEXT_OVERFLOW_TRUNCATE,
)

private fun horizontalTextPadding(): ModifiersBuilders.Modifiers =
    ModifiersBuilders.Modifiers.Builder()
        .setPadding(
            ModifiersBuilders.Padding.Builder()
                .setStart(DimensionBuilders.dp(TILE_TEXT_HORIZONTAL_PADDING_DP))
                .setEnd(DimensionBuilders.dp(TILE_TEXT_HORIZONTAL_PADDING_DP))
                .build(),
        )
        .build()

private fun textContentMaxLines(screenHeightDp: Float): Int {
    val reservedDp = 72f
    val lineHeightDp = 20f
    return ((screenHeightDp - reservedDp) / lineHeightDp).toInt().coerceIn(4, 10)
}

private fun bothTileContentMaxLines(screenHeightDp: Float): Int {
    val reservedDp = 140f
    val lineHeightDp = 20f
    return ((screenHeightDp - reservedDp) / lineHeightDp).toInt().coerceIn(2, 5)
}

private fun openSlotModifiers(
    context: Context,
    slot: SurfaceSlot,
): ModifiersBuilders.Modifiers =
    ModifiersBuilders.Modifiers.Builder()
        .setClickable(
            ModifiersBuilders.Clickable.Builder()
                .setOnClick(
                    ActionBuilders.LaunchAction.Builder()
                        .setAndroidActivity(
                            ActionBuilders.AndroidActivity.Builder()
                                .setPackageName(context.packageName)
                                .setClassName(MainActivity::class.java.name)
                                .addKeyToExtraMapping(
                                    MainActivity.EXTRA_SURFACE_SLOT,
                                    ActionBuilders.stringExtra(slot.name),
                                )
                                .build(),
                        )
                        .build(),
                )
                .build(),
        )
        .build()

private fun openItemModifiers(
    context: Context,
    item: DisplayItem,
): ModifiersBuilders.Modifiers =
    ModifiersBuilders.Modifiers.Builder()
        .setClickable(
            ModifiersBuilders.Clickable.Builder()
                .setOnClick(
                    ActionBuilders.LaunchAction.Builder()
                        .setAndroidActivity(
                            ActionBuilders.AndroidActivity.Builder()
                                .setPackageName(context.packageName)
                                .setClassName(MainActivity::class.java.name)
                                .addKeyToExtraMapping(
                                    MainActivity.EXTRA_ITEM_ID,
                                    ActionBuilders.longExtra(item.id),
                                )
                                .build(),
                        )
                        .build(),
                )
                .build(),
        )
        .build()

private fun Bitmap.toInlineImageResource(): ResourceBuilders.ImageResource {
    val width = width
    val height = height
    val pixelCount = width * height
    val buffer = ByteArray(pixelCount * 4)
    val pixels = IntArray(pixelCount)
    getPixels(pixels, 0, width, 0, 0, width, height)
    for (index in 0 until pixelCount) {
        val pixel = pixels[index]
        val offset = index * 4
        buffer[offset] = ((pixel shr 16) and 0xFF).toByte()
        buffer[offset + 1] = ((pixel shr 8) and 0xFF).toByte()
        buffer[offset + 2] = (pixel and 0xFF).toByte()
        buffer[offset + 3] = ((pixel shr 24) and 0xFF).toByte()
    }
    return ResourceBuilders.ImageResource.Builder()
        .setInlineResource(
            ResourceBuilders.InlineImageResource.Builder()
                .setData(buffer)
                .setWidthPx(width)
                .setHeightPx(height)
                .setFormat(ResourceBuilders.IMAGE_FORMAT_ARGB_8888)
                .build(),
        )
        .build()
}

private const val QR_TILE_SIZE_PX = 280
private const val QR_IN_APP_WIDTH_FRACTION = 0.72f
private const val BOTH_QR_WIDTH_FRACTION = 0.52f
private const val QR_IMAGE_INNER_PADDING_DP = 8f
private const val QR_WHITE_TOP_PADDING_DP = 28f
private const val QR_WHITE_HORIZONTAL_PADDING_DP = 24f
private const val QR_WHITE_BOTTOM_PADDING_DP = 10f
private const val TITLE_TOP_PADDING_DP = 8f
private const val TEXT_CONTENT_TOP_PADDING_DP = 8f
private const val TILE_TEXT_HORIZONTAL_PADDING_DP = 12f
