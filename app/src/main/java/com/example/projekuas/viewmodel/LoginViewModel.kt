package com.example.projekuas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekuas.data.AuthRepository
import com.example.projekuas.data.LoginState
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() { // Pastikan authRepository diakses sebagai properti
    // MutableStateFlow untuk menyimpan state yang dapat berubah
    private val _state = MutableStateFlow(LoginState())
    // StateFlow yang akan diekspos ke Composable (UI)
    val state: StateFlow<LoginState> = _state

    // Handler untuk perubahan input Email
    fun onEmailChange(newEmail: String) {
        _state.update { it.copy(email = newEmail, error = null) }
    }

    // Handler untuk perubahan input Password
    fun onPasswordChange(newPassword: String) {
        _state.update { it.copy(password = newPassword, error = null) }
    }

    // Logika Login
    fun login() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val email = _state.value.email
            val password = _state.value.password

            // Validasi input dasar
            if (email.isBlank() || password.isBlank()) {
                _state.update { it.copy(isLoading = false, error = "Email dan Password harus diisi.") }
                return@launch
            }

            try {
                // FIX: Panggil fungsi signIn dari AuthRepository (Firebase Auth)
                authRepository.signIn(email, password)

                // Jika fungsi signIn berhasil tanpa melempar exception:
                _state.update { it.copy(isLoading = false, isLoginSuccessful = true) }

            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is FirebaseAuthInvalidUserException -> "Akun tidak ditemukan. Silakan daftar terlebih dahulu."
                    is FirebaseAuthInvalidCredentialsException -> "Password salah. Silakan coba lagi."
                    is FirebaseNetworkException -> "Koneksi bermasalah. Periksa internet Anda."
                    else -> "Login Gagal: ${e.localizedMessage ?: "Terjadi kesalahan sistem."}"
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
            }
        }
    }
}