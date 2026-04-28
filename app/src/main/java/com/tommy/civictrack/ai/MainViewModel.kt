package com.tommy.civictrack.ai

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MainViewModel(
    private val imgBBRepository: ImgBBRepository = ImgBBRepository(),
    private val geminiRepository: GeminiRepository = GeminiRepository(),
    private val firebaseRepository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val _uiState = MutableLiveData<ReportUiState>(ReportUiState.Idle)
    val uiState: LiveData<ReportUiState> = _uiState

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _imageUrl = MutableLiveData<String>()
    val imageUrl: LiveData<String> = _imageUrl

    private val _aiResponse = MutableLiveData<IssueAIResponse>()
    val aiResponse: LiveData<IssueAIResponse> = _aiResponse

    fun processImage(contentResolver: ContentResolver, uri: Uri) {
        _loading.value = true
        _uiState.value = ReportUiState.Loading

        viewModelScope.launch {
            try {
                // Phase 1: Upload to ImgBB (for the final URL)
                val uploadedImageUrl = try {
                    imgBBRepository.uploadImage(contentResolver, uri)
                } catch (e: Exception) {
                    throw Exception("ImgBB Upload Failed: ${e.message}")
                }

                // Phase 2: Analyze with Gemini (sending image bits directly)
                val aiResult = try {
                    geminiRepository.analyzeImage(contentResolver, uri)
                } catch (e: Exception) {
                    throw Exception("Gemini AI Analysis Failed: ${e.message}")
                }

                _imageUrl.postValue(uploadedImageUrl)
                _aiResponse.postValue(aiResult)
                _loading.postValue(false)
                _uiState.postValue(
                    ReportUiState.AutofillSuccess(
                        AutofillResult(
                            imageUrl = uploadedImageUrl,
                            aiResponse = aiResult
                        )
                    )
                )
            } catch (error: Exception) {
                val message = when(error) {
                    is HttpException -> "Network Error ${error.code()}: ${error.message()}"
                    else -> error.message ?: "Unexpected processing error."
                }
                postError(message)
            }
        }
    }

    fun submitIssue(
        title: String,
        description: String,
        category: String,
        priority: String,
        imageUrl: String,
        latitude: Double,
        longitude: Double
    ) {
        _loading.value = true

        viewModelScope.launch {
            try {
                val issueId = firebaseRepository.saveIssue(
                    IssueModel(
                        title = title.trim(),
                        description = description.trim(),
                        category = category,
                        priority = priority,
                        imageUrl = imageUrl,
                        location = IssueLocation(latitude, longitude)
                    )
                )
                _loading.postValue(false)
                _uiState.postValue(ReportUiState.SubmitSuccess(issueId))
            } catch (error: Exception) {
                postError(error.message ?: "Failed to save issue.")
            }
        }
    }

    private fun postError(message: String) {
        _loading.postValue(false)
        _uiState.postValue(ReportUiState.Error(message))
    }
}
