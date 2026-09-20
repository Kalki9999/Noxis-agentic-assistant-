package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val JarvisColorScheme =
  darkColorScheme(
    primary = CyanPrimary,
    onPrimary = VoidBackground,
    primaryContainer = SurfaceElevated,
    onPrimaryContainer = CyanGlow,
    secondary = ElectricBlue,
    onSecondary = VoidBackground,
    secondaryContainer = DeepNavy,
    onSecondaryContainer = CyanPrimary,
    tertiary = AccentGold,
    onTertiary = VoidBackground,
    background = VoidBackground,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = BorderGlow,
    error = AccentCrimson,
    onError = VoidBackground
  )

@Composable
fun JarvisTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(colorScheme = JarvisColorScheme, typography = Typography, content = content)
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  JarvisTheme(content = content)
}

