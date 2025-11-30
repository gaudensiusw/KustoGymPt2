package com.example.projekuas.ui.workoutlog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.projekuas.data.SyncStatus
import com.example.projekuas.data.WorkoutLogEntity
import com.example.projekuas.viewmodel.WorkoutLogState
import com.example.projekuas.viewmodel.WorkoutLogViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// FIX: Ubah untuk menerima ViewModel dan onNavigateBack
fun WorkoutLogScreen(
    viewModel: WorkoutLogViewModel,
    onNavigateBack: () -> Unit
) {
    // 1. ViewModel Initialization (sudah dilakukan di AppNavHost)
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Lacak Latihan") },
                // FIX: Tambahkan tombol kembali
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        /* Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali") */
                        Text("Back") // Ganti dengan teks sementara
                    }
                },
                actions = {
                    // Tombol Sinkronisasi (Hanya tampil jika ada data PENDING)
                    if (state.syncNeeded) {
                        // FIX: Ganti TODO dengan panggilan fungsi
                        IconButton(onClick = { viewModel.syncData() }) {
                            /* Icon(Icons.Filled.CloudSync, contentDescription = "Sinkronkan Data", tint = MaterialTheme.colorScheme.primary) */
                            Text("Sync") // Ganti dengan teks sementara
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Formulir Input Log Latihan
            InputForm(viewModel = viewModel, state = state)

            Spacer(Modifier.height(16.dp))
            Divider()
            Spacer(Modifier.height(16.dp))

            // Riwayat Log Latihan
            LogHistoryList(logs = state.logs)
        }
    }
}

// ... (InputForm dan LogHistoryList tidak berubah, kode di bawahnya tetap) ...
// (Saya tidak ulangi kode InputForm dan LogHistoryList di sini karena sudah benar, kecuali jika ada error lain)
@Composable
fun InputForm(viewModel: WorkoutLogViewModel, state: WorkoutLogState) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Input Log Baru (Offline Ready)", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = state.inputExerciseName,
            onValueChange = viewModel::onExerciseNameChange,
            label = { Text("Nama Latihan") },
            /* leadingIcon = { Icon(Icons.Filled.FitnessCenter, contentDescription = null) }, */
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Sets
            OutlinedTextField(
                value = state.inputSets,
                onValueChange = viewModel::onSetsChange,
                label = { Text("Sets") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f).padding(vertical = 4.dp)
            )
            // Reps
            OutlinedTextField(
                value = state.inputReps,
                onValueChange = viewModel::onRepsChange,
                label = { Text("Reps") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f).padding(vertical = 4.dp)
            )
            // Weight
            OutlinedTextField(
                value = state.inputWeight,
                onValueChange = viewModel::onWeightChange,
                label = { Text("Berat (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f).padding(vertical = 4.dp)
            )
        }

        Button(
            onClick = viewModel::onSaveLog,
            enabled = !state.isSaving && state.inputExerciseName.isNotBlank(),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(Modifier.size(24.dp))
            } else {
                Text("SIMPAN LOG LATIHAN")
            }
        }
    }
}

// Composable untuk Riwayat Log
@Composable
fun LogHistoryList(logs: List<WorkoutLogEntity>) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
            Text("Riwayat Latihan", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
        }
        if (logs.isEmpty()) {
            item { Text("Belum ada riwayat latihan yang tercatat.") }
        } else {
            items(logs, key = { it.id }) { log ->
                LogItemCard(log = log)
            }
        }
    }
}

// Card untuk menampilkan satu item Log
@Composable
fun LogItemCard(log: WorkoutLogEntity) {
    val dateFormatter = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    val statusColor = if (log.syncStatus == SyncStatus.PENDING.code) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(log.exerciseName, style = MaterialTheme.typography.titleMedium)
                Text("Sets: ${log.sets} | Reps: ${log.reps} | Berat: ${log.weight} kg", style = MaterialTheme.typography.bodyMedium)
                Text(dateFormatter.format(Date(log.dateMillis)), style = MaterialTheme.typography.bodySmall)
            }

            // Status Sinkronisasi
            Text(
                if (log.syncStatus == SyncStatus.PENDING.code) "OFFLINE" else "SYNCED",
                color = statusColor,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}