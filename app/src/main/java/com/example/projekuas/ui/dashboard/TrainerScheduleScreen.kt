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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
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

// Warna Tema
val TrainerBlue = Color(0xFF1E88E5)

@Composable
fun TrainerScheduleScreen(
    factory: HomeViewModelFactory,
    onNavigateToClassForm: (String?) -> Unit
) {
    val viewModel: TrainerViewModel = viewModel(factory = factory)
    // Mengambil state real-time dari ViewModel (yang terhubung ke Firebase)
    val state by viewModel.uiState.collectAsState()

    // Trigger refresh data saat layar dibuka untuk memastikan sinkronisasi awal
    LaunchedEffect(Unit) { viewModel.refreshData() }

    Scaffold(
        containerColor = TrainerBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToClassForm(null) }, // null = Mode Tambah Baru
                containerColor = TrainerBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Class")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. HEADER TANGGAL (Bulan & Tahun dari Selected Date)
            val headerDateFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            val headerDate = headerDateFormatter.format(Date(state.selectedDate))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "My Schedule",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = headerDate,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E1E)
                    )
                }
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = TrainerBlue,
                    modifier = Modifier.size(28.dp)
                )
            }

            // 2. CALENDAR STRIP (Scroll Horizontal)
            // Generate 14 hari (2 hari lalu s/d 11 hari ke depan)
            val dateList = remember {
                val list = mutableListOf<Long>()
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -2)
                for (i in 0..13) {
                    list.add(cal.timeInMillis)
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }
                list
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(dateList) { dateMillis ->
                    DateSelectorItem(
                        dateMillis = dateMillis,
                        isSelected = isSameDay(dateMillis, state.selectedDate),
                        onClick = { viewModel.onDateSelected(dateMillis) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // 3. LIST JADWAL (REALTIME DARI FIREBASE)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
                    )
                    .padding(24.dp)
            ) {
                val displayDate = SimpleDateFormat("EEEE, dd MMM", Locale.getDefault()).format(Date(state.selectedDate))

                Text(
                    text = displayDate,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E1E),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Kondisi Loading, Kosong, atau Ada Data
                if (state.isLoading) {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TrainerBlue)
                    }
                } else if (state.filteredClasses.isEmpty()) {
                    EmptyStateSchedule()
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // [PERBAIKAN UTAMA] Menggunakan data asli filteredClasses dari State
                        items(state.filteredClasses) { gymClass ->
                            ScheduleCardItem(
                                gymClass = gymClass,
                                onClick = {
                                    // Kirim ID Kelas agar bisa diedit di ClassFormScreen
                                    onNavigateToClassForm(gymClass.classId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- KOMPONEN PENDUKUNG ---

@Composable
fun DateSelectorItem(dateMillis: Long, isSelected: Boolean, onClick: () -> Unit) {
    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault()) // Sen/Mon
    val dateFormat = SimpleDateFormat("dd", Locale.getDefault()) // 01
    val date = Date(dateMillis)

    val backgroundColor = if (isSelected) TrainerBlue else Color.White
    val contentColor = if (isSelected) Color.White else Color.Gray

    Column(
        modifier = Modifier
            .width(60.dp)
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = dayFormat.format(date).uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = contentColor.copy(alpha = 0.7f)
        )
        Text(
            text = dateFormat.format(date),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
        if (isSelected) {
            Box(Modifier.size(4.dp).background(Color.White, CircleShape))
        }
    }
}

@Composable
fun ScheduleCardItem(gymClass: GymClass, onClick: () -> Unit) {
    // Format Waktu dari Data Asli
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val startTime = timeFormat.format(Date(gymClass.startTimeMillis))
    // Hitung waktu selesai berdasarkan durasi
    val endTime = timeFormat.format(Date(gymClass.startTimeMillis + (gymClass.durationMinutes * 60000)))

    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Kolom Waktu
        Column(
            modifier = Modifier.width(60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(startTime, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E1E1E))
            Text(endTime, fontSize = 12.sp, color = Color.Gray)
        }

        Spacer(Modifier.width(12.dp))

        // Kartu Detail Kelas
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F9FF)), // Biru sangat muda
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp)) {
                // Gambar Kecil (Thumbnail)
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray)
                ) {
                    val bitmap = remember(gymClass.imageUrl) {
                        try {
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
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Fallback image resource
                        Image(
                            painter = painterResource(id = R.drawable.image3),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                // Detail Teks
                Column(verticalArrangement = Arrangement.Center, modifier = Modifier.height(60.dp)) {
                    Text(
                        text = gymClass.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1E1E1E),
                        maxLines = 1
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Text(" ${gymClass.durationMinutes} min", fontSize = 12.sp, color = Color.Gray)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Outlined.Group, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Text(" ${gymClass.currentBookings}/${gymClass.capacity}", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateSchedule() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(60.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("No classes scheduled", fontWeight = FontWeight.Bold, color = Color.Gray)
        Text("Tap + to create a new class", fontSize = 12.sp, color = Color.LightGray)
    }
}

fun isSameDay(date1: Long, date2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = date1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}