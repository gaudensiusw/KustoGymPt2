package com.example.projekuas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekuas.data.AdminRepository
import com.example.projekuas.data.AuthRepository
import com.example.projekuas.data.ClassRepository
import com.example.projekuas.data.HomeState
import com.example.projekuas.data.ProfileRepository
import com.example.projekuas.data.WorkoutDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// HomeViewModel ini nanti akan menerima Repository sebagai dependency
// Hapus komentar saat Anda sudah mengimplementasikan Repository
class HomeViewModel(
    authRepository: AuthRepository,
    workoutDataRepository: WorkoutDataRepository,
    profileRepository: ProfileRepository,
    classRepository: ClassRepository,
    adminRepository: AdminRepository
) : ViewModel() {

    // StateFlow untuk menyimpan status dashboard
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state

    init {
        // Panggil fungsi untuk memuat data saat ViewModel pertama kali dibuat
        loadDashboardData()
    }

    /**
     * Handler untuk mengubah item Bottom Bar yang dipilih.
     * Mengubah state yang diamati oleh Composable GymBottomNavBar.
     * @param index Indeks tab yang dipilih (0=Dashboard, 1=Booking, 2=Profile).
     */
    fun onBottomTabSelected(index: Int) {
        _state.update { it.copy(selectedBottomTabIndex = index) }
        // Di masa depan, Anda dapat memuat data spesifik di sini
        // jika perpindahan tab memerlukan refresh data yang berbeda.
    }

    /**
     * Memuat semua data yang dibutuhkan untuk Home Dashboard.
     * Saat ini menggunakan simulasi delay, nanti akan diganti dengan panggilan Repository.
     */
    fun loadDashboardData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // --- SIMULASI PENGAMBILAN DATA (1 detik) ---
            kotlinx.coroutines.delay(1000)

            // Setelah data diambil (simulasi sukses), update state
            _state.update {
                it.copy(
                    userName = "Rahmat Hidayat",
                    fitnessLevel = "Menengah",
                    currentWeight = 75.5,
                    targetWeight = 70.0,
                    workoutsCompletedThisWeek = 3,
                    membershipStatus = "Aktif (Premium)",
                    isLoading = false
                )
            }
        }
    }

    // Anda dapat menambahkan fungsi lain di sini, seperti:
    // fun logout() { /* ... */ }
    // fun refreshProgress() { /* ... */ }
}