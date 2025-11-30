package com.example.projekuas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.projekuas.data.AuthRepository
import com.example.projekuas.data.ProfileRepository
import com.example.projekuas.data.WorkoutDataRepository // Import ini

/**
 * Factory for ViewModels that require repositories.
 */
class AuthViewModelFactory(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    // FIX 1: Tambahkan WorkoutDataRepository ke konstruktor Factory
    private val workoutDataRepository: WorkoutDataRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(authRepository) as T
        }
        if (modelClass.isAssignableFrom(SignUpViewModel::class.java)) {
            return SignUpViewModel(authRepository) as T
        }

        // FIX 2: Gunakan instance workoutDataRepository yang sudah diinjeksi
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(
                authRepository,
                workoutDataRepository, // Gunakan instance yang diinjeksikan
                profileRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}