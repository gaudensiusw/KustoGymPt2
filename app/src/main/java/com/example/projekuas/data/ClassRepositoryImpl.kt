package com.example.projekuas.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class ClassRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val context: Context // TAMBAHAN: Butuh context untuk baca Uri
) : ClassRepository {

    // --- HELPER: Kompresi Gambar ke Base64 (Mirip ProfileRepository) ---
    override suspend fun processClassImage(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val originalBitmap =
                    BitmapFactory.decodeStream(inputStream) ?: return@withContext null
                inputStream?.close()

                // Resize ke max 800px (Cukup untuk cover, jangan terlalu besar untuk Firestore)
                val scaledBitmap = getResizedBitmap(originalBitmap, 800)

                val outputStream = ByteArrayOutputStream()
                // Kompres JPG 60% quality
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
                val byteArrays = outputStream.toByteArray()

                val base64String = Base64.encodeToString(byteArrays, Base64.DEFAULT)
                "data:image/jpeg;base64,$base64String"
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap {
        var width = image.width
        var height = image.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    // Implementasi untuk mengambil semua kelas secara real-time
    override fun getAllClassesStream(): Flow<List<GymClass>> = callbackFlow {
        val classCollection = firestore.collection("classes")

        val subscription = classCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val classes = snapshot.toObjects(GymClass::class.java)
                trySend(classes).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun bookClass(classId: String, userId: String) {
        val classRef = firestore.collection("classes").document(classId)
        val userRef = firestore.collection("users").document(userId)
        val bookingId = "${classId}_${userId}"
        val bookingRef = firestore.collection("bookings").document(bookingId)

        // Gunakan Transaksi untuk mencegah Overbooking
        firestore.runTransaction { transaction ->
            val classSnapshot = transaction.get(classRef)
            val userSnapshot = transaction.get(userRef)
            val bookingSnapshot = transaction.get(bookingRef) // Cek keberadaan dokumen
            //val snapshot = transaction.get(classRef)

            // 1. Validasi Keberadaan Data
            if (!classSnapshot.exists()) {
                throw FirebaseFirestoreException(
                    "Kelas tidak ditemukan.",
                    FirebaseFirestoreException.Code.ABORTED
                )
            }

            // 2. [ATURAN 2] Cek Role User (Trainer tidak boleh booking)
            val role = userSnapshot.getString("role") ?: "Member"
            // Ambil Nama User untuk disimpan di Booking
            val userName = userSnapshot.getString("name") ?: "Unknown User"

            if (role == "Trainer") {
                throw FirebaseFirestoreException(
                    "Trainer tidak diperbolehkan mendaftar kelas.",
                    FirebaseFirestoreException.Code.ABORTED
                )
            }

            // ... (validation code)

            // 4. Catat Siapa yang Booking (Buat dokumen baru di koleksi 'bookings')
            val bookingData = hashMapOf(
                "bookingId" to bookingId,
                "classId" to classId,
                "userId" to userId,
                "userName" to userName,
                "trainerId" to (classSnapshot.getString("trainerId") ?: ""), // FIX: Save trainerId for reviews
                "bookingTimeMillis" to System.currentTimeMillis(),
                "checkInStatus" to false,
                "qrCodeContent" to bookingId // Format simple untuk QR
            )

            transaction.set(bookingRef, bookingData)
        }.await() // Tunggu transaksi selesai
    }

    override suspend fun createClass(gymClass: GymClass) {
        firestore.collection("classes")
            .document(gymClass.classId)
            .set(gymClass)
            .await()
    }

    override suspend fun updateClass(gymClass: GymClass) {
        // set(gymClass) akan menimpa seluruh dokumen dengan data GymClass baru
        firestore.collection("classes").document(gymClass.classId).set(gymClass).await()
    }

    // FIX 2: Implementasi Hapus Kelas
    override suspend fun deleteClass(classId: String) {
        firestore.collection("classes").document(classId).delete().await()
    }

    // [IMPLEMENTASI BARU]
    override fun getUserBookedClassIds(userId: String): Flow<Set<String>> = callbackFlow {
        val query = firestore.collection("bookings")
            .whereEqualTo("userId", userId) // FIX: Consistent userId

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                // Ambil semua field 'classId' dan masukkan ke Set (biar pencarian cepat)
                val bookedIds = snapshot.documents
                    .mapNotNull { it.getString("classId") }
                    .toSet()
                trySend(bookedIds).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }

    override fun getUserBookingsStream(userId: String): Flow<List<Booking>> = callbackFlow {
        val query = firestore.collection("bookings")
            .whereEqualTo("userId", userId)

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val bookings = snapshot.toObjects(Booking::class.java)
                trySend(bookings).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }

    override fun getClassParticipantsStream(classId: String): Flow<List<Booking>> = callbackFlow {
        val bookingCollection = firestore.collection("bookings")

        // Query untuk memfilter hanya booking kelas ini
        val subscription = bookingCollection
            .whereEqualTo("classId", classId)
            .addSnapshotListener { snapshot, error ->
                // ... (logika error)
                if (snapshot != null) {
                    val bookings = snapshot.toObjects(Booking::class.java)
                    trySend(bookings).isSuccess
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun getClassById(classId: String): GymClass? {
        return try {
            val snapshot = firestore.collection("classes").document(classId).get().await()
            snapshot.toObject(GymClass::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getClassesForTrainer(trainerId: String): List<GymClass> {
        return try {
            // CARA LAMA (Salah): Query berdasarkan nama "Trainer ID"
            // val trainerNameQuery = "Trainer $trainerId"

            // CARA BARU (Benar): Query langsung ke field trainerId yang baru kita buat
            val snapshot = firestore.collection("classes")
                .whereEqualTo("trainerId", trainerId)
                .get()
                .await()

            snapshot.toObjects(GymClass::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun updateBookingDetails(
        classId: String,
        bookingId: String,
        attendance: String,
        feedback: String,
        rating: Int
    ) {
        // Asumsi Booking ada di subcollection: classes/{classId}/bookings/{bookingId}
        // Sesuaikan path ini dengan struktur database Anda yang sebenarnya!
        firestore.collection("classes").document(classId)
            .collection("bookings").document(bookingId)
            .update(
                mapOf(
                    "attendanceStatus" to attendance,
                    "trainerFeedback" to feedback,
                    "progressRating" to rating
                )
            ).await()
    }

    override suspend fun cancelBooking(classId: String, userId: String) {
        try {
            // Cari dokumen booking yang cocok
            val snapshot = firestore.collection("bookings")
                .whereEqualTo("classId", classId)
                .whereEqualTo(
                    "userId",
                    userId
                ) // Pastikan field di DB 'memberId' atau 'userId' (sesuaikan Booking.kt)
                .get()
                .await()

            // Hapus semua dokumen yang cocok (seharusnya cuma 1)
            for (document in snapshot.documents) {
                firestore.collection("bookings").document(document.id).delete().await()
            }

            // Update slot di GymClass (Increment capacity)
            // (Opsional: Tergantung logika backend/cloud function Anda,
            // jika manual, Anda perlu get gymClass -> update currentBookings - 1)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // [RATING] 2. Fungsi Submit Rating (Sudah saya perbaiki untuk meng-override)
    override suspend fun submitRating(
        bookingId: String,
        trainerId: String,
        rating: Int,
        review: String
    ) {
        firestore.runTransaction { transaction ->
            val bookingRef = firestore.collection("bookings").document(bookingId)
            val trainerRef = firestore.collection("users").document(trainerId)

            // 2. Baca Data Trainer Saat Ini
            val trainerSnapshot = transaction.get(trainerRef)
            val currentSum = trainerSnapshot.getLong("totalRatingSum") ?: 0L
            val currentCount = trainerSnapshot.getLong("ratingCount") ?: 0L

            // 3. Hitung Statistik Baru
            val newSum = currentSum + rating
            val newCount = currentCount + 1
            val newAverage = newSum.toDouble() / newCount

            // 4. Update Booking
            transaction.update(bookingRef, mapOf(
                "rating" to rating,
                "review" to review,
                "isRated" to true,
                "ratingTimestamp" to System.currentTimeMillis(), // SAVE TIMESTAMP
                "trainerId" to trainerId
            ))

            // 5. Update Trainer
            transaction.update(trainerRef, mapOf(
                "totalRatingSum" to newSum,
                "ratingCount" to newCount,
                "averageRating" to newAverage
            ))
        }.await()
    }


    override suspend fun getBookingId(classId: String, userId: String): String? {
        return try {
            val snapshot = firestore.collection("bookings")
                .whereEqualTo("classId", classId)
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "Confirmed") // Pastikan hanya ambil yang aktif
                .limit(1)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                snapshot.documents[0].id // Kembalikan ID dokumen
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun findBookingDocument(classId: String, userId: String): com.google.firebase.firestore.QuerySnapshot {
        return firestore.collection("bookings")
            .whereEqualTo("classId", classId)
            .whereEqualTo("userId", userId) // FIX: Sudah benar userId
            .get()
            .await()
    }

    override fun getTrainerReviews(trainerId: String): Flow<List<Booking>> = callbackFlow {
        val query = firestore.collection("bookings")
            .whereEqualTo("trainerId", trainerId)
        
        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val bookings = snapshot.toObjects(Booking::class.java)
                // Filter hanya yang sudah memberi rating
                val reviews = bookings.filter { it.rating > 0 }
                trySend(reviews).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }

}
