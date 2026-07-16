package kattcrazy.sharemything.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import kattcrazy.sharemything.theme.ShareMyThingColorSchemes
import kattcrazy.sharemything.ui.LocalMotionEnabled
import kattcrazy.sharemything.ui.rememberMotionEnabled

@Composable
fun ShareMyThingTheme(
    content: @Composable () -> Unit,
) {
    val darkTheme = isSystemInDarkTheme()
    val colorScheme = if (darkTheme) ShareMyThingColorSchemes.dark else ShareMyThingColorSchemes.light
    val motionEnabled = rememberMotionEnabled()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalMotionEnabled provides motionEnabled) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}
