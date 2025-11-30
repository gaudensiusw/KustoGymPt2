package com.example.projekuas.data

import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    // 1. Mengambil semua pengguna (untuk daftar manajemen)
    fun getAllUsersStream(): Flow<List<UserProfile>>

    // 2. Mengubah peran pengguna (Member -> Trainer/Admin)
    suspend fun updateUserRole(userId: String, newRole: String)

    // 3. Menghapus pengguna
    suspend fun deleteUser(userId: String)

    suspend fun deleteClass(classId: String)

    suspend fun updateClass(gymClass: GymClass)
}