package com.example.projekuas.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

// <<< Pastikan @Entity ada
@Entity(tableName = "workout_log")
data class WorkoutLogEntity(
    // ID Lokal (Primary Key Room) - <<< Pastikan @PrimaryKey ada
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // ID yang akan digunakan di Firestore
    val cloudId: String = UUID.randomUUID().toString(),

    val userId: String,
    val dateMillis: Long = System.currentTimeMillis(),
    val exerciseName: String,
    val sets: Int,
    val reps: Int,
    val weight: Double,

    val syncStatus: Int
)