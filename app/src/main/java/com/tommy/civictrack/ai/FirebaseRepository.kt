package com.tommy.civictrack.ai

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun saveIssue(issue: IssueModel): String = suspendCancellableCoroutine { continuation ->
        val document = firestore.collection("issues").document()
        val payload = issue.copy(
            id = document.id,
            timestamp = Timestamp.now()
        )

        document.set(payload)
            .addOnSuccessListener {
                if (continuation.isActive) continuation.resume(document.id)
            }
            .addOnFailureListener {
                if (continuation.isActive) continuation.resumeWithException(it)
            }
    }
}
