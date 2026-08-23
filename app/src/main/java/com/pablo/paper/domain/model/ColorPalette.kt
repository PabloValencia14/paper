package com.pablo.paper.domain.model

/**
 * Standard palette and color utilities according to the Driven-Spec.
 */
object ColorPalette {
    // 8 Primary Observed Colors
    const val BLACK: Long = 0xFF000000
    const val BLUE: Long = 0xFF2F6BFF
    const val RED: Long = 0xFFE53935
    const val GREEN: Long = 0xFF2E8B45
    const val YELLOW: Long = 0xFFF4C430
    const val ORANGE: Long = 0xFFF57C00
    const val PINK: Long = 0xFFEC407A
    const val PURPLE: Long = 0xFF7E3FF2

    val PRIMARY_COLORS: List<Long> = listOf(
        BLACK,
        BLUE,
        RED,
        GREEN,
        YELLOW,
        ORANGE,
        PINK,
        PURPLE
    )

    val DEFAULT_HIGHLIGHTER_COLORS: List<Long> = listOf(
        YELLOW,
        GREEN,
        PINK,
        BLUE,
        ORANGE
    )

    fun getInitialRecentColors(): List<Long> {
        return listOf(BLACK, BLUE, RED, YELLOW, GREEN)
    }
}
