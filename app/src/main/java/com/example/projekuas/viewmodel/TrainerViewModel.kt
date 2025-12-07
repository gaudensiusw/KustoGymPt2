package com.example.projekuas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekuas.data.AuthRepository
import com.example.projekuas.data.ClassRepository
import com.example.projekuas.data.GymClass
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.Calendar
import com.example.projekuas.data.*
import java.util.Date
import java.time.Instant
import java.time.ZoneId
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.time.LocalDate

// State Khusus untuk Halaman Members
data class MemberStatItem(
    val memberId: String,
    val name: String,
    val email: String,
    val profilePictureUrl: String,
    val totalClasses: Int,
    val attendanceRate: Double,
    val joinDate: Long, // Member Since date (dari UserProfile)
    val lastClassDate: Long, // Tanggal kelas terakhir yang diambil
    val progressRating: Double, // Rata-rata rating
    val activeStatus: String // "Active", "Inactive"
)

data class ExtendedBooking(
    val bookingId: String,
    val classId: String,
    val userId: String,
    val classTimeMillis: Long, // Menggunakan Class Time sebagai referensi Last Class
    val attendanceStatus: String = "Pending",
    val progressRating: Int = 0
)

// State yang dibutuhkan Dashboard Trainer (MODIFIED)
data class TrainerUiState(
    val classesTaught: List<GymClass> = emptyList(),
    val totalClasses: Int = 0,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val selectedClassToEdit: GymClass? = null,
    val filteredClasses: List<GymClass> = emptyList(), // Kelas sesuai tanggal dipilih
    val selectedDate: Long = System.currentTimeMillis(), // Tanggal yang dipilih (default hari ini)
    val classesThisWeek: Int = 0,
    val totalMembersThisWeek: Int = 0,
    val totalHoursThisWeek: Int = 0,
    val avgParticipants: Double = 0.0, // Rata-rata peserta per kelas
    val attendanceRate: Double = 0.0,  // Persentase (Bookings / Capacity)
    val trainerName: String = "Trainer",
    val classesTodayCount: Int = 0,
    val activeMembersCount: Int = 0, // Unique members
    val classesThisMonthCount: Int = 0,
    val performanceRate: Double = 0.0, // Attendance Rate sebagai proxy Satisfaction
    val myMembers: List<MemberStatItem> = emptyList(),
    val rating: Double = 0.0, // NEW: Bintang
    val ratingCount: Int = 0,  // NEW: Jumlah Review
    val reviews: List<Booking> = emptyList() // NEW: Daftar Review
)

// Helper Class untuk hasil perhitungan
data class WeeklyStats(
    val count: Int,
    val hours: Int,
    val avg: Double,
    val rate: Double
)

class TrainerViewModel(
    private val classRepository: ClassRepository,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository // Inject ProfileRepo
) : ViewModel() {

    private val trainerId = authRepository.getCurrentUserId() ?: ""
    private val trainerNamePlaceholder = "Trainer"

    // Gunakan 'trainerName' dari State Flow untuk UI
    private val _uiState = MutableStateFlow(TrainerUiState())
    val uiState: StateFlow<TrainerUiState> = _uiState.asStateFlow()
    private val refreshTrigger = MutableStateFlow(Unit)


    init {
        fetchTrainerProfile()
        observeReviews() // Start observing reviews

        viewModelScope.launch {
            refreshTrigger
                .onStart { emit(Unit) }
                .flatMapLatest {
                    _uiState.update { it.copy(isLoading = true) }
                    classRepository.getAllClassesStream()
                }
                .map { allClasses ->
                    val myClasses = allClasses.filter { it.trainerId == trainerId }
                    // ... (Kalkulasi Statistik Dasar) ...
                    val todayCount = calculateClassesToday(myClasses)
                    val monthCount = calculateClassesThisMonth(myClasses)
                    val perfRate = calculatePerformanceRate(myClasses)
                    val weeklyStats = calculateWeeklyStats(myClasses)

                    Triple(myClasses, Triple(todayCount, monthCount, perfRate), weeklyStats)
                }
                .collect { (myClasses, dashboardStats, weeklyStats) ->
                    _uiState.update {
                        it.copy(
                            classesTaught = myClasses,
                            filteredClasses = filterClassesByDate(myClasses, it.selectedDate),
                            classesTodayCount = dashboardStats.first,
                            classesThisMonthCount = dashboardStats.second,
                            performanceRate = dashboardStats.third,
                            classesThisWeek = weeklyStats.count,
                            totalHoursThisWeek = weeklyStats.hours,
                            avgParticipants = weeklyStats.avg,
                            attendanceRate = weeklyStats.rate,
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                    loadMyMembersData(myClasses)
                }
        }
    }

    private fun fetchTrainerProfile() {
        viewModelScope.launch {
            try {
                val profile = profileRepository.getLoggedInUserProfile().first()
                // FIX: Pastikan nama trainer disimpan
                val name = profile.name.ifBlank { trainerNamePlaceholder }
                _uiState.update { 
                    it.copy(
                        trainerName = name,
                        rating = profile.averageRating, // Load Rating
                        ratingCount = profile.ratingCount
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(trainerName = trainerNamePlaceholder) }
            }
        }
    }

    private fun observeReviews() {
        viewModelScope.launch {
            if (trainerId.isNotBlank()) {
                classRepository.getTrainerReviews(trainerId).collect { reviews ->
                    _uiState.update { it.copy(reviews = reviews) }
                }
            }
        }
    }

    // --- LOGIC CALCULATOR ---

    private fun calculateClassesToday(list: List<GymClass>): Int {
        val today = Calendar.getInstance()
        return list.count {
            val c = Calendar.getInstance().apply { timeInMillis = it.startTimeMillis }
            c.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
                    c.get(Calendar.YEAR) == today.get(Calendar.YEAR)
        }
    }

    private fun calculateClassesThisMonth(list: List<GymClass>): Int {
        val today = Calendar.getInstance()
        return list.count {
            val c = Calendar.getInstance().apply { timeInMillis = it.startTimeMillis }
            c.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    c.get(Calendar.YEAR) == today.get(Calendar.YEAR)
        }
    }

    private fun calculatePerformanceRate(list: List<GymClass>): Double {
        // Rata-rata kehadiran (Current Bookings / Capacity)
        val finishedClasses = list.filter { it.capacity > 0 }
        if (finishedClasses.isEmpty()) return 0.0

        val totalRate = finishedClasses.sumOf { (it.currentBookings.toDouble() / it.capacity) * 100 }
        return totalRate / finishedClasses.size
    }

    // --- LOGIC MY MEMBERS (AGGREGATION) ---
    private fun loadMyMembersData(myClasses: List<GymClass>) {
        viewModelScope.launch {
            if (myClasses.isEmpty()) {
                _uiState.update { it.copy(myMembers = emptyList(), totalMembersThisWeek = 0, activeMembersCount = 0) }
                return@launch
            }

            // 1. Map untuk menyimpan List Booking per MemberID (userId)
            val memberBookingsMap = mutableMapOf<String, MutableList<ExtendedBooking>>()

            // 2. Ambil booking dari setiap kelas
            myClasses.forEach { gymClass ->
                try {
                    // Ambil booking real-time (Asumsi ini mengembalikan List<Booking> dari subcollection/koleksi)
                    val bookings = classRepository.getClassParticipantsStream(gymClass.classId).first()

                    bookings.forEach { booking ->
                        // PERBAIKAN: Hanya proses jika userId tidak kosong
                        val userId = booking.userId

                        if (userId.isNotBlank()) {
                            if (!memberBookingsMap.containsKey(userId)) {
                                memberBookingsMap[userId] = mutableListOf()
                            }

                            // FIX: Membuat ExtendedBooking untuk menyertakan Class Time
                            val extendedBooking = ExtendedBooking(
                                bookingId = booking.bookingId,
                                classId = booking.classId,
                                userId = userId,
                                classTimeMillis = gymClass.startTimeMillis, // INJECT Class Time di sini
                                attendanceStatus = if (booking.isCheckedIn) "Present" else "Pending",
                                progressRating = booking.progressRating
                            )
                            memberBookingsMap[userId]?.add(extendedBooking)
                        } else {
                            android.util.Log.w("DATA_INTEGRITY", "Skipping booking with empty userId in class: ${gymClass.classId}. Check Firebase.")
                        }
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }

            // 3. Deklarasi dan Isi MemberStats DENGAN DATA PROFILE
            val memberStats = mutableListOf<MemberStatItem>()

            memberBookingsMap.forEach { (memberId, bookings) ->
                try {
                    // Pastikan memberId valid
                    if (memberId.isBlank()) return@forEach

                    val profile = profileRepository.getUserProfile(memberId).first()

                    // FIX NAMA: Prioritaskan Nama Asli > Username > Email
                    val realName = when {
                        profile.name.isNotBlank() -> profile.name
                        profile.username.isNotBlank() -> profile.username
                        else -> profile.email.substringBefore("@").ifBlank { "Member ($memberId)" }
                    }

                    // Hitung Attendance Rate (Booking dengan status 'Present')
                    val presentCount = bookings.count { it.attendanceStatus == "Present" }
                    val finishedClasses = bookings.count { it.classTimeMillis < System.currentTimeMillis() } // Kelas yang sudah berakhir

                    val attendanceRate = if (finishedClasses > 0) {
                        (presentCount.toDouble() / finishedClasses) * 100
                    } else {
                        100.0 // Anggap 100% jika belum ada kelas yang selesai/diabsen
                    }

                    // Hitung Avg Rating
                    val ratedBookings = bookings.filter { it.progressRating > 0 }
                    val avgRating = if (ratedBookings.isNotEmpty()) {
                        ratedBookings.sumOf { it.progressRating }.toDouble() / ratedBookings.size
                    } else {
                        0.0
                    }

                    // Mencari nilai Long (classTimeMillis) maksimum
                    val lastClass = if (bookings.isNotEmpty()) {
                        bookings.maxOf { it.classTimeMillis }
                    } else {
                        0L
                    }

                    // Pastikan profile.joinDate ada, jika tidak, pakai System.currentTimeMillis()
                    val memberJoinDate = if (profile.joinDate > 0) profile.joinDate else System.currentTimeMillis()

                    memberStats.add(MemberStatItem(
                        memberId = memberId,
                        name = realName,
                        email = profile.email,
                        profilePictureUrl = profile.profilePictureUrl,
                        totalClasses = bookings.size,
                        attendanceRate = attendanceRate,
                        joinDate = memberJoinDate,
                        lastClassDate = lastClass,
                        progressRating = avgRating,
                        activeStatus = if (lastClass > System.currentTimeMillis() - (30L * 24 * 3600 * 1000)) "Active" else "Inactive" // Anggota aktif dalam 30 hari terakhir
                    ))
                } catch (e: Exception) {
                    android.util.Log.e("TrainerViewModel", "Failed to process profile for $memberId: ${e.message}")
                    e.printStackTrace()
                }
            }

            // 4. Update UI State
            val totalMembers = memberStats.size
            val avgAttendanceGlobal = if (totalMembers > 0) memberStats.sumOf { it.attendanceRate } / totalMembers else 0.0

            val oneWeekAgo = System.currentTimeMillis() - (7L * 24 * 3600 * 1000)
            val activeCount = memberStats.count { it.lastClassDate > oneWeekAgo }

            _uiState.update {
                it.copy(
                    myMembers = memberStats.sortedByDescending { m -> m.lastClassDate },
                    totalMembersThisWeek = totalMembers,
                    avgParticipants = avgAttendanceGlobal,
                    activeMembersCount = activeCount,
                    isLoading = false
                )
            }
        }
    }


    // --- LOGIC BARU: Ganti Tanggal ---
    fun onDateSelected(dateMillis: Long) {
        _uiState.update {
            it.copy(
                selectedDate = dateMillis,
                filteredClasses = filterClassesByDate(it.classesTaught, dateMillis)
            )
        }
    }

    // Helper: Filter List by Date (Perbaikan agar tidak terjadi error saat list kosong)
    private fun filterClassesByDate(list: List<GymClass>, dateMillis: Long): List<GymClass> {
        if (list.isEmpty()) return emptyList()
        val selectedCal = Calendar.getInstance().apply { timeInMillis = dateMillis }
        return list.filter {
            val classCal = Calendar.getInstance().apply { timeInMillis = it.startTimeMillis }
            classCal.get(Calendar.YEAR) == selectedCal.get(Calendar.YEAR) &&
                    classCal.get(Calendar.DAY_OF_YEAR) == selectedCal.get(Calendar.DAY_OF_YEAR)
        }.sortedBy { it.startTimeMillis }
    }

    // Helper: Hitung Stats (Classes, Members, Hours)
    private fun calculateWeeklyStats(list: List<GymClass>): WeeklyStats {
        // ... (Tidak ada perubahan signifikan pada fungsi ini)
        val now = System.currentTimeMillis()
        val oneWeekFuture = now + (7L * 24 * 3600 * 1000)

        val weeklyClasses = list.filter { it.startTimeMillis in now..oneWeekFuture }

        val count = weeklyClasses.size

        if (count == 0) {
            return WeeklyStats(0, 0, 0.0, 0.0)
        }

        val totalHours = weeklyClasses.sumOf { it.durationMinutes } / 60
        val totalBookings = weeklyClasses.sumOf { it.currentBookings }
        val totalCapacity = weeklyClasses.sumOf { it.capacity }

        val avg = totalBookings.toDouble() / count

        val rate = if (totalCapacity > 0) {
            (totalBookings.toDouble() / totalCapacity) * 100
        } else {
            0.0
        }

        return WeeklyStats(count, totalHours, avg, rate)
    }

    // --- LOGIC BARU: VALIDASI UNTUK TAMBAH/EDIT KELAS ---

    private val operatingHoursStart = 7  // 07:00 AM
    private val operatingHoursEnd = 22   // 10:00 PM (22:00)

    /**
     * Memvalidasi apakah tanggal yang dipilih valid (Hari ini atau masa depan)
     * dan berada dalam jam operasional.
     */
    fun validateClassTime(dateMillis: Long): String? {
        val selectedDateTime = Instant.ofEpochMilli(dateMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()
        val selectedDate = selectedDateTime.toLocalDate()
        val selectedTime = selectedDateTime.toLocalTime()
        val currentDate = LocalDate.now()
        val currentTime = LocalTime.now()

        // 1. Validasi Tanggal (Tidak boleh masa lalu)
        if (selectedDate.isBefore(currentDate)) {
            return "Tidak dapat membuat jadwal di masa lalu."
        }

        // 2. Validasi Jam Operasional (07:00 - 22:00)
        val startOperatingTime = LocalTime.of(operatingHoursStart, 0) // 07:00
        val endOperatingTime = LocalTime.of(operatingHoursEnd, 0) // 22:00

        if (selectedTime.isBefore(startOperatingTime) || selectedTime.isAfter(endOperatingTime)) {
            return "Jadwal harus berada dalam jam operasional ($operatingHoursStart:00 - $operatingHoursEnd:00)."
        }

        // 3. Validasi Jam di hari yang sama (Tidak boleh kurang dari sekarang)
        if (selectedDate.isEqual(currentDate) && selectedTime.isBefore(currentTime.truncatedTo(ChronoUnit.MINUTES))) {
            return "Tidak dapat membuat jadwal di jam yang sudah lewat hari ini."
        }

        return null // Valid
    }

    // --- FUNGSI CRUD KELAS (MODIFIED UNTUK MENGGUNAKAN VALIDASI) ---

    fun addClass(name: String, description: String, dateMillis: Long, duration: Int, capacity: Int) {
        if (trainerId.isBlank()) return

        // Lakukan Validasi Waktu
        val validationError = validateClassTime(dateMillis)
        if (validationError != null) {
            _uiState.update { it.copy(error = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, successMessage = null) }

            val newClass = GymClass(
                classId = UUID.randomUUID().toString(),
                name = name,
                description = description,
                trainerName = _uiState.value.trainerName, // Ganti dengan nama Trainer asli
                durationMinutes = duration,
                startTimeMillis = dateMillis,
                capacity = capacity,
                currentBookings = 0,
                isAvailable = true
            )

            try {
                classRepository.createClass(newClass)
                _uiState.update { it.copy(isSaving = false, successMessage = "Kelas berhasil ditambahkan!") }

            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "Gagal menyimpan kelas: ${e.message}") }
            }
        }
    }

    // 3. UPDATE: Fungsi Edit Kelas
    fun updateClass(gymClass: GymClass) {

        // Lakukan Validasi Waktu
        val validationError = validateClassTime(gymClass.startTimeMillis)
        if (validationError != null) {
            _uiState.update { it.copy(error = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                classRepository.updateClass(gymClass.copy(trainerName = _uiState.value.trainerName)) // Update nama trainer jika berubah
                _uiState.update { it.copy(isSaving = false, successMessage = "Kelas berhasil diperbarui!", selectedClassToEdit = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "Gagal memperbarui kelas: ${e.message}") }
            }
        }
    }

    // ... (Fungsi deleteClass, selectClassForEditing, clearSelectedClass, refreshData, clearMessages tetap sama)
    fun deleteClass(classId: String) {
        viewModelScope.launch {
            try {
                classRepository.deleteClass(classId)
                refreshData()
                _uiState.update { it.copy(successMessage = "Kelas berhasil dihapus!") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Gagal menghapus kelas: ${e.message}") }
            }
        }
    }

    fun selectClassForEditing(gymClass: GymClass) {
        _uiState.update {
            it.copy(selectedClassToEdit = gymClass)
        }
    }

    fun clearSelectedClass() {
        _uiState.update { it.copy(selectedClassToEdit = null) }
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            refreshTrigger.emit(Unit)
            try {
                val classes = classRepository.getClassesForTrainer(trainerId)

                _uiState.update {
                    it.copy(
                        classesTaught = classes,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}