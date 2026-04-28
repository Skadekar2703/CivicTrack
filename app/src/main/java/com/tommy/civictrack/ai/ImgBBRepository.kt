package com.tommy.civictrack.ai

import android.content.ContentResolver
import android.net.Uri
import com.tommy.civictrack.BuildConfig
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ImgBBRepository(
    private val api: ImgBBApi = ImgBBClient.api
) {

    suspend fun uploadImage(
        contentResolver: ContentResolver,
        imageUri: Uri
    ): String {
        require(BuildConfig.IMGBB_API_KEY.isNotBlank()) {
            "Missing ImgBB API key. Add imgbb.api.key to local.properties or IMGBB_API_KEY to Gradle properties."
        }

        val bytes = contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
            ?: throw IllegalStateException("Could not read selected image.")

        val mimeType = contentResolver.getType(imageUri) ?: "image/jpeg"
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData(
            name = "image",
            filename = "issue_image.jpg",
            body = requestBody
        )

        val response = api.uploadImage(
            apiKey = BuildConfig.IMGBB_API_KEY,
            image = imagePart
        )

        if (!response.success) {
            throw IllegalStateException("ImgBB upload failed with status ${response.status}.")
        }

        return response.data?.url
            ?: response.data?.display_url
            ?: throw IllegalStateException("ImgBB did not return an image URL.")
    }
}
