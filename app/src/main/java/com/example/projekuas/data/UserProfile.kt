package com.example.projekuas.data

data class UserProfile(
    // --- 1. DATA ID & AUTH (Wajib) ---
    val userId: String = "",         // ID Unik dari Firebase Auth
    val email: String = "",          // Email pengguna untuk Login

    // --- 2. DATA PROFIL WAJIB ---
    val name: String = "",           // Nama lengkap pengguna
    val username: String = "",       // Username (opsional, tapi berguna untuk tampilan)
    val phoneNumber: String? = null,
    val address: String = "",

    // --- 3. DATA FISIK (Utamanya untuk Member) ---
    val heightCm: Double = 0.0,      // Tinggi dalam cm
    val weightKg: Double = 0.0,      // Berat badan saat ini
    val targetWeightKg: Double = 0.0, // Tambahkan Target Berat Badan untuk Dashboard
    val dateOfBirthMillis: Long = 0L, // Tanggal Lahir (disimpan dalam timestamp Long)
    val gender: String = "",

    // --- 4. DATA LEVEL & ROLE MANAGEMENT (Kunci Logika Bisnis) ---
    val fitnessLevel: String = "Pemula",
    val role: String = "Member",     // Nilai: Member, Trainer, Admin

    // --- 5. DATA TAMBAHAN (Optional, untuk Trainer/Admin) ---
    val specialties: List<String> = emptyList(), // Misal: Sertifikasi Trainer
    val initialSetupComplete: Boolean = false, // Untuk menandai apakah setup profil awal sudah selesai

    val profilePictureUrl: String = "", // TAMBAHAN PENTING

    val rank: String = "Fitness Newbie",
    val birthDate: Long = 0L, // Timestamp

// Status
    val membershipType: String = "", // "Basic", "Premium", "Elite"
    val membershipValidUntil: Long = 0L, // Timestamp kadaluarsa
    val membershipExpiry: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000), // +30 hari
    val workoutsTotal: Int = 0,
    val caloriesBurnt: Int = 0,
    val joinDate: Long = System.currentTimeMillis(), // Pastikan field ini ada di firestore, atau kita pakai default
    val totalRatingSum: Int = 0, // Jumlah total bintang
    val ratingCount: Int = 0,    // Jumlah orang yang me-rating
    val averageRating: Double = 0.0, // Rata-rata (disimpan biar gampang diambil)
    val loading: Boolean = false,
    val error: String? = null

)
    // --- 6. STATUS UI (Hanya di ViewModel, bukan di Database) ---
    // Catatan: Properti ini sebaiknya dihilangkan dari data class yang MAPPING ke Firestore.
    // val isLoading: Boolean = false,
    // val error: String? = null
