package com.tommy.civictrack

import android.content.ContentResolver
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import android.net.Uri
import com.tommy.civictrack.ai.ImgBBRepository

data class CivicIssue(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val imageUrl: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val status: String = "Pending",
    val priority: String = "Medium",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val userId: String = "demo-user"
)

class IssueRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val imgBBRepository: ImgBBRepository = ImgBBRepository()
) {
    private val issues = firestore.collection("issues")

    fun submitIssue(
        title: String,
        description: String,
        category: String,
        imageUrl: String,
        priority: String,
        latitude: Double,
        longitude: Double,
        userEmail: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val document = issues.document()
        val payload = hashMapOf(
            "id" to document.id,
            "title" to title,
            "description" to description,
            "category" to category,
            "imageUrl" to imageUrl,
            "latitude" to latitude,
            "longitude" to longitude,
            "location" to hashMapOf(
                "latitude" to latitude,
                "longitude" to longitude
            ),
            "status" to "Pending",
            "priority" to priority,
            "createdAt" to Timestamp.now(),
            "updatedAt" to Timestamp.now(),
            "userId" to userEmail
        )

        document.set(payload)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    suspend fun uploadIssueImage(
        contentResolver: ContentResolver,
        uri: Uri,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            val url = imgBBRepository.uploadImage(contentResolver, uri)
            onSuccess(url)
        } catch (e: Exception) {
            onError(e)
        }
    }

    fun listenToIssues(
        onIssuesChanged: (List<CivicIssue>) -> Unit,
        onError: (Exception) -> Unit
    ) = issues
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .limit(20)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError(error)
                return@addSnapshotListener
            }

            val currentIssues = snapshot?.documents.orEmpty().map { document ->
                val location = document.get("location") as? Map<*, *>
                CivicIssue(
                    id = document.getString("id").orEmpty(),
                    title = document.getString("title").orEmpty(),
                    description = document.getString("description").orEmpty(),
                    category = document.getString("category").orEmpty(),
                    imageUrl = document.getString("imageUrl").orEmpty(),
                    latitude = (location?.get("latitude") as? Double)
                        ?: document.getDouble("latitude")
                        ?: 0.0,
                    longitude = (location?.get("longitude") as? Double)
                        ?: document.getDouble("longitude")
                        ?: 0.0,
                    status = document.getString("status") ?: "Pending",
                    priority = document.getString("priority") ?: "Medium",
                    createdAt = document.getTimestamp("createdAt"),
                    updatedAt = document.getTimestamp("updatedAt"),
                    userId = document.getString("userId") ?: ""
                )
            }
            onIssuesChanged(currentIssues)
        }

    fun markIssueResolved(
        issueId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        issues.document(issueId)
            .update(
                mapOf(
                    "status" to "Resolved",
                    "updatedAt" to Timestamp.now()
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun deleteIssue(
        issueId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        issues.document(issueId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }
}
