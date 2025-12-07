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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.projekuas.R
import com.example.projekuas.viewmodel.ProfileViewModel
import com.example.projekuas.ui.theme.* // Import semua warna dari Color.kt
import com.example.projekuas.data.Achievement
import com.example.projekuas.data.UserAchievement
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val fitnessLevels = listOf("Beginner", "Intermediate", "Pro")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.userProfile

    // --- 1. DEFINE MOCK DATA FOR VISUAL TESTING ---
    // In ProfileScreen, replace mockAchievements with this:
    val mockAchievements = listOf(
        // ID "1" is Unlocked -> Will show Purple
        Achievement("1", "First Step", Icons.Default.DirectionsWalk, isUnlocked = true),
        // ID "2" is Locked -> Will show Gray
        Achievement("2", "Heavy Lifter", Icons.Default.FitnessCenter, isUnlocked = false),
        // ID "3" is Locked -> Will show Gray
        Achievement("3", "Hydrated", Icons.Default.WaterDrop, isUnlocked = false),
        // ID "4" is Locked-> Will show Gray
        Achievement("4", "Champion", Icons.Default.EmojiEvents, isUnlocked = false)
    )

    // [BARU] Ambil Role langsung dari data profil yang sedang login
    val userRole = if (profile.role.isNotBlank()) profile.role else "Member"

    // --- LOGIKA WARNA DINAMIS ---
    val (primaryColor, darkPrimaryColor) = when (userRole.lowercase()) {
        "admin" -> GymGreen to GymGreenDark
        "trainer" -> GymBlue to GymBlueDark
        else -> GymPurple to GymPurpleDark // Default Member
    }

    // --- PERBAIKAN DATA UNTUK TAMPILAN ---
    val dobString = remember(profile.dateOfBirthMillis) {
        if (profile.dateOfBirthMillis > 0L) {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(profile.dateOfBirthMillis))
        } else {
            ""
        }
    }

    val phoneString = profile.phoneNumber ?: ""
    val heightVal = profile.heightCm
    val weightVal = profile.weightKg
    val heightM = if (heightVal > 0) heightVal / 100.0 else 1.0
    val bmiValue = if (weightVal > 0) weightVal / (heightM * heightM) else 0.0
    val bmiFormatted = String.format("%.1f", bmiValue)

    val bmiStatus = when {
        bmiValue == 0.0 -> "No Data" to MaterialTheme.colorScheme.onSurfaceVariant
        bmiValue < 18.5 -> "Underweight" to GymBlue
        bmiValue < 25.0 -> "Normal" to GymGreen
        bmiValue < 30.0 -> "Overweight" to GymOrange
        else -> "Obesity" to Color.Red
    }

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }
    var dobInput by remember { mutableStateOf(dobString) }

    // Update dobInput jika data dari DB selesai dimuat
    LaunchedEffect(dobString) {
        if (dobString.isNotBlank()) dobInput = dobString
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            showEditSheet = false
            viewModel.clearMessages()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateProfilePicture(it) }
    }

    // [NEW] Achievement Dialog State (Managed locally or via ViewModel)
    var selectedAchievement by remember { mutableStateOf<Achievement?>(null) }
    // IMPORTANT: Assuming ViewModel has no selectAchievement yet, I'll manage it locally for UI demo
    // Or I'll add a helper in ProfileScreen to handle the click.
    // The onClick above calls viewModel.selectAchievement which doesn't exist.
    // I will change the logic above to use `selectedAchievement = achievement`.
    
    if (selectedAchievement != null) {
        AchievementDetailDialog(achievement = selectedAchievement!!) {
            selectedAchievement = null
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = 100.dp)) }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 20.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // HEADER
            item {
                Box(modifier = Modifier.fillMaxWidth().height(430.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(primaryColor, darkPrimaryColor)
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(top = 40.dp, start = 20.dp, end = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = onNavigateBack) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                                }
                                Text("Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                IconButton(onClick = { showEditSheet = true }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Box(contentAlignment = Alignment.Center) {
                                val imageModifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color.White, CircleShape)
                                    .clickable { imagePickerLauncher.launch("image/*") }

                                if (profile.profilePictureUrl.startsWith("data:image")) {
                                    val bitmap = remember(profile.profilePictureUrl) { base64ToBitmap(profile.profilePictureUrl) }
                                    if (bitmap != null) {
                                        Image(bitmap = bitmap.asImageBitmap(), contentDescription = null, modifier = imageModifier, contentScale = ContentScale.Crop)
                                    } else {
                                        Image(painter = painterResource(R.drawable.logo_kusto_gym), contentDescription = null, modifier = imageModifier, contentScale = ContentScale.Crop)
                                    }
                                } else {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(profile.profilePictureUrl.ifEmpty { R.drawable.logo_kusto_gym })
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = null,
                                        modifier = imageModifier,
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Box(modifier = Modifier.align(Alignment.BottomEnd).background(Color.White, CircleShape).padding(4.dp)) {
                                    Icon(Icons.Default.CameraAlt, null, tint = primaryColor, modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(text = profile.name.ifEmpty { "User Name" }, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = profile.email, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))

                            Spacer(Modifier.height(8.dp))

                            Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                                Text(
                                    text = userRole.uppercase(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 20.dp).offset(y = (-30).dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatFloatingCard(Icons.Outlined.Straighten, "${heightVal.toInt()} cm", "Height", primaryColor)
                        StatFloatingCard(Icons.Outlined.MonitorWeight, "${weightVal.toInt()} kg", "Weight", primaryColor)
                        StatFloatingCard(Icons.Outlined.Speed, bmiFormatted, "BMI", primaryColor)
                    }
                }
            }

            // BMI CARD
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("BMI Status", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(bmiStatus.first, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = bmiStatus.second)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Fitness Level", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(profile.fitnessLevel.ifEmpty { "-" }, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = primaryColor)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // PERSONAL INFO
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Personal Information", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(20.dp))

                        InfoRow(Icons.Outlined.Email, "Email", profile.email)
                        Spacer(Modifier.height(15.dp))
                        InfoRow(Icons.Outlined.Phone, "Phone", phoneString.ifBlank { "-" })
                        Spacer(Modifier.height(15.dp))
                        InfoRow(Icons.Outlined.LocationOn, "Address", profile.address.ifBlank { "-" })
                        Spacer(Modifier.height(15.dp))
                        InfoRow(Icons.Outlined.Cake, "Date of Birth", dobString.ifBlank { "-" })
                        Spacer(Modifier.height(15.dp))
                        InfoRow(Icons.Outlined.Person, "Gender", profile.gender.ifBlank { "-" })
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // --- 2. ACHIEVEMENTS SECTION (USING MOCK DATA FOR NOW) ---
            item {
                Text(
                    text = "Achievements",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                AchievementsSection(achievements = mockAchievements, onAchievementClick = { selectedAchievement = it })
                Spacer(modifier = Modifier.height(12.dp))
            }

            // SETTINGS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column {
                        SettingsItem("Edit Profile") { showEditSheet = true }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                        SettingsItem("Logout", isDestructive = true) { showLogoutDialog = true }
                    }
                }
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }

    // LOGOUT DIALOG
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout Confirmation") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = { showLogoutDialog = false; viewModel.logout(); onLogout() }) {
                    Text("Logout", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") } }
        )
    }

    // EDIT SHEET (Corrected and Duplicates Removed)
    if (showEditSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEditSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 50.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Edit Profile", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 20.dp))

                EditTextField(label = "Full Name", value = profile.name, onValueChange = viewModel::onNameChange, activeColor = primaryColor)
                Spacer(Modifier.height(12.dp))

                EditTextField(label = "Phone Number", value = phoneString, onValueChange = viewModel::onPhoneChange, activeColor = primaryColor, keyboardType = KeyboardType.Phone)
                Spacer(Modifier.height(12.dp))

                EditTextField(label = "Address", value = profile.address, onValueChange = viewModel::onAddressChange, activeColor = primaryColor)
                Spacer(Modifier.height(12.dp))

                EditTextField(
                    label = "Date of Birth (YYYY-MM-DD)",
                    value = dobInput,
                    onValueChange = {
                        dobInput = it
                        viewModel.onDobChange(it)
                    },
                    activeColor = primaryColor,
                    keyboardType = KeyboardType.Number
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = profile.gender.ifBlank { "Not Set" },
                    onValueChange = {},
                    label = { Text("Gender (Locked)") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    enabled = false
                )
                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    EditTextField(label = "Height (cm)", value = if (profile.heightCm > 0) profile.heightCm.toString() else "", onValueChange = viewModel::onHeightChange, activeColor = primaryColor, keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f))
                    EditTextField(label = "Weight (kg)", value = if (profile.weightKg > 0) profile.weightKg.toString() else "", onValueChange = viewModel::onWeightChange, activeColor = primaryColor, keyboardType = KeyboardType.Decimal, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))

                LevelFitnessDropdown(profile.fitnessLevel, primaryColor, viewModel::onFitnessLevelSelected)
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.saveProfile() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !uiState.isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    if (uiState.isSaving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp)) else Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Text("1 hour cooldown between profile updates.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}


// ... Sisa fungsi helper (EditTextField, StatFloatingCard, dll) sama seperti sebelumnya ...
@Composable
fun EditTextField(label: String, value: String, onValueChange: (String) -> Unit, activeColor: Color, keyboardType: KeyboardType = KeyboardType.Text, modifier: Modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = activeColor, focusedLabelColor = activeColor, cursorColor = activeColor)
    )
}

@Composable
fun StatFloatingCard(icon: ImageVector, value: String, label: String, iconTint: Color) {
    Card(
        modifier = Modifier.width(100.dp).height(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
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
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelFitnessDropdown(selectedLevel: String, activeColor: Color, onLevelSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(value = selectedLevel, onValueChange = { }, readOnly = true, label = { Text("Fitness Level") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = activeColor, focusedLabelColor = activeColor, cursorColor = activeColor))
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            fitnessLevels.forEach { level -> DropdownMenuItem(text = { Text(level, color = MaterialTheme.colorScheme.onSurface) }, onClick = { onLevelSelected(level); expanded = false }) }
        }
    }
}

fun base64ToBitmap(base64String: String): android.graphics.Bitmap? {
    return try {
        val pureBase64Encoded = base64String.substringAfter(",")
        val decodedBytes = Base64.decode(pureBase64Encoded, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) { e.printStackTrace(); null }
}

// Add this Composable to your ProfileScreen
@Composable
fun AchievementBadge(achievement: Achievement) {
    // Logic: If unlocked -> Primary Color. If locked -> Gray.
    val backgroundColor = if (achievement.isUnlocked) MaterialTheme.colorScheme.primaryContainer else Color.Gray.copy(alpha = 0.2f)
    val iconColor = if (achievement.isUnlocked) MaterialTheme.colorScheme.primary else Color.Gray
    val textColor = if (achievement.isUnlocked) MaterialTheme.colorScheme.onSurface else Color.Gray

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = achievement.icon,
                contentDescription = achievement.title,
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = achievement.title,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = textColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
@Composable
fun AchievementsSection(achievements: List<Achievement>, onAchievementClick: (Achievement) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        items(achievements) { item ->
            AchievementItem(achievement = item, onClick = { onAchievementClick(item) })
        }
    }
}

@Composable
fun AchievementItem(achievement: Achievement, onClick: () -> Unit) {
    val backgroundColor = if (achievement.isUnlocked) MaterialTheme.colorScheme.primaryContainer else Color.LightGray.copy(alpha = 0.3f)
    val iconColor = if (achievement.isUnlocked) MaterialTheme.colorScheme.primary else Color.Gray

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(70.dp)
            .clickable { onClick() } // Make it clickable
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(60.dp)
                .background(color = backgroundColor, shape = CircleShape)
        ) {
            Icon(
                imageVector = achievement.icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = achievement.title,
            style = MaterialTheme.typography.bodySmall,
            color = if (achievement.isUnlocked) MaterialTheme.colorScheme.onSurface else Color.Gray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AchievementDetailDialog(
    achievement: Achievement,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E), // Dark background as requested
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header (Example hardcoded "Hush-Hush" not needed, just Achievement Title)
                Text(achievement.title, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                
                Spacer(Modifier.height(24.dp))
                
                // Icon Hexagon (Simulated with Box)
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0xFF2D2D2D), RoundedCornerShape(16.dp)) // Darker Hexagon placeholder
                        .border(2.dp, if (achievement.isUnlocked) GymGreen else Color.Gray, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = achievement.icon,
                        contentDescription = null,
                        tint = if (achievement.isUnlocked) GymGreen else Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                Spacer(Modifier.height(24.dp))
                
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    text = if (achievement.isUnlocked) "You have unlocked this achievement!" else "Keep training to unlock this badge.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(24.dp))
                
                Text(
                    text = if (achievement.isUnlocked) "Unlocked" else "Locked",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (achievement.isUnlocked) GymGreen else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color.White)
            }
        }
    )
}


