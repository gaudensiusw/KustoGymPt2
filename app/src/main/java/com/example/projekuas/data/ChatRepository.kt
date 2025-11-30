package com.example.projekuas.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository(private val firestore: FirebaseFirestore) {

    // Bikin ID Chat unik gabungan 2 user biar konsisten
    private fun getChatId(user1: String, user2: String): String {
        return if (user1 < user2) "${user1}_${user2}" else "${user2}_${user1}"
    }

    fun getMessages(currentUserId: String, otherUserId: String): Flow<List<Message>> = callbackFlow {
        val chatId = getChatId(currentUserId, otherUserId)

        val listener = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val messages = snapshot.toObjects(Message::class.java)
                    trySend(messages)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(currentUserId: String, otherUserId: String, text: String) {
        val chatId = getChatId(currentUserId, otherUserId)
        val message = Message(
            id = firestore.collection("chats").document().id,
            senderId = currentUserId,
            text = text,
            timestamp = System.currentTimeMillis()
        )

        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(message)
            .await()

        // Simpan metadata chat (opsional, buat list chat history)
        firestore.collection("chats").document(chatId).set(mapOf(
            "participants" to listOf(currentUserId, otherUserId),
            "lastMessage" to text,
            "lastTimestamp" to message.timestamp
        )).await()
    }
}