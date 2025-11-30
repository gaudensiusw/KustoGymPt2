package com.example.projekuas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekuas.data.AdminRepository
import com.example.projekuas.data.AuthRepository
import com.example.projekuas.data.ClassRepository
import com.example.projekuas.data.HomeState
import com.example.projekuas.data.ProfileRepository
import com.example.projekuas.data.WorkoutDataRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val workoutDataRepository: WorkoutDataRepository,
    private val profileRepository: ProfileRepository,
    private val classRepository: ClassRepository,
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _homeState = MutableStateFlow(HomeState())
    val homeState: StateFlow<HomeState> = _homeState

    init {
        checkLoginStatus()
    }

    // --- FUNGSI AUTHENTICATION ---

    fun checkLoginStatus() {
        viewModelScope.launch {
            _homeState.update { it.copy(isLoading = true) }

            // Cek apakah ada user login di AuthRepository
            val isUserLoggedIn = authRepository.isUserLoggedIn() // Pastikan fungsi ini ada di AuthRepository

            if (isUserLoggedIn) {
                // Jika login, ambil role user dari ProfileRepository
                // (Asumsi: Anda punya cara ambil role, kalau belum kita set default Member dulu)
                val role = try {
                    // profileRepository.getUserRole()
                    "Member" // Default sementara biar ga error
                } catch (e: Exception) {
                    "Member"
                }

                _homeState.update {
                    it.copy(
                        isLoggedIn = true,
                        userRole = role,
                        isLoading = false
                    )
                }
                loadDashboardData() // Load data jika login
            } else {
                _homeState.update {
                    it.copy(
                        isLoggedIn = false,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
            _homeState.update { it.copy(isLoggedIn = false) }
        }
    }

    // --- DASHBOARD LOGIC ---

    fun loadDashboardData() {
        viewModelScope.launch {
            // Simulasi load data
            _homeState.update {
                it.copy(
                    userName = "User Gym",
                    membershipStatus = "Active"
                )
            }
        }
    }
}