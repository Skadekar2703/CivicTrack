package com.tommy.civictrack.ai

import android.content.ContentResolver
import android.net.Uri
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.tommy.civictrack.BuildConfig

class GeminiRepository(
    private val api: GeminiApi = GeminiClient.api,
    private val gson: Gson = Gson()
) {

    suspend fun analyzeImage(contentResolver: ContentResolver, uri: Uri): IssueAIResponse {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) {
            throw Exception("Missing GEMINI_API_KEY in local.properties")
        }

        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw Exception("Could not read image data")
        val base64Image = Base64.encodeToString(bytes, Base64.NO_WRAP)

        val request = GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = PromptBuilder.buildIssuePrompt()),
                        Part(inlineData = InlineData(
                            mimeType = contentResolver.getType(uri) ?: "image/jpeg",
                            data = base64Image
                        ))
                    )
                )
            )
        )

        val response = api.generateContent(
            apiKey = apiKey,
            request = request
        )

        val rawResponse = response.candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.joinToString(separator = "\n") { it.text.orEmpty() }
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Gemini returned an empty response.")

        val json = extractJson(rawResponse)
        return try {
            gson.fromJson(json, IssueAIResponse::class.java)
        } catch (error: JsonSyntaxException) {
            throw IllegalStateException("AI returned invalid data format.")
        }
    }

    private fun extractJson(rawResponse: String): String {
        // Find the first occurrence of '{' and the last occurrence of '}'
        val firstOpen = rawResponse.indexOf('{')
        val lastClose = rawResponse.lastIndexOf('}')
        
        if (firstOpen != -1 && lastClose != -1 && lastClose > firstOpen) {
            return rawResponse.substring(firstOpen, lastClose + 1)
        }
        
        // If no JSON object is found, return the trimmed response as a fallback
        return rawResponse.trim()
    }
}
