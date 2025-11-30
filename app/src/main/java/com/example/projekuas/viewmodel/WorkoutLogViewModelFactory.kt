package com.example.projekuas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.projekuas.data.AuthRepository
import com.example.projekuas.data.WorkoutDataRepository
import com.example.projekuas.data.WorkoutLogDao

/**
 * Factory kustom untuk membuat instance WorkoutLogViewModel,
 * menyediakan WorkoutLogDao yang diambil dari Room Database.
 */
// In WorkoutLogViewModelFactory.kt
class WorkoutLogViewModelFactory(
    private val workoutDataRepository: WorkoutDataRepository,
    private val authRepository: AuthRepository // FIX: Add this as a parameter
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutLogViewModel::class.java)) {
            return WorkoutLogViewModel(
                workoutDataRepository, // Pass the first required dependency
                authRepository         // Pass the second required dependency
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
