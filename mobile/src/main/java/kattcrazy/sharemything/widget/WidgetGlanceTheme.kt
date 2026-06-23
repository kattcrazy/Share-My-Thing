package kattcrazy.sharemything.widget

import androidx.glance.material3.ColorProviders
import kattcrazy.sharemything.theme.ShareMyThingColorSchemes

internal object WidgetGlanceTheme {
    val colors = ColorProviders(
        light = ShareMyThingColorSchemes.light,
        dark = ShareMyThingColorSchemes.dark,
    )
}
