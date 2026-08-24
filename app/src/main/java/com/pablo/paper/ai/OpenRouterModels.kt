package com.pablo.paper.ai

enum class AiProvider(
    val displayName: String,
    val shortName: String,
    val endpointUrl: String,
    val defaultModel: String,
    val keyHint: String,
    val description: String,
    val helpUrl: String
) {
    OPENROUTER(
        displayName = "OpenRouter",
        shortName = "OpenRouter",
        endpointUrl = "https://openrouter.ai/api/v1/chat/completions",
        defaultModel = "openrouter/free",
        keyHint = "sk-or-v1-...",
        description = "Router de modelos de IA de alto rendimiento y modelos libres en openrouter.ai.",
        helpUrl = "https://openrouter.ai/keys"
    )
}

data class AiModelInfo(
    val id: String,
    val name: String,
    val provider: String,
    val isFree: Boolean = true,
    val description: String = ""
)

object OpenRouterModels {

    val OPENROUTER_MODELS = listOf(
        AiModelInfo(
            id = "openrouter/free",
            name = "OpenRouter: Free Models Router (Auto)",
            provider = "OpenRouter",
            isFree = true,
            description = "Enruta automáticamente al mejor modelo gratuito disponible en OpenRouter con soporte para contexto amplio y herramientas."
        ),
        AiModelInfo(
            id = "stealth/ox-alpha",
            name = "Stealth: Ox Alpha",
            provider = "Stealth",
            isFree = false,
            description = "Modelo experimental avanzado Ox Alpha de alta precisión en OpenRouter."
        ),
        AiModelInfo(
            id = "nvidia/nemotron-3-ultra-550b-a55b:free",
            name = "NVIDIA: Nemotron-3 Ultra 550B (Free)",
            provider = "NVIDIA",
            isFree = true,
            description = "Modelo masivo de 550B parámetros optimizado para razonamiento profundo y comprensión técnica."
        ),
        AiModelInfo(
            id = "poolside/laguna-s-2.1:free",
            name = "Poolside: Laguna S 2.1 (Free)",
            provider = "Poolside",
            isFree = true,
            description = "Modelo de alto rendimiento de Poolside optimizado para análisis, síntesis y código."
        ),
        AiModelInfo(
            id = "nvidia/nemotron-3.5-lightning:free",
            name = "NVIDIA: Nemotron-3.5 Lightning (Free)",
            provider = "NVIDIA",
            isFree = true,
            description = "Modelo ultra rápido de NVIDIA con latencia mínima y gran precisión explicativa."
        ),
        AiModelInfo(
            id = "nvidia/nemotron-3-super-120b-a12b:free",
            name = "NVIDIA: Nemotron-3 Super 120B (Free)",
            provider = "NVIDIA",
            isFree = true,
            description = "120B parámetros con balance extraordinario de inteligencia, razonamiento y velocidad."
        ),
        AiModelInfo(
            id = "poolside/laguna-xs-2.1:free",
            name = "Poolside: Laguna XS 2.1 (Free)",
            provider = "Poolside",
            isFree = true,
            description = "Versión extra liviana de Laguna para consultas didácticas y respuestas instantáneas."
        )
    )

    val FREE_MODELS = OPENROUTER_MODELS

    fun getModelsForProvider(provider: AiProvider): List<AiModelInfo> {
        return OPENROUTER_MODELS
    }

    const val DEFAULT_MODEL = "openrouter/free"
}
