package com.example.projekuas.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
// Ganti baris di bawah ini dengan ViewModel yang benar di proyek Anda (misalnya AdminViewModel)
import com.example.projekuas.viewmodel.HomeViewModelFactory
import com.example.projekuas.viewmodel.ReportViewModel
// JANGAN LUPA: Anda harus membuat file ReportViewModel.kt atau mengubah baris di atas
// dan juga baris val viewModel: ReportViewModel = ...
// menjadi ViewModel yang benar.

// --- DATA CLASSES (Diambil dari ReportViewModel) ---
data class MonthlyData(val month: String, val revenue: Int)
data class TopClassData(val name: String, val bookings: Int, val revenue: String)
data class MembershipData(val type: String, val count: Int, val percentage: Float, val color: Color)

// --- WARNA TEMA REPORT ---
val ReportGreenDark = Color(0xFF166534)
val ReportGreenPrimary = Color(0xFF16A34A)
val ReportGreenLight = Color(0xFFDCFCE7)
val ReportBg = Color(0xFFF9FAFB)

@Composable
fun AdminReportsScreen(
    factory: HomeViewModelFactory, // Perlu diganti dengan ReportViewModelFactory jika ada
    onNavigateBack: () -> Unit
) {
    // ASUMSI: ReportViewModel adalah ViewModel yang benar
    val viewModel: ReportViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = ReportBg,
        topBar = { ReportHeader(onBack = onNavigateBack) }
    ) { paddingValues ->

        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ReportGreenPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. KEY METRICS (Data Real)
                item {
                    Text("Key Metrics", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                    Spacer(Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                title = "Total Revenue",
                                value = "Rp ${"%.1f".format(state.totalRevenue / 1_000_000.0)}M",
                                icon = Icons.Default.AttachMoney,
                                iconColor = ReportGreenPrimary,
                                iconBg = ReportGreenLight,
                                trend = "+18%",
                                isTrendUp = true
                            )
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                title = "Active Members",
                                value = state.activeMembers.toString(),
                                icon = Icons.Default.Group,
                                iconColor = Color(0xFF9333EA),
                                iconBg = Color(0xFFF3E8FF),
                                trend = "+12%",
                                isTrendUp = true
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                title = "Classes This Month",
                                value = state.classesThisMonth.toString(),
                                icon = Icons.Default.CalendarToday,
                                iconColor = Color(0xFF2563EB),
                                iconBg = Color(0xFFDBEAFE),
                                trend = "+8%",
                                isTrendUp = true
                            )
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                title = "Avg Attendance",
                                value = "${state.avgAttendance}%",
                                icon = Icons.AutoMirrored.Filled.TrendingUp,
                                iconColor = Color(0xFFEA580C),
                                iconBg = Color(0xFFFFEDD5),
                                trend = "-2%",
                                isTrendUp = false
                            )
                        }
                    }
                }

                // 2. REVENUE CHART
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Revenue Trend", style = MaterialTheme.typography.titleMedium)
                                Text("Last 5 months", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Spacer(Modifier.height(20.dp))

                            if (state.monthlyRevenueData.isEmpty()) {
                                Text("Belum ada data pendapatan.", color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
                            } else {
                                state.monthlyRevenueData.forEach { data ->
                                    RevenueBarItem(data, state.maxRevenue.toInt())
                                    Spacer(Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }

                // 3. MEMBERSHIP DISTRIBUTION
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text("Membership Distribution", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(20.dp))

                            // Stacked Bar Chart
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .height(16.dp)
                                    .clip(RoundedCornerShape(50))
                            ) {
                                state.membershipBreakdown.forEach { item ->
                                    if (item.percentage > 0) {
                                        Box(
                                            modifier = Modifier
                                                .weight(item.percentage)
                                                .fillMaxHeight()
                                                .background(item.color)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(20.dp))

                            // Legend
                            state.membershipBreakdown.forEach { item ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(12.dp).background(item.color, RoundedCornerShape(4.dp)))
                                        Spacer(Modifier.width(8.dp))
                                        Text(item.type, color = Color.Gray)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(item.count.toString(), fontWeight = FontWeight.Bold)
                                        Text("${(item.percentage * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. TOP CLASSES
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text("Top Performing Classes", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(16.dp))

                            if (state.topClasses.isEmpty()) {
                                Text("Belum ada data kelas.", color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
                            } else {
                                state.topClasses.forEachIndexed { index, cls ->
                                    TopClassItem(index + 1, cls)
                                    if (index < state.topClasses.size - 1) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 12.dp),
                                            color = Color.LightGray.copy(alpha = 0.3f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. EXPORT OPTIONS
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ReportGreenLight),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text("Export Reports", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(16.dp))

                            ExportButton(label = "Export as PDF")
                            Spacer(Modifier.height(8.dp))
                            ExportButton(label = "Export as Excel")
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

// --- SUB-COMPONENTS (IMPLEMENTASI LENGKAP) ---

@Composable
fun ReportHeader(onBack: () -> Unit) {
    var selectedPeriod by remember { mutableStateOf("Month") }
    val periods = listOf("Week", "Month", "Quarter", "Year")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(ReportGreenPrimary, ReportGreenDark)
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("Reports & Analytics", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
            Row {
                IconButton(onClick = {}, modifier = Modifier.background(Color.White.copy(0.2f), CircleShape)) {
                    Icon(Icons.Default.FilterList, contentDescription = null, tint = Color.White)
                }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = {}, modifier = Modifier.background(Color.White.copy(0.2f), CircleShape)) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.White)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            periods.forEach { period ->
                val isSelected = selectedPeriod == period
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color.White else Color.White.copy(0.2f))
                        .clickable { selectedPeriod = period }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = period,
                        color = if (isSelected) ReportGreenPrimary else Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color,
    trend: String,
    isTrendUp: Boolean
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(iconBg, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isTrendUp) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = if (isTrendUp) ReportGreenPrimary else Color.Red,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = trend,
                        color = if (isTrendUp) ReportGreenPrimary else Color.Red,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Text(title, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
fun RevenueBarItem(data: MonthlyData, maxRevenue: Int) {
    val percentage = if (maxRevenue > 0) data.revenue.toFloat() / maxRevenue.toFloat() else 0f

    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(data.month, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text("Rp ${data.revenue}M", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50)),
            color = ReportGreenPrimary,
            trackColor = Color(0xFFF3F4F6),
        )
    }
}

@Composable
fun TopClassItem(rank: Int, data: TopClassData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(ReportGreenLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(rank.toString(), color = ReportGreenPrimary, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(data.name, fontWeight = FontWeight.Medium)
                Text("${data.bookings} bookings", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
        Text(data.revenue, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}

@Composable
fun ExportButton(label: String) {
    Button(
        onClick = {},
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
    ) {
        Icon(Icons.Default.Download, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = Color(0xFF374151))
    }
}