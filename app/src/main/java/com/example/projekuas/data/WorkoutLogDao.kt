package com.example.projekuas.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface WorkoutLogDao {
    @Insert
    suspend fun insert(log: WorkoutLogEntity)

    @Update
    suspend fun update(log: WorkoutLogEntity)

    // Mengambil semua log untuk ditampilkan (Flow agar UI update otomatis)
    @Query("SELECT * FROM workout_log WHERE userId = :userId ORDER BY dateMillis DESC")
    fun getAllLogs(userId: String): Flow<List<WorkoutLogEntity>>

    // Query KRUSIAL untuk sinkronisasi
    @Query("SELECT * FROM workout_log WHERE syncStatus = :pendingStatus")
    suspend fun getPendingLogs(pendingStatus: Int = SyncStatus.PENDING.code): List<WorkoutLogEntity>

    @Query("""
    SELECT 
        strftime('%Y-%m-%d', dateMillis / 1000, 'unixepoch') as date, 
        SUM(weight * reps) as totalVolume 
    FROM workout_log 
    WHERE exerciseName = :exerciseName 
    GROUP BY date 
    ORDER BY date ASC
""")
    fun getVolumeHistory(exerciseName: String): Flow<List<VolumeData>>
}