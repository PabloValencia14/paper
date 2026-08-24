package com.pablo.paper.ui.library

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pablo.paper.data.repository.DocumentRepository
import com.pablo.paper.domain.model.Document
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface LibraryNavigationEvent {
    data class OpenReader(val documentId: String) : LibraryNavigationEvent
}

class LibraryViewModel(
    private val documentRepository: DocumentRepository,
    private val preferencesRepository: com.pablo.paper.data.repository.PreferencesRepository
) : ViewModel() {

    val documents: StateFlow<List<Document>> = documentRepository.getDocumentsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val documentCount: StateFlow<Int> = documentRepository.getDocumentCountFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val themeMode: StateFlow<com.pablo.paper.domain.model.AppThemeMode> = preferencesRepository.themeModeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = com.pablo.paper.domain.model.AppThemeMode.SYSTEM
        )

    val stylusPrimaryAction: StateFlow<com.pablo.paper.domain.model.StylusButtonAction> = preferencesRepository.stylusPrimaryButtonActionFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = com.pablo.paper.domain.model.StylusButtonAction.TEMPORARY_ERASER
        )

    val stylusSecondaryAction: StateFlow<com.pablo.paper.domain.model.StylusButtonAction> = preferencesRepository.stylusSecondaryButtonActionFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = com.pablo.paper.domain.model.StylusButtonAction.SWITCH_TO_HIGHLIGHTER
        )

    val openRouterApiKey: StateFlow<String> = preferencesRepository.openRouterApiKeyFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    val aiProvider: StateFlow<com.pablo.paper.ai.AiProvider> = preferencesRepository.aiProviderFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = com.pablo.paper.ai.AiProvider.OPENROUTER
        )

    val selectedAiModel: StateFlow<String> = preferencesRepository.selectedAiModelFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = com.pablo.paper.ai.OpenRouterModels.DEFAULT_MODEL
        )

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<LibraryNavigationEvent>()
    val navigationEvents: SharedFlow<LibraryNavigationEvent> = _navigationEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            try {
                documentRepository.syncDocumentsDirectory()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refreshDocuments() {
        viewModelScope.launch {
            try {
                documentRepository.syncDocumentsDirectory()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setSettingsOpen(open: Boolean) {
        _isSettingsOpen.value = open
    }

    fun onThemeModeChanged(mode: com.pablo.paper.domain.model.AppThemeMode) {
        viewModelScope.launch { preferencesRepository.saveThemeMode(mode) }
    }

    fun onStylusPrimaryActionChanged(action: com.pablo.paper.domain.model.StylusButtonAction) {
        viewModelScope.launch { preferencesRepository.saveStylusPrimaryAction(action) }
    }

    fun onStylusSecondaryActionChanged(action: com.pablo.paper.domain.model.StylusButtonAction) {
        viewModelScope.launch { preferencesRepository.saveStylusSecondaryAction(action) }
    }

    fun onOpenRouterApiKeyChanged(apiKey: String) {
        viewModelScope.launch { preferencesRepository.saveOpenRouterApiKey(apiKey) }
    }

    fun onAiProviderChanged(provider: com.pablo.paper.ai.AiProvider) {
        viewModelScope.launch {
            preferencesRepository.saveAiProvider(provider)
            preferencesRepository.saveSelectedAiModel(provider.defaultModel)
        }
    }

    fun onSelectedAiModelChanged(modelId: String) {
        viewModelScope.launch { preferencesRepository.saveSelectedAiModel(modelId) }
    }

    fun onImportUri(uri: Uri) {
        viewModelScope.launch {
            _isImporting.value = true
            val imported = documentRepository.importDocumentFromUri(uri)
            _isImporting.value = false
            if (imported != null) {
                _navigationEvents.emit(LibraryNavigationEvent.OpenReader(imported.id))
            }
        }
    }

    fun onDocumentClicked(document: Document) {
        viewModelScope.launch {
            _navigationEvents.emit(LibraryNavigationEvent.OpenReader(document.id))
        }
    }

    fun onDeleteDocument(documentId: String) {
        viewModelScope.launch {
            documentRepository.deleteDocument(documentId)
        }
    }

    class Factory(
        private val documentRepository: DocumentRepository,
        private val preferencesRepository: com.pablo.paper.data.repository.PreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LibraryViewModel(documentRepository, preferencesRepository) as T
        }
    }
}
