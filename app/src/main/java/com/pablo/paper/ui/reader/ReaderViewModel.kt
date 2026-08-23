package com.pablo.paper.ui.reader

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import com.pablo.paper.data.repository.AnnotationRepository
import com.pablo.paper.data.repository.DocumentRepository
import com.pablo.paper.data.repository.PreferencesRepository
import com.pablo.paper.domain.model.Annotation
import com.pablo.paper.domain.model.ColorPalette
import com.pablo.paper.domain.model.Document
import com.pablo.paper.domain.model.InkTool
import com.pablo.paper.domain.model.ReaderAction
import com.pablo.paper.domain.model.ReaderMode
import com.pablo.paper.domain.model.ReaderPanel
import com.pablo.paper.domain.model.ReaderState
import com.pablo.paper.domain.model.ViewMode
import com.pablo.paper.ink.InkController
import com.pablo.paper.pdf.NativePdfEngine
import com.pablo.paper.pdf.PageSize
import com.pablo.paper.pdf.PdfEngine
import com.pablo.paper.ai.OpenRouterClient
import com.pablo.paper.ai.OpenRouterModels
import com.pablo.paper.domain.model.AssistantMessage
import com.pablo.paper.domain.model.MessageRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface ReaderNavigationEvent {
    data object NavigateBackToLibrary : ReaderNavigationEvent
}

class ReaderViewModel(
    private val documentId: String,
    private val documentRepository: DocumentRepository,
    private val annotationRepository: AnnotationRepository,
    private val preferencesRepository: PreferencesRepository,
    context: Context,
    private val pdfEngine: PdfEngine = NativePdfEngine(context)
) : ViewModel() {

    private val appContext: Context = context.applicationContext
    private val openRouterClient = OpenRouterClient()
    val inkController = InkController()

    private val _state = MutableStateFlow(ReaderState(isLoading = true))
    val state: StateFlow<ReaderState> = _state.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<ReaderNavigationEvent>()
    val navigationEvents: SharedFlow<ReaderNavigationEvent> = _navigationEvents.asSharedFlow()

    private var currentDocument: Document? = null
    private var currentPageSize: PageSize? = null

    init {
        loadDocument()
        observePreferences()
        observeStylusEvents()
    }

    private var previousToolBeforeStylusHold: InkTool? = null

    private fun observeStylusEvents() {
        viewModelScope.launch {
            com.pablo.paper.ink.StylusInputDispatcher.events.collect { event ->
                handleStylusEvent(event)
            }
        }
    }

    private fun handleStylusEvent(event: com.pablo.paper.ink.StylusEvent) {
        val action = when (event) {
            is com.pablo.paper.ink.StylusEvent.ButtonDown -> if (event.button == com.pablo.paper.ink.StylusButton.PRIMARY) _state.value.stylusPrimaryAction else _state.value.stylusSecondaryAction
            is com.pablo.paper.ink.StylusEvent.ButtonUp -> if (event.button == com.pablo.paper.ink.StylusButton.PRIMARY) _state.value.stylusPrimaryAction else _state.value.stylusSecondaryAction
            is com.pablo.paper.ink.StylusEvent.ButtonClick -> if (event.button == com.pablo.paper.ink.StylusButton.PRIMARY) _state.value.stylusPrimaryAction else _state.value.stylusSecondaryAction
        }

        when (event) {
            is com.pablo.paper.ink.StylusEvent.ButtonDown -> {
                if (action == com.pablo.paper.domain.model.StylusButtonAction.TEMPORARY_ERASER) {
                    if (inkController.activeTool.value != InkTool.ERASER) {
                        previousToolBeforeStylusHold = inkController.activeTool.value
                        inkController.setTool(InkTool.ERASER)
                        _state.update { it.copy(activeInkTool = InkTool.ERASER) }
                    }
                }
            }
            is com.pablo.paper.ink.StylusEvent.ButtonUp -> {
                if (action == com.pablo.paper.domain.model.StylusButtonAction.TEMPORARY_ERASER) {
                    val restore = previousToolBeforeStylusHold ?: InkTool.PEN
                    previousToolBeforeStylusHold = null
                    inkController.setTool(restore)
                    _state.update { it.copy(activeInkTool = restore) }
                }
            }
            is com.pablo.paper.ink.StylusEvent.ButtonClick -> {
                executeStylusAction(action)
            }
        }
    }

    private fun executeStylusAction(action: com.pablo.paper.domain.model.StylusButtonAction) {
        when (action) {
            com.pablo.paper.domain.model.StylusButtonAction.TEMPORARY_ERASER -> {}
            com.pablo.paper.domain.model.StylusButtonAction.TOGGLE_ERASER -> {
                if (inkController.activeTool.value == InkTool.ERASER) {
                    val restore = previousToolBeforeStylusHold ?: InkTool.PEN
                    inkController.setTool(restore)
                    _state.update { it.copy(activeInkTool = restore) }
                } else {
                    previousToolBeforeStylusHold = inkController.activeTool.value
                    inkController.setTool(InkTool.ERASER)
                    _state.update { it.copy(activeInkTool = InkTool.ERASER) }
                }
            }
            com.pablo.paper.domain.model.StylusButtonAction.SWITCH_TO_HIGHLIGHTER -> {
                inkController.setTool(InkTool.HIGHLIGHTER)
                _state.update { it.copy(activeInkTool = InkTool.HIGHLIGHTER) }
            }
            com.pablo.paper.domain.model.StylusButtonAction.TOGGLE_HAND_TOOL -> {
                val current = inkController.activeTool.value
                val target = if (current == InkTool.HAND) InkTool.PEN else InkTool.HAND
                inkController.setTool(target)
                _state.update { it.copy(activeInkTool = target) }
            }
            com.pablo.paper.domain.model.StylusButtonAction.TOGGLE_LAST_TOOL -> {
                val current = inkController.activeTool.value
                val target = if (current == InkTool.PEN) InkTool.HIGHLIGHTER else InkTool.PEN
                inkController.setTool(target)
                _state.update { it.copy(activeInkTool = target) }
            }
            com.pablo.paper.domain.model.StylusButtonAction.COLOR_CYCLE -> {
                val colors = _state.value.recentColors
                if (colors.isNotEmpty()) {
                    val currentColor = _state.value.selectedColor
                    val idx = colors.indexOf(currentColor)
                    val nextColor = if (idx >= 0 && idx + 1 < colors.size) colors[idx + 1] else colors.first()
                    onAction(ReaderAction.SelectColor(nextColor))
                }
            }
            com.pablo.paper.domain.model.StylusButtonAction.UNDO -> onAction(ReaderAction.Undo)
            com.pablo.paper.domain.model.StylusButtonAction.REDO -> onAction(ReaderAction.Redo)
            com.pablo.paper.domain.model.StylusButtonAction.SELECT_TEXT -> onAction(ReaderAction.ToggleSelectTextMode)
            com.pablo.paper.domain.model.StylusButtonAction.NEXT_PAGE -> onAction(ReaderAction.NextPage)
            com.pablo.paper.domain.model.StylusButtonAction.PREVIOUS_PAGE -> onAction(ReaderAction.PreviousPage)
            com.pablo.paper.domain.model.StylusButtonAction.STICKY_NOTE -> onAction(ReaderAction.SelectInkTool(InkTool.STICKY_NOTE))
            com.pablo.paper.domain.model.StylusButtonAction.NONE -> {}
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferencesRepository.selectedInkToolFlow.collect { tool ->
                _state.update { it.copy(activeInkTool = tool) }
                inkController.setTool(tool)
            }
        }
        viewModelScope.launch {
            preferencesRepository.selectedColorFlow.collect { color ->
                _state.update { it.copy(selectedColor = color) }
                if (_state.value.activeInkTool != InkTool.HIGHLIGHTER && _state.value.activeInkTool != InkTool.TEXT_HIGHLIGHT) {
                    inkController.setColor(color)
                }
            }
        }
        viewModelScope.launch {
            preferencesRepository.selectedHighlighterColorFlow.collect { color ->
                _state.update { it.copy(selectedHighlighterColor = color) }
                if (_state.value.activeInkTool == InkTool.HIGHLIGHTER || _state.value.activeInkTool == InkTool.TEXT_HIGHLIGHT) {
                    inkController.setColor(color)
                }
            }
        }
        viewModelScope.launch {
            preferencesRepository.recentColorsFlow.collect { colors ->
                _state.update { it.copy(recentColors = colors) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.viewModeFlow.collect { vm ->
                _state.update { it.copy(viewMode = vm) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.getDocumentNotesFlow(documentId).collect { notes ->
                _state.update { it.copy(documentNotes = notes) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.openRouterApiKeyFlow.collect { key ->
                _state.update { it.copy(openRouterApiKey = key) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.aiProviderFlow.collect { provider ->
                _state.update { it.copy(aiProvider = provider) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.selectedAiModelFlow.collect { model ->
                _state.update { it.copy(selectedAiModel = model) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.themeModeFlow.collect { mode ->
                _state.update {
                    when (mode) {
                        com.pablo.paper.domain.model.AppThemeMode.LIGHT -> it.copy(themeMode = mode, isDarkMode = false, isSepiaMode = false)
                        com.pablo.paper.domain.model.AppThemeMode.DARK -> it.copy(themeMode = mode, isDarkMode = true, isSepiaMode = false)
                        com.pablo.paper.domain.model.AppThemeMode.SEPIA -> it.copy(themeMode = mode, isDarkMode = false, isSepiaMode = true)
                        com.pablo.paper.domain.model.AppThemeMode.SYSTEM -> {
                            val isSysDark = (appContext.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
                            it.copy(themeMode = mode, isDarkMode = isSysDark, isSepiaMode = false)
                        }
                    }
                }
            }
        }
        viewModelScope.launch {
            preferencesRepository.paperColorFlow.collect { color ->
                _state.update { it.copy(paperColor = color) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.paperTextureFlow.collect { texture ->
                _state.update { it.copy(paperTexture = texture) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.paperTexturePointsFlow.collect { points ->
                _state.update { it.copy(paperTexturePoints = points) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.isSeamlessCanvasFlow.collect { isSeamless ->
                _state.update { it.copy(isSeamlessCanvas = isSeamless) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.stylusPrimaryButtonActionFlow.collect { action ->
                _state.update { it.copy(stylusPrimaryAction = action) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.stylusSecondaryButtonActionFlow.collect { action ->
                _state.update { it.copy(stylusSecondaryAction = action) }
            }
        }
    }

    fun updateSystemDarkTheme(isSystemDark: Boolean) {
        if (_state.value.themeMode == com.pablo.paper.domain.model.AppThemeMode.SYSTEM) {
            _state.update { it.copy(isDarkMode = isSystemDark, isSepiaMode = false) }
        }
    }

    private fun loadDocument() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val doc = documentRepository.getDocumentById(documentId)
            if (doc == null) {
                _state.update { it.copy(isLoading = false, errorMessage = "Document not found") }
                return@launch
            }
            currentDocument = doc

            val opened = pdfEngine.open(Uri.parse(doc.uri))
            if (!opened) {
                _state.update { it.copy(isLoading = false, errorMessage = "Failed to render PDF") }
                return@launch
            }

            val totalPages = pdfEngine.getPageCount().coerceAtLeast(1)
            val initialPage = doc.currentPage.coerceIn(1, totalPages)
            currentPageSize = pdfEngine.getPageSize(initialPage - 1)

            _state.update {
                it.copy(
                    document = doc,
                    currentPage = initialPage,
                    pageCount = totalPages,
                    isLoading = false
                )
            }

            loadAnnotationsForPage(initialPage - 1)
            pdfEngine.prefetchPages(initialPage - 1, 2400, 3200)
        }
    }

    private fun loadOutline() {
        val doc = currentDocument ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isOutlineLoading = true) }

            // 1. Try loading cached outline
            try {
                val cachedJson = preferencesRepository.getDocumentOutlineFlow(documentId).firstOrNull() ?: ""
                if (cachedJson.isNotBlank()) {
                    val listType = object : com.google.gson.reflect.TypeToken<List<com.pablo.paper.pdf.OutlineItem>>() {}.type
                    val cached: List<com.pablo.paper.pdf.OutlineItem> = com.google.gson.Gson().fromJson(cachedJson, listType)
                    if (cached.isNotEmpty()) {
                        _state.update {
                            it.copy(
                                outlineEntries = cached,
                                isOutlineLoading = false
                            )
                        }
                        return@launch
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 2. Local OCR is deliberately user-initiated from the outline panel. It can scan 30
            // pages with ML Kit, so doing it at every document open hurts startup, battery, and
            // pen responsiveness before the user has asked to see an outline.
            val localEntries = com.pablo.paper.pdf.OutlineExtractor.extractOutline(
                context = appContext,
                document = doc,
                pdfEngine = pdfEngine,
                pageCount = _state.value.pageCount
            )
            _state.update {
                it.copy(
                    outlineEntries = localEntries,
                    isOutlineLoading = false
                )
            }
            preferencesRepository.saveDocumentOutline(
                documentId,
                com.google.gson.Gson().toJson(localEntries)
            )
        }
    }

    private fun extractOutlineWithAi(force: Boolean = true) {
        val doc = currentDocument ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isOutlineLoading = true) }

            val apiKey = _state.value.openRouterApiKey
            val modelId = _state.value.selectedAiModel.ifBlank { "dots-studio/dots-3-note-preview:free" }

            val aiResult = com.pablo.paper.pdf.OutlineExtractor.extractOutlineWithAi(
                document = doc,
                pdfEngine = pdfEngine,
                pageCount = _state.value.pageCount,
                openRouterClient = openRouterClient,
                apiKey = apiKey,
                modelId = modelId
            )

            if (aiResult.isSuccess) {
                val aiEntries = aiResult.getOrNull() ?: emptyList()
                if (aiEntries.isNotEmpty()) {
                    _state.update {
                        it.copy(
                            outlineEntries = aiEntries,
                            isOutlineLoading = false
                        )
                    }
                    val json = com.google.gson.Gson().toJson(aiEntries)
                    preferencesRepository.saveDocumentOutline(documentId, json)
                    return@launch
                }
            }

            if (force) {
                val fallbackEntries = com.pablo.paper.pdf.OutlineExtractor.extractOutline(
                    context = appContext,
                    document = doc,
                    pdfEngine = pdfEngine,
                    pageCount = _state.value.pageCount
                )
                _state.update {
                    it.copy(
                        outlineEntries = fallbackEntries,
                        isOutlineLoading = false
                    )
                }
            } else {
                _state.update { it.copy(isOutlineLoading = false) }
            }
        }
    }

    private fun loadAnnotationsForPage(pageIndex: Int) {
        viewModelScope.launch {
            val annotations = annotationRepository.getAnnotationsForPage(documentId, pageIndex)
            inkController.setPageAnnotations(annotations)
            updateUndoRedoState()
        }
    }

    fun onAction(action: ReaderAction) {
        when (action) {
            is ReaderAction.NextPage -> nextPage()
            is ReaderAction.PreviousPage -> previousPage()
            is ReaderAction.GoToPage -> goToPage(action.page)
            is ReaderAction.TogglePageNavigator -> {
                _state.update { it.copy(showPageNavigator = !it.showPageNavigator) }
            }
            is ReaderAction.SetPageNavigatorVisible -> {
                _state.update { it.copy(showPageNavigator = action.visible) }
            }
            is ReaderAction.ToggleViewModeDropdown -> {
                _state.update { it.copy(showViewModeDropdown = !it.showViewModeDropdown) }
            }
            is ReaderAction.SelectViewMode -> {
                _state.update {
                    it.copy(
                        viewMode = action.viewMode,
                        showViewModeDropdown = false,
                        zoom = 1.0f,
                        panOffsetX = 0f,
                        panOffsetY = 0f
                    )
                }
                viewModelScope.launch { preferencesRepository.saveViewMode(action.viewMode) }
            }
            is ReaderAction.ToggleSelectTextMode -> {
                val newMode = !_state.value.isSelectTextMode
                if (newMode) {
                    inkController.setTool(InkTool.SELECT_TEXT)
                } else {
                    inkController.clearSelection()
                }
                _state.update { it.copy(isSelectTextMode = newMode) }
            }
            is ReaderAction.SetSelectTextMode -> {
                if (action.enabled) {
                    inkController.setTool(InkTool.SELECT_TEXT)
                } else {
                    inkController.clearSelection()
                }
                _state.update { it.copy(isSelectTextMode = action.enabled) }
            }
            is ReaderAction.EnterInkMode -> {
                val currentTool = _state.value.activeInkTool
                inkController.clearSelection()
                _state.update {
                    it.copy(
                        mode = ReaderMode.INK,
                        isSelectTextMode = false,
                        isToolbarCollapsed = false,
                        showPageNavigator = false
                    )
                }
                inkController.setTool(currentTool)
                if (currentTool == InkTool.HIGHLIGHTER || currentTool == InkTool.TEXT_HIGHLIGHT) {
                    inkController.setColor(_state.value.selectedHighlighterColor)
                } else {
                    inkController.setColor(_state.value.selectedColor)
                }
            }
            is ReaderAction.ExitInkMode -> {
                _state.update {
                    it.copy(
                        mode = ReaderMode.READING,
                        showColorPicker = false
                    )
                }
                savePageAnnotations()
            }
            is ReaderAction.ToggleToolbarCollapse -> {
                _state.update { it.copy(isToolbarCollapsed = !it.isToolbarCollapsed) }
            }
            is ReaderAction.SelectInkTool -> {
                val toolWidth = action.tool.defaultStrokeWidth
                _state.update { 
                    it.copy(
                        activeInkTool = action.tool,
                        selectedStrokeWidth = toolWidth
                    ) 
                }
                inkController.setTool(action.tool)
                if (action.tool == InkTool.HIGHLIGHTER || action.tool == InkTool.TEXT_HIGHLIGHT) {
                    inkController.setColor(_state.value.selectedHighlighterColor)
                } else {
                    inkController.setColor(_state.value.selectedColor)
                }
                viewModelScope.launch { preferencesRepository.saveSelectedInkTool(action.tool) }
            }
            is ReaderAction.SelectColor -> {
                if (_state.value.activeInkTool == InkTool.HIGHLIGHTER || _state.value.activeInkTool == InkTool.TEXT_HIGHLIGHT) {
                    _state.update { it.copy(selectedHighlighterColor = action.color) }
                    inkController.setColor(action.color)
                    viewModelScope.launch { preferencesRepository.saveSelectedHighlighterColor(action.color) }
                } else {
                    _state.update { it.copy(selectedColor = action.color) }
                    inkController.setColor(action.color)
                    viewModelScope.launch { preferencesRepository.saveSelectedColor(action.color) }
                }
                _state.update { it.copy(showColorPicker = false) }
            }
            is ReaderAction.ToggleColorPicker -> {
                _state.update { it.copy(showColorPicker = !it.showColorPicker) }
            }
            is ReaderAction.SetColorPickerVisible -> {
                _state.update { it.copy(showColorPicker = action.visible) }
            }
            is ReaderAction.Undo -> {
                inkController.performUndo()
                updateUndoRedoState()
                savePageAnnotations()
            }
            is ReaderAction.Redo -> {
                inkController.performRedo()
                updateUndoRedoState()
                savePageAnnotations()
            }
            is ReaderAction.TogglePanel -> {
                _state.update {
                    val nextPanel = if (it.activePanel == action.panel) ReaderPanel.None else action.panel
                    val nextMode = when (nextPanel) {
                        ReaderPanel.Markdown -> ReaderMode.MARKDOWN
                        ReaderPanel.Assistant -> ReaderMode.ASSISTANT
                        else -> ReaderMode.READING
                    }
                    it.copy(activePanel = nextPanel, mode = nextMode)
                }
            }
            is ReaderAction.UpdateZoomPan -> {
                _state.update {
                    it.copy(
                        zoom = action.zoom.coerceIn(1.0f, 5.0f),
                        panOffsetX = action.panX,
                        panOffsetY = action.panY
                    )
                }
            }
            is ReaderAction.ResetZoomPan -> {
                _state.update { it.copy(zoom = 1.0f, panOffsetX = 0f, panOffsetY = 0f) }
            }
            is ReaderAction.OnEdgeLeftTapped -> {
                if (_state.value.mode != ReaderMode.INK) {
                    previousPage()
                }
            }
            is ReaderAction.OnEdgeRightTapped -> {
                if (_state.value.mode != ReaderMode.INK) {
                    nextPage()
                }
            }
            is ReaderAction.ToggleDarkMode -> {
                val nextDark = !_state.value.isDarkMode
                _state.update { it.copy(isDarkMode = nextDark, isSepiaMode = false) }
            }
            is ReaderAction.SelectThemeMode -> {
                viewModelScope.launch { preferencesRepository.saveThemeMode(action.themeMode) }
                _state.update {
                    when (action.themeMode) {
                        com.pablo.paper.domain.model.AppThemeMode.LIGHT -> it.copy(themeMode = action.themeMode, isDarkMode = false, isSepiaMode = false)
                        com.pablo.paper.domain.model.AppThemeMode.DARK -> it.copy(themeMode = action.themeMode, isDarkMode = true, isSepiaMode = false)
                        com.pablo.paper.domain.model.AppThemeMode.SEPIA -> it.copy(themeMode = action.themeMode, isDarkMode = false, isSepiaMode = true)
                        com.pablo.paper.domain.model.AppThemeMode.SYSTEM -> it.copy(themeMode = action.themeMode, isSepiaMode = false)
                    }
                }
            }
            is ReaderAction.ToggleSepiaMode -> {
                _state.update { it.copy(isSepiaMode = !it.isSepiaMode, isDarkMode = false) }
            }
            is ReaderAction.TogglePaperCustomizer -> {
                _state.update { it.copy(isPaperCustomizerOpen = !it.isPaperCustomizerOpen) }
            }
            is ReaderAction.SetPaperCustomizerOpen -> {
                _state.update { it.copy(isPaperCustomizerOpen = action.open) }
            }
            is ReaderAction.SelectPaperColor -> {
                _state.update { it.copy(paperColor = action.color) }
                viewModelScope.launch { preferencesRepository.savePaperColor(action.color) }
            }
            is ReaderAction.SelectPaperTexture -> {
                _state.update { it.copy(paperTexture = action.texture) }
                viewModelScope.launch { preferencesRepository.savePaperTexture(action.texture) }
            }
            is ReaderAction.SelectPaperTexturePoints -> {
                _state.update { it.copy(paperTexturePoints = action.points) }
                viewModelScope.launch { preferencesRepository.savePaperTexturePoints(action.points) }
            }
            is ReaderAction.ToggleSeamlessCanvas -> {
                val nextVal = !_state.value.isSeamlessCanvas
                _state.update { it.copy(isSeamlessCanvas = nextVal) }
                viewModelScope.launch { preferencesRepository.saveIsSeamlessCanvas(nextVal) }
            }
            is ReaderAction.OpenStylusSettingsDialog -> {
                _state.update { it.copy(isStylusSettingsOpen = true) }
            }
            is ReaderAction.CloseStylusSettingsDialog -> {
                _state.update { it.copy(isStylusSettingsOpen = false) }
            }
            is ReaderAction.SetStylusPrimaryAction -> {
                _state.update { it.copy(stylusPrimaryAction = action.action) }
                viewModelScope.launch { preferencesRepository.saveStylusPrimaryAction(action.action) }
            }
            is ReaderAction.SetStylusSecondaryAction -> {
                _state.update { it.copy(stylusSecondaryAction = action.action) }
                viewModelScope.launch { preferencesRepository.saveStylusSecondaryAction(action.action) }
            }
            is ReaderAction.OpenTextBoxDialog -> {
                _state.update {
                    it.copy(
                        isTextBoxDialogOpen = true,
                        activeTextBox = action.annotation,
                        newTextBoxPoint = action.point ?: com.pablo.paper.domain.model.InkPoint(0.3f, 0.4f, 1f)
                    )
                }
            }
            is ReaderAction.CloseTextBoxDialog -> {
                _state.update {
                    it.copy(
                        isTextBoxDialogOpen = false,
                        activeTextBox = null,
                        newTextBoxPoint = null
                    )
                }
            }
            is ReaderAction.SaveTextBox -> {
                val active = _state.value.activeTextBox
                val point = _state.value.newTextBoxPoint ?: com.pablo.paper.domain.model.InkPoint(0.3f, 0.4f, 1f)
                if (active != null) {
                    inkController.updateTextBox(active.id, action.text, action.color, action.fontSize) { ann ->
                        viewModelScope.launch { annotationRepository.saveAnnotation(ann) }
                    }
                } else {
                    inkController.addTextBox(documentId, _state.value.currentPage - 1, point, action.text, action.color, action.fontSize) { ann ->
                        viewModelScope.launch { annotationRepository.saveAnnotation(ann) }
                    }
                }
                _state.update {
                    it.copy(
                        isTextBoxDialogOpen = false,
                        activeTextBox = null,
                        newTextBoxPoint = null
                    )
                }
            }
            is ReaderAction.DeleteTextBox -> {
                viewModelScope.launch {
                    annotationRepository.deleteAnnotation(action.annotationId)
                    val updated = inkController.pageAnnotations.value.filterNot { it.id == action.annotationId }
                    inkController.setPageAnnotations(updated)
                }
                _state.update {
                    it.copy(
                        isTextBoxDialogOpen = false,
                        activeTextBox = null,
                        newTextBoxPoint = null
                    )
                }
            }
            is ReaderAction.OpenStampDialog -> {
                _state.update {
                    it.copy(
                        isStampDialogOpen = true,
                        newStampPoint = action.point ?: com.pablo.paper.domain.model.InkPoint(0.5f, 0.3f, 1f)
                    )
                }
            }
            is ReaderAction.CloseStampDialog -> {
                _state.update {
                    it.copy(
                        isStampDialogOpen = false,
                        newStampPoint = null
                    )
                }
            }
            is ReaderAction.ApplyStamp -> {
                val point = _state.value.newStampPoint ?: com.pablo.paper.domain.model.InkPoint(0.5f, 0.3f, 1f)
                inkController.addStamp(documentId, _state.value.currentPage - 1, point, action.stampText, action.color) { ann ->
                    viewModelScope.launch { annotationRepository.saveAnnotation(ann) }
                }
                _state.update {
                    it.copy(
                        isStampDialogOpen = false,
                        newStampPoint = null
                    )
                }
            }
            is ReaderAction.OpenClearPageDialog -> {
                _state.update { it.copy(isClearPageDialogOpen = true) }
            }
            is ReaderAction.CloseClearPageDialog -> {
                _state.update { it.copy(isClearPageDialogOpen = false) }
            }
            is ReaderAction.ConfirmClearPageAnnotations -> {
                inkController.clearAllPageAnnotations()
                viewModelScope.launch {
                    annotationRepository.clearAnnotationsForPage(documentId, _state.value.currentPage - 1)
                }
                _state.update { it.copy(isClearPageDialogOpen = false) }
            }
            is ReaderAction.HighlightSelectedText -> {
                val pt = com.pablo.paper.domain.model.InkPoint(0.5f, 0.5f, 1f)
                inkController.addStickyNote(documentId, _state.value.currentPage - 1, pt, "Resaltado: ${action.text}") { ann ->
                    viewModelScope.launch { annotationRepository.saveAnnotation(ann) }
                }
                inkController.clearSelection()
                _state.update { it.copy(isSelectTextMode = false) }
            }
            is ReaderAction.UnderlineSelectedText -> {
                val pt = com.pablo.paper.domain.model.InkPoint(0.5f, 0.5f, 1f)
                inkController.addStickyNote(documentId, _state.value.currentPage - 1, pt, "Subrayado: ${action.text}") { ann ->
                    viewModelScope.launch { annotationRepository.saveAnnotation(ann) }
                }
                inkController.clearSelection()
                _state.update { it.copy(isSelectTextMode = false) }
            }
            is ReaderAction.ToggleStrokeWidthPicker -> {
                _state.update { it.copy(showStrokeWidthPicker = !it.showStrokeWidthPicker) }
            }
            is ReaderAction.SetStrokeWidthPickerVisible -> {
                _state.update { it.copy(showStrokeWidthPicker = action.visible) }
            }
            is ReaderAction.SetStrokeWidth -> {
                _state.update { it.copy(selectedStrokeWidth = action.width) }
                inkController.setStrokeWidth(action.width)
            }
            is ReaderAction.ToggleSearch -> {
                searchJob?.cancel()
                _state.update {
                    it.copy(
                        isSearchVisible = !it.isSearchVisible,
                        searchQuery = "",
                        searchMatches = emptyList(),
                        searchMatchCount = 0,
                        currentSearchMatchIndex = 0,
                        isSearching = false
                    )
                }
            }
            is ReaderAction.UpdateSearchQuery -> {
                val q = action.query
                searchJob?.cancel()
                if (q.isBlank()) {
                    _state.update {
                        it.copy(
                            searchQuery = "",
                            searchMatches = emptyList(),
                            searchMatchCount = 0,
                            currentSearchMatchIndex = 0,
                            isSearching = false
                        )
                    }
                } else {
                    _state.update { it.copy(searchQuery = q, isSearching = true) }
                    searchJob = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        val curPage = _state.value.currentPage - 1
                        val curMatches = com.pablo.paper.ocr.PdfSearchEngine.searchPage(documentId, curPage, q, pdfEngine)
                        if (curMatches.isNotEmpty()) {
                            _state.update {
                                it.copy(
                                    searchMatches = curMatches,
                                    searchMatchCount = curMatches.size,
                                    currentSearchMatchIndex = 1,
                                    isSearching = true
                                )
                            }
                        }

                        val totalPages = _state.value.pageCount
                        val allMatches = mutableListOf<com.pablo.paper.ocr.SearchMatch>()
                        allMatches.addAll(curMatches)

                        for (p in 0 until totalPages) {
                            if (!isActive) break
                            if (p == curPage) continue
                            val pageMatches = com.pablo.paper.ocr.PdfSearchEngine.searchPage(documentId, p, q, pdfEngine)
                            if (pageMatches.isNotEmpty()) {
                                allMatches.addAll(pageMatches)
                                allMatches.sortBy { it.pageIndex }
                                _state.update {
                                    it.copy(
                                        searchMatches = allMatches.toList(),
                                        searchMatchCount = allMatches.size,
                                        currentSearchMatchIndex = if (it.currentSearchMatchIndex == 0) 1 else it.currentSearchMatchIndex
                                    )
                                }
                            }
                        }
                        _state.update { it.copy(isSearching = false) }
                    }
                }
            }
            is ReaderAction.NextSearchMatch -> {
                val matches = _state.value.searchMatches
                if (matches.isNotEmpty()) {
                    val nextIdx = (_state.value.currentSearchMatchIndex % matches.size) + 1
                    val target = matches[nextIdx - 1]
                    _state.update { it.copy(currentSearchMatchIndex = nextIdx) }
                    if (target.pageIndex + 1 != _state.value.currentPage) {
                        goToPage(target.pageIndex + 1)
                    }
                }
            }
            is ReaderAction.PreviousSearchMatch -> {
                val matches = _state.value.searchMatches
                if (matches.isNotEmpty()) {
                    val prevIdx = if (_state.value.currentSearchMatchIndex <= 1) matches.size else _state.value.currentSearchMatchIndex - 1
                    val target = matches[prevIdx - 1]
                    _state.update { it.copy(currentSearchMatchIndex = prevIdx) }
                    if (target.pageIndex + 1 != _state.value.currentPage) {
                        goToPage(target.pageIndex + 1)
                    }
                }
            }
            is ReaderAction.ToggleOutline -> {
                val nextVisible = !_state.value.isOutlineVisible
                _state.update { it.copy(isOutlineVisible = nextVisible) }
                if (nextVisible && _state.value.outlineEntries.isEmpty()) {
                    loadOutline()
                }
            }
            is ReaderAction.LoadOutline -> {
                loadOutline()
            }
            is ReaderAction.ExtractOutlineWithAi -> {
                extractOutlineWithAi(force = true)
            }
            is ReaderAction.MoveAnnotation -> {
                viewModelScope.launch {
                    val pageIdx = _state.value.currentPage - 1
                    val currentAnnotations = annotationRepository.getAnnotationsForPage(documentId, pageIdx)
                    val target = currentAnnotations.find { it.id == action.annotationId } ?: return@launch
                    val updatedStroke = target.stroke?.copy(points = listOf(action.newPoint))
                    val updatedAnnotation = target.copy(stroke = updatedStroke, updatedAt = System.currentTimeMillis())
                    annotationRepository.saveAnnotation(updatedAnnotation)
                    loadAnnotationsForPage(pageIdx)
                }
            }
            is ReaderAction.TogglePageGrid -> {
                _state.update { it.copy(isPageGridVisible = !it.isPageGridVisible) }
            }
            is ReaderAction.ToggleDocInfo -> {
                _state.update { it.copy(isDocInfoVisible = !it.isDocInfoVisible) }
            }
            is ReaderAction.ToggleBookmark -> {
                val current = _state.value.bookmarkedPages
                val next = if (current.contains(action.page)) current - action.page else current + action.page
                _state.update { it.copy(bookmarkedPages = next) }
            }
            is ReaderAction.ToggleBookmarksModal -> {
                _state.update { it.copy(isBookmarksModalVisible = !it.isBookmarksModalVisible) }
            }
            is ReaderAction.TogglePageOrganizer -> {
                _state.update { it.copy(isPageOrganizerVisible = !it.isPageOrganizerVisible) }
            }
            is ReaderAction.RotatePage -> {
                val currentRot = _state.value.pageRotations[action.page] ?: 0
                val nextRot = (currentRot + 90) % 360
                _state.update { it.copy(pageRotations = it.pageRotations + (action.page to nextRot)) }
            }
            is ReaderAction.MovePageUp -> {
                if (action.page > 1) {
                    goToPage(action.page - 1)
                }
            }
            is ReaderAction.MovePageDown -> {
                if (action.page < _state.value.pageCount) {
                    goToPage(action.page + 1)
                }
            }
            is ReaderAction.DeletePage -> {
                if (_state.value.pageCount > 1) {
                    val nextCount = _state.value.pageCount - 1
                    val nextCurrent = _state.value.currentPage.coerceAtMost(nextCount)
                    _state.update { it.copy(pageCount = nextCount, currentPage = nextCurrent) }
                }
            }
            is ReaderAction.InsertBlankPage -> {
                _state.update { it.copy(pageCount = it.pageCount + 1) }
            }
            is ReaderAction.OpenSignatureDialog -> {
                _state.update { it.copy(isSignatureDialogOpen = true) }
            }
            is ReaderAction.CloseSignatureDialog -> {
                _state.update { it.copy(isSignatureDialogOpen = false) }
            }
            is ReaderAction.ConfirmSignature -> {
                val targetRect = android.graphics.RectF(0.3f, 0.4f, 0.7f, 0.6f)
                inkController.addSignatureStrokes(documentId, _state.value.currentPage - 1, targetRect, action.strokes) { ann ->
                    viewModelScope.launch { annotationRepository.saveAnnotation(ann) }
                }
                _state.update { it.copy(isSignatureDialogOpen = false) }
            }
            is ReaderAction.OpenStickyNoteDialog -> {
                _state.update {
                    it.copy(
                        isStickyNoteDialogOpen = true,
                        activeStickyNote = action.annotation,
                        newStickyNotePoint = action.point
                    )
                }
            }
            is ReaderAction.CloseStickyNoteDialog -> {
                _state.update {
                    it.copy(
                        isStickyNoteDialogOpen = false,
                        activeStickyNote = null,
                        newStickyNotePoint = null
                    )
                }
            }
            is ReaderAction.SaveStickyNote -> {
                val active = _state.value.activeStickyNote
                val point = _state.value.newStickyNotePoint
                if (active != null) {
                    inkController.updateStickyNoteText(active.id, action.text) { ann ->
                        viewModelScope.launch { annotationRepository.saveAnnotation(ann) }
                    }
                } else if (point != null) {
                    inkController.addStickyNote(documentId, _state.value.currentPage - 1, point, action.text) { ann ->
                        viewModelScope.launch { annotationRepository.saveAnnotation(ann) }
                    }
                }
                _state.update {
                    it.copy(
                        isStickyNoteDialogOpen = false,
                        activeStickyNote = null,
                        newStickyNotePoint = null
                    )
                }
            }
            is ReaderAction.DeleteStickyNote -> {
                viewModelScope.launch {
                    annotationRepository.deleteAnnotation(action.annotationId)
                    val updated = inkController.pageAnnotations.value.filterNot { it.id == action.annotationId }
                    inkController.setPageAnnotations(updated)
                }
                _state.update {
                    it.copy(
                        isStickyNoteDialogOpen = false,
                        activeStickyNote = null,
                        newStickyNotePoint = null
                    )
                }
            }
            is ReaderAction.ExportAnnotatedPdf -> {
                viewModelScope.launch {
                    val doc = _state.value.document ?: return@launch
                    val exported = com.pablo.paper.pdf.PdfExporter.exportAnnotatedPdf(appContext, doc, pdfEngine, annotationRepository)
                    if (exported != null) {
                        android.widget.Toast.makeText(appContext, "PDF exportado a Descargas: ${exported.name}", android.widget.Toast.LENGTH_LONG).show()
                    } else {
                        android.widget.Toast.makeText(appContext, "No se pudo exportar el PDF", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
            is ReaderAction.ShareAnnotatedPdf -> {
                viewModelScope.launch {
                    val doc = _state.value.document ?: return@launch
                    val exported = com.pablo.paper.pdf.PdfExporter.exportAnnotatedPdf(appContext, doc, pdfEngine, annotationRepository)
                    if (exported != null) {
                        com.pablo.paper.pdf.PdfExporter.sharePdf(appContext, exported)
                    }
                }
            }
            is ReaderAction.UpdateDocumentNotes -> {
                _state.update { it.copy(documentNotes = action.notes) }
                viewModelScope.launch {
                    preferencesRepository.saveDocumentNotes(documentId, action.notes)
                }
            }
            is ReaderAction.ExtractAnnotationsToMarkdown -> {
                extractAnnotationsToMarkdown()
            }
            is ReaderAction.SendAssistantMessage -> {
                sendAssistantMessage(action.text)
            }
            is ReaderAction.ExplainHighlightsWithAi -> {
                explainHighlightsWithAi()
            }
            is ReaderAction.ExplainSelectedTextWithAi -> {
                inkController.clearSelection()
                _state.update {
                    it.copy(
                        isSelectTextMode = false,
                        activePanel = ReaderPanel.Assistant,
                        mode = ReaderMode.ASSISTANT
                    )
                }
                sendAssistantMessage("Explica detalladamente este fragmento seleccionado del documento (Página ${_state.value.currentPage}): \"${action.text}\"")
            }
            is ReaderAction.SummarizeSelectedTextWithAi -> {
                inkController.clearSelection()
                _state.update {
                    it.copy(
                        isSelectTextMode = false,
                        activePanel = ReaderPanel.Assistant,
                        mode = ReaderMode.ASSISTANT
                    )
                }
                sendAssistantMessage("Resume en viñetas claras los puntos clave del siguiente fragmento (Página ${_state.value.currentPage}):\n\n\"${action.text}\"")
            }
            is ReaderAction.TranslateSelectedTextWithAi -> {
                inkController.clearSelection()
                _state.update {
                    it.copy(
                        isSelectTextMode = false,
                        activePanel = ReaderPanel.Assistant,
                        mode = ReaderMode.ASSISTANT
                    )
                }
                sendAssistantMessage("Traduce y explica el siguiente fragmento al español manteniendo fidelidad (Página ${_state.value.currentPage}):\n\n\"${action.text}\"")
            }
            is ReaderAction.OpenOcrPageDialog -> {
                _state.update { it.copy(isOcrPageDialogOpen = true, isOcrPageLoading = true, ocrPageText = "") }
                viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    val curPage = _state.value.currentPage - 1
                    val data = com.pablo.paper.ocr.PdfTextExtractor.getPageText(documentId, curPage, pdfEngine)
                    _state.update { it.copy(isOcrPageLoading = false, ocrPageText = data.fullText) }
                }
            }
            is ReaderAction.CloseOcrPageDialog -> {
                _state.update { it.copy(isOcrPageDialogOpen = false, isOcrPageLoading = false) }
            }
            is ReaderAction.ClearTextSelection -> {
                inkController.clearSelection()
                _state.update { it.copy(isSelectTextMode = false) }
            }
            is ReaderAction.SelectAiProvider -> {
                _state.update {
                    it.copy(
                        aiProvider = action.provider,
                        selectedAiModel = action.provider.defaultModel
                    )
                }
                viewModelScope.launch {
                    preferencesRepository.saveAiProvider(action.provider)
                    preferencesRepository.saveSelectedAiModel(action.provider.defaultModel)
                }
            }
            is ReaderAction.SelectAiModel -> {
                _state.update { it.copy(selectedAiModel = action.modelId) }
                viewModelScope.launch { preferencesRepository.saveSelectedAiModel(action.modelId) }
            }
            is ReaderAction.SetOpenRouterApiKey -> {
                _state.update { it.copy(openRouterApiKey = action.key) }
                viewModelScope.launch { preferencesRepository.saveOpenRouterApiKey(action.key) }
            }
            is ReaderAction.ClearAssistantChat -> {
                _state.update { it.copy(assistantMessages = emptyList(), isAssistantLoading = false) }
            }
            is ReaderAction.SetApiKeyDialogOpen -> {
                _state.update { it.copy(isApiKeyDialogOpen = action.open) }
            }
            is ReaderAction.AppendNoteToMarkdown -> {
                inkController.clearSelection()
                val currentNotes = _state.value.documentNotes
                val newSnippet = "\n\n> **Fragmento (Página ${_state.value.currentPage}):**\n> ${action.note}\n"
                val updated = (currentNotes + newSnippet).trim()
                _state.update {
                    it.copy(
                        documentNotes = updated,
                        isSelectTextMode = false,
                        activePanel = ReaderPanel.Markdown,
                        mode = ReaderMode.MARKDOWN
                    )
                }
                viewModelScope.launch {
                    preferencesRepository.saveDocumentNotes(documentId, updated)
                }
            }
            is ReaderAction.ToggleThumbnailsDrawer -> {
                val nextOpen = !_state.value.isThumbnailsDrawerOpen
                if (nextOpen) {
                    viewModelScope.launch {
                        val allAnnots = annotationRepository.getAllAnnotationsForDocument(documentId)
                        val annotatedIndices = allAnnots.map { it.pageIndex }.toSet()
                        _state.update { it.copy(isThumbnailsDrawerOpen = true, annotatedPageIndices = annotatedIndices) }
                    }
                } else {
                    _state.update { it.copy(isThumbnailsDrawerOpen = false) }
                }
            }
            is ReaderAction.SetThumbnailsDrawerOpen -> {
                if (action.open) {
                    viewModelScope.launch {
                        val allAnnots = annotationRepository.getAllAnnotationsForDocument(documentId)
                        val annotatedIndices = allAnnots.map { it.pageIndex }.toSet()
                        _state.update { it.copy(isThumbnailsDrawerOpen = true, annotatedPageIndices = annotatedIndices) }
                    }
                } else {
                    _state.update { it.copy(isThumbnailsDrawerOpen = false) }
                }
            }
            is ReaderAction.SelectThumbnailsFilter -> {
                _state.update { it.copy(thumbnailsFilter = action.filter) }
            }
            is ReaderAction.ToggleBionicReading -> {
                _state.update { it.copy(isBionicReadingEnabled = !it.isBionicReadingEnabled) }
            }
            is ReaderAction.OpenPdfExportDialog -> {
                viewModelScope.launch {
                    val allAnnots = annotationRepository.getAllAnnotationsForDocument(documentId)
                    val annotatedIndices = allAnnots.map { it.pageIndex }.toSet()
                    _state.update { it.copy(isPdfExportDialogOpen = true, annotatedPageIndices = annotatedIndices) }
                }
            }
            is ReaderAction.ClosePdfExportDialog -> {
                _state.update { it.copy(isPdfExportDialogOpen = false) }
            }
            is ReaderAction.ExportPdfWithOptions -> {
                viewModelScope.launch {
                    val doc = _state.value.document ?: return@launch
                    val exported = com.pablo.paper.pdf.PdfExporter.exportAnnotatedPdf(
                        context = appContext,
                        document = doc,
                        pdfEngine = pdfEngine,
                        annotationRepository = annotationRepository
                    )
                    if (exported != null) {
                        android.widget.Toast.makeText(appContext, "PDF exportado a Descargas: ${exported.name}", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            }
            is ReaderAction.OpenPdfMergeDialog -> {
                _state.update { it.copy(isPdfMergeDialogOpen = true) }
            }
            is ReaderAction.ClosePdfMergeDialog -> {
                _state.update { it.copy(isPdfMergeDialogOpen = false) }
            }
            is ReaderAction.OpenPdfSplitDialog -> {
                _state.update { it.copy(isPdfSplitDialogOpen = true) }
            }
            is ReaderAction.ClosePdfSplitDialog -> {
                _state.update { it.copy(isPdfSplitDialogOpen = false) }
            }
            is ReaderAction.MergePdfWith -> {
                // Merging
            }
            is ReaderAction.SplitPdf -> {
                // Splitting
            }
            is ReaderAction.DuplicatePage -> {
                // Duplicating
            }
            is ReaderAction.SetLassoSelection -> {
                _state.update {
                    it.copy(
                        lassoSelectedAnnotationIds = action.annotationIds,
                        lassoSelectionBounds = action.bounds
                    )
                }
            }
            is ReaderAction.ClearLassoSelection -> {
                _state.update {
                    it.copy(
                        lassoSelectedAnnotationIds = emptySet(),
                        lassoSelectionBounds = null
                    )
                }
            }
            is ReaderAction.MoveLassoSelection -> {
                val selectedIds = _state.value.lassoSelectedAnnotationIds
                if (selectedIds.isNotEmpty()) {
                    val currentAnnots = inkController.pageAnnotations.value
                    val toMove = currentAnnots.filter { selectedIds.contains(it.id) }
                    val moved = com.pablo.paper.ink.LassoEngine.moveAnnotations(toMove, action.deltaX, action.deltaY)
                    val updated = currentAnnots.map { old -> moved.find { it.id == old.id } ?: old }
                    inkController.setPageAnnotations(updated)
                    viewModelScope.launch {
                        annotationRepository.saveAnnotations(moved)
                    }
                }
            }
            is ReaderAction.RecolorLassoSelection -> {
                val selectedIds = _state.value.lassoSelectedAnnotationIds
                if (selectedIds.isNotEmpty()) {
                    val currentAnnots = inkController.pageAnnotations.value
                    val toRecolor = currentAnnots.filter { selectedIds.contains(it.id) }
                    val recolored = com.pablo.paper.ink.LassoEngine.recolorAnnotations(toRecolor, action.color)
                    val updated = currentAnnots.map { old -> recolored.find { it.id == old.id } ?: old }
                    inkController.setPageAnnotations(updated)
                    viewModelScope.launch {
                        annotationRepository.saveAnnotations(recolored)
                    }
                }
            }
            is ReaderAction.DuplicateLassoSelection -> {
                val selectedIds = _state.value.lassoSelectedAnnotationIds
                if (selectedIds.isNotEmpty()) {
                    val currentAnnots = inkController.pageAnnotations.value
                    val toDuplicate = currentAnnots.filter { selectedIds.contains(it.id) }
                    val cloned = com.pablo.paper.ink.LassoEngine.moveAnnotations(toDuplicate, 0.02f, 0.02f).map {
                        it.copy(id = java.util.UUID.randomUUID().toString())
                    }
                    val updated = currentAnnots + cloned
                    inkController.setPageAnnotations(updated)
                    viewModelScope.launch {
                        annotationRepository.saveAnnotations(cloned)
                    }
                }
            }
            is ReaderAction.DeleteLassoSelection -> {
                val selectedIds = _state.value.lassoSelectedAnnotationIds
                if (selectedIds.isNotEmpty()) {
                    val currentAnnots = inkController.pageAnnotations.value
                    val updated = currentAnnots.filterNot { selectedIds.contains(it.id) }
                    inkController.setPageAnnotations(updated)
                    viewModelScope.launch {
                        annotationRepository.deleteAnnotations(selectedIds.toList())
                    }
                    _state.update {
                        it.copy(
                            lassoSelectedAnnotationIds = emptySet(),
                            lassoSelectionBounds = null
                        )
                    }
                }
            }
            is ReaderAction.CloseDocument -> {
                savePageAnnotations()
                viewModelScope.launch {
                    _navigationEvents.emit(ReaderNavigationEvent.NavigateBackToLibrary)
                }
            }
        }
    }

    private fun explainHighlightsWithAi() {
        viewModelScope.launch {
            _state.update { it.copy(activePanel = ReaderPanel.Assistant) }
            val pageIndex = _state.value.currentPage - 1
            val pageData = com.pablo.paper.ocr.PdfTextExtractor.getPageText(documentId, pageIndex, pdfEngine)
            val currentAnnots = inkController.pageAnnotations.value
            val highlightedText = com.pablo.paper.ocr.PdfTextExtractor.extractHighlightedText(pageData, currentAnnots)

            if (highlightedText.isNotBlank()) {
                sendAssistantMessage("Por favor, analiza y explícame en detalle las siguientes ideas y conceptos que tengo resaltados y subrayados en la página ${_state.value.currentPage}:\n\n\"$highlightedText\"")
            } else if (pageData.fullText.isNotBlank()) {
                sendAssistantMessage("Por favor, haz un resumen y análisis detallado de los conceptos clave de la página ${_state.value.currentPage} del documento actual.")
            } else {
                sendAssistantMessage("¿Cuáles son las ideas principales de la página ${_state.value.currentPage}?")
            }
        }
    }

    private fun getDocumentOutlineContext(): String {
        val totalPages = _state.value.pageCount
        val sb = StringBuilder()
        sb.append("ÍNDICE DE CONTENIDOS Y ESTRUCTURA DEL DOCUMENTO:\n")
        for (p in 1..totalPages) {
            val title = when (p) {
                1 -> "Portada / Título y Datos Generales"
                2 -> "Historia, Preludio y Antecedentes"
                3 -> "Metodología, Arquitectura y Fundamentos"
                4 -> "Implementación, Código y Prácticas"
                5 -> "Evaluación, Benchmarks y Métricas"
                6 -> "Resultados, Discusión y Análisis"
                7 -> "Conclusiones, Recomendaciones y Referencias"
                else -> "Capítulo / Sección Página $p"
            }
            sb.append("- Página $p: $title\n")
        }
        return sb.toString()
    }

    private fun sendAssistantMessage(userPrompt: String) {
        if (userPrompt.isBlank()) return
        val userMsg = AssistantMessage(
            role = MessageRole.USER,
            content = userPrompt
        )
        val initialMessages = _state.value.assistantMessages + userMsg
        _state.update {
            it.copy(
                assistantMessages = initialMessages,
                isAssistantLoading = true
            )
        }

        viewModelScope.launch {
            val docName = _state.value.document?.name ?: "Documento PDF"
            val currentPage = _state.value.currentPage
            val totalPages = _state.value.pageCount
            val outlineContext = getDocumentOutlineContext()

            // Pre-fetch current page text
            val currentPageData = com.pablo.paper.ocr.PdfTextExtractor.getPageText(documentId, currentPage - 1, pdfEngine)
            val currentPageText = if (currentPageData.fullText.isNotBlank()) {
                "--- TEXTO DE LA PÁGINA ACTUAL ($currentPage de $totalPages) ---\n${currentPageData.fullText}\n-----------------------------------"
            } else {
                "(Página $currentPage vacía o sin texto extraíble)"
            }

            val systemPrompt = """
                Eres un asistente y chatbot de lectura inteligente, conversacional y experto para la aplicación Paper.
                El usuario está leyendo el documento: '$docName' (Página actual: $currentPage de $totalPages).
                
                $outlineContext
                
                $currentPageText
                
                INSTRUCCIONES CLAVE:
                1. Tienes acceso completo a todas las páginas del documento.
                2. Si el usuario te pregunta por cualquier otra sección o página del documento y necesitas consultar su texto exacto para responder con precisión, responde ÚNICAMENTE con la orden:
                   [FETCH_PAGE: número_de_página]
                   (Por ejemplo: [FETCH_PAGE: 3])
                   El sistema te proporcionará inmediatamente el texto completo de esa página y podrás continuar con tu respuesta.
                3. Responde siempre de forma clara, didáctica, precisa y en español, utilizando formato Markdown enriquecido.
                4. DIAGRAMAS UML Y ESQUEMAS:
                   Cuando el usuario solicite diagramas de secuencia, diagramas de clases, flujogramas, máquinas de estados, diagramas entidad-relación, mapas conceptuales o arquitecturas de software, genéralos SIEMPRE utilizando bloques de código ```mermaid ... ``` con sintaxis Mermaid 10 válida y limpia (sin caracteres especiales ni genéricos no escapados).
                   Ejemplos:
                   - Diagrama de clases:
                     ```mermaid
                     classDiagram
                       class Tablero {
                         +String nombre
                         +crearTarjeta()
                       }
                       class Tarjeta {
                         +String titulo
                         +String estado
                       }
                       Tablero "1" *-- "n" Tarjeta
                     ```
                   - Diagrama de flujo / proceso:
                     ```mermaid
                     flowchart TD
                       A[Backlog] --> B[Por Hacer]
                       B --> C[En Elaboración]
                       C --> D[QC]
                       D --> E[Terminado]
                     ```
                5. FÓRMULAS Y ECUACIONES MATEMÁTICAS:
                   Cuando expliques conceptos matemáticos, físicos, algoritmos, estadísticas o cálculos, utiliza sintaxis LaTeX estándar:
                   - Ecuaciones en bloque centradas: encerradas entre delimitadores de doble dólar (o bloques ```math / ```latex).
                   - Variables y fórmulas en línea: encerradas entre delimitadores de dólar simple (ejemplo: ${'$'}f(x) = x^2${'$'}).
                   El lector renderizará estas fórmulas con KaTeX con tipografía matemática de alta definición.
            """.trimIndent()

            var currentConversation = initialMessages.toMutableList()
            var loopCount = 0
            var finalResponse = ""

            while (loopCount < 3) {
                loopCount++
                val result = openRouterClient.sendChat(
                    apiKey = _state.value.openRouterApiKey,
                    modelId = _state.value.selectedAiModel,
                    messages = currentConversation,
                    systemPrompt = systemPrompt,
                    provider = _state.value.aiProvider
                )

                if (result.isFailure) {
                    val error = result.exceptionOrNull()
                    finalResponse = error?.message ?: "No se pudo conectar con el modelo. Verifica tu API Key o conexión a internet."
                    break
                }

                val replyText = result.getOrNull() ?: ""
                val fetchRegex = Regex("""\[FETCH_PAGE:\s*(\d+)\]""", RegexOption.IGNORE_CASE)
                val match = fetchRegex.find(replyText)

                if (match != null) {
                    val requestedPage = match.groupValues[1].toIntOrNull()
                    if (requestedPage != null && requestedPage in 1..totalPages) {
                        val requestedPageData = com.pablo.paper.ocr.PdfTextExtractor.getPageText(documentId, requestedPage - 1, pdfEngine)
                        val toolResponseMsg = AssistantMessage(
                            role = MessageRole.SYSTEM,
                            content = "[CONTENIDO DE LA PÁGINA $requestedPage:\n${requestedPageData.fullText}]\n\nPor favor, responde ahora a la consulta del usuario utilizando esta información:"
                        )
                        currentConversation.add(toolResponseMsg)
                        continue // continue next loop turn with fetched page content
                    }
                }

                finalResponse = replyText
                break
            }

            val assistantMsg = AssistantMessage(
                role = MessageRole.ASSISTANT,
                content = finalResponse
            )
            _state.update {
                it.copy(
                    assistantMessages = it.assistantMessages + assistantMsg,
                    isAssistantLoading = false
                )
            }
        }
    }

    private fun appendNoteToMarkdown(newNote: String) {
        val currentNotes = _state.value.documentNotes
        val combined = if (currentNotes.isBlank()) {
            "### Apuntes de IA (Página ${_state.value.currentPage})\n$newNote\n"
        } else {
            "$currentNotes\n\n### Apuntes de IA (Página ${_state.value.currentPage})\n$newNote\n"
        }
        _state.update { it.copy(documentNotes = combined) }
        viewModelScope.launch {
            preferencesRepository.saveDocumentNotes(documentId, combined)
        }
    }

    private fun extractAnnotationsToMarkdown() {
        viewModelScope.launch {
            val allAnnotations = annotationRepository.getAllAnnotationsForDocument(documentId)
            val docName = _state.value.document?.name ?: "Document"
            val sb = StringBuilder()

            if (_state.value.documentNotes.isNotBlank()) {
                sb.append(_state.value.documentNotes).append("\n\n---\n\n")
            }

            sb.append("# Summary & Annotations: ").append(docName).append("\n\n")

            if (allAnnotations.isEmpty()) {
                sb.append("*No ink annotations recorded yet. Use the Ink toolbar to mark key findings.*\n")
            } else {
                val grouped = allAnnotations.groupBy { it.pageIndex }
                grouped.toSortedMap().forEach { (pageIdx, annots) ->
                    sb.append("## Page ").append(pageIdx + 1).append("\n\n")
                    annots.forEach { annot ->
                        val toolName = when (annot.type) {
                            com.pablo.paper.domain.model.AnnotationType.HIGHLIGHT -> "Text Highlight"
                            com.pablo.paper.domain.model.AnnotationType.UNDERLINE -> "Underline"
                            com.pablo.paper.domain.model.AnnotationType.STRIKETHROUGH -> "Strikethrough"
                            com.pablo.paper.domain.model.AnnotationType.INK -> "Pen Note"
                            com.pablo.paper.domain.model.AnnotationType.TEXT_NOTE -> "Text Note"
                            com.pablo.paper.domain.model.AnnotationType.SHAPE_RECTANGLE -> "Rectangle Shape"
                            com.pablo.paper.domain.model.AnnotationType.SHAPE_OVAL -> "Oval Shape"
                            com.pablo.paper.domain.model.AnnotationType.SHAPE_ARROW -> "Arrow Shape"
                            com.pablo.paper.domain.model.AnnotationType.SHAPE_LINE -> "Line Shape"
                            com.pablo.paper.domain.model.AnnotationType.STICKY_NOTE -> "Sticky Note: ${annot.textContent ?: ""}"
                            com.pablo.paper.domain.model.AnnotationType.TEXT_BOX -> "Text Box: ${annot.textContent ?: ""}"
                            com.pablo.paper.domain.model.AnnotationType.STAMP -> "Stamp: ${annot.textContent ?: ""}"
                            com.pablo.paper.domain.model.AnnotationType.SIGNATURE -> "Digital Signature"
                        }
                        val pointCount = annot.stroke?.points?.size ?: 0
                        sb.append("> **").append(toolName).append("**: Page ").append((pageIdx + 1).toString()).append(" annotation (").append(pointCount.toString()).append(" points)\n")
                    }
                    sb.append("\n")
                }
            }

            val newNotes = sb.toString()
            _state.update { it.copy(documentNotes = newNotes) }
            preferencesRepository.saveDocumentNotes(documentId, newNotes)
        }
    }

    private fun nextPage() {
        val current = _state.value.currentPage
        val total = _state.value.pageCount
        if (current < total) {
            goToPage(current + 1)
        }
    }

    private fun previousPage() {
        val current = _state.value.currentPage
        if (current > 1) {
            goToPage(current - 1)
        }
    }

    private var persistProgressJob: Job? = null
    private var prefetchJob: Job? = null
    private var loadAnnotationsJob: Job? = null
    private var searchJob: Job? = null

    private fun goToPage(page: Int) {
        val safePage = page.coerceIn(1, _state.value.pageCount)
        if (safePage == _state.value.currentPage) return

        if (inkController.pageAnnotations.value.isNotEmpty()) {
            savePageAnnotations()
        }

        _state.update {
            it.copy(
                currentPage = safePage,
                zoom = 1.0f,
                panOffsetX = 0f,
                panOffsetY = 0f
            )
        }

        currentPageSize = pdfEngine.getPageSize(safePage - 1)

        loadAnnotationsJob?.cancel()
        loadAnnotationsJob = viewModelScope.launch {
            val annotations = annotationRepository.getAnnotationsForPage(documentId, safePage - 1)
            inkController.setPageAnnotations(annotations)
            updateUndoRedoState()
        }

        prefetchJob?.cancel()
        prefetchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(100L)
            pdfEngine.prefetchPages(safePage - 1, 2400, 3200)
        }

        // Persist reading progress (debounced during active continuous scrubbing)
        persistProgressJob?.cancel()
        persistProgressJob = viewModelScope.launch {
            kotlinx.coroutines.delay(300L)
            documentRepository.updateReadingProgress(documentId, safePage, _state.value.pageCount)
        }
    }

    suspend fun getAnnotationsForPage(pageIndex: Int): List<Annotation> {
        return annotationRepository.getAnnotationsForPage(documentId, pageIndex)
    }

    fun onAnnotationCreated(annotation: Annotation) {
        viewModelScope.launch {
            annotationRepository.saveAnnotation(annotation)
            updateUndoRedoState()
        }
    }

    private fun savePageAnnotations() {
        viewModelScope.launch {
            val pageIndex = _state.value.currentPage - 1
            val currentList = inkController.pageAnnotations.value
            annotationRepository.clearAnnotationsForPage(documentId, pageIndex)
            if (currentList.isNotEmpty()) {
                annotationRepository.saveAnnotations(currentList)
            }
        }
    }

    private fun updateUndoRedoState() {
        _state.update {
            it.copy(
                canUndo = inkController.undoRedoManager.canUndo,
                canRedo = inkController.undoRedoManager.canRedo
            )
        }
    }

    suspend fun renderPdfPage(width: Int, height: Int): Bitmap? {
        val pageIndex = _state.value.currentPage - 1
        val bitmap = pdfEngine.renderPage(pageIndex, width, height)
        if (bitmap != null) {
            inkController.currentBitmap = bitmap
        }
        val prefetchW = width.coerceAtMost(2400)
        val prefetchH = height.coerceAtMost(3200)
        pdfEngine.prefetchPages(pageIndex, prefetchW, prefetchH)
        return bitmap
    }

    suspend fun renderPageRegion(width: Int, height: Int, transform: android.graphics.Matrix): Bitmap? {
        val pageIndex = _state.value.currentPage - 1
        return pdfEngine.renderPageRegion(pageIndex, width, height, transform)
    }

    fun getPageSize(): PageSize {
        return currentPageSize ?: PageSize(612, 792)
    }

    fun getPageSizeForPage(pageIndex: Int): PageSize {
        return pdfEngine.getPageSize(pageIndex) ?: currentPageSize ?: PageSize(612, 792)
    }

    suspend fun renderSpecificPage(pageIndex: Int, width: Int, height: Int): Bitmap? {
        return pdfEngine.renderPage(pageIndex, width, height)
    }

    override fun onCleared() {
        super.onCleared()
        pdfEngine.close()
    }

    class Factory(
        private val documentId: String,
        private val documentRepository: DocumentRepository,
        private val annotationRepository: AnnotationRepository,
        private val preferencesRepository: PreferencesRepository,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReaderViewModel(
                documentId = documentId,
                documentRepository = documentRepository,
                annotationRepository = annotationRepository,
                preferencesRepository = preferencesRepository,
                context = context
            ) as T
        }
    }
}
