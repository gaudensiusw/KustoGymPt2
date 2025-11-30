package com.example.projekuas.data

data class WorkoutLogState(
    val logs: List<WorkoutLogEntity> = emptyList(),
    val currentUserId: String = "member_123", // Diganti dengan ID pengguna yang sedang login
    val inputExerciseName: String = "",
    val inputSets: String = "",
    val inputReps: String = "",
    val inputWeight: String = "",
    val isSaving: Boolean = false,
    val syncNeeded: Boolean = false
)