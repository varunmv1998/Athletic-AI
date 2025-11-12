package com.athleticai.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Athletic AI Theme Presets
 * Beautiful, carefully crafted color schemes for different moods and preferences
 */
enum class ThemePreset(
    val displayName: String,
    val description: String,
    val seedColor: Color,
    val previewColors: List<Color>
) {
    DYNAMIC(
        displayName = "Dynamic",
        description = "Adapts to your wallpaper",
        seedColor = Color(0xFF1B5E20),
        previewColors = listOf(
            Color(0xFF1B5E20),
            Color(0xFF8ECF8B),
            Color(0xFFA8E6A3),
            Color(0xFF526350)
        )
    ),
    
    OCEAN_BLUE(
        displayName = "Ocean Blue",
        description = "Calming blues and teals",
        seedColor = Color(0xFF006494),
        previewColors = listOf(
            Color(0xFF006494),
            Color(0xFF247BA0),
            Color(0xFF1B98E0),
            Color(0xFFE8F1F2)
        )
    ),
    
    FOREST_GREEN(
        displayName = "Forest Green",
        description = "Nature-inspired greens",
        seedColor = Color(0xFF2D6A4F),
        previewColors = listOf(
            Color(0xFF2D6A4F),
            Color(0xFF40916C),
            Color(0xFF52B788),
            Color(0xFF95D5B2)
        )
    ),
    
    SUNSET_ORANGE(
        displayName = "Sunset Orange",
        description = "Warm oranges and reds",
        seedColor = Color(0xFFE85D04),
        previewColors = listOf(
            Color(0xFFE85D04),
            Color(0xFFDC2F02),
            Color(0xFFF48C06),
            Color(0xFFFAA307)
        )
    ),
    
    PURPLE_STORM(
        displayName = "Purple Storm",
        description = "Deep purples and violets",
        seedColor = Color(0xFF6A4C93),
        previewColors = listOf(
            Color(0xFF6A4C93),
            Color(0xFF8B5CF6),
            Color(0xFFA78BFA),
            Color(0xFFC4B5FD)
        )
    ),
    
    MONOCHROME(
        displayName = "Monochrome",
        description = "Elegant grays and blacks",
        seedColor = Color(0xFF424242),
        previewColors = listOf(
            Color(0xFF212121),
            Color(0xFF424242),
            Color(0xFF757575),
            Color(0xFFBDBDBD)
        )
    ),
    
    CHERRY_BLOSSOM(
        displayName = "Cherry Blossom",
        description = "Soft pinks and whites",
        seedColor = Color(0xFFE91E63),
        previewColors = listOf(
            Color(0xFFE91E63),
            Color(0xFFF8BBD0),
            Color(0xFFFCE4EC),
            Color(0xFFF48FB1)
        )
    ),
    
    GOLDEN_HOUR(
        displayName = "Golden Hour",
        description = "Warm yellows and ambers",
        seedColor = Color(0xFFFFB700),
        previewColors = listOf(
            Color(0xFFFFB700),
            Color(0xFFFFC947),
            Color(0xFFFFE082),
            Color(0xFFFFF3B8)
        )
    ),
    
    ARCTIC_FROST(
        displayName = "Arctic Frost",
        description = "Cool blues and whites",
        seedColor = Color(0xFF00BCD4),
        previewColors = listOf(
            Color(0xFF00BCD4),
            Color(0xFF4DD0E1),
            Color(0xFF80DEEA),
            Color(0xFFB2EBF2)
        )
    ),
    
    CUSTOM(
        displayName = "Custom",
        description = "Extract from your photo",
        seedColor = Color(0xFF1B5E20),
        previewColors = listOf(
            Color(0xFFE0E0E0),
            Color(0xFFBDBDBD),
            Color(0xFF9E9E9E),
            Color(0xFF757575)
        )
    );
    
    fun getLightColorScheme(): ColorScheme {
        return when (this) {
            OCEAN_BLUE -> lightColorScheme(
                primary = Color(0xFF006494),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFC7E6FF),
                onPrimaryContainer = Color(0xFF001E2F),
                secondary = Color(0xFF52606D),
                onSecondary = Color(0xFFFFFFFF),
                secondaryContainer = Color(0xFFD5E4F4),
                onSecondaryContainer = Color(0xFF0F1D29),
                tertiary = Color(0xFF695E6F),
                onTertiary = Color(0xFFFFFFFF),
                tertiaryContainer = Color(0xFFF1E0F7),
                onTertiaryContainer = Color(0xFF231B29),
                error = Color(0xFFBA1A1A),
                onError = Color(0xFFFFFFFF),
                errorContainer = Color(0xFFFFDAD6),
                onErrorContainer = Color(0xFF410002),
                background = Color(0xFFFCFCFF),
                onBackground = Color(0xFF1A1C1E),
                surface = Color(0xFFFCFCFF),
                onSurface = Color(0xFF1A1C1E),
                surfaceVariant = Color(0xFFDFE2EB),
                onSurfaceVariant = Color(0xFF43474E),
                outline = Color(0xFF73777F),
                outlineVariant = Color(0xFFC3C7CF),
                inverseSurface = Color(0xFF2F3033),
                inverseOnSurface = Color(0xFFF0F0F4),
                inversePrimary = Color(0xFF88CEFF)
            )
            
            FOREST_GREEN -> lightColorScheme(
                primary = Color(0xFF2D6A4F),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFB7F0D1),
                onPrimaryContainer = Color(0xFF002114),
                secondary = Color(0xFF4D6357),
                onSecondary = Color(0xFFFFFFFF),
                secondaryContainer = Color(0xFFCFE9D9),
                onSecondaryContainer = Color(0xFF0A1F16),
                tertiary = Color(0xFF3D6373),
                onTertiary = Color(0xFFFFFFFF),
                tertiaryContainer = Color(0xFFC1E8FB),
                onTertiaryContainer = Color(0xFF001F29),
                error = Color(0xFFBA1A1A),
                onError = Color(0xFFFFFFFF),
                errorContainer = Color(0xFFFFDAD6),
                onErrorContainer = Color(0xFF410002),
                background = Color(0xFFFBFDF9),
                onBackground = Color(0xFF191C1A),
                surface = Color(0xFFFBFDF9),
                onSurface = Color(0xFF191C1A),
                surfaceVariant = Color(0xFFDDE5DC),
                onSurfaceVariant = Color(0xFF414942),
                outline = Color(0xFF717971),
                outlineVariant = Color(0xFFC1C9C0),
                inverseSurface = Color(0xFF2E312E),
                inverseOnSurface = Color(0xFFF0F2EE),
                inversePrimary = Color(0xFF9BD3B6)
            )
            
            SUNSET_ORANGE -> lightColorScheme(
                primary = Color(0xFFE85D04),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFFFDBCF),
                onPrimaryContainer = Color(0xFF3A0A00),
                secondary = Color(0xFF775651),
                onSecondary = Color(0xFFFFFFFF),
                secondaryContainer = Color(0xFFFFDBCF),
                onSecondaryContainer = Color(0xFF2C1511),
                tertiary = Color(0xFF695E2F),
                onTertiary = Color(0xFFFFFFFF),
                tertiaryContainer = Color(0xFFF2E2A7),
                onTertiaryContainer = Color(0xFF221B00),
                error = Color(0xFFBA1A1A),
                onError = Color(0xFFFFFFFF),
                errorContainer = Color(0xFFFFDAD6),
                onErrorContainer = Color(0xFF410002),
                background = Color(0xFFFFFBFF),
                onBackground = Color(0xFF201A18),
                surface = Color(0xFFFFFBFF),
                onSurface = Color(0xFF201A18),
                surfaceVariant = Color(0xFFF5DED6),
                onSurfaceVariant = Color(0xFF53433E),
                outline = Color(0xFF85736D),
                outlineVariant = Color(0xFFD8C2BA),
                inverseSurface = Color(0xFF362F2C),
                inverseOnSurface = Color(0xFFFBEEE9),
                inversePrimary = Color(0xFFFFB598)
            )
            
            PURPLE_STORM -> lightColorScheme(
                primary = Color(0xFF6A4C93),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFE9DDFF),
                onPrimaryContainer = Color(0xFF22005D),
                secondary = Color(0xFF625B71),
                onSecondary = Color(0xFFFFFFFF),
                secondaryContainer = Color(0xFFE8DEF8),
                onSecondaryContainer = Color(0xFF1D192B),
                tertiary = Color(0xFF7D5260),
                onTertiary = Color(0xFFFFFFFF),
                tertiaryContainer = Color(0xFFFFD8E4),
                onTertiaryContainer = Color(0xFF31101D),
                error = Color(0xFFBA1A1A),
                onError = Color(0xFFFFFFFF),
                errorContainer = Color(0xFFFFDAD6),
                onErrorContainer = Color(0xFF410002),
                background = Color(0xFFFFFBFE),
                onBackground = Color(0xFF1C1B1F),
                surface = Color(0xFFFFFBFE),
                onSurface = Color(0xFF1C1B1F),
                surfaceVariant = Color(0xFFE7E0EC),
                onSurfaceVariant = Color(0xFF49454F),
                outline = Color(0xFF79747E),
                outlineVariant = Color(0xFFCAC4D0),
                inverseSurface = Color(0xFF313033),
                inverseOnSurface = Color(0xFFF4EFF4),
                inversePrimary = Color(0xFFCFBCFF)
            )
            
            MONOCHROME -> lightColorScheme(
                primary = Color(0xFF424242),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFBDBDBD),
                onPrimaryContainer = Color(0xFF000000),
                secondary = Color(0xFF616161),
                onSecondary = Color(0xFFFFFFFF),
                secondaryContainer = Color(0xFFE0E0E0),
                onSecondaryContainer = Color(0xFF1C1C1C),
                tertiary = Color(0xFF757575),
                onTertiary = Color(0xFFFFFFFF),
                tertiaryContainer = Color(0xFFEEEEEE),
                onTertiaryContainer = Color(0xFF212121),
                error = Color(0xFFB00020),
                onError = Color(0xFFFFFFFF),
                errorContainer = Color(0xFFFFDAD6),
                onErrorContainer = Color(0xFF410002),
                background = Color(0xFFFAFAFA),
                onBackground = Color(0xFF212121),
                surface = Color(0xFFFAFAFA),
                onSurface = Color(0xFF212121),
                surfaceVariant = Color(0xFFE0E0E0),
                onSurfaceVariant = Color(0xFF424242),
                outline = Color(0xFF757575),
                outlineVariant = Color(0xFFBDBDBD),
                inverseSurface = Color(0xFF303030),
                inverseOnSurface = Color(0xFFF5F5F5),
                inversePrimary = Color(0xFF9E9E9E)
            )
            
            CHERRY_BLOSSOM -> lightColorScheme(
                primary = Color(0xFFE91E63),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFFFD1DC),
                onPrimaryContainer = Color(0xFF3E001A),
                secondary = Color(0xFF75565E),
                onSecondary = Color(0xFFFFFFFF),
                secondaryContainer = Color(0xFFFFD9E1),
                onSecondaryContainer = Color(0xFF2C151C),
                tertiary = Color(0xFF7C5635),
                onTertiary = Color(0xFFFFFFFF),
                tertiaryContainer = Color(0xFFFFDCC1),
                onTertiaryContainer = Color(0xFF2E1500),
                error = Color(0xFFBA1A1A),
                onError = Color(0xFFFFFFFF),
                errorContainer = Color(0xFFFFDAD6),
                onErrorContainer = Color(0xFF410002),
                background = Color(0xFFFFFBFF),
                onBackground = Color(0xFF201A1B),
                surface = Color(0xFFFFFBFF),
                onSurface = Color(0xFF201A1B),
                surfaceVariant = Color(0xFFF3DDE0),
                onSurfaceVariant = Color(0xFF524345),
                outline = Color(0xFF847375),
                outlineVariant = Color(0xFFD6C2C4),
                inverseSurface = Color(0xFF352F30),
                inverseOnSurface = Color(0xFFFAEEEF),
                inversePrimary = Color(0xFFFFB1C0)
            )
            
            GOLDEN_HOUR -> lightColorScheme(
                primary = Color(0xFFFFB700),
                onPrimary = Color(0xFF442B00),
                primaryContainer = Color(0xFFFFDDB3),
                onPrimaryContainer = Color(0xFF2A1A00),
                secondary = Color(0xFF715A41),
                onSecondary = Color(0xFFFFFFFF),
                secondaryContainer = Color(0xFFFCDEBD),
                onSecondaryContainer = Color(0xFF281805),
                tertiary = Color(0xFF55643A),
                onTertiary = Color(0xFFFFFFFF),
                tertiaryContainer = Color(0xFFD8EAB5),
                onTertiaryContainer = Color(0xFF131F01),
                error = Color(0xFFBA1A1A),
                onError = Color(0xFFFFFFFF),
                errorContainer = Color(0xFFFFDAD6),
                onErrorContainer = Color(0xFF410002),
                background = Color(0xFFFFFBFF),
                onBackground = Color(0xFF1F1B16),
                surface = Color(0xFFFFFBFF),
                onSurface = Color(0xFF1F1B16),
                surfaceVariant = Color(0xFFEFE0CF),
                onSurfaceVariant = Color(0xFF4F4539),
                outline = Color(0xFF817567),
                outlineVariant = Color(0xFFD2C4B4),
                inverseSurface = Color(0xFF34302A),
                inverseOnSurface = Color(0xFFF9EFE6),
                inversePrimary = Color(0xFFE7C068)
            )
            
            ARCTIC_FROST -> lightColorScheme(
                primary = Color(0xFF00BCD4),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFB2EBF2),
                onPrimaryContainer = Color(0xFF001F24),
                secondary = Color(0xFF4B6269),
                onSecondary = Color(0xFFFFFFFF),
                secondaryContainer = Color(0xFFCEE7EF),
                onSecondaryContainer = Color(0xFF061F24),
                tertiary = Color(0xFF5A5D7D),
                onTertiary = Color(0xFFFFFFFF),
                tertiaryContainer = Color(0xFFE0E0FF),
                onTertiaryContainer = Color(0xFF161937),
                error = Color(0xFFBA1A1A),
                onError = Color(0xFFFFFFFF),
                errorContainer = Color(0xFFFFDAD6),
                onErrorContainer = Color(0xFF410002),
                background = Color(0xFFFAFDFD),
                onBackground = Color(0xFF191C1D),
                surface = Color(0xFFFAFDFD),
                onSurface = Color(0xFF191C1D),
                surfaceVariant = Color(0xFFDBE4E7),
                onSurfaceVariant = Color(0xFF3F484B),
                outline = Color(0xFF6F797B),
                outlineVariant = Color(0xFFBFC8CB),
                inverseSurface = Color(0xFF2E3132),
                inverseOnSurface = Color(0xFFF0F4F4),
                inversePrimary = Color(0xFF66D3E8)
            )
            
            else -> lightColorScheme(
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
                inversePrimary = md_theme_light_inversePrimary
            )
        }
    }
    
    fun getDarkColorScheme(): ColorScheme {
        return when (this) {
            OCEAN_BLUE -> darkColorScheme(
                primary = Color(0xFF88CEFF),
                onPrimary = Color(0xFF00344F),
                primaryContainer = Color(0xFF004C70),
                onPrimaryContainer = Color(0xFFC7E6FF),
                secondary = Color(0xFFB9C8DA),
                onSecondary = Color(0xFF24323F),
                secondaryContainer = Color(0xFF3A4856),
                onSecondaryContainer = Color(0xFFD5E4F4),
                tertiary = Color(0xFFD4C4DB),
                onTertiary = Color(0xFF392E3F),
                tertiaryContainer = Color(0xFF504557),
                onTertiaryContainer = Color(0xFFF1E0F7),
                error = Color(0xFFFFB4AB),
                onError = Color(0xFF690005),
                errorContainer = Color(0xFF93000A),
                onErrorContainer = Color(0xFFFFDAD6),
                background = Color(0xFF1A1C1E),
                onBackground = Color(0xFFE2E2E6),
                surface = Color(0xFF1A1C1E),
                onSurface = Color(0xFFE2E2E6),
                surfaceVariant = Color(0xFF43474E),
                onSurfaceVariant = Color(0xFFC3C7CF),
                outline = Color(0xFF8D9199),
                outlineVariant = Color(0xFF43474E),
                inverseSurface = Color(0xFFE2E2E6),
                inverseOnSurface = Color(0xFF2F3033),
                inversePrimary = Color(0xFF006494)
            )
            
            FOREST_GREEN -> darkColorScheme(
                primary = Color(0xFF9BD3B6),
                onPrimary = Color(0xFF003824),
                primaryContainer = Color(0xFF005137),
                onPrimaryContainer = Color(0xFFB7F0D1),
                secondary = Color(0xFFB3CCBD),
                onSecondary = Color(0xFF1F352A),
                secondaryContainer = Color(0xFF354B40),
                onSecondaryContainer = Color(0xFFCFE9D9),
                tertiary = Color(0xFFA5CCDE),
                onTertiary = Color(0xFF073543),
                tertiaryContainer = Color(0xFF244C5A),
                onTertiaryContainer = Color(0xFFC1E8FB),
                error = Color(0xFFFFB4AB),
                onError = Color(0xFF690005),
                errorContainer = Color(0xFF93000A),
                onErrorContainer = Color(0xFFFFDAD6),
                background = Color(0xFF191C1A),
                onBackground = Color(0xFFE1E3DF),
                surface = Color(0xFF191C1A),
                onSurface = Color(0xFFE1E3DF),
                surfaceVariant = Color(0xFF414942),
                onSurfaceVariant = Color(0xFFC1C9C0),
                outline = Color(0xFF8B938A),
                outlineVariant = Color(0xFF414942),
                inverseSurface = Color(0xFFE1E3DF),
                inverseOnSurface = Color(0xFF2E312E),
                inversePrimary = Color(0xFF2D6A4F)
            )
            
            SUNSET_ORANGE -> darkColorScheme(
                primary = Color(0xFFFFB598),
                onPrimary = Color(0xFF5D1A00),
                primaryContainer = Color(0xFF852400),
                onPrimaryContainer = Color(0xFFFFDBCF),
                secondary = Color(0xFFE6BDB0),
                onSecondary = Color(0xFF442A25),
                secondaryContainer = Color(0xFF5D3F3A),
                onSecondaryContainer = Color(0xFFFFDBCF),
                tertiary = Color(0xFFD5C68D),
                onTertiary = Color(0xFF383005),
                tertiaryContainer = Color(0xFF50461A),
                onTertiaryContainer = Color(0xFFF2E2A7),
                error = Color(0xFFFFB4AB),
                onError = Color(0xFF690005),
                errorContainer = Color(0xFF93000A),
                onErrorContainer = Color(0xFFFFDAD6),
                background = Color(0xFF201A18),
                onBackground = Color(0xFFECE0DB),
                surface = Color(0xFF201A18),
                onSurface = Color(0xFFECE0DB),
                surfaceVariant = Color(0xFF53433E),
                onSurfaceVariant = Color(0xFFD8C2BA),
                outline = Color(0xFF9F8C86),
                outlineVariant = Color(0xFF53433E),
                inverseSurface = Color(0xFFECE0DB),
                inverseOnSurface = Color(0xFF362F2C),
                inversePrimary = Color(0xFFE85D04)
            )
            
            PURPLE_STORM -> darkColorScheme(
                primary = Color(0xFFCFBCFF),
                onPrimary = Color(0xFF381E72),
                primaryContainer = Color(0xFF4F378A),
                onPrimaryContainer = Color(0xFFE9DDFF),
                secondary = Color(0xFFCCC2DC),
                onSecondary = Color(0xFF332D41),
                secondaryContainer = Color(0xFF4A4458),
                onSecondaryContainer = Color(0xFFE8DEF8),
                tertiary = Color(0xFFEFB8C8),
                onTertiary = Color(0xFF4A2532),
                tertiaryContainer = Color(0xFF633B48),
                onTertiaryContainer = Color(0xFFFFD8E4),
                error = Color(0xFFFFB4AB),
                onError = Color(0xFF690005),
                errorContainer = Color(0xFF93000A),
                onErrorContainer = Color(0xFFFFDAD6),
                background = Color(0xFF1C1B1F),
                onBackground = Color(0xFFE6E1E5),
                surface = Color(0xFF1C1B1F),
                onSurface = Color(0xFFE6E1E5),
                surfaceVariant = Color(0xFF49454F),
                onSurfaceVariant = Color(0xFFCAC4D0),
                outline = Color(0xFF938F99),
                outlineVariant = Color(0xFF49454F),
                inverseSurface = Color(0xFFE6E1E5),
                inverseOnSurface = Color(0xFF313033),
                inversePrimary = Color(0xFF6A4C93)
            )
            
            MONOCHROME -> darkColorScheme(
                primary = Color(0xFF9E9E9E),
                onPrimary = Color(0xFF000000),
                primaryContainer = Color(0xFF424242),
                onPrimaryContainer = Color(0xFFE0E0E0),
                secondary = Color(0xFFBDBDBD),
                onSecondary = Color(0xFF303030),
                secondaryContainer = Color(0xFF424242),
                onSecondaryContainer = Color(0xFFE0E0E0),
                tertiary = Color(0xFFE0E0E0),
                onTertiary = Color(0xFF303030),
                tertiaryContainer = Color(0xFF424242),
                onTertiaryContainer = Color(0xFFEEEEEE),
                error = Color(0xFFCF6679),
                onError = Color(0xFF690005),
                errorContainer = Color(0xFF93000A),
                onErrorContainer = Color(0xFFFFDAD6),
                background = Color(0xFF121212),
                onBackground = Color(0xFFE0E0E0),
                surface = Color(0xFF121212),
                onSurface = Color(0xFFE0E0E0),
                surfaceVariant = Color(0xFF424242),
                onSurfaceVariant = Color(0xFFBDBDBD),
                outline = Color(0xFF757575),
                outlineVariant = Color(0xFF424242),
                inverseSurface = Color(0xFFE0E0E0),
                inverseOnSurface = Color(0xFF303030),
                inversePrimary = Color(0xFF616161)
            )
            
            CHERRY_BLOSSOM -> darkColorScheme(
                primary = Color(0xFFFFB1C0),
                onPrimary = Color(0xFF650030),
                primaryContainer = Color(0xFF8E0047),
                onPrimaryContainer = Color(0xFFFFD1DC),
                secondary = Color(0xFFE4BDC5),
                onSecondary = Color(0xFF432930),
                secondaryContainer = Color(0xFF5B3F46),
                onSecondaryContainer = Color(0xFFFFD9E1),
                tertiary = Color(0xFFEFBD94),
                onTertiary = Color(0xFF48290C),
                tertiaryContainer = Color(0xFF623F20),
                onTertiaryContainer = Color(0xFFFFDCC1),
                error = Color(0xFFFFB4AB),
                onError = Color(0xFF690005),
                errorContainer = Color(0xFF93000A),
                onErrorContainer = Color(0xFFFFDAD6),
                background = Color(0xFF201A1B),
                onBackground = Color(0xFFEBE0E1),
                surface = Color(0xFF201A1B),
                onSurface = Color(0xFFEBE0E1),
                surfaceVariant = Color(0xFF524345),
                onSurfaceVariant = Color(0xFFD6C2C4),
                outline = Color(0xFF9E8C8E),
                outlineVariant = Color(0xFF524345),
                inverseSurface = Color(0xFFEBE0E1),
                inverseOnSurface = Color(0xFF352F30),
                inversePrimary = Color(0xFFE91E63)
            )
            
            GOLDEN_HOUR -> darkColorScheme(
                primary = Color(0xFFE7C068),
                onPrimary = Color(0xFF3D2E00),
                primaryContainer = Color(0xFF584400),
                onPrimaryContainer = Color(0xFFFFDDB3),
                secondary = Color(0xFFDFC2A2),
                onSecondary = Color(0xFF3F2D17),
                secondaryContainer = Color(0xFF57432C),
                onSecondaryContainer = Color(0xFFFCDEBD),
                tertiary = Color(0xFFBCCD9B),
                onTertiary = Color(0xFF273510),
                tertiaryContainer = Color(0xFF3D4C24),
                onTertiaryContainer = Color(0xFFD8EAB5),
                error = Color(0xFFFFB4AB),
                onError = Color(0xFF690005),
                errorContainer = Color(0xFF93000A),
                onErrorContainer = Color(0xFFFFDAD6),
                background = Color(0xFF1F1B16),
                onBackground = Color(0xFFEAE1D9),
                surface = Color(0xFF1F1B16),
                onSurface = Color(0xFFEAE1D9),
                surfaceVariant = Color(0xFF4F4539),
                onSurfaceVariant = Color(0xFFD2C4B4),
                outline = Color(0xFF9A8F80),
                outlineVariant = Color(0xFF4F4539),
                inverseSurface = Color(0xFFEAE1D9),
                inverseOnSurface = Color(0xFF34302A),
                inversePrimary = Color(0xFF715A0A)
            )
            
            ARCTIC_FROST -> darkColorScheme(
                primary = Color(0xFF66D3E8),
                onPrimary = Color(0xFF00363D),
                primaryContainer = Color(0xFF004F58),
                onPrimaryContainer = Color(0xFFB2EBF2),
                secondary = Color(0xFFB2CBD3),
                onSecondary = Color(0xFF1D343A),
                secondaryContainer = Color(0xFF334A51),
                onSecondaryContainer = Color(0xFFCEE7EF),
                tertiary = Color(0xFFC3C3EB),
                onTertiary = Color(0xFF2C2F4D),
                tertiaryContainer = Color(0xFF424664),
                onTertiaryContainer = Color(0xFFE0E0FF),
                error = Color(0xFFFFB4AB),
                onError = Color(0xFF690005),
                errorContainer = Color(0xFF93000A),
                onErrorContainer = Color(0xFFFFDAD6),
                background = Color(0xFF191C1D),
                onBackground = Color(0xFFE0E3E3),
                surface = Color(0xFF191C1D),
                onSurface = Color(0xFFE0E3E3),
                surfaceVariant = Color(0xFF3F484B),
                onSurfaceVariant = Color(0xFFBFC8CB),
                outline = Color(0xFF899295),
                outlineVariant = Color(0xFF3F484B),
                inverseSurface = Color(0xFFE0E3E3),
                inverseOnSurface = Color(0xFF2E3132),
                inversePrimary = Color(0xFF006875)
            )
            
            else -> darkColorScheme(
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
                inversePrimary = md_theme_dark_inversePrimary
            )
        }
    }
}

/**
 * Custom theme configuration for storing extracted colors
 */
data class CustomThemeColors(
    val seedColor: Long,
    val primary: Long,
    val secondary: Long,
    val tertiary: Long,
    val neutral: Long
)