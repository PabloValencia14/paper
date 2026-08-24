package com.pablo.paper.ui.reader

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.pablo.paper.domain.model.Annotation
import com.pablo.paper.domain.model.AnnotationType
import com.pablo.paper.ui.theme.AccentBlue

/**
 * Study Mask Overlay for Active Recall learning.
 * Masks highlights with opaque study bars; tapping reveals/hides the text underneath.
 */
@Composable
fun StudyMaskOverlay(
    annotations: List<Annotation>,
    pageBounds: Rect,
    revealedMaskIds: Set<String>,
    onToggleMask: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val highlightAnnotations = remember(annotations) {
        annotations.filter {
            it.type == AnnotationType.HIGHLIGHT ||
            it.type == AnnotationType.UNDERLINE ||
            (it.highlightRects != null && it.highlightRects.isNotEmpty())
        }
    }

    if (highlightAnnotations.isEmpty()) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(highlightAnnotations, pageBounds, revealedMaskIds) {
                detectTapGestures { tapOffset ->
                    for (ann in highlightAnnotations) {
                        val rects = ann.highlightRects
                        if (rects != null && rects.isNotEmpty()) {
                            for (r in rects) {
                                if (r.size >= 4) {
                                    val screenLeft = pageBounds.left + r[0] * pageBounds.width
                                    val screenTop = pageBounds.top + r[1] * pageBounds.height
                                    val screenRight = pageBounds.left + r[2] * pageBounds.width
                                    val screenBottom = pageBounds.top + r[3] * pageBounds.height
                                    val rect = Rect(screenLeft - 4f, screenTop - 4f, screenRight + 4f, screenBottom + 4f)
                                    if (rect.contains(tapOffset)) {
                                        onToggleMask(ann.id)
                                        return@detectTapGestures
                                    }
                                }
                            }
                        } else if (ann.stroke != null && ann.stroke.points.isNotEmpty()) {
                            val pts = ann.stroke.points
                            val minX = pts.minOf { it.x }
                            val maxX = pts.maxOf { it.x }
                            val minY = pts.minOf { it.y }
                            val maxY = pts.maxOf { it.y }
                            val screenLeft = pageBounds.left + minX * pageBounds.width
                            val screenTop = pageBounds.top + minY * pageBounds.height
                            val screenRight = pageBounds.left + maxX * pageBounds.width
                            val screenBottom = pageBounds.top + maxY * pageBounds.height
                            val rect = Rect(screenLeft - 6f, screenTop - 6f, screenRight + 6f, screenBottom + 6f)
                            if (rect.contains(tapOffset)) {
                                onToggleMask(ann.id)
                                return@detectTapGestures
                            }
                        }
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (ann in highlightAnnotations) {
                val isRevealed = revealedMaskIds.contains(ann.id)
                val rects = ann.highlightRects

                if (rects != null && rects.isNotEmpty()) {
                    for (r in rects) {
                        if (r.size >= 4) {
                            val left = pageBounds.left + r[0] * pageBounds.width
                            val top = pageBounds.top + r[1] * pageBounds.height
                            val width = (r[2] - r[0]) * pageBounds.width
                            val height = (r[3] - r[1]) * pageBounds.height

                            if (!isRevealed) {
                                // Opaque black/dark mask bar with subtle rounded corners
                                drawRoundRect(
                                    color = Color(0xFF1E293B),
                                    topLeft = Offset(left, top),
                                    size = Size(width, height),
                                    cornerRadius = CornerRadius(4f, 4f)
                                )
                                drawRoundRect(
                                    color = AccentBlue.copy(alpha = 0.5f),
                                    topLeft = Offset(left, top),
                                    size = Size(width, height),
                                    cornerRadius = CornerRadius(4f, 4f),
                                    style = Stroke(width = 1.5f)
                                )
                            } else {
                                // Revealed outline indicator
                                drawRoundRect(
                                    color = Color(0x3310B981),
                                    topLeft = Offset(left, top),
                                    size = Size(width, height),
                                    cornerRadius = CornerRadius(4f, 4f)
                                )
                                drawRoundRect(
                                    color = Color(0xFF10B981),
                                    topLeft = Offset(left, top),
                                    size = Size(width, height),
                                    cornerRadius = CornerRadius(4f, 4f),
                                    style = Stroke(width = 1.2f)
                                )
                            }
                        }
                    }
                } else if (ann.stroke != null && ann.stroke.points.isNotEmpty()) {
                    val pts = ann.stroke.points
                    val minX = pts.minOf { it.x }
                    val maxX = pts.maxOf { it.x }
                    val minY = pts.minOf { it.y }
                    val maxY = pts.maxOf { it.y }
                    val left = pageBounds.left + minX * pageBounds.width
                    val top = pageBounds.top + minY * pageBounds.height
                    val width = (maxX - minX) * pageBounds.width
                    val height = (maxY - minY) * pageBounds.height

                    if (!isRevealed) {
                        drawRoundRect(
                            color = Color(0xFF1E293B),
                            topLeft = Offset(left, top),
                            size = Size(width, height),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                        drawRoundRect(
                            color = AccentBlue.copy(alpha = 0.5f),
                            topLeft = Offset(left, top),
                            size = Size(width, height),
                            cornerRadius = CornerRadius(4f, 4f),
                            style = Stroke(width = 1.5f)
                        )
                    } else {
                        drawRoundRect(
                            color = Color(0x3310B981),
                            topLeft = Offset(left, top),
                            size = Size(width, height),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                    }
                }
            }
        }
    }
}
