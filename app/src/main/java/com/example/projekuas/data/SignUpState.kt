package com.example.projekuas.data

data class SignUpState(
    // Kriteria Autentikasi
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",

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
    // Status
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRegistrationSuccessful: Boolean = false
)