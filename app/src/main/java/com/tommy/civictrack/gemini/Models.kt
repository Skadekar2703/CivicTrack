package com.tommy.civictrack.gemini

data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GeminiResponse(
    val candidates: List<Candidate>? = null
)

data class Candidate(
    val content: ContentResponse? = null
)

data class ContentResponse(
    val parts: List<PartResponse>? = null
)

data class PartResponse(
    val text: String? = null
)

data class IssueAIResponse(
    val title: String,
    val description: String,
    val category: String,
    val priority: String
)

sealed class GeminiUiState {
    data object Idle : GeminiUiState()
    data object Loading : GeminiUiState()
    data class Success(
        val issue: IssueAIResponse,
        val rawResponse: String
    ) : GeminiUiState()
    data class Error(
        val message: String
    ) : GeminiUiState()
}
