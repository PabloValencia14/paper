package com.pablo.paper.ai

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.pablo.paper.domain.model.AssistantMessage
import com.pablo.paper.domain.model.MessageRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class OpenRouterClient(
    private val gson: Gson = Gson()
) {
    suspend fun fetchDynamicModels(
        provider: AiProvider,
        apiKey: String = ""
    ): List<AiModelInfo> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val modelsUrl = if (provider.endpointUrl.contains("/chat/completions")) {
                provider.endpointUrl.replace("/chat/completions", "/models")
            } else {
                "${provider.endpointUrl.trimEnd('/')}/models"
            }
            val url = URL(modelsUrl)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000
            connection.readTimeout = 10000
            val safeApiKey = apiKey.trim()
            if (safeApiKey.isNotEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer $safeApiKey")
            }
            if (connection.responseCode in 200..299) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = gson.fromJson(responseText, JsonObject::class.java)
                val data = json.getAsJsonArray("data")
                if (data != null && data.size() > 0) {
                    val list = mutableListOf<AiModelInfo>()
                    for (element in data) {
                        if (element.isJsonObject) {
                            val obj = element.asJsonObject
                            val id = obj.get("id")?.asString ?: continue
                            val name = obj.get("name")?.asString ?: id
                            val desc = obj.get("description")?.asString ?: "Disponible en el servidor proxy"
                            list.add(AiModelInfo(id = id, name = name, provider = provider.shortName, description = desc))
                        }
                    }
                    if (list.isNotEmpty()) return@withContext list
                }
            }
        } catch (e: Exception) {
            // Silently fallback
        } finally {
            connection?.disconnect()
        }
        emptyList()
    }

    suspend fun sendChat(
        apiKey: String,
        modelId: String,
        messages: List<AssistantMessage>,
        systemPrompt: String? = null,
        provider: AiProvider = AiProvider.GOOGLE_GEMINI
    ): Result<String> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(provider.endpointUrl)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 25000
            connection.readTimeout = 60000
            connection.doInput = true
            connection.doOutput = true

            // Headers
            val safeApiKey = apiKey.trim()
            if (safeApiKey.isNotEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer $safeApiKey")
            }
            connection.setRequestProperty("HTTP-Referer", "https://github.com/pablo/paper")
            connection.setRequestProperty("X-Title", "Paper PDF Reader")
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")

            // Auto-resolve model
            val resolvedModel = if (modelId.equals("auto", ignoreCase = true) || modelId.isBlank()) {
                if (provider == AiProvider.HOMELAB_TAILSCALE) {
                    "claude-3-7-sonnet"
                } else {
                    provider.defaultModel
                }
            } else {
                modelId
            }

            // Build JSON payload
            val root = JsonObject()
            root.addProperty("model", resolvedModel)

            val messagesArray = JsonArray()
            if (!systemPrompt.isNullOrEmpty()) {
                val sysObj = JsonObject()
                sysObj.addProperty("role", "system")
                sysObj.addProperty("content", systemPrompt)
                messagesArray.add(sysObj)
            }

            for (msg in messages) {
                val msgObj = JsonObject()
                msgObj.addProperty(
                    "role", when (msg.role) {
                        MessageRole.USER -> "user"
                        MessageRole.ASSISTANT -> "assistant"
                        MessageRole.SYSTEM -> "system"
                    }
                )
                msgObj.addProperty("content", msg.content)
                messagesArray.add(msgObj)
            }
            root.add("messages", messagesArray)

            // Write Body
            val writer = OutputStreamWriter(connection.outputStream, "UTF-8")
            writer.write(gson.toJson(root))
            writer.flush()
            writer.close()

            val responseCode = connection.responseCode
            val inputStream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream ?: connection.inputStream
            }

            val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            val responseText = reader.use { it.readText() }

            if (responseCode in 200..299) {
                val jsonResponse = gson.fromJson(responseText, JsonObject::class.java)
                val choices = jsonResponse.getAsJsonArray("choices")
                if (choices != null && choices.size() > 0) {
                    val choice = choices.get(0).asJsonObject
                    val message = choice.getAsJsonObject("message")
                    val content = message?.get("content")?.asString ?: ""
                    Result.success(content)
                } else {
                    Result.failure(Exception("Respuesta vacía del proveedor (${provider.shortName})"))
                }
            } else {
                val errorMsg = try {
                    val jsonResponse = gson.fromJson(responseText, JsonObject::class.java)
                    jsonResponse.getAsJsonObject("error")?.get("message")?.asString ?: responseText
                } catch (e: Exception) {
                    responseText
                }
                Result.failure(Exception("Error en ${provider.shortName} ($responseCode): $errorMsg"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        } finally {
            connection?.disconnect()
        }
    }
}

