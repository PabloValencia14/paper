package com.pablo.paper.ui.reader

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StickyNoteDialog(
    initialText: String = "",
    pageNumber: Int,
    isDarkMode: Boolean = false,
    onSave: (String) -> Unit,
    onDelete: (() -> Unit)? = null,
    onDismissRequest: () -> Unit
) {
    var text by remember { mutableStateOf(initialText) }

    val textColor = if (isDarkMode) TextPrimaryDark else TextPrimary
    val secTextColor = if (isDarkMode) TextSecondaryDark else TextSecondary
    val borderColor = if (isDarkMode) BorderGlassDark else BorderGlassLight
    val inputBg = if (isDarkMode) Color(0x20FFFFFF) else Color(0x25000000)

    LiquidGlassDialog(
        onDismissRequest = onDismissRequest,
        isDarkMode = isDarkMode,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .width(440.dp)
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
                            .background(Color(0xFFFFD54F), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.EditNote,
                            contentDescription = null,
                            tint = Color(0xFF5D4037),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Nota Adhesiva",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                fontSize = 17.sp
                            )
                        )
                        Text(
                            text = "Página $pageNumber",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = secTextColor,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                Row {
                    if (onDelete != null) {
                        IconButton(
                            onClick = {
                                onDelete()
                                onDismissRequest()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Eliminar",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(onClick = onDismissRequest, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Cerrar",
                            tint = secTextColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Text Input
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Escribe un comentario o apunte...", fontSize = 14.sp, color = secTextColor) },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = borderColor,
                    focusedContainerColor = inputBg,
                    unfocusedContainerColor = inputBg,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                ),
                minLines = 4,
                maxLines = 8,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onDismissRequest,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = secTextColor
                    )
                ) {
                    Text("Cancelar", fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (text.isNotBlank()) {
                            onSave(text.trim())
                        }
                        onDismissRequest()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue,
                        contentColor = Color.White
                    )
                ) {
                    Text("Guardar Nota", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
