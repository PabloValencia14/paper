package com.pablo.paper.ui.ink

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.isTertiaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.pablo.paper.domain.model.Annotation
import com.pablo.paper.domain.model.InkPoint
import com.pablo.paper.domain.model.InkTool
import com.pablo.paper.ink.InkController
import com.pablo.paper.ink.StrokeSmoother
import com.pablo.paper.pdf.CoordinateTransformer
import com.pablo.paper.pdf.PageSize
import kotlin.math.max
import kotlin.math.min

@Composable
fun InkCanvas(
    inkController: InkController,
    pageBounds: Rect,
    pageSize: PageSize = PageSize(612, 792),
    documentId: String,
    pageIndex: Int,
    annotations: List<Annotation> = emptyList(),
    isInkModeEnabled: Boolean,
    isSelectModeEnabled: Boolean = false,
    isContinuousScroll: Boolean = false,
    zoom: Float = 1.0f,
    panOffsetX: Float = 0f,
    panOffsetY: Float = 0f,
    stylusPrimaryAction: com.pablo.paper.domain.model.StylusButtonAction = com.pablo.paper.domain.model.StylusButtonAction.TEMPORARY_ERASER,
    stylusSecondaryAction: com.pablo.paper.domain.model.StylusButtonAction = com.pablo.paper.domain.model.StylusButtonAction.SWITCH_TO_HIGHLIGHTER,
    isToolbarCollapsed: Boolean = false,
    onZoomPanChanged: (Float, Float, Float) -> Unit = { _, _, _ -> },
    onNextPage: () -> Unit = {},
    onPreviousPage: () -> Unit = {},
    onTogglePageNavigator: () -> Unit = {},
    onToggleImmersiveMode: () -> Unit = {},
    onAnnotationCreated: (Annotation) -> Unit,
    onOpenStickyNote: (Annotation) -> Unit = {},
    onNewStickyNote: (com.pablo.paper.domain.model.InkPoint) -> Unit = {},
    onOpenTextBox: (Annotation) -> Unit = {},
    onNewTextBox: (com.pablo.paper.domain.model.InkPoint) -> Unit = {},
    onOpenStamp: (com.pablo.paper.domain.model.InkPoint) -> Unit = {},
    onMoveAnnotation: (String, com.pablo.paper.domain.model.InkPoint) -> Unit = { _, _ -> },
    onPerformUndo: () -> Unit = {},
    onPerformRedo: () -> Unit = {},
    onCycleColor: () -> Unit = {},
    onToggleSelectMode: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val liveStrokePoints by inkController.currentLiveStroke.collectAsState()
    val liveSnappedSegments by inkController.liveSnappedSegments.collectAsState()
    val storedAnnotations by inkController.pageAnnotations.collectAsState()
    val resolvedAnnotations = if (annotations.isNotEmpty()) annotations else storedAnnotations
    val activeTool by inkController.activeTool.collectAsState()
    val currentColor by inkController.currentColor.collectAsState()
    val strokeWidth by inkController.strokeWidth.collectAsState()
    val opacity by inkController.opacity.collectAsState()
    val selectionRect by inkController.selectionNormalizedRect.collectAsState()
    val lassoBounds by inkController.lassoBounds.collectAsState()

    val smoother = remember { StrokeSmoother() }
    val transformer = remember { CoordinateTransformer() }

    val currentBounds by androidx.compose.runtime.rememberUpdatedState(pageBounds)
    val currentZoom by androidx.compose.runtime.rememberUpdatedState(zoom)
    val currentPanX by androidx.compose.runtime.rememberUpdatedState(panOffsetX)
    val currentPanY by androidx.compose.runtime.rememberUpdatedState(panOffsetY)

    var previousToolBeforeHold by remember { androidx.compose.runtime.mutableStateOf<InkTool?>(null) }
    var minimizedNoteIds by remember { androidx.compose.runtime.mutableStateOf(setOf<String>()) }
    var liveMovingAnnotation by remember { androidx.compose.runtime.mutableStateOf<Pair<String, com.pablo.paper.domain.model.InkPoint>?>(null) }

    val inkGestureModifier = if (isInkModeEnabled || isSelectModeEnabled) {
        Modifier.pointerInput(documentId, pageIndex, isInkModeEnabled, isSelectModeEnabled, isToolbarCollapsed, stylusPrimaryAction, stylusSecondaryAction, resolvedAnnotations, minimizedNoteIds) {
            val canvasWidth = size.width.toFloat()
            val canvasHeight = size.height.toFloat()

            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                if (!isToolbarCollapsed && down.position.y < 200f) {
                    return@awaitEachGesture
                }

                val baseWidthPt = if (pageSize.width > 0) pageSize.width.toFloat() else 612f
                val scaleFactor = currentBounds.width / baseWidthPt

                // 1. Universal Annotation Hit-Test (Works for both Finger and Stylus)
                var hitAnnotation: Pair<Annotation, Boolean>? = null // (Annotation, isMinimizeClick)
                for (ann in resolvedAnnotations.asReversed()) {
                    val firstPt = ann.stroke?.points?.firstOrNull() ?: continue
                    val noteScreenPos = transformer.pdfToScreen(firstPt, currentBounds)

                    val isStickyNote = ann.type == com.pablo.paper.domain.model.AnnotationType.STICKY_NOTE || ann.stroke?.tool == InkTool.STICKY_NOTE
                    val isTextBox = ann.type == com.pablo.paper.domain.model.AnnotationType.TEXT_BOX || ann.type == com.pablo.paper.domain.model.AnnotationType.TEXT_NOTE || ann.stroke?.tool == InkTool.TEXT_BOX

                    if (isStickyNote) {
                        val isMin = minimizedNoteIds.contains(ann.id) || ann.textContent.isNullOrBlank()
                        if (isMin) {
                            val hitRadius = 36f * scaleFactor
                            val dx = down.position.x - noteScreenPos.x
                            val dy = down.position.y - noteScreenPos.y
                            if (dx * dx + dy * dy <= hitRadius * hitRadius) {
                                hitAnnotation = Pair(ann, false)
                                break
                            }
                        } else {
                            val text = ann.textContent ?: ""
                            val maxCardWidth = 240f * scaleFactor
                            val longestLineLen = text.lines().maxOfOrNull { it.length } ?: 0
                            val approxTextW = longestLineLen * (8.5f * scaleFactor)
                            val cardW = (approxTextW + 36f * scaleFactor).coerceIn(100f * scaleFactor, maxCardWidth)
                            val cardH = 140f * scaleFactor
                            val cardRect = android.graphics.RectF(
                                noteScreenPos.x,
                                noteScreenPos.y,
                                noteScreenPos.x + cardW,
                                noteScreenPos.y + cardH
                            )
                            if (cardRect.contains(down.position.x, down.position.y)) {
                                val isMinClick = down.position.x >= (cardRect.right - 38f * scaleFactor) &&
                                                 down.position.y <= (cardRect.top + 38f * scaleFactor)
                                hitAnnotation = Pair(ann, isMinClick)
                                break
                            }
                        }
                    } else if (isTextBox) {
                        val boxW = 240f * scaleFactor
                        val boxH = 100f * scaleFactor
                        val boxRect = android.graphics.RectF(
                            noteScreenPos.x,
                            noteScreenPos.y,
                            noteScreenPos.x + boxW,
                            noteScreenPos.y + boxH
                        )
                        if (boxRect.contains(down.position.x, down.position.y)) {
                            hitAnnotation = Pair(ann, false)
                            break
                        }
                    }
                }

                if (hitAnnotation != null) {
                    val (targetAnn, isMinClick) = hitAnnotation
                    down.consume()
                    if (isMinClick) {
                        minimizedNoteIds = if (minimizedNoteIds.contains(targetAnn.id)) {
                            minimizedNoteIds.filter { it != targetAnn.id }.toSet()
                        } else {
                            minimizedNoteIds + targetAnn.id
                        }
                        return@awaitEachGesture
                    }

                    var hasMoved = false
                    val startPos = down.position
                    val noteStartPdf = targetAnn.stroke?.points?.firstOrNull() ?: transformer.screenToPdf(down.position, currentBounds) ?: com.pablo.paper.domain.model.InkPoint(0.5f, 0.5f, 1f)
                    var currentPdfPoint = noteStartPdf

                    while (true) {
                        val event = awaitPointerEvent()
                        val activePointers = event.changes.filter { it.pressed }
                        if (activePointers.isEmpty()) break
                        val currentPos = activePointers.first().position
                        val dist = (currentPos - startPos).getDistance()
                        if (dist > 8f) {
                            hasMoved = true
                            val startPdf = transformer.screenToPdf(startPos, currentBounds)
                            val currPdf = transformer.screenToPdf(currentPos, currentBounds)
                            if (startPdf != null && currPdf != null) {
                                val dx = currPdf.x - startPdf.x
                                val dy = currPdf.y - startPdf.y
                                val updatedPoint = com.pablo.paper.domain.model.InkPoint(
                                    x = (noteStartPdf.x + dx).coerceIn(0f, 0.95f),
                                    y = (noteStartPdf.y + dy).coerceIn(0f, 0.95f),
                                    pressure = 1f
                                )
                                currentPdfPoint = updatedPoint
                                liveMovingAnnotation = Pair(targetAnn.id, updatedPoint)
                            }
                        }
                        event.changes.forEach { it.consume() }
                    }

                    liveMovingAnnotation = null
                    if (hasMoved && currentPdfPoint != null) {
                        onMoveAnnotation(targetAnn.id, currentPdfPoint)
                    } else {
                        if (targetAnn.type == com.pablo.paper.domain.model.AnnotationType.STICKY_NOTE) {
                            if (minimizedNoteIds.contains(targetAnn.id)) {
                                minimizedNoteIds = minimizedNoteIds.filter { it != targetAnn.id }.toSet()
                            } else {
                                onOpenStickyNote(targetAnn)
                            }
                        } else {
                            onOpenTextBox(targetAnn)
                        }
                    }
                    return@awaitEachGesture
                }

                val currentActiveTool = inkController.activeTool.value
                val isHandTool = currentActiveTool == InkTool.HAND
                val isStylusEraser = down.type == PointerType.Eraser
                val hasStylus = down.type == PointerType.Stylus || isStylusEraser || down.type == PointerType.Mouse
                if (hasStylus) {
                    com.pablo.paper.ink.StylusInputDispatcher.notifyStylusActive()
                }
                val isStylusActive = hasStylus || com.pablo.paper.ink.StylusInputDispatcher.isStylusNearOrTouching()
                val isSelectionGesture = isSelectModeEnabled || currentActiveTool == InkTool.SELECT_TEXT
                val isStickyNoteGesture = currentActiveTool == InkTool.STICKY_NOTE
                val isTextBoxGesture = currentActiveTool == InkTool.TEXT_BOX
                val isStampGesture = currentActiveTool == InkTool.STAMP
                
                // ONLY optical stylus (or explicit UI tool placing) can draw when not in Hand tool.
                val isDrawingTouch = !isHandTool && (hasStylus || isSelectionGesture || isStickyNoteGesture || isTextBoxGesture || isStampGesture)

                if (isDrawingTouch) {
                    val pdfPoint = transformer.screenToPdf(down.position, currentBounds)
                    if (pdfPoint != null) {
                        if (isStickyNoteGesture) {
                            onNewStickyNote(pdfPoint)
                            down.consume()
                            return@awaitEachGesture
                        }

                        if (isTextBoxGesture) {
                            onNewTextBox(pdfPoint)
                            down.consume()
                            return@awaitEachGesture
                        }

                        if (isStampGesture) {
                            onOpenStamp(pdfPoint)
                            down.consume()
                            return@awaitEachGesture
                        }
                    }

                    // Handle Stylus Button Action trigger (e.g. Eraser button on stylus)
                    if (isStylusEraser && currentActiveTool != InkTool.ERASER) {
                        previousToolBeforeHold = currentActiveTool
                        inkController.setTool(InkTool.ERASER)
                    }

                    // --- DRAWING / TEXT SELECTION MODE ---
                    if (isSelectModeEnabled) {
                        inkController.setTool(InkTool.SELECT_TEXT)
                    }
                    inkController.onTouchDown(
                        screenOffset = down.position,
                        pageBounds = currentBounds,
                        pressure = down.pressure,
                        isStylus = hasStylus
                    )
                    down.consume()

                    while (true) {
                        val event = awaitPointerEvent()
                        val activePointers = event.changes.filter { it.pressed }

                        // Check stylus barrel buttons during touch
                        val isSecondaryPressed = event.buttons.isSecondaryPressed || 
                            event.changes.any { (it.type == PointerType.Stylus || it.type == PointerType.Eraser) && event.buttons.isSecondaryPressed }
                        val isTertiaryPressed = event.buttons.isTertiaryPressed

                        if (isSecondaryPressed) {
                            when (stylusPrimaryAction) {
                                com.pablo.paper.domain.model.StylusButtonAction.TEMPORARY_ERASER -> {
                                    if (inkController.activeTool.value != InkTool.ERASER) {
                                        previousToolBeforeHold = inkController.activeTool.value
                                        inkController.setTool(InkTool.ERASER)
                                    }
                                }
                                com.pablo.paper.domain.model.StylusButtonAction.SWITCH_TO_HIGHLIGHTER -> {
                                    inkController.setTool(InkTool.HIGHLIGHTER)
                                }
                                com.pablo.paper.domain.model.StylusButtonAction.TOGGLE_HAND_TOOL -> {
                                    inkController.setTool(InkTool.HAND)
                                }
                                com.pablo.paper.domain.model.StylusButtonAction.TOGGLE_ERASER -> {
                                    if (inkController.activeTool.value == InkTool.ERASER) {
                                        val restore = previousToolBeforeHold ?: InkTool.PEN
                                        inkController.setTool(restore)
                                    } else {
                                        previousToolBeforeHold = inkController.activeTool.value
                                        inkController.setTool(InkTool.ERASER)
                                    }
                                }
                                com.pablo.paper.domain.model.StylusButtonAction.TOGGLE_LAST_TOOL -> {
                                    val current = inkController.activeTool.value
                                    val target = if (current == InkTool.PEN) InkTool.HIGHLIGHTER else InkTool.PEN
                                    inkController.setTool(target)
                                }
                                com.pablo.paper.domain.model.StylusButtonAction.COLOR_CYCLE -> onCycleColor()
                                com.pablo.paper.domain.model.StylusButtonAction.UNDO -> onPerformUndo()
                                com.pablo.paper.domain.model.StylusButtonAction.REDO -> onPerformRedo()
                                com.pablo.paper.domain.model.StylusButtonAction.NEXT_PAGE -> onNextPage()
                                com.pablo.paper.domain.model.StylusButtonAction.PREVIOUS_PAGE -> onPreviousPage()
                                com.pablo.paper.domain.model.StylusButtonAction.SELECT_TEXT -> onToggleSelectMode()
                                else -> {}
                            }
                        }

                        if (isTertiaryPressed) {
                            when (stylusSecondaryAction) {
                                com.pablo.paper.domain.model.StylusButtonAction.TEMPORARY_ERASER -> {
                                    if (inkController.activeTool.value != InkTool.ERASER) {
                                        previousToolBeforeHold = inkController.activeTool.value
                                        inkController.setTool(InkTool.ERASER)
                                    }
                                }
                                com.pablo.paper.domain.model.StylusButtonAction.SWITCH_TO_HIGHLIGHTER -> {
                                    inkController.setTool(InkTool.HIGHLIGHTER)
                                }
                                com.pablo.paper.domain.model.StylusButtonAction.TOGGLE_HAND_TOOL -> {
                                    inkController.setTool(InkTool.HAND)
                                }
                                com.pablo.paper.domain.model.StylusButtonAction.TOGGLE_ERASER -> {
                                    if (inkController.activeTool.value == InkTool.ERASER) {
                                        val restore = previousToolBeforeHold ?: InkTool.PEN
                                        inkController.setTool(restore)
                                    } else {
                                        previousToolBeforeHold = inkController.activeTool.value
                                        inkController.setTool(InkTool.ERASER)
                                    }
                                }
                                com.pablo.paper.domain.model.StylusButtonAction.TOGGLE_LAST_TOOL -> {
                                    val current = inkController.activeTool.value
                                    val target = if (current == InkTool.PEN) InkTool.HIGHLIGHTER else InkTool.PEN
                                    inkController.setTool(target)
                                }
                                com.pablo.paper.domain.model.StylusButtonAction.COLOR_CYCLE -> onCycleColor()
                                com.pablo.paper.domain.model.StylusButtonAction.UNDO -> onPerformUndo()
                                com.pablo.paper.domain.model.StylusButtonAction.REDO -> onPerformRedo()
                                com.pablo.paper.domain.model.StylusButtonAction.NEXT_PAGE -> onNextPage()
                                com.pablo.paper.domain.model.StylusButtonAction.PREVIOUS_PAGE -> onPreviousPage()
                                com.pablo.paper.domain.model.StylusButtonAction.SELECT_TEXT -> onToggleSelectMode()
                                else -> {}
                            }
                        }

                        if (activePointers.isEmpty()) {
                            inkController.onTouchUp(
                                documentId = documentId,
                                pageIndex = pageIndex,
                                onAnnotationCreated = onAnnotationCreated
                            )
                            // Restore previous tool if we were in temporary hold eraser mode
                            if (previousToolBeforeHold != null) {
                                inkController.setTool(previousToolBeforeHold!!)
                                previousToolBeforeHold = null
                            }
                            event.changes.forEach { it.consume() }
                            break
                        }

                        val change = if (hasStylus) {
                            activePointers.find { it.type == PointerType.Stylus || it.type == PointerType.Eraser } ?: activePointers.first()
                        } else {
                            if (activePointers.size >= 2) {
                                inkController.cancelLiveStroke()
                                event.changes.forEach { it.consume() }
                                break
                            }
                            activePointers.first()
                        }

                        // Consume and discard palm touch events while stylus is drawing
                        if (hasStylus) {
                            event.changes.filter { it.type == PointerType.Touch }.forEach { it.consume() }
                        }

                        inkController.onTouchMove(
                            screenOffset = change.position,
                            pageBounds = currentBounds,
                            pressure = change.pressure,
                            isStylus = hasStylus
                        )
                        event.changes.forEach { it.consume() }
                    }
                } else {
                    // --- FINGER TOUCH / NAVIGATION / PAN / ZOOM ---

                    // 1. Proximity Palm Rejection: If the stylus is near or touching, discard finger touches
                    // so the resting palm does not trigger unwanted movements while writing.
                    if (isStylusActive) {
                        down.consume()
                        while (true) {
                            val event = awaitPointerEvent()
                            val activePointers = event.changes.filter { it.pressed }
                            if (activePointers.isEmpty()) break

                            // If user deliberately uses 2 distinct fingers with the other hand, allow pinch zoom/pan
                            if (activePointers.size >= 2) {
                                val centroid = event.calculateCentroid(useCurrent = true)
                                val zoomChange = event.calculateZoom()
                                val panChange = event.calculatePan()
                                if (zoomChange != 1.0f || panChange != Offset.Zero) {
                                    val newZoom = (currentZoom * zoomChange).coerceIn(1.0f, 5.0f)
                                    val k = newZoom / currentZoom
                                    val pageCenterX = (currentBounds.left + currentBounds.right) / 2f
                                    val pageCenterY = (currentBounds.top + currentBounds.bottom) / 2f
                                    val newPanX = if (newZoom <= 1.05f) 0f else (currentPanX + panChange.x + (k - 1f) * (pageCenterX - centroid.x))
                                    val newPanY = if (newZoom <= 1.05f) 0f else (currentPanY + panChange.y + (k - 1f) * (pageCenterY - centroid.y))
                                    onZoomPanChanged(newZoom, newPanX, newPanY)
                                }
                            }
                            event.changes.forEach { it.consume() }
                        }
                        return@awaitEachGesture
                    }

                    // 2. Stylus is SEPARATED / AWAY: Palm rejection is INACTIVE.
                    // In Continuous Scroll Mode, do not consume single finger touches so LazyColumn scrolls natively!
                    if (isContinuousScroll) {
                        while (true) {
                            val event = awaitPointerEvent()
                            val activePointers = event.changes.filter { it.pressed }
                            if (activePointers.isEmpty()) break
                            if (activePointers.size >= 2) {
                                // 2-finger pinch zoom in continuous scroll
                                val centroid = event.calculateCentroid(useCurrent = true)
                                val zoomChange = event.calculateZoom()
                                val panChange = event.calculatePan()
                                if (zoomChange != 1.0f || panChange != Offset.Zero) {
                                    val newZoom = (currentZoom * zoomChange).coerceIn(1.0f, 5.0f)
                                    val k = newZoom / currentZoom
                                    val pageCenterX = (currentBounds.left + currentBounds.right) / 2f
                                    val pageCenterY = (currentBounds.top + currentBounds.bottom) / 2f
                                    val newPanX = if (newZoom <= 1.05f) 0f else (currentPanX + panChange.x + (k - 1f) * (pageCenterX - centroid.x))
                                    val newPanY = if (newZoom <= 1.05f) 0f else (currentPanY + panChange.y + (k - 1f) * (pageCenterY - centroid.y))
                                    onZoomPanChanged(newZoom, newPanX, newPanY)
                                }
                                event.changes.forEach { it.consume() }
                            }
                        }
                        return@awaitEachGesture
                    }

                    // 3. Single-Page / Two-Page Mode with Stylus Away:
                    // Single finger pans when zoomed, swipes to change page when not zoomed, or quick-taps to navigate/toggle UI.
                    down.consume()
                    var currentGestureZoom = currentZoom
                    var currentGesturePanX = currentPanX
                    var currentGesturePanY = currentPanY
                    val startDownPos = down.position
                    val downTimestamp = System.currentTimeMillis()
                    var totalPanX = 0f
                    var totalPanY = 0f
                    var isMultiTouch = false
                    var previousPointerCount = 1

                    while (true) {
                        val event = awaitPointerEvent()
                        val activePointers = event.changes.filter { it.pressed }

                        // Check stylus buttons during navigation touch
                        val isSecondaryPressed = event.buttons.isSecondaryPressed || 
                            event.changes.any { (it.type == PointerType.Stylus || it.type == PointerType.Eraser) && event.buttons.isSecondaryPressed }
                        val isTertiaryPressed = event.buttons.isTertiaryPressed

                        if (isSecondaryPressed) {
                            when (stylusPrimaryAction) {
                                com.pablo.paper.domain.model.StylusButtonAction.TOGGLE_HAND_TOOL -> inkController.setTool(InkTool.PEN)
                                com.pablo.paper.domain.model.StylusButtonAction.SWITCH_TO_HIGHLIGHTER -> inkController.setTool(InkTool.HIGHLIGHTER)
                                com.pablo.paper.domain.model.StylusButtonAction.NEXT_PAGE -> onNextPage()
                                com.pablo.paper.domain.model.StylusButtonAction.PREVIOUS_PAGE -> onPreviousPage()
                                else -> {}
                            }
                        }
                        if (isTertiaryPressed) {
                            when (stylusSecondaryAction) {
                                com.pablo.paper.domain.model.StylusButtonAction.TOGGLE_HAND_TOOL -> inkController.setTool(InkTool.PEN)
                                com.pablo.paper.domain.model.StylusButtonAction.SWITCH_TO_HIGHLIGHTER -> inkController.setTool(InkTool.HIGHLIGHTER)
                                com.pablo.paper.domain.model.StylusButtonAction.NEXT_PAGE -> onNextPage()
                                com.pablo.paper.domain.model.StylusButtonAction.PREVIOUS_PAGE -> onPreviousPage()
                                else -> {}
                            }
                        }

                        if (activePointers.isEmpty()) {
                            break
                        }

                        val pointerCountChanged = (activePointers.size != previousPointerCount)
                        previousPointerCount = activePointers.size

                        if (!pointerCountChanged) {
                            if (activePointers.size >= 2) {
                                isMultiTouch = true
                                val centroid = event.calculateCentroid(useCurrent = true)
                                val zoomChange = event.calculateZoom()
                                val panChange = event.calculatePan()

                                if (zoomChange != 1.0f || panChange != Offset.Zero) {
                                    val newZoom = (currentGestureZoom * zoomChange).coerceIn(1.0f, 5.0f)
                                    val k = newZoom / currentGestureZoom

                                    // Center of page on screen before this micro-step
                                    val pageCenterX = (currentBounds.left + currentBounds.right) / 2f
                                    val pageCenterY = (currentBounds.top + currentBounds.bottom) / 2f

                                    currentGestureZoom = newZoom
                                    currentGesturePanX += panChange.x + (k - 1f) * (pageCenterX - centroid.x)
                                    currentGesturePanY += panChange.y + (k - 1f) * (pageCenterY - centroid.y)

                                    if (currentGestureZoom <= 1.05f) {
                                        currentGesturePanX = 0f
                                        currentGesturePanY = 0f
                                    }

                                    onZoomPanChanged(currentGestureZoom, currentGesturePanX, currentGesturePanY)
                                }
                            } else {
                                // 1 pointer pan / scroll
                                val panChange = event.calculatePan()
                                totalPanX += panChange.x
                                totalPanY += panChange.y

                                if (currentGestureZoom > 1.05f && panChange != Offset.Zero) {
                                    currentGesturePanX += panChange.x
                                    currentGesturePanY += panChange.y
                                    onZoomPanChanged(currentGestureZoom, currentGesturePanX, currentGesturePanY)
                                }
                            }
                        }

                        event.changes.forEach { it.consume() }
                    }

                    // On release: check for Left/Right Edge Taps or Swipes
                    val gestureDuration = System.currentTimeMillis() - downTimestamp
                    val totalPanDist = kotlin.math.sqrt(totalPanX * totalPanX + totalPanY * totalPanY)
                    val isQuickTap = !isMultiTouch && totalPanDist < 25f && gestureDuration < 500L

                    if (isQuickTap) {
                        // Check if tap was outside top toolbar area (toolbar is ~60dp + status bar)
                        if (startDownPos.y > 220f) {
                            val tapX = startDownPos.x
                            val width = if (canvasWidth > 0f) canvasWidth else (currentBounds.width + currentBounds.left * 2)

                            // 1. Left 22% of screen -> Previous Page
                            if (tapX < width * 0.22f) {
                                onPreviousPage()
                            }
                            // 2. Right 22% of screen -> Next Page
                            else if (tapX > width * 0.78f) {
                                onNextPage()
                            }
                            // 3. Center of the Document / Screen -> Toggle Full-Screen Immersive UI
                            else {
                                onToggleImmersiveMode()
                            }
                        }
                    } else if (!isMultiTouch && currentGestureZoom <= 1.25f) {
                        val swipeThreshold = 55f
                        if (totalPanX < -swipeThreshold && kotlin.math.abs(totalPanX) > kotlin.math.abs(totalPanY) * 1.1f) {
                            // Swiped Left -> Next Page
                            onNextPage()
                        } else if (totalPanX > swipeThreshold && kotlin.math.abs(totalPanX) > kotlin.math.abs(totalPanY) * 1.1f) {
                            // Swiped Right -> Previous Page
                            onPreviousPage()
                        }
                    }
                }
            }
        }
    } else {
        Modifier
    }

    val hoverModifier = Modifier.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(androidx.compose.ui.input.pointer.PointerEventPass.Initial)
                if (event.changes.any { it.type == PointerType.Stylus || it.type == PointerType.Eraser }) {
                    com.pablo.paper.ink.StylusInputDispatcher.notifyStylusActive()
                }
            }
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .then(hoverModifier)
            .then(inkGestureModifier)
    ) {
        if (pageBounds.width <= 0f || pageBounds.height <= 0f) return@Canvas

        val baseWidthPt = if (pageSize.width > 0) pageSize.width.toFloat() else 612f
        val scaleFactor = pageBounds.width / baseWidthPt

        // -------------------------------------------------------------
        // LAYER 1: HIGHLIGHTERS (Uniform composited layer - no dark overlapping buildup!)
        // -------------------------------------------------------------
        val highlightAnnotations = resolvedAnnotations.filter {
            it.stroke?.tool == InkTool.HIGHLIGHTER || it.stroke?.tool == InkTool.TEXT_HIGHLIGHT
        }
        val isLiveHighlighting = liveStrokePoints.isNotEmpty() &&
                (activeTool == InkTool.HIGHLIGHTER || activeTool == InkTool.TEXT_HIGHLIGHT)

        if (highlightAnnotations.isNotEmpty() || isLiveHighlighting) {
            drawIntoCanvas { canvas ->
                val layerPaint = android.graphics.Paint().apply {
                    alpha = ((opacity.coerceIn(0.15f, 0.35f)) * 255).toInt().coerceIn(0, 255)
                }
                canvas.nativeCanvas.saveLayer(
                    pageBounds.left, pageBounds.top, pageBounds.right, pageBounds.bottom,
                    layerPaint
                )

                // 1.1 Draw Persisted Highlighters (solid inside the layer so overlaps do not darken)
                for (annotation in highlightAnnotations) {
                    val stroke = annotation.stroke ?: continue
                    val solidColor = Color(stroke.color).copy(alpha = 1.0f)
                    val effectiveWidth = stroke.width * scaleFactor

                    when (stroke.tool) {
                        InkTool.TEXT_HIGHLIGHT -> {
                            for (i in 0 until stroke.points.size - 1 step 2) {
                                val p1 = transformer.pdfToScreen(stroke.points[i], pageBounds)
                                val p2 = transformer.pdfToScreen(stroke.points[i + 1], pageBounds)
                                val left = min(p1.x, p2.x)
                                val right = max(p1.x, p2.x)
                                val top = min(p1.y, p2.y)
                                val bottom = max(p1.y, p2.y)

                                drawRoundRect(
                                    color = solidColor,
                                    topLeft = Offset(left, top),
                                    size = Size((right - left).coerceAtLeast(4f), (bottom - top).coerceAtLeast(8f)),
                                    cornerRadius = CornerRadius(2.5f * scaleFactor, 2.5f * scaleFactor)
                                )
                            }
                        }
                        InkTool.HIGHLIGHTER -> {
                            val points = stroke.points.map { transformer.pdfToScreen(it, pageBounds) }
                            val pressures = stroke.points.map { it.pressure }
                            smoother.drawPressureStroke(
                                drawScope = this,
                                points = points,
                                pressures = pressures,
                                baseWidth = effectiveWidth,
                                color = solidColor,
                                isHighlighter = true
                            )
                        }
                        else -> {}
                    }
                }

                // 1.2 Draw Live Highlighting inside the same layer
                if (isLiveHighlighting) {
                    val solidLiveColor = Color(currentColor).copy(alpha = 1.0f)
                    val effectiveWidth = strokeWidth * scaleFactor

                    if (activeTool == InkTool.TEXT_HIGHLIGHT && liveSnappedSegments.isNotEmpty()) {
                        for (seg in liveSnappedSegments) {
                            val startScreenX = pageBounds.left + seg.startX * pageBounds.width
                            val endScreenX = pageBounds.left + seg.endX * pageBounds.width
                            val leftScreenX = min(startScreenX, endScreenX)
                            val rightScreenX = max(startScreenX, endScreenX)
                            val topScreenY = pageBounds.top + seg.topY * pageBounds.height
                            val heightPx = (seg.lineHeight * pageBounds.height).coerceIn(10f * scaleFactor, 20f * scaleFactor)

                            drawRoundRect(
                                color = solidLiveColor,
                                topLeft = Offset(leftScreenX, topScreenY),
                                size = Size((rightScreenX - leftScreenX).coerceAtLeast(4f), heightPx),
                                cornerRadius = CornerRadius(2.5f * scaleFactor, 2.5f * scaleFactor)
                            )
                        }
                    } else if (activeTool == InkTool.HIGHLIGHTER) {
                        val livePoints = liveStrokePoints.map { it.offset }
                        val livePressures = liveStrokePoints.map { it.pressure }
                        smoother.drawPressureStroke(
                            drawScope = this,
                            points = livePoints,
                            pressures = livePressures,
                            baseWidth = effectiveWidth,
                            color = solidLiveColor,
                            isHighlighter = true
                        )
                    }
                }

                canvas.nativeCanvas.restore()
            }
        }

        // -------------------------------------------------------------
        // LAYER 2: PEN, UNDERLINE, STRIKETHROUGH, WAVY (Crisp & Sharp on top)
        // -------------------------------------------------------------
        val nonHighlightAnnotations = resolvedAnnotations.filter {
            it.stroke?.tool != InkTool.HIGHLIGHTER && it.stroke?.tool != InkTool.TEXT_HIGHLIGHT
        }
        for (annotation in nonHighlightAnnotations) {
            val stroke = annotation.stroke ?: continue
            val strokeColor = Color(stroke.color).copy(alpha = stroke.opacity)
            val effectiveWidth = stroke.width * scaleFactor

            when (stroke.tool) {
                InkTool.UNDERLINE -> {
                    for (i in 0 until stroke.points.size - 1 step 2) {
                        val p1 = transformer.pdfToScreen(stroke.points[i], pageBounds)
                        val p2 = transformer.pdfToScreen(stroke.points[i + 1], pageBounds)
                        drawLine(
                            color = strokeColor,
                            start = p1,
                            end = p2,
                            strokeWidth = effectiveWidth,
                            cap = StrokeCap.Round
                        )
                    }
                }
                InkTool.STRIKETHROUGH -> {
                    for (i in 0 until stroke.points.size - 1 step 2) {
                        val p1 = transformer.pdfToScreen(stroke.points[i], pageBounds)
                        val p2 = transformer.pdfToScreen(stroke.points[i + 1], pageBounds)
                        drawLine(
                            color = strokeColor,
                            start = p1,
                            end = p2,
                            strokeWidth = effectiveWidth,
                            cap = StrokeCap.Round
                        )
                    }
                }
                InkTool.WAVY_UNDERLINE -> {
                    for (i in 0 until stroke.points.size - 1 step 2) {
                        val p1 = transformer.pdfToScreen(stroke.points[i], pageBounds)
                        val p2 = transformer.pdfToScreen(stroke.points[i + 1], pageBounds)
                        val wavyPath = smoother.createWavyPath(
                            start = p1,
                            end = p2,
                            waveLength = 8f * scaleFactor,
                            amplitude = 1.8f * scaleFactor
                        )
                        drawPath(
                            path = wavyPath,
                            color = strokeColor,
                            style = Stroke(
                                width = effectiveWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
                InkTool.RECTANGLE -> {
                    if (stroke.points.size >= 2) {
                        val p1 = transformer.pdfToScreen(stroke.points.first(), pageBounds)
                        val p2 = transformer.pdfToScreen(stroke.points.last(), pageBounds)
                        val left = minOf(p1.x, p2.x)
                        val right = maxOf(p1.x, p2.x)
                        val top = minOf(p1.y, p2.y)
                        val bottom = maxOf(p1.y, p2.y)
                        drawRoundRect(
                            color = strokeColor,
                            topLeft = Offset(left, top),
                            size = Size((right - left).coerceAtLeast(1f), (bottom - top).coerceAtLeast(1f)),
                            cornerRadius = CornerRadius(4f * scaleFactor, 4f * scaleFactor),
                            style = Stroke(width = effectiveWidth)
                        )
                    }
                }
                InkTool.OVAL -> {
                    if (stroke.points.size >= 2) {
                        val p1 = transformer.pdfToScreen(stroke.points.first(), pageBounds)
                        val p2 = transformer.pdfToScreen(stroke.points.last(), pageBounds)
                        val left = minOf(p1.x, p2.x)
                        val right = maxOf(p1.x, p2.x)
                        val top = minOf(p1.y, p2.y)
                        val bottom = maxOf(p1.y, p2.y)
                        drawOval(
                            color = strokeColor,
                            topLeft = Offset(left, top),
                            size = Size((right - left).coerceAtLeast(1f), (bottom - top).coerceAtLeast(1f)),
                            style = Stroke(width = effectiveWidth)
                        )
                    }
                }
                InkTool.ARROW -> {
                    if (stroke.points.size >= 2) {
                        val p1 = transformer.pdfToScreen(stroke.points.first(), pageBounds)
                        val p2 = transformer.pdfToScreen(stroke.points.last(), pageBounds)
                        drawLine(
                            color = strokeColor,
                            start = p1,
                            end = p2,
                            strokeWidth = effectiveWidth,
                            cap = StrokeCap.Round
                        )
                        val angle = kotlin.math.atan2((p2.y - p1.y).toDouble(), (p2.x - p1.x).toDouble())
                        val headLen = 14f * scaleFactor
                        val x1 = (p2.x - headLen * kotlin.math.cos(angle - Math.PI / 6)).toFloat()
                        val y1 = (p2.y - headLen * kotlin.math.sin(angle - Math.PI / 6)).toFloat()
                        val x2 = (p2.x - headLen * kotlin.math.cos(angle + Math.PI / 6)).toFloat()
                        val y2 = (p2.y - headLen * kotlin.math.sin(angle + Math.PI / 6)).toFloat()
                        val headPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(p2.x, p2.y)
                            lineTo(x1, y1)
                            lineTo(x2, y2)
                            close()
                        }
                        drawPath(headPath, color = strokeColor)
                    }
                }
                InkTool.LINE -> {
                    if (stroke.points.size >= 2) {
                        val p1 = transformer.pdfToScreen(stroke.points.first(), pageBounds)
                        val p2 = transformer.pdfToScreen(stroke.points.last(), pageBounds)
                        drawLine(
                            color = strokeColor,
                            start = p1,
                            end = p2,
                            strokeWidth = effectiveWidth,
                            cap = StrokeCap.Round
                        )
                    }
                }
                InkTool.STICKY_NOTE -> {
                    if (stroke.points.isNotEmpty()) {
                        val isNoteMin = minimizedNoteIds.contains(annotation.id) || (annotation.textContent ?: "").isBlank()
                        val livePos = if (liveMovingAnnotation?.first == annotation.id) {
                            liveMovingAnnotation?.second?.let { transformer.pdfToScreen(it, pageBounds) }
                        } else null
                        val center = livePos ?: transformer.pdfToScreen(stroke.points.first(), pageBounds)
                        val text = annotation.textContent ?: ""

                        drawIntoCanvas { canvas ->
                            val native = canvas.nativeCanvas

                            if (!isNoteMin && text.isNotBlank()) {
                                // Draw High-Contrast Sticky Note Card with Content Preview
                                val textPaint = android.graphics.Paint().apply {
                                    color = 0xFF1E293B.toInt() // Dark slate for sharp readable contrast
                                    textSize = (13.5f * scaleFactor).coerceIn(12f, 32f)
                                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.NORMAL)
                                    isAntiAlias = true
                                }
                                val headerPaint = android.graphics.Paint().apply {
                                    color = 0xFFB45309.toInt() // Amber 700
                                    textSize = (11f * scaleFactor).coerceIn(10f, 24f)
                                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                                    isAntiAlias = true
                                }
                                val cardBgPaint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.argb(252, 254, 243, 199) // Solid Warm Amber Note
                                    style = android.graphics.Paint.Style.FILL
                                    isAntiAlias = true
                                }
                                val cardBorderPaint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.argb(220, 245, 158, 11) // Amber border
                                    this.strokeWidth = 1.5f * scaleFactor
                                    style = android.graphics.Paint.Style.STROKE
                                    isAntiAlias = true
                                }

                                // Break text into wrapped lines
                                val maxCardWidth = 240f * scaleFactor
                                val words = text.split(" ")
                                val lines = mutableListOf<String>()
                                var currentLine = StringBuilder()

                                for (word in words) {
                                    val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                                    if (textPaint.measureText(testLine) <= maxCardWidth - 24f * scaleFactor) {
                                        currentLine = StringBuilder(testLine)
                                    } else {
                                        if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
                                        currentLine = StringBuilder(word)
                                    }
                                }
                                if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
                                val displayLines = lines.take(7)

                                var measuredMaxW = 0f
                                for (l in displayLines) {
                                    val w = textPaint.measureText(l)
                                    if (w > measuredMaxW) measuredMaxW = w
                                }
                                val cardWidth = (measuredMaxW + 36f * scaleFactor).coerceIn(100f * scaleFactor, maxCardWidth)
                                val lineHeight = textPaint.fontSpacing
                                val headerHeight = 22f * scaleFactor
                                val cardHeight = headerHeight + (lineHeight * displayLines.size) + 12f * scaleFactor

                                val cardRect = android.graphics.RectF(
                                    center.x,
                                    center.y,
                                    center.x + cardWidth,
                                    center.y + cardHeight
                                )

                                // Soft ambient shadow
                                val shadowPaint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.argb(60, 0, 0, 0)
                                    style = android.graphics.Paint.Style.FILL
                                    isAntiAlias = true
                                }
                                val shadowRect = android.graphics.RectF(
                                    cardRect.left + 2.5f * scaleFactor,
                                    cardRect.top + 3.5f * scaleFactor,
                                    cardRect.right + 2.5f * scaleFactor,
                                    cardRect.bottom + 3.5f * scaleFactor
                                )
                                native.drawRoundRect(shadowRect, 10f * scaleFactor, 10f * scaleFactor, shadowPaint)

                                // Solid note card body
                                native.drawRoundRect(cardRect, 10f * scaleFactor, 10f * scaleFactor, cardBgPaint)
                                native.drawRoundRect(cardRect, 10f * scaleFactor, 10f * scaleFactor, cardBorderPaint)

                                // Header Pin Icon & Label
                                val pinPaint = android.graphics.Paint().apply {
                                    color = 0xFFD97706.toInt()
                                    style = android.graphics.Paint.Style.FILL
                                    isAntiAlias = true
                                }
                                native.drawCircle(cardRect.left + 12f * scaleFactor, cardRect.top + 12f * scaleFactor, 3.5f * scaleFactor, pinPaint)
                                native.drawText("Nota", cardRect.left + 20f * scaleFactor, cardRect.top + 15f * scaleFactor, headerPaint)

                                // Header Minimize Icon '-'
                                val minIconPaint = android.graphics.Paint().apply {
                                    color = 0xFF92400E.toInt()
                                    this.strokeWidth = 2.2f * scaleFactor
                                    style = android.graphics.Paint.Style.STROKE
                                    isAntiAlias = true
                                }
                                val minX = cardRect.right - 14f * scaleFactor
                                val minY = cardRect.top + 12f * scaleFactor
                                native.drawLine(minX - 5f * scaleFactor, minY, minX + 5f * scaleFactor, minY, minIconPaint)

                                // Note text lines
                                var textY = cardRect.top + headerHeight + 8f * scaleFactor
                                for (l in displayLines) {
                                    native.drawText(l, cardRect.left + 12f * scaleFactor, textY, textPaint)
                                    textY += lineHeight
                                }
                            } else {
                                // Compact Amber Glass Pin Badge for Minimized / Empty Note
                                val badgeSize = 30f * scaleFactor
                                val rect = android.graphics.RectF(
                                    center.x - badgeSize / 2f, center.y - badgeSize / 2f,
                                    center.x + badgeSize / 2f, center.y + badgeSize / 2f
                                )
                                val bgPaint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.argb(250, 255, 213, 79)
                                    style = android.graphics.Paint.Style.FILL
                                    isAntiAlias = true
                                }
                                val borderPaint = android.graphics.Paint().apply {
                                    color = 0xFF5D4037.toInt()
                                    this.strokeWidth = 1.5f * scaleFactor
                                    style = android.graphics.Paint.Style.STROKE
                                    isAntiAlias = true
                                }
                                val shadowPaint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.argb(50, 0, 0, 0)
                                    style = android.graphics.Paint.Style.FILL
                                    isAntiAlias = true
                                }
                                val shadowRect = android.graphics.RectF(
                                    rect.left + 2f * scaleFactor, rect.top + 2f * scaleFactor,
                                    rect.right + 2f * scaleFactor, rect.bottom + 2f * scaleFactor
                                )
                                native.drawRoundRect(shadowRect, 8f * scaleFactor, 8f * scaleFactor, shadowPaint)
                                native.drawRoundRect(rect, 8f * scaleFactor, 8f * scaleFactor, bgPaint)
                                native.drawRoundRect(rect, 8f * scaleFactor, 8f * scaleFactor, borderPaint)

                                // Mini note lines inside badge
                                val linePaint = android.graphics.Paint().apply {
                                    color = 0xFF5D4037.toInt()
                                    this.strokeWidth = 1.5f * scaleFactor
                                    style = android.graphics.Paint.Style.STROKE
                                    isAntiAlias = true
                                }
                                native.drawLine(rect.left + 7f * scaleFactor, rect.top + 9f * scaleFactor, rect.right - 7f * scaleFactor, rect.top + 9f * scaleFactor, linePaint)
                                native.drawLine(rect.left + 7f * scaleFactor, rect.top + 15f * scaleFactor, rect.right - 7f * scaleFactor, rect.top + 15f * scaleFactor, linePaint)
                                native.drawLine(rect.left + 7f * scaleFactor, rect.top + 21f * scaleFactor, rect.left + 15f * scaleFactor, rect.top + 21f * scaleFactor, linePaint)
                            }
                        }
                    }
                }
                InkTool.TEXT_BOX -> {
                    if (stroke.points.isNotEmpty()) {
                        val livePos = if (liveMovingAnnotation?.first == annotation.id) {
                            liveMovingAnnotation?.second?.let { transformer.pdfToScreen(it, pageBounds) }
                        } else null
                        val center = livePos ?: transformer.pdfToScreen(stroke.points.first(), pageBounds)
                        val text = annotation.textContent ?: ""
                        if (text.isNotEmpty()) {
                            drawIntoCanvas { canvas ->
                                val paint = android.graphics.Paint().apply {
                                    color = stroke.color.toInt()
                                    textSize = 14f * scaleFactor
                                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                                    isAntiAlias = true
                                }
                                val bgPaint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.argb(235, 255, 255, 255)
                                    style = android.graphics.Paint.Style.FILL
                                    isAntiAlias = true
                                }
                                val borderPaint = android.graphics.Paint().apply {
                                    color = stroke.color.toInt()
                                    this.strokeWidth = 1.5f * scaleFactor
                                    style = android.graphics.Paint.Style.STROKE
                                    isAntiAlias = true
                                }
                                val textLines = text.split("\n")
                                var maxLineWidth = 0f
                                for (line in textLines) {
                                    val w = paint.measureText(line)
                                    if (w > maxLineWidth) maxLineWidth = w
                                }
                                val lineHeight = paint.fontSpacing
                                val totalHeight = lineHeight * textLines.size
                                val pad = 8f * scaleFactor
                                val rect = android.graphics.RectF(
                                    center.x - pad,
                                    center.y - lineHeight + pad / 2f,
                                    center.x + maxLineWidth + pad,
                                    center.y + totalHeight - lineHeight + pad
                                )
                                canvas.nativeCanvas.drawRoundRect(rect, 6f * scaleFactor, 6f * scaleFactor, bgPaint)
                                canvas.nativeCanvas.drawRoundRect(rect, 6f * scaleFactor, 6f * scaleFactor, borderPaint)
                                var curY = center.y
                                for (line in textLines) {
                                    canvas.nativeCanvas.drawText(line, center.x, curY, paint)
                                    curY += lineHeight
                                }
                            }
                        }
                    }
                }
                InkTool.STAMP -> {
                    if (stroke.points.isNotEmpty()) {
                        val center = transformer.pdfToScreen(stroke.points.first(), pageBounds)
                        val stampText = annotation.textContent ?: "APROBADO"
                        drawIntoCanvas { canvas ->
                            canvas.nativeCanvas.save()
                            canvas.nativeCanvas.rotate(-6f, center.x, center.y)
                            val stampColor = stroke.color.toInt()
                            val paint = android.graphics.Paint().apply {
                                color = stampColor
                                textSize = 15f * scaleFactor
                                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                                isAntiAlias = true
                            }
                            val bgPaint = android.graphics.Paint().apply {
                                color = android.graphics.Color.argb(35, android.graphics.Color.red(stampColor), android.graphics.Color.green(stampColor), android.graphics.Color.blue(stampColor))
                                style = android.graphics.Paint.Style.FILL
                            }
                            val borderPaint = android.graphics.Paint().apply {
                                color = stampColor
                                this.strokeWidth = 2.5f * scaleFactor
                                style = android.graphics.Paint.Style.STROKE
                            }
                            val textW = paint.measureText(stampText)
                            val textH = paint.fontSpacing
                            val padX = 14f * scaleFactor
                            val padY = 8f * scaleFactor
                            val rect = android.graphics.RectF(
                                center.x - textW / 2f - padX,
                                center.y - textH / 2f - padY,
                                center.x + textW / 2f + padX,
                                center.y + textH / 2f + padY
                            )
                            canvas.nativeCanvas.drawRoundRect(rect, 8f * scaleFactor, 8f * scaleFactor, bgPaint)
                            canvas.nativeCanvas.drawRoundRect(rect, 8f * scaleFactor, 8f * scaleFactor, borderPaint)
                            canvas.nativeCanvas.drawText(stampText, center.x - textW / 2f, center.y + textH * 0.28f, paint)
                            canvas.nativeCanvas.restore()
                        }
                    }
                }
                else -> {
                    // Pen / Signature with full pressure sensitivity
                    val points = stroke.points.map { transformer.pdfToScreen(it, pageBounds) }
                    val pressures = stroke.points.map { it.pressure }
                    smoother.drawPressureStroke(
                        drawScope = this,
                        points = points,
                        pressures = pressures,
                        baseWidth = effectiveWidth,
                        color = strokeColor,
                        isHighlighter = false
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // LAYER 3: LIVE PEN / SHAPES / SNAPPED LINE STROKE
        // -------------------------------------------------------------
        if (liveStrokePoints.isNotEmpty() && !isLiveHighlighting) {
            val liveColor = Color(currentColor).copy(alpha = opacity)
            val effectiveWidth = strokeWidth * scaleFactor

            val isTextSnapped = activeTool == InkTool.UNDERLINE ||
                    activeTool == InkTool.STRIKETHROUGH ||
                    activeTool == InkTool.WAVY_UNDERLINE

            if (liveSnappedSegments.isNotEmpty() && isTextSnapped) {
                for (seg in liveSnappedSegments) {
                    val startScreenX = pageBounds.left + seg.startX * pageBounds.width
                    val endScreenX = pageBounds.left + seg.endX * pageBounds.width
                    val leftScreenX = min(startScreenX, endScreenX)
                    val rightScreenX = max(startScreenX, endScreenX)
                    val centerScreenY = pageBounds.top + seg.centerY * pageBounds.height
                    val baselineScreenY = pageBounds.top + seg.baselineY * pageBounds.height

                    when (activeTool) {
                        InkTool.UNDERLINE -> {
                            drawLine(
                                color = liveColor,
                                start = Offset(leftScreenX, baselineScreenY),
                                end = Offset(rightScreenX, baselineScreenY),
                                strokeWidth = effectiveWidth,
                                cap = StrokeCap.Round
                            )
                        }
                        InkTool.STRIKETHROUGH -> {
                            drawLine(
                                color = liveColor,
                                start = Offset(leftScreenX, centerScreenY),
                                end = Offset(rightScreenX, centerScreenY),
                                strokeWidth = effectiveWidth,
                                cap = StrokeCap.Round
                            )
                        }
                        InkTool.WAVY_UNDERLINE -> {
                            val wavyPath = smoother.createWavyPath(
                                start = Offset(leftScreenX, baselineScreenY),
                                end = Offset(rightScreenX, baselineScreenY),
                                waveLength = 8f * scaleFactor,
                                amplitude = 1.8f * scaleFactor
                            )
                            drawPath(
                                path = wavyPath,
                                color = liveColor,
                                style = Stroke(
                                    width = effectiveWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                        else -> {}
                    }
                }
            } else if (activeTool == InkTool.SELECT_TEXT) {
                if (liveStrokePoints.size >= 2) {
                    val start = liveStrokePoints.first().offset
                    val current = liveStrokePoints.last().offset
                    val left = minOf(start.x, current.x)
                    val top = minOf(start.y, current.y)
                    val right = maxOf(start.x, current.x)
                    val bottom = maxOf(start.y, current.y)
                    drawRect(
                        color = Color(0x333B82F6),
                        topLeft = Offset(left, top),
                        size = Size(right - left, bottom - top)
                    )
                    drawRect(
                        color = Color(0xFF3B82F6),
                        topLeft = Offset(left, top),
                        size = Size(right - left, bottom - top),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            } else if (activeTool == InkTool.RECTANGLE && liveStrokePoints.size >= 2) {
                val p1 = liveStrokePoints.first().offset
                val p2 = liveStrokePoints.last().offset
                val left = minOf(p1.x, p2.x)
                val right = maxOf(p1.x, p2.x)
                val top = minOf(p1.y, p2.y)
                val bottom = maxOf(p1.y, p2.y)
                drawRoundRect(
                    color = liveColor,
                    topLeft = Offset(left, top),
                    size = Size((right - left).coerceAtLeast(1f), (bottom - top).coerceAtLeast(1f)),
                    cornerRadius = CornerRadius(4f * scaleFactor, 4f * scaleFactor),
                    style = Stroke(width = effectiveWidth)
                )
            } else if (activeTool == InkTool.OVAL && liveStrokePoints.size >= 2) {
                val p1 = liveStrokePoints.first().offset
                val p2 = liveStrokePoints.last().offset
                val left = minOf(p1.x, p2.x)
                val right = maxOf(p1.x, p2.x)
                val top = minOf(p1.y, p2.y)
                val bottom = maxOf(p1.y, p2.y)
                drawOval(
                    color = liveColor,
                    topLeft = Offset(left, top),
                    size = Size((right - left).coerceAtLeast(1f), (bottom - top).coerceAtLeast(1f)),
                    style = Stroke(width = effectiveWidth)
                )
            } else if (activeTool == InkTool.ARROW && liveStrokePoints.size >= 2) {
                val p1 = liveStrokePoints.first().offset
                val p2 = liveStrokePoints.last().offset
                drawLine(
                    color = liveColor,
                    start = p1,
                    end = p2,
                    strokeWidth = effectiveWidth,
                    cap = StrokeCap.Round
                )
                val angle = kotlin.math.atan2((p2.y - p1.y).toDouble(), (p2.x - p1.x).toDouble())
                val headLen = 14f * scaleFactor
                val x1 = (p2.x - headLen * kotlin.math.cos(angle - Math.PI / 6)).toFloat()
                val y1 = (p2.y - headLen * kotlin.math.sin(angle - Math.PI / 6)).toFloat()
                val x2 = (p2.x - headLen * kotlin.math.cos(angle + Math.PI / 6)).toFloat()
                val y2 = (p2.y - headLen * kotlin.math.sin(angle + Math.PI / 6)).toFloat()
                val headPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(p2.x, p2.y)
                    lineTo(x1, y1)
                    lineTo(x2, y2)
                    close()
                }
                drawPath(headPath, color = liveColor)
            } else if (activeTool == InkTool.LINE && liveStrokePoints.size >= 2) {
                val p1 = liveStrokePoints.first().offset
                val p2 = liveStrokePoints.last().offset
                drawLine(
                    color = liveColor,
                    start = p1,
                    end = p2,
                    strokeWidth = effectiveWidth,
                    cap = StrokeCap.Round
                )
            } else if (activeTool == InkTool.LASER_POINTER && liveStrokePoints.isNotEmpty()) {
                val latest = liveStrokePoints.last().offset
                drawCircle(
                    color = Color(0x40FF2D55),
                    center = latest,
                    radius = 16.dp.toPx()
                )
                drawCircle(
                    color = Color(0x90FF2D55),
                    center = latest,
                    radius = 9.dp.toPx()
                )
                drawCircle(
                    color = Color(0xFFFF2D55),
                    center = latest,
                    radius = 4.dp.toPx()
                )
            } else if (activeTool == InkTool.LASSO && liveStrokePoints.size >= 2) {
                val path = androidx.compose.ui.graphics.Path().apply {
                    val p0 = liveStrokePoints.first().offset
                    moveTo(p0.x, p0.y)
                    for (i in 1 until liveStrokePoints.size) {
                        lineTo(liveStrokePoints[i].offset.x, liveStrokePoints[i].offset.y)
                    }
                }
                drawPath(
                    path = path,
                    color = Color(0xFF007AFF),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                    )
                )
            } else {
                // Live Pen stroke
                val livePoints = liveStrokePoints.map { it.offset }
                val livePressures = liveStrokePoints.map { it.pressure }
                smoother.drawPressureStroke(
                    drawScope = this,
                    points = livePoints,
                    pressures = livePressures,
                    baseWidth = effectiveWidth,
                    color = liveColor,
                    isHighlighter = false
                )
            }
        }

        // Persistent text selection rectangle
        val currentSel = selectionRect
        if (currentSel != null && pageBounds != null) {
            val left = pageBounds.left + currentSel.left * pageBounds.width
            val top = pageBounds.top + currentSel.top * pageBounds.height
            val right = pageBounds.left + currentSel.right * pageBounds.width
            val bottom = pageBounds.top + currentSel.bottom * pageBounds.height

            drawRect(
                color = Color(0x283B82F6),
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top)
            )
            drawRect(
                color = Color(0xFF3B82F6),
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        // Persistent Lasso Selection Boundary
        val currentLasso = lassoBounds
        if (currentLasso != null && pageBounds != null) {
            val left = pageBounds.left + currentLasso.left * pageBounds.width
            val top = pageBounds.top + currentLasso.top * pageBounds.height
            val right = pageBounds.left + currentLasso.right * pageBounds.width
            val bottom = pageBounds.top + currentLasso.bottom * pageBounds.height

            drawRoundRect(
                color = Color(0x20007AFF),
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
            drawRoundRect(
                color = Color(0xFF007AFF),
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                style = Stroke(
                    width = 1.8.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 6f))
                )
            )
            // Corner handles
            val handleRadius = 4.5.dp.toPx()
            drawCircle(Color(0xFF007AFF), handleRadius, Offset(left, top))
            drawCircle(Color(0xFF007AFF), handleRadius, Offset(right, top))
            drawCircle(Color(0xFF007AFF), handleRadius, Offset(left, bottom))
            drawCircle(Color(0xFF007AFF), handleRadius, Offset(right, bottom))
        }
    }
}
