package com.pablo.paper.desktop.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pablo.paper.desktop.ai.DesktopOpenRouterClient
import com.pablo.paper.desktop.model.AiModelInfo
import com.pablo.paper.desktop.model.AssistantMessage
import com.pablo.paper.desktop.model.MessageRole
import com.pablo.paper.desktop.model.OpenRouterDefaults
import com.pablo.paper.desktop.model.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

enum class LeftDockTab(val title: String) {
    THUMBNAILS("Miniaturas"),
    OUTLINE("Marcadores"),
    COMMENTS("Comentarios"),
    SIGNATURES("Firmas"),
    ATTACHMENTS("Adjuntos")
}

enum class RightDockTab(val title: String) {
    AI_ASSISTANT("Asistente IA"),
    MARKDOWN_NOTES("Notas"),
    FLASHCARDS("Estudio"),
    METADATA("Propiedades")
}

enum class DesktopDialog {
    NONE,
    PRINT,
    ORGANIZE_PAGES,
    WATERMARK,
    HEADER_FOOTER,
    PASSWORD_SECURITY,
    CERTIFICATE_SIGN,
    SEARCH_ADVANCED,
    PREFERENCES,
    ABOUT
}

class WorkspaceState(
    private val scope: CoroutineScope
) {
    val tabs = mutableStateListOf<TabDocumentState>()
    var activeTabIndex by mutableIntStateOf(-1)

    val activeTab: TabDocumentState?
        get() = if (activeTabIndex in 0 until tabs.size) tabs[activeTabIndex] else null

    // Docks
    var isLeftDockOpen by mutableStateOf(true)
    var leftDockTab by mutableStateOf(LeftDockTab.THUMBNAILS)

    var isRightDockOpen by mutableStateOf(true)
    var rightDockTab by mutableStateOf(RightDockTab.AI_ASSISTANT)

    // Appearance & Global Settings
    var themeMode by mutableStateOf(ThemeMode.SYSTEM)
    var activeDialog by mutableStateOf(DesktopDialog.NONE)


    // OpenRouter AI
    val openRouterClient = DesktopOpenRouterClient()
    var openRouterApiKey by mutableStateOf("")
    var selectedModelId by mutableStateOf("stealth/ox-alpha")
    val availableModels = mutableStateListOf<AiModelInfo>().apply {
        addAll(OpenRouterDefaults.CURATED_MODELS)
    }
    val assistantMessages = mutableStateListOf<AssistantMessage>()
    var isAiThinking by mutableStateOf(false)

    init {
        // Load dynamic models from OpenRouter
        scope.launch {
            val dynamic = openRouterClient.fetchDynamicModels(openRouterApiKey)
            if (dynamic.isNotEmpty()) {
                availableModels.clear()
                availableModels.addAll(dynamic)
            }
        }
    }

    fun openDocument(file: File, password: String? = null) {
        // Check if already open
        val existingIndex = tabs.indexOfFirst { it.file.absolutePath == file.absolutePath }
        if (existingIndex != -1) {
            activeTabIndex = existingIndex
            return
        }

        val newTab = TabDocumentState(file)
        tabs.add(newTab)
        activeTabIndex = tabs.size - 1

        scope.launch {
            val ok = newTab.engine.open(file, password)
            if (ok) {
                newTab.pageCount = newTab.engine.getPageCount()
                newTab.currentPage = 0
                newTab.outlineNodes.addAll(newTab.engine.extractOutline())
                newTab.metadata = newTab.engine.extractMetadata()
                newTab.acroForms.addAll(newTab.engine.extractAcroForms())
                newTab.isLoaded = true
            }
        }
    }

    fun closeTab(index: Int) {
        if (index !in 0 until tabs.size) return
        val tab = tabs.removeAt(index)
        tab.close()

        if (tabs.isEmpty()) {
            activeTabIndex = -1
        } else if (activeTabIndex >= tabs.size) {
            activeTabIndex = tabs.size - 1
        }
    }

    fun sendAiMessage(prompt: String) {
        if (prompt.isBlank() || isAiThinking) return
        val currentTab = activeTab

        val userMsg = AssistantMessage(role = MessageRole.USER, content = prompt)
        assistantMessages.add(userMsg)
        isAiThinking = true

        scope.launch {
            val docContext = if (currentTab != null && currentTab.isLoaded) {
                val fullText = currentTab.engine.extractAllText()
                val pageText = currentTab.engine.extractText(currentTab.currentPage)
                val total = currentTab.pageCount
                val pageNum = currentTab.currentPage + 1
                "--- CONTEXTO DEL DOCUMENTO ---\n" +
                "Título: ${currentTab.title}\n" +
                "Página activa: $pageNum de $total\n" +
                "Texto de la página activa:\n$pageText\n\n" +
                "Texto completo del documento (resumen/extracto):\n${fullText.take(16000)}\n" +
                "--- FIN CONTEXTO ---"
            } else {
                "No hay ningún documento PDF abierto en este momento."
            }

            val systemPrompt = """
                Eres el asistente de IA integrado en Paper Desktop para Windows, una suite profesional de lectura y análisis de PDFs.
                Responde de manera estructurada, clara y precisa en español (o en el idioma de la consulta).
                - Cuando expliques conceptos matemáticos, ecuaciones o fórmulas físicas/estadísticas, escribe siempre en notación LaTeX delimitada por $$...$$ para bloques de ecuación o $...$ para expresiones inline.
                - Cuando estructures comparativas o datos tabulares, utiliza tablas de Markdown con cabeceras y separadores de columnas.
                - Utiliza negritas, listas y bloques de código cuando sea apropiado.
                
                $docContext
            """.trimIndent()

            val result = openRouterClient.sendChat(
                apiKey = openRouterApiKey,
                modelId = selectedModelId,
                messages = assistantMessages.toList(),
                systemPrompt = systemPrompt
            )

            isAiThinking = false
            result.onSuccess { (content, reasoning) ->
                assistantMessages.add(
                    AssistantMessage(
                        role = MessageRole.ASSISTANT,
                        content = content,
                        modelUsed = selectedModelId,
                        reasoningText = reasoning
                    )
                )
            }.onFailure { err ->
                assistantMessages.add(
                    AssistantMessage(
                        role = MessageRole.ASSISTANT,
                        content = "⚠️ Error al consultar el modelo: ${err.message ?: "Error de conexión"}"
                    )
                )
            }
        }
    }

    fun clearChat() {
        assistantMessages.clear()
    }
}
