package com.pablo.paper.domain.model

/**
 * A freehand or highlighter stroke drawn over a PDF page.
 *
 * @param points Ordered list of points relative to the PDF page coordinate space.
 * @param color 32-bit ARGB or 64-bit Color long value.
 * @param width Base stroke width in PDF coordinates.
 * @param opacity Stroke opacity (0.0 to 1.0, e.g. ~0.35 for Highlighter).
 * @param tool The tool used to create this stroke.
 */
data class InkStroke(
    val points: List<InkPoint>,
    val color: Long,
    val width: Float,
    val opacity: Float = 1.0f,
    val tool: InkTool = InkTool.PEN
)
