package com.pablo.paper.ui.reader

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.paper.ui.common.LiquidGlassDialog
import com.pablo.paper.ui.theme.AccentBlue
import com.pablo.paper.ui.theme.BorderGlassDark
import com.pablo.paper.ui.theme.BorderGlassLight
import com.pablo.paper.ui.theme.TextPrimary
import com.pablo.paper.ui.theme.TextPrimaryDark
import com.pablo.paper.ui.theme.TextSecondary
import com.pablo.paper.ui.theme.TextSecondaryDark

private val TEXT_COLORS = listOf(
    0xFF1C1C1E, // Black
    0xFF007AFF, // Blue
    0xFFFF3B30, // Red
    0xFF34C759, // Green
    0xFFFF9500, // Orange
    0xFF8E8E93  // Grey
)

private val FONT_SIZES = listOf(
    "Pequeño" to 13f,
    "Mediano" to 16f,
    "Grande" to 22f,
    "Título" to 28f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextBoxDialog(
    initialText: String = "",
    initialColor: Long = 0xFF1C1C1E,
    initialFontSize: Float = 16f,
    pageNumber: Int,
    isDarkMode: Boolean = false,
    onSave: (text: String, color: Long, fontSize: Float) -> Unit,
    onDelete: (() -> Unit)? = null,
    onDismissRequest: () -> Unit
) {
    var text by remember { mutableStateOf(initialText) }
    var selectedColor by remember { mutableLongStateOf(initialColor) }
    var selectedFontSize by remember { mutableFloatStateOf(initialFontSize) }

    val textColor = if (isDarkMode) TextPrimaryDark else TextPrimary
    val textSecColor = if (isDarkMode) TextSecondaryDark else TextSecondary
    val borderColor = if (isDarkMode) BorderGlassDark else BorderGlassLight
    val inputBg = if (isDarkMode) Color(0x20FFFFFF) else Color(0x25000000)

    LiquidGlassDialog(
        onDismissRequest = onDismissRequest,
        isDarkMode = isDarkMode,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .width(480.dp)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AccentBlue.copy(alpha = if (isDarkMode) 0.25f else 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "T",
                            fontWeight = FontWeight.Black,
                            color = AccentBlue,
                            fontSize = 20.sp
                        )
                    }
                    Column {
                        Text(
                            text = if (initialText.isEmpty()) "Añadir Cuadro de Texto" else "Editar Texto",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            ),
                            color = textColor
                        )
                        Text(
                            text = "Página $pageNumber",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecColor
                        )
                    }
                }

                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Cerrar",
                        tint = textSecColor
                    )
                }
            }

            // Text Input
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Escribe tu texto aquí...", color = textSecColor) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = borderColor,
                    focusedContainerColor = inputBg,
                    unfocusedContainerColor = inputBg
                ),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = if (selectedColor == 0xFF1C1C1E && isDarkMode) Color.White else Color(selectedColor),
                    fontSize = selectedFontSize.sp
                )
            )

            // Font Size Selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Tamaño de fuente",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = textSecColor
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FONT_SIZES.forEach { (label, size) ->
                        val isSelected = selectedFontSize == size
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) AccentBlue.copy(alpha = if (isDarkMode) 0.30f else 0.18f) else Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) AccentBlue else borderColor
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedFontSize = size }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) AccentBlue else textColor
                                )
                            }
                        }
                    }
                }
            }

            // Color Selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Color del texto",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = textSecColor
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TEXT_COLORS.forEach { colorVal ->
                        val isSelected = selectedColor == colorVal
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (colorVal == 0xFF1C1C1E && isDarkMode) Color.White else Color(colorVal)
                                )
                                .clickable { selectedColor = colorVal }
                                .then(
                                    if (isSelected) Modifier.border(2.5.dp, AccentBlue, CircleShape)
                                    else Modifier.border(1.dp, borderColor, CircleShape)
                                )
                        )
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onDelete != null) {
                    IconButton(
                        onClick = {
                            onDelete()
                            onDismissRequest()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Eliminar",
                            tint = Color(0xFFFF3B30),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            if (text.isNotBlank()) {
                                onSave(text.trim(), selectedColor, selectedFontSize)
                                onDismissRequest()
                            }
                        },
                        enabled = text.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}
