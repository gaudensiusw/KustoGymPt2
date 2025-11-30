package com.example.projekuas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekuas.data.AuthRepository
import com.example.projekuas.data.SignUpState
import com.example.projekuas.data.UserProfile // Wajib: Import UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignUpViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(SignUpState())
    val state: StateFlow<SignUpState> = _state

    // --- HANDLER KREDENSIAL UTAMA ---
    fun onNameChange(newName: String) {
        _state.update { it.copy(name = newName, error = null) }
    }

    fun onEmailChange(newEmail: String) {
        _state.update { it.copy(email = newEmail, error = null) }
    }

    fun onPasswordChange(newPassword: String) {
        _state.update { it.copy(password = newPassword, error = null) }
    }

    // --- FIX: TAMBAHKAN HANDLER UNTUK FIELD YANG HILANG ---
    fun onHeightChange(newHeight: String) {
        _state.update { it.copy(heightCm = newHeight, error = null) }
    }

    fun onWeightChange(newWeight: String) {
        _state.update { it.copy(weightKg = newWeight, error = null) }
    }

    fun onDobChange(newDob: String) {
        _state.update { it.copy(dob = newDob, error = null) }
    }

    fun onPhoneChange(newPhone: String) {
        _state.update { it.copy(phoneNumber = newPhone, error = null) }
    }

    fun onAddressChange(newAddress: String) {
        _state.update { it.copy(address = newAddress, error = null) }
    }

    fun setGender(newGender: String) {
        _state.update { it.copy(gender = newGender) }
    }
    // --- AKHIR HANDLER BARU ---


    // Logika Registrasi
    fun register() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val currentState = _state.value

            // --- VALIDASI DASAR ---
            if (currentState.name.isBlank() || currentState.email.isBlank() || currentState.password.length < 6) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Nama, Email, dan Password minimal 6 karakter wajib diisi."
                    )
                }
                return@launch
            }

            // FIX PENTING: Kompilasi semua data menjadi objek UserProfile
            val userProfileData = UserProfile(
                // Data Kredensial & Dasar
                name = currentState.name,
                email = currentState.email,
                username = currentState.name, // Menggunakan nama sebagai username default

                // Data Fisik: Konversi String UI ke Double untuk Database
                heightCm = currentState.heightCm.toDoubleOrNull() ?: 0.0,
                weightKg = currentState.weightKg.toDoubleOrNull() ?: 0.0,

                // Data Lain
                // Catatan: Konversi DOB (String) ke Long harus dilakukan di sini
                dateOfBirthMillis = 0L, // FIX: Menggunakan 0L sementara waktu
                gender = currentState.gender,
                phoneNumber = currentState.phoneNumber, // FIX: Menggunakan phoneNumber dari state
                address = currentState.address,

                // Set default role dan level
                role = "Member",
                fitnessLevel = "Pemula",
                targetWeightKg = 0.0 // Target awal
            )

            try {
                // FIX: Panggil REPOSITORY dengan tanda tangan fungsi yang benar
                // Argumen: (email, password, UserProfile)
                authRepository.signUp(currentState.email, currentState.password, userProfileData)

                // Jika berhasil tanpa exception
                _state.update { it.copy(isLoading = false, isRegistrationSuccessful = true) }

            } catch (e: Exception) {
                // Tangkap error dari Firebase/Firestore
                val errorMessage = when {
                    e.message?.contains("email address is already in use") == true -> "Email sudah terdaftar."
                    e.message?.contains("password is too weak") == true -> "Password terlalu lemah."
                    else -> "Pendaftaran Gagal: ${e.message}"
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