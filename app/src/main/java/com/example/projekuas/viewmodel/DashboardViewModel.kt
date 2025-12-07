package com.example.projekuas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekuas.data.AuthRepository
import com.example.projekuas.data.ProfileRepository
import com.example.projekuas.data.WorkoutDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 1. Update DashboardState dengan field profile
data class DashboardState(
    val userRole: String = "",
    val name: String = "",
    val profilePictureUrl: String = "",
    val membershipType: String = "",
    val isLoading: Boolean = false
)

class DashboardViewModel(
    private val authRepository: AuthRepository,
    private val workoutRepository: WorkoutDataRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    init {
        observeAuthState()
    }
    
    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.currentUser.collect { firebaseUser ->
                if (firebaseUser != null) {
                    loadUserData()
                } else {
                    _dashboardState.update { DashboardState(isLoading = false) }
                }
            }
        }
    }

    fun refreshData() {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _dashboardState.update { it.copy(isLoading = true) }
            try {
                profileRepository.getLoggedInUserProfile().collect { userProfile ->
                    val role = userProfile.role
                    _dashboardState.update {
                        it.copy(
                            userRole = role,
                            name = userProfile.name,
                            profilePictureUrl = userProfile.profilePictureUrl,
                            membershipType = userProfile.membershipType.ifBlank { "Basic" },
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _dashboardState.update {
                    it.copy(
                        userRole = "", 
                        isLoading = false
                    )
                }
            }
        }
    }
}