package com.pablo.paper.domain.model

enum class AnnotationType {
    INK,
    HIGHLIGHT,
    UNDERLINE,
    STRIKETHROUGH,
    SHAPE_RECTANGLE,
    SHAPE_OVAL,
    SHAPE_ARROW,
    SHAPE_LINE,
    STICKY_NOTE,
    SIGNATURE,
    TEXT_NOTE,
    TEXT_BOX,
    STAMP
}

/**
 * Persisted annotation model. Stored independently of the PDF file to allow non-destructive
 * editing, undo/redo, and cross-platform synchronization.
 */
data class Annotation(
    val id: String,
    val documentId: String,
    val pageIndex: Int,
    val type: AnnotationType,
    val stroke: InkStroke? = null,
    val highlightRects: List<FloatArray>? = null, // [left, top, right, bottom] normalized
    val textContent: String? = null, // For Sticky Notes / Comments
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
