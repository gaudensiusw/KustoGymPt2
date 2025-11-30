package com.example.projekuas.data

// --- MODEL DATA UNTUK WORKOUT (FINAL & KOMPATIBEL) ---

// 1. Model untuk SATU SET
// (Diganti jadi SetLog agar cocok dengan WorkoutViewModel yang baru)
data class SetLog(
    val setNumber: Int = 0,
    val weightKg: Double = 0.0,
    val reps: Int = 0,
    val isCompleted: Boolean = false
)

// 2. Model untuk SATU JENIS LATIHAN (Contoh: Bench Press berisi 3 set)
data class ExerciseLog(
    val exerciseId: String = "",      // ID unik latihan (opsional, bisa kosong)
    val exerciseName: String = "",    // Nama latihan (Wajib)
    val muscleGroup: String = "",     // Target otot (Chest, Back, dll)
    val sets: List<SetLog> = emptyList() // FIX: Menggunakan List<SetLog>
)

// 3. Model untuk SATU SESI LATIHAN PENUH (History Session)
// (Ini yang akan disimpan ke Firebase sebagai riwayat)
data class WorkoutSession(
    val sessionId: String = "",
    val userId: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val durationSeconds: Long = 0,
    val totalCaloriesBurned: Int = 0,
    val exercises: List<ExerciseLog> = emptyList(),
    val note: String = "",
    val workoutName: String = "Workout" // Tambahan: Nama Sesi (e.g. "Chest Workout")
)

// 4. Model untuk DATABASE JENIS LATIHAN (Master Data)
// (Tetap dipertahankan untuk fitur 'Choose Muscle Group' nanti)
data class ExerciseMaster(
    val id: String = "",
    val name: String = "",
    val targetMuscle: String = ""
)