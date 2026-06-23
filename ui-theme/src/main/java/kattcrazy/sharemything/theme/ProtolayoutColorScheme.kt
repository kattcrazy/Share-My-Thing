package kattcrazy.sharemything.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.wear.compose.material3.ColorScheme as WearColorScheme
import androidx.wear.protolayout.material3.ColorScheme as ProtolayoutColorScheme
import androidx.wear.protolayout.types.argb

private fun Color.toLayoutColor() = toArgb().argb

fun WearColorScheme.toProtolayoutColorScheme(): ProtolayoutColorScheme =
    ProtolayoutColorScheme(
        primary = primary.toLayoutColor(),
        primaryDim = primaryDim.toLayoutColor(),
        primaryContainer = primaryContainer.toLayoutColor(),
        onPrimary = onPrimary.toLayoutColor(),
        onPrimaryContainer = onPrimaryContainer.toLayoutColor(),
        secondary = secondary.toLayoutColor(),
        secondaryDim = secondaryDim.toLayoutColor(),
        secondaryContainer = secondaryContainer.toLayoutColor(),
        onSecondary = onSecondary.toLayoutColor(),
        onSecondaryContainer = onSecondaryContainer.toLayoutColor(),
        tertiary = tertiary.toLayoutColor(),
        tertiaryDim = tertiaryDim.toLayoutColor(),
        tertiaryContainer = tertiaryContainer.toLayoutColor(),
        onTertiary = onTertiary.toLayoutColor(),
        onTertiaryContainer = onTertiaryContainer.toLayoutColor(),
        surfaceContainerLow = surfaceContainerLow.toLayoutColor(),
        surfaceContainer = surfaceContainer.toLayoutColor(),
        surfaceContainerHigh = surfaceContainerHigh.toLayoutColor(),
        onSurface = onSurface.toLayoutColor(),
        onSurfaceVariant = onSurfaceVariant.toLayoutColor(),
        outline = outline.toLayoutColor(),
        outlineVariant = outlineVariant.toLayoutColor(),
        background = background.toLayoutColor(),
        onBackground = onBackground.toLayoutColor(),
        error = error.toLayoutColor(),
        errorDim = errorDim.toLayoutColor(),
        errorContainer = errorContainer.toLayoutColor(),
        onError = onError.toLayoutColor(),
        onErrorContainer = onErrorContainer.toLayoutColor(),
    )
