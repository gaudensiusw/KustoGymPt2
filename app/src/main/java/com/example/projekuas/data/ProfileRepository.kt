package com.example.projekuas.data

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {

    // BARU: Mengambil profil user yang sedang login (digunakan oleh TrainerViewModel)
    fun getLoggedInUserProfile(): Flow<UserProfile>

    // Mengambil data user realtime (Flow)
    fun getUserProfile(userId: String): Flow<UserProfile>

    // Fungsi kompatibilitas (arahkan ke getUserProfile)
    fun getCurrentUserProfile(userId: String): Flow<UserProfile>

    // Helper untuk user yang sedang login (opsional)
    fun getProfile(): Flow<UserProfile?>

    fun getTrainers(): Flow<List<UserProfile>>

    // Update profil menggunakan Object utuh
    suspend fun updateProfile(userProfile: UserProfile)

    // Update profil menggunakan Map (Partial update, misal untuk membership)
    suspend fun updateProfile(userId: String, updates: Map<String, Any>)

    // Upload foto profil (sekarang return String Base64)
    suspend fun uploadProfileImage(uri: Uri): String?

    // Simpan profil baru (saat register)
    suspend fun saveUserProfile(profile: UserProfile)

}