package com.example.projekuas.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projekuas.data.ExerciseLog
import com.example.projekuas.ui.theme.GymPurple
import com.example.projekuas.ui.theme.GymPurpleDark
import com.example.projekuas.viewmodel.WorkoutViewModel
import java.util.Locale
import androidx.compose.ui.draw.shadow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    viewModel: WorkoutViewModel,
    onFinish: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    // State untuk Dialog Konfirmasi
    var showFinishDialog by remember { mutableStateOf(false) }
    var workoutNote by remember { mutableStateOf("") }

    // Format Timer
    val timerText = remember(state.elapsedSeconds) {
        val min = state.elapsedSeconds / 60
        val sec = state.elapsedSeconds % 60
        String.format(Locale.getDefault(), "%02d:%02d", min, sec)
    }

    Scaffold(
        // FIX: Background mengikuti tema
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            // HEADER TIMER YANG MENGAMBANG
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp) // Sedikit lebih tinggi untuk nama workout
                    .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                    .background(Brush.verticalGradient(listOf(GymPurple, GymPurpleDark)))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Nama Workout yang Aktif
                    Text(
                        text = state.activeWorkoutName,
                        color = Color.White.copy(0.9f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Timer Besar
                    Text(
                        text = timerText,
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // Estimasi Kalori
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalFireDepartment, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${state.totalCalories} kcal",
                            color = Color.White.copy(0.9f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Play/Pause Button Floating
                IconButton(
                    onClick = { if (state.isTimerRunning) viewModel.pauseWorkout() else viewModel.startWorkout() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp)
                        .size(56.dp)
                        .background(Color.White, CircleShape)
                        .shadow(4.dp, CircleShape)
                ) {
                    Icon(
                        imageVector = if (state.isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = GymPurple
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                shadowElevation = 16.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                Button(
                    onClick = {
                        viewModel.pauseWorkout() // Pause dulu
                        showFinishDialog = true // Tampilkan Dialog
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .height(50.dp),
                    // Warna Merah untuk tombol finish (Theme Error color)
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("FINISH WORKOUT", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.currentExercises.isEmpty()) {
                item {
                    Text(
                        text = "No exercises selected.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp)
                    )
                }
            }

            itemsIndexed(state.currentExercises) { index, exercise ->
                ExerciseInputCard(
                    exercise = exercise,
                    onAddSet = { viewModel.addSet(index) },
                    onUpdateSet = { setIdx, w, r -> viewModel.updateSetData(index, setIdx, w, r) }
                )
            }
        }
    }

    // DIALOG KONFIRMASI & FEEDBACK
    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = { Text("Finish Workout?", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Great job! How did it feel?", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = workoutNote,
                        onValueChange = { workoutNote = it },
                        label = { Text("Add notes (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFinishDialog = false
                        viewModel.saveWorkout(workoutNote, onFinish)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Save & Finish")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ExerciseInputCard(
    exercise: ExerciseLog,
    onAddSet: () -> Unit,
    onUpdateSet: (Int, String, String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exercise.exerciseName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = { /* Option to remove exercise could go here */ }) {
                    Icon(Icons.Default.MoreHoriz, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))

            // Header Kolom
            Row(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Text("SET", Modifier.weight(0.5f), textAlign = TextAlign.Center, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                Text("KG", Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                Text("REPS", Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }

            // List Sets
            exercise.sets.forEachIndexed { i, set ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Set Number Badge
                    Box(
                        modifier = Modifier.weight(0.5f),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text((i + 1).toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    // Input Berat
                    OutlinedTextField(
                        value = if(set.weightKg > 0.0) set.weightKg.toString() else "",
                        onValueChange = { onUpdateSet(i, it, set.reps.toString()) },
                        modifier = Modifier.weight(1f).height(48.dp).padding(horizontal = 4.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("-", textAlign = TextAlign.Center) },
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(0.3f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    // Input Reps
                    OutlinedTextField(
                        value = if(set.reps > 0) set.reps.toString() else "",
                        onValueChange = { onUpdateSet(i, set.weightKg.toString(), it) },
                        modifier = Modifier.weight(1f).height(48.dp).padding(horizontal = 4.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("-", textAlign = TextAlign.Center) },
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(0.3f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Add Set Button
            Button(
                onClick = onAddSet,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Set")
            }
        }
    }
}