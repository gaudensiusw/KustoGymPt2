package com.example.projekuas.data

import java.util.Calendar

data class BookingState(
    val selectedDateMillis: Long = Calendar.getInstance().timeInMillis, // Tanggal yang sedang dilihat
    val classesToday: List<GymClass> = emptyList(), // Daftar kelas untuk tanggal yang dipilih
    val upcomingBookings: List<BookingDetail> = emptyList(), // Booking yang akan datang
    val isLoading: Boolean = false,
    val error: String? = null,
    val isBookingInProgress: Boolean = false
)