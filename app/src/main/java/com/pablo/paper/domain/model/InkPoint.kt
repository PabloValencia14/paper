package com.pablo.paper.domain.model

/**
 * Represents a point within a stylus or finger stroke in normalized PDF page coordinates (0.0 to 1.0)
 * or PDF point coordinates. Storing coordinates relative to PDF page size ensures annotations scale
 * perfectly across different zoom levels, orientations, aspect ratios, and resolutions.
 */
data class InkPoint(
    val x: Float,
    val y: Float,
    val pressure: Float = 0.5f,
    val timestamp: Long = System.currentTimeMillis()
)
