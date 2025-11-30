package com.example.projekuas.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.text.font.FontWeight
import com.example.projekuas.utils.rememberBitmapFromBase64
import com.example.projekuas.viewmodel.MemberStatItem
import com.example.projekuas.viewmodel.TrainerViewModel
import java.util.Locale

// Konstanta Warna
val PrimaryBlue = Color(0xFF1976D2)
private val BackgroundGray = Color(0xFFF5F5F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailScreen(
    navController: NavController,
    viewModel: TrainerViewModel,
    memberId: String
) {
    // Mengambil UI State dari ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // Akses daftar anggota melalui uiState.myMembers
    val memberStat = uiState.myMembers.find { it.memberId == memberId }

    if (memberStat == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (uiState.isLoading) {
                CircularProgressIndicator(color = PrimaryBlue)
                Text("Loading member data...", modifier = Modifier.align(Alignment.Center).offset(y = 50.dp))
            } else {
                Text("Member not found (ID: $memberId)")
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Member Profile", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundGray)
        ) {
            // 1. Header Profil dan Kontak
            item {
                HeaderProfileSection(
                    member = memberStat,
                    navController = navController,
                    memberId = memberId
                )
            }

            // 2. Bagian Performa (Stats)
            item {
                Spacer(modifier = Modifier.height(16.dp))
                PerformanceSection(member = memberStat)
            }

            // 3. Informasi Kontak Detail
            item {
                Spacer(modifier = Modifier.height(16.dp))
                ContactInfoSection(member = memberStat)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// --- SUB-KOMPONEN UI ---

@Composable
fun HeaderProfileSection(member: MemberStatItem, navController: NavController, memberId: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Gambar Profil atau Inisial
            // Variabel 'bitmap' di sini sudah bertipe ImageBitmap (jika sukses di-decode oleh rememberBitmapFromBase64)
            val bitmap = rememberBitmapFromBase64(member.profilePictureUrl)

            // Perbaikan untuk menampilkan gambar Base64 atau inisial
            if (bitmap != null) {
                Image(
                    // FIX: Langsung menggunakan variabel bitmap (yang sudah bertipe ImageBitmap)
                    bitmap = bitmap,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = PrimaryBlue
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = member.name.take(1).uppercase(Locale.getDefault()),
                            fontSize = 40.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(member.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)

            // Format joinDate dari Long (milidetik) ke string
            val joinDate = try {
                java.text.SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(java.util.Date(member.joinDate))
            } catch (e: Exception) { "-" }

            Text("Member since $joinDate", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                // Tombol Chat (Navigasi ke ChatScreen)
                ContactButton(
                    icon = Icons.Default.Chat,
                    text = "Message",
                    onClick = {
                        // Navigasi ke ChatScreen (chat_screen/{memberId})
                        if (memberId.isNotBlank()) {
                            navController.navigate("chat_screen/$memberId")
                        }
                    }
                )

            }
        }
    }
}

@Composable
fun PerformanceSection(member: MemberStatItem) {
    // Menghitung data yang lebih akurat dari MemberStatItem
    val attendanceRate = "${member.attendanceRate.toInt()}%"
    val progressLabel = when {
        member.progressRating >= 4.0 -> "Excellent"
        member.progressRating >= 2.5 -> "Good"
        else -> "Fair"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Performance", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            PerformanceRow(label = "Attendance Rate", value = attendanceRate)
            PerformanceRow(label = "Classes Completed", value = "${member.totalClasses}")
            // Menggunakan properti progressRating dari MemberStatItem
            PerformanceRow(label = "Average Rating", value = "${member.progressRating} (${progressLabel})")
        }
    }
}

@Composable
fun ContactInfoSection(member: MemberStatItem) {
    // Menggunakan placeholder untuk Phone Number karena tidak ada di MemberStatItem
    val dummyPhoneNumber = "N/A"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Contact Information", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow(label = "Email", value = member.email)
            InfoRow(label = "Phone", value = dummyPhoneNumber)
            InfoRow(label = "Status", value = member.activeStatus)
        }
    }
}

@Composable
fun ContactButton(icon: ImageVector, text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE3F2FD),
            contentColor = PrimaryBlue
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(icon, contentDescription = text, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun PerformanceRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 16.sp)
        Text(value, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 16.sp)
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Medium)
    }
}