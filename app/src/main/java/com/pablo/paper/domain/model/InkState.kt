package com.pablo.paper.domain.model

/**
 * State and actions for the real-time Ink drawing engine.
 */
data class InkState(
    val activeTool: InkTool = InkTool.PEN,
    val currentColor: Long = ColorPalette.BLACK,
    val strokeWidth: Float = 2.5f,
    val opacity: Float = 1.0f,
    val isDrawing: Boolean = false,
    val currentStrokePoints: List<InkPoint> = emptyList(),
    val pageAnnotations: List<Annotation> = emptyList()
)

sealed interface InkAction {
    data class StartStroke(val point: InkPoint) : InkAction
    data class AppendPoint(val point: InkPoint) : InkAction
    data object FinishStroke : InkAction
    data class SelectTool(val tool: InkTool) : InkAction
    data class SelectColor(val color: Long) : InkAction
    data class ChangeWidth(val width: Float) : InkAction
    data class EraseAt(val point: InkPoint, val toleranceRadius: Float) : InkAction
    data object Undo : InkAction
    data object Redo : InkAction
}
