package com.pablo.paper.ui.reader

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.pablo.paper.domain.model.ReaderAction
import com.pablo.paper.domain.model.ReaderMode
import com.pablo.paper.domain.model.ReaderPanel
import com.pablo.paper.domain.model.ViewMode
import com.pablo.paper.pdf.CoordinateTransformer
import com.pablo.paper.ui.ink.ColorPickerPopover
import com.pablo.paper.ui.ink.InkCanvas
import com.pablo.paper.ui.ink.InkToolbar
import com.pablo.paper.ui.ink.StrokeWidthPopover
import com.pablo.paper.ui.ink.ToolInfoBanner
import com.pablo.paper.ui.theme.AccentBlue
import com.pablo.paper.ui.theme.BorderSubtle
import com.pablo.paper.ui.theme.CanvasBackground
import com.pablo.paper.ui.theme.CanvasBackgroundDark
import com.pablo.paper.ui.theme.PaperTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.HighlightAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val transformer = remember { CoordinateTransformer() }
    val density = LocalDensity.current
    val isSystemDark = isSystemInDarkTheme()

    LaunchedEffect(isSystemDark, state.themeMode) {
        viewModel.updateSystemDarkTheme(isSystemDark)
    }

    // Intercept back button: dismiss active modal/search/panel/ink first before closing document
    BackHandler {
        if (state.isThumbnailsDrawerOpen) {
            viewModel.onAction(ReaderAction.SetThumbnailsDrawerOpen(false))
        } else if (state.isPdfExportDialogOpen) {
            viewModel.onAction(ReaderAction.ClosePdfExportDialog)
        } else if (state.isSearchVisible) {
            viewModel.onAction(ReaderAction.ToggleSearch)
        } else if (state.isBookmarksModalVisible) {
            viewModel.onAction(ReaderAction.ToggleBookmarksModal)
        } else if (state.isPageOrganizerVisible) {
            viewModel.onAction(ReaderAction.TogglePageOrganizer)
        } else if (state.isStylusSettingsOpen) {
            viewModel.onAction(ReaderAction.CloseStylusSettingsDialog)
        } else if (state.isTextBoxDialogOpen) {
            viewModel.onAction(ReaderAction.CloseTextBoxDialog)
        } else if (state.isStampDialogOpen) {
            viewModel.onAction(ReaderAction.CloseStampDialog)
        } else if (state.isClearPageDialogOpen) {
            viewModel.onAction(ReaderAction.CloseClearPageDialog)
        } else if (state.activePanel != ReaderPanel.None) {
            viewModel.onAction(ReaderAction.TogglePanel(state.activePanel))
        } else if (state.mode == ReaderMode.INK) {
            viewModel.onAction(ReaderAction.ExitInkMode)
        } else {
            viewModel.onAction(ReaderAction.CloseDocument)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                is ReaderNavigationEvent.NavigateBackToLibrary -> onNavigateBack()
            }
        }
    }

    PaperTheme(darkTheme = state.isDarkMode) {
        val currentBg = state.paperColor.getColor(state.isDarkMode)

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(currentBg)
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentBlue)
                }
            } else if (state.errorMessage != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.errorMessage ?: "Unknown error",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                // Layer 1 & 2: Viewport & Ink Canvas with unified zero-jank bounds calculation
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val availableWidth = constraints.maxWidth.toFloat()
                    val availableHeight = constraints.maxHeight.toFloat()
                    val densityVal = density.density

                    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                    val topPaddingPx = with(density) { statusBarHeight.toPx() } + (8f * densityVal)
                    val toolbarBottomPx = topPaddingPx + (54f * densityVal)

                    val displaySize = Size(availableWidth, availableHeight)
                    val pageBounds = transformer.calculatePageBounds(
                        viewportSize = displaySize,
                        pageSize = viewModel.getPageSize(),
                        zoom = state.zoom,
                        panOffset = Offset(state.panOffsetX, state.panOffsetY),
                        viewMode = state.viewMode,
                        topInset = toolbarBottomPx,
                        bottomInset = 24f * densityVal,
                        horizontalInset = if (state.viewMode == ViewMode.FIT_WIDTH) 12f * densityVal else 24f * densityVal
                    )

                    if (state.viewMode == ViewMode.CONTINUOUS_SCROLL) {
                        state.document?.let { doc ->
                            ContinuousScrollViewport(
                                state = state,
                                inkController = viewModel.inkController,
                                documentId = doc.id,
                                onRenderPage = { pIdx, w, h -> viewModel.renderSpecificPage(pIdx, w, h) },
                                getPageSizeForPage = { pIdx -> viewModel.getPageSizeForPage(pIdx) },
                                getAnnotationsForPage = { pIdx -> viewModel.getAnnotationsForPage(pIdx) },
                                onAnnotationCreated = { viewModel.onAnnotationCreated(it) },
                                onPageChanged = { newPage -> viewModel.onAction(ReaderAction.GoToPage(newPage)) },
                                onAction = { viewModel.onAction(it) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else if (state.viewMode == ViewMode.TWO_PAGE) {
                        state.document?.let { doc ->
                            TwoPageSpreadViewport(
                                state = state,
                                inkController = viewModel.inkController,
                                documentId = doc.id,
                                onRenderPage = { pIdx, w, h -> viewModel.renderSpecificPage(pIdx, w, h) },
                                getPageSizeForPage = { pIdx -> viewModel.getPageSizeForPage(pIdx) },
                                getAnnotationsForPage = { pIdx -> viewModel.getAnnotationsForPage(pIdx) },
                                onAnnotationCreated = { viewModel.onAnnotationCreated(it) },
                                onAction = { viewModel.onAction(it) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        PdfViewport(
                            state = state,
                            pageBounds = pageBounds,
                            displaySize = displaySize,
                            pageSize = viewModel.getPageSize(),
                            onRenderPage = { w, h -> viewModel.renderPdfPage(w, h) },
                            onAction = { viewModel.onAction(it) },
                            modifier = Modifier.fillMaxSize()
                        )

                        state.document?.let { doc ->
                            InkCanvas(
                                inkController = viewModel.inkController,
                                pageBounds = pageBounds,
                                pageSize = viewModel.getPageSize(),
                                documentId = doc.id,
                                pageIndex = state.currentPage - 1,
                                isInkModeEnabled = true,
                                isSelectModeEnabled = state.isSelectTextMode,
                                zoom = state.zoom,
                                panOffsetX = state.panOffsetX,
                                panOffsetY = state.panOffsetY,
                                stylusPrimaryAction = state.stylusPrimaryAction,
                                stylusSecondaryAction = state.stylusSecondaryAction,
                                isToolbarCollapsed = state.isToolbarCollapsed,
                                onZoomPanChanged = { newZoom, panX, panY ->
                                    viewModel.onAction(ReaderAction.UpdateZoomPan(newZoom, panX, panY))
                                },
                                onNextPage = { viewModel.onAction(ReaderAction.NextPage) },
                                onPreviousPage = { viewModel.onAction(ReaderAction.PreviousPage) },
                                onTogglePageNavigator = { viewModel.onAction(ReaderAction.TogglePageNavigator) },
                                onToggleImmersiveMode = { viewModel.onAction(ReaderAction.ToggleToolbarCollapse) },
                                onAnnotationCreated = { viewModel.onAnnotationCreated(it) },
                                onOpenStickyNote = { viewModel.onAction(ReaderAction.OpenStickyNoteDialog(it)) },
                                onNewStickyNote = { viewModel.onAction(ReaderAction.OpenStickyNoteDialog(null, it)) },
                                onOpenTextBox = { viewModel.onAction(ReaderAction.OpenTextBoxDialog(it)) },
                                onNewTextBox = { viewModel.onAction(ReaderAction.OpenTextBoxDialog(null, it)) },
                                onOpenStamp = { viewModel.onAction(ReaderAction.OpenStampDialog(it)) },
                                onMoveAnnotation = { id, point -> viewModel.onAction(ReaderAction.MoveAnnotation(id, point)) },
                                onPerformUndo = { viewModel.onAction(ReaderAction.Undo) },
                                onPerformRedo = { viewModel.onAction(ReaderAction.Redo) },
                                onCycleColor = {
                                    if (state.recentColors.isNotEmpty()) {
                                        val nextColor = state.recentColors.first()
                                        viewModel.onAction(ReaderAction.SelectColor(nextColor))
                                    }
                                },
                                onToggleSelectMode = { viewModel.onAction(ReaderAction.ToggleSelectTextMode) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                // Banner when Text Selection mode is active in Reading mode
                if (state.isSelectTextMode && state.mode != ReaderMode.INK) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                            .padding(top = 58.dp)
                            .zIndex(20f),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                        border = BorderStroke(1.dp, BorderSubtle),
                        shadowElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.HighlightAlt,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Arrastra sobre el texto para seleccionarlo y explicar con IA",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Layer: Floating Page Status Badge (Shown in Immersive/Collapsed mode)
                AnimatedVisibility(
                    visible = state.isToolbarCollapsed && state.mode == ReaderMode.READING && !state.isSearchVisible,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(top = 12.dp, end = 16.dp)
                ) {
                    PageStatusBadge(
                        currentPage = state.currentPage,
                        pageCount = state.pageCount,
                        viewMode = state.viewMode
                    )
                }

                // Layer 4: Top Toolbars
                if (state.isSearchVisible) {
                    SearchOverlay(
                        query = state.searchQuery,
                        matchCount = state.searchMatchCount,
                        currentMatchIndex = state.currentSearchMatchIndex,
                        isSearching = state.isSearching,
                        isDarkMode = state.isDarkMode,
                        onQueryChanged = { viewModel.onAction(ReaderAction.UpdateSearchQuery(it)) },
                        onNextMatch = { viewModel.onAction(ReaderAction.NextSearchMatch) },
                        onPreviousMatch = { viewModel.onAction(ReaderAction.PreviousSearchMatch) },
                        onClose = { viewModel.onAction(ReaderAction.ToggleSearch) },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .zIndex(10f)
                    )
                } else {
                    AnimatedVisibility(
                        visible = !state.isToolbarCollapsed,
                        enter = fadeIn() + slideInVertically { -it },
                        exit = fadeOut() + slideOutVertically { -it },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .zIndex(10f)
                    ) {
                        ReaderToolbar(
                            state = state,
                            onAction = { viewModel.onAction(it) }
                        )
                    }
                }

                // Layer 4: Floating Page Navigator (Bottom)
                PageNavigator(
                    visible = state.showPageNavigator,
                    currentPage = state.currentPage,
                    pageCount = state.pageCount,
                    isDarkMode = state.isDarkMode,
                    onPageSelected = { viewModel.onAction(ReaderAction.GoToPage(it)) },
                    onToggleOutline = { viewModel.onAction(ReaderAction.ToggleOutline) },
                    onTogglePageGrid = { viewModel.onAction(ReaderAction.TogglePageGrid) },
                    onClose = { viewModel.onAction(ReaderAction.SetPageNavigatorVisible(false)) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .zIndex(10f)
                )

                // Layer 5: Popovers

                if (state.showColorPicker) {
                    ColorPickerPopover(
                        tool = state.activeInkTool,
                        selectedColor = if (state.activeInkTool == com.pablo.paper.domain.model.InkTool.HIGHLIGHTER || state.activeInkTool == com.pablo.paper.domain.model.InkTool.TEXT_HIGHLIGHT) {
                            state.selectedHighlighterColor
                        } else {
                            state.selectedColor
                        },
                        recentColors = state.recentColors,
                        onColorSelected = { viewModel.onAction(ReaderAction.SelectColor(it)) },
                        onDismissRequest = { viewModel.onAction(ReaderAction.SetColorPickerVisible(false)) }
                    )
                }

                if (state.showStrokeWidthPicker) {
                    StrokeWidthPopover(
                        tool = state.activeInkTool,
                        currentWidth = state.selectedStrokeWidth,
                        currentColor = if (state.activeInkTool == com.pablo.paper.domain.model.InkTool.HIGHLIGHTER || state.activeInkTool == com.pablo.paper.domain.model.InkTool.TEXT_HIGHLIGHT) {
                            state.selectedHighlighterColor
                        } else {
                            state.selectedColor
                        },
                        onWidthSelected = { viewModel.onAction(ReaderAction.SetStrokeWidth(it)) },
                        onDismissRequest = { viewModel.onAction(ReaderAction.SetStrokeWidthPickerVisible(false)) }
                    )
                }

                val selectedText by viewModel.inkController.selectedText.collectAsState()

                // Layer 5: Text Selection Action Menu
                TextSelectionActionMenu(
                    selectedText = selectedText,
                    isDarkMode = state.isDarkMode,
                    onExplainWithAi = { viewModel.onAction(ReaderAction.ExplainSelectedTextWithAi(it)) },
                    onSummarizeWithAi = { viewModel.onAction(ReaderAction.SummarizeSelectedTextWithAi(it)) },
                    onTranslateWithAi = { viewModel.onAction(ReaderAction.TranslateSelectedTextWithAi(it)) },
                    onAddToNotes = { viewModel.onAction(ReaderAction.AppendNoteToMarkdown(it)) },
                    onHighlight = { viewModel.onAction(ReaderAction.HighlightSelectedText(it)) },
                    onUnderline = { viewModel.onAction(ReaderAction.UnderlineSelectedText(it)) },
                    onDismiss = { viewModel.onAction(ReaderAction.ClearTextSelection) },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 80.dp)
                        .zIndex(25f)
                )

                // Layer 6: Side Panels (Markdown & Assistant)
                SidePanels(
                    activePanel = state.activePanel,
                    document = state.document,
                    currentPage = state.currentPage,
                    pageCount = state.pageCount,
                    notesText = state.documentNotes,
                    assistantMessages = state.assistantMessages,
                    isAssistantLoading = state.isAssistantLoading,
                    aiProvider = state.aiProvider,
                    selectedAiModel = state.selectedAiModel,
                    openRouterApiKey = state.openRouterApiKey,
                    isApiKeyDialogOpen = state.isApiKeyDialogOpen,
                    isDarkMode = state.isDarkMode,
                    onNotesChanged = { viewModel.onAction(ReaderAction.UpdateDocumentNotes(it)) },
                    onExtractAnnotations = { viewModel.onAction(ReaderAction.ExtractAnnotationsToMarkdown) },
                    onSendAssistantMessage = { viewModel.onAction(ReaderAction.SendAssistantMessage(it)) },
                    onExplainHighlights = { viewModel.onAction(ReaderAction.ExplainHighlightsWithAi) },
                    onSelectAiProvider = { viewModel.onAction(ReaderAction.SelectAiProvider(it)) },
                    onSelectAiModel = { viewModel.onAction(ReaderAction.SelectAiModel(it)) },
                    onSetOpenRouterApiKey = { viewModel.onAction(ReaderAction.SetOpenRouterApiKey(it)) },
                    onSetApiKeyDialogOpen = { viewModel.onAction(ReaderAction.SetApiKeyDialogOpen(it)) },
                    onClearAssistantChat = { viewModel.onAction(ReaderAction.ClearAssistantChat) },
                    onAppendNoteToMarkdown = { viewModel.onAction(ReaderAction.AppendNoteToMarkdown(it)) },
                    onClose = { viewModel.onAction(ReaderAction.TogglePanel(ReaderPanel.None)) },
                    modifier = Modifier.align(Alignment.TopEnd)
                )

                // Layer 6: Document Info Dialog
                if (state.isDocInfoVisible) {
                    DocumentInfoDialog(
                        document = state.document,
                        pageCount = state.pageCount,
                        currentPage = state.currentPage,
                        isDarkMode = state.isDarkMode,
                        onDismissRequest = { viewModel.onAction(ReaderAction.ToggleDocInfo) }
                    )
                }

                // Layer 7: Outline / Table of Contents Modal
                if (state.isOutlineVisible) {
                    OutlineModal(
                        document = state.document,
                        currentPage = state.currentPage,
                        pageCount = state.pageCount,
                        entries = state.outlineEntries,
                        isLoading = state.isOutlineLoading,
                        isDarkMode = state.isDarkMode,
                        onPageSelected = { viewModel.onAction(ReaderAction.GoToPage(it)) },
                        onRefreshOutline = { viewModel.onAction(ReaderAction.LoadOutline) },
                        onExtractWithAi = { viewModel.onAction(ReaderAction.ExtractOutlineWithAi) },
                        onDismissRequest = { viewModel.onAction(ReaderAction.ToggleOutline) }
                    )
                }

                // Layer 8: Page Thumbnail Grid Modal
                if (state.isPageGridVisible) {
                    PageGridModal(
                        currentPage = state.currentPage,
                        pageCount = state.pageCount,
                        isDarkMode = state.isDarkMode,
                        onPageSelected = { viewModel.onAction(ReaderAction.GoToPage(it)) },
                        onDismissRequest = { viewModel.onAction(ReaderAction.TogglePageGrid) }
                    )
                }

                // Layer 9: Acrobat Page Organizer Modal
                if (state.isPageOrganizerVisible) {
                    PageOrganizerModal(
                        currentPage = state.currentPage,
                        pageCount = state.pageCount,
                        pageRotations = state.pageRotations,
                        isDarkMode = state.isDarkMode,
                        onPageSelected = { viewModel.onAction(ReaderAction.GoToPage(it)) },
                        onRotatePage = { viewModel.onAction(ReaderAction.RotatePage(it)) },
                        onMovePageUp = { viewModel.onAction(ReaderAction.MovePageUp(it)) },
                        onMovePageDown = { viewModel.onAction(ReaderAction.MovePageDown(it)) },
                        onDeletePage = { viewModel.onAction(ReaderAction.DeletePage(it)) },
                        onInsertBlankPage = { viewModel.onAction(ReaderAction.InsertBlankPage(it)) },
                        onDismissRequest = { viewModel.onAction(ReaderAction.TogglePageOrganizer) }
                    )
                }

                // Layer 10: Bookmarks Modal
                if (state.isBookmarksModalVisible) {
                    BookmarksModal(
                        bookmarkedPages = state.bookmarkedPages,
                        currentPage = state.currentPage,
                        isDarkMode = state.isDarkMode,
                        onPageSelected = { viewModel.onAction(ReaderAction.GoToPage(it)) },
                        onToggleBookmark = { viewModel.onAction(ReaderAction.ToggleBookmark(it)) },
                        onDismissRequest = { viewModel.onAction(ReaderAction.ToggleBookmarksModal) }
                    )
                }

                // Layer 11: Signature Dialog
                if (state.isSignatureDialogOpen) {
                    SignatureDialog(
                        isDarkMode = state.isDarkMode,
                        onSignatureConfirmed = { viewModel.onAction(ReaderAction.ConfirmSignature(it)) },
                        onDismissRequest = { viewModel.onAction(ReaderAction.CloseSignatureDialog) }
                    )
                }

                // Layer 12: Sticky Note Comment Dialog
                if (state.isStickyNoteDialogOpen) {
                    StickyNoteDialog(
                        initialText = state.activeStickyNote?.textContent ?: "",
                        pageNumber = state.currentPage,
                        isDarkMode = state.isDarkMode,
                        onSave = { viewModel.onAction(ReaderAction.SaveStickyNote(it)) },
                        onDelete = state.activeStickyNote?.let { ann -> { viewModel.onAction(ReaderAction.DeleteStickyNote(ann.id)) } },
                        onDismissRequest = { viewModel.onAction(ReaderAction.CloseStickyNoteDialog) }
                    )
                }

                // Layer 13: Acrobat Text Box Dialog
                if (state.isTextBoxDialogOpen) {
                    TextBoxDialog(
                        initialText = state.activeTextBox?.textContent ?: "",
                        initialColor = state.activeTextBox?.stroke?.color ?: state.selectedColor,
                        initialFontSize = state.activeTextBox?.stroke?.width ?: 16f,
                        pageNumber = state.currentPage,
                        isDarkMode = state.isDarkMode,
                        onSave = { text, color, fontSize ->
                            viewModel.onAction(ReaderAction.SaveTextBox(text, color, fontSize))
                        },
                        onDelete = state.activeTextBox?.let { ann -> { viewModel.onAction(ReaderAction.DeleteTextBox(ann.id)) } },
                        onDismissRequest = { viewModel.onAction(ReaderAction.CloseTextBoxDialog) }
                    )
                }

                // Layer 14: Acrobat Stamp Picker Dialog
                if (state.isStampDialogOpen) {
                    StampDialog(
                        pageNumber = state.currentPage,
                        isDarkMode = state.isDarkMode,
                        onStampSelected = { text, color ->
                            viewModel.onAction(ReaderAction.ApplyStamp(text, color))
                        },
                        onDismissRequest = { viewModel.onAction(ReaderAction.CloseStampDialog) }
                    )
                }

                // Layer 15: Clear Page Annotations Confirmation Dialog
                if (state.isClearPageDialogOpen) {
                    ClearPageDialog(
                        pageNumber = state.currentPage,
                        isDarkMode = state.isDarkMode,
                        onConfirm = { viewModel.onAction(ReaderAction.ConfirmClearPageAnnotations) },
                        onDismissRequest = { viewModel.onAction(ReaderAction.CloseClearPageDialog) }
                    )
                }

                // Layer 16: Stylus Button Settings Dialog
                if (state.isStylusSettingsOpen) {
                    StylusSettingsDialog(
                        primaryAction = state.stylusPrimaryAction,
                        secondaryAction = state.stylusSecondaryAction,
                        isDarkMode = state.isDarkMode,
                        onPrimaryActionChanged = { viewModel.onAction(ReaderAction.SetStylusPrimaryAction(it)) },
                        onSecondaryActionChanged = { viewModel.onAction(ReaderAction.SetStylusSecondaryAction(it)) },
                        onDismissRequest = { viewModel.onAction(ReaderAction.CloseStylusSettingsDialog) }
                    )
                }

                // Layer 17: Paper Color & Texture Customizer Dialog
                if (state.isPaperCustomizerOpen) {
                    PaperCustomizerDialog(
                        selectedPaperColor = state.paperColor,
                        selectedPaperTexture = state.paperTexture,
                        selectedPaperTexturePoints = state.paperTexturePoints,
                        isSeamlessCanvas = state.isSeamlessCanvas,
                        isDarkMode = state.isDarkMode,
                        onSelectPaperColor = { viewModel.onAction(ReaderAction.SelectPaperColor(it)) },
                        onSelectPaperTexture = { viewModel.onAction(ReaderAction.SelectPaperTexture(it)) },
                        onSelectPaperTexturePoints = { viewModel.onAction(ReaderAction.SelectPaperTexturePoints(it)) },
                        onToggleSeamlessCanvas = { viewModel.onAction(ReaderAction.ToggleSeamlessCanvas) },
                        onDismiss = { viewModel.onAction(ReaderAction.SetPaperCustomizerOpen(false)) }
                    )
                }

                // Layer 18: Full Page OCR Text Extraction Dialog
                if (state.isOcrPageDialogOpen) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    OcrPageDialog(
                        currentPage = state.currentPage,
                        pageCount = state.pageCount,
                        ocrText = state.ocrPageText,
                        isLoading = state.isOcrPageLoading,
                        isDarkMode = state.isDarkMode,
                        onCopyToClipboard = { text ->
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Texto OCR", text)
                            clipboard.setPrimaryClip(clip)
                            android.widget.Toast.makeText(context, "Texto copiado al portapapeles", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        onExplainWithAi = { text ->
                            viewModel.onAction(ReaderAction.ExplainSelectedTextWithAi(text))
                        },
                        onSummarizeWithAi = { text ->
                            viewModel.onAction(ReaderAction.SummarizeSelectedTextWithAi(text))
                        },
                        onTranslateWithAi = { text ->
                            viewModel.onAction(ReaderAction.TranslateSelectedTextWithAi(text))
                        },
                        onAddToNotes = { text ->
                            viewModel.onAction(ReaderAction.AppendNoteToMarkdown(text))
                            android.widget.Toast.makeText(context, "Texto añadido a Notas", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        onDismissRequest = { viewModel.onAction(ReaderAction.CloseOcrPageDialog) }
                    )
                }

                // Layer 19: Thumbnails Sidebar Drawer with Filter Chips
                ThumbnailsDrawer(
                    state = state,
                    onAction = { viewModel.onAction(it) },
                    onRenderThumbnail = { pIdx, w, h -> viewModel.renderSpecificPage(pIdx, w, h) },
                    getPageSizeForPage = { pIdx -> viewModel.getPageSizeForPage(pIdx) }
                )

                // Layer 20: Visual Page Organizer Dialog
                if (state.isPageOrganizerVisible) {
                    PageOrganizerDialog(
                        state = state,
                        onAction = { viewModel.onAction(it) },
                        onRenderThumbnail = { pIdx, w, h -> viewModel.renderSpecificPage(pIdx, w, h) },
                        getPageSizeForPage = { pIdx -> viewModel.getPageSizeForPage(pIdx) },
                        onRebuildComplete = {
                            viewModel.onAction(ReaderAction.GoToPage(1))
                        }
                    )
                }

                // Layer 21: Acrobat Pro PDF Export Dialog
                if (state.isPdfExportDialogOpen) {
                    PdfExportDialog(
                        state = state,
                        onAction = { viewModel.onAction(it) }
                    )
                }
            }
        }
    }
}
