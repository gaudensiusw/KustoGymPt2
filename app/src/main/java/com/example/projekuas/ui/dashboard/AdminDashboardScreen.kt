package com.example.projekuas.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projekuas.viewmodel.AdminViewModel
import java.text.NumberFormat
import java.util.Locale

// --- WARNA TEMA ADMIN ---
val AdminGreenPrimary = Color(0xFF4CAF50)
val AdminGreenDark = Color(0xFF2E7D32)
// Warna Teks khusus Light Mode (di Dark Mode kita pakai Color.White)

@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    onNavigateToReports: () -> Unit,
    onNavigateToTrainers: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToClasses: () -> Unit
) {
    val state by viewModel.dashboardState.collectAsState()
    val recentActivities by viewModel.recentActivities.collectAsState()

    // Deteksi Tema Sistem (Dark/Light)
    val isDark = isSystemInDarkTheme()

    // 1. Background Layar: Hitam di Dark Mode, Abu terang di Light Mode
    val backgroundColor = if (isDark) Color(0xFF121212) else Color(0xFFF5F7FA)

    Scaffold(
        containerColor = backgroundColor
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding()),
        ) {
            // --- BAGIAN 1: HEADER GRADIENT + STATISTIK ---
            // Bagian ini backgroundnya SELALU HIJAU, jadi teksnya SELALU PUTIH (Aman untuk kedua mode)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                        .background(Brush.verticalGradient(listOf(AdminGreenPrimary, AdminGreenDark)))
                        .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 48.dp)
                ) {
                    Column {
                        // Welcome Text
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Welcome back,",
                                    color = Color.White.copy(0.7f),
                                    fontSize = 16.sp
                                )
                                Text(
                                    "Admin",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
                            }
                            // Icon Notifikasi
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color.White.copy(0.2f), CircleShape)
                                    .clickable {},
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Notifications, null, tint = Color.White)
                            }
                        }

                        Spacer(Modifier.height(30.dp))

                        // Stats Grid (Glassmorphism - Transparan Putih)
                        Column(
                            Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                AdminStatCard(
                                    icon = Icons.Default.MonetizationOn,
                                    value = formatCurrency(state.totalRevenue),
                                    label = "Total Revenue",
                                    modifier = Modifier.weight(1f)
                                )
                                AdminStatCard(
                                    icon = Icons.Default.Group,
                                    value = state.activeMembers.toString(),
                                    label = "Active Members",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                AdminStatCard(
                                    icon = Icons.Default.FitnessCenter,
                                    value = state.totalTrainers.toString(),
                                    label = "Total Trainers",
                                    modifier = Modifier.weight(1f)
                                )
                                AdminStatCard(
                                    icon = Icons.Default.EventAvailable,
                                    value = state.activeSessions.toString(),
                                    label = "Active Sessions",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // --- BAGIAN 2: QUICK ACTIONS (FLOATING CARD) ---
            item {
                AdminQuickActionsSection(
                    onReportsClick = onNavigateToReports,
                    onTrainersClick = onNavigateToTrainers,
                    onChatClick = onNavigateToChat,
                    onClassesClick = onNavigateToClasses
                )
            }

            // --- BAGIAN 3: RECENT ACTIVITY ---
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                    // Judul otomatis putih di Dark Mode
                    Text(
                        text = "Recent Activity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else TextDark,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (recentActivities.isEmpty()) {
                        Text(
                            "Belum ada aktivitas terbaru.",
                            color = if (isDark) Color.Gray else Color.DarkGray,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    } else {
                        recentActivities.forEach { activity ->
                            AdminActivityItem(activity)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

// --- KOMPONEN PENDUKUNG ---

@Composable
fun AdminStatCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    // Style Glassmorphism (Transparan) -> Selalu aman karena background induknya Hijau
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.White.copy(0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    value,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                label,
                color = Color.White.copy(0.8f),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}

@Composable
fun AdminQuickActionsSection(
    onReportsClick: () -> Unit,
    onTrainersClick: () -> Unit,
    onChatClick: () -> Unit,
    onClassesClick: () -> Unit
) {
    // 2. Kartu Floating: Warna container mengikuti tema (Putih/Hitam)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .offset(y = (-24).dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            // Teks judul mengikuti tema (Hitam/Putih)
            Text(
                "Quick Actions",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(20.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Background icon dibuat semi-transparan warna agar tidak terlalu kontras di dark mode
                AdminQuickActionItem(Icons.Default.Person, "Member", Color(0xFFFFF3E0), Color(0xFFF57C00), onChatClick)
                AdminQuickActionItem(Icons.Default.SupervisorAccount, "Trainer", Color(0xFFE3F2FD), Color(0xFF1976D2), onTrainersClick)
                AdminQuickActionItem(Icons.Default.Class, "Class", Color(0xFFF3E5F5), Color(0xFF9C27B0), onClassesClick)
                AdminQuickActionItem(Icons.Default.Assessment, "Report", Color(0xFFE8F5E9), AdminGreenPrimary, onReportsClick)

            }
        }
    }
}

@Composable
fun AdminQuickActionItem(
    icon: ImageVector,
    label: String,
    bg: Color,
    tint: Color,
    onClick: () -> Unit
) {
    // Menyesuaikan background icon agar tidak terlalu terang di dark mode
    // Jika dark mode, background icon digelapkan sedikit
    val isDark = isSystemInDarkTheme()
    val adaptiveBg = if (isDark) bg.copy(alpha = 0.2f) else bg

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(adaptiveBg, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(8.dp))
        // Label teks otomatis menyesuaikan tema
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

@Composable
fun AdminActivityItem(activity: com.example.projekuas.data.RecentActivity) {
    // 3. Kartu Aktivitas: Warna container mengikuti tema (Putih/Hitam)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AdminGreenPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(activity.userInitial, fontWeight = FontWeight.Bold, color = AdminGreenPrimary)
            }
            Column(modifier = Modifier.weight(1f)) {
                // Judul Item mengikuti tema
                Text(
                    activity.action,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "${activity.user} • ${activity.time}",
                    fontSize = 12.sp,
                    color = if (isSystemInDarkTheme()) Color.Gray else Color.DarkGray
                )
            }
            Text(
                activity.amount,
                fontWeight = FontWeight.Bold,
                color = AdminGreenPrimary,
                fontSize = 14.sp
            )
        }
    }
}

fun formatCurrency(amount: Double): String {
    return if (amount >= 1000000) {
        val inMillions = amount / 1000000
        "Rp ${String.format("%.1f", inMillions)}jt"
    } else {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(amount)
    }
}