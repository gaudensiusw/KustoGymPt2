package com.example.projekuas.ui.workout

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projekuas.ui.theme.GymPurple
import com.example.projekuas.ui.theme.GymPurpleDark
import com.example.projekuas.viewmodel.WorkoutViewModel
import com.example.projekuas.data.WorkoutSession
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WorkoutTrackerScreen(
    viewModel: WorkoutViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSelection: (String) -> Unit,
    onNavigateToActiveWorkout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val totalWorkouts by viewModel.totalCount.collectAsState()
    val totalMinutes by viewModel.totalMins.collectAsState()
    val totalCalories by viewModel.totalCals.collectAsState()

    // State untuk Delete Mode
    val selectionState by viewModel.historySelection.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- HEADER SECTION ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                .background(Brush.verticalGradient(listOf(GymPurple, GymPurpleDark)))
                .padding(bottom = 20.dp)
        ) {
            Column(modifier = Modifier.padding(top = 40.dp, start = 20.dp, end = 20.dp)) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }

                    // Judul berubah jika mode seleksi aktif
                    if (selectionState.isSelectionMode) {
                        Text(
                            "${selectionState.selectedIds.size} Selected",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    } else {
                        Text("Workout Tracker", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }

                    // Tombol Kanan (Add atau Delete)
                    if (selectionState.isSelectionMode) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = Color.White)
                        }
                    } else {
                        IconButton(onClick = { /* Add Manual Workout */ }) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.background(Color.White.copy(0.2f), CircleShape))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatBadge(Icons.Default.FitnessCenter, "$totalWorkouts", "Workouts")
                    StatBadge(Icons.Default.AccessTime, "${totalMinutes}m", "Minutes")
                    StatBadge(Icons.Default.LocalFireDepartment, "$totalCalories", "Calories")
                }

                Spacer(modifier = Modifier.height(25.dp))

                // Tab Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(Color.White.copy(0.2f))
                        .padding(4.dp)
                ) {
                    TabButton("Start Workout", selectedTab == 0, Modifier.weight(1f)) { selectedTab = 0 }
                    TabButton("History", selectedTab == 1, Modifier.weight(1f)) { selectedTab = 1 }
                }
            }
        }

        // --- CONTENT SECTION ---
        if (selectedTab == 0) {
            StartWorkoutContent(onNavigateToSelection = onNavigateToSelection, onNavigateToActiveWorkout = onNavigateToActiveWorkout)
        } else {
            val historyList by viewModel.workoutHistory.collectAsState()
            HistoryContent(
                historyList = historyList,
                selectionState = selectionState,
                onToggleSelection = { viewModel.toggleItemSelection(it) },
                onLongPress = { viewModel.toggleSelectionMode(); viewModel.toggleItemSelection(it) }
            )
        }
    }

    // Dialog Konfirmasi Hapus
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete History") },
            text = { Text("Are you sure you want to delete ${selectionState.selectedIds.size} items? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSelectedSessions()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// --- TAB 1: START WORKOUT (Kode Sama) ---
@Composable
fun StartWorkoutContent(
    onNavigateToSelection: (String) -> Unit,
    onNavigateToActiveWorkout: () -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(20.dp)) {
        item {
            Text("Choose Muscle Group", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp), color = MaterialTheme.colorScheme.onBackground)
        }
        item {
            val muscleGroups = listOf(
                "Chest" to Icons.Default.AccessibilityNew,
                "Back" to Icons.Default.Accessibility,
                "Legs" to Icons.Default.DirectionsRun,
                "Shoulders" to Icons.Default.EmojiPeople,
                "Arms" to Icons.Default.FitnessCenter,
                "Core" to Icons.Default.SelfImprovement
            )

            Column {
                muscleGroups.chunked(2).forEach { rowItems ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        rowItems.forEach { (name, icon) ->
                            MuscleCard(
                                name,
                                icon,
                                Modifier.weight(1f),
                                onClick = { onNavigateToSelection(name) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Quick Start", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                Text("View All", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        items(listOf(
            "Push Day" to "5 exercises • 60 min",
            "Pull Day" to "5 exercises • 60 min",
            "Leg Day" to "6 exercises • 70 min"
        )) { (title, sub) ->
            QuickStartItem(title, sub, onClick = onNavigateToActiveWorkout)
        }
    }
}

// --- TAB 2: HISTORY (DENGAN FITUR DELETE) ---
@Composable
fun HistoryContent(
    historyList: List<WorkoutSession>,
    selectionState: com.example.projekuas.data.HistorySelectionState,
    onToggleSelection: (String) -> Unit,
    onLongPress: (String) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(20.dp)) {
        item {
            // Header Card dihapus jika dalam mode seleksi agar bersih
            if (!selectionState.isSelectionMode) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("All Time", color = MaterialTheme.colorScheme.onPrimary.copy(0.7f), fontSize = 12.sp)
                            Text("Keep Going!", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                                Column {
                                    Text("Total Sessions", color = MaterialTheme.colorScheme.onPrimary.copy(0.7f), fontSize = 10.sp)
                                    Text("${historyList.size}", color = MaterialTheme.colorScheme.onPrimary, fontSize = 14.sp)
                                }
                            }
                        }
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(40.dp))
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text("Recent Workouts", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(10.dp))
            } else {
                Text("Select items to delete", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(bottom=10.dp))
            }
        }

        if (historyList.isEmpty()) {
            item {
                Text("Belum ada riwayat latihan.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 20.dp))
            }
        } else {
            items(historyList) { workout ->
                WorkoutHistoryCard(
                    workout = workout,
                    isSelectionMode = selectionState.isSelectionMode,
                    isSelected = selectionState.selectedIds.contains(workout.sessionId),
                    onToggle = { onToggleSelection(workout.sessionId) },
                    onLongPress = { onLongPress(workout.sessionId) }
                )
            }
        }
    }
}

// --- KOMPONEN PENDUKUNG ---

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkoutHistoryCard(
    workout: WorkoutSession,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onToggle: () -> Unit,
    onLongPress: () -> Unit
) {
    val dateString = remember(workout.dateMillis) {
        SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(workout.dateMillis))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Indikator Seleksi (Bulat O / Centang)
        if (isSelectionMode) {
            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) GymPurple else Color.Transparent)
                    .border(2.dp, if (isSelected) GymPurple else Color.Gray, CircleShape)
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .combinedClickable(
                    onClick = {
                        if (isSelectionMode) onToggle() else { /* Buka Detail Nanti */ }
                    },
                    onLongClick = { onLongPress() }
                ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(16.dp),
            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, GymPurple) else null
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header Card
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        val muscleName = workout.exercises.firstOrNull()?.muscleGroup ?: "Mixed"
                        Text("$muscleName Workout", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                            Text(" $dateString", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))

                            val mins = workout.durationSeconds / 60
                            Text(" $mins min", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFFF5722), modifier = Modifier.size(14.dp))
                            Text(" ${workout.totalCaloriesBurned}", color = Color(0xFFFF5722), fontWeight = FontWeight.Bold)
                        }
                        Text("calories", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                    }
                }

                if(workout.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Note: ${workout.note}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.8f),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 12.dp))

                if (workout.exercises.isEmpty()) {
                    Text("- No detail exercises -", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    workout.exercises.take(3).forEach { ex ->
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(ex.exerciseName, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            val bestSet = ex.sets.maxByOrNull { it.weightKg }
                            if (bestSet != null) {
                                Text("${ex.sets.size} Sets • Best: ${bestSet.weightKg}kg", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatBadge(icon: ImageVector, value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(90.dp)
            .background(Color.White.copy(0.15f), RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(label, color = Color.White.copy(0.8f), fontSize = 10.sp)
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(25.dp))
            .background(if (isSelected) Color.White else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (isSelected) GymPurple else Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MuscleCard(name: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(120.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Text("12 exercises", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
        }
    }
}

@Composable
fun QuickStartItem(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Icon(
                Icons.Rounded.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape).padding(4.dp)
            )
        }
    }
}