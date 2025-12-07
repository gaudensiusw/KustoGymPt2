package com.example.projekuas.data

import androidx.compose.ui.graphics.vector.ImageVector

data class Achievement(
    val id: String,
    val title: String,
    val icon: ImageVector, // Or Int (R.drawable.xxx) if using XML resources
    val isUnlocked: Boolean = false, // Default to false (Grey)
    val type: String = "COUNT", // Type of rule: "COUNT" (sessions) or "WEIGHT" (kg)
    val threshold: Int = 0      // The target number needed to unlock (e.g., 1, 5, 100)
)