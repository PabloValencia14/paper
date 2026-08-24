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
        provider: AiProvider = AiProvider.OPENROUTER,
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
                            val desc = obj.get("description")?.asString ?: "Modelo disponible en OpenRouter"
                            list.add(AiModelInfo(id = id, name = name, provider = provider.shortName, description = desc))
                        }
                    }
                    if (list.isNotEmpty()) return@withContext list
                }
            }
        } catch (e: Exception) {
            // Fallback
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
        provider: AiProvider = AiProvider.OPENROUTER
    ): Result<String> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(provider.endpointUrl)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 30000
            connection.readTimeout = 120000
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
            val resolvedModel = if (modelId.isBlank() || modelId.equals("auto", ignoreCase = true)) {
                provider.defaultModel
            } else {
                modelId
            }

            // Build JSON payload (OpenAI Chat Completions format supported by OpenRouter)
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
                val roleStr = when (msg.role) {
                    MessageRole.USER -> "user"
                    MessageRole.ASSISTANT -> "assistant"
                    MessageRole.SYSTEM -> "user"
                }
                msgObj.addProperty("role", roleStr)
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

                // OpenAI / OpenRouter response format (choices array)
                val choices = jsonResponse.getAsJsonArray("choices")
                if (choices != null && choices.size() > 0) {
                    val choice = choices.get(0).asJsonObject
                    val message = choice.getAsJsonObject("message")

                    var contentText = ""
                    var reasoningText = ""

                    // 1. Extract content safely without throwing on JsonNull or JsonArray
                    val contentElem = message?.get("content")
                    if (contentElem != null && !contentElem.isJsonNull) {
                        if (contentElem.isJsonPrimitive) {
                            contentText = contentElem.asString.trim()
                        } else if (contentElem.isJsonArray) {
                            val sb = StringBuilder()
                            for (elem in contentElem.asJsonArray) {
                                if (elem.isJsonObject) {
                                    val txt = elem.asJsonObject.get("text")?.asString
                                    if (txt != null) sb.append(txt)
                                } else if (elem.isJsonPrimitive) {
                                    sb.append(elem.asString)
                                }
                            }
                            contentText = sb.toString().trim()
                        }
                    }

                    // 2. Extract reasoning / thinking tokens (crucial for stealth/ox-alpha and reasoning models)
                    val reasoningElem = message?.get("reasoning")
                        ?: message?.get("reasoning_content")
                        ?: message?.get("thought")
                        ?: choice.get("reasoning")
                    if (reasoningElem != null && !reasoningElem.isJsonNull && reasoningElem.isJsonPrimitive) {
                        reasoningText = reasoningElem.asString.trim()
                    }

                    // 3. Fallback to choice.text (legacy completions)
                    if (contentText.isBlank() && reasoningText.isBlank()) {
                        val textElem = choice.get("text")
                        if (textElem != null && !textElem.isJsonNull && textElem.isJsonPrimitive) {
                            contentText = textElem.asString.trim()
                        }
                    }

                    // 4. Combine or select the best response
                    val finalOutput = when {
                        contentText.isNotBlank() && reasoningText.isNotBlank() -> {
                            if (contentText.length < 50 && reasoningText.length > 100) {
                                "$reasoningText\n\n$contentText"
                            } else {
                                contentText
                            }
                        }
                        contentText.isNotBlank() -> contentText
                        reasoningText.isNotBlank() -> reasoningText
                        else -> ""
                    }

                    if (finalOutput.isNotBlank()) {
                        return@withContext Result.success(finalOutput)
                    }
                }

                // Anthropic fallback if any
                val contentArray = jsonResponse.getAsJsonArray("content")
                if (contentArray != null && contentArray.size() > 0) {
                    val textBuilder = StringBuilder()
                    for (elem in contentArray) {
                        if (elem.isJsonObject) {
                            val text = elem.asJsonObject.get("text")?.asString
                            if (text != null) textBuilder.append(text)
                        }
                    }
                    val result = textBuilder.toString().trim()
                    if (result.isNotEmpty()) return@withContext Result.success(result)
                }

                Result.failure(Exception("Respuesta vacía de OpenRouter"))
            } else {
                val errorMsg = try {
                    val jsonResponse = gson.fromJson(responseText, JsonObject::class.java)
                    val errObj = jsonResponse.getAsJsonObject("error")
                    errObj?.get("message")?.asString 
                        ?: jsonResponse.get("detail")?.asString
                        ?: responseText
                } catch (e: Exception) {
                    responseText
                }
                val finalMessage = if (responseCode == 401) {
                    "Error 401: Clave API de OpenRouter no válida o no configurada. Pulsa el icono de la llave (🔑) para introducir tu clave de openrouter.ai/keys."
                } else {
                    "Error en OpenRouter ($responseCode): $errorMsg"
                }
                Result.failure(Exception(finalMessage))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        } finally {
            connection?.disconnect()
        }
    }
}
