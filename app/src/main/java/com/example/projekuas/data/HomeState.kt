package com.example.projekuas.data

data class HomeState(
    // Informasi Dasar Pengguna
    val userName: String = "Member Gym",
    val fitnessLevel: String = "Pemula",

    // Status Progress
    val currentWeight: Double = 0.0,
    val targetWeight: Double = 0.0,
    val workoutsCompletedThisWeek: Int = 0,

    // Status Membership
    val membershipStatus: String = "Aktif",
    val membershipExpiryDate: String = "20/12/2025",

    // State untuk Bottom Navigation Bar
    val selectedBottomTabIndex: Int = 0, // 0=Dashboard (default)

    // Status Loading & Error
    val isLoading: Boolean = false,
    val error: String? = null
)