package com.example.projekuas.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projekuas.viewmodel.HomeViewModelFactory

// --- WARNA TEMA ADMIN (HIJAU) ---
val AdminGreenPrimary = Color(0xFF2E7D32)
val AdminGreenDark = Color(0xFF1B5E20)
val AdminBg = Color(0xFFF1F8E9)

@Composable
fun AdminDashboardScreen(
    factory: HomeViewModelFactory, // Placeholder factory jika nanti butuh VM
    onNavigateToReports: () -> Unit = {} // Callback Navigasi
) {
    Scaffold(containerColor = AdminBg) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            // 1. HEADER (Hijau)
            item {
                AdminHeaderSection()
            }

            // 2. QUICK ACTIONS
            item {
                AdminQuickActions(onNavigateToReports)
            }

            // 3. PERFORMANCE OVERVIEW (Growth)
            item {
                PerformanceOverviewCard()
            }

            // 4. RECENT REVENUE CARD
            item {
                RevenueCard()
            }

            // 5. RECENT ACTIVITY LIST
            item {
                RecentActivitySection()
            }
        }
    }
}

@Composable
fun AdminHeaderSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
            .background(Brush.verticalGradient(listOf(AdminGreenPrimary, AdminGreenDark)))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Admin Dashboard", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Row {
                    IconButton(onClick = {}) { Icon(Icons.Default.Settings, null, tint = Color.White) }
                    IconButton(onClick = {}) { Icon(Icons.Default.ExitToApp, null, tint = Color.White) }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Stats Grid
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AdminStatCard(
                    icon = Icons.Default.Group,
                    value = "1,234",
                    label = "Total Members",
                    trend = "+12%",
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    icon = Icons.Default.FitnessCenter,
                    value = "24",
                    label = "Active Trainers",
                    trend = "+2",
                    isBlue = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AdminStatCard(
                    icon = Icons.Default.AttachMoney,
                    value = "Rp 245M",
                    label = "Monthly Revenue",
                    trend = "+18%",
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    icon = Icons.Default.CalendarToday,
                    value = "32",
                    label = "Classes Today",
                    trend = "+5",
                    isOrange = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun AdminStatCard(icon: ImageVector, value: String, label: String, trend: String, isBlue: Boolean = false, isOrange: Boolean = false, modifier: Modifier = Modifier) {
    val bgColor = when {
        isBlue -> Color(0xFF1E88E5)
        isOrange -> Color(0xFFFF6F00)
        else -> Color(0xFF43A047)
    }

    Card(
        modifier = modifier.height(90.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.15f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(
                    modifier = Modifier.size(28.dp).background(bgColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Text(trend, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Column {
                Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(label, color = Color.White.copy(0.8f), fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun AdminQuickActions(onNavigateToReports: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .offset(y = (-30).dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Quick Actions", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                AdminActionItem(Icons.Default.Group, "Members", Color(0xFFF3E5F5), Color(0xFF9C27B0))
                AdminActionItem(Icons.Default.FitnessCenter, "Trainers", Color(0xFFE3F2FD), Color(0xFF1976D2))
                AdminActionItem(Icons.Default.Event, "Classes", Color(0xFFE8F5E9), Color(0xFF388E3C))
                // Navigasi ke Laporan
                AdminActionItem(
                    icon = Icons.Default.BarChart,
                    label = "Reports",
                    bg = Color(0xFFFFF3E0),
                    tint = Color(0xFFFF9800),
                    onClick = onNavigateToReports
                )
            }
        }
    }
}

@Composable
fun AdminActionItem(icon: ImageVector, label: String, bg: Color, tint: Color, onClick: () -> Unit = {}) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(
            modifier = Modifier.size(50.dp).background(bg, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = tint)
        }
        Spacer(Modifier.height(8.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun PerformanceOverviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Performance Overview", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(16.dp))

            // Dummy Growth Bar
            GrowthRow("January", 0.7f, "1050 members")
            Spacer(Modifier.height(12.dp))
            GrowthRow("February", 0.8f, "1120 members")
            Spacer(Modifier.height(12.dp))
            GrowthRow("March", 0.9f, "1180 members")
        }
    }
}

@Composable
fun GrowthRow(label: String, progress: Float, value: String) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = AdminGreenPrimary,
            trackColor = Color(0xFFE8F5E9)
        )
    }
}

@Composable
fun RevenueCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        colors = CardDefaults.cardColors(containerColor = AdminGreenPrimary),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("This Month Revenue", color = Color.White.copy(0.8f), fontSize = 12.sp)
                Text("Rp 245,000,000", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column {
                        Text("Memberships", color = Color.White.copy(0.7f), fontSize = 10.sp)
                        Text("Rp 180M", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Classes", color = Color.White.copy(0.7f), fontSize = 10.sp)
                        Text("Rp 65M", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Icon(Icons.Default.AttachMoney, null, tint = Color.White.copy(0.3f), modifier = Modifier.size(60.dp))
        }
    }
}

@Composable
fun RecentActivitySection() {
    Column(Modifier.padding(horizontal = 20.dp)) {
        Text("Recent Activity", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(12.dp))

        ActivityItem(Icons.Default.PersonAdd, "New member John Doe joined", "5 min ago", Color(0xFFE3F2FD), Color(0xFF1E88E5))
        ActivityItem(Icons.Default.EventBusy, "Yoga class fully booked", "15 min ago", Color(0xFFE8F5E9), Color(0xFF43A047))
        ActivityItem(Icons.Default.Payment, "Payment received from Jane", "1 hour ago", Color(0xFFFFF3E0), Color(0xFFFF9800))
    }
}

@Composable
fun ActivityItem(icon: ImageVector, title: String, time: String, bg: Color, tint: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(bg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(time, color = Color.Gray, fontSize = 12.sp)
            }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
    }
}