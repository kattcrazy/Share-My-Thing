package kattcrazy.sharemything.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.wear.compose.material3.MaterialTheme
import kattcrazy.sharemything.theme.ShareMyThingColorSchemes
import kattcrazy.sharemything.ui.LocalMotionEnabled
import kattcrazy.sharemything.ui.rememberMotionEnabled

@Composable
fun ShareMyThingTheme(
    content: @Composable () -> Unit,
) {
    val motionEnabled = rememberMotionEnabled()
    CompositionLocalProvider(LocalMotionEnabled provides motionEnabled) {
        MaterialTheme(
            colorScheme = ShareMyThingColorSchemes.watchDark,
            content = content,
        )
    }
}
