package com.example.projekuas.data

import com.google.firebase.firestore.FirebaseFirestore
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

        // Listener untuk mendapatkan semua UserProfile secara real-time
        val subscription = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                // Mapping setiap dokumen ke data class UserProfile
                val userList = snapshot.toObjects(UserProfile::class.java)
                trySend(userList).isSuccess
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

    override suspend fun updateClass(gymClass: GymClass) {
        // set(gymClass) akan menimpa seluruh dokumen, menggunakan classId sebagai ID dokumen
        firestore.collection("classes").document(gymClass.classId).set(gymClass).await()
    }

    override suspend fun deleteClass(classId: String) {
        // Akses koleksi "classes" dan hapus dokumen berdasarkan classId
        firestore.collection("classes").document(classId).delete().await()
    }
}