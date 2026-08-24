package com.pablo.paper.ui.reader

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Translucent Acrylic Digital Ruler with millimeter tick marks and rotation angle.
 */
@Composable
fun DigitalRulerOverlay(
    isVisible: Boolean,
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    var offsetX by remember { mutableFloatStateOf(200f) }
    var offsetY by remember { mutableFloatStateOf(400f) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }

    val rulerWidthDp = 500.dp
    val rulerHeightDp = 72.dp

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .rotate(rotationAngle)
                .width(rulerWidthDp)
                .height(rulerHeightDp)
                .shadow(16.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xCCF8FAFC))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, rotation ->
                        offsetX += pan.x
                        offsetY += pan.y
                        rotationAngle = (rotationAngle + rotation) % 360f
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Border
                drawRoundRect(
                    color = Color(0x40334155),
                    topLeft = Offset.Zero,
                    size = size,
                    style = Stroke(width = 1.5f)
                )

                // Millimeter Marks on top edge
                val step = 16f
                var x = 20f
                var count = 0
                while (x < w - 20f) {
                    val markHeight = when {
                        count % 10 == 0 -> 24f
                        count % 5 == 0 -> 16f
                        else -> 9f
                    }

                    val markColor = if (count % 10 == 0) Color(0xFF0F172A) else Color(0x80334155)
                    val strokeW = if (count % 10 == 0) 2f else 1f

                    drawLine(
                        color = markColor,
                        start = Offset(x, 0f),
                        end = Offset(x, markHeight),
                        strokeWidth = strokeW
                    )

                    // Also mirror ticks on bottom edge
                    drawLine(
                        color = markColor,
                        start = Offset(x, h),
                        end = Offset(x, h - markHeight),
                        strokeWidth = strokeW
                    )

                    x += step
                    count++
                }

                // Center indicator with degree label
                val centerCircleRadius = 14f
                drawCircle(
                    color = Color(0xFF2563EB),
                    radius = centerCircleRadius,
                    center = Offset(w / 2f, h / 2f),
                    style = Stroke(width = 2f)
                )
                drawCircle(
                    color = Color(0x332563EB),
                    radius = centerCircleRadius,
                    center = Offset(w / 2f, h / 2f)
                )
            }
        }
    }
}
