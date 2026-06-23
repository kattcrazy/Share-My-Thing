package kattcrazy.sharemything.widget

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.GlanceTheme
import androidx.glance.unit.ColorProvider
import kattcrazy.sharemything.R
import kattcrazy.sharemything.data.DisplayItem
import kattcrazy.sharemything.data.ItemType
import kattcrazy.sharemything.data.SurfaceSlot
import kattcrazy.sharemything.data.usesQr
import kattcrazy.sharemything.presentation.MainActivity
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
internal data class WidgetColors(
    val background: ColorProvider,
    val onSurface: ColorProvider,
    val onSurfaceVariant: ColorProvider,
)

internal data class PhoneWidgetState(
    val slot: SurfaceSlot,
    val item: DisplayItem?,
    val qrBitmap: Bitmap?,
    val configureText: String,
)

private data class WidgetLayoutMetrics(
    val titleFontSp: Float,
    val bodyFontSp: Float,
    val titleMaxLines: Int,
    val bodyMaxLines: Int,
    val titleVerticalPadding: Dp,
)

@Composable
internal fun PhoneWidgetContent(state: PhoneWidgetState) {
    val context = LocalContext.current
    val colors = widgetColors()
    val clickModifier = when (val item = state.item) {
        null -> GlanceModifier.clickable(
            actionStartActivity(
                MainActivity.launchIntentForSlot(context, state.slot),
            ),
        )
        else -> GlanceModifier.clickable(
            actionStartActivity(MainActivity.launchIntent(context, item.id)),
        )
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .fillMaxWidth()
            .background(colors.background)
            .then(clickModifier),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    ) {
        when (val item = state.item) {
            null -> {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.configureText,
                        style = TextStyle(
                            color = colors.onSurfaceVariant,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                        ),
                        maxLines = 4,
                        modifier = GlanceModifier.padding(12.dp),
                    )
                }
            }

            else -> when (item.type) {
                ItemType.QR_CODE -> QrCodeWidgetLayout(
                    title = item.title,
                    qrBitmap = state.qrBitmap,
                    colors = colors,
                )

                ItemType.BOTH -> BothWidgetLayout(
                    title = item.title,
                    content = item.content,
                    qrBitmap = state.qrBitmap,
                    colors = colors,
                )

                ItemType.TEXT -> TextWidgetLayout(
                    title = item.title,
                    content = item.content,
                    colors = colors,
                )
            }
        }
    }
}

@Composable
private fun QrCodeWidgetLayout(
    title: String,
    qrBitmap: Bitmap?,
    colors: WidgetColors,
) {
    val metrics = widgetLayoutMetrics(includeBody = false)
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .fillMaxWidth(),
    ) {
        QrWhiteBlock(
            qrBitmap = qrBitmap,
        )
        TitleBand(
            title = title,
            colors = colors,
            fontSizeSp = metrics.titleFontSp,
            maxLines = metrics.titleMaxLines,
            verticalPadding = metrics.titleVerticalPadding,
        )
    }
}

@Composable
private fun BothWidgetLayout(
    title: String,
    content: String,
    qrBitmap: Bitmap?,
    colors: WidgetColors,
) {
    val metrics = widgetLayoutMetrics(
        includeBody = content.isNotBlank(),
        hasQrBlock = qrBitmap != null && content.isNotBlank(),
    )
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .fillMaxWidth(),
    ) {
        if (qrBitmap != null && content.isNotBlank()) {
            QrWhiteBlock(
                qrBitmap = qrBitmap,
            )
        }
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(
                    horizontal = 10.dp,
                    vertical = metrics.titleVerticalPadding,
                ),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            verticalAlignment = Alignment.Vertical.CenterVertically,
        ) {
            CenteredTitleText(
                title = title,
                colors = colors,
                fontSizeSp = metrics.titleFontSp,
                maxLines = metrics.titleMaxLines,
            )
            if (content.isNotBlank()) {
                CenteredBodyText(
                    value = content,
                    colors = colors,
                    fontSizeSp = metrics.bodyFontSp,
                    maxLines = metrics.bodyMaxLines,
                )
            }
        }
    }
}

@Composable
private fun TextWidgetLayout(
    title: String,
    content: String,
    colors: WidgetColors,
) {
    val metrics = widgetLayoutMetrics(includeBody = content.isNotBlank())
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        CenteredTitleText(
            title = title,
            colors = colors,
            fontSizeSp = metrics.titleFontSp,
            maxLines = metrics.titleMaxLines,
        )
        if (content.isNotBlank()) {
            CenteredBodyText(
                value = content,
                colors = colors,
                fontSizeSp = metrics.bodyFontSp,
                maxLines = metrics.bodyMaxLines,
            )
        }
    }
}

@Composable
private fun ColumnScope.QrWhiteBlock(
    qrBitmap: Bitmap?,
) {
    Box(
        modifier = GlanceModifier
            .defaultWeight()
            .fillMaxWidth()
            .background(ColorProvider(Color.White)),
        contentAlignment = Alignment.Center,
    ) {
        if (qrBitmap != null) {
            Image(
                provider = ImageProvider(qrBitmap),
                contentDescription = null,
                modifier = GlanceModifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
private fun TitleBand(
    title: String,
    colors: WidgetColors,
    fontSizeSp: Float,
    maxLines: Int,
    verticalPadding: Dp,
) {
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = verticalPadding),
        contentAlignment = Alignment.Center,
    ) {
        CenteredTitleText(
            title = title,
            colors = colors,
            fontSizeSp = fontSizeSp,
            maxLines = maxLines,
        )
    }
}

@Composable
private fun widgetLayoutMetrics(
    includeBody: Boolean,
    hasQrBlock: Boolean = false,
): WidgetLayoutMetrics {
    val size = LocalSize.current
    return computeWidgetLayoutMetrics(
        widthDp = size.width.value,
        heightDp = size.height.value,
        includeBody = includeBody,
        hasQrBlock = hasQrBlock,
    )
}

private fun computeWidgetLayoutMetrics(
    widthDp: Float,
    heightDp: Float,
    includeBody: Boolean,
    hasQrBlock: Boolean = false,
): WidgetLayoutMetrics {
    val safeHeight = heightDp.coerceAtLeast(110f)
    val compact = safeHeight < 120f
    val veryCompact = safeHeight < 100f
    val titleMaxLines = if (compact) 1 else 2
    val titleFontSp = when {
        veryCompact -> 9f
        compact -> 10f
        else -> (safeHeight * 0.038f + 9f).coerceIn(10f, 15f)
    }
    val bodyFontSp = when {
        veryCompact -> 8f
        compact -> 9f
        else -> (safeHeight * 0.032f + 8f).coerceIn(9f, 13f)
    }

    val titleVerticalPadding = when {
        veryCompact -> 2.dp
        compact -> 4.dp
        else -> 6.dp
    }

    val bodyMaxLines = if (!includeBody) {
        0
    } else {
        fitBodyMaxLines(
            heightDp = safeHeight,
            widthDp = widthDp,
            titleMaxLines = titleMaxLines,
            titleFontSp = titleFontSp,
            bodyFontSp = bodyFontSp,
            titleVerticalPaddingDp = titleVerticalPadding.value,
            hasQrBlock = hasQrBlock,
        )
    }

    return WidgetLayoutMetrics(
        titleFontSp = titleFontSp,
        bodyFontSp = bodyFontSp,
        titleMaxLines = titleMaxLines,
        bodyMaxLines = bodyMaxLines,
        titleVerticalPadding = titleVerticalPadding,
    )
}

private fun fitBodyMaxLines(
    heightDp: Float,
    widthDp: Float,
    titleMaxLines: Int,
    titleFontSp: Float,
    bodyFontSp: Float,
    titleVerticalPaddingDp: Float,
    hasQrBlock: Boolean,
): Int {
    val titleLineHeight = titleFontSp * LINE_HEIGHT_MULTIPLIER
    val bodyLineHeight = bodyFontSp * LINE_HEIGHT_MULTIPLIER
    val titleBlockHeight = titleMaxLines * titleLineHeight + 2f

    val reservedHeight = if (hasQrBlock) {
        val minQrHeight = min(widthDp, heightDp * 0.55f).coerceAtLeast(48f)
        minQrHeight + titleBlockHeight + titleVerticalPaddingDp * 2f
    } else {
        titleBlockHeight + TEXT_WIDGET_VERTICAL_PADDING_DP * 2f
    }

    val remaining = heightDp - reservedHeight
    if (remaining <= 0f) return 1
    return max(1, min(MAX_BODY_LINES, floor(remaining / bodyLineHeight).toInt()))
}

private const val LINE_HEIGHT_MULTIPLIER = 1.35f
private const val TEXT_WIDGET_VERTICAL_PADDING_DP = 8f
private const val MAX_BODY_LINES = 64

@Composable
private fun CenteredTitleText(
    title: String,
    colors: WidgetColors,
    fontSizeSp: Float,
    maxLines: Int,
    modifier: GlanceModifier = GlanceModifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = breakForWidgetWrap(title),
            style = TextStyle(
                color = colors.onSurface,
                fontSize = fontSizeSp.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            ),
            maxLines = maxLines,
        )
    }
}

@Composable
private fun CenteredBodyText(
    value: String,
    colors: WidgetColors,
    fontSizeSp: Float,
    maxLines: Int,
    modifier: GlanceModifier = GlanceModifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = breakForWidgetWrap(value),
            style = TextStyle(
                color = colors.onSurfaceVariant,
                fontSize = fontSizeSp.sp,
                textAlign = TextAlign.Center,
            ),
            maxLines = maxLines,
        )
    }
}

internal fun breakForWidgetWrap(text: String): String {
    if (text.isBlank()) return text
    val breakAfter = setOf('/', '-', '.', ':', '@', '?', '&', '=', '_')
    return buildString(text.length + text.length / 4) {
        text.forEachIndexed { index, char ->
            if (index > 0 && char in breakAfter) {
                append('\u200B')
            }
            append(char)
        }
    }
}

@Composable
private fun widgetColors(): WidgetColors =
    WidgetColors(
        background = GlanceTheme.colors.widgetBackground,
        onSurface = GlanceTheme.colors.onSurface,
        onSurfaceVariant = GlanceTheme.colors.onSurfaceVariant,
    )

internal suspend fun loadPhoneWidgetState(
    context: Context,
    slot: SurfaceSlot,
): PhoneWidgetState {
    val app = context.applicationContext as kattcrazy.sharemything.ShareMyThingApplication
    val itemId = app.repository.surfacePreferences.getItemId(slot)
    val item = itemId?.let { app.repository.getItem(it) }
    val qrBitmap = if (item != null && item.type.usesQr && item.content.isNotBlank()) {
        runCatching {
            kattcrazy.sharemything.util.QrCodeGenerator.generate(item.content, QR_WIDGET_SIZE_PX)
        }.getOrNull()
    } else {
        null
    }
    return PhoneWidgetState(
        slot = slot,
        item = item,
        qrBitmap = qrBitmap,
        configureText = context.getString(R.string.widget_configure),
    )
}

private const val QR_WIDGET_SIZE_PX = 512
