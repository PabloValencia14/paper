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

enum class AiProvider(
    val displayName: String,
    val shortName: String,
    val defaultModel: String,
    val endpointUrl: String
) {
    OPENROUTER(
        displayName = "OpenRouter AI",
        shortName = "OpenRouter",
        defaultModel = "stealth/ox-alpha",
        endpointUrl = "https://openrouter.ai/api/v1/chat/completions"
    )
}

data class AiModelInfo(
    val id: String,
    val name: String,
    val provider: String,
    val description: String = "",
    val contextLength: Int = 128000,
    val isFree: Boolean = false
)

object OpenRouterDefaults {
    val CURATED_MODELS = listOf(
        AiModelInfo(
            id = "stealth/ox-alpha",
            name = "Stealth: Ox Alpha",
            provider = "Stealth",
            description = "Razonamiento profundo ultra-preciso con soporte LaTeX y demostraciones matemáticas",
            contextLength = 131072,
            isFree = false
        ),
        AiModelInfo(
            id = "openrouter/free",
            name = "OpenRouter: Free Router (Auto)",
            provider = "OpenRouter",
            description = "Enrutador automático inteligente entre los mejores modelos gratuitos disponibles",
            contextLength = 1000000,
            isFree = true
        ),
        AiModelInfo(
            id = "nvidia/nemotron-3-ultra-550b-a55b:free",
            name = "NVIDIA: Nemotron 3 Ultra 550B (Free)",
            provider = "NVIDIA",
            description = "Modelo masivo de 550B de NVIDIA con 1M de tokens de contexto para PDFs extensos",
            contextLength = 1000000,
            isFree = true
        ),
        AiModelInfo(
            id = "nvidia/nemotron-3.5-lightning:free",
            name = "NVIDIA: Nemotron 3.5 Lightning (Free)",
            provider = "NVIDIA",
            description = "Respuestas ultra rápidas con 1M de tokens de contexto gratuito",
            contextLength = 1000000,
            isFree = true
        ),
        AiModelInfo(
            id = "nvidia/nemotron-3-nano-omni-30b-a3b-reasoning:free",
            name = "NVIDIA: Nemotron 3 Nano Omni Reasoning (Free)",
            provider = "NVIDIA",
            description = "Razonamiento matemático y lógica con cadena de pensamiento paso a paso",
            contextLength = 256000,
            isFree = true
        ),
        AiModelInfo(
            id = "nvidia/nemotron-3-super-120b-a12b:free",
            name = "NVIDIA: Nemotron 3 Super 120B (Free)",
            provider = "NVIDIA",
            description = "Excelente balance entre profundidad analítica y velocidad",
            contextLength = 262144,
            isFree = true
        ),
        AiModelInfo(
            id = "google/gemma-4-31b-it:free",
            name = "Google: Gemma 4 31B (Free)",
            provider = "Google",
            description = "Modelo avanzado de Google de última generación para comprensión de documentos",
            contextLength = 262144,
            isFree = true
        ),
        AiModelInfo(
            id = "google/gemma-4-26b-a4b-it:free",
            name = "Google: Gemma 4 26B (Free)",
            provider = "Google",
            description = "Modelo eficiente de Google optimizado para análisis de texto y síntesis",
            contextLength = 262144,
            isFree = true
        ),
        AiModelInfo(
            id = "thinkingmachines/inkling:free",
            name = "Thinking Machines: Inkling (Free)",
            provider = "Thinking Machines",
            description = "Especializado en lectura de textos densos y documentos académicos con 1M de contexto",
            contextLength = 1048576,
            isFree = true
        ),
        AiModelInfo(
            id = "thinkingmachines/inkling-small:free",
            name = "Thinking Machines: Inkling Small (Free)",
            provider = "Thinking Machines",
            description = "Versión ágil con 1M de contexto para resúmenes rápidos",
            contextLength = 1048576,
            isFree = true
        ),
        AiModelInfo(
            id = "minimax/minimax-m3:free",
            name = "MiniMax: MiniMax M3 (Free)",
            provider = "MiniMax",
            description = "Comprensión avanzada de documentos con 1M de tokens de contexto",
            contextLength = 1048576,
            isFree = true
        ),
        AiModelInfo(
            id = "poolside/laguna-s-2.1:free",
            name = "Poolside: Laguna S 2.1 (Free)",
            provider = "Poolside",
            description = "Especializado en código, estructuración y análisis formal de datos",
            contextLength = 262144,
            isFree = true
        ),
        AiModelInfo(
            id = "poolside/laguna-xs-2.1:free",
            name = "Poolside: Laguna XS 2.1 (Free)",
            provider = "Poolside",
            description = "Ultraligero y de respuesta instantánea para consultas puntuales",
            contextLength = 262144,
            isFree = true
        ),
        AiModelInfo(
            id = "z-ai/glm-5.2:free",
            name = "Z.ai: GLM 5.2 (Free)",
            provider = "Z.ai",
            description = "Modelo multilingüe de alta capacidad para extracción y resúmenes",
            contextLength = 256000,
            isFree = true
        ),
        AiModelInfo(
            id = "cohere/north-mini-code:free",
            name = "Cohere: North Mini Code (Free)",
            provider = "Cohere",
            description = "Especializado en código, Markdown, tablas y estructuras lógicas",
            contextLength = 256000,
            isFree = true
        ),
        AiModelInfo(
            id = "dots-studio/dots-3-note-preview:free",
            name = "Dots Studio: Dots3-Note Preview (Free)",
            provider = "Dots Studio",
            description = "Optimizado para toma de apuntes, extracción de notas y flashcards",
            contextLength = 512000,
            isFree = true
        ),
        AiModelInfo(
            id = "inclusionai/ling-3.0-flash-fin:free",
            name = "InclusionAI: Ling 3.0 Flash Fin (Free)",
            provider = "InclusionAI",
            description = "Especializado en tablas financieras, balances y métricas cuantitativas",
            contextLength = 262144,
            isFree = true
        )
    )
}
