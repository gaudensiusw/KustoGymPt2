package com.example.projekuas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekuas.data.AdminRepository
import com.example.projekuas.data.Booking
import com.example.projekuas.data.GymClass
import com.example.projekuas.data.RecentActivity
import com.example.projekuas.data.UserProfile
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Kita tidak butuh data class 'ReportData' lagi karena Booking sudah lengkap
// Hapus data class ReportData jika ada

data class AdminDashboardState(
    val totalRevenue: Double = 0.0,
    val activeMembers: Int = 0,
    val totalTrainers: Int = 0,
    val activeSessions: Int = 0,
    val isLoading: Boolean = true
)

class AdminViewModel(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _dashboardState = MutableStateFlow(AdminDashboardState())
    val dashboardState: StateFlow<AdminDashboardState> = _dashboardState.asStateFlow()

    private val _recentActivities = MutableStateFlow<List<RecentActivity>>(emptyList())
    val recentActivities: StateFlow<List<RecentActivity>> = _recentActivities.asStateFlow()

    // List Data Realtime
    private val _memberList = MutableStateFlow<List<UserProfile>>(emptyList())
    val memberList: StateFlow<List<UserProfile>> = _memberList.asStateFlow()

    private val _trainerList = MutableStateFlow<List<UserProfile>>(emptyList())
    val trainerList: StateFlow<List<UserProfile>> = _trainerList.asStateFlow()

    val classList: StateFlow<List<GymClass>> = adminRepository.getAllClassesStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // List Booking Langsung (Tanpa Mapping Rumit)
    val transactionList: StateFlow<List<Booking>> = adminRepository.getAllBookingsStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Logika Statistik
        combine(
            adminRepository.getAllUsersStream(),
            adminRepository.getAllBookingsStream()
        ) { users, bookings ->
            processData(users, bookings)
        }.launchIn(viewModelScope)
    }

    private fun processData(users: List<UserProfile>, bookings: List<Booking>) {
        val members = users.filter { it.role.equals("Member", ignoreCase = true) }
        val trainers = users.filter { it.role.equals("Trainer", ignoreCase = true) }

        _memberList.value = members
        _trainerList.value = trainers

        // Hitung Revenue dari harga yang tersimpan di booking
        val totalRevenue = bookings.sumOf { it.price }

        _dashboardState.update {
            it.copy(
                totalRevenue = totalRevenue,
                activeMembers = members.size,
                totalTrainers = trainers.size,
                activeSessions = bookings.size,
                isLoading = false
            )
        }

        // Recent Activity
        val activities = bookings.sortedByDescending { it.bookingTimeMillis }
            .take(3)
            .map { booking ->
                RecentActivity(
                    id = booking.bookingId,
                    userInitial = getInitials(booking.userName),
                    user = booking.userName.ifBlank { "User" },
                    action = "Booked ${booking.gymClassTitle}", // Gunakan field langsung
                    time = convertMillisToTime(booking.bookingTimeMillis),
                    amount = "+ ${formatCurrency(booking.price)}"
                )
            }
        _recentActivities.value = activities
    }

    // --- Helper Functions ---
    private fun getInitials(name: String): String {
        return if (name.isNotBlank()) {
            name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase()
        } else "?"
    }

    fun formatCurrency(amount: Double): String {
        return NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(amount)
    }

    private fun convertMillisToTime(millis: Long): String {
        if (millis == 0L) return "-"
        val formatter = SimpleDateFormat("dd MMM, HH:mm", Locale("id", "ID"))
        return formatter.format(Date(millis))
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch { adminRepository.deleteUser(userId) }
    }

    fun updateUserRole(userId: String, newRole: String) {
        viewModelScope.launch {
            adminRepository.updateUserRole(userId, newRole)
        }
    }

    fun deleteClass(classId: String) {
        viewModelScope.launch {
            adminRepository.deleteClass(classId)
        }
    }
}