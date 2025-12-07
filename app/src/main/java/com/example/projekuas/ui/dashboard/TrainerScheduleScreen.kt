package com.example.projekuas.ui.dashboard

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projekuas.R
import com.example.projekuas.data.GymClass
import com.example.projekuas.viewmodel.HomeViewModelFactory
import com.example.projekuas.viewmodel.TrainerViewModel
import java.text.SimpleDateFormat
import java.util.*

// Kita tetap simpan warna branding, tapi gunakan MaterialTheme untuk surface/background

@Composable
fun TrainerScheduleScreen(
    factory: HomeViewModelFactory,
    onNavigateToClassForm: (String?) -> Unit
) {
    val viewModel: TrainerViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.refreshData() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background, // FIX: Adaptif Tema
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToClassForm(null) },
                containerColor = TrainerBluePrimary,
                contentColor = Color.White,
                modifier = Modifier.padding(bottom = 80.dp) // Lift FAB above Bottom Nav
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Class")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background) // FIX: Adaptif Tema
                .verticalScroll(rememberScrollState())
        ) {
            // 1. HEADER SECTION
            ScheduleHeader(
                classesCount = state.classesThisWeek,
                totalMembers = 0,
                totalHours = state.totalHoursThisWeek,
                onNavigateBack = { /* Implement global back */ }
            )

            // 2. CALENDAR STRIP
            DateSelectorStrip(
                selectedDate = state.selectedDate,
                onDateSelected = { viewModel.onDateSelected(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. CLASS LIST
            if (state.isLoading && state.classesTaught.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TrainerBluePrimary)
                }
            } else {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        "${state.filteredClasses.size} Classes Scheduled",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground, // FIX: Warna Teks Adaptif
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (state.filteredClasses.isEmpty()) {
                        EmptyScheduleState()
                    } else {
                        state.filteredClasses.forEach { gymClass ->
                            ClassSessionCard(
                                gymClass = gymClass,
                                onClick = { onNavigateToClassForm(gymClass.classId) }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 4. WEEK SUMMARY
                    WeekSummaryCard(
                        totalClasses = state.classesThisWeek,
                        totalHours = state.totalHoursThisWeek,
                        avgParticipants = state.avgParticipants,
                        attendanceRate = state.attendanceRate
                    )

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

// --- COMPONENT: HEADER ---
@Composable
fun ScheduleHeader(classesCount: Int, totalMembers: Int, totalHours: Int, onNavigateBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(Brush.verticalGradient(listOf(TrainerBluePrimary, TrainerBlueDark)))
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Back button dihapus atau disesuaikan jika perlu
                    Text("My Schedule", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HeaderStatItem(Icons.Default.DateRange, "$classesCount", "Classes This Week", Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                HeaderStatItem(Icons.Outlined.Group, "N/A", "Total Booked", Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                HeaderStatItem(Icons.Outlined.Schedule, "${totalHours}h", "Total Hours", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun HeaderStatItem(icon: ImageVector, value: String, label: String, modifier: Modifier) {
    Box(
        modifier = modifier
            .height(100.dp)
            .background(Color.White.copy(0.15f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(label, color = Color.White.copy(0.8f), fontSize = 10.sp, lineHeight = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

// --- COMPONENT: CALENDAR STRIP ---
@Composable
fun DateSelectorStrip(selectedDate: Long, onDateSelected: (Long) -> Unit) {
    val dates = remember {
        val list = mutableListOf<Calendar>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -3)
        for (i in 0..13) {
            list.add(cal.clone() as Calendar)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    val selectedCal = Calendar.getInstance().apply { timeInMillis = selectedDate }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(dates) { date ->
            val isSelected = date.get(Calendar.DAY_OF_YEAR) == selectedCal.get(Calendar.DAY_OF_YEAR) &&
                    date.get(Calendar.YEAR) == selectedCal.get(Calendar.YEAR)

            val dayName = SimpleDateFormat("EEE", Locale("id", "ID")).format(date.time).take(3)
            val dayNum = SimpleDateFormat("dd", Locale.getDefault()).format(date.time)

            // FIX: Warna Adaptif
            val backgroundColor = if (isSelected) TrainerBluePrimary else MaterialTheme.colorScheme.surfaceVariant
            val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

            Column(
                modifier = Modifier
                    .width(60.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(backgroundColor)
                    .clickable { onDateSelected(date.timeInMillis) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isSelected) {
                    Box(Modifier.size(4.dp).background(Color.White, CircleShape))
                    Spacer(Modifier.height(4.dp))
                }

                Text(
                    text = dayName,
                    color = textColor.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
                Text(
                    text = dayNum,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}

// --- COMPONENT: CLASS CARD ---
@Composable
fun ClassSessionCard(gymClass: GymClass, onClick: () -> Unit) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val start = timeFormat.format(Date(gymClass.startTimeMillis))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        // FIX: Warna Card Adaptif
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.width(110.dp).fillMaxHeight()) {
                val bitmap = remember(gymClass.imageUrl) {
                    try {
                        val pureBase64 = gymClass.imageUrl.substringAfter(",")
                        val bytes = android.util.Base64.decode(pureBase64, android.util.Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    } catch (e: Exception) { null }
                }

                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.image3),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(TrainerBluePrimary, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(start, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // FIX: Warna Teks Adaptif
                    Text(gymClass.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                        Text(" ${gymClass.durationMinutes} minutes", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Group, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Text(" ${gymClass.currentBookings}/${gymClass.capacity} participants", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    // Status Badge
                    val (containerColor, contentColor) = when (gymClass.status) {
                        "Ongoing" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
                        "Finished" -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
                    }

                    Surface(
                        color = containerColor,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            gymClass.status,
                            color = contentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- COMPONENT: WEEK SUMMARY ---
@Composable
fun WeekSummaryCard(
    totalClasses: Int,
    totalHours: Int,
    avgParticipants: Double,
    attendanceRate: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        // FIX: Warna Card Adaptif
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Week Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Total Classes", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Text("$totalClasses sessions", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)

                    Spacer(Modifier.height(12.dp))

                    Text("Avg Participants", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    val formattedAvg = String.format(Locale.getDefault(), "%.1f", avgParticipants)
                    Text("$formattedAvg per class", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("Total Hours", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Text("$totalHours hours", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)

                    Spacer(Modifier.height(12.dp))

                    Text("Attendance Rate", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    val formattedRate = "${attendanceRate.toInt()}%"
                    Text(formattedRate, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
fun EmptyScheduleState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("No classes scheduled", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Tap '+' to add a class", color = TrainerBluePrimary, fontSize = 12.sp, modifier = Modifier.clickable { /* TBD */ })
    }
}