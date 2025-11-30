package com.example.projekuas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekuas.data.AuthRepository
import com.example.projekuas.data.ExerciseLog
import com.example.projekuas.data.ExerciseMaster
import com.example.projekuas.data.HistorySelectionState
import com.example.projekuas.data.SetLog
import com.example.projekuas.data.WorkoutDataRepository
import com.example.projekuas.data.WorkoutSession
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

// State untuk Active Workout
data class WorkoutUiState(
    val isTimerRunning: Boolean = false,
    val elapsedSeconds: Long = 0L,
    val currentExercises: List<ExerciseLog> = emptyList(),
    val totalCalories: Int = 0,
    val activeWorkoutName: String = "Workout"
)

// State untuk Selection Screen
data class SelectionState(
    val availableExercises: List<ExerciseMaster> = emptyList(), // Menggunakan ExerciseMaster
    val selectedExercises: Set<ExerciseMaster> = emptySet(),
    val isLoading: Boolean = false
)

class WorkoutViewModel(
    private val workoutRepository: WorkoutDataRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    // --- Active Workout State ---
    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    // --- Selection State ---
    private val _selectionState = MutableStateFlow(SelectionState())
    val selectionState: StateFlow<SelectionState> = _selectionState.asStateFlow()

    // --- History State ---
    private val _workoutHistory = MutableStateFlow<List<WorkoutSession>>(emptyList())
    val workoutHistory: StateFlow<List<WorkoutSession>> = _workoutHistory.asStateFlow()

    // Statistik
    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount.asStateFlow()

    private val _totalMins = MutableStateFlow(0)
    val totalMins: StateFlow<Int> = _totalMins.asStateFlow()

    private val _totalCals = MutableStateFlow(0)
    val totalCals: StateFlow<Int> = _totalCals.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadHistoryData()
    }

    private fun loadHistoryData() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                val history = workoutRepository.getWorkoutHistory(userId)
                _workoutHistory.value = history
                _totalCount.value = history.size
                _totalMins.value = history.sumOf { it.durationSeconds.toInt() } / 60
                _totalCals.value = history.sumOf { it.totalCaloriesBurned }
            }
        }
    }

    // ==============================
    // 1. LOGIC SELECTION (FIREBASE)
    // ==============================

    fun loadExercisesForMuscleGroup(muscleGroup: String) {
        viewModelScope.launch {
            _selectionState.update { it.copy(isLoading = true) }

            try {
                // Coba ambil dari Firebase collection 'exercises'
                val snapshot = firestore.collection("exercises")
                    .whereEqualTo("targetMuscle", muscleGroup)
                    .get()
                    .await()

                val exercises = snapshot.toObjects(ExerciseMaster::class.java)

                // Jika di Firebase belum ada datanya, gunakan Fallback Data (Dummy) agar tidak kosong
                val finalExercises = if (exercises.isEmpty()) {
                    generateFallbackExercises(muscleGroup)
                } else {
                    exercises
                }

                _selectionState.update {
                    it.copy(availableExercises = finalExercises, selectedExercises = emptySet(), isLoading = false)
                }
            } catch (e: Exception) {
                // Jika error koneksi, gunakan Fallback
                _selectionState.update {
                    it.copy(availableExercises = generateFallbackExercises(muscleGroup), isLoading = false)
                }
            }
        }
    }

    private fun generateFallbackExercises(muscle: String): List<ExerciseMaster> {
        val names = when (muscle.lowercase()) {
            "chest" -> listOf("Bench Press", "Incline Press", "Push Up", "Cable Fly")
            "back" -> listOf("Pull Up", "Lat Pulldown", "Deadlift", "Bent Over Row")
            "legs" -> listOf("Squat", "Leg Press", "Lunges", "Leg Extension")
            "shoulders" -> listOf("Overhead Press", "Lateral Raise", "Front Raise")
            "arms" -> listOf("Bicep Curl", "Tricep Extension", "Hammer Curl")
            else -> listOf("Burpees", "Plank", "Jumping Jacks")
        }
        return names.map { ExerciseMaster(id = UUID.randomUUID().toString(), name = it, targetMuscle = muscle) }
    }

    fun toggleExerciseSelection(exercise: ExerciseMaster) {
        _selectionState.update { state ->
            val currentSelected = state.selectedExercises.toMutableSet()
            if (currentSelected.contains(exercise)) {
                currentSelected.remove(exercise)
            } else {
                currentSelected.add(exercise)
            }
            state.copy(selectedExercises = currentSelected)
        }
    }

    fun startWorkoutFromSelection(muscleGroupName: String) {
        val selectedMasters = _selectionState.value.selectedExercises.toList()

        val sessionName = if (selectedMasters.isNotEmpty()) "$muscleGroupName Workout" else "Mixed Workout"

        val initialLogs = selectedMasters.map { master ->
            ExerciseLog(
                exerciseId = master.id,
                exerciseName = master.name,
                muscleGroup = master.targetMuscle,
                sets = listOf(SetLog(setNumber = 1, weightKg = 0.0, reps = 0))
            )
        }

        _uiState.update {
            WorkoutUiState(
                isTimerRunning = true,
                elapsedSeconds = 0L,
                currentExercises = initialLogs,
                totalCalories = 0,
                activeWorkoutName = sessionName
            )
        }
        startTimer()
    }

    // ==============================
    // 2. LOGIC ACTIVE WORKOUT
    // ==============================

    fun startWorkout() {
        if (_uiState.value.isTimerRunning) return
        _uiState.update { it.copy(isTimerRunning = true) }
        startTimer()
    }

    fun pauseWorkout() {
        _uiState.update { it.copy(isTimerRunning = false) }
        timerJob?.cancel()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }

    fun addSet(exerciseIndex: Int) {
        _uiState.update { state ->
            val exercises = state.currentExercises.toMutableList()
            val exercise = exercises[exerciseIndex]
            val newSetNumber = exercise.sets.size + 1
            val newSets = exercise.sets + SetLog(setNumber = newSetNumber)

            exercises[exerciseIndex] = exercise.copy(sets = newSets)
            state.copy(currentExercises = exercises)
        }
    }

    fun updateSetData(exerciseIndex: Int, setIndex: Int, weight: String, reps: String) {
        _uiState.update { state ->
            val exercises = state.currentExercises.toMutableList()
            val exercise = exercises[exerciseIndex]
            val sets = exercise.sets.toMutableList()

            val w = weight.toDoubleOrNull() ?: 0.0
            val r = reps.toIntOrNull() ?: 0

            sets[setIndex] = sets[setIndex].copy(weightKg = w, reps = r)
            exercises[exerciseIndex] = exercise.copy(sets = sets)

            val totalReps = exercises.sumOf { ex -> ex.sets.sumOf { it.reps } }
            val cal = (totalReps * 0.5).toInt()

            state.copy(currentExercises = exercises, totalCalories = cal)
        }
    }

    // FUNGSI INI YANG SEBELUMNYA HILANG
    fun saveWorkout(note: String, onSuccess: () -> Unit) {
        val currentState = _uiState.value
        pauseWorkout()

        val userId = authRepository.getCurrentUserId() ?: return

        val session = WorkoutSession(
            sessionId = UUID.randomUUID().toString(),
            userId = userId,
            dateMillis = System.currentTimeMillis(),
            durationSeconds = currentState.elapsedSeconds,
            totalCaloriesBurned = currentState.totalCalories,
            exercises = currentState.currentExercises,
            note = note,
            workoutName = currentState.activeWorkoutName
        )

        viewModelScope.launch {
            workoutRepository.saveWorkoutSession(session)
            loadHistoryData() // Refresh history
        }

        // LANGSUNG PINDAH HALAMAN (UX lancar)
        // Reset state UI
        _uiState.update { WorkoutUiState() }
        _selectionState.update { SelectionState() }
        onSuccess()
    }


    // Fallback untuk finish tanpa save jika perlu (tombol back)
    fun finishWorkout(onFinish: () -> Unit) {
        // Biasanya kita ingin save, tapi jika tombolnya "Finish Workout" yang memicu dialog,
        // logika ini ada di UI. Fungsi ini mungkin sisa dari kode lama,
        // tapi kita bisa arahkan ke saveWorkout default atau biarkan untuk logika cancel.
        // Untuk saat ini, UI memanggil saveWorkout melalui Dialog, jadi ini aman.
    }

    private val _historySelection = MutableStateFlow(HistorySelectionState())
    val historySelection: StateFlow<HistorySelectionState> = _historySelection.asStateFlow()

    // 1. Masuk/Keluar Mode Seleksi
    fun toggleSelectionMode() {
        _historySelection.update {
            if (it.isSelectionMode) {
                // Kalau keluar mode, reset pilihan
                it.copy(isSelectionMode = false, selectedIds = emptySet())
            } else {
                // Masuk mode
                it.copy(isSelectionMode = true)
            }
        }
    }

    // 2. Pilih/Hapus Pilihan Item
    fun toggleItemSelection(sessionId: String) {
        _historySelection.update { state ->
            val currentIds = state.selectedIds.toMutableSet()
            if (currentIds.contains(sessionId)) {
                currentIds.remove(sessionId)
            } else {
                currentIds.add(sessionId)
            }

            // Jika tidak ada yg dipilih, otomatis keluar mode seleksi (opsional)
            // if (currentIds.isEmpty()) return@update state.copy(isSelectionMode = false, selectedIds = emptySet())

            state.copy(selectedIds = currentIds)
        }
    }

    // 3. Hapus Item Terpilih (Firebase)
    fun deleteSelectedSessions() {
        val userId = authRepository.getCurrentUserId() ?: return
        val idsToDelete = _historySelection.value.selectedIds.toList()

        viewModelScope.launch {
            try {
                workoutRepository.deleteWorkoutSessions(userId, idsToDelete)
                // Refresh data setelah hapus
                loadHistoryData()
                // Keluar mode seleksi
                toggleSelectionMode()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}