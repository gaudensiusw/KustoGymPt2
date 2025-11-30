package com.example.projekuas.data

data class BookingDetail(
    val bookingId: String,
    val classId: String, // Foreign Key ke GymClass
    val memberId: String,
    val bookingTimeMillis: Long,
    val checkInStatus: Boolean = false, // Untuk QR Code Check-in
    val qrCodeContent: String // Konten yang akan di-encode ke QR Code
)