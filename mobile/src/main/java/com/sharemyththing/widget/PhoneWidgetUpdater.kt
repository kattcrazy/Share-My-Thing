package com.sharemyththing.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.sharemyththing.data.SurfaceSlot
import com.sharemyththing.data.SurfaceUpdateListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PhoneWidgetUpdater(
    private val context: Context,
    private val scope: CoroutineScope,
) : SurfaceUpdateListener {
    override fun onSurfaceUpdatesNeeded(slots: Collection<SurfaceSlot>) {
        scope.launch {
            val manager = GlanceAppWidgetManager(context)
            slots.filter { it.isPhoneWidget }.forEach { slot ->
                val widgetClass = widgetClassFor(slot) ?: return@forEach
                val widget = widgetFor(slot)
                manager.getGlanceIds(widgetClass).forEach { glanceId ->
                    widget.update(context, glanceId)
                }
            }
        }
    }

    fun requestUpdateAll() {
        onSurfaceUpdatesNeeded(SurfaceSlot.phoneWidgets)
    }

    companion object {
        fun widgetFor(slot: SurfaceSlot) = when (slot) {
            SurfaceSlot.PHONE_WIDGET_1 -> PhoneWidget1()
            SurfaceSlot.PHONE_WIDGET_2 -> PhoneWidget2()
            SurfaceSlot.PHONE_WIDGET_3 -> PhoneWidget3()
            SurfaceSlot.PHONE_WIDGET_4 -> PhoneWidget4()
            SurfaceSlot.PHONE_WIDGET_5 -> PhoneWidget5()
            else -> error("Not a phone widget slot: $slot")
        }

        fun widgetClassFor(slot: SurfaceSlot): Class<out PhoneSlotWidget>? = when (slot) {
            SurfaceSlot.PHONE_WIDGET_1 -> PhoneWidget1::class.java
            SurfaceSlot.PHONE_WIDGET_2 -> PhoneWidget2::class.java
            SurfaceSlot.PHONE_WIDGET_3 -> PhoneWidget3::class.java
            SurfaceSlot.PHONE_WIDGET_4 -> PhoneWidget4::class.java
            SurfaceSlot.PHONE_WIDGET_5 -> PhoneWidget5::class.java
            else -> null
        }
    }
}
