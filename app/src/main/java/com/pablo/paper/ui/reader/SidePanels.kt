package com.pablo.paper.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pablo.paper.domain.model.Document
import com.pablo.paper.domain.model.ReaderPanel
import com.pablo.paper.ui.common.LiquidGlassPanel

@Composable
fun SidePanels(
    activePanel: ReaderPanel,
    document: Document?,
    currentPage: Int,
    pageCount: Int,
    notesText: String,
    assistantMessages: List<com.pablo.paper.domain.model.AssistantMessage>,
    isAssistantLoading: Boolean,
    aiProvider: com.pablo.paper.ai.AiProvider = com.pablo.paper.ai.AiProvider.OPENROUTER,
    selectedAiModel: String,
    openRouterApiKey: String,
    isApiKeyDialogOpen: Boolean,
    isDarkMode: Boolean = false,
    onNotesChanged: (String) -> Unit,
    onExtractAnnotations: () -> Unit,
    onSendAssistantMessage: (String) -> Unit,
    onExplainHighlights: () -> Unit = {},
    onSelectAiProvider: (com.pablo.paper.ai.AiProvider) -> Unit = {},
    onSelectAiModel: (String) -> Unit,
    onSetOpenRouterApiKey: (String) -> Unit,
    onSetApiKeyDialogOpen: (Boolean) -> Unit,
    onClearAssistantChat: () -> Unit,
    onAppendNoteToMarkdown: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = activePanel != ReaderPanel.None,
        enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
        exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it }),
        modifier = modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(top = 68.dp, bottom = 16.dp, end = 20.dp)
    ) {
        LiquidGlassPanel(
            modifier = Modifier
                .width(480.dp)
                .fillMaxHeight(),
            shape = RoundedCornerShape(24.dp),
            isDarkMode = isDarkMode,
            elevation = 20.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                if (activePanel == ReaderPanel.Markdown) {
                    MarkdownPanel(
                        document = document,
                        notesText = notesText,
                        onNotesChanged = onNotesChanged,
                        onExtractAnnotations = onExtractAnnotations,
                        onClose = onClose
                    )
                } else if (activePanel == ReaderPanel.Assistant) {
                    AssistantPanel(
                        document = document,
                        currentPage = currentPage,
                        pageCount = pageCount,
                        messages = assistantMessages,
                        isLoading = isAssistantLoading,
                        aiProvider = aiProvider,
                        selectedModel = selectedAiModel,
                        apiKey = openRouterApiKey,
                        isApiKeyDialogOpen = isApiKeyDialogOpen,
                        isDarkMode = isDarkMode,
                        onSendMessage = onSendAssistantMessage,
                        onExplainHighlights = onExplainHighlights,
                        onSelectAiProvider = onSelectAiProvider,
                        onSelectModel = onSelectAiModel,
                        onSetApiKey = onSetOpenRouterApiKey,
                        onSetApiKeyDialogOpen = onSetApiKeyDialogOpen,
                        onClearChat = onClearAssistantChat,
                        onAppendNote = onAppendNoteToMarkdown,
                        onClose = onClose
                    )
                }
            }
        }
    }
}
