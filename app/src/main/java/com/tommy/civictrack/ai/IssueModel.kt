package com.tommy.civictrack.ai

import com.google.firebase.Timestamp
import com.google.gson.annotations.SerializedName

data class IssueLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

data class IssueModel(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val priority: String = "Medium",
    val imageUrl: String = "",
    val location: IssueLocation = IssueLocation(),
    val timestamp: Timestamp = Timestamp.now()
)

data class IssueAIResponse(
    val title: String,
    val description: String,
    val category: String,
    val priority: String
)

data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String? = null,
    @SerializedName("inline_data")
    val inlineData: InlineData? = null
)

data class InlineData(
    @SerializedName("mime_type")
    val mimeType: String,
    val data: String
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

data class ImgBBResponse(
    val data: ImgBBData? = null,
    val success: Boolean = false,
    val status: Int = 0
)

data class ImgBBData(
    val url: String? = null,
    val display_url: String? = null
)

data class AutofillResult(
    val imageUrl: String,
    val aiResponse: IssueAIResponse
)

sealed class ReportUiState {
    data object Idle : ReportUiState()
    data object Loading : ReportUiState()
    data class AutofillSuccess(val result: AutofillResult) : ReportUiState()
    data class SubmitSuccess(val issueId: String) : ReportUiState()
    data class Error(val message: String) : ReportUiState()
}
