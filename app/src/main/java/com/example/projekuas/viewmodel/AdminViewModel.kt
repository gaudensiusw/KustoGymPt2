package com.example.projekuas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekuas.data.AdminRepository
import com.example.projekuas.data.Booking
import com.example.projekuas.data.GymClass
import com.example.projekuas.data.RecentActivity
import com.example.projekuas.data.Transaction // ADDED
import com.example.projekuas.data.UserProfile
import androidx.compose.ui.graphics.Color
import com.example.projekuas.ui.dashboard.MonthlyData
import com.example.projekuas.ui.dashboard.TopClassData
import com.example.projekuas.ui.dashboard.MembershipData
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ReportState(
    val monthlyRevenue: List<MonthlyData> = emptyList(),
    val topClasses: List<TopClassData> = emptyList(),
    val membershipStats: List<MembershipData> = emptyList()
)

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

    private val _reportState = MutableStateFlow(ReportState())
    val reportState: StateFlow<ReportState> = _reportState.asStateFlow()

    // Filter Period
    private val _selectedPeriod = MutableStateFlow("Month")
    val selectedPeriod: StateFlow<String> = _selectedPeriod.asStateFlow()

    fun setReportPeriod(period: String) {
        _selectedPeriod.value = period
    }

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
            adminRepository.getAllBookingsStream(),
            adminRepository.getTransactionsStream() // ADDED
        ) { users, bookings, transactions ->
            processData(users, bookings, transactions)
        }.launchIn(viewModelScope)
    }

    private fun processData(users: List<UserProfile>, bookings: List<Booking>, transactions: List<Transaction>) {
        val members = users.filter { it.role.equals("Member", ignoreCase = true) }
        val trainers = users.filter { it.role.equals("Trainer", ignoreCase = true) }

        _memberList.value = members
        _trainerList.value = trainers

        // Hitung Revenue dari Transaksi Membership (sebelumnya bookings.sumOf { it.price })
        val totalRevenue = transactions.sumOf { it.amount }

        _dashboardState.update {
            it.copy(
                totalRevenue = totalRevenue,
                activeMembers = members.size,
                totalTrainers = trainers.size,
                activeSessions = bookings.size,
                isLoading = false
            )
        }

        // Recent Activity (Gabungan Booking & Transaksi Membership?)
        // Untuk sekarang kita pakai Booking karena lebih visual "aktivitas gym",
        // tapi user minta log transaksi?
        // User request: "sekarang +duid kalau member memperpanjang membership... bukan per booking"
        // Jadi Activity Log mungkin lebih relevan menampilkan Booking, tapi Revenue Widget menampilkan Revenue.
        // Kita tampilkan Booking sebagai "Recent Activity" (check-in/book)
        // Dan kita bisa tambahkan Transaction log jika perlu, tapi widget Recent Activity terbatas.
        // Mari kita biarkan Recent Activity menampilkan Booking untuk saat ini, atau kita bisa mix.
        
        val activities = transactions.sortedByDescending { it.dateMillis }
            .take(5)
            .map { transaction ->
                RecentActivity(
                    id = transaction.id,
                    userInitial = getInitials(transaction.userName),
                    user = transaction.userName.ifBlank { "Unknown User" },
                    action = transaction.type, // e.g. "Elite Membership"
                    time = formatTimeAgo(transaction.dateMillis),
                    amount = "+${formatCurrency(transaction.amount)}"
                )
            }
        _recentActivities.value = activities

        // --- CALCULATION FOR REPORTS ---
        // 1. Monthly Revenue (Dari Transactions)
        val dateFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val monthlyMap = transactions.groupBy { dateFormat.format(Date(it.dateMillis)) }
            .map { (month, list) ->
                MonthlyData(month, list.sumOf { it.amount } / 1000000.0) // Dalam Juta (Double)
            }

        // 2. Top Classes (by Booking Count only)
        val topClassMap = bookings.groupBy { it.gymClassTitle }
            .map { (name, list) ->
                TopClassData(
                    name = name.ifBlank { "Unknown Class" },
                    bookings = list.size,
                    revenue = "" // Removed usage
                )
            }.sortedByDescending { it.bookings }.take(5)

        // 3. Membership Stats
        val totalMembers = members.size
        // Ensure we always have entries for Basic, Premium, Elite
        val categories = listOf("Elite", "Premium", "Basic")
        val statsMap = members.groupBy { 
             // Normalize DB value to match our categories
             val raw = it.membershipType.ifBlank { "Basic" }
             // Handle potential case casing or "Standard" -> "Basic"
             if(raw.equals("Standard", true)) "Basic" else raw
        }
        
        val membershipMap = categories.map { type ->
            // Match case insensitive
            val matchingList = statsMap.entries.firstOrNull { it.key.equals(type, ignoreCase = true) }?.value ?: emptyList()
            val count = matchingList.size
            val percentage = if (totalMembers > 0) count.toFloat() / totalMembers else 0f
            
            MembershipData(
                type = type,
                count = count,
                percentage = percentage,
                color = when(type) {
                    "Elite" -> Color(0xFFEAB308) // Gold
                    "Premium" -> Color(0xFFC0C0C0) // Silver
                    else -> Color(0xFFCD7F32)    // Bronze (Basic)
                }
            )
        }

        _reportState.update {
            it.copy(
                monthlyRevenue = monthlyMap,
                topClasses = topClassMap,
                membershipStats = membershipMap
            )
        }
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

    private fun formatTimeAgo(timeMillis: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timeMillis
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "$minutes mins ago"
            hours < 24 -> "$hours hours ago"
            else -> "$days days ago"
        }
    }
}