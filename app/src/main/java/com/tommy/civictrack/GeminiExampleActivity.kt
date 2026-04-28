package com.tommy.civictrack

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.tommy.civictrack.gemini.GeminiUiState
import com.tommy.civictrack.gemini.GeminiViewModel

class GeminiExampleActivity : AppCompatActivity() {

    private val geminiViewModel: GeminiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gemini_example)

        val imageUrlInput: EditText = findViewById(R.id.imageUrlInput)
        val analyzeButton: Button = findViewById(R.id.analyzeButton)
        val statusText: TextView = findViewById(R.id.statusText)
        val resultText: TextView = findViewById(R.id.resultText)

        analyzeButton.setOnClickListener {
            geminiViewModel.analyzeImage(imageUrlInput.text.toString().trim())
        }

        geminiViewModel.uiState.observe(this) { state ->
            when (state) {
                GeminiUiState.Idle -> {
                    statusText.text = "Ready"
                }

                GeminiUiState.Loading -> {
                    statusText.text = "Loading..."
                }

                is GeminiUiState.Success -> {
                    val issue = state.issue
                    statusText.text = "Success"
                    resultText.text = """
                        title: ${issue.title}
                        description: ${issue.description}
                        category: ${issue.category}
                        priority: ${issue.priority}
                    """.trimIndent()

                    Log.d("GeminiExample", "Title: ${issue.title}")
                    Log.d("GeminiExample", "Description: ${issue.description}")
                    Log.d("GeminiExample", "Category: ${issue.category}")
                    Log.d("GeminiExample", "Priority: ${issue.priority}")
                }

                is GeminiUiState.Error -> {
                    statusText.text = state.message
                    resultText.text = ""
                    Log.e("GeminiExample", state.message)
                }
            }
        }
    }
}
