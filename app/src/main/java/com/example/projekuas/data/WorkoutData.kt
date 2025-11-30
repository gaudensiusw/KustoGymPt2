package com.example.projekuas.data

import androidx.room.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow

// 1. MODEL DATA (Ganti nama jadi WorkoutSessionEntity agar tidak bentrok)
@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val muscleGroup: String, // e.g., "Chest", "Legs"
    val dateMillis: Long,
    val durationMinutes: Int,
    val caloriesBurnt: Int,
    val exercisesJson: String, // List detail latihan disimpan sebagai JSON string
    val isSynced: Boolean = false
) {
    // Helper untuk convert JSON balik ke List Object
    fun getExercisesList(): List<WorkoutExercise> {
        val type = object : TypeToken<List<WorkoutExercise>>() {}.type
        return Gson().fromJson(exercisesJson, type) ?: emptyList()
    }
}

// Model helper untuk detail isi latihan (tidak perlu @Entity karena disimpan sbg JSON)
data class WorkoutExercise(
    val name: String,
    val sets: Int,
    val reps: Int,
    val weightKg: Double
)

// 2. DAO (Data Access Object)
@Dao
interface WorkoutSessionDao {
    @Query("SELECT * FROM workout_sessions ORDER BY dateMillis DESC")
    fun getAllSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE isSynced = 0")
    suspend fun getUnsyncedSessions(): List<WorkoutSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity)

    @Query("UPDATE workout_sessions SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)

    // Statistik Header
    @Query("SELECT COUNT(*) FROM workout_sessions")
    fun getTotalSessionsCount(): Flow<Int>

    @Query("SELECT SUM(durationMinutes) FROM workout_sessions")
    fun getTotalMinutes(): Flow<Int?>

    @Query("SELECT SUM(caloriesBurnt) FROM workout_sessions")
    fun getTotalCalories(): Flow<Int?>
}