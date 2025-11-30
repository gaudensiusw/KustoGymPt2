package com.example.projekuas.ui.profile

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.projekuas.R
import com.example.projekuas.viewmodel.ProfileViewModel

// Data Dropdown Global
val fitnessLevels = listOf("Pemula", "Menengah", "Mahir")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    // 1. Ambil State dari ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.userProfile

    // State Lokal UI
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }

    // 2. Logic Kalkulasi Data (BMI & Status)
    val heightM = if (profile.heightCm > 0) profile.heightCm.toDouble() / 100.0 else 1.0
    val weightKg = profile.weightKg.toDouble()

    val bmiValue = if (weightKg > 0) weightKg / (heightM * heightM) else 0.0
    val bmiFormatted = String.format("%.1f", bmiValue)

    // Warna Status BMI (Tetap Hardcoded agar konsisten maknanya)
    val bmiStatus = when {
        bmiValue == 0.0 -> "Belum ada data" to MaterialTheme.colorScheme.onSurfaceVariant
        bmiValue < 18.5 -> "Underweight" to Color(0xFF2196F3)
        bmiValue < 25.0 -> "Normal" to Color(0xFF4CAF50)
        bmiValue < 30.0 -> "Overweight" to Color(0xFFFFC107)
        else -> "Obesity" to Color.Red
    }

    // 3. Launcher Ganti Foto
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateProfilePicture(it) }
    }

    Scaffold(
        // FIX: Gunakan warna background dari tema
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {}
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 20.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {

            // --- BAGIAN 1: HEADER UNGU + KARTU STATISTIK MENGAMBANG ---
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(430.dp)
                ) {
                    // A. Background Ungu Melengkung
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                            .background(
                                Brush.verticalGradient(
                                    // Gunakan warna Primary tema (Ungu)
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 40.dp, start = 20.dp, end = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Row Navigasi Atas
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = onNavigateBack) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                                }
                                Text(
                                    text = "Profile",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                // Tombol Edit
                                IconButton(onClick = { showEditSheet = true }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Foto Profil
                            Box(contentAlignment = Alignment.Center) {
                                val imageModifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color.White, CircleShape)
                                    .clickable { imagePickerLauncher.launch("image/*") }

                                if (profile.profilePictureUrl.startsWith("data:image")) {
                                    val bitmap = remember(profile.profilePictureUrl) {
                                        base64ToBitmap(profile.profilePictureUrl)
                                    }
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Profile Pic",
                                            modifier = imageModifier,
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Image(
                                            painter = painterResource(R.drawable.logo_kusto_gym),
                                            contentDescription = "Profile Pic",
                                            modifier = imageModifier,
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                } else {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(profile.profilePictureUrl.ifEmpty { R.drawable.logo_kusto_gym })
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Profile Pic",
                                        modifier = imageModifier,
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                if (uiState.isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(100.dp), color = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Nama & Email (Selalu Putih di Header Ungu)
                            Text(
                                text = profile.name.ifEmpty { "Nama Pengguna" },
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = profile.email,
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // B. Row Kartu Statistik (Overlap)
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .offset(y = (-30).dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatFloatingCard(
                            icon = Icons.Outlined.Straighten,
                            value = "${profile.heightCm.toInt()} cm",
                            label = "Height"
                        )
                        StatFloatingCard(
                            icon = Icons.Outlined.MonitorWeight,
                            value = "${profile.weightKg.toInt()} kg",
                            label = "Weight"
                        )
                        StatFloatingCard(
                            icon = Icons.Outlined.Speed,
                            value = bmiFormatted,
                            label = "BMI"
                        )
                    }
                }
            }

            // --- BAGIAN 3: KARTU BMI & FITNESS ---
            item {
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    // FIX: Gunakan Surface Color
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("BMI Status", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Text(bmiStatus.first, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = bmiStatus.second)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Fitness Level", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Text(profile.fitnessLevel.ifEmpty { "-" }, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // --- BAGIAN 4: PERSONAL INFORMATION ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    // FIX: Gunakan Surface Color
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Personal Information", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(20.dp))

                        InfoRow(Icons.Outlined.Email, "Email", profile.email)
                        Spacer(Modifier.height(15.dp))

                        InfoRow(Icons.Outlined.Phone, "Phone", "Belum diatur")
                        Spacer(Modifier.height(15.dp))
                        InfoRow(Icons.Outlined.LocationOn, "Address", "Indonesia")
                        Spacer(Modifier.height(15.dp))
                        InfoRow(Icons.Outlined.Cake, "Date of Birth", "-")
                        Spacer(Modifier.height(15.dp))
                        InfoRow(Icons.Outlined.Person, "Gender", "-")
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // --- BAGIAN 5: MENU SETTINGS ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    // FIX: Gunakan Surface Color
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column {
                        SettingsItem("Privacy Settings") {}
                        Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                        SettingsItem("Notifications") {}
                        Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                        SettingsItem("Help & Support") {}
                        Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                        SettingsItem("Logout", isDestructive = true) {
                            showLogoutDialog = true
                        }
                    }
                }
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }

    // --- DIALOG LOGOUT ---
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Konfirmasi Logout") },
            text = { Text("Apakah Anda yakin ingin keluar dari akun ini?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                        onLogout()
                    }
                ) { Text("Keluar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Batal") }
            }
        )
    }

    // --- BOTTOM SHEET: FORM EDIT PROFIL ---
    if (showEditSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEditSheet = false },
            // FIX: Gunakan Surface Color
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp)
            ) {
                Text(
                    "Edit Profil",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Input Nama
                OutlinedTextField(
                    value = profile.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Nama Lengkap") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    // FIX: Warna input menyesuaikan tema
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(Modifier.height(12.dp))

                // Input Tinggi & Berat
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = if (profile.heightCm > 0) profile.heightCm.toString() else "",
                        onValueChange = viewModel::onHeightChange,
                        label = { Text("Tinggi (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    OutlinedTextField(
                        value = if (profile.weightKg > 0) profile.weightKg.toString() else "",
                        onValueChange = viewModel::onWeightChange,
                        label = { Text("Berat (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
                Spacer(Modifier.height(12.dp))

                // Dropdown Level
                LevelFitnessDropdown(
                    selectedLevel = profile.fitnessLevel,
                    onLevelSelected = viewModel::onFitnessLevelSelected
                )
                Spacer(Modifier.height(24.dp))

                // Tombol Simpan
                Button(
                    onClick = {
                        viewModel.saveProfile()
                        showEditSheet = false
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("SIMPAN PERUBAHAN", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                uiState.error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        }
    }
}

// --- KOMPONEN UI PENDUKUNG ---

@Composable
fun StatFloatingCard(icon: ImageVector, value: String, label: String) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(110.dp),
        shape = RoundedCornerShape(16.dp),
        // FIX: Gunakan Surface Color
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(15.dp))
        Column {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun SettingsItem(title: String, isDestructive: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelFitnessDropdown(selectedLevel: String, onLevelSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedLevel,
            onValueChange = { },
            readOnly = true,
            label = { Text("Level Fitness") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            fitnessLevels.forEach { level ->
                DropdownMenuItem(
                    text = { Text(level, color = MaterialTheme.colorScheme.onSurface) },
                    onClick = {
                        onLevelSelected(level)
                        expanded = false
                    }
                )
            }
        }
    }
}

fun base64ToBitmap(base64String: String): android.graphics.Bitmap? {
    return try {
        val pureBase64Encoded = base64String.substringAfter(",")
        val decodedBytes = Base64.decode(pureBase64Encoded, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}