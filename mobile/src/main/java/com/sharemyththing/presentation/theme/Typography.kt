package com.sharemyththing.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.sharemyththing.R

val RalewayFontFamily = FontFamily(
    Font(R.font.raleway_medium, FontWeight.Medium),
    Font(R.font.raleway_semibold, FontWeight.SemiBold),
)

@Composable
fun appNameTextStyle(baseStyle: TextStyle = MaterialTheme.typography.titleLarge): TextStyle =
    baseStyle.copy(
        fontFamily = RalewayFontFamily,
        fontWeight = FontWeight.SemiBold,
    )
