package com.athleticai.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// Material3 Motion and Animation Constants
object AthleticAIMotion {
    // Animation durations
    const val SHORT_DURATION = 300
    const val MEDIUM_DURATION = 500
    const val LONG_DURATION = 700
    
    // Spring animation constants
    val SPRING_STIFFNESS = Spring.StiffnessMedium
    val SPRING_DAMPING_RATIO = Spring.DampingRatioMediumBouncy
    
    // Interactive element transforms
    const val HOVER_SCALE = 1.02f
    const val PRESS_SCALE = 0.98f
    
    // Elevation changes
    val CARD_ELEVATION_DEFAULT = 2.dp
    val CARD_ELEVATION_HOVER = 8.dp
    val CARD_ELEVATION_PRESS = 1.dp
}

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
)

private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
)

@Composable
fun AthleticAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themePreset: ThemePreset = ThemePreset.DYNAMIC,
    customColors: CustomThemeColors? = null,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Custom extracted colors take priority
        customColors != null && themePreset == ThemePreset.CUSTOM -> {
            createColorSchemeFromCustomColors(customColors, darkTheme)
        }
        
        // Dynamic colors for DYNAMIC preset on Android 12+
        themePreset == ThemePreset.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        
        // Use preset color schemes
        themePreset != ThemePreset.DYNAMIC -> {
            if (darkTheme) themePreset.getDarkColorScheme() else themePreset.getLightColorScheme()
        }
        
        // Fallback to default theme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
private fun createColorSchemeFromCustomColors(
    customColors: CustomThemeColors,
    darkTheme: Boolean
): ColorScheme {
    // Convert custom colors to Material3 color scheme
    // This would be enhanced with proper tonal palette generation
    val seedColor = Color(customColors.seedColor)
    
    return if (darkTheme) {
        darkColorScheme(
            primary = Color(customColors.primary),
            secondary = Color(customColors.secondary),
            tertiary = Color(customColors.tertiary)
        )
    } else {
        lightColorScheme(
            primary = Color(customColors.primary),
            secondary = Color(customColors.secondary),
            tertiary = Color(customColors.tertiary)
        )
    }
}