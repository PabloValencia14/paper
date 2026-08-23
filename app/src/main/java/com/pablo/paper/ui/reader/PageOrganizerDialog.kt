package com.pablo.paper.ui.reader

import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pablo.paper.domain.model.ReaderAction
import com.pablo.paper.domain.model.ReaderState
import com.pablo.paper.pdf.PageSize
import com.pablo.paper.pdf.PdfManipulator
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun PageOrganizerDialog(
    state: ReaderState,
    onAction: (ReaderAction) -> Unit,
    onRenderThumbnail: suspend (pageIndex: Int, width: Int, height: Int) -> Bitmap?,
    getPageSizeForPage: (Int) -> PageSize,
    onRebuildComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!state.isPageOrganizerVisible) return

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }

    // Mutable page sequence list for local reordering
    val pageOrder = remember(state.pageCount) {
        mutableStateListOf<Int>().apply {
            for (i in 0 until state.pageCount) add(i)
        }
    }

    val pageRotations = remember { mutableStateMapOf<Int, Int>() }
    val insertedPages = remember { mutableStateMapOf<Int, String>() }
    var selectedPageIndex by remember { mutableIntStateOf(0) }
    var showInsertMenu by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { onAction(ReaderAction.TogglePageOrganizer) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Top Header Toolbar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onAction(ReaderAction.TogglePageOrganizer) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Organizador de Páginas",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${pageOrder.size} páginas en el documento",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Action Controls: Rotate, Move Left/Right, Duplicate, Delete, Insert
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rotate 90 deg
                        FilledTonalButton(
                            onClick = {
                                val orig = pageOrder.getOrNull(selectedPageIndex) ?: return@FilledTonalButton
                                val currentRot = pageRotations[orig] ?: 0
                                pageRotations[orig] = (currentRot + 90) % 360
                            }
                        ) {
                            Icon(Icons.Default.RotateRight, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Rotar 90°", fontSize = 13.sp)
                        }

                        // Move Left
                        IconButton(
                            enabled = selectedPageIndex > 0,
                            onClick = {
                                if (selectedPageIndex > 0) {
                                    val item = pageOrder.removeAt(selectedPageIndex)
                                    pageOrder.add(selectedPageIndex - 1, item)
                                    selectedPageIndex--
                                }
                            }
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Mover antes")
                        }

                        // Move Right
                        IconButton(
                            enabled = selectedPageIndex < pageOrder.size - 1,
                            onClick = {
                                if (selectedPageIndex < pageOrder.size - 1) {
                                    val item = pageOrder.removeAt(selectedPageIndex)
                                    pageOrder.add(selectedPageIndex + 1, item)
                                    selectedPageIndex++
                                }
                            }
                        ) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "Mover después")
                        }

                        // Duplicate
                        IconButton(
                            onClick = {
                                val item = pageOrder.getOrNull(selectedPageIndex) ?: return@IconButton
                                pageOrder.add(selectedPageIndex + 1, item)
                                selectedPageIndex++
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Duplicar")
                        }

                        // Delete
                        IconButton(
                            enabled = pageOrder.size > 1,
                            onClick = {
                                if (pageOrder.size > 1) {
                                    pageOrder.removeAt(selectedPageIndex)
                                    selectedPageIndex = selectedPageIndex.coerceAtMost(pageOrder.size - 1)
                                }
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }

                        // Insert Blank
                        Box {
                            OutlinedButton(onClick = { showInsertMenu = true }) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Insertar Hoja", fontSize = 13.sp)
                            }
                            DropdownMenu(
                                expanded = showInsertMenu,
                                onDismissRequest = { showInsertMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Página Lisa (Blanco)") },
                                    onClick = {
                                        val cur = pageOrder.getOrNull(selectedPageIndex) ?: 0
                                        insertedPages[cur] = "BLANK"
                                        showInsertMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Página Punteada (Dot Grid)") },
                                    onClick = {
                                        val cur = pageOrder.getOrNull(selectedPageIndex) ?: 0
                                        insertedPages[cur] = "DOT"
                                        showInsertMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Página Cuadriculada (Grid)") },
                                    onClick = {
                                        val cur = pageOrder.getOrNull(selectedPageIndex) ?: 0
                                        insertedPages[cur] = "GRID"
                                        showInsertMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Página Rayada (Lined)") },
                                    onClick = {
                                        val cur = pageOrder.getOrNull(selectedPageIndex) ?: 0
                                        insertedPages[cur] = "LINED"
                                        showInsertMenu = false
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Save & Apply Changes
                        Button(
                            onClick = {
                                val doc = state.document ?: return@Button
                                isProcessing = true
                                coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                    val uri = android.net.Uri.parse(doc.uri)
                                    val sourcePath = uri.path ?: doc.uri
                                    val sourceFile = java.io.File(sourcePath)
                                    val parent = sourceFile.parentFile ?: context.cacheDir
                                    val tempOut = java.io.File(parent, "rebuilt_${System.currentTimeMillis()}.pdf")
                                    val success = PdfManipulator.rebuildDocument(
                                        context = context,
                                        sourceFile = sourceFile,
                                        pageOrder = pageOrder.toList(),
                                        rotations = pageRotations.toMap(),
                                        insertedBlankPages = insertedPages.toMap(),
                                        outputFile = tempOut
                                    )
                                    if (success && tempOut.exists()) {
                                        tempOut.copyTo(sourceFile, overwrite = true)
                                        tempOut.delete()
                                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                            onRebuildComplete()
                                        }
                                    }
                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                        isProcessing = false
                                        onAction(ReaderAction.TogglePageOrganizer)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Guardar Cambios", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Pages Visual Grid
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    itemsIndexed(pageOrder) { index, origPageIndex ->
                        val isSelected = index == selectedPageIndex
                        val rotation = pageRotations[origPageIndex] ?: 0
                        val pageSize = getPageSizeForPage(origPageIndex)
                        val aspect = if (pageSize.height > 0) pageSize.width.toFloat() / pageSize.height.toFloat() else 0.75f

                        OrganizerCard(
                            displayIndex = index + 1,
                            originalPageIndex = origPageIndex,
                            rotation = rotation,
                            aspectRatio = aspect,
                            isSelected = isSelected,
                            onRenderThumbnail = onRenderThumbnail,
                            onClick = { selectedPageIndex = index }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrganizerCard(
    displayIndex: Int,
    originalPageIndex: Int,
    rotation: Int,
    aspectRatio: Float,
    isSelected: Boolean,
    onRenderThumbnail: suspend (pageIndex: Int, width: Int, height: Int) -> Bitmap?,
    onClick: () -> Unit
) {
    var thumbBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(originalPageIndex) {
        val bmp = onRenderThumbnail(originalPageIndex, 300, (300 / aspectRatio).toInt())
        if (bmp != null) {
            thumbBitmap = bmp
        }
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White)
                    .rotate(rotation.toFloat()),
                contentAlignment = Alignment.Center
            ) {
                val bmp = thumbBitmap
                if (bmp != null && !bmp.isRecycled) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Pág $displayIndex",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("$displayIndex", color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Página $displayIndex",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp
                    ),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                if (rotation != 0) {
                    Text(
                        text = "$rotation°",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
