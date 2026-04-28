package com.tommy.civictrack.gemini

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.tommy.civictrack.gemini.utils.GeminiService

class GeminiRepository(
    private val geminiService: GeminiService = GeminiService(),
    private val gson: Gson = Gson()
) {
    suspend fun analyzeImage(imageUrl: String): IssueAIResponse {
        val rawResponse = geminiService.analyzeImage(imageUrl)
        val json = extractJson(rawResponse)
        val parsed = try {
            gson.fromJson(json, IssueAIResponse::class.java)
        } catch (error: JsonSyntaxException) {
            throw IllegalStateException("Invalid JSON returned by Gemini.")
        }
        validate(parsed)
        return parsed
    }

    private fun extractJson(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return trimmed
        }

        val match = Regex("\\{[\\s\\S]*}").find(raw)
        return match?.value ?: throw IllegalStateException("No JSON found in AI response.")
    }

    private fun validate(response: IssueAIResponse) {
        require(response.title.isNotBlank()) { "Title is empty." }
        require(response.description.isNotBlank()) { "Description is empty." }
        require(response.category in setOf("pothole", "garbage", "streetlight")) {
            "Invalid category."
        }
        require(response.priority in setOf("High", "Medium", "Low")) {
            "Invalid priority."
        }
    }
}
