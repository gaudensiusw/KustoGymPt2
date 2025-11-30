package com.example.projekuas.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AdminRepositoryImpl(
    private val firestore: FirebaseFirestore
) : AdminRepository {

    // 1. Mengambil semua pengguna (untuk daftar manajemen)
    override fun getAllUsersStream(): Flow<List<UserProfile>> = callbackFlow {
        val collectionRef = firestore.collection("users")

        val subscription = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val userList = snapshot.toObjects(UserProfile::class.java)
                trySend(userList).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }

    override fun getAllBookingsStream(): Flow<List<Booking>> = callbackFlow {
        val collectionRef = firestore.collection("bookings")

        val subscription = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                // Konversi dokumen Firestore ke objek Booking
                val bookingList = snapshot.toObjects(Booking::class.java)
                trySend(bookingList).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }

    override fun getAllClassesStream(): Flow<List<GymClass>> = callbackFlow {
        val collectionRef = firestore.collection("classes") // Pastikan nama koleksi di Firestore adalah "classes"
        val subscription = collectionRef.addSnapshotListener { snapshot, error ->
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

    // 2. Mengubah peran pengguna (Mengubah field 'role')
    override suspend fun updateUserRole(userId: String, newRole: String) {
        val userDocRef = firestore.collection("users").document(userId)

        // Data yang hanya berisi field 'role'
        val roleUpdate = hashMapOf(
            "role" to newRole
        )

        // set(roleUpdate, SetOptions.merge()) memastikan hanya field 'role' yang diubah
        userDocRef.set(roleUpdate, SetOptions.merge()).await()
    }

    // 3. Menghapus pengguna
    override suspend fun deleteUser(userId: String) {
        val userDocRef = firestore.collection("users").document(userId)

        // Catatan: Menghapus dokumen profil di Firestore TIDAK menghapus akun di Firebase Auth.
        // Penghapusan Auth harus dilakukan di sisi server (Cloud Functions) untuk keamanan.
        userDocRef.delete().await()
    }

    override fun getRealtimeMembers(): Flow<List<UserProfile>> = callbackFlow {
        val listener = firestore.collection("users")
            .whereEqualTo("role", "member")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val members = snapshot?.toObjects(UserProfile::class.java) ?: emptyList()
                trySend(members)
            }
        awaitClose { listener.remove() }
    }

    override fun getRealtimeTrainers(): Flow<List<UserProfile>> = callbackFlow {
        val listener = firestore.collection("users")
            .whereEqualTo("role", "trainer")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val trainers = snapshot?.toObjects(UserProfile::class.java) ?: emptyList()
                trySend(trainers)
            }
        awaitClose { listener.remove() }
    }

    override fun getRealtimeClasses(): Flow<List<GymClass>> = callbackFlow {
        val listener = firestore.collection("classes")
            .orderBy("date", Query.Direction.DESCENDING) // Urutkan berdasarkan tanggal terbaru
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val classes = snapshot?.toObjects(GymClass::class.java) ?: emptyList()
                trySend(classes)
            }
        awaitClose { listener.remove() }
    }

    override fun getDashboardStats(): Flow<DashboardStats> = callbackFlow {
        // Gabungan listener sederhana untuk statistik realtime
        // Catatan: Di aplikasi skala besar, gunakan Cloud Functions untuk agregasi (count)
        // Untuk skala project kuliah, listener client-side ini sudah cukup.

        val userRef = firestore.collection("users")
        val classRef = firestore.collection("classes")

        val listener = userRef.addSnapshotListener { userSnap, _ ->
            classRef.addSnapshotListener { classSnap, _ ->
                val users = userSnap?.toObjects(UserProfile::class.java) ?: emptyList()
                val classes = classSnap?.size() ?: 0

                val members = users.filter { it.role == "member" }
                val trainers = users.filter { it.role == "trainer" }
                val active = members.count { it.membershipType != "None" && it.membershipType != null }

                trySend(DashboardStats(members.size, trainers.size, classes, active))
            }
        }
        awaitClose { listener.remove() } // Note: Ini penyederhanaan cleanup
    }

    override suspend fun addClass(gymClass: GymClass) {
        val ref = firestore.collection("classes").document()
        val newClass = gymClass.copy(classId = ref.id)
        ref.set(newClass).await()
    }

    override suspend fun updateClass(gymClass: GymClass) {
        firestore.collection("classes").document(gymClass.classId).set(gymClass).await()
    }

    override suspend fun deleteClass(classId: String) {
        firestore.collection("classes").document(classId).delete().await()
    }
}