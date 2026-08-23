package com.pablo.paper.ui.reader

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Draw
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.paper.domain.model.InkPoint
import com.pablo.paper.ui.common.LiquidGlassDialog
import com.pablo.paper.ui.theme.AccentBlue
import com.pablo.paper.ui.theme.BorderGlassDark
import com.pablo.paper.ui.theme.BorderGlassLight
import com.pablo.paper.ui.theme.TextPrimary
import com.pablo.paper.ui.theme.TextPrimaryDark
import com.pablo.paper.ui.theme.TextSecondary
import com.pablo.paper.ui.theme.TextSecondaryDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignatureDialog(
    isDarkMode: Boolean = false,
    onSignatureConfirmed: (List<List<InkPoint>>) -> Unit,
    onDismissRequest: () -> Unit
) {
    val strokes = remember { mutableStateListOf<List<Offset>>() }
    var currentStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }

    val padBg = if (isDarkMode) Color(0x20FFFFFF) else Color(0x30FFFFFF)
    val textColor = if (isDarkMode) TextPrimaryDark else TextPrimary
    val secTextColor = if (isDarkMode) TextSecondaryDark else TextSecondary
    val borderColor = if (isDarkMode) BorderGlassDark else BorderGlassLight

    LiquidGlassDialog(
        onDismissRequest = onDismissRequest,
        isDarkMode = isDarkMode,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .width(520.dp)
                .height(440.dp)
                .padding(22.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AccentBlue.copy(alpha = if (isDarkMode) 0.25f else 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Draw,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Firma Digital",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                fontSize = 17.sp
                            )
                        )
                        Text(
                            text = "Firma con tu lápiz o dedo en el recuadro",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = secTextColor,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                IconButton(onClick = onDismissRequest, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Cerrar",
                        tint = secTextColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Signature Pad Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(padBg)
                    .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentStroke = listOf(offset)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentStroke = currentStroke + change.position
                            },
                            onDragEnd = {
                                if (currentStroke.isNotEmpty()) {
                                    strokes.add(currentStroke)
                                    currentStroke = emptyList()
                                }
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Base guideline
                    val lineY = size.height * 0.78f
                    drawLine(
                        color = Color(0x308E8E93),
                        start = Offset(24f, lineY),
                        end = Offset(size.width - 24f, lineY),
                        strokeWidth = 1.5f.dp.toPx()
                    )

                    // Render completed strokes
                    val strokeColor = if (isDarkMode) Color.White else Color(0xFF1E293B)
                    for (stroke in strokes) {
                        if (stroke.size >= 2) {
                            val path = Path().apply {
                                moveTo(stroke.first().x, stroke.first().y)
                                for (i in 1 until stroke.size) {
                                    lineTo(stroke[i].x, stroke[i].y)
                                }
                            }
                            drawPath(
                                path = path,
                                color = strokeColor,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }

                    // Render active stroke
                    if (currentStroke.size >= 2) {
                        val path = Path().apply {
                            moveTo(currentStroke.first().x, currentStroke.first().y)
                            for (i in 1 until currentStroke.size) {
                                lineTo(currentStroke[i].x, currentStroke[i].y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = strokeColor,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }

                if (strokes.isEmpty() && currentStroke.isEmpty()) {
                    Text(
                        text = "Firma aquí...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = secTextColor.copy(alpha = 0.6f),
                            fontSize = 15.sp
                        ),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Actions Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        strokes.clear()
                        currentStroke = emptyList()
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Limpiar", fontSize = 13.sp)
                }

                Row {
                    Button(
                        onClick = onDismissRequest,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = secTextColor
                        )
                    ) {
                        Text("Cancelar")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (strokes.isNotEmpty()) {
                                var minX = Float.MAX_VALUE
                                var maxX = Float.MIN_VALUE
                                var minY = Float.MAX_VALUE
                                var maxY = Float.MIN_VALUE

                                for (stroke in strokes) {
                                    for (p in stroke) {
                                        if (p.x < minX) minX = p.x
                                        if (p.x > maxX) maxX = p.x
                                        if (p.y < minY) minY = p.y
                                        if (p.y > maxY) maxY = p.y
                                    }
                                }

                                val width = (maxX - minX).coerceAtLeast(1f)
                                val height = (maxY - minY).coerceAtLeast(1f)

                                val normalizedStrokes = strokes.map { stroke ->
                                    stroke.map { p ->
                                        InkPoint(
                                            x = (p.x - minX) / width,
                                            y = (p.y - minY) / height,
                                            pressure = 1.0f
                                        )
                                    }
                                }

                                onSignatureConfirmed(normalizedStrokes)
                            }
                            onDismissRequest()
                        },
                        enabled = strokes.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Estampar Firma", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
