package com.pablo.paper.desktop.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.pablo.paper.desktop.model.ThemeMode

// "Vellum and graphite": a reading desk, not a generic SaaS dashboard.
val Oxide = Color(0xFF9A4C39)
val OcherMark = Color(0xFFD3A631)
val Graphite = Color(0xFF252A29)
val Vellum = Color(0xFFF5F1E9)
val Desk = Color(0xFFE8E4DC)
val DeskLine = Color(0xFFC9C3B8)

private val LightColorScheme = lightColorScheme(
    primary = Oxide,
    onPrimary = Color.White,
    secondary = Color(0xFF4E625F),
    onSecondary = Color.White,
    tertiary = OcherMark,
    background = Desk,
    onBackground = Graphite,
    surface = Vellum,
    onSurface = Graphite,
    surfaceVariant = Color(0xFFECE7DE),
    onSurfaceVariant = Color(0xFF5E625D),
    outline = DeskLine
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE3A08F),
    onPrimary = Color(0xFF402016),
    secondary = Color(0xFFB7CBC4),
    onSecondary = Color(0xFF1E2B28),
    tertiary = Color(0xFFE3BE63),
    background = Color(0xFF181B1A),
    onBackground = Color(0xFFE7E5DF),
    surface = Color(0xFF222624),
    onSurface = Color(0xFFE7E5DF),
    surfaceVariant = Color(0xFF2D312E),
    onSurfaceVariant = Color(0xFFBFC4BD),
    outline = Color(0xFF4C524D)
)

private val SepiaColorScheme = lightColorScheme(
    primary = Color(0xFF7A4830),
    onPrimary = Color.White,
    secondary = Color(0xFF5B6653),
    onSecondary = Color.White,
    tertiary = Color(0xFFA57920),
    background = Color(0xFFE7DCC7),
    onBackground = Color(0xFF392B22),
    surface = Color(0xFFF7EDD9),
    onSurface = Color(0xFF392B22),
    surfaceVariant = Color(0xFFEDE0C9),
    onSurfaceVariant = Color(0xFF675647),
    outline = Color(0xFFCAB79A)
)

private val PaperTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 21.sp,
        lineHeight = 27.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 19.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 13.sp,
        lineHeight = 19.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)

@Composable
fun PaperDesktopTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val colors = when (themeMode) {
        ThemeMode.SYSTEM -> if (systemDark) DarkColorScheme else LightColorScheme
        ThemeMode.LIGHT -> LightColorScheme
        ThemeMode.DARK, ThemeMode.INVERTED -> DarkColorScheme
        ThemeMode.SEPIA -> SepiaColorScheme
        ThemeMode.HIGH_CONTRAST -> darkColorScheme(
            primary = Color.White,
            onPrimary = Color.Black,
            background = Color.Black,
            onBackground = Color.White,
            surface = Color(0xFF111111),
            onSurface = Color.White,
            surfaceVariant = Color(0xFF242424),
            onSurfaceVariant = Color.White,
            outline = Color.White
        )
    }
    MaterialTheme(colorScheme = colors, typography = PaperTypography, content = content)
}
