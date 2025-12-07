package com.example.projekuas.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.projekuas.data.AppDatabase

class WorkoutSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val unsyncedLogs = database.workoutLogDao().getPendingLogs()
        if (unsyncedLogs.isEmpty()) return Result.success()

        return try {
            // Loop through logs and push to Firestore
            unsyncedLogs.forEach { log ->
                // Call your Firebase Repository here to upload 'log'
                // On success: database.workoutLogDao().markAsSynced(log.id)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}