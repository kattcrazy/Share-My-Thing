package kattcrazy.sharemything.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.MaterialTheme
import kattcrazy.sharemything.theme.ShareMyThingColorSchemes

@Composable
fun ShareMyThingTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = ShareMyThingColorSchemes.watchDark,
        content = content,
    )
}
