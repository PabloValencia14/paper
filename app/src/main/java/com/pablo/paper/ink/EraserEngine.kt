package com.pablo.paper.ink

import com.pablo.paper.domain.model.Annotation
import com.pablo.paper.domain.model.InkPoint
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/**
 * High-performance object erasing engine. Tests proximity between eraser points and stroke segments.
 */
class EraserEngine {

    /**
     * Identifies which annotations intersect with the given eraser touch point in normalized coordinates.
     */
    fun findIntersectingAnnotations(
        annotations: List<Annotation>,
        eraserPoint: InkPoint,
        normalizedToleranceRadius: Float = 0.02f
    ): List<Annotation> {
        val hitList = mutableListOf<Annotation>()

        for (annotation in annotations) {
            val stroke = annotation.stroke ?: continue
            val points = stroke.points
            if (points.isEmpty()) continue

            var hit = false
            for (i in 0 until points.size - 1) {
                val p1 = points[i]
                val p2 = points[i + 1]
                val dist = distancePointToSegment(eraserPoint.x, eraserPoint.y, p1.x, p1.y, p2.x, p2.y)
                if (dist <= normalizedToleranceRadius) {
                    hit = true
                    break
                }
            }

            if (!hit && points.size == 1) {
                val p = points[0]
                val dist = hypot(eraserPoint.x - p.x, eraserPoint.y - p.y)
                if (dist <= normalizedToleranceRadius) {
                    hit = true
                }
            }

            if (hit) {
                hitList.add(annotation)
            }
        }

        return hitList
    }

    private fun distancePointToSegment(
        px: Float, py: Float,
        x1: Float, y1: Float,
        x2: Float, y2: Float
    ): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        val lengthSquared = dx * dx + dy * dy

        if (lengthSquared == 0f) {
            return hypot(px - x1, py - y1)
        }

        val t = max(0f, min(1f, ((px - x1) * dx + (py - y1) * dy) / lengthSquared))
        val projX = x1 + t * dx
        val projY = y1 + t * dy

        return hypot(px - projX, py - projY)
    }
}
