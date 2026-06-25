package kattcrazy.sharemything.complication

import android.app.PendingIntent
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImageComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import kattcrazy.sharemything.ShareMyThingApplication
import kattcrazy.sharemything.R
import kattcrazy.sharemything.data.ItemIcon
import kattcrazy.sharemything.data.ItemsRepository
import kattcrazy.sharemything.data.SurfaceSlot
import kattcrazy.sharemything.presentation.MainActivity

abstract class SlotComplicationService(
    private val slot: SurfaceSlot,
) : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {
            ComplicationType.SHORT_TEXT, ComplicationType.MONOCHROMATIC_IMAGE ->
                ComplicationDataFactory.create(
                    context = this,
                    title = "SMT",
                    itemId = 1L,
                    icon = ItemIcon.TEXT,
                    type = type,
                )
            else -> null
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val repository = repository()
        repository.surfacePreferences.setPlacedOnWatch(slot, placed = true)
        val itemId = repository.surfacePreferences.getItemId(slot)
        val item = itemId?.let { repository.getItem(it) }
        return if (item == null) {
            ComplicationDataFactory.createPlaceholder(this, request.complicationType)
        } else {
            ComplicationDataFactory.create(
                context = this,
                title = item.title,
                itemId = item.id,
                icon = item.icon,
                type = request.complicationType,
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
    fun createPlaceholder(context: android.content.Context, type: ComplicationType): ComplicationData =
        when (type) {
            ComplicationType.MONOCHROMATIC_IMAGE -> MonochromaticImageComplicationData.Builder(
                monochromaticImage = ComplicationIconFactory.monochromaticImage(
                    context,
                    ItemIcon.TEXT,
                ),
                contentDescription = PlainComplicationText.Builder(
                    context.getString(R.string.surface_not_set),
                ).build(),
            ).build()
            else -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder("SMT").build(),
                contentDescription = PlainComplicationText.Builder(
                    context.getString(R.string.surface_not_set),
                ).build(),
            ).build()
        }

    fun create(
        context: android.content.Context,
        title: String,
        itemId: Long,
        icon: ItemIcon,
        type: ComplicationType,
    ): ComplicationData {
        val pendingIntent = PendingIntent.getActivity(
            context,
            itemId.toInt(),
            MainActivity.launchIntent(context, itemId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val monochromaticImage = ComplicationIconFactory.monochromaticImage(context, icon)
        return when (type) {
            ComplicationType.MONOCHROMATIC_IMAGE -> MonochromaticImageComplicationData.Builder(
                monochromaticImage = monochromaticImage,
                contentDescription = PlainComplicationText.Builder(title).build(),
            )
                .setTapAction(pendingIntent)
                .build()
            else -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(title).build(),
                contentDescription = PlainComplicationText.Builder(title).build(),
            )
                .setTapAction(pendingIntent)
                .build()
        }
    }
}
