package com.example.projekuas.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projekuas.ui.theme.GymPurple
import com.example.projekuas.ui.theme.GymPurpleDark
import com.example.projekuas.viewmodel.WorkoutViewModel

@Composable
fun ExerciseSelectionScreen(
    viewModel: WorkoutViewModel,
    muscleGroup: String,
    onNavigateToActiveWorkout: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.selectionState.collectAsState()

    // Load data saat pertama kali dibuka
    LaunchedEffect(muscleGroup) {
        viewModel.loadExercisesForMuscleGroup(muscleGroup)
    }

    Scaffold(
        // FIX: Menggunakan background tema (Hitam di Dark Mode)
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            // HEADER UNGU MELENGKUNG
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                    .background(Brush.verticalGradient(listOf(GymPurple, GymPurpleDark)))
            ) {
                Column(modifier = Modifier.padding(top = 40.dp, start = 20.dp, end = 20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "$muscleGroup Exercises",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Select exercises to start",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Tombol Start Melayang (Hanya muncul jika ada yang dipilih)
            if (state.selectedExercises.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 16.dp,
                    modifier = Modifier.clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    Button(
                        onClick = {
                            // Mulai workout dengan nama otot yang dipilih sebagai nama sesi
                            viewModel.startWorkoutFromSelection(muscleGroup)
                            onNavigateToActiveWorkout()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = "Start Workout (${state.selectedExercises.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (state.availableExercises.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No exercises found for $muscleGroup", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.availableExercises) { exercise ->
                    val isSelected = state.selectedExercises.contains(exercise)

                    // ITEM CARD LATIHAN
                    ExerciseOptionCard(
                        name = exercise.name, // Mengambil nama dari ExerciseMaster
                        isSelected = isSelected,
                        onClick = { viewModel.toggleExerciseSelection(exercise) }
                    )
                }
            }
        }
    }
}

@Composable
fun ExerciseOptionCard(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        // Warna Kartu berubah jika dipilih
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp),
        // Border ungu jika dipilih
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox Icon Custom
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}