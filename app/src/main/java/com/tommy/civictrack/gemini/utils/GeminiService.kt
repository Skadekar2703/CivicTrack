package com.tommy.civictrack.gemini.utils

import com.tommy.civictrack.BuildConfig
import com.tommy.civictrack.gemini.Content
import com.tommy.civictrack.gemini.GeminiApi
import com.tommy.civictrack.gemini.GeminiRequest
import com.tommy.civictrack.gemini.Part
import com.tommy.civictrack.gemini.RetrofitClient

class GeminiService(
    private val api: GeminiApi = RetrofitClient.geminiApi
) {

    suspend fun analyzeImage(imageUrl: String): String {
        require(imageUrl.isNotBlank()) { "Image URL cannot be empty." }
        require(BuildConfig.GEMINI_API_KEY.isNotBlank()) {
            "Missing Gemini API key. Add gemini.api.key to local.properties or GEMINI_API_KEY to Gradle properties."
        }

        val request = GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(
                            text = PromptBuilder.buildIssuePrompt(imageUrl)
                        )
                    )
                )
            )
        )

        val response = api.generateContent(
            apiKey = BuildConfig.GEMINI_API_KEY,
            request = request
        )

        return response.candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.joinToString(separator = "\n") { it.text.orEmpty() }
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Gemini returned an empty response.")
    }
}
