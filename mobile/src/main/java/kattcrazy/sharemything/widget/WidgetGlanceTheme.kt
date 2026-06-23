package kattcrazy.sharemything.widget

import androidx.glance.material3.ColorProviders
import kattcrazy.sharemything.theme.ShareMyThingColorSchemes

/** App-branded fallback when the launcher dynamic palette is unavailable (pre-Android 12). */
internal object WidgetGlanceTheme {
    val fallbackColors = ColorProviders(
        light = ShareMyThingColorSchemes.light,
        dark = ShareMyThingColorSchemes.dark,
    )
}
