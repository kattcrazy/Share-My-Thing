package com.sharemyththing.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme as WearColorScheme

/** Brand seed and M3 Tonal Spot schemes generated from [#6495ED](https://material-foundation.github.io/material-theme-builder/). */
object ShareMyThingColorSchemes {
    val BrandBlue = Color(0xFF6495ED)

    val light: ColorScheme = lightColorScheme(
        primary = Color(0xFF235CB1),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD7E2FF),
        onPrimaryContainer = Color(0xFF001A40),
        secondary = Color(0xFF565E71),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFDAE2F9),
        onSecondaryContainer = Color(0xFF131C2C),
        tertiary = Color(0xFF705574),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFBD7FC),
        onTertiaryContainer = Color(0xFF29132E),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        background = Color(0xFFFEFBFF),
        onBackground = Color(0xFF1B1B1F),
        surface = Color(0xFFFEFBFF),
        onSurface = Color(0xFF1B1B1F),
        surfaceVariant = Color(0xFFE1E2EC),
        onSurfaceVariant = Color(0xFF44474E),
        outline = Color(0xFF74777F),
        outlineVariant = Color(0xFFC4C6D0),
        inverseSurface = Color(0xFF2F3033),
        inverseOnSurface = Color(0xFFF2F0F4),
        inversePrimary = Color(0xFFACC7FF),
    )

    /** Fixed watch palette — always dark, no system theme. */
    val dark: ColorScheme = darkColorScheme(
        primary = Color(0xFFACC7FF),
        onPrimary = Color(0xFF002F67),
        primaryContainer = Color(0xFF004492),
        onPrimaryContainer = Color(0xFFD7E2FF),
        secondary = Color(0xFFBEC6DC),
        onSecondary = Color(0xFF283041),
        secondaryContainer = Color(0xFF3F4759),
        onSecondaryContainer = Color(0xFFDAE2F9),
        tertiary = Color(0xFFDEBCDF),
        onTertiary = Color(0xFF402844),
        tertiaryContainer = Color(0xFF573E5B),
        onTertiaryContainer = Color(0xFFFBD7FC),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF1B1B1F),
        onBackground = Color(0xFFE3E2E6),
        surface = Color(0xFF1B1B1F),
        onSurface = Color(0xFFE3E2E6),
        surfaceVariant = Color(0xFF44474E),
        onSurfaceVariant = Color(0xFFC4C6D0),
        outline = Color(0xFF8E9099),
        outlineVariant = Color(0xFF44474E),
        inverseSurface = Color(0xFFE3E2E6),
        inverseOnSurface = Color(0xFF2F3033),
        inversePrimary = Color(0xFF235CB1),
    )

    /** Watch uses true black behind content so round bezels blend with the display edge. */
    val watchDark: WearColorScheme = dark
        .copy(background = Color.Black, surface = Color.Black)
        .toWearColorScheme()
}
