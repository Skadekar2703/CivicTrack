package com.tommy.civictrack.gemini

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.io.IOException

class GeminiViewModel(
    private val repository: GeminiRepository = GeminiRepository()
) : ViewModel() {

    private val _uiState = MutableLiveData<GeminiUiState>(GeminiUiState.Idle)
    val uiState: LiveData<GeminiUiState> = _uiState

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _successData = MutableLiveData<IssueAIResponse?>()
    val successData: LiveData<IssueAIResponse?> = _successData

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun analyzeImage(imageUrl: String) {
        if (imageUrl.isBlank()) {
            val message = "Image URL cannot be empty."
            _error.value = message
            _uiState.value = GeminiUiState.Error(message)
            return
        }

        _loading.value = true
        _error.value = null
        _uiState.value = GeminiUiState.Loading
        viewModelScope.launch {
            try {
                val parsed = repository.analyzeImage(imageUrl)
                _loading.postValue(false)
                _successData.postValue(parsed)
                _uiState.postValue(GeminiUiState.Success(parsed, parsed.toString()))
            } catch (error: IOException) {
                val message = "Network failure: ${error.message.orEmpty()}"
                _loading.postValue(false)
                _error.postValue(message)
                _uiState.postValue(GeminiUiState.Error(message))
            } catch (error: Exception) {
                val message = error.message ?: "Unexpected Gemini error."
                _loading.postValue(false)
                _error.postValue(message)
                _uiState.postValue(GeminiUiState.Error(message))
            }
        }
    }
}
