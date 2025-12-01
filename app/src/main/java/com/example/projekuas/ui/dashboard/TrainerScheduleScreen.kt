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
import androidx.compose.ui.text.style.TextAlign
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
    val state by viewModel.uiState.collectAsState()


    val dates = remember {
        (0..6).map {
            java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, it) }
        }
    }
    var selectedDateIndex by remember { mutableIntStateOf(0) }
    // Trigger refresh saat layar dibuka
    LaunchedEffect(Unit) { viewModel.refreshData() }

    Scaffold(
        containerColor = Color(0xFFF5F9FF),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToClassForm(null) },
                containerColor = Color(0xFF1E88E5), // Trainer Blue
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "Add Class")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            // Header
            Text(
                text = "My Schedule",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E1E),
                modifier = Modifier.padding(start = 24.dp, top = 40.dp, bottom = 20.dp)
            )

            // Date Strip (Horizontal Scroll)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(dates.size) { index ->
                    val date = dates[index]
                    val isSelected = selectedDateIndex == index
                    val bgColor = if (isSelected) Color(0xFF1E88E5) else Color.White
                    val textColor = if (isSelected) Color.White else Color.Gray

                    Column(
                        modifier = Modifier
                            .width(60.dp)
                            .height(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(bgColor)
                            .clickable { selectedDateIndex = index }
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Hari (Sen, Sel, Rab)
                        Text(
                            text = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault()).format(date.time),
                            color = textColor.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                        // Tanggal (01, 02)
                        Text(
                            text = java.text.SimpleDateFormat("dd", java.util.Locale.getDefault()).format(date.time),
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Class List
            // Container Putih melengkung di bawah
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Color.White)
                    .padding(24.dp)
            ) {
                Text(
                    text = "Classes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Gunakan LazyColumn untuk list
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp), // Ruang untuk Floating Nav
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Contoh Item Jadwal (Ganti dengan data asli dari ViewModel nanti)
                    items(3) {
                        TrainerScheduleCard(
                            time = "09:00 AM",
                            title = "Yoga Flow",
                            participants = "12/20"
                        )
                    }
                }
            }
        }
    }
}

// --- KOMPONEN PENDUKUNG ---

@Composable
fun TrainerScheduleCard(time: String, title: String, participants: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Waktu
        Text(
            text = time,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.width(70.dp)
        )

        // Kartu
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8FF)), // Biru sangat muda
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(30.dp)
                        .background(Color(0xFF1E88E5), RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(text = title, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E1E))
                    Spacer(Modifier.height(4.dp))
                    Text(text = "$participants Participants", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

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
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val startTime = timeFormat.format(Date(gymClass.startTimeMillis))
    // Estimasi selesai
    val endTime = timeFormat.format(Date(gymClass.startTimeMillis + (gymClass.durationMinutes * 60000)))

    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Waktu
        Column(
            modifier = Modifier.width(60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(startTime, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E1E1E))
            Text(endTime, fontSize = 12.sp, color = Color.Gray)
        }

        Spacer(Modifier.width(12.dp))

        // Kartu
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F9FF)), // Biru sangat muda
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp)) {
                // Gambar Kecil
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

                Column(verticalArrangement = Arrangement.Center, modifier = Modifier.height(60.dp)) {
                    Text(gymClass.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E1E1E), maxLines = 1)
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