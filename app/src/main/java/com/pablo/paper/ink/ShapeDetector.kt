package com.pablo.paper.ink

import com.pablo.paper.domain.model.AnnotationType
import com.pablo.paper.domain.model.InkPoint
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

enum class DetectedShapeType {
    NONE,
    LINE,
    RECTANGLE,
    CIRCLE,
    TRIANGLE,
    ARROW
}

data class DetectedShapeResult(
    val type: DetectedShapeType,
    val annotationType: AnnotationType,
    val canonicalPoints: List<InkPoint>,
    val confidence: Float
)

object ShapeDetector {

    /**
     * Analyze a drawn freehand stroke and attempt to recognize a geometric primitive.
     */
    fun detectShape(points: List<InkPoint>): DetectedShapeResult? {
        if (points.size < 6) return null

        val pStart = points.first()
        val pEnd = points.last()

        val totalLength = points.zipWithNext { a, b -> hypot(b.x - a.x, b.y - a.y) }.sum()
        val directDistance = hypot(pEnd.x - pStart.x, pEnd.y - pStart.y)

        if (totalLength <= 0.001f) return null

        // 1. STRAIGHT LINE DETECTION
        val lineStraightness = directDistance / totalLength
        if (lineStraightness > 0.93f) {
            val canonical = listOf(
                InkPoint(pStart.x, pStart.y, pStart.pressure, pStart.timestamp),
                InkPoint(pEnd.x, pEnd.y, pEnd.pressure, pEnd.timestamp)
            )
            return DetectedShapeResult(
                type = DetectedShapeType.LINE,
                annotationType = AnnotationType.SHAPE_LINE,
                canonicalPoints = canonical,
                confidence = lineStraightness
            )
        }

        // 2. CLOSED SHAPES (Circle, Rectangle, Triangle)
        val isClosed = (directDistance / totalLength) < 0.22f || directDistance < 0.06f

        if (isClosed) {
            // Find centroid
            val cx = points.map { it.x }.average().toFloat()
            val cy = points.map { it.y }.average().toFloat()

            // Radii from centroid
            val radii = points.map { hypot(it.x - cx, it.y - cy) }
            val avgRadius = radii.average().toFloat()
            val radiusVariance = radii.map { abs(it - avgRadius) }.average().toFloat() / (avgRadius.coerceAtLeast(0.001f))

            // Check Circle / Oval
            if (radiusVariance < 0.20f) {
                val minX = points.minOf { it.x }
                val maxX = points.maxOf { it.x }
                val minY = points.minOf { it.y }
                val maxY = points.maxOf { it.y }

                val canonical = listOf(
                    InkPoint(minX, minY, pStart.pressure, pStart.timestamp),
                    InkPoint(maxX, maxY, pEnd.pressure, pEnd.timestamp)
                )
                return DetectedShapeResult(
                    type = DetectedShapeType.CIRCLE,
                    annotationType = AnnotationType.SHAPE_OVAL,
                    canonicalPoints = canonical,
                    confidence = 1.0f - radiusVariance
                )
            }

            // Check Rectangle
            val minX = points.minOf { it.x }
            val maxX = points.maxOf { it.x }
            val minY = points.minOf { it.y }
            val maxY = points.maxOf { it.y }

            val bboxArea = (maxX - minX) * (maxY - minY)
            if (bboxArea > 0.0005f) {
                val canonical = listOf(
                    InkPoint(minX, minY, pStart.pressure, pStart.timestamp),
                    InkPoint(maxX, maxY, pEnd.pressure, pEnd.timestamp)
                )
                return DetectedShapeResult(
                    type = DetectedShapeType.RECTANGLE,
                    annotationType = AnnotationType.SHAPE_RECTANGLE,
                    canonicalPoints = canonical,
                    confidence = 0.85f
                )
            }
        }

        // 3. ARROW DETECTION
        if (points.size >= 12 && lineStraightness > 0.70f) {
            val mainStart = points.first()
            val mainEnd = points[points.size - points.size / 4]
            val canonical = listOf(
                InkPoint(mainStart.x, mainStart.y, mainStart.pressure, mainStart.timestamp),
                InkPoint(mainEnd.x, mainEnd.y, mainEnd.pressure, mainEnd.timestamp)
            )
            return DetectedShapeResult(
                type = DetectedShapeType.ARROW,
                annotationType = AnnotationType.SHAPE_ARROW,
                canonicalPoints = canonical,
                confidence = 0.80f
            )
        }

        return null
    }
}
