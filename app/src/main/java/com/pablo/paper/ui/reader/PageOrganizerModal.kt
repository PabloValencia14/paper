package com.pablo.paper.ui.reader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesomeMotion
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.RotateRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageOrganizerModal(
    currentPage: Int,
    pageCount: Int,
    pageRotations: Map<Int, Int> = emptyMap(), // pageIndex -> degrees (0, 90, 180, 270)
    isDarkMode: Boolean = false,
    onPageSelected: (Int) -> Unit,
    onRotatePage: (Int) -> Unit,
    onMovePageUp: (Int) -> Unit,
    onMovePageDown: (Int) -> Unit,
    onDeletePage: (Int) -> Unit,
    onInsertBlankPage: (Int) -> Unit,
    onDismissRequest: () -> Unit
) {
    var selectedPage by remember { mutableIntStateOf(currentPage) }
    val pages = (1..pageCount).toList()

    val cardBg = if (isDarkMode) Color(0x20FFFFFF) else Color(0x25000000)
    val textColor = if (isDarkMode) TextPrimaryDark else TextPrimary
    val textSecColor = if (isDarkMode) TextSecondaryDark else TextSecondary
    val borderColor = if (isDarkMode) BorderGlassDark else BorderGlassLight

    LiquidGlassDialog(
        onDismissRequest = onDismissRequest,
        isDarkMode = isDarkMode,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .width(740.dp)
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
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AccentBlue.copy(alpha = if (isDarkMode) 0.25f else 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesomeMotion,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Organizador de Páginas",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = "$pageCount páginas en el documento",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = textSecColor,
                                fontSize = 12.sp
                            )
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

            Spacer(modifier = Modifier.height(14.dp))

            // Action Bar for Selected Page
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = cardBg,
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Página seleccionada: #$selectedPage",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = textColor,
                            fontSize = 13.sp
                        )
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rotate
                        OutlinedButton(
                            onClick = { onRotatePage(selectedPage) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Rounded.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Girar 90°", fontSize = 12.sp)
                        }

                        // Move Left / Up
                        IconButton(
                            onClick = {
                                if (selectedPage > 1) {
                                    onMovePageUp(selectedPage)
                                    selectedPage--
                                }
                            },
                            enabled = selectedPage > 1,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Rounded.ArrowBack, contentDescription = "Mover anterior", tint = if (selectedPage > 1) textColor else textSecColor.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                        }

                        // Move Right / Down
                        IconButton(
                            onClick = {
                                if (selectedPage < pageCount) {
                                    onMovePageDown(selectedPage)
                                    selectedPage++
                                }
                            },
                            enabled = selectedPage < pageCount,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Rounded.ArrowForward, contentDescription = "Mover siguiente", tint = if (selectedPage < pageCount) textColor else textSecColor.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                        }

                        // Insert Blank Page
                        OutlinedButton(
                            onClick = { onInsertBlankPage(selectedPage) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Insertar Blanca", fontSize = 12.sp)
                        }

                        // Delete Page
                        if (pageCount > 1) {
                            IconButton(
                                onClick = {
                                    onDeletePage(selectedPage)
                                    if (selectedPage > 1) selectedPage--
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Eliminar página", tint = Color(0xFFE53935), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Page Thumbnails Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 110.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(pages) { pageNum ->
                    val isSelected = pageNum == selectedPage
                    val rotation = pageRotations[pageNum] ?: 0

                    Box(
                        modifier = Modifier
                            .aspectRatio(0.75f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) AccentBlue.copy(alpha = if (isDarkMode) 0.30f else 0.18f) else cardBg)
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) AccentBlue else borderColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedPage = pageNum
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "$pageNum",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) (if (isDarkMode) Color.White else AccentBlue) else textColor,
                                    fontSize = 22.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (pageNum == currentPage) "Actual" else "Página",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (pageNum == currentPage) (if (isDarkMode) Color.White.copy(alpha = 0.8f) else AccentBlue) else textSecColor,
                                    fontSize = 11.sp,
                                    fontWeight = if (pageNum == currentPage) FontWeight.Bold else FontWeight.Normal
                                )
                            )

                            if (rotation != 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .background(AccentBlue, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("$rotation°", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer: Jump to selected
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        onPageSelected(selectedPage)
                        onDismissRequest()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue, contentColor = Color.White)
                ) {
                    Text("Ir a Página $selectedPage", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
