package com.example.projekuas.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekuas.ui.dashboard.MembershipData
import com.example.projekuas.ui.dashboard.MonthlyData
import com.example.projekuas.ui.dashboard.TopClassData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// -----------------------------------------------------
// 1. UI STATE CLASS (Untuk menampung semua data laporan)
// -----------------------------------------------------

data class AdminReportsUiState(
    val isLoading: Boolean = true,
    val totalRevenue: Double = 0.0,
    val activeMembers: Int = 0,
    val classesThisMonth: Int = 0,
    val avgAttendance: Int = 0,
    val maxRevenue: Double = 0.0,
    val monthlyRevenueData: List<MonthlyData> = emptyList(),
    val topClasses: List<TopClassData> = emptyList(),
    val membershipBreakdown: List<MembershipData> = emptyList()
)

// -----------------------------------------------------
// 2. VIEWMODEL CLASS
// -----------------------------------------------------

class ReportViewModel(
    // Anda bisa inject Repository di sini, contoh: private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminReportsUiState())
    val uiState: StateFlow<AdminReportsUiState> = _uiState.asStateFlow()

    init {
        loadReportData()
    }

    private fun loadReportData() {
        // Melakukan fetch data dari repository di sini
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // --- DATA DUMMY (Ganti dengan data asli dari database/API) ---
            val monthlyData = listOf(
                MonthlyData("Jul", 5_000_000),
                MonthlyData("Agu", 7_500_000),
                MonthlyData("Sep", 12_000_000),
                MonthlyData("Okt", 15_000_000),
                MonthlyData("Nov", 18_000_000)
            )

            val maxRev = monthlyData.maxOfOrNull { it.revenue }?.toDouble() ?: 0.0

            val totalMembers = 500
            val breakdown = listOf(
                MembershipData("Gold", 100, 100f / totalMembers, Color(0xFFFACC15)),
                MembershipData("Silver", 250, 250f / totalMembers, Color(0xFF9CA3AF)),
                MembershipData("Bronze", 150, 150f / totalMembers, Color(0xFFC45A3C))
            )

            _uiState.value = AdminReportsUiState(
                isLoading = false,
                totalRevenue = 85_000_000.0,
                activeMembers = 500,
                classesThisMonth = 35,
                avgAttendance = 78,
                maxRevenue = maxRev,
                monthlyRevenueData = monthlyData,
                topClasses = listOf(
                    TopClassData("Yoga Flow", 75, "Rp 15.0M"),
                    TopClassData("HIIT Extreme", 62, "Rp 12.4M"),
                    TopClassData("Zumba Dance", 55, "Rp 11.0M")
                ),
                membershipBreakdown = breakdown
            )
        }
    }

    // Fungsi lain untuk memfilter periode laporan bisa ditambahkan di sini
}