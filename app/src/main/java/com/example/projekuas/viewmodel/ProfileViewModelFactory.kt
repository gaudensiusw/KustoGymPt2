package com.example.projekuas.viewmodel

// Import yang diperlukan
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.projekuas.data.AuthRepository
import com.example.projekuas.data.ProfileRepository
import com.example.projekuas.viewmodel.ProfileViewModel

class ProfileViewModelFactory(
    // FIX PENTING: Urutan harus ProfileRepository lalu AuthRepository
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(
                profileRepository,
                authRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}