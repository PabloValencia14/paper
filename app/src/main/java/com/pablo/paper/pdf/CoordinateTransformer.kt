package com.pablo.paper.pdf

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import com.pablo.paper.domain.model.InkPoint
import com.pablo.paper.domain.model.ViewMode

/**
 * High-precision coordinate transformation between Screen/Viewport space and PDF Page space.
 *
 * Page coordinates are normalized (0.0f to 1.0f) relative to the PDF page aspect ratio.
 */
class CoordinateTransformer {

    /**
     * Calculates the layout rectangle of the PDF page within the viewport given the viewport size,
     * native page size, zoom scale, pan offsets, view mode, and safe content insets.
     */
    fun calculatePageBounds(
        viewportSize: Size,
        pageSize: PageSize,
        zoom: Float = 1.0f,
        panOffset: Offset = Offset.Zero,
        viewMode: ViewMode = ViewMode.FULL_PAGE,
        topInset: Float = 0f,
        bottomInset: Float = 0f,
        horizontalInset: Float = 0f
    ): Rect {
        if (viewportSize.width <= 0f || viewportSize.height <= 0f || pageSize.width <= 0 || pageSize.height <= 0) {
            return Rect.Zero
        }

        val availableWidth = (viewportSize.width - (2f * horizontalInset)).coerceAtLeast(10f)
        val availableHeight = (viewportSize.height - topInset - bottomInset).coerceAtLeast(10f)

        val pageAspect = pageSize.width.toFloat() / pageSize.height.toFloat()
        val contentAspect = availableWidth / availableHeight

        val baseWidth: Float
        val baseHeight: Float

        when (viewMode) {
            ViewMode.FULL_PAGE -> {
                if (contentAspect > pageAspect) {
                    // Viewport is wider than page -> height is bounding dimension
                    baseHeight = availableHeight
                    baseWidth = baseHeight * pageAspect
                } else {
                    // Viewport is taller than page -> width is bounding dimension
                    baseWidth = availableWidth
                    baseHeight = baseWidth / pageAspect
                }
            }
            ViewMode.FIT_WIDTH, ViewMode.CONTINUOUS_SCROLL -> {
                baseWidth = availableWidth
                baseHeight = baseWidth / pageAspect
            }
            ViewMode.ACTUAL_SIZE -> {
                baseWidth = pageSize.width.toFloat()
                baseHeight = pageSize.height.toFloat()
            }
            ViewMode.TWO_PAGE -> {
                val halfWidth = (availableWidth / 2f).coerceAtLeast(10f)
                val halfAspect = halfWidth / availableHeight
                if (halfAspect > pageAspect) {
                    baseHeight = availableHeight
                    baseWidth = baseHeight * pageAspect
                } else {
                    baseWidth = halfWidth
                    baseHeight = baseWidth / pageAspect
                }
            }
        }

        val scaledWidth = baseWidth * zoom
        val scaledHeight = baseHeight * zoom

        // Calculate horizontal position
        val baseX = horizontalInset + (availableWidth - scaledWidth) / 2f
        val left = baseX + panOffset.x

        // Calculate vertical position
        val baseY = topInset + (availableHeight - scaledHeight) / 2f
        val top = baseY + panOffset.y

        return Rect(
            left = left,
            top = top,
            right = left + scaledWidth,
            bottom = top + scaledHeight
        )
    }

    /**
     * Converts a Screen touch offset to a normalized PDF page coordinate (0.0f to 1.0f).
     * Returns null if the touch is outside the page bounds.
     */
    fun screenToPdf(
        screenOffset: Offset,
        pageBounds: Rect
    ): InkPoint? {
        if (pageBounds.width <= 0f || pageBounds.height <= 0f) return null

        val normX = (screenOffset.x - pageBounds.left) / pageBounds.width
        val normY = (screenOffset.y - pageBounds.top) / pageBounds.height

        return InkPoint(
            x = normX.coerceIn(0f, 1f),
            y = normY.coerceIn(0f, 1f)
        )
    }

    /**
     * Converts a normalized PDF coordinate (0.0f to 1.0f) to Viewport Screen Offset.
     */
    fun pdfToScreen(
        pdfPoint: InkPoint,
        pageBounds: Rect
    ): Offset {
        return Offset(
            x = pageBounds.left + (pdfPoint.x * pageBounds.width),
            y = pageBounds.top + (pdfPoint.y * pageBounds.height)
        )
    }

    /**
     * Determines whether a tap offset is in the Left navigation edge (15–20% of document or screen).
     */
    fun isLeftEdgeTap(tapOffset: Offset, viewportWidth: Float, edgePercentage: Float = 0.18f): Boolean {
        if (viewportWidth <= 0f) return false
        return tapOffset.x < (viewportWidth * edgePercentage)
    }

    /**
     * Determines whether a tap offset is in the Right navigation edge (15–20% of document or screen).
     */
    fun isRightEdgeTap(tapOffset: Offset, viewportWidth: Float, edgePercentage: Float = 0.18f): Boolean {
        if (viewportWidth <= 0f) return false
        return tapOffset.x > (viewportWidth * (1f - edgePercentage))
    }
}
