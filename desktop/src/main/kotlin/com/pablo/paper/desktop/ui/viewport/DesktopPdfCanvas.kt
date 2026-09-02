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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.paper.desktop.model.Annotation
import com.pablo.paper.desktop.model.AnnotationType
import com.pablo.paper.desktop.model.DesktopTool
import com.pablo.paper.desktop.model.InkPoint
import com.pablo.paper.desktop.model.InkStroke
import com.pablo.paper.desktop.model.TextSelectionRange
import com.pablo.paper.desktop.model.ViewMode
import com.pablo.paper.desktop.state.RightDockTab
import com.pablo.paper.desktop.state.TabDocumentState
import com.pablo.paper.desktop.state.WorkspaceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Native PDFBox viewport. It intentionally owns the document surface so page
 * state, gestures, annotations and rendering all speak to the same engine.
 */
@Composable
fun DesktopPdfCanvas(
    workspaceState: WorkspaceState,
    modifier: Modifier = Modifier
) {
    val tab = workspaceState.activeTab

    when {
        tab == null -> ReadingDeskEmpty(
            onOpenPdf = { openPdfPicker(workspaceState) },
            modifier = modifier
        )
        tab.isLoading -> DocumentLoadingState(tab.title, modifier)
        !tab.isLoaded -> DocumentFailureState(
            title = tab.title,
            message = tab.loadError ?: "No se pudo preparar el documento.",
            onClose = { workspaceState.closeTab(workspaceState.activeTabIndex) },
            modifier = modifier
        )
        else -> ReaderWorkspace(tab = tab, workspaceState = workspaceState, modifier = modifier)
    }
}

@Composable
private fun ReaderWorkspace(
    tab: TabDocumentState,
    workspaceState: WorkspaceState,
    modifier: Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (tab.viewMode) {
            ViewMode.CONTINUOUS_SCROLL -> ContinuousReader(tab, maxWidth, maxHeight)
            ViewMode.TWO_PAGE_SPREAD -> SpreadReader(tab, maxWidth, maxHeight)
            else -> SinglePageReader(tab, maxWidth, maxHeight)
        }

        SelectionActions(
            tab = tab,
            workspaceState = workspaceState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 14.dp)
        )

        ReaderControls(
            tab = tab,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 14.dp)
        )
    }
}

@Composable
private fun ReadingDeskEmpty(onOpenPdf: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.width(420.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = "Tu mesa está vacía",
                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                fontSize = 23.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Abre un PDF para leer, marcar y guardar una sesión de trabajo local.",
                fontSize = 13.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Button(
                onClick = onOpenPdf,
                modifier = Modifier.height(42.dp),
                shape = RoundedCornerShape(7.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Abrir PDF", fontWeight = FontWeight.SemiBold)
            }
            Text(
                text = "Ctrl+O",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DocumentLoadingState(title: String, modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(26.dp))
            Text("Preparando $title", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DocumentFailureState(title: String, message: String, onClose: () -> Unit, modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.width(440.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
            Text(message, textAlign = TextAlign.Center, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onClose, shape = RoundedCornerShape(7.dp)) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Cerrar documento")
            }
        }
    }
}

@Composable
private fun SinglePageReader(tab: TabDocumentState, maxWidth: Dp, maxHeight: Dp) {
    val pageInfo = remember(tab.currentPage, tab.rotation) { tab.engine.getPageInfo(tab.currentPage) }
    val rawAspect = pageInfo?.aspectRatio ?: 0.707f
    val aspect = if (tab.rotation % 180 == 0) rawAspect else 1f / rawAspect
    val availableHeight = (maxHeight - 48.dp).coerceAtLeast(220.dp)
    val pageHeight = (availableHeight.value * tab.zoomScale.coerceIn(0.35f, 4f)).dp
    val pageWidth = (pageHeight.value * aspect).dp

    Box(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        PageSurface(
            tab = tab,
            pageIndex = tab.currentPage,
            width = pageWidth,
            height = pageHeight,
            interactive = true,
            allowPan = true,
            modifier = Modifier.offset { IntOffset(tab.panOffset.x.roundToInt(), tab.panOffset.y.roundToInt()) }
        )
    }
}

@Composable
private fun ContinuousReader(tab: TabDocumentState, maxWidth: Dp, maxHeight: Dp) {
    val listState = rememberLazyListState()
    val pageWidth = (maxWidth * 0.76f * tab.zoomScale.coerceIn(0.45f, 1.8f)).coerceIn(260.dp, 980.dp)

    LaunchedEffect(tab.currentPage) {
        if (tab.currentPage in 0 until tab.pageCount) listState.animateScrollToItem(tab.currentPage)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items((0 until tab.pageCount).toList(), key = { it }) { pageIndex ->
            val info = remember(pageIndex, tab.rotation) { tab.engine.getPageInfo(pageIndex) }
            val rawAspect = info?.aspectRatio ?: 0.707f
            val aspect = if (tab.rotation % 180 == 0) rawAspect else 1f / rawAspect
            val pageHeight = (pageWidth.value / aspect).dp

            PageSurface(
                tab = tab,
                pageIndex = pageIndex,
                width = pageWidth,
                height = pageHeight,
                interactive = false,
                allowPan = false,
                modifier = Modifier.pointerInput(pageIndex) {
                    detectTapGestures { tab.currentPage = pageIndex }
                }
            )
        }
    }
}

@Composable
private fun SpreadReader(tab: TabDocumentState, maxWidth: Dp, maxHeight: Dp) {
    val leftPage = tab.currentPage.coerceAtMost((tab.pageCount - 1).coerceAtLeast(0))
    val rightPage = (leftPage + 1).takeIf { it < tab.pageCount }
    val gap = 16.dp
    val maximumHeight = (maxHeight - 76.dp).coerceAtLeast(220.dp)
    val pageInfo = remember(leftPage, tab.rotation) { tab.engine.getPageInfo(leftPage) }
    val rawAspect = pageInfo?.aspectRatio ?: 0.707f
    val aspect = if (tab.rotation % 180 == 0) rawAspect else 1f / rawAspect
    val widthBoundHeight = ((maxWidth - gap - 64.dp).value / (aspect * if (rightPage == null) 1f else 2f)).dp
    val pageHeight = min(maximumHeight.value, widthBoundHeight.value).coerceAtLeast(200f).times(tab.zoomScale.coerceIn(0.45f, 1.5f)).dp
    val pageWidth = (pageHeight.value * aspect).dp

    Row(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PageSurface(tab, leftPage, pageWidth, pageHeight, interactive = false, allowPan = false)
        if (rightPage != null) {
            Spacer(Modifier.width(gap))
            PageSurface(tab, rightPage, pageWidth, pageHeight, interactive = false, allowPan = false)
        }
    }
}

@Composable
private fun PageSurface(
    tab: TabDocumentState,
    pageIndex: Int,
    width: Dp,
    height: Dp,
    interactive: Boolean,
    allowPan: Boolean,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val targetWidth = remember(width, density) { (with(density) { width.roundToPx() } * 1.25f).roundToInt().coerceIn(320, 4096) }
    val targetHeight = remember(height, density) { (with(density) { height.roundToPx() } * 1.25f).roundToInt().coerceIn(420, 4096) }
    var bitmap by remember(pageIndex, targetWidth, targetHeight, tab.rotation) { mutableStateOf<ImageBitmap?>(null) }
    var textLayout by remember(pageIndex) { mutableStateOf<com.pablo.paper.desktop.model.PageTextLayout?>(null) }
    var dragStart by remember(pageIndex) { mutableStateOf<Offset?>(null) }

    LaunchedEffect(pageIndex, targetWidth, targetHeight, tab.rotation) {
        bitmap = tab.engine.renderPage(pageIndex, targetWidth, targetHeight, tab.rotation)
        if (pageIndex == tab.currentPage) tab.engine.prefetch(pageIndex, targetWidth, targetHeight)
    }
    LaunchedEffect(pageIndex, interactive) {
        if (interactive) textLayout = withContext(Dispatchers.IO) { tab.getPageTextLayout(pageIndex) }
    }

    val pointerModifier = if (!interactive) {
        Modifier
    } else {
        Modifier
            .pointerHoverIcon(
                when (tab.activeTool) {
                    DesktopTool.PAN_HAND -> PointerIcon.Hand
                    DesktopTool.TEXT_SELECTION, DesktopTool.HIGHLIGHT -> PointerIcon.Text
                    else -> PointerIcon.Default
                }
            )
            .pointerInput(tab.activeTool, pageIndex, textLayout) {
                when (tab.activeTool) {
                    DesktopTool.PAN_HAND -> if (allowPan) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            tab.panOffset += dragAmount
                        }
                    }
                    DesktopTool.PEN -> {
                        detectDragGestures(
                            onDragStart = { point ->
                                tab.discardDraft()
                                tab.draftPageIndex = pageIndex
                                tab.currentDraftPoints.add(normalizedPoint(point, size))
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                tab.currentDraftPoints.add(normalizedPoint(change.position, size))
                            },
                            onDragCancel = tab::discardDraft,
                            onDragEnd = {
                                val points = tab.currentDraftPoints.toList()
                                if (points.size > 1) {
                                    tab.addAnnotation(
                                        Annotation(
                                            id = java.util.UUID.randomUUID().toString(),
                                            pageIndex = pageIndex,
                                            type = AnnotationType.INK,
                                            stroke = InkStroke(
                                                points = points,
                                                color = tab.strokeColor.value.toLong(),
                                                strokeWidth = tab.strokeWidth
                                            ),
                                            color = tab.strokeColor.value.toLong(),
                                            strokeWidth = tab.strokeWidth
                                        )
                                    )
                                }
                                tab.discardDraft()
                            }
                        )
                    }
                    DesktopTool.TEXT_SELECTION,
                    DesktopTool.HIGHLIGHT -> {
                        detectDragGestures(
                            onDragStart = { point ->
                                dragStart = point
                                tab.clearSelection()
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val start = dragStart ?: return@detectDragGestures
                                val selected = wordsInSelection(textLayout, start, change.position, size.width.toFloat(), size.height.toFloat())
                                if (selected.isNotEmpty()) {
                                    tab.selectedTextRange = TextSelectionRange(
                                        pageIndex = pageIndex,
                                        selectedWords = selected,
                                        selectedText = selected.joinToString(" ") { it.text }
                                    )
                                }
                            },
                            onDragEnd = { dragStart = null },
                            onDragCancel = { dragStart = null }
                        )
                    }
                    else -> Unit
                }
            }
    }

    Box(
        modifier = modifier
            .size(width, height)
            .shadow(10.dp, RoundedCornerShape(2.dp), clip = false)
            .background(Color.White, RoundedCornerShape(2.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f), RoundedCornerShape(2.dp))
            .then(pointerModifier)
    ) {
        val currentBitmap = bitmap
        if (currentBitmap == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
            }
        } else {
            Image(
                bitmap = currentBitmap,
                contentDescription = "Página ${pageIndex + 1}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
        }

        AnnotationLayer(tab = tab, pageIndex = pageIndex)
    }
}

@Composable
private fun AnnotationLayer(tab: TabDocumentState, pageIndex: Int) {
    val annotations = tab.annotations.filter { it.pageIndex == pageIndex }
    Canvas(modifier = Modifier.fillMaxSize()) {
        annotations.forEach { annotation ->
            val color = Color(annotation.color.toULong())
            when (annotation.type) {
                AnnotationType.INK -> annotation.stroke?.let { stroke ->
                    drawInkStroke(stroke, color)
                }
                AnnotationType.HIGHLIGHT -> annotation.rects.orEmpty().forEach { rect ->
                    drawRect(
                        color = color,
                        alpha = 0.34f,
                        topLeft = Offset(rect.getOrElse(0) { 0f } * size.width, rect.getOrElse(1) { 0f } * size.height),
                        size = androidx.compose.ui.geometry.Size(
                            ((rect.getOrElse(2) { 0f } - rect.getOrElse(0) { 0f }) * size.width).coerceAtLeast(1f),
                            ((rect.getOrElse(3) { 0f } - rect.getOrElse(1) { 0f }) * size.height).coerceAtLeast(1f)
                        )
                    )
                }
                AnnotationType.UNDERLINE,
                AnnotationType.STRIKETHROUGH -> annotation.rects.orEmpty().forEach { rect ->
                    val left = rect.getOrElse(0) { 0f } * size.width
                    val top = rect.getOrElse(1) { 0f } * size.height
                    val right = rect.getOrElse(2) { 0f } * size.width
                    val bottom = rect.getOrElse(3) { 0f } * size.height
                    val y = if (annotation.type == AnnotationType.UNDERLINE) bottom else (top + bottom) / 2f
                    drawLine(color = color, start = Offset(left, y), end = Offset(right, y), strokeWidth = 1.8f)
                }
                else -> Unit
            }
        }
        if (tab.draftPageIndex == pageIndex && tab.currentDraftPoints.size > 1) {
            drawInkStroke(
                stroke = InkStroke(tab.currentDraftPoints.toList(), tab.strokeColor.value.toLong(), tab.strokeWidth),
                color = tab.strokeColor
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawInkStroke(stroke: InkStroke, color: Color) {
    if (stroke.points.size < 2) return
    val path = Path().apply {
        moveTo(stroke.points.first().x * size.width, stroke.points.first().y * size.height)
        stroke.points.drop(1).forEach { point -> lineTo(point.x * size.width, point.y * size.height) }
    }
    drawPath(
        path = path,
        color = color,
        alpha = if (stroke.isHighlighter) 0.35f else stroke.alpha,
        style = Stroke(
            width = if (stroke.isHighlighter) stroke.strokeWidth * 3f else stroke.strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

private fun normalizedPoint(point: Offset, size: androidx.compose.ui.unit.IntSize): InkPoint = InkPoint(
    x = (point.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f),
    y = (point.y / size.height.coerceAtLeast(1)).coerceIn(0f, 1f)
)

private fun wordsInSelection(
    layout: com.pablo.paper.desktop.model.PageTextLayout?,
    start: Offset,
    end: Offset,
    pageWidth: Float,
    pageHeight: Float
): List<com.pablo.paper.desktop.model.TextWord> {
    val selection = Rect(
        left = min(start.x, end.x),
        top = min(start.y, end.y),
        right = max(start.x, end.x),
        bottom = max(start.y, end.y)
    )
    return layout?.words.orEmpty().filter { word ->
        selection.overlaps(
            Rect(
                left = word.x * pageWidth,
                top = word.y * pageHeight,
                right = (word.x + word.width) * pageWidth,
                bottom = (word.y + word.height) * pageHeight
            )
        )
    }
}

@Composable
private fun SelectionActions(tab: TabDocumentState, workspaceState: WorkspaceState, modifier: Modifier) {
    val selection = tab.selectedTextRange ?: return
    if (selection.selectedText.isBlank()) return

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
            .shadow(10.dp, RoundedCornerShape(8.dp))
            .padding(horizontal = 5.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        IconButton(
            onClick = {
                Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(selection.selectedText), null)
                tab.clearSelection()
            },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copiar selección", modifier = Modifier.size(17.dp))
        }
        IconButton(
            onClick = {
                tab.addAnnotation(
                    Annotation(
                        id = java.util.UUID.randomUUID().toString(),
                        pageIndex = selection.pageIndex,
                        type = AnnotationType.HIGHLIGHT,
                        rects = selection.selectedWords.map { it.bounds },
                        color = 0xFFD3A631L
                    )
                )
                tab.clearSelection()
            },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(Icons.Default.Highlight, contentDescription = "Resaltar selección", tint = Color(0xFF9A7212), modifier = Modifier.size(18.dp))
        }
        IconButton(
            onClick = {
                workspaceState.isRightDockOpen = true
                workspaceState.rightDockTab = RightDockTab.AI_ASSISTANT
                workspaceState.sendAiMessage("Explica este fragmento del documento sin inventar contexto:\n\n${selection.selectedText}")
                tab.clearSelection()
            },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = "Preguntar a la IA sobre la selección", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp))
        }
    }
}

@Composable
private fun ReaderControls(tab: TabDocumentState, modifier: Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
            .shadow(8.dp, RoundedCornerShape(8.dp))
            .padding(horizontal = 5.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { if (tab.currentPage > 0) tab.currentPage-- },
            enabled = tab.currentPage > 0,
            modifier = Modifier.size(36.dp)
        ) { Icon(Icons.Default.ChevronLeft, contentDescription = "Página anterior", modifier = Modifier.size(18.dp)) }
        Text(
            text = "${tab.currentPage + 1} / ${tab.pageCount}",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 6.dp)
        )
        IconButton(
            onClick = { if (tab.currentPage < tab.pageCount - 1) tab.currentPage++ },
            enabled = tab.currentPage < tab.pageCount - 1,
            modifier = Modifier.size(36.dp)
        ) { Icon(Icons.Default.ChevronRight, contentDescription = "Página siguiente", modifier = Modifier.size(18.dp)) }
        Spacer(Modifier.width(4.dp))
        IconButton(
            onClick = { tab.zoomScale = (tab.zoomScale / 1.15f).coerceAtLeast(0.35f) },
            modifier = Modifier.size(36.dp)
        ) { Icon(Icons.Default.ZoomOut, contentDescription = "Alejar", modifier = Modifier.size(16.dp)) }
        Text(
            text = "${(tab.zoomScale * 100).roundToInt()}%",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        IconButton(
            onClick = { tab.zoomScale = (tab.zoomScale * 1.15f).coerceAtMost(4f) },
            modifier = Modifier.size(36.dp)
        ) { Icon(Icons.Default.ZoomIn, contentDescription = "Acercar", modifier = Modifier.size(16.dp)) }
    }
}

private fun openPdfPicker(state: WorkspaceState) {
    javax.swing.SwingUtilities.invokeLater {
        val picker = java.awt.FileDialog(null as java.awt.Frame?, "Abrir PDF", java.awt.FileDialog.LOAD)
        picker.setFilenameFilter { _, name -> name.endsWith(".pdf", ignoreCase = true) }
        picker.isVisible = true
        if (picker.file != null) state.openDocument(java.io.File(picker.directory, picker.file))
    }
}
