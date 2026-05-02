package com.tommy.civictrack

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.tommy.civictrack.ai.MainViewModel
import com.tommy.civictrack.ai.ReportUiState

class AiIssueReportActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var previewImage: ImageView
    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var prioritySpinner: Spinner
    private lateinit var latitudeInput: EditText
    private lateinit var longitudeInput: EditText
    private lateinit var statusText: TextView
    private lateinit var progressBar: ProgressBar

    private var selectedImageUri: Uri? = null
    private var uploadedImageUrl: String = ""

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            previewImage.setImageURI(it)
            viewModel.processImage(contentResolver, it)
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            selectedImageUri?.let {
                previewImage.setImageURI(it)
                viewModel.processImage(contentResolver, it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_issue_report)

        bindViews()
        setUpSpinners()
        observeViewModel()

        findViewById<Button>(R.id.pickImageButton).setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        findViewById<Button>(R.id.captureImageButton).setOnClickListener {
            launchCamera()
        }

        findViewById<Button>(R.id.submitIssueButton).setOnClickListener {
            submitIssue()
        }
    }

    private fun bindViews() {
        previewImage = findViewById(R.id.previewImage)
        titleInput = findViewById(R.id.titleInput)
        descriptionInput = findViewById(R.id.descriptionInput)
        categorySpinner = findViewById(R.id.categorySpinner)
        prioritySpinner = findViewById(R.id.prioritySpinner)
        latitudeInput = findViewById(R.id.latitudeInput)
        longitudeInput = findViewById(R.id.longitudeInput)
        statusText = findViewById(R.id.statusText)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setUpSpinners() {
        categorySpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("pothole", "garbage", "streetlight")
        )
        prioritySpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("High", "Medium", "Low")
        )
        prioritySpinner.setSelection(1)
    }

    private fun observeViewModel() {
        viewModel.loading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        }

        viewModel.aiResponse.observe(this) { ai ->
            titleInput.setText(ai.title)
            descriptionInput.setText(ai.description)
            categorySpinner.setSelection(
                when (ai.category) {
                    "pothole" -> 0
                    "garbage" -> 1
                    "streetlight" -> 2
                    else -> 0
                }
            )
            prioritySpinner.setSelection(
                when (ai.priority) {
                    "High" -> 0
                    "Medium" -> 1
                    "Low" -> 2
                    else -> 1
                }
            )
        }

        viewModel.imageUrl.observe(this) { uploadedImageUrl = it }

        viewModel.uiState.observe(this) { state ->
            when (state) {
                ReportUiState.Idle -> statusText.text = "Ready"
                ReportUiState.Loading -> statusText.text = "Processing image..."
                is ReportUiState.AutofillSuccess -> {
                    statusText.text = "AI autofill completed.\nImage URL: ${state.result.imageUrl}"
                }
                is ReportUiState.SubmitSuccess -> {
                    statusText.text = "Issue saved to Firestore. ID: ${state.issueId}"
                    showTextToast("Issue submitted")
                }
                is ReportUiState.Error -> {
                    statusText.text = state.message
                    showTextToast(state.message, android.widget.Toast.LENGTH_LONG)
                }
            }
        }
    }

    private fun launchCamera() {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "civictrack_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri == null) {
            showTextToast("Could not create camera image")
            return
        }
        selectedImageUri = uri
        cameraLauncher.launch(uri)
    }

    private fun submitIssue() {
        val imageUrl = uploadedImageUrl
        if (imageUrl.isBlank()) {
            showTextToast("Process an image first")
            return
        }

        val latitude = latitudeInput.text.toString().toDoubleOrNull()
        val longitude = longitudeInput.text.toString().toDoubleOrNull()
        if (latitude == null || longitude == null) {
            showTextToast("Enter valid latitude and longitude")
            return
        }

        viewModel.submitIssue(
            title = titleInput.text.toString(),
            description = descriptionInput.text.toString(),
            category = categorySpinner.selectedItem.toString(),
            priority = prioritySpinner.selectedItem.toString(),
            imageUrl = imageUrl,
            latitude = latitude,
            longitude = longitude
        )
    }

    companion object {
        fun newIntent(context: android.content.Context): Intent =
            Intent(context, AiIssueReportActivity::class.java)
    }
}
