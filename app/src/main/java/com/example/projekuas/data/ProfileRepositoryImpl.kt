package com.example.projekuas.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Repeat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

// Ensure we are using YOUR Achievement class
import com.example.projekuas.data.Achievement

class ProfileRepositoryImpl(
    private val context: Context,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    storage: FirebaseStorage
) : ProfileRepository {

    // --- EXISTING HELPER FUNCTIONS ---
    private suspend fun compressAndConvertToBase64(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val originalBitmap =
                    BitmapFactory.decodeStream(inputStream) ?: return@withContext null
                inputStream?.close()
                val scaledBitmap = getResizedBitmap(originalBitmap, 500)
                val outputStream = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
                val byteArrays = outputStream.toByteArray()
                val base64String = Base64.encodeToString(byteArrays, Base64.DEFAULT)
                "data:image/jpeg;base64,$base64String"
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap {
        var width = image.width
        var height = image.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    override suspend fun uploadProfileImage(uri: Uri): String? {
        val userId = auth.currentUser?.uid ?: return null
        val base64Image = compressAndConvertToBase64(uri) ?: return null
        return try {
            firestore.collection("users").document(userId)
                .update("profilePictureUrl", base64Image)
                .await()
            base64Image
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun getLoggedInUserProfile(): Flow<UserProfile> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(UserProfile()).isSuccess
            awaitClose {}
            return@callbackFlow
        }
        val docRef = firestore.collection("users").document(userId)
        val subscription = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error); return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val profile = snapshot.toObject(UserProfile::class.java) ?: UserProfile()
                trySend(profile).isSuccess
            } else {
                trySend(UserProfile()).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }

    override fun getUserProfile(userId: String): Flow<UserProfile> = callbackFlow {
        if (userId.isBlank()) {
            trySend(UserProfile()).isSuccess
            awaitClose {}
            return@callbackFlow
        }
        val docRef = firestore.collection("users").document(userId)
        val subscription = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error); return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val profile = snapshot.toObject(UserProfile::class.java) ?: UserProfile()
                trySend(profile).isSuccess
            } else {
                trySend(UserProfile()).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }

    override fun getCurrentUserProfile(userId: String): Flow<UserProfile> = getUserProfile(userId)

    override fun getProfile(): Flow<UserProfile?> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(null).isSuccess; close(); return@callbackFlow
        }
        val docRef = firestore.collection("users").document(userId)
        val subscription = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            if (snapshot != null && snapshot.exists()) {
                val userProfile = snapshot.toObject(UserProfile::class.java)
                trySend(userProfile).isSuccess
            } else {
                trySend(null).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun updateProfile(userProfile: UserProfile) {
        firestore.collection("users").document(userProfile.userId)
            .set(userProfile, SetOptions.merge()).await()
    }

    override suspend fun updateProfile(userId: String, updates: Map<String, Any>) {
        firestore.collection("users").document(userId).update(updates).await()
    }

    override suspend fun saveUserProfile(profile: UserProfile) {
        val userId = auth.currentUser?.uid ?: return
        val profileWithId = profile.copy(userId = userId)
        firestore.collection("users").document(userId).set(profileWithId).await()
    }

    override fun getTrainers(): Flow<List<UserProfile>> = callbackFlow {
        val listener = firestore.collection("users")
            .whereEqualTo("role", "Trainer")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val trainers = snapshot.toObjects(UserProfile::class.java)
                    trySend(trainers)
                }
            }
        awaitClose { listener.remove() }
    }

    // --- NEW ACHIEVEMENT IMPLEMENTATION (Fixed Imports) ---

    // 1. CHANGE THIS: Hardcode the list here because ImageVector cannot come from Firebase
    override suspend fun getAllAchievements(): List<Achievement> {
        return listOf(
            Achievement(
                id = "1",
                title = "First Step",
                icon = Icons.Default.DirectionsWalk,
                type = "COUNT",
                threshold = 1
            ),
            Achievement(
                id = "2",
                title = "Consistency",
                icon = Icons.Default.Repeat,
                type = "COUNT",
                threshold = 5
            ),
            Achievement(
                id = "3",
                title = "Heavy Lifter",
                icon = Icons.Default.FitnessCenter,
                type = "WEIGHT",
                threshold = 100
            ),
            Achievement(
                id = "4",
                title = "Champion",
                icon = Icons.Default.EmojiEvents,
                type = "COUNT",
                threshold = 50
            )
        )
    }

    // 2. THIS IS FINE: Checks what the user actually owns
    override suspend fun getUserUnlockedAchievements(userId: String): List<String> {
        return try {
            val snapshot = firestore.collection("users").document(userId)
                .collection("achievements").get().await()
            // We only need the IDs to match against our hardcoded list
            snapshot.documents.map { it.id }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // 3. THIS IS FINE: Saves the unlock status to Firebase
    override suspend fun unlockAchievement(userId: String, achievement: Achievement) {
        try {
            // Set the current time explicitly here
            val userAchievement = UserAchievement(
                achievementId = achievement.id,
                dateUnlocked = System.currentTimeMillis()
            )

            firestore.collection("users").document(userId)
                .collection("achievements").document(achievement.id)
                .set(userAchievement)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}