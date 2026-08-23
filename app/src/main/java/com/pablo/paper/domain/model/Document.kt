package com.pablo.paper.domain.model

/**
 * Domain representation of an imported PDF document.
 */
data class Document(
    val id: String,
    val uri: String,
    val name: String,
    val pageCount: Int,
    val currentPage: Int = 1,
    val lastOpened: Long = System.currentTimeMillis(),
    val progress: Float = if (pageCount > 0) 1f / pageCount else 0f,
    val thumbnailPath: String? = null
) {
    val progressPercentage: Int
        get() = (progress * 100f).toInt().coerceIn(0, 100)
}
