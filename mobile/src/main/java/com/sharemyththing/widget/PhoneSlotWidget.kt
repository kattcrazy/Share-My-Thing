package com.sharemyththing.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import com.sharemyththing.data.SurfaceSlot

abstract class PhoneSlotWidget(
    private val slot: SurfaceSlot,
) : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = loadPhoneWidgetState(context, slot)
        provideContent {
            PhoneWidgetContent(state)
        }
    }
}

class PhoneWidget1 : PhoneSlotWidget(SurfaceSlot.PHONE_WIDGET_1)

class PhoneWidget2 : PhoneSlotWidget(SurfaceSlot.PHONE_WIDGET_2)

class PhoneWidget3 : PhoneSlotWidget(SurfaceSlot.PHONE_WIDGET_3)

class PhoneWidget4 : PhoneSlotWidget(SurfaceSlot.PHONE_WIDGET_4)

class PhoneWidget5 : PhoneSlotWidget(SurfaceSlot.PHONE_WIDGET_5)

class PhoneWidget1Receiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PhoneWidget1()
}

class PhoneWidget2Receiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PhoneWidget2()
}

class PhoneWidget3Receiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PhoneWidget3()
}

class PhoneWidget4Receiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PhoneWidget4()
}

class PhoneWidget5Receiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PhoneWidget5()
}
