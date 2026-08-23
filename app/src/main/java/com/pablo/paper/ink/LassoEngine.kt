package com.pablo.paper.ink

import androidx.compose.ui.geometry.Rect
import com.pablo.paper.domain.model.Annotation
import com.pablo.paper.domain.model.InkPoint

object LassoEngine {

    /**
     * Determines if a 2D point (normalized 0..1) is inside a closed polygon using Ray-Casting.
     */
    fun isPointInPolygon(px: Float, py: Float, polygon: List<InkPoint>): Boolean {
        if (polygon.size < 3) return false
        var inside = false
        var j = polygon.size - 1
        for (i in polygon.indices) {
            val xi = polygon[i].x
            val yi = polygon[i].y
            val xj = polygon[j].x
            val yj = polygon[j].y

            val intersect = ((yi > py) != (yj > py)) &&
                    (px < (xj - xi) * (py - yi) / (yj - yi) + xi)
            if (intersect) {
                inside = !inside
            }
            j = i
        }
        return inside
    }

    /**
     * Select annotations where at least one key point or the bounding centroid falls inside the lasso polygon.
     */
    fun findSelectedAnnotations(
        annotations: List<Annotation>,
        lassoPolygon: List<InkPoint>
    ): List<Annotation> {
        if (lassoPolygon.size < 3 || annotations.isEmpty()) return emptyList()

        return annotations.filter { ann ->
            val stroke = ann.stroke
            if (stroke != null && stroke.points.isNotEmpty()) {
                // Check points
                val anyPointInside = stroke.points.any { p -> isPointInPolygon(p.x, p.y, lassoPolygon) }
                if (anyPointInside) return@filter true

                // Check centroid
                val cx = stroke.points.map { it.x }.average().toFloat()
                val cy = stroke.points.map { it.y }.average().toFloat()
                isPointInPolygon(cx, cy, lassoPolygon)
            } else {
                false
            }
        }
    }

    /**
     * Calculates the bounding rectangle in normalized (0..1) coordinates for a set of selected annotations.
     */
    fun calculateSelectionBounds(annotations: List<Annotation>): Rect? {
        val allPoints = annotations.mapNotNull { it.stroke?.points }.flatten()
        if (allPoints.isEmpty()) return null

        val minX = allPoints.minOf { it.x }
        val maxX = allPoints.maxOf { it.x }
        val minY = allPoints.minOf { it.y }
        val maxY = allPoints.maxOf { it.y }

        return Rect(minX, minY, maxX, maxY)
    }

    /**
     * Applies a delta shift to all points in the selected annotations.
     */
    fun moveAnnotations(
        annotations: List<Annotation>,
        deltaX: Float,
        deltaY: Float
    ): List<Annotation> {
        return annotations.map { ann ->
            val stroke = ann.stroke ?: return@map ann
            val shiftedPoints = stroke.points.map { p ->
                p.copy(x = (p.x + deltaX).coerceIn(0f, 1f), y = (p.y + deltaY).coerceIn(0f, 1f))
            }
            ann.copy(stroke = stroke.copy(points = shiftedPoints))
        }
    }

    /**
     * Updates the stroke color for all selected ink annotations.
     */
    fun recolorAnnotations(
        annotations: List<Annotation>,
        color: Long
    ): List<Annotation> {
        return annotations.map { ann ->
            val stroke = ann.stroke ?: return@map ann
            ann.copy(stroke = stroke.copy(color = color))
        }
    }
}
