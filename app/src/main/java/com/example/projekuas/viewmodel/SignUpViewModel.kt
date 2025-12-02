package com.example.projekuas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekuas.data.AuthRepository
import com.example.projekuas.data.SignUpState
import com.example.projekuas.data.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignUpViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpState())
    val uiState: StateFlow<SignUpState> = _uiState.asStateFlow()

    // --- INPUT HANDLERS ---
    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName, nameError = null) }
    }

    fun onEmailChange(newEmail: String) {
        _uiState.update { it.copy(email = newEmail, emailError = null) }
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.update { it.copy(password = newPassword, passwordError = null) }
    }

    fun onConfirmPasswordChange(newConfirm: String) {
        _uiState.update { it.copy(confirmPassword = newConfirm, confirmPasswordError = null) }
    }

    // --- LOGIC SIGN UP ---
    fun signUp() {
        val currentState = _uiState.value
        var hasError = false

        // 1. Validasi Nama
        if (currentState.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Nama tidak boleh kosong") }
            hasError = true
        }

        // 2. Validasi Email
        if (currentState.email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            _uiState.update { it.copy(emailError = "Format email tidak valid") }
            hasError = true
        }

        // 3. Validasi Password
        if (currentState.password.length < 6) {
            _uiState.update { it.copy(passwordError = "Password minimal 6 karakter") }
            hasError = true
        }

        // 4. Validasi Konfirmasi Password
        if (currentState.password != currentState.confirmPassword) {
            _uiState.update { it.copy(confirmPasswordError = "Password tidak cocok") }
            hasError = true
        }

        if (hasError) return

        // Jika Lolos Validasi, Lanjut ke Firebase
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Buat objek UserProfile standar untuk member baru
                // Data fisik (tinggi/berat) di-set 0 atau default, user isi nanti di profile
                val newUserProfile = UserProfile(
                    name = currentState.name,
                    email = currentState.email,
                    username = currentState.email.substringBefore("@"), // Generate username dari email
                    role = "Member",
                    fitnessLevel = "Pemula",
                    targetWeightKg = 0.0,
                    heightCm = 0.0,
                    weightKg = 0.0,
                    gender = "",
                    dateOfBirthMillis = 0L
                )

                authRepository.signUp(currentState.email, currentState.password, newUserProfile)
                _uiState.update { it.copy(isLoading = false, isSignUpSuccessful = true) }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Terjadi kesalahan saat mendaftar"
                    )
                }
            }
        }
    }
}