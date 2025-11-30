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
import com.example.projekuas.R // Pastikan resource R.drawable.image3 tersedia
import com.example.projekuas.data.GymClass
import com.example.projekuas.viewmodel.HomeViewModelFactory
import com.example.projekuas.viewmodel.TrainerViewModel
import java.text.SimpleDateFormat
import java.util.*

// Warna Tema Biru (Sesuai Referensi)
val ScheduleBluePrimary = Color(0xFF1E88E5)
val ScheduleBlueDark = Color(0xFF1565C0)
val ScheduleCardBg = Color(0xFFF5F7FA)

@Composable
fun TrainerScheduleScreen(
    factory: HomeViewModelFactory,
    onNavigateToClassForm: (String?) -> Unit
) {
    val viewModel: TrainerViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsState()

    // Trigger refresh saat layar dibuka
    LaunchedEffect(Unit) { viewModel.refreshData() }

    Scaffold(
        floatingActionButton = {
            // Floating Button Tambah Kelas
            FloatingActionButton(
                onClick = { onNavigateToClassForm(null) },
                containerColor = ScheduleBluePrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Class")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .verticalScroll(rememberScrollState()) // Tambahkan scroll agar aman di layar kecil
        ) {
            // 1. HEADER SECTION (Blue Gradient + Stats)
            // Menggunakan 0 untuk totalMembers karena data ini tidak tersedia di ViewModel yang ada
            ScheduleHeader(
                classesCount = state.classesThisWeek,
                totalMembers = 0,
                totalHours = state.totalHoursThisWeek,
                // TODO: Ganti onNavigateBack dengan logika navigasi yang benar
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
                    CircularProgressIndicator(color = ScheduleBluePrimary)
                }
            } else {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        "${state.filteredClasses.size} Classes Scheduled",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
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
            .background(Brush.verticalGradient(listOf(ScheduleBluePrimary, ScheduleBlueDark)))
            .padding(20.dp)
    ) {
        Column {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White) }
                    Spacer(Modifier.width(16.dp))
                    Text("My Schedule", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Row {
                    IconButton(onClick = {}) { Icon(Icons.Default.FilterList, null, tint = Color.White) }
                    // Tombol Add di sini sudah diganti dengan FAB di Scaffold, tapi dipertahankan jika ingin di header juga
                    IconButton(onClick = {}) { Icon(Icons.Default.Add, null, tint = Color.White) }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stats Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HeaderStatItem(Icons.Default.DateRange, "$classesCount", "Classes This Week", Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                HeaderStatItem(Icons.Outlined.Group, "N/A", "Total Booked", Modifier.weight(1f)) // Menggunakan N/A karena totalMembers tidak dihitung di ViewModel Anda
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
        cal.add(Calendar.DAY_OF_YEAR, -3) // Start from 3 days ago
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
            // Perbandingan tanggal tanpa mempertimbangkan waktu
            val isSelected = date.get(Calendar.DAY_OF_YEAR) == selectedCal.get(Calendar.DAY_OF_YEAR) &&
                    date.get(Calendar.YEAR) == selectedCal.get(Calendar.YEAR)

            val dayName = SimpleDateFormat("EEE", Locale("id", "ID")).format(date.time).take(3) // Sab, Min, Sen
            val dayNum = SimpleDateFormat("dd", Locale.getDefault()).format(date.time)

            Column(
                modifier = Modifier
                    .width(60.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) ScheduleBluePrimary else Color.White)
                    .clickable { onDateSelected(date.timeInMillis) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Indikator Dot untuk Hari Ini (Jika bukan hari yang dipilih)
                if (isSelected) {
                    Box(Modifier.size(4.dp).background(Color.White, CircleShape))
                    Spacer(Modifier.height(4.dp))
                }

                Text(
                    text = dayName,
                    color = if (isSelected) Color.White.copy(0.8f) else Color.Gray,
                    fontSize = 12.sp
                )
                Text(
                    text = dayNum,
                    color = if (isSelected) Color.White else Color.Black,
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Left: Image
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .fillMaxHeight()
            ) {
                // Logic load Base64 Image
                val bitmap = remember(gymClass.imageUrl) {
                    try {
                        // Hilangkan prefix "data:image/jpeg;base64," jika ada
                        val pureBase64 = gymClass.imageUrl.substringAfter(",")
                        val bytes = Base64.decode(pureBase64, Base64.DEFAULT)
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
                    // Fallback Image
                    Image(
                        painter = painterResource(R.drawable.image3), // Pastikan R.drawable.image3 ada
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    )
                }

                // Badge Jam (Biru di pojok kiri atas gambar)
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(ScheduleBluePrimary, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(start, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Right: Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(gymClass.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Text(" ${gymClass.durationMinutes} minutes", color = Color.Gray, fontSize = 12.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Group, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Text(" ${gymClass.currentBookings}/${gymClass.capacity} participants", fontSize = 12.sp, color = Color.Gray)
                        // TODO: Tambahkan Studio Room 2 di sini
                    }

                    // Status Badge (Contoh: Upcoming)
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "Upcoming",
                            color = Color(0xFF2E7D32),
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
        colors = CardDefaults.cardColors(containerColor = ScheduleCardBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Week Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                // Kolom Kiri
                Column(modifier = Modifier.weight(1f)) {
                    // Total Classes
                    Text("Total Classes", color = Color.Gray, fontSize = 12.sp)
                    Text("$totalClasses sessions", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

                    Spacer(Modifier.height(12.dp))

                    // Avg Participants
                    Text("Avg Participants", color = Color.Gray, fontSize = 12.sp)
                    val formattedAvg = String.format(Locale.getDefault(), "%.1f", avgParticipants)
                    Text("$formattedAvg per class", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }

                // Kolom Kanan
                Column(modifier = Modifier.weight(1f)) {
                    // Total Hours
                    Text("Total Hours", color = Color.Gray, fontSize = 12.sp)
                    Text("$totalHours hours", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

                    Spacer(Modifier.height(12.dp))

                    // Attendance Rate
                    Text("Attendance Rate", color = Color.Gray, fontSize = 12.sp)
                    val formattedRate = "${attendanceRate.toInt()}%"
                    Text(formattedRate, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
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
        Text("No classes scheduled", color = Color.Gray)
        Text("Tap '+' to add a class", color = ScheduleBluePrimary, fontSize = 12.sp, modifier = Modifier.clickable { /* TBD */ })
    }
}