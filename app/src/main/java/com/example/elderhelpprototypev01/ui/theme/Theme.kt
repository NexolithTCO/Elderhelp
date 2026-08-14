package com.example.elderhelpprototypev01.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = SahaayPrimary,
    onPrimary = SahaayOnPrimary,
    primaryContainer = SahaayPrimaryContainer,
    onPrimaryContainer = SahaayOnPrimaryContainer,
    secondary = SahaaySecondary,
    onSecondary = SahaayOnSecondary,
    secondaryContainer = SahaaySecondaryContainer,
    onSecondaryContainer = SahaayOnSecondaryContainer,
    tertiary = SahaayTertiary,
    onTertiary = SahaayOnTertiary,
    tertiaryContainer = SahaayTertiaryContainer,
    onTertiaryContainer = SahaayOnTertiaryContainer,
    background = SahaayBackground,
    onBackground = SahaayOnBackground,
    surface = SahaaySurface,
    onSurface = SahaayOnSurface,
    surfaceVariant = SahaaySurfaceVariant,
    onSurfaceVariant = SahaayOnSurfaceVariant,
    surfaceContainer = SahaaySurfaceContainer,
    surfaceContainerHigh = SahaaySurfaceContainerHigh,
    surfaceContainerHighest = SahaaySurfaceContainerHighest,
    outline = SahaayOutline,
    outlineVariant = SahaayOutlineVariant,
    error = SahaayError,
    onError = SahaayOnError,
    errorContainer = SahaayErrorContainer,
    onErrorContainer = SahaayOnErrorContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = SahaayPrimaryDark,
    onPrimary = SahaayOnPrimaryDark,
    primaryContainer = SahaayPrimaryContainerDark,
    onPrimaryContainer = SahaayOnPrimaryContainerDark,
    secondary = SahaaySecondaryDark,
    onSecondary = SahaayOnSecondaryDark,
    secondaryContainer = SahaaySecondaryContainerDark,
    onSecondaryContainer = SahaayOnSecondaryContainerDark,
    tertiary = SahaayTertiaryDark,
    onTertiary = SahaayOnTertiaryDark,
    tertiaryContainer = SahaayTertiaryContainerDark,
    onTertiaryContainer = SahaayOnTertiaryContainerDark,
    background = SahaayBackgroundDark,
    onBackground = SahaayOnBackgroundDark,
    surface = SahaaySurfaceDark,
    onSurface = SahaayOnSurfaceDark,
    surfaceVariant = SahaaySurfaceVariantDark,
    onSurfaceVariant = SahaayOnSurfaceVariantDark,
    surfaceContainer = SahaaySurfaceContainerDark,
    surfaceContainerHigh = SahaaySurfaceContainerHighDark,
    surfaceContainerHighest = SahaaySurfaceContainerHighestDark,
    outline = SahaayOutlineDark,
    outlineVariant = SahaayOutlineVariantDark,
    error = SahaayErrorDark,
    onError = SahaayOnErrorDark,
    errorContainer = SahaayErrorDark,
    onErrorContainer = SahaayOnErrorContainerDark
)

@Composable
fun ElderHelpPrototypeV01Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SahaayTypography,
        content = content
    )
}