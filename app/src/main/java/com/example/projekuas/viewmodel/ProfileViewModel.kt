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
import java.text.SimpleDateFormat
import java.util.Locale

// Wrapper Class untuk State UI
data class ProfileUiState(
    val userProfile: UserProfile = UserProfile(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val currentUserId = authRepository.getCurrentUserId() ?: ""
    private val currentUserEmail = authRepository.getCurrentUserEmail() ?: ""

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // Variable untuk cooldown update (1 jam)
    private var lastUpdateTimestamp: Long = 0

    init {
        _uiState.update {
            it.copy(userProfile = UserProfile(userId = currentUserId, email = currentUserEmail))
        }
        loadUserProfile()
    }

    private fun loadUserProfile() {
        if (currentUserId.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                profileRepository.getCurrentUserProfile(currentUserId)
                    .collect { profileFromDb ->
                        _uiState.update { currentState ->
                            currentState.copy(
                                // Gabungkan data DB dengan email auth
                                userProfile = profileFromDb.copy(email = currentUserEmail),
                                isLoading = false
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    // --- HANDLER INPUT (FORMULIR) ---

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(userProfile = it.userProfile.copy(name = newName)) }
    }

    fun onEmailChange(newEmail: String) {
        _uiState.update { it.copy(userProfile = it.userProfile.copy(email = newEmail)) }
    }

    fun onPhoneChange(newPhone: String) {
        _uiState.update { it.copy(userProfile = it.userProfile.copy(phoneNumber = newPhone)) }
    }

    fun onAddressChange(newAddress: String) {
        _uiState.update { it.copy(userProfile = it.userProfile.copy(address = newAddress)) }
    }

    // [PERBAIKAN] Konversi String input ke Long untuk dateOfBirthMillis
    fun onDobChange(newDob: String) {
        try {
            // Mencoba parsing format YYYY-MM-DD
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(newDob)
            val millis = date?.time ?: 0L

            _uiState.update {
                it.copy(userProfile = it.userProfile.copy(dateOfBirthMillis = millis))
            }
        } catch (e: Exception) {
            // Jika format belum valid (sedang mengetik), jangan update dulu atau biarkan 0
        }
    }

    fun onHeightChange(newHeight: String) {
        val cm = newHeight.toDoubleOrNull() ?: 0.0
        _uiState.update { it.copy(userProfile = it.userProfile.copy(heightCm = cm)) }
    }

    fun onWeightChange(newWeight: String) {
        val kg = newWeight.toDoubleOrNull() ?: 0.0
        _uiState.update { it.copy(userProfile = it.userProfile.copy(weightKg = kg)) }
    }

    fun onFitnessLevelSelected(level: String) {
        _uiState.update { it.copy(userProfile = it.userProfile.copy(fitnessLevel = level)) }
    }

    // --- FITUR GANTI FOTO ---
    fun updateProfilePicture(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val downloadUrl = profileRepository.uploadProfileImage(uri)

            if (downloadUrl != null) {
                val updatedProfile = _uiState.value.userProfile.copy(profilePictureUrl = downloadUrl)
                profileRepository.saveUserProfile(updatedProfile)
                _uiState.update {
                    it.copy(userProfile = updatedProfile, isLoading = false, successMessage = "Foto berhasil diperbarui!")
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Gagal mengupload gambar.") }
            }
        }
    }

    // --- LOGIKA SIMPAN ---
    fun saveProfile() {
        val currentTime = System.currentTimeMillis()
        val oneHourMillis = 60 * 60 * 1000

        // Cek Cooldown
        if (currentTime - lastUpdateTimestamp < oneHourMillis) {
            val remainingMinutes = (oneHourMillis - (currentTime - lastUpdateTimestamp)) / 1000 / 60
            _uiState.update { it.copy(error = "Tunggu $remainingMinutes menit lagi untuk update profil.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, successMessage = null) }

            val currentProfile = _uiState.value.userProfile

            if (currentProfile.name.isBlank()) {
                _uiState.update { it.copy(isSaving = false, error = "Nama tidak boleh kosong.") }
                return@launch
            }

            try {
                profileRepository.saveUserProfile(currentProfile)
                lastUpdateTimestamp = System.currentTimeMillis()

                kotlinx.coroutines.delay(500)
                _uiState.update { it.copy(isSaving = false, successMessage = "Profil berhasil disimpan!") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "Gagal menyimpan: ${e.message}") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    fun logout() {
        viewModelScope.launch {
            try { authRepository.signOut() } catch (e: Exception) {}
        }
    }
}