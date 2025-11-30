package com.example.projekuas.data
import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface ClassRepository {
    fun getAllClassesStream(): Flow<List<GymClass>>
    // Tambahkan fungsi untuk booking di sini:
    suspend fun bookClass(classId: String, userId: String)
    suspend fun createClass(gymClass: GymClass) // Sudah ada
    suspend fun updateClass(gymClass: GymClass) // FIX: Tambahkan
    suspend fun deleteClass(classId: String)     // FIX: Tambahkan
    suspend fun getClassById(classId: String): GymClass? // Tambahkan ini
    suspend fun getClassesForTrainer(trainerId: String): List<GymClass>
    fun getClassParticipantsStream(classId: String): Flow<List<Booking>>
    fun getUserBookedClassIds(userId: String): Flow<Set<String>>
    suspend fun processClassImage(uri: Uri): String?
    suspend fun cancelBooking(classId: String, userId: String)
    suspend fun updateBookingDetails(classId: String, bookingId: String, attendance: String, feedback: String, rating: Int)
    suspend fun getBookingId(classId: String, userId: String): String?
    suspend fun findBookingDocument(classId: String, userId: String): com.google.firebase.firestore.QuerySnapshot
    suspend fun submitRating(bookingId: String, trainerId: String, rating: Int, review: String)
}

// File: com/example.projekuas.data.ClassRepositoryImpl.kt (Implementasi)