package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KamuiDarkColorScheme = darkColorScheme(
    primary = KamuiCrimson,
    onPrimary = Color.White,
    primaryContainer = KamuiDarkRed,
    onPrimaryContainer = Color.White,
    secondary = CyberPurple,
    onSecondary = Color.White,
    background = KamuiBlack,
    onBackground = KamuiTextPrimary,
    surface = KamuiDarkSurface,
    onSurface = KamuiTextPrimary,
    surfaceVariant = KamuiDarkCard,
    onSurfaceVariant = KamuiTextSecondary,
    outline = KamuiNeonBorder
)

private val AotesLightColorScheme = lightColorScheme(
    primary = AotesPrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = AotesLightBlue,
    onPrimaryContainer = AotesDarkBlue,
    secondary = AotesIndigo,
    onSecondary = Color.White,
    background = AotesBackground,
    onBackground = AotesTextPrimary,
    surface = AotesSurface,
    onSurface = AotesTextPrimary,
    surfaceVariant = AotesSurface,
    onSurfaceVariant = AotesTextSecondary,
    outline = AotesCardBorder
)

@Composable
fun KamuiTheme(
    isAotesMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isAotesMode) AotesLightColorScheme else KamuiDarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
