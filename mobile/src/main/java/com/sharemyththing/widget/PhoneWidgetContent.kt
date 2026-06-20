package com.sharemyththing.widget

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.sharemyththing.R
import com.sharemyththing.data.DisplayItem
import com.sharemyththing.data.ItemType
import com.sharemyththing.data.SurfaceSlot
import com.sharemyththing.data.usesQr
import com.sharemyththing.presentation.MainActivity

internal data class PhoneWidgetState(
    val slot: SurfaceSlot,
    val item: DisplayItem?,
    val qrBitmap: Bitmap?,
    val configureText: String,
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
            .background(ColorProvider(Color(0xFF1C1B1F)))
            .then(clickModifier)
            .padding(8.dp),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        when (val item = state.item) {
            null -> {
                Text(
                    text = state.configureText,
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 14.sp,
                    ),
                    maxLines = 4,
                )
            }

            else -> when (item.type) {
                ItemType.QR_CODE -> {
                    if (item.content.isNotBlank() && state.qrBitmap != null) {
                        QrSection(state.qrBitmap)
                        TitleText(item.title)
                    } else {
                        TitleText(item.title)
                        BodyText(item.content)
                    }
                }

                ItemType.BOTH -> {
                    if (item.content.isNotBlank() && state.qrBitmap != null) {
                        QrSection(state.qrBitmap, compact = true)
                        TitleText(item.title)
                        BodyText(item.content, maxLines = 3)
                    } else {
                        TitleText(item.title)
                        BodyText(item.content)
                    }
                }

                ItemType.TEXT -> {
                    TitleText(item.title)
                    BodyText(item.content)
                }
            }
        }
    }
}

@Composable
private fun QrSection(bitmap: Bitmap, compact: Boolean = false) {
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(ColorProvider(Color.White))
            .padding(if (compact) 4.dp else 8.dp),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    ) {
        Image(
            provider = ImageProvider(bitmap),
            contentDescription = null,
            modifier = GlanceModifier.fillMaxWidth(),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun TitleText(title: String) {
    Text(
        text = title,
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        style = TextStyle(
            color = ColorProvider(Color.White),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        ),
        maxLines = 2,
    )
}

@Composable
private fun BodyText(value: String, maxLines: Int = 6) {
    Text(
        text = value,
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        style = TextStyle(
            color = ColorProvider(Color(0xFFE6E1E5)),
            fontSize = 12.sp,
        ),
        maxLines = maxLines,
    )
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
    )
}

private const val QR_WIDGET_SIZE_PX = 256
