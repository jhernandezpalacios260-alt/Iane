package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CineDarkColorScheme = darkColorScheme(
    primary = CineGold,
    onPrimary = CineVoid,
    primaryContainer = CineSurfaceElevated,
    onPrimaryContainer = CineGoldLight,
    secondary = CineCyan,
    onSecondary = CineVoid,
    secondaryContainer = CineSurfaceVariant,
    onSecondaryContainer = CineCyan,
    tertiary = CineNeonRed,
    onTertiary = CineVoid,
    background = CineVoid,
    onBackground = CineTextPrimary,
    surface = CineSurface,
    onSurface = CineTextPrimary,
    surfaceVariant = CineSurfaceVariant,
    onSurfaceVariant = CineTextSecondary,
    outline = CineBorder,
    outlineVariant = CineBorder
)

@Composable
fun CineGenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Cinema production studio apps excel in a consistent, immersive dark aesthetic
    MaterialTheme(
        colorScheme = CineDarkColorScheme,
        typography = CineTypography,
        content = content
    )
}
