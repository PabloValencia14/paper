package com.pablo.paper.desktop.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.pablo.paper.desktop.model.ThemeMode

// Professional Acrobat / Windows 11 Neutral Theme Tokens
val AcrobatBlue = Color(0xFF0078D4)       // Windows 11 / Acrobat Blue
val AcrobatBlueHover = Color(0xFF1084D9)
val AcrobatRed = Color(0xFFEB1000)        // PDF Red Accent

// Neutral Dark (Acrobat Pro Dark)
val DarkDeskBg = Color(0xFF181818)        // Neutral dark desk background
val DarkSurface = Color(0xFF242424)       // Neutral dark toolbar / panel
val DarkSurfaceElevated = Color(0xFF2E2E2E)
val DarkBorder = Color(0xFF383838)        // Subtle neutral border
val DarkText = Color(0xFFFFFFFF)          // Crisp white
val DarkTextMuted = Color(0xFFA0A0A0)     // Muted text

// Neutral Light (Acrobat Pro Light)
val LightDeskBg = Color(0xFFEBEBEB)       // Neutral light gray desk
val LightSurface = Color(0xFFFFFFFF)      // Pure white panels
val LightSurfaceElevated = Color(0xFFF7F7F7)
val LightBorder = Color(0xFFD4D4D4)       // Subtle light border
val LightText = Color(0xFF1A1A1A)         // Dark charcoal text
val LightTextMuted = Color(0xFF666666)    // Muted text

// Sepia Reading Theme
val SepiaDeskBg = Color(0xFFEDE3D2)
val SepiaSurface = Color(0xFFFBF0D9)
val SepiaBorder = Color(0xFFD8C7B0)
val SepiaText = Color(0xFF3B281B)

private val LightColorScheme = lightColorScheme(
    primary = AcrobatBlue,
    onPrimary = Color.White,
    secondary = AcrobatBlueHover,
    onSecondary = Color.White,
    tertiary = AcrobatRed,
    background = LightDeskBg,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightTextMuted,
    outline = LightBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = AcrobatBlue,
    onPrimary = Color.White,
    secondary = AcrobatBlueHover,
    onSecondary = Color.White,
    tertiary = AcrobatRed,
    background = DarkDeskBg,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextMuted,
    outline = DarkBorder
)

private val SepiaColorScheme = lightColorScheme(
    primary = Color(0xFF8B4513),
    onPrimary = Color.White,
    secondary = Color(0xFFA0522D),
    onSecondary = Color.White,
    background = SepiaDeskBg,
    onBackground = SepiaText,
    surface = SepiaSurface,
    onSurface = SepiaText,
    surfaceVariant = Color(0xFFF4E5CA),
    onSurfaceVariant = Color(0xFF5C4033),
    outline = SepiaBorder
)

@Composable
fun PaperDesktopTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()

    val colorScheme = when (themeMode) {
        ThemeMode.SYSTEM -> if (isSystemDark) DarkColorScheme else LightColorScheme
        ThemeMode.LIGHT -> LightColorScheme
        ThemeMode.DARK, ThemeMode.INVERTED -> DarkColorScheme
        ThemeMode.SEPIA -> SepiaColorScheme
        ThemeMode.HIGH_CONTRAST -> darkColorScheme(
            primary = Color(0xFF0078D4),
            onPrimary = Color.White,
            background = Color.Black,
            onBackground = Color.White,
            surface = Color(0xFF121212),
            onSurface = Color.White,
            outline = Color.White
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
