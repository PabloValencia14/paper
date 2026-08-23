package com.pablo.paper.ink

import android.graphics.Bitmap
import android.graphics.Color
import com.pablo.paper.domain.model.InkPoint
import com.pablo.paper.domain.model.InkTool
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * High-performance text snapping and multi-line detection engine for Paper PDF Reader.
 *
 * Aligns hand-drawn sweeps (Underline, Strikethrough, Wavy Underline, and Text Highlight)
 * precisely to single or multiple text lines, baselines, and word boundaries in real-time.
 */
class TextSnapper {

    data class SnappedSegment(
        val startX: Float,
        val endX: Float,
        val topY: Float,
        val centerY: Float,
        val baselineY: Float,
        val lineHeight: Float
    )

    data class DetectedLine(
        val topY: Float,
        val centerY: Float,
        val baselineY: Float,
        val lineHeight: Float,
        val textLeftX: Float,
        val textRightX: Float
    )

    /**
     * Detects all snapped line segments traversed by a gesture.
     * Supports single-line and multi-line sweeps in real-time.
     */
    fun detectSegments(
        points: List<InkPoint>,
        bitmap: Bitmap? = null
    ): List<SnappedSegment> {
        if (points.isEmpty()) return emptyList()

        val startPoint = points.first()
        val currentPoint = points.last()

        val startX = startPoint.x.coerceIn(0f, 1f)
        val startY = startPoint.y.coerceIn(0f, 1f)
        val endX = currentPoint.x.coerceIn(0f, 1f)
        val endY = currentPoint.y.coerceIn(0f, 1f)

        // Anchor start line precisely to start point Y
        val startLine = detectLineAtY(startY, bitmap)
        // Anchor end line precisely to current point Y
        val endLine = detectLineAtY(endY, bitmap)

        // Single-line gesture check: any gesture within 1 line height is strictly single-line
        val lineThreshold = max(startLine.lineHeight, 0.020f) * 1.15f
        val verticalDiff = abs(startLine.centerY - endLine.centerY)
        val isSameLine = verticalDiff < lineThreshold

        if (isSameLine) {
            val line = startLine
            val rawMinX = min(startX, endX)
            val rawMaxX = max(startX, endX)

            // Clamp precisely to actual text start and end
            val clampedStartX = max(rawMinX, line.textLeftX)
            val clampedEndX = min(rawMaxX, line.textRightX).coerceAtLeast(clampedStartX + 0.005f)

            return listOf(
                SnappedSegment(
                    startX = clampedStartX,
                    endX = clampedEndX,
                    topY = line.topY,
                    centerY = line.centerY,
                    baselineY = line.baselineY,
                    lineHeight = line.lineHeight
                )
            )
        }

        // Multi-line gesture: strictly deduplicate all candidate lines by vertical distance
        val minLineSpacing = 0.016f
        val isMovingDown = endY >= startY

        val topLine = if (startLine.centerY <= endLine.centerY) startLine else endLine
        val bottomLine = if (startLine.centerY > endLine.centerY) startLine else endLine

        val intermediate = detectIntermediateLines(topLine.centerY, bottomLine.centerY, bitmap)

        val rawLines = mutableListOf<DetectedLine>()
        rawLines.add(topLine)
        rawLines.addAll(intermediate)
        rawLines.add(bottomLine)
        rawLines.sortBy { it.centerY }

        // Deduplicate lines to ensure no text line is ever painted twice
        val deduplicatedLines = mutableListOf<DetectedLine>()
        for (line in rawLines) {
            if (deduplicatedLines.isEmpty()) {
                deduplicatedLines.add(line)
            } else {
                val prev = deduplicatedLines.last()
                if (line.centerY - prev.centerY >= minLineSpacing) {
                    deduplicatedLines.add(line)
                }
            }
        }

        if (deduplicatedLines.size <= 1) {
            val line = deduplicatedLines.firstOrNull() ?: startLine
            val rawMinX = min(startX, endX)
            val rawMaxX = max(startX, endX)
            val clampedStartX = max(rawMinX, line.textLeftX)
            val clampedEndX = min(rawMaxX, line.textRightX).coerceAtLeast(clampedStartX + 0.005f)
            return listOf(
                SnappedSegment(
                    startX = clampedStartX,
                    endX = clampedEndX,
                    topY = line.topY,
                    centerY = line.centerY,
                    baselineY = line.baselineY,
                    lineHeight = line.lineHeight
                )
            )
        }

        val segments = mutableListOf<SnappedSegment>()

        if (isMovingDown) {
            for (i in deduplicatedLines.indices) {
                val line = deduplicatedLines[i]
                when (i) {
                    0 -> {
                        // First line: from startX to text end
                        val segStart = max(startX, line.textLeftX).coerceAtMost(line.textRightX - 0.005f)
                        val segEnd = line.textRightX
                        segments.add(
                            SnappedSegment(
                                startX = segStart,
                                endX = segEnd,
                                topY = line.topY,
                                centerY = line.centerY,
                                baselineY = line.baselineY,
                                lineHeight = line.lineHeight
                            )
                        )
                    }
                    deduplicatedLines.size - 1 -> {
                        // Last line: strictly from text start to endX
                        val segStart = line.textLeftX
                        val segEnd = min(endX, line.textRightX).coerceAtLeast(segStart + 0.005f)
                        segments.add(
                            SnappedSegment(
                                startX = segStart,
                                endX = segEnd,
                                topY = line.topY,
                                centerY = line.centerY,
                                baselineY = line.baselineY,
                                lineHeight = line.lineHeight
                            )
                        )
                    }
                    else -> {
                        // Intermediate lines: entire text bounds
                        segments.add(
                            SnappedSegment(
                                startX = line.textLeftX,
                                endX = line.textRightX,
                                topY = line.topY,
                                centerY = line.centerY,
                                baselineY = line.baselineY,
                                lineHeight = line.lineHeight
                            )
                        )
                    }
                }
            }
        } else {
            // Moving upwards
            val linesReversed = deduplicatedLines.reversed()
            for (i in linesReversed.indices) {
                val line = linesReversed[i]
                when (i) {
                    0 -> {
                        // Bottom line: from text start to startX
                        val segStart = line.textLeftX
                        val segEnd = min(startX, line.textRightX).coerceAtLeast(segStart + 0.005f)
                        segments.add(
                            SnappedSegment(
                                startX = segStart,
                                endX = segEnd,
                                topY = line.topY,
                                centerY = line.centerY,
                                baselineY = line.baselineY,
                                lineHeight = line.lineHeight
                            )
                        )
                    }
                    linesReversed.size - 1 -> {
                        // Top line: from endX to text end
                        val segStart = max(endX, line.textLeftX).coerceAtMost(line.textRightX - 0.005f)
                        val segEnd = line.textRightX
                        segments.add(
                            SnappedSegment(
                                startX = segStart,
                                endX = segEnd,
                                topY = line.topY,
                                centerY = line.centerY,
                                baselineY = line.baselineY,
                                lineHeight = line.lineHeight
                            )
                        )
                    }
                    else -> {
                        // Intermediate lines
                        segments.add(
                            SnappedSegment(
                                startX = line.textLeftX,
                                endX = line.textRightX,
                                topY = line.topY,
                                centerY = line.centerY,
                                baselineY = line.baselineY,
                                lineHeight = line.lineHeight
                            )
                        )
                    }
                }
            }
        }

        return segments
    }

    private fun findHorizontalTextBounds(
        topPx: Int,
        bottomPx: Int,
        bitmap: Bitmap
    ): Pair<Float, Float> {
        val bmpW = bitmap.width
        val bmpH = bitmap.height

        val safeTop = topPx.coerceIn(0, bmpH - 1)
        val safeBottom = bottomPx.coerceIn(0, bmpH - 1)
        if (safeBottom <= safeTop) return Pair(0.08f, 0.92f)

        var firstCol = -1
        var lastCol = -1

        // Scan from left to right for first dark text pixel (lum < 205)
        for (c in 0 until bmpW) {
            var isDark = false
            for (r in safeTop..safeBottom) {
                val pixel = bitmap.getPixel(c, r)
                val lum = (Color.red(pixel) * 299 + Color.green(pixel) * 587 + Color.blue(pixel) * 114) / 1000
                if (lum < 205) {
                    isDark = true
                    break
                }
            }
            if (isDark) {
                firstCol = c
                break
            }
        }

        // Scan from right to left for last dark text pixel (lum < 205)
        for (c in bmpW - 1 downTo 0) {
            var isDark = false
            for (r in safeTop..safeBottom) {
                val pixel = bitmap.getPixel(c, r)
                val lum = (Color.red(pixel) * 299 + Color.green(pixel) * 587 + Color.blue(pixel) * 114) / 1000
                if (lum < 205) {
                    isDark = true
                    break
                }
            }
            if (isDark) {
                lastCol = c
                break
            }
        }

        if (firstCol != -1 && lastCol != -1 && lastCol > firstCol + 10) {
            val leftNorm = (firstCol.toFloat() / bmpW.toFloat()).coerceIn(0.01f, 0.95f)
            val rightNorm = (lastCol.toFloat() / bmpW.toFloat()).coerceIn(leftNorm + 0.02f, 0.99f)
            return Pair(leftNorm, rightNorm)
        }

        return Pair(0.08f, 0.92f)
    }

    private fun detectLineAtY(
        targetY: Float,
        bitmap: Bitmap?
    ): DetectedLine {
        val normY = targetY.coerceIn(0f, 1f)

        if (bitmap != null && !bitmap.isRecycled && bitmap.width > 100 && bitmap.height > 100) {
            val bmpW = bitmap.width
            val bmpH = bitmap.height

            val py = (normY * bmpH).toInt().coerceIn(0, bmpH - 1)
            val scanRadius = (bmpH * 0.028f).toInt().coerceIn(18, 50)
            val topRow = (py - scanRadius).coerceAtLeast(0)
            val bottomRow = (py + scanRadius).coerceAtMost(bmpH - 1)

            val rowDarkness = IntArray(bottomRow - topRow + 1)
            val sampleCols = (bmpW * 0.08f).toInt()..(bmpW * 0.92f).toInt() step 6

            for (r in topRow..bottomRow) {
                var darkCount = 0
                for (c in sampleCols) {
                    val pixel = bitmap.getPixel(c, r)
                    val rVal = Color.red(pixel)
                    val gVal = Color.green(pixel)
                    val bVal = Color.blue(pixel)
                    val lum = (rVal * 299 + gVal * 587 + bVal * 114) / 1000
                    if (lum < 200) darkCount++
                }
                rowDarkness[r - topRow] = darkCount
            }

            val targetIdx = (py - topRow).coerceIn(0, rowDarkness.size - 1)
            var bestIdx = targetIdx
            var maxScore = -1f

            for (i in rowDarkness.indices) {
                val count = rowDarkness[i]
                if (count >= 2) {
                    val dist = abs(i - targetIdx).toFloat()
                    val distPenalty = 1.0f - (dist / rowDarkness.size.toFloat()) * 0.7f
                    val score = count * distPenalty
                    if (score > maxScore) {
                        maxScore = score
                        bestIdx = i
                    }
                }
            }

            if (maxScore > 0f) {
                val peakDarkness = rowDarkness[bestIdx]
                val threshold = (peakDarkness * 0.15f).toInt().coerceAtLeast(1)

                // 1. Top of uppercase and ascenders
                var firstRow = bestIdx
                while (firstRow > 0 && rowDarkness[firstRow - 1] >= threshold) firstRow--

                // 2. Main baseline of lowercase letters (base of x-height)
                var baseRow = bestIdx
                val baseThreshold = (peakDarkness * 0.35f).toInt().coerceAtLeast(1)
                while (baseRow < rowDarkness.size - 1 && rowDarkness[baseRow + 1] >= baseThreshold) baseRow++

                // 3. Descenders bottom
                var lastRow = baseRow
                while (lastRow < rowDarkness.size - 1 && rowDarkness[lastRow + 1] >= threshold) lastRow++

                val textTopPx = topRow + firstRow
                val textBasePx = topRow + baseRow
                val textBottomPx = topRow + lastRow
                // True optical center: exact midpoint between letter top and baseline (cuts right through the middle of characters)
                val textCenterPx = (textTopPx + textBasePx) / 2.0f

                val normCenter = (textCenterPx / bmpH.toFloat()).coerceIn(0f, 1f)
                val rawHeight = (textBottomPx - textTopPx).toFloat() / bmpH.toFloat()
                val normHeight = rawHeight.coerceIn(0.018f, 0.024f)

                // Homogeneous highlight bounds centered on line center
                val normTop = (normCenter - normHeight * 0.5f).coerceIn(0f, 1f)
                // Baseline directly under text
                val normBaseline = ((textBasePx + (bmpH * 0.0012f)).toFloat() / bmpH.toFloat()).coerceIn(0f, 1f)

                val (lineLeftX, lineRightX) = findHorizontalTextBounds(textTopPx, textBottomPx, bitmap)

                return DetectedLine(
                    topY = normTop,
                    centerY = normCenter,
                    baselineY = normBaseline,
                    lineHeight = normHeight,
                    textLeftX = lineLeftX,
                    textRightX = lineRightX
                )
            }
        }

        val lineH = 0.020f
        return DetectedLine(
            topY = (normY - lineH * 0.5f).coerceIn(0f, 1f),
            centerY = normY,
            baselineY = (normY + lineH * 0.45f).coerceIn(0f, 1f),
            lineHeight = lineH,
            textLeftX = 0.08f,
            textRightX = 0.92f
        )
    }

    private fun detectIntermediateLines(
        minCenterY: Float,
        maxCenterY: Float,
        bitmap: Bitmap?
    ): List<DetectedLine> {
        val result = mutableListOf<DetectedLine>()
        val defaultPitch = 0.022f

        if (maxCenterY <= minCenterY + defaultPitch * 0.8f) {
            return emptyList()
        }

        if (bitmap != null && !bitmap.isRecycled && bitmap.width > 100 && bitmap.height > 100) {
            val bmpW = bitmap.width
            val bmpH = bitmap.height

            val topRow = (minCenterY * bmpH).toInt().coerceIn(0, bmpH - 1)
            val bottomRow = (maxCenterY * bmpH).toInt().coerceIn(0, bmpH - 1)

            if (bottomRow > topRow + 15) {
                val rowDarkness = IntArray(bottomRow - topRow + 1)
                val sampleCols = (bmpW * 0.08f).toInt()..(bmpW * 0.92f).toInt() step 6

                for (r in topRow..bottomRow) {
                    var darkCount = 0
                    for (c in sampleCols) {
                        val pixel = bitmap.getPixel(c, r)
                        val rVal = Color.red(pixel)
                        val gVal = Color.green(pixel)
                        val bVal = Color.blue(pixel)
                        val lum = (rVal * 299 + gVal * 587 + bVal * 114) / 1000
                        if (lum < 200) darkCount++
                    }
                    rowDarkness[r - topRow] = darkCount
                }

                val minSeparation = (bmpH * 0.012f).toInt().coerceAtLeast(10)
                var lastPeakRow = -minSeparation

                for (i in 1 until rowDarkness.size - 1) {
                    val count = rowDarkness[i]
                    if (count >= 5 && count >= rowDarkness[i - 1] && count >= rowDarkness[i + 1]) {
                        if (i - lastPeakRow >= minSeparation) {
                            val peakRow = topRow + i
                            val normCenter = peakRow.toFloat() / bmpH

                            if (normCenter > minCenterY + 0.008f && normCenter < maxCenterY - 0.008f) {
                                val peakDarkness = count
                                val threshold = (peakDarkness * 0.15f).toInt().coerceAtLeast(1)

                                var firstRow = i
                                while (firstRow > 0 && rowDarkness[firstRow - 1] >= threshold) firstRow--

                                var baseRow = i
                                val baseThreshold = (peakDarkness * 0.35f).toInt().coerceAtLeast(1)
                                while (baseRow < rowDarkness.size - 1 && rowDarkness[baseRow + 1] >= baseThreshold) baseRow++

                                var lastRow = baseRow
                                while (lastRow < rowDarkness.size - 1 && rowDarkness[lastRow + 1] >= threshold) lastRow++

                                val textTopPx = topRow + firstRow
                                val textBasePx = topRow + baseRow
                                val textBottomPx = topRow + lastRow
                                val textCenterPx = (textTopPx + textBasePx) / 2.0f

                                val normLineCenter = (textCenterPx / bmpH.toFloat()).coerceIn(0f, 1f)
                                val rawHeight = (textBottomPx - textTopPx).toFloat() / bmpH.toFloat()
                                val normHeight = rawHeight.coerceIn(0.018f, 0.024f)

                                val normTop = (normLineCenter - normHeight * 0.5f).coerceIn(0f, 1f)
                                val normBaseline = ((textBasePx + (bmpH * 0.0012f)).toFloat() / bmpH.toFloat()).coerceIn(0f, 1f)

                                val (lineLeftX, lineRightX) = findHorizontalTextBounds(textTopPx, textBottomPx, bitmap)

                                result.add(
                                    DetectedLine(
                                        topY = normTop,
                                        centerY = normLineCenter,
                                        baselineY = normBaseline,
                                        lineHeight = normHeight,
                                        textLeftX = lineLeftX,
                                        textRightX = lineRightX
                                    )
                                )
                                lastPeakRow = i
                            }
                        }
                    }
                }
            }
        }

        if (result.isEmpty()) {
            var currentY = minCenterY + defaultPitch
            while (currentY < maxCenterY - defaultPitch * 0.5f) {
                val lineH = 0.020f
                result.add(
                    DetectedLine(
                        topY = (currentY - lineH * 0.5f).coerceIn(0f, 1f),
                        centerY = currentY,
                        baselineY = (currentY + lineH * 0.45f).coerceIn(0f, 1f),
                        lineHeight = lineH,
                        textLeftX = 0.08f,
                        textRightX = 0.92f
                    )
                )
                currentY += defaultPitch
            }
        }

        return result
    }

    /**
     * Converts a raw sweep gesture into snapped points according to the selected tool.
     * Supports multi-line segment preservation.
     */
    fun snapStrokePoints(
        rawPoints: List<InkPoint>,
        tool: InkTool,
        bitmap: Bitmap? = null
    ): List<InkPoint> {
        if (rawPoints.isEmpty()) return rawPoints

        if (tool == InkTool.PEN || tool == InkTool.HIGHLIGHTER || tool == InkTool.ERASER) {
            return rawPoints
        }

        val segments = detectSegments(rawPoints, bitmap)
        if (segments.isEmpty()) return rawPoints

        val resultPoints = mutableListOf<InkPoint>()
        for (seg in segments) {
            when (tool) {
                InkTool.UNDERLINE, InkTool.WAVY_UNDERLINE -> {
                    resultPoints.add(InkPoint(x = seg.startX, y = seg.baselineY))
                    resultPoints.add(InkPoint(x = seg.endX, y = seg.baselineY))
                }
                InkTool.STRIKETHROUGH -> {
                    resultPoints.add(InkPoint(x = seg.startX, y = seg.centerY))
                    resultPoints.add(InkPoint(x = seg.endX, y = seg.centerY))
                }
                InkTool.TEXT_HIGHLIGHT -> {
                    resultPoints.add(InkPoint(x = seg.startX, y = seg.topY))
                    resultPoints.add(InkPoint(x = seg.endX, y = seg.baselineY))
                }
                else -> {}
            }
        }

        return resultPoints
    }
}
