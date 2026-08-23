package com.pablo.paper.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.paper.domain.model.Document
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
fun DocumentInfoDialog(
    document: Document?,
    pageCount: Int,
    currentPage: Int,
    isDarkMode: Boolean = false,
    onDismissRequest: () -> Unit
) {
    if (document == null) return

    val textColor = if (isDarkMode) TextPrimaryDark else TextPrimary
    val textSecColor = if (isDarkMode) TextSecondaryDark else TextSecondary

    LiquidGlassDialog(
        onDismissRequest = onDismissRequest,
        isDarkMode = isDarkMode,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .width(440.dp)
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Description,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.padding(end = 12.dp).size(26.dp)
                )
                Text(
                    text = "Información del Documento",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        fontSize = 17.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            InfoRow(label = "Nombre", value = document.name, labelColor = textSecColor, valueColor = textColor)
            InfoRow(label = "Páginas totales", value = "$pageCount páginas", labelColor = textSecColor, valueColor = textColor)
            InfoRow(label = "Página actual", value = "Página $currentPage", labelColor = textSecColor, valueColor = textColor)
            InfoRow(label = "Progreso", value = "${((currentPage.toFloat() / pageCount.toFloat()) * 100).toInt()}%", labelColor = textSecColor, valueColor = textColor)
            InfoRow(label = "ID Documento", value = document.id.take(8) + "...", labelColor = textSecColor, valueColor = textColor)

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismissRequest,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentBlue,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Text(text = "Cerrar", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    labelColor: Color = TextSecondary,
    valueColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = labelColor,
                fontSize = 14.sp
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = valueColor,
                fontSize = 14.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrPageDialog(
    currentPage: Int,
    pageCount: Int,
    ocrText: String,
    isLoading: Boolean,
    isDarkMode: Boolean = false,
    onCopyToClipboard: (String) -> Unit,
    onExplainWithAi: (String) -> Unit,
    onSummarizeWithAi: (String) -> Unit,
    onTranslateWithAi: (String) -> Unit,
    onAddToNotes: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val textColor = if (isDarkMode) TextPrimaryDark else TextPrimary
    val textSecColor = if (isDarkMode) TextSecondaryDark else TextSecondary
    val cardBg = if (isDarkMode) Color(0x25FFFFFF) else Color(0x40FFFFFF)

    LiquidGlassDialog(
        onDismissRequest = onDismissRequest,
        isDarkMode = isDarkMode,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .width(620.dp)
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Description,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.padding(end = 10.dp).size(26.dp)
                    )
                    Column {
                        Text(
                            text = "Extracción de Texto OCR",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = "Página $currentPage de $pageCount · Reconocimiento on-device",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = textSecColor,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = AccentBlue,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Escaneando y reconociendo texto con OCR...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = textSecColor,
                            fontSize = 14.sp
                        )
                    )
                }
            } else if (ocrText.isBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No se ha detectado texto en esta página.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = textSecColor,
                            fontSize = 14.sp
                        )
                    )
                }
            } else {
                var isBionicLocal by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Texto Reconocido (${ocrText.length} caracteres)",
                        style = MaterialTheme.typography.labelSmall,
                        color = textSecColor
                    )

                    androidx.compose.material3.FilterChip(
                        selected = isBionicLocal,
                        onClick = { isBionicLocal = !isBionicLocal },
                        label = { Text("Lectura Biónica", fontSize = 11.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardBg)
                        .border(
                            1.dp,
                            if (isDarkMode) BorderGlassDark else BorderGlassLight,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    SelectionContainer {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                com.pablo.paper.ui.common.BionicText(
                                    text = ocrText,
                                    enabled = isBionicLocal,
                                    fontSize = 14.sp,
                                    lineHeight = 22.sp,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Copiar al Portapapeles
                Button(
                    onClick = { onCopyToClipboard(ocrText) },
                    enabled = !isLoading && ocrText.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Text(text = "Copiar Todo", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Explicar con IA
                Button(
                    onClick = {
                        onExplainWithAi(ocrText)
                        onDismissRequest()
                    },
                    enabled = !isLoading && ocrText.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8B5CF6),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Text(text = "Explicar con IA", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Resumir con IA
                androidx.compose.material3.OutlinedButton(
                    onClick = {
                        onSummarizeWithAi(ocrText)
                        onDismissRequest()
                    },
                    enabled = !isLoading && ocrText.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Text(text = "Resumir", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                // Cerrar
                androidx.compose.material3.OutlinedButton(
                    onClick = onDismissRequest,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Text(text = "Cerrar", fontSize = 13.sp)
                }
            }
        }
    }
}

