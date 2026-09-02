package com.pablo.paper.desktop.ai

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.pablo.paper.desktop.model.AiModelInfo
import com.pablo.paper.desktop.model.AiProvider
import com.pablo.paper.desktop.model.AssistantMessage
import com.pablo.paper.desktop.model.MessageRole
import com.pablo.paper.desktop.model.OpenRouterDefaults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class DesktopOpenRouterClient(
    private val gson: Gson = Gson()
) {
    suspend fun fetchDynamicModels(apiKey: String = ""): List<AiModelInfo> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL("https://openrouter.ai/api/v1/models")
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
                            val desc = obj.get("description")?.asString ?: "Modelo OpenRouter"
                            val context = obj.get("context_length")?.asInt ?: 128000
                            list.add(AiModelInfo(id = id, name = name, provider = "OpenRouter", description = desc, contextLength = context, isFree = id.contains(":free")))
                        }
                    }
                    if (list.isNotEmpty()) return@withContext list
                }
            }
        } catch (e: Exception) {
            // Fallback to curated defaults
        } finally {
            connection?.disconnect()
        }
        OpenRouterDefaults.CURATED_MODELS
    }

    suspend fun sendChat(
        apiKey: String,
        modelId: String,
        messages: List<AssistantMessage>,
        systemPrompt: String? = null
    ): Result<Pair<String, String?>> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(AiProvider.OPENROUTER.endpointUrl)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 30000
            connection.readTimeout = 120000
            connection.doInput = true
            connection.doOutput = true

            val safeApiKey = apiKey.trim()
            if (safeApiKey.isNotEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer $safeApiKey")
            }
            connection.setRequestProperty("HTTP-Referer", "https://github.com/pablo/paper")
            connection.setRequestProperty("X-Title", "Paper Desktop")
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")

            val resolvedModel = if (modelId.isBlank()) "stealth/ox-alpha" else modelId

            val root = JsonObject()
            root.addProperty("model", resolvedModel)

            val messagesArray = JsonArray()
            if (!systemPrompt.isNullOrBlank()) {
                val sysObj = JsonObject()
                sysObj.addProperty("role", "system")
                sysObj.addProperty("content", systemPrompt)
                messagesArray.add(sysObj)
            }

            for (msg in messages) {
                val msgObj = JsonObject()
                msgObj.addProperty(
                    "role",
                    when (msg.role) {
                        MessageRole.USER -> "user"
                        MessageRole.ASSISTANT -> "assistant"
                        MessageRole.SYSTEM -> "system"
                    }
                )
                msgObj.addProperty("content", msg.content)
                messagesArray.add(msgObj)
            }

            root.add("messages", messagesArray)

            val jsonBody = gson.toJson(root)
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(jsonBody)
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val responseText = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                val json = gson.fromJson(responseText, JsonObject::class.java)

                var contentText = ""
                var reasoningText: String? = null

                val choices = json.getAsJsonArray("choices")
                if (choices != null && choices.size() > 0) {
                    val firstChoice = choices[0].asJsonObject
                    val message = firstChoice.getAsJsonObject("message")
                    
                    val contentElem = message?.get("content")
                    if (contentElem != null && !contentElem.isJsonNull) {
                        if (contentElem.isJsonPrimitive) {
                            contentText = contentElem.asString.trim()
                        } else if (contentElem.isJsonArray) {
                            val sb = java.lang.StringBuilder()
                            for (part in contentElem.asJsonArray) {
                                if (part.isJsonObject && part.asJsonObject.has("text")) {
                                    sb.append(part.asJsonObject.get("text").asString)
                                }
                            }
                            contentText = sb.toString().trim()
                        }
                    }

                    val reasoningElem = message?.get("reasoning")
                        ?: message?.get("reasoning_content")
                        ?: message?.get("thought")
                    if (reasoningElem != null && !reasoningElem.isJsonNull && reasoningElem.isJsonPrimitive) {
                        reasoningText = reasoningElem.asString.trim()
                    }

                    if (contentText.isBlank()) {
                        val choiceTextElem = firstChoice.get("text")
                        if (choiceTextElem != null && !choiceTextElem.isJsonNull && choiceTextElem.isJsonPrimitive) {
                            contentText = choiceTextElem.asString.trim()
                        }
                    }
                }

                if (contentText.isBlank() && !reasoningText.isNullOrBlank()) {
                    contentText = reasoningText
                    reasoningText = null
                }

                if (contentText.isNotBlank()) {
                    Result.success(Pair(contentText, reasoningText))
                } else {
                    Result.failure(Exception("Respuesta vacía del modelo $resolvedModel"))
                }
            } else {
                val errorBody = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
                Result.failure(Exception("Error $responseCode en OpenRouter: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            connection?.disconnect()
        }
    }
}
