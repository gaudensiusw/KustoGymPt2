package com.example.projekuas.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WorkoutRepository(
    private val sessionDao: WorkoutSessionDao, // Pakai DAO yang baru
    private val firestore: FirebaseFirestore
) {
    // Read Data
    val allSessions: Flow<List<WorkoutSessionEntity>> = sessionDao.getAllSessions()
    val totalWorkouts = sessionDao.getTotalSessionsCount()
    val totalMinutes = sessionDao.getTotalMinutes()
    val totalCalories = sessionDao.getTotalCalories()

    // Save Data
    suspend fun saveSession(session: WorkoutSessionEntity) {
        sessionDao.insertSession(session.copy(isSynced = false))
        trySyncToFirebase(session)
    }

    // Sync Logic
    suspend fun syncPendingSessions() {
        withContext(Dispatchers.IO) {
            val pendingList = sessionDao.getUnsyncedSessions()
            pendingList.forEach { session ->
                trySyncToFirebase(session)
            }
        }
    }

    private suspend fun trySyncToFirebase(session: WorkoutSessionEntity) {
        try {
            val data = hashMapOf(
                "id" to session.id,
                "muscleGroup" to session.muscleGroup,
                "dateMillis" to session.dateMillis,
                "durationMinutes" to session.durationMinutes,
                "caloriesBurnt" to session.caloriesBurnt,
                "exercisesJson" to session.exercisesJson
            )

            firestore.collection("workout_sessions").document(session.id)
                .set(data, SetOptions.merge())
                .addOnSuccessListener {
                    kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
                        sessionDao.markAsSynced(session.id)
                    }
                }
        } catch (e: Exception) {
            Log.e("WorkoutRepo", "Sync error: ${e.message}")
        }
    }
}