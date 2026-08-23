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
    GOOGLE_GEMINI(
        displayName = "Google Gemini (Recomendado)",
        shortName = "Google Gemini",
        endpointUrl = "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions",
        defaultModel = "gemini-2.0-flash",
        keyHint = "AIzaSy...",
        description = "1.000.000 tokens/minuto y 1.500 peticiones diarias 100% gratis en Google AI Studio (aistudio.google.com).",
        helpUrl = "https://aistudio.google.com/app/apikey"
    ),
    NVIDIA_NIM(
        displayName = "NVIDIA NIM (DeepSeek-R1, Llama 3.3)",
        shortName = "NVIDIA NIM",
        endpointUrl = "https://integrate.api.nvidia.com/v1/chat/completions",
        defaultModel = "deepseek-ai/deepseek-r1",
        keyHint = "nvapi-...",
        description = "1.000 créditos gratis por cuenta para DeepSeek-R1 (671B), Llama 3.3 70B y Mistral Large en build.nvidia.com.",
        helpUrl = "https://build.nvidia.com"
    ),
    OPENROUTER(
        displayName = "OpenRouter (Dots3-Note, Modelos Libres)",
        shortName = "OpenRouter",
        endpointUrl = "https://openrouter.ai/api/v1/chat/completions",
        defaultModel = "dots-studio/dots-3-note-preview:free",
        keyHint = "sk-or-v1-...",
        description = "Router de modelos abiertos con opciones gratuitas en openrouter.ai.",
        helpUrl = "https://openrouter.ai/keys"
    ),
    HOMELAB_TAILSCALE(
        displayName = "Homelab Proxy (Auto / Free Claude Code)",
        shortName = "Homelab",
        endpointUrl = "http://100.94.0.92:8082/v1/chat/completions",
        defaultModel = "auto",
        keyHint = "homelab_paper_2026 (opcional)",
        description = "Proxy en 100.94.0.92 vía Tailscale. Se sincroniza automáticamente con cualquier modelo del servidor.",
        helpUrl = "https://github.com/Alishahryar1/free-claude-code"
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
    val GEMINI_MODELS = listOf(
        AiModelInfo(
            id = "gemini-2.0-flash",
            name = "Gemini 2.0 Flash (Recomendado · 1M Tokens)",
            provider = "Google AI Studio",
            description = "Velocidad instantánea, 1M tokens de contexto, esquemas UML y fórmulas matemáticas"
        ),
        AiModelInfo(
            id = "gemini-2.0-flash-thinking-exp-01-21",
            name = "Gemini 2.0 Flash Thinking (Razonamiento Profundo)",
            provider = "Google AI Studio",
            description = "Pensamiento paso a paso, análisis lógico exhaustivo y resolución de problemas"
        ),
        AiModelInfo(
            id = "gemini-1.5-pro",
            name = "Gemini 1.5 Pro (2M Tokens de Contexto)",
            provider = "Google AI Studio",
            description = "Capacidad masiva de análisis para libros completos de cientos de páginas"
        ),
        AiModelInfo(
            id = "gemini-1.5-flash",
            name = "Gemini 1.5 Flash (Alta Eficiencia)",
            provider = "Google AI Studio",
            description = "Resúmenes rápidos, traducción y consultas directas"
        )
    )

    val NVIDIA_MODELS = listOf(
        AiModelInfo(
            id = "deepseek-ai/deepseek-r1",
            name = "DeepSeek R1 (671B Razonamiento · Top Mundial)",
            provider = "NVIDIA NIM",
            description = "Modelo líder en razonamiento matemático, fórmulas LaTeX, código y deducción lógica"
        ),
        AiModelInfo(
            id = "meta/llama-3.3-70b-instruct",
            name = "Meta Llama 3.3 70B Instruct",
            provider = "NVIDIA NIM",
            description = "128k contexto, redacción en español excepcional y comprensión estructural"
        ),
        AiModelInfo(
            id = "mistralai/mistral-large-2-instruct",
            name = "Mistral Large 2 (123B)",
            provider = "NVIDIA NIM",
            description = "Gran capacidad multilingüe, rigor analítico y síntesis precisa"
        ),
        AiModelInfo(
            id = "qwen/qwen2.5-coder-32b-instruct",
            name = "Qwen 2.5 Coder 32B",
            provider = "NVIDIA NIM",
            description = "Especializado en algoritmos, esquemas UML y documentación técnica"
        )
    )

    val HOMELAB_MODELS = listOf(
        AiModelInfo(
            id = "auto",
            name = "⚡ Auto (Cualquiera disponible en el proxy)",
            provider = "Homelab Proxy",
            description = "Utiliza automáticamente cualquier modelo disponible en el servidor sin tener que configurar ni elegir nada"
        ),
        AiModelInfo(
            id = "claude-3-7-sonnet",
            name = "Claude 3.7 Sonnet (Máxima Inteligencia)",
            provider = "Homelab Proxy",
            description = "Modelo líder en razonamiento complejo, análisis de documentos y generación estructurada"
        ),
        AiModelInfo(
            id = "claude-3-5-sonnet",
            name = "Claude 3.5 Sonnet (Rápido y Preciso)",
            provider = "Homelab Proxy",
            description = "Alta velocidad y síntesis brillante con pool de tokens libre"
        ),
        AiModelInfo(
            id = "claude-3-opus",
            name = "Claude 3 Opus",
            provider = "Homelab Proxy",
            description = "Profundidad máxima para libros y textos académicos extensos"
        )
    )

    val OPENROUTER_FREE_MODELS = listOf(
        AiModelInfo(
            id = "dots-studio/dots-3-note-preview:free",
            name = "Dots Studio: Dots3-Note (Rápido)",
            provider = "Dots Studio",
            description = "El más rápido en OpenRouter. Optimizado para análisis de documentos y notas"
        ),
        AiModelInfo(
            id = "deepseek/deepseek-r1:free",
            name = "DeepSeek: R1 Reasoning (Free)",
            provider = "DeepSeek",
            description = "Razonamiento paso a paso y análisis matemático"
        ),
        AiModelInfo(
            id = "meta-llama/llama-3.3-70b-instruct:free",
            name = "Meta: Llama 3.3 70B (Free)",
            provider = "Meta",
            description = "Capacidad líder para resúmenes complejos y comprensión (128k)"
        ),
        AiModelInfo(
            id = "google/gemini-2.0-flash-exp:free",
            name = "Google: Gemini 2.0 Flash (Free)",
            provider = "Google",
            description = "Modelo multimodal con gran ventana de contexto"
        ),
        AiModelInfo(
            id = "openrouter/free",
            name = "OpenRouter: Free Router (Auto)",
            provider = "OpenRouter",
            description = "Enruta automáticamente al mejor modelo gratuito disponible"
        )
    )

    val FREE_MODELS = GEMINI_MODELS + NVIDIA_MODELS + HOMELAB_MODELS + OPENROUTER_FREE_MODELS

    fun getModelsForProvider(provider: AiProvider): List<AiModelInfo> {
        return when (provider) {
            AiProvider.GOOGLE_GEMINI -> GEMINI_MODELS
            AiProvider.NVIDIA_NIM -> NVIDIA_MODELS
            AiProvider.OPENROUTER -> OPENROUTER_FREE_MODELS
            AiProvider.HOMELAB_TAILSCALE -> HOMELAB_MODELS
        }
    }

    const val DEFAULT_MODEL = "gemini-2.0-flash"
}
