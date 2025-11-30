package com.example.projekuas.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projekuas.data.Booking
import com.example.projekuas.viewmodel.AdminViewModel
// Ganti baris di bawah ini dengan ViewModel yang benar di proyek Anda (misalnya AdminViewModel)
import com.example.projekuas.viewmodel.HomeViewModelFactory
import com.example.projekuas.viewmodel.ReportViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit
) {
    // Ambil reportList yang sudah diproses
    val transactions by viewModel.transactionList.collectAsState()
    val state by viewModel.dashboardState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan Keuangan", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Total Pendapatan", color = Color.White.copy(0.8f))
                        Text(
                            text = viewModel.formatCurrency(state.totalRevenue),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${state.activeSessions} Transaksi Berhasil", color = Color.White.copy(0.8f), fontSize = 12.sp)
                    }
                }
            }

            item {
                Text("Riwayat Transaksi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            if (transactions.isEmpty()) {
                item { Text("Belum ada data transaksi.", color = Color.Gray) }
            } else {
                items(transactions) { booking ->
                    TransactionItem(booking, viewModel)
                }
            }
        }
    }
}

// --- SUB-COMPONENTS (IMPLEMENTASI LENGKAP) ---

@Composable
fun TransactionItem(booking: Booking, viewModel: AdminViewModel) { // Terima Booking
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Receipt, null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                // [FIX] Gunakan field gymClassTitle langsung dari Booking
                Text(text = booking.gymClassTitle, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(text = booking.userName, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(
                    text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(Date(booking.bookingTimeMillis)),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray
                )
            }
            Text(
                text = "+ ${viewModel.formatCurrency(booking.price)}",
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Bold
            )
            }
            Text(
                    text = "+ ${viewModel.formatCurrency(booking.price)}",
        color = Color(0xFF4CAF50),
        fontWeight = FontWeight.Bold
        )
        }
    }


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