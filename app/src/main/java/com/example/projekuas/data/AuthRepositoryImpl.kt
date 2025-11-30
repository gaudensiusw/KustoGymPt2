package com.example.projekuas.data

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    // FIX: Implementasi abstract member 'currentUser'
    override val currentUser: Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(authStateListener)
        // Cleanup: Hapus listener ketika Flow dihentikan
        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }

    // Registrasi Pengguna
    // FIX: Ubah signature untuk menerima UserProfile lengkap dari ViewModel
    override suspend fun signUp(email: String, password: String, profileData: UserProfile) {

        Log.d(TAG, "Mulai pendaftaran untuk email: $email")

        // Langkah 1: Firebase Auth
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val userId = result.user?.uid

        if (userId == null) {
            Log.e(TAG, "Gagal mendapatkan UID setelah pendaftaran Auth.")
            throw Exception("Pendaftaran Gagal: UID tidak ditemukan.")
        }

        Log.d(TAG, "Auth Berhasil. UID Pengguna: $userId")

        // Langkah 2: Firestore

        // FIX: Update UserProfile dengan data Auth final sebelum disimpan
        // Data ini mencakup semua field (berat, tinggi, dob, dll) dari SignUpState
        val finalProfile = profileData.copy(
            userId = userId,
            email = email,
            role = "Member", // Set default role
            initialSetupComplete = true // Tandai setup awal selesai
        )

        try {
            Log.d(TAG, "Mencoba menulis profil lengkap ke Firestore di path: users/$userId")

            // Perintah penulisan data ke Firestore.
            // set(finalProfile) akan menggunakan semua field dari data class UserProfile.
            firestore.collection("users").document(userId).set(finalProfile).await()

            Log.d(TAG, "Penulisan profil ke Firestore berhasil.")

        } catch (e: Exception) {
            Log.e(TAG, "Penulisan profil ke Firestore GAGAL: ${e.message}", e)
            throw Exception("Pendaftaran Gagal: Gagal menyimpan data profil. Cek koneksi atau aturan.")
        }
    }

    // Login Pengguna
    override suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    override fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    override fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }

    override fun signOut() {
        auth.signOut()
    }

    override fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}