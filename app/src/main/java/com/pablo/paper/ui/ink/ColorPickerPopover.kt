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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

@Composable
fun ColorPickerPopover(
    tool: InkTool,
    selectedColor: Long,
    recentColors: List<Long>,
    isDarkMode: Boolean = false,
    onColorSelected: (Long) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isHighlighter = tool == InkTool.HIGHLIGHTER || tool == InkTool.TEXT_HIGHLIGHT
    val textPrimary = if (isDarkMode) TextPrimaryDark else TextPrimary
    val textSecondary = if (isDarkMode) TextSecondaryDark else TextSecondary

    val primaryPalette = if (isHighlighter) {
        listOf(
            0xFFFFE600, // Pastel Yellow
            0xFF70E000, // Pastel Lime Green
            0xFF38B6FF, // Pastel Sky Blue
            0xFFFF70A6, // Pastel Pink
            0xFFFF9770, // Pastel Coral Orange
            0xFF9D4EDD, // Pastel Lavender Purple
            0xFF00F5D4, // Pastel Mint Teal
            0xFFFFD166  // Pastel Warm Amber
        )
    } else {
        listOf(
            0xFF000000, // Black
            0xFF007AFF, // Blue
            0xFFFF3B30, // Red
            0xFF34C759, // Green
            0xFFFF9500, // Orange
            0xFFAF52DE, // Purple
            0xFFFF2D55, // Pink
            0xFF5856D6  // Indigo
        )
    }

    val shadeVariations = remember(selectedColor) {
        val r = ((selectedColor shr 16) and 0xFF).toInt()
        val g = ((selectedColor shr 8) and 0xFF).toInt()
        val b = (selectedColor and 0xFF).toInt()

        val hsv = FloatArray(3)
        android.graphics.Color.RGBToHSV(r, g, b, hsv)

        if (hsv[1] < 0.1f && hsv[2] < 0.2f) {
            listOf(0xFFD1D1D6, 0xFF8E8E93, 0xFF48484A, 0xFF000000)
        } else {
            val shade1 = FloatArray(3).apply { this[0] = hsv[0]; this[1] = (hsv[1] * 0.35f).coerceIn(0.15f, 1f); this[2] = 0.98f }
            val shade2 = FloatArray(3).apply { this[0] = hsv[0]; this[1] = (hsv[1] * 0.65f).coerceIn(0.35f, 1f); this[2] = 0.94f }
            val shade3 = FloatArray(3).apply { this[0] = hsv[0]; this[1] = hsv[1]; this[2] = hsv[2] }
            val shade4 = FloatArray(3).apply { this[0] = hsv[0]; this[1] = (hsv[1] * 1.15f).coerceIn(0f, 1f); this[2] = (hsv[2] * 0.65f).coerceIn(0.2f, 1f) }

            listOf(
                (android.graphics.Color.HSVToColor(shade1).toLong() and 0xFFFFFFFFL),
                (android.graphics.Color.HSVToColor(shade2).toLong() and 0xFFFFFFFFL),
                (android.graphics.Color.HSVToColor(shade3).toLong() and 0xFFFFFFFFL),
                (android.graphics.Color.HSVToColor(shade4).toLong() and 0xFFFFFFFFL)
            )
        }
    }

    Popup(
        alignment = Alignment.TopCenter,
        offset = IntOffset(0, 150),
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true, dismissOnClickOutside = true)
    ) {
        LiquidGlassSurface(
            modifier = modifier.width(360.dp),
            shape = RoundedCornerShape(20.dp),
            isDarkMode = isDarkMode,
            elevation = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: Tool name
                Text(
                    text = "Paleta de Color · ${tool.displayName}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Live Stroke Preview Bar
                val strokeColor = Color(selectedColor).copy(alpha = tool.defaultAlpha)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDarkMode) Color(0xFF1E293B) else Color(0xFFF2F2F7)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxWidth(0.85f).height(20.dp)) {
                        val thickness = when (tool) {
                            InkTool.HIGHLIGHTER, InkTool.TEXT_HIGHLIGHT -> 14.dp.toPx()
                            InkTool.UNDERLINE, InkTool.STRIKETHROUGH, InkTool.WAVY_UNDERLINE -> 4.dp.toPx()
                            else -> 5.dp.toPx()
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

                Spacer(modifier = Modifier.height(18.dp))

                // Row 1: Primary Palette Swatches
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    primaryPalette.forEach { colorVal ->
                        val isSelected = (colorVal and 0xFFFFFFL) == (selectedColor and 0xFFFFFFL)
                        ColorDot(
                            color = Color(colorVal),
                            isSelected = isSelected,
                            isDarkMode = isDarkMode,
                            onClick = { onColorSelected(colorVal) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Divider line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(if (isDarkMode) Color(0x33FFFFFF) else Color(0x22000000))
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Row 2: Shades / Variations
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    shadeVariations.forEach { colorVal ->
                        val isSelected = (colorVal and 0xFFFFFFL) == (selectedColor and 0xFFFFFFL)
                        ColorDot(
                            color = Color(colorVal),
                            isSelected = isSelected,
                            isDarkMode = isDarkMode,
                            onClick = { onColorSelected(colorVal) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ColorDot(
    color: Color,
    isSelected: Boolean,
    isDarkMode: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(2.dp, if (isDarkMode) Color.White else Color(0xFF1C1C1E), CircleShape)
            )
        }
        Box(
            modifier = Modifier
                .size(if (isSelected) 22.dp else 26.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, if (isDarkMode) Color(0x44FFFFFF) else Color(0x22000000), CircleShape)
        )
    }
}
