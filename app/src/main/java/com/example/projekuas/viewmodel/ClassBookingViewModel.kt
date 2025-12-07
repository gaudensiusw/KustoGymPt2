package com.example.projekuas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekuas.data.AuthRepository
import com.example.projekuas.data.ClassRepository
import com.example.projekuas.data.GymClass
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import com.google.firebase.firestore.QuerySnapshot // <-- FIX: Import ini diperlukan untuk type safety

import com.example.projekuas.data.ClassBookingState

// Data class State UI moved to com.example.projekuas.data.ClassBookingState

class ClassBookingViewModel(
    private val classRepository: ClassRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUserId: String = authRepository.getCurrentUserId() ?: ""

    // Channel untuk Notifikasi Toast
    private val _bookingEventChannel = Channel<String>()
    val bookingEvent = _bookingEventChannel.receiveAsFlow()

    // State Internal
    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    private val _searchQuery = MutableStateFlow("")
    private val _isRefreshing = MutableStateFlow(false)

    // [CORE LOGIC] Menggabungkan semua data secara Realtime
    val state: StateFlow<ClassBookingState> = combine(
        classRepository.getAllClassesStream(),      // 1. Data Kelas (Realtime)
        classRepository.getUserBookingsStream(currentUserId), // 2. Data Booking User (Realtime) - FIX: Get Full Objects
        _selectedDate,                              // 3. Filter Tanggal
        _searchQuery,                               // 4. Filter Search
        _isRefreshing                               // 5. Status Refresh
    ) { classes, bookings, date, query, refreshing ->

        val bookingsMap = bookings.associateBy { it.classId }
        val bookedIds = bookingsMap.keys

        val filteredClasses = classes.filter { gymClass ->
            val isDateMatch = isSameDay(gymClass.startTimeMillis, date)
            val isSearchMatch = if (query.isBlank()) true else {
                gymClass.name.contains(query, ignoreCase = true) || gymClass.trainerName.contains(query, ignoreCase = true)
            }
            isDateMatch && isSearchMatch
        }.sortedBy { it.startTimeMillis }

        // Inject Booking Data into GymClass for My Bookings
        val myBookedList = classes
            .filter { it.classId in bookedIds }
            .map { gymClass ->
                val booking = bookingsMap[gymClass.classId]
                if (booking != null) {
                    gymClass.copy(
                        rating = booking.rating,
                        ratingTimestamp = booking.ratingTimestamp,
                        isRated = booking.rating > 0 // Or booking.isRated
                    )
                } else {
                    gymClass
                }
            }
            .sortedBy { it.startTimeMillis }

        // Ambil semua tanggal unik dari daftar kelas mentah (sebelum filter tanggal)
        val allClassDates = classes.map { it.startTimeMillis }.toSet()

        ClassBookingState(
            classes = filteredClasses,
            myBookings = myBookedList,
            bookedClassIds = bookedIds,
            isLoading = false,
            isRefreshing = refreshing,
            selectedDate = date,
            searchQuery = query,
            classDates = allClassDates
        )
    }
        .catch { e ->
            emit(ClassBookingState(error = e.message, isLoading = false))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ClassBookingState(isLoading = true)
        )

    // --- USER ACTIONS ---

    fun onDateSelected(newDate: Long) {
        _selectedDate.value = newDate
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(1000)
            _isRefreshing.value = false
        }
    }

    // Fungsi Booking
    fun onBookClass(classId: String) {
        if (currentUserId.isBlank()) return

        viewModelScope.launch {
            try {
                val currentBooked = state.value.bookedClassIds
                if (currentBooked.contains(classId)) {
                    _bookingEventChannel.send("Anda sudah terdaftar di kelas ini.")
                    return@launch
                }

                classRepository.bookClass(classId, currentUserId)
                _bookingEventChannel.send("Berhasil mendaftar kelas!")
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Gagal booking."
                _bookingEventChannel.send("Error: $errorMsg")
            }
        }
    }

    fun onCancelBooking(classId: String) {
        if (currentUserId.isBlank()) return

        viewModelScope.launch {
            try {
                classRepository.cancelBooking(classId, currentUserId)
                _bookingEventChannel.send("Booking berhasil dibatalkan!")
            } catch (e: Exception) {
                _bookingEventChannel.send("Gagal membatalkan: ${e.message}")
            }
        }
    }

    // [FINAL IMPLEMENTASI] Fungsi Member Memberi Rating
    fun giveRating(gymClass: GymClass, rating: Int, review: String) {
        if (currentUserId.isBlank() || gymClass.trainerName.isBlank() || rating == 0) return

        viewModelScope.launch {
            try {
                // 1. Cari ID dokumen Booking spesifik
                val bookingSnapshot: QuerySnapshot = classRepository.findBookingDocument(gymClass.classId, currentUserId)

                if (bookingSnapshot.isEmpty) {
                    _bookingEventChannel.send("Error: Booking tidak ditemukan untuk kelas ini.")
                    return@launch
                }

                // 2. Mengambil ID dokumen Booking (FIX: Error 'id' teratasi karena menggunakan .documents.first().id)
                val bookingId = bookingSnapshot.documents.first().id

                // 3. Lakukan Transaksi Rating
                // Asumsi field trainerId ada di model GymClass, jika tidak, Anda harus mencarinya dulu.
                classRepository.submitRating(
                    bookingId,
                    gymClass.trainerId, // Menggunakan trainerId dari GymClass
                    rating,
                    review
                )

                _bookingEventChannel.send("Rating berhasil dikirim!")
            } catch (e: Exception) {
                _bookingEventChannel.send("Gagal mengirim rating: ${e.message}")
            }
        }
    }

    // [HELPER] Cek Kesamaan Hari
    private fun isSameDay(millis1: Long, millis2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = millis1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = millis2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}