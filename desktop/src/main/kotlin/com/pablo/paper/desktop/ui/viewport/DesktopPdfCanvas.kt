package com.pablo.paper.desktop.ui.viewport

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.paper.desktop.model.Annotation
import com.pablo.paper.desktop.model.AnnotationType
import com.pablo.paper.desktop.model.DesktopTool
import com.pablo.paper.desktop.model.InkPoint
import com.pablo.paper.desktop.model.InkStroke
import com.pablo.paper.desktop.model.TextSelectionRange
import com.pablo.paper.desktop.model.TextWord
import com.pablo.paper.desktop.model.ViewMode
import com.pablo.paper.desktop.state.RightDockTab
import com.pablo.paper.desktop.state.TabDocumentState
import com.pablo.paper.desktop.state.WorkspaceState
import java.awt.FileDialog
import java.awt.Frame
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun DesktopPdfCanvas(
    workspaceState: WorkspaceState,
    modifier: Modifier = Modifier
) {
    val tab = workspaceState.activeTab

    if (tab == null || !tab.isLoaded || tab.pageCount <= 0) {
        MinimalistWelcomeScreen(
            onOpenPdf = {
                javax.swing.SwingUtilities.invokeLater {
                    val fd = FileDialog(null as Frame?, "Abrir Documento PDF", FileDialog.LOAD)
                    fd.setFilenameFilter { _, name -> name.lowercase().endsWith(".pdf") }
                    fd.isVisible = true
                    if (fd.file != null) workspaceState.openDocument(File(fd.directory, fd.file))
                }
            },
            onOpenSample = {
                val sample = File("sample_paper.pdf")
                if (sample.exists()) workspaceState.openDocument(sample)
            }
        )
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .background(MaterialTheme.colorScheme.background)
    ) {

        PdfJsViewer(
            tab = tab,
            modifier = Modifier.fillMaxSize(),
            viewMode = tab.viewMode
        )

        // Floating Bottom HUD Pill Bar (Acrobat Style)
        FloatingPdfHud(
            tab = tab,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
fun MinimalistWelcomeScreen(
    onOpenPdf: () -> Unit,
    onOpenSample: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.width(460.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFEB1000).copy(alpha = 0.12f))
                    .border(1.dp, Color(0xFFEB1000).copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = Color(0xFFEB1000),
                    modifier = Modifier.size(30.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Paper",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Lector y editor de documentos PDF de alto rendimiento",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = onOpenPdf,
                modifier = Modifier
                    .height(42.dp)
                    .width(220.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(8.dp))
                Text(text = "Abrir archivo PDF", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            Text(
                text = "o arrastra y suelta un archivo aquí",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            val sampleFile = remember { File("sample_paper.pdf") }
            if (sampleFile.exists()) {
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenSample() },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = sampleFile.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = "Documento de muestra • ${(sampleFile.length() / 1024)} KB", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SinglePageRenderer(
    tab: TabDocumentState,
    workspaceState: WorkspaceState,
    containerWidth: Int,
    containerHeight: Int
) {
    val pageInfo = remember(tab.currentPage) { tab.engine.getPageInfo(tab.currentPage) }
    val aspect = pageInfo?.aspectRatio ?: 0.707f
    val basePdfW = pageInfo?.width ?: 595
    val basePdfH = pageInfo?.height ?: 842

    // Compute display size in DP based on container and zoomScale
    val availableH = (containerHeight - 70).coerceAtLeast(400)
    val baseDisplayH = availableH.toFloat()
    val baseDisplayW = baseDisplayH * aspect

    val displayWidthDp = (baseDisplayW * tab.zoomScale).dp
    val displayHeightDp = (baseDisplayH * tab.zoomScale).dp

    // High-resolution pixel dimensions for rasterization
    val renderTargetW = (baseDisplayW * tab.zoomScale * 1.5f).toInt().coerceIn(400, 8192)
    val renderTargetH = (baseDisplayH * tab.zoomScale * 1.5f).toInt().coerceIn(500, 8192)

    var renderedBitmap by remember(tab.currentPage, tab.zoomScale, tab.rotation) {
        mutableStateOf<ImageBitmap?>(null)
    }

    // Text Layout for Selection
    val textLayout = remember(tab.currentPage) {
        tab.getPageTextLayout(tab.currentPage)
    }

    var dragStartOffset by remember { mutableStateOf<Offset?>(null) }
    var dragCurrentOffset by remember { mutableStateOf<Offset?>(null) }

    LaunchedEffect(tab.currentPage, tab.zoomScale, tab.rotation, renderTargetW, renderTargetH) {
        renderedBitmap = tab.engine.renderPage(tab.currentPage, renderTargetW, renderTargetH, tab.rotation)
        tab.engine.prefetch(tab.currentPage, renderTargetW, renderTargetH)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(tab.activeTool) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        // Smooth Mouse Wheel Zoom with Ctrl
                        if (event.type == PointerEventType.Scroll && event.keyboardModifiers.isCtrlPressed) {
                            val delta = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f
                            if (delta < 0) {
                                tab.zoomScale = (tab.zoomScale * 1.15f).coerceAtMost(6.0f)
                            } else if (delta > 0) {
                                tab.zoomScale = (tab.zoomScale / 1.15f).coerceAtLeast(0.25f)
                            }
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Main Paper Sheet
        Box(
            modifier = Modifier
                .offset { IntOffset(tab.panOffset.x.roundToInt(), tab.panOffset.y.roundToInt()) }
                .size(displayWidthDp, displayHeightDp)
                .shadow(12.dp, RoundedCornerShape(2.dp))
                .background(Color.White, RoundedCornerShape(2.dp))
                .pointerHoverIcon(
                    when (tab.activeTool) {
                        DesktopTool.TEXT_SELECTION, DesktopTool.HIGHLIGHT -> PointerIcon.Text
                        DesktopTool.PAN_HAND -> PointerIcon.Hand
                        else -> PointerIcon.Default
                    }
                )
                .pointerInput(tab.activeTool, textLayout) {
                    if (tab.activeTool == DesktopTool.PAN_HAND) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            tab.panOffset += dragAmount
                        }
                    } else if (tab.activeTool == DesktopTool.TEXT_SELECTION || tab.activeTool == DesktopTool.HIGHLIGHT) {
                        // Native Text Drag Selection
                        detectDragGestures(
                            onDragStart = { offset ->
                                dragStartOffset = offset
                                dragCurrentOffset = offset
                                tab.clearSelection()
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                dragCurrentOffset = change.position

                                val start = dragStartOffset ?: return@detectDragGestures
                                val cur = dragCurrentOffset ?: return@detectDragGestures

                                val selRect = Rect(
                                    left = min(start.x, cur.x),
                                    top = min(start.y, cur.y),
                                    right = max(start.x, cur.x),
                                    bottom = max(start.y, cur.y)
                                )

                                // Convert selection rect to normalized coordinates 0..1
                                val pageW = size.width.toFloat()
                                val pageH = size.height.toFloat()

                                val selectedWords = textLayout.words.filter { word ->
                                    val wordRect = Rect(
                                        left = word.x * pageW,
                                        top = word.y * pageH,
                                        right = (word.x + word.width) * pageW,
                                        bottom = (word.y + word.height) * pageH
                                    )
                                    selRect.overlaps(wordRect)
                                }

                                if (selectedWords.isNotEmpty()) {
                                    val fullStr = selectedWords.joinToString(" ") { it.text }
                                    tab.selectedTextRange = TextSelectionRange(
                                        pageIndex = tab.currentPage,
                                        selectedWords = selectedWords,
                                        selectedText = fullStr
                                    )
                                }
                            },
                            onDragEnd = {
                                dragStartOffset = null
                                dragCurrentOffset = null
                            }
                        )
                    } else if (tab.activeTool == DesktopTool.PEN) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                tab.currentDraftPoints.clear()
                                tab.currentDraftPoints.add(InkPoint(offset.x, offset.y))
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                tab.currentDraftPoints.add(InkPoint(change.position.x, change.position.y))
                            },
                            onDragEnd = {
                                if (tab.currentDraftPoints.size > 1) {
                                    val stroke = InkStroke(
                                        points = tab.currentDraftPoints.toList(),
                                        color = tab.strokeColor.value.toLong(),
                                        strokeWidth = tab.strokeWidth,
                                        isHighlighter = false
                                    )
                                    tab.addAnnotation(
                                        Annotation(
                                            id = java.util.UUID.randomUUID().toString(),
                                            pageIndex = tab.currentPage,
                                            type = AnnotationType.INK,
                                            stroke = stroke
                                        )
                                    )
                                }
                                tab.currentDraftPoints.clear()
                            }
                        )
                    } else if (tab.activeTool == DesktopTool.STICKY_NOTE) {
                        detectTapGestures { offset ->
                            tab.addAnnotation(
                                Annotation(
                                    id = java.util.UUID.randomUUID().toString(),
                                    pageIndex = tab.currentPage,
                                    type = AnnotationType.STICKY_NOTE,
                                    textContent = "Nota adhesiva",
                                    rects = listOf(floatArrayOf(offset.x, offset.y, offset.x + 20f, offset.y + 20f))
                                )
                            )
                        }
                    }
                }
        ) {
            val bmp = renderedBitmap
            if (bmp != null) {
                Image(
                    bitmap = bmp,
                    contentDescription = "Página ${tab.currentPage + 1}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp)
                }
            }

            // Real-Time Text Selection Highlighting Layer
            Canvas(modifier = Modifier.fillMaxSize()) {
                val sel = tab.selectedTextRange
                if (sel != null && sel.pageIndex == tab.currentPage) {
                    for (word in sel.selectedWords) {
                        val rx = word.x * size.width
                        val ry = word.y * size.height
                        val rw = word.width * size.width
                        val rh = word.height * size.height

                        drawRect(
                            color = Color(0xFF0078D4),
                            topLeft = Offset(rx, ry),
                            size = androidx.compose.ui.geometry.Size(rw, rh),
                            alpha = 0.35f
                        )
                    }
                }

                // Render vector annotations
                val pageAnnotations = tab.annotations.filter { it.pageIndex == tab.currentPage }
                for (ann in pageAnnotations) {
                    val st = ann.stroke
                    if (st != null && st.points.size > 1) {
                        val path = Path().apply {
                            moveTo(st.points[0].x, st.points[0].y)
                            for (p in 1 until st.points.size) {
                                lineTo(st.points[p].x, st.points[p].y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = Color(st.color.toULong()),
                            alpha = if (st.isHighlighter) 0.4f else 1.0f,
                            style = Stroke(
                                width = if (st.isHighlighter) st.strokeWidth * 3f else st.strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }

                if (tab.currentDraftPoints.size > 1) {
                    val path = Path().apply {
                        moveTo(tab.currentDraftPoints[0].x, tab.currentDraftPoints[0].y)
                        for (p in 1 until tab.currentDraftPoints.size) {
                            lineTo(tab.currentDraftPoints[p].x, tab.currentDraftPoints[p].y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = tab.strokeColor,
                        alpha = 1.0f,
                        style = Stroke(
                            width = tab.strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            // Sticky Note icons
            tab.annotations.filter { it.pageIndex == tab.currentPage && it.type == AnnotationType.STICKY_NOTE }.forEach { note ->
                val r = note.rects?.firstOrNull() ?: floatArrayOf(20f, 20f, 40f, 40f)
                Box(
                    modifier = Modifier
                        .offset { IntOffset(r[0].roundToInt(), r[1].roundToInt()) }
                        .size(22.dp)
                        .background(Color(0xFFF59E0B), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.ChatBubble, contentDescription = "Nota", tint = Color.White, modifier = Modifier.size(13.dp))
                }
            }
        }

        // Floating Text Selection Action Pill (Acrobat / Edge Style)
        val selection = tab.selectedTextRange
        if (selection != null && selection.selectedText.isNotBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .shadow(12.dp, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Copy
                    Button(
                        onClick = {
                            val str = selection.selectedText
                            val sel = StringSelection(str)
                            Toolkit.getDefaultToolkit().systemClipboard.setContents(sel, sel)
                            tab.clearSelection()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Copiar", fontSize = 11.sp)
                    }

                    // Highlight
                    Button(
                        onClick = {
                            val boundsList = selection.selectedWords.map { it.bounds }
                            tab.addAnnotation(
                                Annotation(
                                    id = java.util.UUID.randomUUID().toString(),
                                    pageIndex = tab.currentPage,
                                    type = AnnotationType.HIGHLIGHT,
                                    rects = boundsList
                                )
                            )
                            tab.clearSelection()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.Highlight, contentDescription = null, tint = Color.Black, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Resaltar", fontSize = 11.sp, color = Color.Black)
                    }

                    // Ask AI
                    Button(
                        onClick = {
                            workspaceState.isRightDockOpen = true
                            workspaceState.rightDockTab = RightDockTab.AI_ASSISTANT
                            workspaceState.sendAiMessage("Explica o analiza este fragmento del documento:\n\"${selection.selectedText}\"")
                            tab.clearSelection()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Preguntar a la IA", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
fun ContinuousScrollRenderer(
    tab: TabDocumentState,
    containerWidth: Int
) {
    val listState = rememberLazyListState()
    val targetW = (containerWidth * 0.7f * tab.zoomScale).toInt().coerceIn(350, 4000)

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(tab.pageCount) { pageIndex ->
            var pageBmp by remember(pageIndex, tab.zoomScale, tab.rotation) { mutableStateOf<ImageBitmap?>(null) }
            val pageInfo = remember(pageIndex) { tab.engine.getPageInfo(pageIndex) }
            val aspect = pageInfo?.aspectRatio ?: 0.707f
            val targetH = (targetW / aspect).toInt()

            LaunchedEffect(pageIndex, tab.zoomScale, tab.rotation) {
                pageBmp = tab.engine.renderPage(pageIndex, (targetW * 1.5f).toInt(), (targetH * 1.5f).toInt(), tab.rotation)
            }

            Box(
                modifier = Modifier
                    .size((targetW / 1.25f).dp, (targetH / 1.25f).dp)
                    .shadow(10.dp, RoundedCornerShape(2.dp))
                    .background(Color.White, RoundedCornerShape(2.dp)),
                contentAlignment = Alignment.Center
            ) {
                val bmp = pageBmp
                if (bmp != null) {
                    Image(
                        bitmap = bmp,
                        contentDescription = "Página ${pageIndex + 1}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                } else {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp)
                }
            }
        }
    }
}

@Composable
fun TwoPageSpreadRenderer(
    tab: TabDocumentState,
    containerWidth: Int,
    containerHeight: Int
) {
    val leftPage = tab.currentPage
    val rightPage = (tab.currentPage + 1).takeIf { it < tab.pageCount }

    Row(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SinglePageSpreadItem(tab = tab, pageIndex = leftPage, maxHeight = containerHeight - 60)
        Spacer(Modifier.width(16.dp))
        if (rightPage != null) {
            SinglePageSpreadItem(tab = tab, pageIndex = rightPage, maxHeight = containerHeight - 60)
        }
    }
}

@Composable
fun SinglePageSpreadItem(tab: TabDocumentState, pageIndex: Int, maxHeight: Int) {
    var bmp by remember(pageIndex, tab.zoomScale, tab.rotation) { mutableStateOf<ImageBitmap?>(null) }
    val pageInfo = remember(pageIndex) { tab.engine.getPageInfo(pageIndex) }
    val aspect = pageInfo?.aspectRatio ?: 0.707f
    val targetH = (maxHeight * 0.85f * tab.zoomScale).toInt().coerceIn(300, 3000)
    val targetW = (targetH * aspect).toInt()

    LaunchedEffect(pageIndex, tab.zoomScale, tab.rotation) {
        bmp = tab.engine.renderPage(pageIndex, (targetW * 1.5f).toInt(), (targetH * 1.5f).toInt(), tab.rotation)
    }

    Box(
        modifier = Modifier
            .size((targetW / 1.25f).dp, (targetH / 1.25f).dp)
            .shadow(10.dp, RoundedCornerShape(2.dp))
            .background(Color.White, RoundedCornerShape(2.dp)),
        contentAlignment = Alignment.Center
    ) {
        val img = bmp
        if (img != null) {
            Image(
                bitmap = img,
                contentDescription = "Página ${pageIndex + 1}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
        } else {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp)
        }
    }
}

@Composable
fun FloatingPdfHud(
    tab: TabDocumentState,
    modifier: Modifier = Modifier
) {
    var showZoomMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            .shadow(16.dp, RoundedCornerShape(24.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Page Backward
            IconButton(
                onClick = { if (tab.currentPage > 0) tab.currentPage-- },
                enabled = tab.currentPage > 0,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Página anterior", modifier = Modifier.size(18.dp))
            }

            // Page Number
            Text(
                text = "${tab.currentPage + 1} / ${tab.pageCount}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // Page Forward
            IconButton(
                onClick = { if (tab.currentPage < tab.pageCount - 1) tab.currentPage++ },
                enabled = tab.currentPage < tab.pageCount - 1,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Página siguiente", modifier = Modifier.size(18.dp))
            }

            Box(modifier = Modifier.height(18.dp).width(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)))

            // Fit to Width
            IconButton(
                onClick = { tab.zoomScale = 1.35f; tab.panOffset = Offset.Zero },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.FitScreen, contentDescription = "Ajustar al ancho", modifier = Modifier.size(15.dp))
            }

            // Fit to Page
            IconButton(
                onClick = { tab.zoomScale = 1.0f; tab.panOffset = Offset.Zero },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.Description, contentDescription = "Ajustar a página", modifier = Modifier.size(15.dp))
            }

            Box(modifier = Modifier.height(18.dp).width(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)))

            // Zoom Out
            IconButton(
                onClick = { tab.zoomScale = (tab.zoomScale / 1.15f).coerceAtLeast(0.25f) },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.ZoomOut, contentDescription = "Zoom -", modifier = Modifier.size(15.dp))
            }

            // Zoom Percentage Dropdown
            Box {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { showZoomMenu = true }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${(tab.zoomScale * 100).roundToInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                DropdownMenu(expanded = showZoomMenu, onDismissRequest = { showZoomMenu = false }) {
                    val presets = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 3.0f, 4.0f)
                    presets.forEach { scale ->
                        DropdownMenuItem(
                            text = { Text("${(scale * 100).toInt()}%", fontSize = 12.sp) },
                            onClick = {
                                tab.zoomScale = scale
                                showZoomMenu = false
                            }
                        )
                    }
                }
            }

            // Zoom In
            IconButton(
                onClick = { tab.zoomScale = (tab.zoomScale * 1.15f).coerceAtMost(6.0f) },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.ZoomIn, contentDescription = "Zoom +", modifier = Modifier.size(15.dp))
            }
        }
    }
}
