package com.pablo.paper.ink

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import com.pablo.paper.domain.model.InkPoint
import com.pablo.paper.pdf.CoordinateTransformer
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Generates smooth Bézier curves and mathematical wave paths from stylus/touch points.
 */
class StrokeSmoother(
    private val transformer: CoordinateTransformer = CoordinateTransformer()
) {

    /**
     * Converts a list of normalized PDF points into a smoothed Compose Path in screen coordinates.
     */
    fun createPathFromNormalizedPoints(
        points: List<InkPoint>,
        pageBounds: Rect
    ): Path {
        val path = Path()
        if (points.isEmpty()) return path

        if (points.size == 1) {
            val offset = transformer.pdfToScreen(points[0], pageBounds)
            path.addOval(
                Rect(
                    offset.x - 1f,
                    offset.y - 1f,
                    offset.x + 1f,
                    offset.y + 1f
                )
            )
            return path
        }

        val screenOffsets = points.map { transformer.pdfToScreen(it, pageBounds) }

        path.moveTo(screenOffsets[0].x, screenOffsets[0].y)

        if (screenOffsets.size == 2) {
            path.lineTo(screenOffsets[1].x, screenOffsets[1].y)
            return path
        }

        // Quadratic Bézier curve interpolation through midpoints
        for (i in 1 until screenOffsets.size - 1) {
            val p0 = screenOffsets[i]
            val p1 = screenOffsets[i + 1]
            val midPointX = (p0.x + p1.x) / 2f
            val midPointY = (p0.y + p1.y) / 2f
            path.quadraticBezierTo(p0.x, p0.y, midPointX, midPointY)
        }

        // Connect to the final point
        val last = screenOffsets.last()
        val secondLast = screenOffsets[screenOffsets.size - 2]
        path.quadraticBezierTo(secondLast.x, secondLast.y, last.x, last.y)

        return path
    }

    /**
     * Fast path generator for the currently active (live) stroke.
     */
    fun createLivePath(screenOffsets: List<Offset>): Path {
        val path = Path()
        if (screenOffsets.isEmpty()) return path
        if (screenOffsets.size == 1) {
            val p = screenOffsets[0]
            path.addOval(Rect(p.x - 1f, p.y - 1f, p.x + 1f, p.y + 1f))
            return path
        }

        path.moveTo(screenOffsets[0].x, screenOffsets[0].y)
        if (screenOffsets.size == 2) {
            path.lineTo(screenOffsets[1].x, screenOffsets[1].y)
            return path
        }

        for (i in 1 until screenOffsets.size - 1) {
            val p0 = screenOffsets[i]
            val p1 = screenOffsets[i + 1]
            val midX = (p0.x + p1.x) / 2f
            val midY = (p0.y + p1.y) / 2f
            path.quadraticBezierTo(p0.x, p0.y, midX, midY)
        }

        val last = screenOffsets.last()
        val secondLast = screenOffsets[screenOffsets.size - 2]
        path.quadraticBezierTo(secondLast.x, secondLast.y, last.x, last.y)
        return path
    }

    /**
     * Calculates width of a stroke point based on stylus pressure.
     */
    fun calculateEffectiveWidth(baseWidth: Float, pressure: Float, isHighlighter: Boolean): Float {
        val factor = if (isHighlighter) {
            (0.65f + 0.70f * pressure).coerceIn(0.5f, 1.7f)
        } else {
            (0.30f + 1.40f * pressure).coerceIn(0.20f, 2.4f)
        }
        return baseWidth * factor
    }

    /**
     * Renders a beautiful, continuous variable-width stroke interpolated along Quadratic Béziers.
     */
    fun drawPressureStroke(
        drawScope: androidx.compose.ui.graphics.drawscope.DrawScope,
        points: List<Offset>,
        pressures: List<Float>,
        baseWidth: Float,
        color: androidx.compose.ui.graphics.Color,
        isHighlighter: Boolean
    ) {
        if (points.isEmpty()) return
        val count = points.size

        if (count == 1) {
            val p0 = points[0]
            val press0 = pressures.getOrElse(0) { 0.5f }
            val w = calculateEffectiveWidth(baseWidth, press0, isHighlighter)
            drawScope.drawCircle(
                color = color,
                radius = (w / 2f).coerceAtLeast(1f),
                center = p0
            )
            return
        }

        if (count == 2) {
            val p0 = points[0]
            val p1 = points[1]
            val w0 = calculateEffectiveWidth(baseWidth, pressures.getOrElse(0) { 0.5f }, isHighlighter)
            val w1 = calculateEffectiveWidth(baseWidth, pressures.getOrElse(1) { 0.5f }, isHighlighter)
            drawScope.drawLine(
                color = color,
                start = p0,
                end = p1,
                strokeWidth = (w0 + w1) / 2f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            return
        }

        // 3 or more points: smooth midpoints and interpolated Bezier segments with variable width
        val widths = List(count) { i ->
            calculateEffectiveWidth(baseWidth, pressures.getOrElse(i) { 0.5f }, isHighlighter)
        }

        // Draw start cap
        drawScope.drawLine(
            color = color,
            start = points[0],
            end = Offset((points[0].x + points[1].x) / 2f, (points[0].y + points[1].y) / 2f),
            strokeWidth = widths[0],
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        for (i in 1 until count - 1) {
            val pPrev = points[i - 1]
            val pCurr = points[i]
            val pNext = points[i + 1]

            val midStart = Offset((pPrev.x + pCurr.x) / 2f, (pPrev.y + pCurr.y) / 2f)
            val midEnd = Offset((pCurr.x + pNext.x) / 2f, (pCurr.y + pNext.y) / 2f)

            val wStart = (widths[i - 1] + widths[i]) / 2f
            val wEnd = (widths[i] + widths[i + 1]) / 2f

            // Subdivide the quadratic Bezier into 3 segments for smooth width transitions
            val steps = 3
            var prevPos = midStart
            for (step in 1..steps) {
                val t = step.toFloat() / steps.toFloat()
                val oneMinusT = 1f - t
                val curX = oneMinusT * oneMinusT * midStart.x + 2f * oneMinusT * t * pCurr.x + t * t * midEnd.x
                val curY = oneMinusT * oneMinusT * midStart.y + 2f * oneMinusT * t * pCurr.y + t * t * midEnd.y
                val curPos = Offset(curX, curY)
                val curWidth = wStart + (wEnd - wStart) * t

                drawScope.drawLine(
                    color = color,
                    start = prevPos,
                    end = curPos,
                    strokeWidth = curWidth,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                prevPos = curPos
            }
        }

        // Draw end cap
        drawScope.drawLine(
            color = color,
            start = Offset((points[count - 2].x + points[count - 1].x) / 2f, (points[count - 2].y + points[count - 1].y) / 2f),
            end = points[count - 1],
            strokeWidth = widths[count - 1],
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }

    /**
     * Creates a crisp, beautiful sinusoidal wave path between two points.
     */
    fun createWavyPath(
        start: Offset,
        end: Offset,
        waveLength: Float = 14f,
        amplitude: Float = 3.5f
    ): Path {
        val path = Path()
        val minX = min(start.x, end.x)
        val maxX = max(start.x, end.x)
        val baseY = (start.y + end.y) / 2f

        val totalWidth = maxX - minX
        if (totalWidth <= 2f) {
            path.moveTo(minX, baseY)
            path.lineTo(maxX, baseY)
            return path
        }

        path.moveTo(minX, baseY)
        val halfWave = (waveLength / 2f).coerceAtLeast(3f)
        var currentX = minX
        var isCrest = true

        while (currentX < maxX) {
            val nextX = min(currentX + halfWave, maxX)
            val segmentWidth = nextX - currentX
            val midX = currentX + (segmentWidth / 2f)
            val peakY = if (isCrest) baseY - amplitude else baseY + amplitude

            path.quadraticBezierTo(midX, peakY, nextX, baseY)
            currentX = nextX
            isCrest = !isCrest
        }

        return path
    }
}
