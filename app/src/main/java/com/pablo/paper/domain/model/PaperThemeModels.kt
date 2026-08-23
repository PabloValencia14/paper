package com.pablo.paper.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Available Paper Colors for the document canvas.
 */
enum class PaperColor(
    val id: String,
    val displayName: String,
    val lightColor: Long,
    val darkColor: Long,
    val textColor: Long,
    val isDarkPalette: Boolean = false
) {
    WHITE(
        id = "white",
        displayName = "Blanco Puro",
        lightColor = 0xFFFFFFFF,
        darkColor = 0xFF141416,
        textColor = 0xFF1C1C1E,
        isDarkPalette = false
    ),
    IVORY(
        id = "ivory",
        displayName = "Marfil Crema",
        lightColor = 0xFFFAF7EE,
        darkColor = 0xFF1A1916,
        textColor = 0xFF2C2621,
        isDarkPalette = false
    ),
    WARM_SEPIA(
        id = "warm_sepia",
        displayName = "Sepia Cálido",
        lightColor = 0xFFF4ECD8,
        darkColor = 0xFF241E18,
        textColor = 0xFF3D2E1E,
        isDarkPalette = false
    ),
    KRAFT(
        id = "kraft",
        displayName = "Papel Kraft",
        lightColor = 0xFFEADFC9,
        darkColor = 0xFF26201B,
        textColor = 0xFF382E25,
        isDarkPalette = false
    ),
    SOFT_MINT(
        id = "soft_mint",
        displayName = "Verde Menta",
        lightColor = 0xFFEAF2EB,
        darkColor = 0xFF16221A,
        textColor = 0xFF203828,
        isDarkPalette = false
    ),
    ROSE(
        id = "rose",
        displayName = "Rosa Pálido",
        lightColor = 0xFFFDF2F0,
        darkColor = 0xFF241819,
        textColor = 0xFF3E2723,
        isDarkPalette = false
    ),
    SLATE_GRAY(
        id = "slate_gray",
        displayName = "Gris Moleskine",
        lightColor = 0xFFE9ECF0,
        darkColor = 0xFF1A1D22,
        textColor = 0xFF252B33,
        isDarkPalette = false
    ),
    CHARCOAL(
        id = "charcoal",
        displayName = "Carbón Oscuro",
        lightColor = 0xFF212226,
        darkColor = 0xFF18191C,
        textColor = 0xFFE0E0E0,
        isDarkPalette = true
    ),
    NIGHT_BLUE(
        id = "night_blue",
        displayName = "Azul Noche",
        lightColor = 0xFF131826,
        darkColor = 0xFF0D121F,
        textColor = 0xFFE2E8F0,
        isDarkPalette = true
    ),
    AMOLED_BLACK(
        id = "amoled_black",
        displayName = "Negro AMOLED",
        lightColor = 0xFF000000,
        darkColor = 0xFF000000,
        textColor = 0xFFEDEDED,
        isDarkPalette = true
    );

    fun getColor(isDarkMode: Boolean): Color {
        return if (isDarkMode || isDarkPalette) Color(darkColor) else Color(lightColor)
    }
}

/**
 * Available Paper Textures for realistic tactile document surfaces.
 */
enum class PaperTexture(
    val id: String,
    val displayName: String,
    val description: String
) {
    SMOOTH(
        id = "smooth",
        displayName = "Liso",
        description = "Superficie digital limpia"
    ),
    FINE_GRAIN(
        id = "fine_grain",
        displayName = "Grano Fino",
        description = "Papel de algodón orgánico"
    ),
    DOT_GRID(
        id = "dot_grid",
        displayName = "Puntos",
        description = "Pauta Bullet Journal"
    ),
    GRID(
        id = "grid",
        displayName = "Cuadrícula",
        description = "Cuadrícula técnica milimétrica"
    ),
    LINED(
        id = "lined",
        displayName = "Rayado",
        description = "Líneas de cuaderno pautado"
    ),
    PARCHMENT(
        id = "parchment",
        displayName = "Pergamino",
        description = "Moteado clásico vintage"
    ),
    ISOMETRIC(
        id = "isometric",
        displayName = "Isométrica",
        description = "Malla triangular de dibujo"
    )
}
