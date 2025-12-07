// AdminRepository.kt
package com.example.projekuas.data

import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    // --- FITUR LAMA (Agar tidak error 'overrides nothing') ---
    fun getAllUsersStream(): Flow<List<UserProfile>>
    fun getAllBookingsStream(): Flow<List<Booking>> // <--- Tambahkan ini
    fun getAllClassesStream(): Flow<List<GymClass>> // <--- TAMBAHKAN INI
    fun getTransactionsStream(): Flow<List<Transaction>> // <--- Logic Baru Revenue
    suspend fun updateUserRole(userId: String, newRole: String)
    suspend fun deleteUser(userId: String)

    // --- FITUR BARU (Dashboard) ---
    fun getRealtimeMembers(): Flow<List<UserProfile>>
    fun getRealtimeTrainers(): Flow<List<UserProfile>>
    fun getRealtimeClasses(): Flow<List<GymClass>>

    // Statistik
    fun getDashboardStats(): Flow<DashboardStats>

    // CRUD Kelas
    suspend fun addClass(gymClass: GymClass)
    suspend fun updateClass(gymClass: GymClass)
    suspend fun deleteClass(classId: String)
}