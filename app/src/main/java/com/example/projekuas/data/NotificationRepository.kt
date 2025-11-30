package com.example.projekuas.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotificationRepository(private val firestore: FirebaseFirestore) {

    // Mengambil notifikasi realtime untuk user tertentu
    fun getNotifications(userId: String): Flow<List<NotificationModel>> = callbackFlow {
        val listener = firestore.collection("users")
            .document(userId)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val notifs = snapshot.toObjects(NotificationModel::class.java)
                    trySend(notifs)
                }
            }
        awaitClose { listener.remove() }
    }

    // Mengirim notifikasi ke user lain
    suspend fun sendNotification(toUserId: String, title: String, message: String, type: String) {
        val notifRef = firestore.collection("users").document(toUserId).collection("notifications").document()
        val notification = NotificationModel(
            id = notifRef.id,
            title = title,
            message = message,
            type = type
        )
        notifRef.set(notification).await()
    }

    suspend fun clearAllNotifications(userId: String) {
        val batch = firestore.batch()
        val snapshot = firestore.collection("users")
            .document(userId)
            .collection("notifications")
            .get()
            .await()

        for (doc in snapshot.documents) {
            batch.delete(doc.reference)
        }
        batch.commit().await()
    }
}