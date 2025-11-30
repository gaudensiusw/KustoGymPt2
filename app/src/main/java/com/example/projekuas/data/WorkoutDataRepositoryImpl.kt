package com.example.projekuas.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class WorkoutDataRepositoryImpl(
    private val firestore: FirebaseFirestore
) : WorkoutDataRepository {

    // ==========================================
    // BAGIAN 2: FITUR BARU (PURE FIREBASE) - INI YANG DIPAKAI SEKARANG
    // ==========================================

    // 1. Simpan Sesi Latihan
    override suspend fun saveWorkoutSession(session: WorkoutSession) {
        try {
            firestore.collection("users")
                .document(session.userId)
                .collection("workouts")
                .document(session.sessionId)
                .set(session)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    // 2. Ambil Riwayat Latihan
    override suspend fun getWorkoutHistory(userId: String): List<WorkoutSession> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("workouts")
                .orderBy("dateMillis", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.toObjects(WorkoutSession::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 3. Hapus Sesi Latihan (Multi-select)
    override suspend fun deleteWorkoutSessions(userId: String, sessionIds: List<String>) {
        try {
            val batch: WriteBatch = firestore.batch()
            sessionIds.forEach { sessionId ->
                val docRef = firestore.collection("users")
                    .document(userId)
                    .collection("workouts")
                    .document(sessionId)
                batch.delete(docRef)
            }
            batch.commit().await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    // 4. Ambil Daftar Latihan (Master Data)
    override suspend fun getExercisesByMuscle(muscle: String): List<ExerciseMaster> {
        return try {
            // Coba ambil dari Firebase
            val snapshot = firestore.collection("exercises")
                .whereEqualTo("targetMuscle", muscle)
                .get()
                .await()

            val exercises = snapshot.toObjects(ExerciseMaster::class.java)

            // Jika kosong di DB, pakai Fallback Data lokal (agar UI tidak blank)
            if (exercises.isEmpty()) {
                getFallbackExercises(muscle)
            } else {
                exercises
            }
        } catch (e: Exception) {
            getFallbackExercises(muscle)
        }
    }

    // Data Dummy/Fallback jika Firebase belum diisi
    private fun getFallbackExercises(muscle: String): List<ExerciseMaster> {
        val listNames = when (muscle.lowercase()) {
            "chest" -> listOf("Bench Press", "Incline Dumbbell Press", "Push Up", "Cable Fly", "Dips")
            "back" -> listOf("Pull Up", "Lat Pulldown", "Deadlift", "Bent Over Row", "Seated Row")
            "legs" -> listOf("Squat", "Leg Press", "Lunges", "Leg Extension", "Calf Raise")
            "shoulders" -> listOf("Overhead Press", "Lateral Raise", "Front Raise", "Face Pull")
            "arms" -> listOf("Bicep Curl", "Tricep Extension", "Hammer Curl", "Skullcrusher")
            "core" -> listOf("Plank", "Crunches", "Leg Raise", "Russian Twist")
            else -> listOf("Burpees", "Jumping Jacks")
        }

        return listNames.mapIndexed { index, name ->
            ExerciseMaster(id = "${muscle}_$index", name = name, targetMuscle = muscle)
        }
    }

    // ==========================================
    // BAGIAN 1: FITUR LAMA (STUB / KOSONG)
    // Diimplementasikan hanya agar tidak ERROR di Interface
    // ==========================================

    override fun getAllLogsFlow(userId: String): Flow<List<WorkoutLogEntity>> {
        // Kembalikan Flow kosong karena kita tidak pakai Room lagi
        return flow { emit(emptyList()) }
    }

    override fun getAllLogsStream(userId: String): Flow<List<WorkoutLogEntity>> {
        return flow { emit(emptyList()) }
    }

    override suspend fun insertLog(log: WorkoutLogEntity) {
        // Tidak melakukan apa-apa (Deprecated)
    }

    override suspend fun saveLogLocal(log: WorkoutLogEntity) {
        // Tidak melakukan apa-apa (Deprecated)
    }

    override suspend fun syncPendingLogs(userId: String) {
        // Tidak melakukan apa-apa (Deprecated)
    }
}