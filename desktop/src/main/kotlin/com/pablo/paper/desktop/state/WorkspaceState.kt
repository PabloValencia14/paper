package com.pablo.paper.desktop.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pablo.paper.desktop.ai.DesktopAiClient
import com.pablo.paper.desktop.model.AiModelInfo
import com.pablo.paper.desktop.model.AiProvider
import com.pablo.paper.desktop.model.AssistantMessage
import com.pablo.paper.desktop.model.MessageRole
import com.pablo.paper.desktop.model.PaperAiDefaults
import com.pablo.paper.desktop.model.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.min

enum class LeftDockTab(val title: String) {
    THUMBNAILS("Páginas"),
    OUTLINE("Índice"),
    COMMENTS("Marcas")
}

enum class RightDockTab(val title: String) {
    AI_ASSISTANT("Asistente"),
    MARKDOWN_NOTES("Notas"),
    METADATA("Documento")
}

enum class DesktopDialog {
    NONE,
    PRINT,
    SEARCH_ADVANCED,
    PREFERENCES,
    ABOUT
}

enum class NoticeTone { INFO, SUCCESS, ERROR }

data class WorkspaceNotice(
    val text: String,
    val tone: NoticeTone = NoticeTone.INFO
)

private data class LoadedDocument(
    val pageCount: Int,
    val outline: List<com.pablo.paper.desktop.model.OutlineNode>,
    val metadata: com.pablo.paper.desktop.model.DocumentMetadata,
    val forms: List<com.pablo.paper.desktop.model.AcroFormField>,
    val session: DesktopSessionStore.Session?
)

class WorkspaceState(
    private val scope: CoroutineScope
) {
    val tabs = mutableStateListOf<TabDocumentState>()
    var activeTabIndex by mutableIntStateOf(-1)

    val activeTab: TabDocumentState?
        get() = tabs.getOrNull(activeTabIndex)

    // Keep the reader clear by default. Panels are deliberate, temporary tools.
    var isLeftDockOpen by mutableStateOf(false)
    var leftDockTab by mutableStateOf(LeftDockTab.THUMBNAILS)
    var isRightDockOpen by mutableStateOf(false)
    var rightDockTab by mutableStateOf(RightDockTab.AI_ASSISTANT)

    var themeMode by mutableStateOf(ThemeMode.SYSTEM)
    var activeDialog by mutableStateOf(DesktopDialog.NONE)
    var notice by mutableStateOf<WorkspaceNotice?>(null)

    // Same local, OpenAI-compatible route used by the tablet build.
    private val aiClient = DesktopAiClient()
    var aiProvider by mutableStateOf(AiProvider.HOMELAB)
    var aiEndpoint by mutableStateOf(AiProvider.HOMELAB.endpointUrl)
    var aiAccessToken by mutableStateOf("")
    var selectedModelId by mutableStateOf(AiProvider.HOMELAB.defaultModel)
    val availableModels = mutableStateListOf<AiModelInfo>().apply {
        addAll(PaperAiDefaults.MODELS)
    }
    val assistantMessages = mutableStateListOf<AssistantMessage>()
    var isAiThinking by mutableStateOf(false)

    fun showNotice(text: String, tone: NoticeTone = NoticeTone.INFO) {
        notice = WorkspaceNotice(text, tone)
    }

    fun dismissNotice() {
        notice = null
    }

    fun openDocument(file: File, password: String? = null) {
        if (!file.exists() || !file.isFile || !file.canRead()) {
            showNotice("No se puede leer el archivo seleccionado.", NoticeTone.ERROR)
            return
        }
        if (!file.extension.equals("pdf", ignoreCase = true)) {
            showNotice("Paper solo abre documentos PDF.", NoticeTone.ERROR)
            return
        }

        val existingIndex = tabs.indexOfFirst { it.file.absolutePath == file.absolutePath }
        if (existingIndex >= 0) {
            activeTabIndex = existingIndex
            return
        }

        val newTab = TabDocumentState(file)
        tabs.add(newTab)
        activeTabIndex = tabs.lastIndex

        scope.launch {
            val loaded = withContext(Dispatchers.IO) {
                if (!newTab.engine.open(file, password)) {
                    null
                } else {
                    LoadedDocument(
                        pageCount = newTab.engine.getPageCount(),
                        outline = newTab.engine.extractOutline(),
                        metadata = newTab.engine.extractMetadata(),
                        forms = newTab.engine.extractAcroForms(),
                        session = DesktopSessionStore.load(file).getOrNull()
                    )
                }
            }

            if (newTab !in tabs) {
                newTab.close()
                return@launch
            }

            newTab.isLoading = false
            if (loaded == null || loaded.pageCount <= 0) {
                newTab.loadError = "No se ha podido abrir este PDF. Si está protegido, todavía no admite solicitud de contraseña."
                newTab.isLoaded = false
                showNotice("No se pudo abrir ${file.name}.", NoticeTone.ERROR)
                return@launch
            }

            newTab.pageCount = loaded.pageCount
            newTab.outlineNodes.clear()
            newTab.outlineNodes.addAll(loaded.outline)
            newTab.metadata = loaded.metadata
            newTab.acroForms.clear()
            newTab.acroForms.addAll(loaded.forms)
            loaded.session?.let(newTab::applySession)
            newTab.isLoaded = true
            showNotice("${file.name} está listo.", NoticeTone.SUCCESS)
        }
    }

    fun closeTab(index: Int) {
        if (index !in tabs.indices) return
        val tab = tabs[index]
        saveTab(tab, showResult = false)
        tabs.removeAt(index)
        tab.close()

        activeTabIndex = when {
            tabs.isEmpty() -> -1
            index < activeTabIndex -> activeTabIndex - 1
            index == activeTabIndex -> min(index, tabs.lastIndex)
            else -> activeTabIndex
        }
    }

    fun saveActiveSession() {
        val tab = activeTab ?: run {
            showNotice("No hay un documento abierto que guardar.")
            return
        }
        saveTab(tab, showResult = true)
    }

    private fun saveTab(tab: TabDocumentState, showResult: Boolean) {
        scope.launch {
            val result = DesktopSessionStore.save(tab)
            result.fold(
                onSuccess = { sidecar ->
                    tab.isDirty = false
                    if (showResult) showNotice("Sesión guardada en ${sidecar.name}.", NoticeTone.SUCCESS)
                },
                onFailure = { error ->
                    if (showResult) showNotice("No se pudo guardar la sesión: ${error.message ?: "error de escritura"}.", NoticeTone.ERROR)
                }
            )
        }
    }

    fun refreshAiModels() {
        scope.launch {
            val modelsEndpoint = aiEndpoint.replace(Regex("/v1/chat/completions/?$"), "/v1/models")
            val models = aiClient.fetchModels(modelsEndpoint, aiAccessToken)
            if (models.isEmpty()) {
                showNotice("No se ha podido obtener la lista de modelos del proxy local.", NoticeTone.ERROR)
            } else {
                availableModels.clear()
                availableModels.addAll(models)
                if (availableModels.none { it.id == selectedModelId }) {
                    selectedModelId = availableModels.first().id
                }
                showNotice("Lista de modelos actualizada.", NoticeTone.SUCCESS)
            }
        }
    }

    fun sendAiMessage(prompt: String) {
        val cleanedPrompt = prompt.trim()
        if (cleanedPrompt.isBlank() || isAiThinking) return

        val currentTab = activeTab
        assistantMessages.add(AssistantMessage(role = MessageRole.USER, content = cleanedPrompt))
        isAiThinking = true

        scope.launch {
            val documentContext = withContext(Dispatchers.IO) {
                currentTab?.takeIf { it.isLoaded }?.let { tab ->
                    val pageText = tab.engine.extractText(tab.currentPage).take(8_000)
                    val documentExcerpt = tab.engine.extractAllText().take(12_000)
                    buildString {
                        appendLine("CONTEXTO DEL DOCUMENTO")
                        appendLine("Título: ${tab.title}")
                        appendLine("Página: ${tab.currentPage + 1} de ${tab.pageCount}")
                        appendLine("Texto de la página activa:")
                        appendLine(pageText)
                        if (documentExcerpt.isNotBlank()) {
                            appendLine()
                            appendLine("Extracto del documento:")
                            append(documentExcerpt)
                        }
                    }
                } ?: "No hay ningún documento PDF abierto."
            }

            val systemPrompt = """
                Eres el asistente de Paper, un lector local de PDFs.
                Responde con rigor, separa hechos del documento de inferencias y usa el idioma de la consulta.
                No inventes referencias, páginas ni contenido que no figure en el contexto.

                $documentContext
            """.trimIndent()

            val result = aiClient.sendChat(
                endpointUrl = aiEndpoint,
                accessToken = aiAccessToken,
                modelId = selectedModelId,
                messages = assistantMessages.toList(),
                systemPrompt = systemPrompt
            )

            isAiThinking = false
            result.fold(
                onSuccess = { (content, reasoning) ->
                    assistantMessages.add(
                        AssistantMessage(
                            role = MessageRole.ASSISTANT,
                            content = content,
                            modelUsed = selectedModelId,
                            reasoningText = reasoning
                        )
                    )
                },
                onFailure = { error ->
                    val message = DesktopAiClient.userFacingFailure(error, aiEndpoint)
                    assistantMessages.add(AssistantMessage(role = MessageRole.ASSISTANT, content = "Error de IA: $message"))
                    showNotice(message, NoticeTone.ERROR)
                }
            )
        }
    }

    fun clearChat() {
        assistantMessages.clear()
    }

    fun saveAllSessions(onComplete: () -> Unit) {
        val tabsToSave = tabs.toList()
        if (tabsToSave.isEmpty()) {
            onComplete()
            return
        }
        scope.launch {
            tabsToSave.forEach { tab ->
                DesktopSessionStore.save(tab).onSuccess { tab.isDirty = false }
            }
            onComplete()
        }
    }
}
