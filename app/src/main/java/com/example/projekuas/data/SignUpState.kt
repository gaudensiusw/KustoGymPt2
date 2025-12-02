package com.example.projekuas.data

data class SignUpState(
    // Kriteria Autentikasi
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "", // Tambahan untuk UI

    // Kriteria Profil
    val height: String = "", // Simpan sebagai String dulu untuk input teks
    val weight: String = "", // Simpan sebagai String dulu
    val gender: String = "Pilih Jenis Kelamin",
    val phone: String = "",
    val address: String = "",
    val heightCm: String = "",
    val weightKg: String = "",
    val dob: String = "",        // Tanggal Lahir (String)
    val phoneNumber: String = "",// Nomor Telepon

    // Validasi Error per Field (Agar UI bisa menampilkan error merah di bawah input)
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,

    // Status
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSignUpSuccessful: Boolean = false // Diganti agar konsisten dengan UI
)