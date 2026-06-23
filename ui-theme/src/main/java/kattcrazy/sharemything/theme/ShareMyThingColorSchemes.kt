package kattcrazy.sharemything.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme as WearColorScheme

object ShareMyThingColorSchemes {
    val BrandBlue = Color(0xFF6495ED)

    val watchSyncSuccessContainer = Color(0xFF6BAA80)
    val watchSyncSuccessOn = Color(0xFFE8F5EB)

    val light: ColorScheme = lightColorScheme(
        primary = BrandBlue,
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD6E6FF),
        onPrimaryContainer = Color(0xFF0F3566),
        secondary = Color(0xFF4578C9),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE8F0FE),
        onSecondaryContainer = Color(0xFF1A3050),
        tertiary = Color(0xFF83B4F5),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFEDF4FF),
        onTertiaryContainer = Color(0xFF0A2E5C),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        background = Color(0xFFFFFFFF),
        onBackground = Color(0xFF1B1B1F),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1B1B1F),
        surfaceVariant = Color(0xFFEEF3FB),
        onSurfaceVariant = Color(0xFF424752),
        outline = Color(0xFF74777F),
        outlineVariant = Color(0xFFC4C6D0),
        inverseSurface = Color(0xFF2F3033),
        inverseOnSurface = Color(0xFFF2F0F4),
        inversePrimary = Color(0xFFACC7FF),
    )

    val dark: ColorScheme = darkColorScheme(
        primary = Color(0xFFACC7FF),
        onPrimary = Color(0xFF002F67),
        primaryContainer = Color(0xFF004492),
        onPrimaryContainer = Color(0xFFD7E2FF),
        secondary = Color(0xFFB8C8E8),
        onSecondary = Color(0xFF1A3050),
        secondaryContainer = Color(0xFF334465),
        onSecondaryContainer = Color(0xFFDAE2F9),
        tertiary = Color(0xFF83B4F5),
        onTertiary = Color(0xFF0A2E5C),
        tertiaryContainer = Color(0xFF1E4A7A),
        onTertiaryContainer = Color(0xFFD6E6FF),
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

    val watchDark: WearColorScheme = dark
        .copy(background = Color.Black, surface = Color.Black)
        .toWearColorScheme()
}
