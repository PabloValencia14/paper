package com.pablo.paper.domain.model

/**
 * Immutable State for the Reader Screen.
 */
data class ReaderState(
    val document: Document? = null,
    val currentPage: Int = 1,
    val pageCount: Int = 1,
    val zoom: Float = 1.0f,
    val panOffsetX: Float = 0f,
    val panOffsetY: Float = 0f,
    val mode: ReaderMode = ReaderMode.READING,
    val viewMode: ViewMode = ViewMode.FULL_PAGE,
    val isDarkMode: Boolean = false,
    val isSepiaMode: Boolean = false,
    val paperColor: PaperColor = PaperColor.WHITE,
    val paperTexture: PaperTexture = PaperTexture.SMOOTH,
    val paperTexturePoints: Float = 24f,
    val isSeamlessCanvas: Boolean = true,
    val isPaperCustomizerOpen: Boolean = false,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val stylusPrimaryAction: StylusButtonAction = StylusButtonAction.TEMPORARY_ERASER,
    val stylusSecondaryAction: StylusButtonAction = StylusButtonAction.SWITCH_TO_HIGHLIGHTER,
    val isStylusSettingsOpen: Boolean = false,
    val isTextBoxDialogOpen: Boolean = false,
    val activeTextBox: Annotation? = null,
    val newTextBoxPoint: InkPoint? = null,
    val isStampDialogOpen: Boolean = false,
    val newStampPoint: InkPoint? = null,
    val isClearPageDialogOpen: Boolean = false,
    val showPageNavigator: Boolean = false,
    val showViewModeDropdown: Boolean = false,
    val showColorPicker: Boolean = false,
    val showStrokeWidthPicker: Boolean = false,
    val isToolbarCollapsed: Boolean = false,
    val activeInkTool: InkTool = InkTool.PEN,
    val selectedColor: Long = ColorPalette.BLACK,
    val selectedHighlighterColor: Long = ColorPalette.YELLOW,
    val selectedStrokeWidth: Float = 3.0f,
    val penWidth: Float = 2.5f,
    val highlighterWidth: Float = 16f,
    val recentColors: List<Long> = ColorPalette.getInitialRecentColors(),
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val activePanel: ReaderPanel = ReaderPanel.None,
    val isSearchVisible: Boolean = false,
    val searchQuery: String = "",
    val searchMatchCount: Int = 0,
    val currentSearchMatchIndex: Int = 0,
    val searchMatches: List<com.pablo.paper.ocr.SearchMatch> = emptyList(),
    val isSearching: Boolean = false,
    val isOcrPageDialogOpen: Boolean = false,
    val ocrPageText: String = "",
    val isOcrPageLoading: Boolean = false,
    val isOutlineVisible: Boolean = false,
    val isPageGridVisible: Boolean = false,
    val isDocInfoVisible: Boolean = false,
    val documentNotes: String = "",
    val assistantMessages: List<AssistantMessage> = emptyList(),
    val isAssistantLoading: Boolean = false,
    val aiProvider: com.pablo.paper.ai.AiProvider = com.pablo.paper.ai.AiProvider.OPENROUTER,
    val selectedAiModel: String = com.pablo.paper.ai.OpenRouterModels.DEFAULT_MODEL,
    val openRouterApiKey: String = "",
    val isApiKeyDialogOpen: Boolean = false,
    val isSelectTextMode: Boolean = false,
    val bookmarkedPages: Set<Int> = emptySet(),
    val isBookmarksModalVisible: Boolean = false,
    val isPageOrganizerVisible: Boolean = false,
    val isSignatureDialogOpen: Boolean = false,
    val isStickyNoteDialogOpen: Boolean = false,
    val activeStickyNote: Annotation? = null,
    val newStickyNotePoint: InkPoint? = null,
    val outlineEntries: List<com.pablo.paper.pdf.OutlineItem> = emptyList(),
    val isOutlineLoading: Boolean = false,
    val pageRotations: Map<Int, Int> = emptyMap(),
    val isThumbnailsDrawerOpen: Boolean = false,
    val thumbnailsFilter: ThumbnailsFilter = ThumbnailsFilter.ALL,
    val annotatedPageIndices: Set<Int> = emptySet(),
    val isBionicReadingEnabled: Boolean = false,
    val isPdfExportDialogOpen: Boolean = false,
    val isPdfMergeDialogOpen: Boolean = false,
    val isPdfSplitDialogOpen: Boolean = false,
    val lassoSelectedAnnotationIds: Set<String> = emptySet(),
    val lassoSelectionBounds: androidx.compose.ui.geometry.Rect? = null,
    val isStudyMaskEnabled: Boolean = false,
    val revealedMaskIds: Set<String> = emptySet(),
    val isDigitalRulerVisible: Boolean = false,
    val isFlashcardModalOpen: Boolean = false,
    val flashcards: List<com.pablo.paper.ui.reader.FlashcardItem> = emptyList(),
    val quizzes: List<com.pablo.paper.ui.reader.QuizQuestion> = emptyList(),
    val isStudyGenerating: Boolean = false,
    val eraserMode: EraserMode = EraserMode.STROKE,
    val exportStatusMessage: String? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = false
)

enum class EraserMode {
    STROKE,
    PRECISION
}

enum class ThumbnailsFilter {
    ALL,
    BOOKMARKED,
    ANNOTATED
}

sealed interface ReaderPanel {
    data object None : ReaderPanel
    data object Markdown : ReaderPanel
    data object Assistant : ReaderPanel
    data object Outline : ReaderPanel
    data object Search : ReaderPanel
}

sealed interface ReaderAction {
    data object NextPage : ReaderAction
    data object PreviousPage : ReaderAction
    data class GoToPage(val page: Int) : ReaderAction
    data object TogglePageNavigator : ReaderAction
    data class SetPageNavigatorVisible(val visible: Boolean) : ReaderAction
    data object ToggleViewModeDropdown : ReaderAction
    data class SelectViewMode(val viewMode: ViewMode) : ReaderAction
    data object ToggleDarkMode : ReaderAction
    data class SelectThemeMode(val themeMode: AppThemeMode) : ReaderAction
    data object ToggleSepiaMode : ReaderAction
    data object TogglePaperCustomizer : ReaderAction
    data class SetPaperCustomizerOpen(val open: Boolean) : ReaderAction
    data class SelectPaperColor(val color: PaperColor) : ReaderAction
    data class SelectPaperTexture(val texture: PaperTexture) : ReaderAction
    data class SelectPaperTexturePoints(val points: Float) : ReaderAction
    data object ToggleSeamlessCanvas : ReaderAction
    data object OpenStylusSettingsDialog : ReaderAction
    data object CloseStylusSettingsDialog : ReaderAction
    data class SetStylusPrimaryAction(val action: StylusButtonAction) : ReaderAction
    data class SetStylusSecondaryAction(val action: StylusButtonAction) : ReaderAction
    data object ToggleSelectTextMode : ReaderAction
    data class SetSelectTextMode(val enabled: Boolean) : ReaderAction
    data object EnterInkMode : ReaderAction
    data object ExitInkMode : ReaderAction
    data object ToggleToolbarCollapse : ReaderAction
    data class SelectInkTool(val tool: InkTool) : ReaderAction
    data class SelectColor(val color: Long) : ReaderAction
    data object ToggleColorPicker : ReaderAction
    data class SetColorPickerVisible(val visible: Boolean) : ReaderAction
    data object ToggleStrokeWidthPicker : ReaderAction
    data class SetStrokeWidthPickerVisible(val visible: Boolean) : ReaderAction
    data class SetStrokeWidth(val width: Float) : ReaderAction
    data object Undo : ReaderAction
    data object Redo : ReaderAction
    data class TogglePanel(val panel: ReaderPanel) : ReaderAction
    data object ToggleSearch : ReaderAction
    data class UpdateSearchQuery(val query: String) : ReaderAction
    data object NextSearchMatch : ReaderAction
    data object PreviousSearchMatch : ReaderAction
    data object ToggleOutline : ReaderAction
    data object LoadOutline : ReaderAction
    data object ExtractOutlineWithAi : ReaderAction
    data object TogglePageGrid : ReaderAction
    data object ToggleDocInfo : ReaderAction
    data class ToggleBookmark(val page: Int) : ReaderAction
    data object ToggleBookmarksModal : ReaderAction
    data object ToggleThumbnailsDrawer : ReaderAction
    data class SetThumbnailsDrawerOpen(val open: Boolean) : ReaderAction
    data class SelectThumbnailsFilter(val filter: ThumbnailsFilter) : ReaderAction
    data object ToggleBionicReading : ReaderAction
    data object OpenPdfExportDialog : ReaderAction
    data object ClosePdfExportDialog : ReaderAction
    data class ExportPdfWithOptions(val includeAnnotations: Boolean, val onlyAnnotatedPages: Boolean) : ReaderAction
    data object OpenPdfMergeDialog : ReaderAction
    data object ClosePdfMergeDialog : ReaderAction
    data object OpenPdfSplitDialog : ReaderAction
    data object ClosePdfSplitDialog : ReaderAction
    data class MergePdfWith(val sourceUri: android.net.Uri) : ReaderAction
    data class SplitPdf(val startPage: Int, val endPage: Int) : ReaderAction
    data object TogglePageOrganizer : ReaderAction
    data class RotatePage(val page: Int) : ReaderAction
    data class MovePageUp(val page: Int) : ReaderAction
    data class MovePageDown(val page: Int) : ReaderAction
    data class DuplicatePage(val page: Int) : ReaderAction
    data class DeletePage(val page: Int) : ReaderAction
    data class InsertBlankPage(val afterPage: Int) : ReaderAction
    data class SetLassoSelection(val annotationIds: Set<String>, val bounds: androidx.compose.ui.geometry.Rect?) : ReaderAction
    data object ClearLassoSelection : ReaderAction
    data class MoveLassoSelection(val deltaX: Float, val deltaY: Float) : ReaderAction
    data class RecolorLassoSelection(val color: Long) : ReaderAction
    data object DuplicateLassoSelection : ReaderAction
    data object DeleteLassoSelection : ReaderAction
    data class MoveAnnotation(val annotationId: String, val newPoint: InkPoint, val pageIndex: Int? = null) : ReaderAction
    data object OpenSignatureDialog : ReaderAction
    data object CloseSignatureDialog : ReaderAction
    data class ConfirmSignature(val strokes: List<List<InkPoint>>) : ReaderAction
    data class OpenStickyNoteDialog(val annotation: Annotation?, val point: InkPoint? = null) : ReaderAction
    data object CloseStickyNoteDialog : ReaderAction
    data class SaveStickyNote(val text: String) : ReaderAction
    data class DeleteStickyNote(val annotationId: String) : ReaderAction
    data class OpenTextBoxDialog(val annotation: Annotation?, val point: InkPoint? = null) : ReaderAction
    data object CloseTextBoxDialog : ReaderAction
    data class SaveTextBox(val text: String, val color: Long, val fontSize: Float) : ReaderAction
    data class DeleteTextBox(val annotationId: String) : ReaderAction
    data class OpenStampDialog(val point: InkPoint? = null) : ReaderAction
    data object CloseStampDialog : ReaderAction
    data class ApplyStamp(val stampText: String, val color: Long) : ReaderAction
    data object OpenClearPageDialog : ReaderAction
    data object CloseClearPageDialog : ReaderAction
    data object ConfirmClearPageAnnotations : ReaderAction
    data class HighlightSelectedText(val text: String) : ReaderAction
    data class UnderlineSelectedText(val text: String) : ReaderAction
    data object ExportAnnotatedPdf : ReaderAction
    data object ShareAnnotatedPdf : ReaderAction
    data class UpdateDocumentNotes(val notes: String) : ReaderAction
    data object ExtractAnnotationsToMarkdown : ReaderAction
    data class SendAssistantMessage(val text: String) : ReaderAction
    data object ExplainHighlightsWithAi : ReaderAction
    data class ExplainSelectedTextWithAi(val text: String) : ReaderAction
    data class SummarizeSelectedTextWithAi(val text: String) : ReaderAction
    data class TranslateSelectedTextWithAi(val text: String) : ReaderAction
    data object OpenOcrPageDialog : ReaderAction
    data object CloseOcrPageDialog : ReaderAction
    data object ClearTextSelection : ReaderAction
    data class SelectAiProvider(val provider: com.pablo.paper.ai.AiProvider) : ReaderAction
    data class SelectAiModel(val modelId: String) : ReaderAction
    data class SetOpenRouterApiKey(val key: String) : ReaderAction
    data object ClearAssistantChat : ReaderAction
    data class SetApiKeyDialogOpen(val open: Boolean) : ReaderAction
    data class AppendNoteToMarkdown(val note: String) : ReaderAction
    data class UpdateZoomPan(val zoom: Float, val panX: Float, val panY: Float) : ReaderAction
    data object ResetZoomPan : ReaderAction
    data object OnEdgeLeftTapped : ReaderAction
    data object OnEdgeRightTapped : ReaderAction
    data object ToggleStudyMask : ReaderAction
    data class ToggleMaskItem(val annotationId: String) : ReaderAction
    data object ToggleDigitalRuler : ReaderAction
    data object ToggleFlashcardModal : ReaderAction
    data class GenerateStudyContent(val isQuiz: Boolean) : ReaderAction
    data class SelectEraserMode(val mode: EraserMode) : ReaderAction
    data object CloseDocument : ReaderAction
}
