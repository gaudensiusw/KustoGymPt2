package com.example.projekuas.data

import kotlinx.coroutines.flow.Flow

/**
 * Interface Repository yang mendefinisikan kontrak untuk pengelolaan data Workout.
 * Menggabungkan fitur Simple Log (Room) dan Advanced Tracker (Firebase).
 */
interface WorkoutDataRepository {

    // ==========================================
    // 1. FITUR LAMA: SIMPLE LOG (Room Database)
    // ==========================================

    // Mengambil semua log dari database lokal (Room) secara real-time.
    fun getAllLogsFlow(userId: String): Flow<List<WorkoutLogEntity>>

    // Stream alternatif (bisa diarahkan ke getAllLogsFlow)
    fun getAllLogsStream(userId: String): Flow<List<WorkoutLogEntity>>

    // Memasukkan log baru (Wrapper)
    suspend fun insertLog(log: WorkoutLogEntity)

    // Menyimpan log baru secara lokal (Status PENDING/Offline).
    suspend fun saveLogLocal(log: WorkoutLogEntity)

    // Sinkronisasi data PENDING dari Room ke Firestore.
    suspend fun syncPendingLogs(userId: String)

    suspend fun deleteWorkoutSessions(userId: String, sessionIds: List<String>) // Tambah ini
    // ==========================================
    // 2. FITUR BARU: ADVANCED TRACKER (Firebase)
    // ==========================================

    // Mengambil daftar jenis latihan berdasarkan otot (Chest, Back, dll)
    suspend fun getExercisesByMuscle(muscle: String): List<ExerciseMaster>

    // Menyimpan sesi latihan lengkap (dengan set & repetisi) ke Firebase
    // Menyimpan sesi latihan baru
    suspend fun saveWorkoutSession(session: WorkoutSession)

    // Mengambil semua riwayat latihan user dari Firebase
    suspend fun getWorkoutHistory(userId: String): List<WorkoutSession>
}