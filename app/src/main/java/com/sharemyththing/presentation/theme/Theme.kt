package com.sharemyththing.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.MaterialTheme

@Composable
fun ShareMyThingTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(content = content)
}
