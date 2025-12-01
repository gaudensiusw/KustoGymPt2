package com.example.projekuas.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DarkMode // Tambahkan ini
import androidx.compose.material.icons.outlined.LightMode // Tambahkan ini
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projekuas.R
import com.example.projekuas.data.GymClass
import com.example.projekuas.viewmodel.HomeViewModelFactory
import com.example.projekuas.viewmodel.ThemeViewModel // Tambahkan ini
import com.example.projekuas.viewmodel.TrainerViewModel
import java.text.SimpleDateFormat
import java.util.*

// Warna Tema Trainer (Biru)
val TrainerBluePrimary = Color(0xFF1E88E5)
val TrainerBlueDark = Color(0xFF1565C0)
val TrainerBg = Color(0xFFF5F9FF)
val TextDark = Color(0xFF1E1E1E)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun TrainerDashboardScreen(
    factory: HomeViewModelFactory,
    themeViewModel: ThemeViewModel, // <--- 1. Parameter Baru
    onNavigateToClassForm: (String?) -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToMembers: () -> Unit
) {
    val viewModel: TrainerViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsState()
    val isRefreshing = state.isRefreshing
    var showEarningsDialog by remember { mutableStateOf(false) }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.refreshData() }
    )

    if (showEarningsDialog) {
        AlertDialog(
            onDismissRequest = { showEarningsDialog = false },
            icon = { Icon(Icons.Default.AttachMoney, null) },
            title = { Text("Earnings") },
            text = { Text("Fitur Earnings belum tersedia. Total pendapatan bulan ini diperkirakan: Rp 12.500.000") },
            confirmButton = { TextButton(onClick = { showEarningsDialog = false }) { Text("OK") } }
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }
    state.error?.let { msg -> LaunchedEffect(msg) { snackbarHostState.showSnackbar(msg); viewModel.clearMessages() } }
    state.successMessage?.let { msg -> LaunchedEffect(msg) { snackbarHostState.showSnackbar(msg); viewModel.clearMessages() } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Gunakan tema
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // 1. HEADER SECTION
            item {
                TrainerHeaderSection(
                    name = state.trainerName,
                    classesCount = state.classesTodayCount,
                    activeMembers = state.activeMembersCount,
                    performanceRate = state.performanceRate,
                    // 2. Kirim Data Tema ke Header
                    isDarkMode = themeViewModel.isDarkMode,
                    onToggleTheme = { themeViewModel.toggleTheme() }
                )
            }

            // 2. QUICK ACTIONS
            item {
                QuickActionsSection(
                    onScheduleClick = onNavigateToSchedule,
                    onMembersClick = onNavigateToMembers,
                    onEarningsClick = { showEarningsDialog = true }
                )
            }

            // 3. THIS MONTH PERFORMANCE
            item {
                MonthlyPerformanceCard(
                    sessionsCount = state.classesThisMonthCount,
                    satisfactionRate = state.performanceRate
                )
            }

            // 4. TODAY'S CLASSES LIST
            item {
                SectionHeader("Today's Classes", onSeeAll = onNavigateToSchedule)
                if (state.classesTaught.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("No classes found. Pull to refresh.", color = Color.Gray)
                    }
                }
            }

            val todayClasses = state.classesTaught.take(3)
            items(todayClasses) { gymClass ->
                TrainerClassItem(
                    gymClass = gymClass,
                    onEdit = { onNavigateToClassForm(gymClass.classId) },
                    onDelete = { viewModel.deleteClass(gymClass.classId) }
                )
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// --- UPDATE HEADER UNTUK ADA TOMBOL TEMA ---
@Composable
fun TrainerHeaderSection(
    name: String,
    classesCount: Int,
    activeMembers: Int,
    performanceRate: Double,
    isDarkMode: Boolean,      // <--- Parameter Baru
    onToggleTheme: () -> Unit // <--- Parameter Baru
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
            .background(Brush.verticalGradient(listOf(TrainerBluePrimary, TrainerBlueDark)))
            .padding(24.dp)
    ) {
        Column {
            // Row Header Atas (Welcome + Theme Toggle)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Welcome back,", color = Color.White.copy(0.7f), fontSize = 16.sp)
                    Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                }

                // Tombol Tema
                IconButton(onClick = onToggleTheme) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = Color.White
                    )
                }
            }

            Spacer(Modifier.height(30.dp))

            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TrainerStatCard(Icons.Default.CalendarToday, "$classesCount", "Classes Today", Modifier.weight(1f))
                    TrainerStatCard(Icons.Default.Group, "$activeMembers", "Active Members", Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val rating = (performanceRate / 100) * 5
                    val formattedRating = String.format("%.1f", rating)
                    TrainerStatCard(Icons.Default.TrendingUp, "Top 10%", "Ranking", Modifier.weight(1f))
                    TrainerStatCard(Icons.Default.Star, formattedRating, "Avg Rating", Modifier.weight(1f), isOrange = true)
                }
            }
        }
    }
}

// ... (Sisa komponen TrainerStatCard, QuickActionsSection, dll TETAP SAMA seperti file lama Anda)
@Composable
fun TrainerStatCard(icon: ImageVector, value: String, label: String, modifier: Modifier = Modifier, isOrange: Boolean = false) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Box(modifier = Modifier.size(24.dp).background(Color.White.copy(0.2f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
                Spacer(Modifier.height(4.dp))
                Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, maxLines = 1)
            }
            Text(label, color = Color.White.copy(0.8f), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Clip)
        }
    }
}

@Composable
fun QuickActionsSection(onScheduleClick: () -> Unit, onMembersClick: () -> Unit, onEarningsClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).offset(y = (-10).dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), // Gunakan surface color agar adaptif tema
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Quick Actions", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                QuickActionItem(Icons.Default.CalendarMonth, "Schedule", Color(0xFFE3F2FD), TrainerBluePrimary, onScheduleClick)
                QuickActionItem(Icons.Default.Group, "Members", Color(0xFFF3E5F5), Color(0xFF9C27B0), onMembersClick)
                QuickActionItem(Icons.Default.AttachMoney, "Earnings", Color(0xFFE8F5E9), Color(0xFF4CAF50), onEarningsClick)
            }
        }
    }
}

@Composable
fun QuickActionItem(icon: ImageVector, label: String, bg: Color, tint: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(modifier = Modifier.size(60.dp).background(bg, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun TrainerClassItem(gymClass: GymClass, onEdit: () -> Unit, onDelete: () -> Unit) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateObj = Date(gymClass.startTimeMillis)
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), // Adaptif tema
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (gymClass.imageUrl.isNotBlank()) {
                    val bitmap = remember(gymClass.imageUrl) {
                        try {
                            val pureBase64 = gymClass.imageUrl.substringAfter(",")
                            val bytes = android.util.Base64.decode(pureBase64, android.util.Base64.DEFAULT)
                            android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        } catch (e: Exception) { null }
                    }
                    if (bitmap != null) {
                        Image(bitmap = bitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else { Icon(Icons.Default.Image, null, tint = Color.White) }
                } else {
                    Image(painter = painterResource(id = R.drawable.image3), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(gymClass.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("${timeFormat.format(dateObj)} • ${gymClass.durationMinutes} min", color = Color.Gray, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Text(" ${gymClass.currentBookings}/${gymClass.capacity}", fontSize = 12.sp, color = Color.Gray)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Edit, null, tint = TrainerBluePrimary) }
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                }
            }
        }
    }
}

@Composable
fun MonthlyPerformanceCard(sessionsCount: Int, satisfactionRate: Double) {
    val formattedRate = String.format("%.0f%%", satisfactionRate)
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = TrainerBlueDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("This Month Performance", color = Color.White.copy(0.8f), fontSize = 12.sp)
                Text("Excellent Work!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(30.dp)) {
                    Column { Text("Classes Taught", color = Color.White.copy(0.7f), fontSize = 11.sp); Text("$sessionsCount sessions", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp) }
                    Column { Text("Satisfaction", color = Color.White.copy(0.7f), fontSize = 11.sp); Text(formattedRate, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp) }
                }
            }
            Icon(Icons.Default.TrendingUp, null, tint = Color.White, modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String, onSeeAll: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
        TextButton(onClick = onSeeAll) { Text("View All", color = TrainerBluePrimary) }
    }
}