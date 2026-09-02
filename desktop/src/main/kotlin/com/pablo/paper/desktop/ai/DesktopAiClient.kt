package com.pablo.paper.desktop.ai

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.pablo.paper.desktop.model.AiModelInfo
import com.pablo.paper.desktop.model.AssistantMessage
import com.pablo.paper.desktop.model.MessageRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException

/** Small client for Paper's OpenAI-compatible local proxy. */
class DesktopAiClient(
    private val gson: Gson = Gson()
) {
    suspend fun fetchModels(
        endpointUrl: String,
        accessToken: String = ""
    ): List<AiModelInfo> = withContext(Dispatchers.IO) {
        val connection = (URL(endpointUrl).openConnection() as HttpURLConnection)
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 5_000
            connection.readTimeout = 8_000
            connection.setRequestProperty("Accept", "application/json")
            accessToken.trim().takeIf { it.isNotEmpty() }?.let {
                connection.setRequestProperty("Authorization", "Bearer $it")
            }

            if (connection.responseCode !in 200..299) return@withContext emptyList()
            val root = gson.fromJson(connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }, JsonObject::class.java)
            val data = root.getAsJsonArray("data") ?: return@withContext emptyList()

            data.mapNotNull { element ->
                element.takeIf { it.isJsonObject }?.asJsonObject?.let { item ->
                    item.get("id")?.asString?.takeIf { it.isNotBlank() }?.let { id ->
                        AiModelInfo(
                            id = id,
                            name = item.get("name")?.asString?.ifBlank { id } ?: id,
                            provider = "IA local",
                            description = item.get("description")?.asString ?: "",
                            contextLength = item.get("context_length")?.asInt ?: 0
                        )
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        } finally {
            connection.disconnect()
        }
    }

    suspend fun sendChat(
        endpointUrl: String,
        accessToken: String,
        modelId: String,
        messages: List<AssistantMessage>,
        systemPrompt: String? = null
    ): Result<Pair<String, String?>> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            connection = URL(endpointUrl).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 7_000
            connection.readTimeout = 90_000
            connection.doInput = true
            connection.doOutput = true
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            accessToken.trim().takeIf { it.isNotEmpty() }?.let {
                connection.setRequestProperty("Authorization", "Bearer $it")
            }

            val request = JsonObject().apply {
                addProperty("model", modelId.trim().ifBlank { "auto" })
                add("messages", JsonArray().apply {
                    systemPrompt?.takeIf { it.isNotBlank() }?.let { prompt ->
                        add(JsonObject().apply {
                            addProperty("role", "system")
                            addProperty("content", prompt)
                        })
                    }
                    messages.forEach { message ->
                        add(JsonObject().apply {
                            addProperty(
                                "role",
                                when (message.role) {
                                    MessageRole.USER -> "user"
                                    MessageRole.ASSISTANT -> "assistant"
                                    MessageRole.SYSTEM -> "system"
                                }
                            )
                            addProperty("content", message.content)
                        })
                    }
                })
            }

            connection.outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write(gson.toJson(request))
            }

            val code = connection.responseCode
            if (code !in 200..299) {
                val detail = connection.errorStream
                    ?.bufferedReader(Charsets.UTF_8)
                    ?.use { it.readText() }
                    ?.take(500)
                    ?.ifBlank { null }
                return@withContext Result.failure(IllegalStateException("El proxy respondió HTTP $code${detail?.let { ": $it" } ?: ""}"))
            }

            val root = gson.fromJson(connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }, JsonObject::class.java)
            val message = root.getAsJsonArray("choices")
                ?.firstOrNull()
                ?.takeIf { it.isJsonObject }
                ?.asJsonObject
                ?.getAsJsonObject("message")

            val content = message?.readContent().orEmpty().trim()
            val reasoning = message?.firstString("reasoning", "reasoning_content", "thought")?.trim()?.ifBlank { null }
            when {
                content.isNotBlank() -> Result.success(content to reasoning)
                !reasoning.isNullOrBlank() -> Result.success(reasoning to null)
                else -> Result.failure(IllegalStateException("El proxy devolvió una respuesta sin contenido."))
            }
        } catch (error: Exception) {
            Result.failure(error)
        } finally {
            connection?.disconnect()
        }
    }

    private fun JsonObject.readContent(): String? {
        val value = get("content") ?: return null
        if (value.isJsonPrimitive) return value.asString
        if (!value.isJsonArray) return null
        return value.asJsonArray.joinToString("") { part ->
            part.takeIf { it.isJsonObject }
                ?.asJsonObject
                ?.get("text")
                ?.takeIf { it.isJsonPrimitive }
                ?.asString
                .orEmpty()
        }
    }

    private fun JsonObject.firstString(vararg names: String): String? =
        names.firstNotNullOfOrNull { name ->
            get(name)?.takeIf { it.isJsonPrimitive }?.asString
        }

    companion object {
        fun userFacingFailure(error: Throwable, endpointUrl: String): String = when (error) {
            is SocketTimeoutException,
            is ConnectException,
            is UnknownHostException -> "No responde la IA local en $endpointUrl. Comprueba que el servicio esté iniciado y que el puerto 8082 sea accesible por Tailscale."
            else -> error.message ?: "No se pudo completar la consulta de IA."
        }
    }
}
