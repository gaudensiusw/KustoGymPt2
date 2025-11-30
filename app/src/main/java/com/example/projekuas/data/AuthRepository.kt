package com.example.projekuas.data

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<FirebaseUser?>
    // Fungsi untuk registrasi (membuat akun)
    suspend fun signUp(email: String, password: String, profileData: UserProfile)
    // Fungsi untuk login (masuk ke akun)
    suspend fun signIn(email: String, password: String)

    // Mendapatkan ID pengguna saat ini
    fun getCurrentUserId(): String?

    // Cek status login
    fun isUserLoggedIn(): Boolean
    fun getCurrentUserEmail(): String? // <-- TAMBAHKAN INI
    fun signOut()
}