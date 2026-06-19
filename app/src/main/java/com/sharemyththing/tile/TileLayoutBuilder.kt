package com.sharemyththing.tile

import android.content.Context
import android.graphics.Bitmap
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.Typography.BODY_LARGE
import androidx.wear.protolayout.material3.Typography.BODY_MEDIUM
import androidx.wear.protolayout.material3.Typography.TITLE_SMALL
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.types.layoutString
import com.sharemyththing.data.DisplayItem
import com.sharemyththing.data.ItemType
import com.sharemyththing.presentation.MainActivity
import com.sharemyththing.util.QrCodeGenerator

internal const val QR_IMAGE_RESOURCE_ID = "qr_image"

private const val WRAPPING_MAX_LINES = 100

private fun MaterialScope.wrappingText(
    value: String,
    typography: Int,
) = text(
    text = value.layoutString,
    typography = typography,
    maxLines = WRAPPING_MAX_LINES,
    overflow = LayoutElementBuilders.TEXT_OVERFLOW_TRUNCATE,
)

internal fun tileResourcesVersion(item: DisplayItem?): String =
    when (item) {
        null -> "empty"
        else -> "${item.id}-${item.type}-${item.title.hashCode()}-${item.content.hashCode()}"
    }

internal fun buildTileResources(item: DisplayItem?): ResourceBuilders.Resources {
    val builder = ResourceBuilders.Resources.Builder()
        .setVersion(tileResourcesVersion(item))

    if (item?.type == ItemType.QR_CODE) {
        val bitmap = runCatching {
            QrCodeGenerator.generate(item.content, QR_TILE_SIZE_PX)
        }.getOrNull()
        if (bitmap != null) {
            builder.addIdToImageMapping(QR_IMAGE_RESOURCE_ID, bitmap.toInlineImageResource())
        }
    }

    return builder.build()
}

internal fun buildTileLayout(
    context: Context,
    deviceConfiguration: DeviceParametersBuilders.DeviceParameters,
    item: DisplayItem?,
    configureText: String,
) = materialScope(context, deviceConfiguration) {
    when (item?.type) {
        ItemType.QR_CODE -> qrTileLayout(context, deviceConfiguration, item)

        else -> primaryLayout(
            mainSlot = {
                when (item) {
                    null -> wrappingText(configureText, BODY_LARGE)
                    else -> textItemColumn(item, openItemModifiers(context, item))
                }
            },
        )
    }
}

private fun MaterialScope.qrTileLayout(
    context: Context,
    deviceConfiguration: DeviceParametersBuilders.DeviceParameters,
    item: DisplayItem,
) = LayoutElementBuilders.Column.Builder()
    .setWidth(DimensionBuilders.expand())
    .setHeight(DimensionBuilders.expand())
    .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
    .setModifiers(openItemModifiers(context, item))
    .addContent(qrImageSection(deviceConfiguration))
    .addContent(
        LayoutElementBuilders.Column.Builder()
            .setWidth(DimensionBuilders.expand())
            .setHeight(DimensionBuilders.wrap())
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
            .setModifiers(textSectionPadding())
            .addContent(wrappingText(item.title, TITLE_SMALL))
            .addContent(wrappingText(item.content, BODY_MEDIUM))
            .build(),
    )
    .build()

private fun MaterialScope.textItemColumn(
    item: DisplayItem,
    clickableModifiers: ModifiersBuilders.Modifiers,
) = LayoutElementBuilders.Column.Builder()
    .setWidth(DimensionBuilders.expand())
    .setHeight(DimensionBuilders.wrap())
    .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
    .setModifiers(clickableModifiers)
    .addContent(wrappingText(item.title, TITLE_SMALL))
    .addContent(wrappingText(item.content, BODY_LARGE))
    .build()

private fun qrImageSizeDp(
    deviceConfiguration: DeviceParametersBuilders.DeviceParameters,
): Float {
    val maxByWidth = deviceConfiguration.screenWidthDp * QR_TILE_WIDTH_FRACTION
    val maxByHeight = deviceConfiguration.screenHeightDp * QR_TILE_MAX_HEIGHT_FRACTION
    return minOf(maxByWidth, maxByHeight)
}

private fun qrImageSection(
    deviceConfiguration: DeviceParametersBuilders.DeviceParameters,
): LayoutElementBuilders.Box {
    val qrSizeDp = qrImageSizeDp(deviceConfiguration)
    return LayoutElementBuilders.Box.Builder()
        .setWidth(DimensionBuilders.expand())
        .setHeight(DimensionBuilders.wrap())
        .setModifiers(
            ModifiersBuilders.Modifiers.Builder()
                .setBackground(
                    ModifiersBuilders.Background.Builder()
                        .setColor(ColorBuilders.argb(0xFFFFFFFF.toInt()))
                        .build(),
                )
                .setPadding(
                    ModifiersBuilders.Padding.Builder()
                        .setTop(DimensionBuilders.dp(QR_TILE_VERTICAL_PADDING_DP))
                        .setBottom(DimensionBuilders.dp(QR_TILE_VERTICAL_PADDING_DP))
                        .build(),
                )
                .build(),
        )
        .addContent(
            LayoutElementBuilders.Image.Builder()
                .setWidth(DimensionBuilders.dp(qrSizeDp))
                .setHeight(DimensionBuilders.dp(qrSizeDp))
                .setContentScaleMode(LayoutElementBuilders.CONTENT_SCALE_MODE_FIT)
                .setResourceId(QR_IMAGE_RESOURCE_ID)
                .build(),
        )
        .build()
}

private fun textSectionPadding(): ModifiersBuilders.Modifiers =
    ModifiersBuilders.Modifiers.Builder()
        .setPadding(
            ModifiersBuilders.Padding.Builder()
                .setStart(DimensionBuilders.dp(TILE_TEXT_HORIZONTAL_PADDING_DP))
                .setEnd(DimensionBuilders.dp(TILE_TEXT_HORIZONTAL_PADDING_DP))
                .setTop(DimensionBuilders.dp(TILE_TEXT_TOP_PADDING_DP))
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
private const val QR_TILE_WIDTH_FRACTION = 0.72f
private const val QR_TILE_MAX_HEIGHT_FRACTION = 0.38f
private const val QR_TILE_VERTICAL_PADDING_DP = 8f
private const val TILE_TEXT_HORIZONTAL_PADDING_DP = 8f
private const val TILE_TEXT_TOP_PADDING_DP = 4f
