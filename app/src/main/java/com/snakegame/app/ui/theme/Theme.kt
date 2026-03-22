package com.snakegame.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SnakeDarkColors = darkColorScheme(
    primary = SnakeHeadGreen,
    onPrimary = Color(0xFF052E16),
    primaryContainer = Color(0xFF14532D),
    onPrimaryContainer = SnakeHeadGreen,
    secondary = SnakeBodyGreen,
    onSecondary = Color(0xFF052E16),
    tertiary = FoodAccent,
    background = BackgroundDeep,
    onBackground = Color(0xFFF1F5F9),
    surface = BackgroundElevated,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = BoardSurface,
    onSurfaceVariant = HudMuted,
    outline = Color(0xFF475569),
)

@Composable
fun SnakeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SnakeDarkColors,
        typography = SnakeTypography,
        content = content,
    )
}
