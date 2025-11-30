package com.example.projekuas.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projekuas.viewmodel.MembershipViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembershipScreen(
    viewModel: MembershipViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val profile = state.userProfile

    // Format Tanggal Validasi
    val validUntilString = remember(profile.membershipValidUntil) {
        if (profile.membershipValidUntil > 0) {
            val date = Date(profile.membershipValidUntil)
            val format = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            format.format(date)
        } else {
            "-"
        }
    }

    // Hitung sisa hari
    val daysRemaining = remember(profile.membershipValidUntil) {
        if (profile.membershipValidUntil > System.currentTimeMillis()) {
            val diff = profile.membershipValidUntil - System.currentTimeMillis()
            (diff / (1000 * 60 * 60 * 24)).toInt()
        } else {
            0
        }
    }

    Scaffold(
        // FIX: Gunakan background tema (Hitam di Dark, Abu di Light)
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { /* Custom Header di body */ }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 20.dp),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // 1. HEADER & QR CARD
            item {
                Box(modifier = Modifier.fillMaxWidth().height(520.dp)) {
                    // Background Ungu Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                            // Gunakan warna Primary tema untuk gradasi
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = onNavigateBack) {
                                    // Icon di atas header ungu selalu putih
                                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                // Teks di atas header ungu selalu putih
                                Text("Membership", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // Floating QR Card
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 24.dp)
                            .fillMaxWidth()
                            .height(420.dp)
                            .offset(y = (-20).dp),
                        shape = RoundedCornerShape(24.dp),
                        // FIX: Gunakan Surface color (Putih di Light, Abu Gelap di Dark)
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Header Kartu
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    // FIX: Teks mengikuti tema (Hitam/Putih)
                                    Text("Member ID", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(profile.userId.take(8).uppercase(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(0.1f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.QrCode, null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(30.dp))

                            // QR CODE IMAGE
                            if (state.qrBitmap != null) {
                                // QR Code biasanya hitam-putih, aman di dark mode.
                                // Opsional: Beri background putih kecil jika QR transparan
                                Surface(color = Color.White, shape = RoundedCornerShape(8.dp)) {
                                    Image(
                                        bitmap = state.qrBitmap!!.asImageBitmap(),
                                        contentDescription = "QR Code",
                                        modifier = Modifier.size(200.dp).padding(8.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier.size(200.dp).background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(profile.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Scan this QR code at gym entrance", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Spacer(modifier = Modifier.weight(1f))

                            // Footer Info
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primary.copy(0.05f), RoundedCornerShape(12.dp))
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Plan", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(profile.membershipType.ifBlank { "Basic" }, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Valid Until", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(validUntilString, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
            }

            // 2. STATUS AKTIF
            item {
                if (daysRemaining > 0) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        // FIX: Gunakan warna dinamis, misal surfaceVariant atau Container spesifik
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Gunakan warna hijau tetap untuk status positif, tapi pastikan kontras
                            Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Membership Active", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text("$daysRemaining days remaining", fontSize = 12.sp, color = Color(0xFF4CAF50))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                } else {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // 3. TITLE UPGRADE
            item {
                Text(
                    "Upgrade Your Plan",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground, // FIX: Teks mengikuti tema
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }

            // 4. PLAN CARDS
            item {
                val currentPlan = profile.membershipType.ifBlank { "Basic" }

                PlanCard(
                    title = "Basic",
                    price = "Rp 299.000",
                    features = listOf("Access to gym equipment", "1 guest pass per month", "Locker access", "Mobile app access"),
                    isCurrent = currentPlan == "Basic",
                    color = MaterialTheme.colorScheme.outline, // Warna netral
                    onUpgrade = { viewModel.updateMembership("Basic") }
                )

                PlanCard(
                    title = "Premium",
                    price = "Rp 599.000",
                    features = listOf("All Basic features", "Unlimited class bookings", "5 guest passes per month", "Free towel service", "Nutrition consultation"),
                    isCurrent = currentPlan == "Premium",
                    isPopular = true,
                    color = MaterialTheme.colorScheme.primary, // Warna Primary (Ungu)
                    onUpgrade = { viewModel.updateMembership("Premium") }
                )

                PlanCard(
                    title = "Elite",
                    price = "Rp 999.000",
                    features = listOf("All Premium features", "Personal trainer (4x/month)", "Unlimited guest passes", "Priority booking", "Spa & sauna access"),
                    isCurrent = currentPlan == "Elite",
                    color = MaterialTheme.colorScheme.secondary, // Warna Secondary (Orange)
                    onUpgrade = { viewModel.updateMembership("Elite") }
                )
            }
        }
    }
}

// --- KOMPONEN KARTU PAKET ---

@Composable
fun PlanCard(
    title: String,
    price: String,
    features: List<String>,
    isCurrent: Boolean,
    isPopular: Boolean = false,
    color: Color, // Warna aksen (tombol/header)
    onUpgrade: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp)),
        // FIX: Gunakan Surface color (Putih di Light, Abu di Dark)
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // Header Popular
            if (isPopular) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Most Popular", color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(price, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (isCurrent) {
                        // Badge Current Plan
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer, // Warna lembut
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Current Plan",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                // Divider mengikuti tema
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                // Fitur List
                features.forEach { feature ->
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(feature, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Tombol Upgrade
                if (!isCurrent) {
                    Button(
                        onClick = onUpgrade,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = color),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Upgrade to $title", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}