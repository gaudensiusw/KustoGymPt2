package com.example.projekuas.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekuas.data.AuthRepository
import com.example.projekuas.data.ProfileRepository
import com.example.projekuas.data.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Wrapper Class untuk State UI agar Data User terpisah dari Status Loading/Error
data class ProfileUiState(
    val userProfile: UserProfile = UserProfile(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val currentUserId = authRepository.getCurrentUserId() ?: ""
    private val currentUserEmail = authRepository.getCurrentUserEmail() ?: ""

    // Menggunakan UiState sebagai Single Source of Truth
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // Set data awal minimal (ID & Email) sebelum load dari DB
        _uiState.update {
            it.copy(userProfile = UserProfile(userId = currentUserId, email = currentUserEmail))
        }
        loadUserProfile()
    }

    private fun loadUserProfile() {
        if (currentUserId.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Mengamati perubahan profil secara real-time dari Repository
            profileRepository.getCurrentUserProfile(currentUserId)
                .collect { profileFromDb ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            // Gabungkan data DB dengan email auth yang valid
                            userProfile = profileFromDb.copy(email = currentUserEmail),
                            isLoading = false
                        )
                    }
                }
        }
    }

    // --- FITUR BARU: GANTI FOTO PROFIL ---

    fun updateProfilePicture(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 1. Upload ke Firebase Storage
            val downloadUrl = profileRepository.uploadProfileImage(uri)

            if (downloadUrl != null) {
                // 2. Update URL di object UserProfile lokal
                val updatedProfile = _uiState.value.userProfile.copy(profilePictureUrl = downloadUrl)

                // 3. Simpan perubahan URL ke Firestore
                profileRepository.saveUserProfile(updatedProfile)

                // Update state UI
                _uiState.update {
                    it.copy(userProfile = updatedProfile, isLoading = false)
                }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, error = "Gagal mengupload gambar.")
                }
            }
        }
    }

    // --- HANDLER INPUT (FORMULIR) ---

    fun onNameChange(newName: String) {
        _uiState.update {
            it.copy(userProfile = it.userProfile.copy(name = newName), error = null)
        }
    }

    fun onWeightChange(newWeight: String) {
        val kg = newWeight.toDoubleOrNull() ?: 0.0
        _uiState.update {
            it.copy(userProfile = it.userProfile.copy(weightKg = kg), error = null)
        }
    }

    fun onHeightChange(newHeight: String) {
        val cm = newHeight.toDoubleOrNull() ?: 0.0
        _uiState.update {
            it.copy(userProfile = it.userProfile.copy(heightCm = cm), error = null)
        }
    }

    fun onFitnessLevelSelected(level: String) {
        _uiState.update {
            it.copy(userProfile = it.userProfile.copy(fitnessLevel = level))
        }
    }

    // --- LOGIKA SIMPAN PERUBAHAN TEXT ---

    fun saveProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val currentProfile = _uiState.value.userProfile

            // Validasi sederhana
            if (currentProfile.name.isBlank()) {
                _uiState.update { it.copy(isLoading = false, error = "Nama tidak boleh kosong.") }
                return@launch
            }

            try {
                // Simpan ke Firestore
                profileRepository.saveUserProfile(currentProfile)

                // Delay sedikit untuk efek visual
                kotlinx.coroutines.delay(500)

                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Gagal menyimpan: ${e.message}") }
            }
        }
    }

    // --- LOGIKA LOGOUT ---

    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
            } catch (e: Exception) {
                // Log error jika perlu
            }
        }
    }
}