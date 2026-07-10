package kattcrazy.sharemything.complication

import android.app.PendingIntent
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImageComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.SmallImageComplicationData
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
        if (type !in ComplicationDataFactory.supportedTypes) return null
        return ComplicationDataFactory.create(
            context = this,
            title = "SMT",
            itemId = 1L,
            icon = ItemIcon.TEXT,
            type = type,
        )
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
    val supportedTypes = setOf(
        ComplicationType.SHORT_TEXT,
        ComplicationType.LONG_TEXT,
        ComplicationType.MONOCHROMATIC_IMAGE,
        ComplicationType.SMALL_IMAGE,
    )

    fun createPlaceholder(context: android.content.Context, type: ComplicationType): ComplicationData =
        when (type) {
            ComplicationType.MONOCHROMATIC_IMAGE -> iconOnly(
                context = context,
                icon = ItemIcon.TEXT,
                contentDescription = context.getString(R.string.surface_not_set),
            )
            ComplicationType.SMALL_IMAGE -> smallImageOnly(
                context = context,
                icon = ItemIcon.TEXT,
                contentDescription = context.getString(R.string.surface_not_set),
            )
            ComplicationType.LONG_TEXT -> longText(
                context = context,
                title = context.getString(R.string.complication_configure),
                icon = ItemIcon.TEXT,
                includeIcon = true,
                contentDescription = context.getString(R.string.surface_not_set),
            )
            else -> shortText(
                context = context,
                title = "SMT",
                icon = ItemIcon.TEXT,
                includeIcon = true,
                contentDescription = context.getString(R.string.surface_not_set),
            )
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
        return when (type) {
            ComplicationType.MONOCHROMATIC_IMAGE -> iconOnly(
                context = context,
                icon = icon,
                contentDescription = title,
                tapAction = pendingIntent,
            )
            ComplicationType.SMALL_IMAGE -> smallImageOnly(
                context = context,
                icon = icon,
                contentDescription = title,
                tapAction = pendingIntent,
            )
            ComplicationType.LONG_TEXT -> longText(
                context = context,
                title = title,
                icon = icon,
                includeIcon = true,
                contentDescription = title,
                tapAction = pendingIntent,
            )
            else -> shortText(
                context = context,
                title = title,
                icon = icon,
                includeIcon = true,
                contentDescription = title,
                tapAction = pendingIntent,
            )
        }
    }

    private fun shortText(
        context: android.content.Context,
        title: String,
        icon: ItemIcon,
        includeIcon: Boolean,
        contentDescription: String,
        tapAction: PendingIntent? = null,
    ): ShortTextComplicationData {
        val builder = ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(shortLabel(title)).build(),
            contentDescription = PlainComplicationText.Builder(contentDescription).build(),
        )
        if (includeIcon) {
            builder.setMonochromaticImage(ComplicationIconFactory.monochromaticImage(context, icon))
        }
        tapAction?.let(builder::setTapAction)
        return builder.build()
    }

    private fun longText(
        context: android.content.Context,
        title: String,
        icon: ItemIcon,
        includeIcon: Boolean,
        contentDescription: String,
        tapAction: PendingIntent? = null,
    ): LongTextComplicationData {
        val builder = LongTextComplicationData.Builder(
            text = PlainComplicationText.Builder(title).build(),
            contentDescription = PlainComplicationText.Builder(contentDescription).build(),
        )
        if (includeIcon) {
            builder.setMonochromaticImage(ComplicationIconFactory.monochromaticImage(context, icon))
        }
        tapAction?.let(builder::setTapAction)
        return builder.build()
    }

    private fun iconOnly(
        context: android.content.Context,
        icon: ItemIcon,
        contentDescription: String,
        tapAction: PendingIntent? = null,
    ): MonochromaticImageComplicationData {
        val builder = MonochromaticImageComplicationData.Builder(
            monochromaticImage = ComplicationIconFactory.monochromaticImage(context, icon),
            contentDescription = PlainComplicationText.Builder(contentDescription).build(),
        )
        tapAction?.let(builder::setTapAction)
        return builder.build()
    }

    private fun smallImageOnly(
        context: android.content.Context,
        icon: ItemIcon,
        contentDescription: String,
        tapAction: PendingIntent? = null,
    ): SmallImageComplicationData {
        val builder = SmallImageComplicationData.Builder(
            smallImage = ComplicationIconFactory.smallImage(context, icon),
            contentDescription = PlainComplicationText.Builder(contentDescription).build(),
        )
        tapAction?.let(builder::setTapAction)
        return builder.build()
    }

    private fun shortLabel(title: String): String =
        if (title.length <= 7) title else title.take(6) + "…"
}
