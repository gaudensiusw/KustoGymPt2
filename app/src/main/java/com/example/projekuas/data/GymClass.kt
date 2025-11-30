package com.example.projekuas.data

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class GymClass(
    val classId: String = "",
    val name: String = "",
    val description: String = "",
    val trainerName: String = "",
    val trainerId: String = "", // <--- TAMBAHKAN INI (PENTING!)
    val durationMinutes: Int = 0,
    val startTimeMillis: Long = 0L,
    val capacity: Int = 0,
    val currentBookings: Int = 0,
    val imageUrl: String = "",
    val rating: Int = 0,    // Tambahkan ini agar ClassItemCard bisa memeriksa rating
    val review: String = "", // Ulasan
    val isRated: Boolean = false, // Opsional, tapi mempermudah UI

    // FIX 1: Mapping nama field. Di database namanya "available", di sini "isAvailable"
    @get:PropertyName("available")
    @set:PropertyName("available")
    var isAvailable: Boolean = true
) {
    @get:Exclude
    // FIX 2: Tambahkan @Exclude agar Firestore MENGABAIKAN ini (tidak dianggap error)
    val timeString: String
        get() {
            // Gunakan SimpleDateFormat untuk mengonversi milidetik ke HH:mm
            return try {
                val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                formatter.format(Date(startTimeMillis))
            } catch (e: Exception) {
                // Fallback jika startTimeMillis invalid (misal 0 atau null)
                "N/A"
            }
        }
}