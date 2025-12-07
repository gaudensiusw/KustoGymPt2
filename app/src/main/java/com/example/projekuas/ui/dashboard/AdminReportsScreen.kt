package com.example.projekuas.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projekuas.data.Booking
import com.example.projekuas.viewmodel.AdminViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- WARNA TEMA REPORT ---
val ReportGreenDark = Color(0xFF166534)
val ReportGreenPrimary = Color(0xFF16A34A)
val ReportGreenLight = Color(0xFFDCFCE7)
val ReportBg = Color(0xFFF9FAFB)

// --- DATA CLASSES (Shared with ViewModel) ---
data class MonthlyData(val month: String, val revenue: Double)
data class TopClassData(val name: String, val bookings: Int, val revenue: String)
data class MembershipData(val type: String, val count: Int, val percentage: Float, val color: Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit
) {
    // Ambil Data State
    val state by viewModel.dashboardState.collectAsState()
    val reportState by viewModel.reportState.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val displayedRevenue = viewModel.formatCurrency(state.totalRevenue)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background // Adapt to Dark Mode
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 1. Header with Filters
            item {
                ReportHeader(
                    onBack = onNavigateBack,
                    selectedPeriod = selectedPeriod,
                    onPeriodSelect = { viewModel.setReportPeriod(it) }
                )
            }

            // 2. Summary Metrics Grid
            item {
                Column(Modifier.padding(16.dp)) {
                    Text("Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max) // Force Equal Height
                    ) {
                        MetricCard(
                            title = "Total Revenue",
                            value = displayedRevenue,
                            icon = Icons.Default.MonetizationOn,
                            iconColor = ReportGreenPrimary,
                            iconBg = ReportGreenLight.copy(alpha = 0.2f),
                            trend = "+12%",
                            isTrendUp = true,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                        MetricCard(
                            title = "Active Sessions",
                            value = state.activeSessions.toString(),
                            icon = Icons.Default.EventAvailable,
                            iconColor = Color(0xFFF59E0B),
                            iconBg = Color(0xFFFEF3C7).copy(alpha = 0.2f),
                            trend = "+5%",
                            isTrendUp = true,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }

            // 3. Monthly Membership Revenue
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), // Adapt
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Membership Revenue", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("(in Millions IDR)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(20.dp))
                        
                        val maxRevenue = reportState.monthlyRevenue.maxOfOrNull { it.revenue } ?: 1.0
                        
                        if (reportState.monthlyRevenue.isEmpty()) {
                             Text("No revenue data for this period.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            reportState.monthlyRevenue.forEach { data ->
                                RevenueBarItem(data, maxRevenue)
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }

            // 4. Membership Distribution
            item {
                Column(Modifier.padding(16.dp)) {
                    Text("Membership Distribution", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            // Always show list if categories are present (ViewModel ensures this)
                            reportState.membershipStats.forEach { stat ->
                                MembershipBarItem(stat)
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-COMPONENTS ---

@Composable
fun ReportHeader(
    onBack: () -> Unit,
    selectedPeriod: String,
    onPeriodSelect: (String) -> Unit
) {
    val periods = listOf("Week", "Month", "Quarter", "Year")

    // Header Background: Gradient Green
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
        }

        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            periods.forEach { period ->
                val isSelected = selectedPeriod == period
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color.White else Color.White.copy(0.2f))
                        .clickable { onPeriodSelect(period) }
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), // Adapt
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween // Distribute vertical space
        ) {
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
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun RevenueBarItem(data: MonthlyData, maxRevenue: Double) {
    val percentage = if (maxRevenue > 0) (data.revenue / maxRevenue).toFloat() else 0f

    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(data.month, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Rp ${String.format("%.1f", data.revenue)}M", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50)),
            color = ReportGreenPrimary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant, // Adapt
        )
    }
}

@Composable
fun MembershipBarItem(data: MembershipData) {
    Column {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(data.color)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = data.type,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface // Adapt
                )
            }
            Text(
                text = "${data.count} members",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, // Adapt
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { data.percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50)),
            color = data.color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant // Adapt
        )
    }
}