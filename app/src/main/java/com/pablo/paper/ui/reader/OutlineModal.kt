package com.pablo.paper.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.paper.domain.model.Document
import com.pablo.paper.pdf.OutlineItem
import com.pablo.paper.ui.common.LiquidGlassButton
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
fun OutlineModal(
    document: Document?,
    currentPage: Int,
    pageCount: Int,
    entries: List<OutlineItem> = emptyList(),
    isLoading: Boolean = false,
    isDarkMode: Boolean = false,
    onPageSelected: (Int) -> Unit,
    onRefreshOutline: () -> Unit = {},
    onExtractWithAi: () -> Unit = {},
    onDismissRequest: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val displayEntries = remember(entries, document, pageCount) {
        if (entries.isNotEmpty()) {
            entries
        } else {
            val list = mutableListOf<OutlineItem>()
            val docTitle = document?.name?.removeSuffix(".pdf")?.replace('_', ' ')?.replace('-', ' ') ?: "Portada"
            list.add(OutlineItem(docTitle, 1, 0))
            for (p in 2..pageCount) {
                list.add(OutlineItem("Página $p", p, 0))
            }
            list
        }
    }

    val filteredEntries = remember(displayEntries, searchQuery) {
        if (searchQuery.isBlank()) displayEntries
        else displayEntries.filter {
            it.title.contains(searchQuery, ignoreCase = true) || "página ${it.pageNumber}".contains(searchQuery, ignoreCase = true) || "pág ${it.pageNumber}".contains(searchQuery, ignoreCase = true)
        }
    }

    val hasAiEntries = displayEntries.any { it.isAiGenerated }
    val borderColor = if (isDarkMode) BorderGlassDark else BorderGlassLight
    val textColor = if (isDarkMode) TextPrimaryDark else TextPrimary
    val textSecColor = if (isDarkMode) TextSecondaryDark else TextSecondary

    LiquidGlassDialog(
        onDismissRequest = onDismissRequest,
        isDarkMode = isDarkMode,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .width(620.dp)
                .height(580.dp)
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
                            .size(38.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(AccentBlue.copy(alpha = if (isDarkMode) 0.25f else 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Índice de Contenidos",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                    fontSize = 18.sp
                                )
                            )
                            if (hasAiEntries) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF8B5CF6).copy(alpha = 0.22f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "✨ IA",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFFC084FC),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (isLoading) "Analizando documento con IA..." else "${displayEntries.size} secciones detectadas · $pageCount páginas",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isLoading) AccentBlue else textSecColor,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            strokeWidth = 2.2.dp,
                            color = AccentBlue,
                            modifier = Modifier
                                .size(22.dp)
                                .padding(end = 6.dp)
                        )
                    } else {
                        // AI Extraction Button
                        LiquidGlassButton(
                            onClick = onExtractWithAi,
                            isDarkMode = isDarkMode,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (hasAiEntries) "Reanalizar IA" else "Extraer con IA",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkMode) Color.White else AccentBlue,
                                    fontSize = 11.5.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(onClick = onRefreshOutline, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Reescanear OCR",
                                tint = textSecColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(onClick = onDismissRequest, modifier = Modifier.size(34.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Cerrar",
                            tint = textSecColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar sección o página...", fontSize = 13.sp, color = textSecColor) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = textSecColor,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Rounded.Close, contentDescription = "Limpiar", tint = textSecColor, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = borderColor,
                    focusedContainerColor = if (isDarkMode) Color(0x20FFFFFF) else Color(0x30FFFFFF),
                    unfocusedContainerColor = if (isDarkMode) Color(0x15FFFFFF) else Color(0x20FFFFFF),
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Outline List
            LazyColumn(
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
            ) {
                items(filteredEntries) { entry ->
                    val isCurrent = entry.pageNumber == currentPage
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isCurrent) AccentBlue.copy(alpha = if (isDarkMode) 0.30f else 0.18f)
                                else Color.Transparent
                            )
                            .clickable {
                                onPageSelected(entry.pageNumber)
                                onDismissRequest()
                            }
                            .padding(
                                start = (14 + entry.level * 16).dp,
                                end = 14.dp,
                                top = 10.dp,
                                bottom = 10.dp
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isCurrent) Icons.Rounded.Check else Icons.Rounded.Bookmark,
                                contentDescription = null,
                                tint = if (isCurrent) AccentBlue else (if (entry.isAiGenerated) Color(0xFFA855F7) else textSecColor.copy(alpha = 0.6f)),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = entry.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isCurrent) FontWeight.Bold else (if (entry.level == 0) FontWeight.SemiBold else FontWeight.Normal),
                                    color = if (isCurrent) (if (isDarkMode) Color.White else AccentBlue) else textColor,
                                    fontSize = if (entry.level == 0) 14.sp else 13.sp
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (entry.isAiGenerated) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF8B5CF6).copy(alpha = 0.18f))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "IA",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFFA855F7),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isCurrent) AccentBlue else (if (isDarkMode) Color(0x35FFFFFF) else Color(0x35000000)))
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "pág. ${entry.pageNumber}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isCurrent) Color.White else textSecColor,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
