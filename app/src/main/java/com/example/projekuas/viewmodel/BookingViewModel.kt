package com.example.projekuas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekuas.data.BookingDetail
import com.example.projekuas.data.BookingState
import com.example.projekuas.data.GymClass
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar



class BookingViewModel(/* private val bookingRepository: BookingRepository */) : ViewModel() {

    private val _state = MutableStateFlow(BookingState())
    val state: StateFlow<BookingState> = _state

    init {
        // Muat jadwal untuk hari ini saat ViewModel dibuat
        loadClassesForDate(_state.value.selectedDateMillis)
        loadUpcomingBookings()
    }

    fun onDateSelected(dateMillis: Long) {
        _state.update { it.copy(selectedDateMillis = dateMillis) }
        loadClassesForDate(dateMillis)
    }

    private fun loadClassesForDate(dateMillis: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, classesToday = emptyList(), error = null) }

            // --- SIMULASI PENGAMBILAN JADWAL KELAS ---
            kotlinx.coroutines.delay(1000)

            // Data Simulasi
            val dummyClasses = listOf(
                GymClass(
                    classId = "C001", name = "Yoga Pagi", description = "Yoga Vinyasa",
                    trainerName = "Luna", durationMinutes = 60, startTimeMillis = dateMillis + (8 * 3600 * 1000),
                    capacity = 20, currentBookings = 15
                ),
                GymClass(
                    classId = "C002", name = "Zumba Power", description = "Kardio Intens",
                    trainerName = "Dito", durationMinutes = 45, startTimeMillis = dateMillis + (18 * 3600 * 1000),
                    capacity = 15, currentBookings = 15, isAvailable = false // Penuh
                )
            )

            _state.update {
                it.copy(
                    classesToday = dummyClasses,
                    isLoading = false
                )
            }
        }
    }

    private fun loadUpcomingBookings() {
        // Logika untuk memuat booking yang sudah dilakukan user
        // (Akan diimplementasikan lebih lanjut)
    }

    fun bookClass(classId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isBookingInProgress = true, error = null) }

            // --- LOGIKA BOOKING (HARUS MENGGUNAKAN TRANSACTION NANTI) ---
            kotlinx.coroutines.delay(1500)

            // Simulasi Berhasil
            val newBooking = BookingDetail(
                bookingId = "B-123", classId = classId, memberId = "M-999",
                bookingTimeMillis = System.currentTimeMillis(), qrCodeContent = "QR-${classId}"
            )

            _state.update {
                it.copy(
                    isBookingInProgress = false,
                    // Tambahkan notifikasi sukses atau navigasi
                )
            }
            // Di sini Anda akan mengupdate database dan memicu notifikasi pengingat
        }
    }
}