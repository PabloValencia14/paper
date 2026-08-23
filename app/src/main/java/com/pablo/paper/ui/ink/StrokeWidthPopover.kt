package com.pablo.paper.ui.ink

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.pablo.paper.domain.model.InkTool
import com.pablo.paper.ui.theme.AccentBlue
import com.pablo.paper.ui.theme.BorderSubtle
import com.pablo.paper.ui.theme.SurfaceToolbar
import com.pablo.paper.ui.theme.TextPrimary
import com.pablo.paper.ui.theme.TextSecondary

data class StrokePreset(
    val label: String,
    val widthDp: Float
)

val DEFAULT_STROKE_PRESETS = listOf(
    StrokePreset("Fine", 1.5f),
    StrokePreset("Medium", 3.0f),
    StrokePreset("Thick", 6.0f),
    StrokePreset("Heavy", 10.0f)
)

@Composable
fun StrokeWidthPopover(
    tool: InkTool,
    currentWidth: Float,
    currentColor: Long,
    onWidthSelected: (Float) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isHighlighter = tool == InkTool.HIGHLIGHTER || tool == InkTool.TEXT_HIGHLIGHT
    val isEraser = tool == InkTool.ERASER

    val presets = when {
        isHighlighter -> listOf(
            StrokePreset("Fine", 8.0f),
            StrokePreset("Medium", 14.0f),
            StrokePreset("Thick", 20.0f),
            StrokePreset("Wide", 28.0f)
        )
        isEraser -> listOf(
            StrokePreset("Small", 10.0f),
            StrokePreset("Medium", 20.0f),
            StrokePreset("Large", 35.0f),
            StrokePreset("Block", 50.0f)
        )
        else -> listOf(
            StrokePreset("Fine", 1.2f),
            StrokePreset("Medium", 2.0f),
            StrokePreset("Thick", 3.5f),
            StrokePreset("Heavy", 5.5f)
        )
    }

    val valueRange = when {
        isHighlighter -> 5.0f..35.0f
        isEraser -> 5.0f..60.0f
        else -> 0.5f..10.0f
    }

    val safeWidth = currentWidth.coerceIn(valueRange.start, valueRange.endInclusive)

    Popup(
        alignment = Alignment.TopCenter,
        offset = IntOffset(0, 140),
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true, dismissOnClickOutside = true)
    ) {
        com.pablo.paper.ui.common.LiquidGlassSurface(
            modifier = modifier.width(340.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Stroke Thickness",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    )
                    Text(
                        text = "${String.format("%.1f", safeWidth)} pt",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = AccentBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Live Preview Bar
                val strokeColor = Color(currentColor).copy(alpha = tool.defaultAlpha)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF2F2F7)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxWidth(0.85f).height(32.dp)) {
                        val thickness = if (isHighlighter) {
                            (safeWidth * 0.8f).dp.toPx().coerceIn(6f, 30f)
                        } else {
                            (safeWidth * 1.5f).dp.toPx().coerceIn(2f, 26f)
                        }
                        drawLine(
                            color = strokeColor,
                            start = Offset(0f, size.height / 2f),
                            end = Offset(size.width, size.height / 2f),
                            strokeWidth = thickness,
                            cap = StrokeCap.Round
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Thickness Preset Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    presets.forEach { preset ->
                        val isSelected = kotlin.math.abs(preset.widthDp - safeWidth) < 0.6f
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 3.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) AccentBlue else Color(0xFFF2F2F7))
                                .clickable { onWidthSelected(preset.widthDp) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Dot of proportional thickness
                                Box(
                                    modifier = Modifier
                                        .size(
                                            if (isHighlighter) {
                                                (preset.widthDp * 0.5f).coerceIn(6f, 18f).dp
                                            } else {
                                                (preset.widthDp * 2.2f).coerceIn(4f, 18f).dp
                                            }
                                        )
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color.White else TextPrimary)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = preset.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) Color.White else TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Continuous Slider
                Slider(
                    value = safeWidth,
                    onValueChange = onWidthSelected,
                    valueRange = valueRange,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentBlue,
                        activeTrackColor = AccentBlue,
                        inactiveTrackColor = Color(0xFFE5E5EA)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
