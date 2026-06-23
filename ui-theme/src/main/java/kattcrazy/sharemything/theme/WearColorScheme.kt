package kattcrazy.sharemything.theme

import androidx.compose.material3.ColorScheme as MobileColorScheme
import androidx.wear.compose.material3.ColorScheme as WearColorScheme

fun MobileColorScheme.toWearColorScheme(): WearColorScheme =
    WearColorScheme(
        primary = primary,
        primaryDim = primaryContainer,
        primaryContainer = primaryContainer,
        onPrimary = onPrimary,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        secondaryDim = secondaryContainer,
        secondaryContainer = secondaryContainer,
        onSecondary = onSecondary,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        tertiaryDim = tertiaryContainer,
        tertiaryContainer = tertiaryContainer,
        onTertiary = onTertiary,
        onTertiaryContainer = onTertiaryContainer,
        surfaceContainerLow = surface,
        surfaceContainer = surfaceVariant,
        surfaceContainerHigh = surfaceVariant,
        onSurface = onSurface,
        onSurfaceVariant = onSurfaceVariant,
        outline = outline,
        outlineVariant = outlineVariant,
        background = background,
        onBackground = onBackground,
        error = error,
        errorDim = errorContainer,
        errorContainer = errorContainer,
        onError = onError,
        onErrorContainer = onErrorContainer,
    )
