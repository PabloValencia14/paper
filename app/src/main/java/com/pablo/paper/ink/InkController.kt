package com.pablo.paper.ink

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.pablo.paper.domain.model.Annotation
import com.pablo.paper.domain.model.AnnotationType
import com.pablo.paper.domain.model.ColorPalette
import com.pablo.paper.domain.model.InkPoint
import com.pablo.paper.domain.model.InkStroke
import com.pablo.paper.domain.model.InkTool
import com.pablo.paper.pdf.CoordinateTransformer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class LiveStrokePoint(
    val offset: Offset,
    val pressure: Float
)

class InkController(
    private val transformer: CoordinateTransformer = CoordinateTransformer(),
    private val eraserEngine: EraserEngine = EraserEngine(),
    private val textSnapper: TextSnapper = TextSnapper(),
    val undoRedoManager: UndoRedoManager = UndoRedoManager()
) {
    private val _activeTool = MutableStateFlow(InkTool.PEN)
    val activeTool: StateFlow<InkTool> = _activeTool.asStateFlow()

    private val _currentColor = MutableStateFlow(ColorPalette.BLACK)
    val currentColor: StateFlow<Long> = _currentColor.asStateFlow()

    private val _strokeWidth = MutableStateFlow(2.0f)
    val strokeWidth: StateFlow<Float> = _strokeWidth.asStateFlow()

    private val _opacity = MutableStateFlow(1.0f)
    val opacity: StateFlow<Float> = _opacity.asStateFlow()

    private val _currentLiveStroke = MutableStateFlow<List<LiveStrokePoint>>(emptyList())
    val currentLiveStroke: StateFlow<List<LiveStrokePoint>> = _currentLiveStroke.asStateFlow()

    private val _liveSnappedSegments = MutableStateFlow<List<TextSnapper.SnappedSegment>>(emptyList())
    val liveSnappedSegments: StateFlow<List<TextSnapper.SnappedSegment>> = _liveSnappedSegments.asStateFlow()

    private val _pageAnnotations = MutableStateFlow<List<Annotation>>(emptyList())
    val pageAnnotations: StateFlow<List<Annotation>> = _pageAnnotations.asStateFlow()

    private val _selectedText = MutableStateFlow<String?>(null)
    val selectedText: StateFlow<String?> = _selectedText.asStateFlow()

    private val _selectionNormalizedRect = MutableStateFlow<android.graphics.RectF?>(null)
    val selectionNormalizedRect: StateFlow<android.graphics.RectF?> = _selectionNormalizedRect.asStateFlow()

    private val _lassoSelectedIds = MutableStateFlow<Set<String>>(emptySet())
    val lassoSelectedIds: StateFlow<Set<String>> = _lassoSelectedIds.asStateFlow()

    private val _lassoBounds = MutableStateFlow<androidx.compose.ui.geometry.Rect?>(null)
    val lassoBounds: StateFlow<androidx.compose.ui.geometry.Rect?> = _lassoBounds.asStateFlow()

    private val currentNormalizedPoints = mutableListOf<InkPoint>()
    private var lastStylusEventTimestamp = 0L
    var currentBitmap: Bitmap? = null
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.Job())

    fun setPageAnnotations(annotations: List<Annotation>) {
        _pageAnnotations.value = annotations
        undoRedoManager.clear()
    }

    fun clearSelection() {
        _selectedText.value = null
        _selectionNormalizedRect.value = null
    }

    fun setTool(tool: InkTool) {
        _activeTool.value = tool
        _strokeWidth.value = tool.defaultStrokeWidth
        _opacity.value = tool.defaultAlpha
    }

    fun setColor(color: Long) {
        _currentColor.value = color
    }

    fun setStrokeWidth(width: Float) {
        _strokeWidth.value = width
    }

    fun cancelLiveStroke() {
        _currentLiveStroke.value = emptyList()
        _liveSnappedSegments.value = emptyList()
        currentNormalizedPoints.clear()
    }

    fun onTouchDown(
        screenOffset: Offset,
        pageBounds: Rect,
        pressure: Float,
        isStylus: Boolean
    ) {
        if (_activeTool.value == InkTool.HAND) return
        if (!isStylus) return

        val now = System.currentTimeMillis()
        lastStylusEventTimestamp = now

        val safePressure = pressure.coerceIn(0.1f, 1.5f)
        val pdfPoint = transformer.screenToPdf(screenOffset, pageBounds) ?: return
        val pointWithPressure = pdfPoint.copy(pressure = safePressure)

        if (_activeTool.value == InkTool.ERASER) {
            eraseAtPoint(pointWithPressure)
        } else {
            currentNormalizedPoints.clear()
            currentNormalizedPoints.add(pointWithPressure)
            _currentLiveStroke.value = listOf(LiveStrokePoint(screenOffset, safePressure))

            val tool = _activeTool.value
            if (isTextSnappedTool(tool)) {
                _liveSnappedSegments.value = textSnapper.detectSegments(currentNormalizedPoints, currentBitmap)
            } else {
                _liveSnappedSegments.value = emptyList()
            }
        }
    }

    fun onTouchMove(
        screenOffset: Offset,
        pageBounds: Rect,
        pressure: Float,
        isStylus: Boolean
    ) {
        if (_activeTool.value == InkTool.HAND) return
        if (!isStylus) return

        val now = System.currentTimeMillis()
        lastStylusEventTimestamp = now

        val safePressure = pressure.coerceIn(0.1f, 1.5f)
        val pdfPoint = transformer.screenToPdf(screenOffset, pageBounds) ?: return
        val pointWithPressure = pdfPoint.copy(pressure = safePressure)

        if (_activeTool.value == InkTool.ERASER) {
            eraseAtPoint(pointWithPressure)
        } else {
            val lastPoint = currentNormalizedPoints.lastOrNull()
            if (lastPoint != null) {
                val dx = pointWithPressure.x - lastPoint.x
                val dy = pointWithPressure.y - lastPoint.y
                if (dx * dx + dy * dy < 0.0000003f && kotlin.math.abs(safePressure - lastPoint.pressure) < 0.02f) {
                    return
                }
            }

            currentNormalizedPoints.add(pointWithPressure)
            _currentLiveStroke.update { it + LiveStrokePoint(screenOffset, safePressure) }

            val tool = _activeTool.value
            if (isTextSnappedTool(tool)) {
                _liveSnappedSegments.value = textSnapper.detectSegments(currentNormalizedPoints, currentBitmap)
            }
        }
    }

    fun onTouchUp(
        documentId: String,
        pageIndex: Int,
        onAnnotationCreated: (Annotation) -> Unit
    ) {
        if (_activeTool.value == InkTool.HAND && currentNormalizedPoints.isEmpty()) return

        _currentLiveStroke.value = emptyList()
        _liveSnappedSegments.value = emptyList()

        if (_activeTool.value == InkTool.SELECT_TEXT && currentNormalizedPoints.isNotEmpty()) {
            var minX = 1f
            var maxX = 0f
            var minY = 1f
            var maxY = 0f
            for (p in currentNormalizedPoints) {
                if (p.x < minX) minX = p.x
                if (p.x > maxX) maxX = p.x
                if (p.y < minY) minY = p.y
                if (p.y > maxY) maxY = p.y
            }
            val rect = android.graphics.RectF(minX, minY, maxX, maxY)
            val bmp = currentBitmap
            if (bmp != null && !bmp.isRecycled) {
                scope.launch {
                    val pageData = com.pablo.paper.ocr.PdfTextExtractor.processBitmap(bmp, pageIndex)
                    val text = com.pablo.paper.ocr.PdfTextExtractor.extractTextInRect(pageData, rect)
                    if (text.isNotBlank()) {
                        _selectedText.value = text
                        _selectionNormalizedRect.value = rect
                    }
                }
            }
            currentNormalizedPoints.clear()
            return
        }

        if (_activeTool.value == InkTool.LASER_POINTER) {
            currentNormalizedPoints.clear()
            return
        }

        if (_activeTool.value == InkTool.LASSO && currentNormalizedPoints.size >= 3) {
            val selectedAnnotations = com.pablo.paper.ink.LassoEngine.findSelectedAnnotations(_pageAnnotations.value, currentNormalizedPoints)
            val selectedIds = selectedAnnotations.map { it.id }.toSet()
            val bounds = com.pablo.paper.ink.LassoEngine.calculateSelectionBounds(selectedAnnotations)
            _lassoSelectedIds.value = selectedIds
            _lassoBounds.value = bounds
            currentNormalizedPoints.clear()
            return
        }

        if (_activeTool.value != InkTool.ERASER && currentNormalizedPoints.isNotEmpty()) {
            val tool = _activeTool.value
            val isSnapped = isTextSnappedTool(tool)

            // Automatic Shape Detection on regular Pen strokes
            if (tool == InkTool.PEN && currentNormalizedPoints.size >= 8) {
                val detected = ShapeDetector.detectShape(currentNormalizedPoints)
                if (detected != null && detected.confidence >= 0.88f) {
                    val stroke = InkStroke(
                        points = detected.canonicalPoints,
                        color = _currentColor.value,
                        width = _strokeWidth.value,
                        opacity = _opacity.value,
                        tool = when (detected.annotationType) {
                            AnnotationType.SHAPE_RECTANGLE -> InkTool.RECTANGLE
                            AnnotationType.SHAPE_OVAL -> InkTool.OVAL
                            AnnotationType.SHAPE_ARROW -> InkTool.ARROW
                            AnnotationType.SHAPE_LINE -> InkTool.LINE
                            else -> InkTool.PEN
                        }
                    )
                    val annotation = Annotation(
                        id = UUID.randomUUID().toString(),
                        documentId = documentId,
                        pageIndex = pageIndex,
                        type = detected.annotationType,
                        stroke = stroke
                    )
                    _pageAnnotations.update { it + annotation }
                    undoRedoManager.recordAdd(annotation)
                    onAnnotationCreated(annotation)
                    currentNormalizedPoints.clear()
                    return
                }
            }

            val isShape = tool == InkTool.RECTANGLE ||
                    tool == InkTool.OVAL ||
                    tool == InkTool.ARROW ||
                    tool == InkTool.LINE

            val pointsToSave = when {
                isSnapped -> textSnapper.snapStrokePoints(currentNormalizedPoints, tool, currentBitmap)
                isShape && currentNormalizedPoints.size >= 2 -> listOf(currentNormalizedPoints.first(), currentNormalizedPoints.last())
                tool == InkTool.STICKY_NOTE -> listOf(currentNormalizedPoints.first())
                else -> currentNormalizedPoints.toList()
            }

            val stroke = InkStroke(
                points = pointsToSave,
                color = _currentColor.value,
                width = _strokeWidth.value,
                opacity = _opacity.value,
                tool = tool
            )

            val annotationType = when (tool) {
                InkTool.HIGHLIGHTER, InkTool.TEXT_HIGHLIGHT -> AnnotationType.HIGHLIGHT
                InkTool.UNDERLINE, InkTool.WAVY_UNDERLINE -> AnnotationType.UNDERLINE
                InkTool.STRIKETHROUGH -> AnnotationType.STRIKETHROUGH
                InkTool.RECTANGLE -> AnnotationType.SHAPE_RECTANGLE
                InkTool.OVAL -> AnnotationType.SHAPE_OVAL
                InkTool.ARROW -> AnnotationType.SHAPE_ARROW
                InkTool.LINE -> AnnotationType.SHAPE_LINE
                InkTool.STICKY_NOTE -> AnnotationType.STICKY_NOTE
                InkTool.SIGNATURE -> AnnotationType.SIGNATURE
                else -> AnnotationType.INK
            }

            val annotation = Annotation(
                id = UUID.randomUUID().toString(),
                documentId = documentId,
                pageIndex = pageIndex,
                type = annotationType,
                stroke = stroke
            )

            _pageAnnotations.update { it + annotation }
            undoRedoManager.recordAdd(annotation)
            onAnnotationCreated(annotation)
        }

        currentNormalizedPoints.clear()
    }

    fun addTextBox(
        documentId: String,
        pageIndex: Int,
        point: InkPoint,
        text: String,
        color: Long,
        fontSize: Float,
        onAnnotationCreated: (Annotation) -> Unit
    ) {
        val stroke = InkStroke(
            points = listOf(point),
            color = color,
            width = fontSize,
            opacity = 1.0f,
            tool = InkTool.TEXT_BOX
        )
        val annotation = Annotation(
            id = UUID.randomUUID().toString(),
            documentId = documentId,
            pageIndex = pageIndex,
            type = AnnotationType.TEXT_BOX,
            stroke = stroke,
            textContent = text
        )
        _pageAnnotations.update { it + annotation }
        undoRedoManager.recordAdd(annotation)
        onAnnotationCreated(annotation)
    }

    fun updateTextBox(
        annotationId: String,
        newText: String,
        newColor: Long,
        newFontSize: Float,
        onAnnotationUpdated: (Annotation) -> Unit
    ) {
        val existing = _pageAnnotations.value.find { it.id == annotationId } ?: return
        val updatedStroke = existing.stroke?.copy(color = newColor, width = newFontSize)
        val updated = existing.copy(
            stroke = updatedStroke,
            textContent = newText,
            updatedAt = System.currentTimeMillis()
        )
        _pageAnnotations.update { list -> list.map { if (it.id == annotationId) updated else it } }
        onAnnotationUpdated(updated)
    }

    fun addStamp(
        documentId: String,
        pageIndex: Int,
        point: InkPoint,
        stampText: String,
        color: Long,
        onAnnotationCreated: (Annotation) -> Unit
    ) {
        val stroke = InkStroke(
            points = listOf(point),
            color = color,
            width = 2.5f,
            opacity = 1.0f,
            tool = InkTool.STAMP
        )
        val annotation = Annotation(
            id = UUID.randomUUID().toString(),
            documentId = documentId,
            pageIndex = pageIndex,
            type = AnnotationType.STAMP,
            stroke = stroke,
            textContent = stampText
        )
        _pageAnnotations.update { it + annotation }
        undoRedoManager.recordAdd(annotation)
        onAnnotationCreated(annotation)
    }

    fun clearAllPageAnnotations(): List<Annotation> {
        val current = _pageAnnotations.value
        if (current.isNotEmpty()) {
            undoRedoManager.recordDelete(current)
            _pageAnnotations.value = emptyList()
        }
        return current
    }

    fun addStickyNote(
        documentId: String,
        pageIndex: Int,
        point: InkPoint,
        text: String,
        onAnnotationCreated: (Annotation) -> Unit
    ) {
        val stroke = InkStroke(
            points = listOf(point),
            color = 0xFFFFD54F,
            width = 1.0f,
            opacity = 1.0f,
            tool = InkTool.STICKY_NOTE
        )
        val annotation = Annotation(
            id = UUID.randomUUID().toString(),
            documentId = documentId,
            pageIndex = pageIndex,
            type = AnnotationType.STICKY_NOTE,
            stroke = stroke,
            textContent = text
        )
        _pageAnnotations.update { it + annotation }
        undoRedoManager.recordAdd(annotation)
        onAnnotationCreated(annotation)
    }

    fun updateStickyNoteText(
        annotationId: String,
        newText: String,
        onAnnotationUpdated: (Annotation) -> Unit
    ) {
        val existing = _pageAnnotations.value.find { it.id == annotationId } ?: return
        val updated = existing.copy(textContent = newText, updatedAt = System.currentTimeMillis())
        _pageAnnotations.update { list -> list.map { if (it.id == annotationId) updated else it } }
        onAnnotationUpdated(updated)
    }

    fun updateAnnotationPosition(
        annotationId: String,
        newPoint: InkPoint,
        onAnnotationUpdated: ((Annotation) -> Unit)? = null
    ) {
        val existing = _pageAnnotations.value.find { it.id == annotationId }
        if (existing != null) {
            val updated = existing.copy(
                stroke = existing.stroke?.copy(points = listOf(newPoint)),
                updatedAt = System.currentTimeMillis()
            )
            _pageAnnotations.update { list -> list.map { if (it.id == annotationId) updated else it } }
            onAnnotationUpdated?.invoke(updated)
        }
    }

    fun addSignatureStrokes(
        documentId: String,
        pageIndex: Int,
        targetBounds: android.graphics.RectF,
        normalizedStrokes: List<List<InkPoint>>,
        onAnnotationCreated: (Annotation) -> Unit
    ) {
        for (subStroke in normalizedStrokes) {
            val mappedPoints = subStroke.map { p ->
                InkPoint(
                    x = targetBounds.left + p.x * targetBounds.width(),
                    y = targetBounds.top + p.y * targetBounds.height(),
                    pressure = 1.0f
                )
            }
            val stroke = InkStroke(
                points = mappedPoints,
                color = _currentColor.value,
                width = 2.5f,
                opacity = 1.0f,
                tool = InkTool.SIGNATURE
            )
            val annotation = Annotation(
                id = UUID.randomUUID().toString(),
                documentId = documentId,
                pageIndex = pageIndex,
                type = AnnotationType.SIGNATURE,
                stroke = stroke
            )
            _pageAnnotations.update { it + annotation }
            undoRedoManager.recordAdd(annotation)
            onAnnotationCreated(annotation)
        }
    }

    private fun eraseAtPoint(point: InkPoint) {
        val currentList = _pageAnnotations.value
        val hits = eraserEngine.findIntersectingAnnotations(currentList, point, normalizedToleranceRadius = 0.035f)
        if (hits.isNotEmpty()) {
            val hitIds = hits.map { it.id }.toSet()
            _pageAnnotations.update { list -> list.filterNot { it.id in hitIds } }
            undoRedoManager.recordDelete(hits)
        }
    }

    private fun isTextSnappedTool(tool: InkTool): Boolean {
        return tool == InkTool.TEXT_HIGHLIGHT ||
                tool == InkTool.UNDERLINE ||
                tool == InkTool.STRIKETHROUGH ||
                tool == InkTool.WAVY_UNDERLINE
    }

    fun performUndo(): List<Annotation>? {
        val updated = undoRedoManager.undo(_pageAnnotations.value)
        if (updated != null) {
            _pageAnnotations.value = updated
        }
        return updated
    }

    fun performRedo(): List<Annotation>? {
        val updated = undoRedoManager.redo(_pageAnnotations.value)
        if (updated != null) {
            _pageAnnotations.value = updated
        }
        return updated
    }
}
