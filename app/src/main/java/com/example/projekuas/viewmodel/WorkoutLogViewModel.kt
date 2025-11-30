package com.example.projekuas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekuas.data.AuthRepository
import com.example.projekuas.data.WorkoutLogEntity
import com.example.projekuas.data.SyncStatus
import com.example.projekuas.data.WorkoutDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseUser // FIX: Import FirebaseUser (jika AuthRepository mengembalikan FirebaseUser)

data class WorkoutLogState(
    val logs: List<WorkoutLogEntity> = emptyList(),
    val currentUserId: String? = null, // FIX: Gunakan String? karena mungkin belum login
    val inputExerciseName: String = "",
    val inputSets: String = "",
    val inputReps: String = "",
    val inputWeight: String = "",
    val isSaving: Boolean = false,
    val syncNeeded: Boolean = false,
    val isLoading: Boolean = true // Tambah state loading
)

class WorkoutLogViewModel(
    private val workoutDataRepository: WorkoutDataRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(WorkoutLogState())
    val state: StateFlow<WorkoutLogState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val user = authRepository.currentUser.firstOrNull()
            val userId = user?.uid

            _state.update { it.copy(currentUserId = userId, isLoading = false) }

            if (userId != null) {
                // FIX: Tambahkan tipe List<WorkoutLogEntity> secara eksplisit ke 'logs'
                workoutDataRepository.getAllLogsStream(userId).collect { logs: List<WorkoutLogEntity> ->
                    _state.update {
                        it.copy(
                            logs = logs,
                            // FIX: Ambiguity diatasi dengan tipe eksplisit
                            syncNeeded = logs.any { log: WorkoutLogEntity ->
                                log.syncStatus == SyncStatus.PENDING.code
                            }
                        )
                    }
                }
            } else {
                _state.update { it.copy(isLoading = false, logs = emptyList()) }
            }
        }
    }
    fun syncData() {
        val userId = _state.value.currentUserId
        if (userId == null) return

        viewModelScope.launch {
            try {
                // Panggil fungsi sinkronisasi dari Repository
                workoutDataRepository.syncPendingLogs(userId)
                // Status logs akan otomatis terupdate karena Repository mengembalikan Flow yang diamati.
            } catch (e: Exception) {
                // TODO: Tambahkan logika error handling, misalnya menampilkan Toast
                // e.g., _state.update { it.copy(error = "Sinkronisasi gagal: ${e.message}") }
            }
        }
    }

    fun onExerciseNameChange(name: String) {
        _state.update { it.copy(inputExerciseName = name) }
    }

    fun onSetsChange(sets: String) {
        _state.update { it.copy(inputSets = sets) }
    }

    fun onRepsChange(reps: String) {
        _state.update { it.copy(inputReps = reps) }
    }

    fun onWeightChange(weight: String) {
        _state.update { it.copy(inputWeight = weight) }
    }

    fun onSaveLog() {
        val currentState = _state.value
        if (currentState.inputExerciseName.isBlank() || currentState.currentUserId == null) return

        // FIX: Definisikan newLog di sini (di luar scope launch)
        val newLog = WorkoutLogEntity(
            userId = currentState.currentUserId!!,
            exerciseName = currentState.inputExerciseName,
            sets = currentState.inputSets.toIntOrNull() ?: 0,
            reps = currentState.inputReps.toIntOrNull() ?: 0,
            weight = currentState.inputWeight.toDoubleOrNull() ?: 0.0,
            syncStatus = SyncStatus.PENDING.code
        )

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            // FIX: Hanya panggil satu kali dan di dalam launch scope
            workoutDataRepository.insertLog(newLog)

            // FIX: Hanya panggil update sekali untuk mereset input dan status
            _state.update {
                it.copy(
                    isSaving = false,
                    inputExerciseName = "",
                    inputSets = "",
                    inputReps = "",
                    inputWeight = "",
                    syncNeeded = true
                    )
                }
            }
        }
    }

