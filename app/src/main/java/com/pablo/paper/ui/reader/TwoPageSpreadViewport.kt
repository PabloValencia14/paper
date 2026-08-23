package com.pablo.paper.ui.reader

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.pablo.paper.domain.model.Annotation
import com.pablo.paper.domain.model.ReaderAction
import com.pablo.paper.domain.model.ReaderState
import com.pablo.paper.ink.InkController
import com.pablo.paper.pdf.PageSize
import com.pablo.paper.ui.ink.InkCanvas

@Composable
fun TwoPageSpreadViewport(
    state: ReaderState,
    inkController: InkController,
    documentId: String,
    onRenderPage: suspend (pageIndex: Int, width: Int, height: Int) -> Bitmap?,
    getPageSizeForPage: (Int) -> PageSize,
    getAnnotationsForPage: suspend (Int) -> List<Annotation>,
    onAnnotationCreated: (Annotation) -> Unit,
    onAction: (ReaderAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current.density
    val isDark = state.isDarkMode || state.paperColor.isDarkPalette
    val textureShader = rememberTextureShader(state.paperTexture, isDark, density, state.paperTexturePoints)

    val leftPageIndex = (state.currentPage - 1).coerceAtLeast(0)
    val rightPageIndex = (state.currentPage).coerceAtMost(state.pageCount - 1)
    val hasRightPage = rightPageIndex > leftPageIndex && rightPageIndex < state.pageCount

    var zoom by remember { mutableFloatStateOf(1f) }
    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }

    val darkColorMatrix = remember {
        androidx.compose.ui.graphics.ColorMatrix(
            floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    }

    val sepiaColorMatrix = remember {
        androidx.compose.ui.graphics.ColorMatrix(
            floatArrayOf(
                0.393f, 0.769f, 0.189f, 0f, 0f,
                0.349f, 0.686f, 0.168f, 0f, 0f,
                0.272f, 0.534f, 0.131f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(state.paperColor.getColor(state.isDarkMode))
    ) {
        // Texture background
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (textureShader != null) {
                val matrix = android.graphics.Matrix()
                textureShader.setLocalMatrix(matrix)
                val paint = android.graphics.Paint().apply {
                    shader = textureShader
                    isAntiAlias = true
                    isDither = true
                }
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawRect(0f, 0f, size.width, size.height, paint)
                }
            }
        }

        // Zoom, Pan & Tap gestures for 2-page spread
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures(panZoomLock = false) { _, pan, zoomFactor, _ ->
                        val newZoom = (zoom * zoomFactor).coerceIn(1f, 5f)
                        if (newZoom <= 1.05f) {
                            zoom = 1f
                            panX = 0f
                            panY = 0f
                        } else {
                            zoom = newZoom
                            panX += pan.x
                            panY += pan.y
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (zoom > 1.05f) {
                                zoom = 1f
                                panX = 0f
                                panY = 0f
                            } else {
                                zoom = 2.2f
                            }
                        },
                        onTap = { offset ->
                            val w = size.width.toFloat()
                            val h = size.height.toFloat()
                            if (offset.y > 200f) {
                                if (offset.y > h * 0.82f && offset.x in (w * 0.20f)..(w * 0.80f)) {
                                    onAction(ReaderAction.TogglePageNavigator)
                                } else if (offset.x < w * 0.18f) {
                                    onAction(ReaderAction.PreviousPage)
                                } else if (offset.x > w * 0.82f) {
                                    onAction(ReaderAction.NextPage)
                                } else {
                                    onAction(ReaderAction.ToggleToolbarCollapse)
                                }
                            }
                        }
                    )
                }
                .padding(
                    top = if (state.isToolbarCollapsed) 16.dp else 72.dp,
                    bottom = 24.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = zoom,
                        scaleY = zoom,
                        translationX = panX,
                        translationY = panY
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Page
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    val pageSize = getPageSizeForPage(leftPageIndex)
                    val aspect = if (pageSize.height > 0) pageSize.width.toFloat() / pageSize.height.toFloat() else 0.75f
                    SpreadPageCard(
                        pageIndex = leftPageIndex,
                        aspectRatio = aspect,
                        pageSize = pageSize,
                        documentId = documentId,
                        state = state,
                        inkController = inkController,
                        zoom = zoom,
                        panOffsetX = panX,
                        panOffsetY = panY,
                        onZoomPanChanged = { z, px, py ->
                            zoom = z
                            panX = px
                            panY = py
                        },
                        isDarkMode = state.isDarkMode,
                        isDarkPalette = state.paperColor.isDarkPalette,
                        isSepiaMode = state.isSepiaMode,
                        darkColorMatrix = darkColorMatrix,
                        sepiaColorMatrix = sepiaColorMatrix,
                        onRenderPage = onRenderPage,
                        getAnnotationsForPage = getAnnotationsForPage,
                        onAnnotationCreated = onAnnotationCreated,
                        onAction = onAction
                    )
                }

                // Center Book Spine
                Spacer(
                    modifier = Modifier
                        .width(3.dp)
                        .fillMaxHeight(0.92f)
                        .background(Color.Black.copy(alpha = 0.12f), RoundedCornerShape(1.5.dp))
                )

                // Right Page (if exists)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasRightPage) {
                        val pageSize = getPageSizeForPage(rightPageIndex)
                        val aspect = if (pageSize.height > 0) pageSize.width.toFloat() / pageSize.height.toFloat() else 0.75f
                        SpreadPageCard(
                            pageIndex = rightPageIndex,
                            aspectRatio = aspect,
                            pageSize = pageSize,
                            documentId = documentId,
                            state = state,
                            inkController = inkController,
                            zoom = zoom,
                            panOffsetX = panX,
                            panOffsetY = panY,
                            onZoomPanChanged = { z, px, py ->
                                zoom = z
                                panX = px
                                panY = py
                            },
                            isDarkMode = state.isDarkMode,
                            isDarkPalette = state.paperColor.isDarkPalette,
                            isSepiaMode = state.isSepiaMode,
                            darkColorMatrix = darkColorMatrix,
                            sepiaColorMatrix = sepiaColorMatrix,
                            onRenderPage = onRenderPage,
                            getAnnotationsForPage = getAnnotationsForPage,
                            onAnnotationCreated = onAnnotationCreated,
                            onAction = onAction
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpreadPageCard(
    pageIndex: Int,
    aspectRatio: Float,
    pageSize: PageSize,
    documentId: String,
    state: ReaderState,
    inkController: InkController,
    zoom: Float = 1f,
    panOffsetX: Float = 0f,
    panOffsetY: Float = 0f,
    onZoomPanChanged: (Float, Float, Float) -> Unit = { _, _, _ -> },
    isDarkMode: Boolean,
    isDarkPalette: Boolean,
    isSepiaMode: Boolean,
    darkColorMatrix: androidx.compose.ui.graphics.ColorMatrix,
    sepiaColorMatrix: androidx.compose.ui.graphics.ColorMatrix,
    onRenderPage: suspend (pageIndex: Int, width: Int, height: Int) -> Bitmap?,
    getAnnotationsForPage: suspend (Int) -> List<Annotation>,
    onAnnotationCreated: (Annotation) -> Unit,
    onAction: (ReaderAction) -> Unit
) {
    var pageBitmap by remember(pageIndex) { mutableStateOf<Bitmap?>(null) }
    var pageAnnotations by remember(documentId, pageIndex) { mutableStateOf<List<Annotation>>(emptyList()) }

    LaunchedEffect(documentId, pageIndex) {
        pageAnnotations = getAnnotationsForPage(pageIndex)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxHeight(0.96f)
            .aspectRatio(aspectRatio),
        contentAlignment = Alignment.Center
    ) {
        val widthPx = (constraints.maxWidth).coerceIn(600, 2400)
        val heightPx = (widthPx / aspectRatio).toInt().coerceIn(800, 3200)

        LaunchedEffect(pageIndex, widthPx, heightPx) {
            val bmp = onRenderPage(pageIndex, widthPx, heightPx)
            if (bmp != null) {
                pageBitmap = bmp
            }
        }

        val bmp = pageBitmap
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (bmp != null && !bmp.isRecycled) {
                val srcSize = IntSize(bmp.width, bmp.height)
                val dstSize = IntSize(size.width.toInt(), size.height.toInt())
                val filter = when {
                    isSepiaMode -> ColorFilter.colorMatrix(sepiaColorMatrix)
                    isDarkMode && !isDarkPalette -> ColorFilter.colorMatrix(darkColorMatrix)
                    else -> null
                }
                val blendMode = if (isDarkMode || isDarkPalette) androidx.compose.ui.graphics.BlendMode.Screen else androidx.compose.ui.graphics.BlendMode.Multiply

                drawImage(
                    image = bmp.asImageBitmap(),
                    srcOffset = IntOffset.Zero,
                    srcSize = srcSize,
                    dstOffset = IntOffset.Zero,
                    dstSize = dstSize,
                    filterQuality = androidx.compose.ui.graphics.FilterQuality.High,
                    colorFilter = filter,
                    blendMode = blendMode
                )
            }
        }

        // Optical Inking & Drawing Canvas Overlay for this specific page
        val bounds = Rect(0f, 0f, constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
        InkCanvas(
            inkController = inkController,
            pageBounds = bounds,
            pageSize = pageSize,
            documentId = documentId,
            pageIndex = pageIndex,
            annotations = pageAnnotations,
            isInkModeEnabled = true,
            isSelectModeEnabled = state.isSelectTextMode,
            zoom = zoom,
            panOffsetX = panOffsetX,
            panOffsetY = panOffsetY,
            stylusPrimaryAction = state.stylusPrimaryAction,
            stylusSecondaryAction = state.stylusSecondaryAction,
            isToolbarCollapsed = state.isToolbarCollapsed,
            onZoomPanChanged = onZoomPanChanged,
            onNextPage = { onAction(ReaderAction.NextPage) },
            onPreviousPage = { onAction(ReaderAction.PreviousPage) },
            onTogglePageNavigator = { onAction(ReaderAction.TogglePageNavigator) },
            onToggleImmersiveMode = { onAction(ReaderAction.ToggleToolbarCollapse) },
            onAnnotationCreated = { ann ->
                pageAnnotations = pageAnnotations + ann
                onAnnotationCreated(ann)
            },
            onOpenStickyNote = { onAction(ReaderAction.OpenStickyNoteDialog(it)) },
            onNewStickyNote = { onAction(ReaderAction.OpenStickyNoteDialog(null, it)) },
            onOpenTextBox = { onAction(ReaderAction.OpenTextBoxDialog(it)) },
            onNewTextBox = { onAction(ReaderAction.OpenTextBoxDialog(null, it)) },
            onOpenStamp = { onAction(ReaderAction.OpenStampDialog(it)) },
            onMoveAnnotation = { id, point -> onAction(ReaderAction.MoveAnnotation(id, point)) },
            onPerformUndo = { onAction(ReaderAction.Undo) },
            onPerformRedo = { onAction(ReaderAction.Redo) },
            onCycleColor = {
                if (state.recentColors.isNotEmpty()) {
                    onAction(ReaderAction.SelectColor(state.recentColors.first()))
                }
            },
            onToggleSelectMode = { onAction(ReaderAction.ToggleSelectTextMode) },
            modifier = Modifier.fillMaxSize()
        )
    }
}
