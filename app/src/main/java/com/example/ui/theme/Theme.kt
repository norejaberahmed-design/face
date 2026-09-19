package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val BeautyColorScheme = darkColorScheme(
    primary = BeautyPrimary,
    onPrimary = BeautyBackground,
    primaryContainer = BeautySurfaceVariant,
    onPrimaryContainer = BeautyPrimary,
    secondary = BeautyBlue,
    onSecondary = BeautyText,
    secondaryContainer = BeautySurfaceVariant,
    onSecondaryContainer = BeautyBlue,
    tertiary = BeautyPurple,
    onTertiary = BeautyText,
    background = BeautyBackground,
    onBackground = BeautyText,
    surface = BeautySurface,
    onSurface = BeautyText,
    surfaceVariant = BeautySurfaceVariant,
    onSurfaceVariant = BeautyTextMuted,
    outline = BeautyCardBorder,
    error = BeautyDanger,
    onError = BeautyText
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force the designed futuristic dark theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BeautyColorScheme,
        typography = Typography,
        content = content
    )
}
