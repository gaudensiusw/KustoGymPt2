package com.example.projekuas.data

import com.google.firebase.firestore.PropertyName

data class Booking(
    val bookingId: String = "",
    val classId: String = "",

    @get:PropertyName("memberId")
    @set:PropertyName("memberId")
    var userId: String = "",

    val userName: String = "",

    @get:PropertyName("bookingTimeMillis")
    @set:PropertyName("bookingTimeMillis")
    var bookingTimeMillis: Long = 0L,

    val status: String = "Confirmed",

    // --- PERBAIKAN DI SINI ---
    // Ubah tipe data menjadi Boolean sesuai database
    @get:PropertyName("checkInStatus")
    @set:PropertyName("checkInStatus")
    var isCheckedIn: Boolean = false,
    // -------------------------

    @get:PropertyName("qrCodeContent")
    @set:PropertyName("qrCodeContent")
    var qrCodeContent: String = "",

    val trainerFeedback: String = "",
    val progressRating: Int = 0,
    val rating: Int = 0, // 0 = Belum dirating, 1-5 = Rating
    val review: String = "", // Ulasan teks (opsional)
    val trainerId: String = "", // PENTING: Kita butuh ID trainer di sini untuk update statistik

    val gymClassTitle: String = "", // Judul kelas (opsional)
    val gymClassDescription: String = "", // Deskripsi kelas (opsional
    val price: Double = 150000.0,
    val ratingTimestamp: Long = 0L // Timestamp when rating was submitted
)