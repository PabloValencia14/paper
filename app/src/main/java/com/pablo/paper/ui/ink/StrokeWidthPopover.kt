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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.pablo.paper.ui.common.LiquidGlassSurface
import com.pablo.paper.ui.theme.AccentBlue
import com.pablo.paper.ui.theme.TextPrimary
import com.pablo.paper.ui.theme.TextPrimaryDark
import com.pablo.paper.ui.theme.TextSecondary
import com.pablo.paper.ui.theme.TextSecondaryDark

data class StrokePreset(
    val label: String,
    val widthDp: Float
)

@Composable
fun StrokeWidthPopover(
    tool: InkTool,
    currentWidth: Float,
    currentColor: Long,
    isDarkMode: Boolean = false,
    onWidthSelected: (Float) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isHighlighter = tool == InkTool.HIGHLIGHTER || tool == InkTool.TEXT_HIGHLIGHT
    val isEraser = tool == InkTool.ERASER
    val textPrimary = if (isDarkMode) TextPrimaryDark else TextPrimary
    val textSecondary = if (isDarkMode) TextSecondaryDark else TextSecondary

    val presets = when {
        isHighlighter -> listOf(
            StrokePreset("Fino", 8.0f),
            StrokePreset("Medio", 14.0f),
            StrokePreset("Grueso", 20.0f),
            StrokePreset("Ancho", 28.0f)
        )
        isEraser -> listOf(
            StrokePreset("Fino", 10.0f),
            StrokePreset("Medio", 20.0f),
            StrokePreset("Grande", 35.0f),
            StrokePreset("Bloque", 50.0f)
        )
        else -> listOf(
            StrokePreset("Fino", 1.2f),
            StrokePreset("Medio", 2.5f),
            StrokePreset("Grueso", 4.5f),
            StrokePreset("Fuerte", 7.0f)
        )
    }

    val valueRange = when {
        isHighlighter -> 5.0f..35.0f
        isEraser -> 5.0f..60.0f
        else -> 0.5f..12.0f
    }

    val safeWidth = currentWidth.coerceIn(valueRange.start, valueRange.endInclusive)

    Popup(
        alignment = Alignment.TopCenter,
        offset = IntOffset(0, 140),
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true, dismissOnClickOutside = true)
    ) {
        LiquidGlassSurface(
            modifier = modifier.width(340.dp),
            shape = RoundedCornerShape(20.dp),
            isDarkMode = isDarkMode,
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
                        text = "Grosor del Trazo",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    )
                    Text(
                        text = "${String.format(java.util.Locale.US, "%.1f", safeWidth)} pt",
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
                        .background(if (isDarkMode) Color(0xFF1E293B) else Color(0xFFF2F2F7)),
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
                                .background(if (isSelected) AccentBlue else if (isDarkMode) Color(0xFF1E293B) else Color(0xFFF2F2F7))
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
                                        .background(if (isSelected) Color.White else textPrimary)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = preset.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) Color.White else textSecondary,
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
                        inactiveTrackColor = if (isDarkMode) Color(0xFF334155) else Color(0xFFE5E5EA)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
