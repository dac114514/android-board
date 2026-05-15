package com.faster.aiboard.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

private val json = Json { ignoreUnknownKeys = true }
private val client = OkHttpClient()
private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

@Serializable
data class DeepSeekMessage(
    val role: String,
    val content: List<ContentPart>
)

@Serializable
data class ContentPart(
    val type: String,
    val text: String? = null,
    val image_url: ImageUrl? = null
)

@Serializable
data class ImageUrl(val url: String)

@Serializable
data class DeepSeekRequest(
    val model: String = "deepseek-vl2",
    val messages: List<DeepSeekMessage>,
    val max_tokens: Int = 1024
)

@Serializable
data class DeepSeekResponse(
    val choices: List<Choice>? = null
)

@Serializable
data class Choice(
    val message: MessageContent? = null
)

@Serializable
data class MessageContent(
    val content: String? = null
)

class AIService(private val apiKey: String) {

    suspend fun analyzeImage(base64Image: String, question: String): String = withContext(Dispatchers.IO) {
        val requestBody = DeepSeekRequest(
            messages = listOf(
                DeepSeekMessage(
                    role = "user",
                    content = listOf(
                        ContentPart(type = "text", text = question),
                        ContentPart(type = "image_url", image_url = ImageUrl(url = "data:image/png;base64,$base64Image"))
                    )
                )
            )
        )

        val jsonBody = json.encodeToString(requestBody)
        val request = Request.Builder()
            .url("https://api.deepseek.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(jsonBody.toRequestBody(JSON_MEDIA))
            .build()

        try {
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext "API 返回为空"
            val apiResponse = json.decodeFromString<DeepSeekResponse>(body)
            apiResponse.choices?.firstOrNull()?.message?.content ?: "无法获取回复"
        } catch (e: Exception) {
            "请求失败: ${e.message}"
        }
    }
}
