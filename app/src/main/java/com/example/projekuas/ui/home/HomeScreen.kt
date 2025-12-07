@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.projekuas.ui.home

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projekuas.R
import com.example.projekuas.data.GymClass
import com.example.projekuas.ui.theme.GymOrange
import com.example.projekuas.ui.theme.GymPurple
import com.example.projekuas.utils.rememberBitmapFromBase64
import com.example.projekuas.viewmodel.ClassBookingViewModel
import com.example.projekuas.viewmodel.DashboardViewModel
import com.example.projekuas.viewmodel.ThemeViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Konstanta Rute
const val TRAINER_MEMBERS_ROUTE = "trainer_members_route"
const val MEMBER_DETAIL_ROUTE = "member_detail/{memberId}"

val HomeClassImages = listOf(
    R.drawable.image3,
    R.drawable.image4,
    R.drawable.image5,
    R.drawable.image7,
    R.drawable.image9
)

fun base64ToBitmapHome(base64String: String): android.graphics.Bitmap? {
    return try {
        val pureBase64Encoded = if (base64String.contains(",")) {
            base64String.substringAfter(",")
        } else {
            base64String
        }
        val decodedBytes = Base64.decode(pureBase64Encoded, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// ==========================================
// --- 1. MEMBER DASHBOARD SCREEN (RESTORED UI + DARK MODE FIX) ---
// ==========================================

@OptIn(ExperimentalMaterial3Api::class) // Pastikan opt-in ini ada
@Composable
fun MemberDashboardScreen(
    dashboardViewModel: DashboardViewModel,
    classViewModel: ClassBookingViewModel,
    themeViewModel: ThemeViewModel,
    onNavigateToBooking: () -> Unit,
    onNavigateToWorkoutLog: () -> Unit,
    onNavigateToMembership: () -> Unit,
    onNavigateToProfileTab: () -> Unit,
    onNavigateToSelection: (String) -> Unit,
    onNavigateToActiveWorkout: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToChatList: () -> Unit
) {
    val state by dashboardViewModel.dashboardState.collectAsState()
    val bookingState by classViewModel.state.collectAsState()

    // State Refresh
    var isRefreshing by remember { mutableStateOf(false) }
    var selectedClass by remember { mutableStateOf<GymClass?>(null) } // [NEW] Class Detail State
    val scope = rememberCoroutineScope()

    // Fungsi Refresh
    fun onRefresh() {
        isRefreshing = true
        scope.launch {
            // TODO: Panggil fungsi refresh data dari ViewModel di sini
            delay(1500)
            isRefreshing = false
        }
    }

    // Upcoming Classes Logic
    val upcomingClasses = remember(bookingState.classes) {
        val now = System.currentTimeMillis()
        bookingState.classes
            .filter { it.startTimeMillis > now }
            .sortedBy { it.startTimeMillis }
            .take(3)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        // --- PERUBAHAN UTAMA DI SINI ---
        // LazyColumn dibungkus oleh PullToRefreshBox
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { onRefresh() },
            modifier = Modifier.padding(padding) // Padding Scaffold dipindah ke Box ini
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(), // Hapus padding(padding) dari sini
                contentPadding = PaddingValues(bottom = 120.dp) // Tambahan padding bawah agar konten terbawah tidak kepotong
            ) {
                // 1. HEADER UNGU (Solid/Dark Gradient)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp) // Reduced height slightly
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(MaterialTheme.colorScheme.primary, Color(0xFF4A148C)) // Purple to Dark Purple
                                ),
                                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                            )
                            .padding(24.dp)
                    ) {
                        Column {
                            // Top Row (Profile & Notif)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val bitmap = rememberBitmapFromBase64(state.profilePictureUrl)
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap,
                                            contentDescription = null,
                                            modifier = Modifier.size(50.dp).clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Surface(modifier = Modifier.size(50.dp), shape = CircleShape, color = Color.White.copy(0.3f)) {
                                            Icon(Icons.Default.Person, null, tint = Color.White)
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text("Welcome back,", color = Color.White.copy(0.9f), fontSize = 12.sp)
                                        Text(
                                            state.name.ifBlank { "Member" },
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    }
                                }

                                // Tombol Kanan
                                Row {
                                    IconButton(onClick = { themeViewModel.toggleTheme() }) {
                                        Icon(
                                            imageVector = if (themeViewModel.isDarkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                                            contentDescription = "Theme",
                                            tint = Color.White
                                        )
                                    }
                                    IconButton(onClick = onNavigateToNotifications) {
                                        Icon(Icons.Outlined.Notifications, null, tint = Color.White)
                                    }
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            // Stats Cards Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatCardHeader(icon = Icons.Default.FitnessCenter, value = "3", label = "Workouts")
                                StatCardHeader(icon = Icons.Default.AccessTime, value = "36h", label = "Hours")
                                StatCardHeader(icon = Icons.Default.LocalFireDepartment, value = "12k", label = "Calories")
                            }
                        }
                    }
                }

                // 2. GRID MENU (Overlapping Card)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .offset(y = (-30).dp), // Negative Offset to Overlap
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(4.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            // Row 1 (3 Buttons)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MenuButton(Icons.Default.FitnessCenter, "Workout", Color(0xFFFFF3E0), Color(0xFFFF9800), onNavigateToWorkoutLog)
                                MenuButton(Icons.Default.CalendarMonth, "Book Class", Color(0xFFE3F2FD), Color(0xFF2196F3), onNavigateToBooking)
                                MenuButton(Icons.Default.QrCode, "QR Code", Color(0xFFE8F5E9), Color(0xFF4CAF50), onNavigateToMembership)
                            }

                            Spacer(Modifier.height(20.dp))

                            // Row 2 (2 Buttons)
                             Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start // Left aligned or maybe SpaceEvenly? Let's use Start with spacing
                            ) {
                                MenuButton(Icons.Default.Person, "Profile", Color(0xFFF3E5F5), Color(0xFF9C27B0), onNavigateToProfileTab)
                                Spacer(Modifier.width(28.dp)) // Manual spacing to align largely with top row columns
                                MenuButton(Icons.Default.Chat, "Consultation", Color(0xFFFCE4EC), Color(0xFFE91E63), onNavigateToChatList)
                            }
                        }
                    }
                }

                // 3. UPCOMING CLASSES
                item {
                    if (upcomingClasses.isNotEmpty()) {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            SectionTitle(title = "Upcoming Classes", onSeeAll = onNavigateToBooking)
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(upcomingClasses) { gymClass ->
                                    // FIX: Update onClick to show detail popup
                                    PopularClassItem(gymClass = gymClass, onClick = { selectedClass = gymClass })
                                }
                            }
                        }
                    }
                }

                // 4. BANNER MEMBERSHIP
                item {
                    Spacer(Modifier.height(8.dp))
                    // Dynamic Card Color
                    val cardColor = when (state.membershipType.lowercase()) {
                        "elite", "gold" -> Color(0xFFEAB308) // Gold
                        "pro", "platinum" -> Color(0xFF3B82F6) // Blue
                        "standard", "basic" -> Color(0xFF4B5563) // Gray
                        else -> Color(0xFF212121) // Dark default
                    }
                    val textColor = if (cardColor == Color(0xFF212121)) GymOrange else Color.White

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(100.dp)
                            .clickable { onNavigateToMembership() },
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(Modifier.fillMaxSize().padding(16.dp)) {
                            Column(Modifier.align(Alignment.CenterStart)) {
                                Text("Membership Status", color = textColor, fontSize = 12.sp)
                                Text(state.membershipType.ifBlank { "No Membership" }, color = textColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("View Details >", color = textColor, fontSize = 12.sp, modifier = Modifier.align(Alignment.BottomEnd))
                        }
                    }
                    Spacer(Modifier.height(120.dp))
                }
            }
        }
    }

    if (selectedClass != null) {
        ClassDetailDialog(gymClass = selectedClass!!, onDismiss = { selectedClass = null })
    }
}


// --- KOMPONEN PENDUKUNG (Disesuaikan untuk Dark Mode) ---

@Composable
fun StatCardHeader(icon: ImageVector, value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(Color.White.copy(alpha = 0.15f)) // Transparan Putih
            .padding(vertical = 15.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
    }
}

@Composable
fun MenuButton(icon: ImageVector, label: String, bgColor: Color, iconTint: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Surface(
            modifier = Modifier.size(70.dp),
            shape = RoundedCornerShape(16.dp),
            color = bgColor, // Custom specific color
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = iconTint) // Custom specific tint
            }
        }
        Spacer(Modifier.height(8.dp))
        // FIX: Teks mengikuti warna onBackground (Hitam/Putih)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun SectionTitle(title: String, onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // FIX: Warna Teks Judul
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        TextButton(onClick = onSeeAll) {
            Text(
                text = "See All",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun PopularClassItem(gymClass: GymClass, onClick: () -> Unit) {
    val imageIndex = kotlin.math.abs(gymClass.classId.hashCode()) % HomeClassImages.size
    val selectedImage = HomeClassImages[imageIndex]
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    val timeString = try { timeFormat.format(Date(gymClass.startTimeMillis)) } catch (e:Exception) { "00:00" }
    val dateString = try { dateFormat.format(Date(gymClass.startTimeMillis)) } catch (e:Exception) { "" }

    Card(
        modifier = Modifier
            .width(220.dp)
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        // FIX: Warna Kartu mengikuti tema
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Box(modifier = Modifier.height(90.dp).fillMaxWidth()) {
                Image(
                    painter = painterResource(id = selectedImage),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    modifier = Modifier.padding(8.dp).align(Alignment.TopStart),
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("$dateString, $timeString", color = MaterialTheme.colorScheme.onPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal=6.dp, vertical=2.dp))
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                // FIX: Warna Teks
                Text(gymClass.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(gymClass.trainerName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
    }


}

}
