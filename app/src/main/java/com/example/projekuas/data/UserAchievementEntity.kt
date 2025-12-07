package com.example.projekuas.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_achievements")
data class UserAchievementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val userId: String,        // ID user yang login
    val achievementId: String, // ID achievement (misal: "workout_5_times")
    val unlockedAt: Long       // Timestamp kapan didapatkan
)