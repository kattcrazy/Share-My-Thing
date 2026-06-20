package com.sharemyththing.widget

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.sharemyththing.R
import com.sharemyththing.data.DisplayItem
import com.sharemyththing.data.ItemType
import com.sharemyththing.data.SurfaceSlot
import com.sharemyththing.data.usesQr
import com.sharemyththing.presentation.MainActivity
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
    val colors: WidgetColors,
)

@Composable
internal fun PhoneWidgetContent(state: PhoneWidgetState) {
    val context = androidx.glance.LocalContext.current
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
            .background(state.colors.background)
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
                            color = state.colors.onSurfaceVariant,
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
                    colors = state.colors,
                )

                ItemType.BOTH -> BothWidgetLayout(
                    title = item.title,
                    content = item.content,
                    qrBitmap = state.qrBitmap,
                    colors = state.colors,
                )

                ItemType.TEXT -> TextWidgetLayout(
                    title = item.title,
                    content = item.content,
                    colors = state.colors,
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
    val widgetSize = LocalSize.current
    val qrHeight = min(widgetSize.width.value, widgetSize.height.value * 0.72f).dp
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(qrHeight)
                .background(ColorProvider(Color.White)),
            contentAlignment = Alignment.Center,
        ) {
            if (qrBitmap != null) {
                Image(
                    provider = ImageProvider(qrBitmap),
                    contentDescription = null,
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(6.dp),
                    contentScale = ContentScale.Fit,
                )
            }
        }
        TitleText(
            title = title,
            colors = colors,
            modifier = GlanceModifier.padding(horizontal = 10.dp, vertical = 8.dp),
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
    val widgetSize = LocalSize.current
    val qrHeight = min(widgetSize.width.value / 1.4f, widgetSize.height.value * 0.42f).dp
    Column(modifier = GlanceModifier.fillMaxSize()) {
        if (qrBitmap != null && content.isNotBlank()) {
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(qrHeight)
                    .background(ColorProvider(Color.White)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    provider = ImageProvider(qrBitmap),
                    contentDescription = null,
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(4.dp),
                    contentScale = ContentScale.Fit,
                )
            }
        }
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            TitleText(title = title, colors = colors)
            if (content.isNotBlank()) {
                BodyText(
                    value = content,
                    colors = colors,
                    maxLines = 4,
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
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        TitleText(title = title, colors = colors)
        if (content.isNotBlank()) {
            BodyText(value = content, colors = colors, maxLines = 8)
        }
    }
}

@Composable
private fun TitleText(
    title: String,
    colors: WidgetColors,
    modifier: GlanceModifier = GlanceModifier,
) {
    Text(
        text = breakForWidgetWrap(title),
        modifier = modifier.fillMaxWidth(),
        style = TextStyle(
            color = colors.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        ),
        maxLines = 2,
    )
}

@Composable
private fun BodyText(
    value: String,
    colors: WidgetColors,
    maxLines: Int,
    modifier: GlanceModifier = GlanceModifier,
) {
    Text(
        text = breakForWidgetWrap(value),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        style = TextStyle(
            color = colors.onSurfaceVariant,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
        ),
        maxLines = maxLines,
    )
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

internal fun widgetColorsFor(context: Context): WidgetColors {
    val isDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
        Configuration.UI_MODE_NIGHT_YES
    return if (isDark) {
        WidgetColors(
            background = ColorProvider(Color(0xFF1B1B1F)),
            onSurface = ColorProvider(Color(0xFFE3E2E6)),
            onSurfaceVariant = ColorProvider(Color(0xFFC4C6D0)),
        )
    } else {
        WidgetColors(
            background = ColorProvider(Color(0xFFFEFBFF)),
            onSurface = ColorProvider(Color(0xFF1B1B1F)),
            onSurfaceVariant = ColorProvider(Color(0xFF44474E)),
        )
    }
}

internal suspend fun loadPhoneWidgetState(
    context: Context,
    slot: SurfaceSlot,
): PhoneWidgetState {
    val app = context.applicationContext as com.sharemyththing.ShareMyThingApplication
    val itemId = app.repository.surfacePreferences.getItemId(slot)
    val item = itemId?.let { app.repository.getItem(it) }
    val qrBitmap = if (item != null && item.type.usesQr && item.content.isNotBlank()) {
        runCatching {
            com.sharemyththing.util.QrCodeGenerator.generate(item.content, QR_WIDGET_SIZE_PX)
        }.getOrNull()
    } else {
        null
    }
    return PhoneWidgetState(
        slot = slot,
        item = item,
        qrBitmap = qrBitmap,
        configureText = context.getString(R.string.widget_configure),
        colors = widgetColorsFor(context),
    )
}

private const val QR_WIDGET_SIZE_PX = 512
