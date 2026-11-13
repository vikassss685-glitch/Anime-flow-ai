package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

private val VikasDarkColorScheme = darkColorScheme(
    primary = CyanElectric,
    onPrimary = Color(0xFF042029),
    primaryContainer = CyanDeepContainer,
    onPrimaryContainer = Color(0xFFBDF4FC),
    secondary = VioletSoft,
    onSecondary = Color(0xFF1A0F33),
    secondaryContainer = VioletDeepContainer,
    onSecondaryContainer = Color(0xFFE5DCFF),
    tertiary = AmberStudio,
    onTertiary = Color(0xFF281802),
    tertiaryContainer = AmberDeepContainer,
    onTertiaryContainer = Color(0xFFFDE68A),
    background = ObsidianBg,
    onBackground = Color(0xFFF1F5F9),
    surface = ObsidianSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = ObsidianSurfaceHigh,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = ObsidianOutline,
    error = CoralError,
    onError = Color(0xFF310B0B)
)

private val VikasLightColorScheme = lightColorScheme(
    primary = CyanLightPrimary,
    onPrimary = Color.White,
    primaryContainer = CyanLightContainer,
    onPrimaryContainer = Color(0xFF082F49),
    secondary = VioletLightSecondary,
    onSecondary = Color.White,
    secondaryContainer = VioletLightContainer,
    onSecondaryContainer = Color(0xFF2E1065),
    tertiary = AmberLightTertiary,
    onTertiary = Color.White,
    tertiaryContainer = AmberLightContainer,
    onTertiaryContainer = Color(0xFF451A03),
    background = LightBg,
    onBackground = Color(0xFF0F172A),
    surface = LightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = LightSurfaceHigh,
    onSurfaceVariant = Color(0xFF475569),
    outline = LightOutline,
    error = Color(0xFFDC2626),
    onError = Color.White
)

val VikasShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp)
)

@Composable
fun VikasAiTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) VikasDarkColorScheme else VikasLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = VikasTypography,
        shapes = VikasShapes,
        content = content
    )
}
