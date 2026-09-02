package com.pablo.paper.desktop.model

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

data class AssistantMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: String? = null,
    val reasoningText: String? = null
)

/**
 * Paper talks to the same OpenAI-compatible proxy used by the tablet app.
 * Keeping this as a provider, rather than baking an OpenRouter-branded client
 * into the UI, makes the network boundary explicit and avoids presenting
 * unverified third-party model catalogues as available choices.
 */
enum class AiProvider(
    val displayName: String,
    val endpointUrl: String,
    val modelsUrl: String,
    val defaultModel: String
) {
    HOMELAB(
        displayName = "IA local",
        endpointUrl = "http://100.94.0.92:8082/v1/chat/completions",
        modelsUrl = "http://100.94.0.92:8082/v1/models",
        defaultModel = "auto"
    )
}

data class AiModelInfo(
    val id: String,
    val name: String,
    val provider: String,
    val description: String = "",
    val contextLength: Int = 0,
    val isFree: Boolean = false
)

object PaperAiDefaults {
    /** A proxy can resolve this without us inventing a model identifier. */
    val MODELS = listOf(
        AiModelInfo(
            id = AiProvider.HOMELAB.defaultModel,
            name = "Automático",
            provider = AiProvider.HOMELAB.displayName,
            description = "Deja que el proxy local seleccione el modelo adecuado."
        )
    )
}
