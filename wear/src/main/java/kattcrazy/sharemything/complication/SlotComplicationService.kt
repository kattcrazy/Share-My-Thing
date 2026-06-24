package kattcrazy.sharemything.complication

import android.app.PendingIntent
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import kattcrazy.sharemything.ShareMyThingApplication
import kattcrazy.sharemything.R
import kattcrazy.sharemything.data.ItemType
import kattcrazy.sharemything.data.ItemsRepository
import kattcrazy.sharemything.data.SurfaceSlot
import kattcrazy.sharemything.presentation.MainActivity

abstract class SlotComplicationService(
    private val slot: SurfaceSlot,
) : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        if (type != ComplicationType.SHORT_TEXT) return null
        return ComplicationDataFactory.create(this, title = "SMT", itemId = 1L, itemType = ItemType.TEXT)
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val repository = repository()
        repository.surfacePreferences.setPlacedOnWatch(slot, placed = true)
        val itemId = repository.surfacePreferences.getItemId(slot)
        val item = itemId?.let { repository.getItem(it) }
        return if (item == null) {
            ComplicationDataFactory.createPlaceholder(this)
        } else {
            ComplicationDataFactory.create(
                context = this,
                title = item.title,
                itemId = item.id,
                itemType = item.type,
            )
        }
    }

    private fun repository(): ItemsRepository =
        (application as ShareMyThingApplication).repository
}

class Complication1Service : SlotComplicationService(SurfaceSlot.COMPLICATION_1)

class Complication2Service : SlotComplicationService(SurfaceSlot.COMPLICATION_2)

class Complication3Service : SlotComplicationService(SurfaceSlot.COMPLICATION_3)

class Complication4Service : SlotComplicationService(SurfaceSlot.COMPLICATION_4)

class Complication5Service : SlotComplicationService(SurfaceSlot.COMPLICATION_5)

internal object ComplicationDataFactory {
    fun createPlaceholder(context: android.content.Context): ComplicationData =
        ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder("SMT").build(),
            contentDescription = PlainComplicationText.Builder(
                context.getString(R.string.surface_not_set),
            ).build(),
        ).build()

    fun create(
        context: android.content.Context,
        title: String,
        itemId: Long,
        itemType: ItemType,
    ): ComplicationData {
        val pendingIntent = PendingIntent.getActivity(
            context,
            itemId.toInt(),
            MainActivity.launchIntent(context, itemId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val icon = MonochromaticImage.Builder(
            Icon.createWithResource(context, itemType.complicationIconRes()),
        ).build()
        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(title).build(),
            contentDescription = PlainComplicationText.Builder(title).build(),
        )
            .setMonochromaticImage(icon)
            .setTapAction(pendingIntent)
            .build()
    }
}
