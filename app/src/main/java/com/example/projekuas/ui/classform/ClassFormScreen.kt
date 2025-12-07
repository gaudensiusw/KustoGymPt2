package com.example.projekuas.ui.classform

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projekuas.viewmodel.ClassFormViewModel
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale
import kotlin.io.encoding.ExperimentalEncodingApi

// --- HAPUS WARNA HARDCODED, GANTI DENGAN MATERIAL THEME ---
/* val FormPrimaryColor = Color(0xFF1E88E5)
val FormPrimaryDark = Color(0xFF1565C0)
val FormBgColor = Color(0xFFF5F9FF)
val InputBgColor = Color.White
*/

@OptIn(ExperimentalMaterial3Api::class, ExperimentalEncodingApi::class)
@Composable
fun ClassFormScreen(
    factory: ViewModelProvider.Factory,
    classIdToEdit: String?,
    onNavigateBack: () -> Unit
) {
    val viewModel: ClassFormViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Load data jika Edit Mode
    LaunchedEffect(classIdToEdit) {
        if (classIdToEdit != null) {
            viewModel.loadClassData(classIdToEdit)
        } else {
            viewModel.resetForm()
        }
    }

    // Handle Navigasi setelah simpan
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onNavigateBack()
            viewModel.resetForm()
        }
    }

    // Launcher untuk memilih gambar
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    Scaffold(
        // FIX: Gunakan background dari tema
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            // Custom Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                    .background(
                // FIX: Warna Gradasi menggunakan warna Primary tema
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.background(MaterialTheme.colorScheme.onPrimary.copy(0.2f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (state.classIdToEdit == null) "Add New Class" else if (state.isReadOnly) "Class Details (Ended)" else "Edit Class",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = if (state.isReadOnly) "Class is ongoing/ended, cannot edit" else "Fill in the class details below",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(0.8f)
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (!state.isReadOnly) {
                // Tombol Simpan Melayang di Bawah
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                ) {
                    Button(
                        onClick = { viewModel.saveOrUpdateClass {} },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .height(50.dp),
                    // FIX: Tombol menggunakan Primary color
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !state.isSaving
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Saving...")
                    } else {
                        Text("Save Class", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(20.dp), // Menambahkan padding agar konten tidak terlalu mepet
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Warning Box
            if (state.isReadOnly) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "This class is ongoing or has ended. You cannot modify it.",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // 0. AREA UPLOAD GAMBAR COVER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    // FIX: Background placeholder mengikuti tema (Surface Variant)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(enabled = !state.isReadOnly) { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (state.currentImageUrl.isNotBlank()) {
                    val bitmap = remember(state.currentImageUrl) {
                        try {
                            val pureBase64 = state.currentImageUrl.substringAfter(",")
                            val bytes = Base64.decode(pureBase64, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        } catch (e: Exception) { null }
                    }

                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Cover Kelas",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Overlay tipis
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.3f)))
                    }
                }

                // Placeholder / Hint
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (state.currentImageUrl.isNotBlank()) Icons.Default.Edit else Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        // FIX: Warna ikon menyesuaikan kondisi gambar
                        tint = if (state.currentImageUrl.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (state.currentImageUrl.isNotBlank()) "Change Cover" else "Upload Cover Photo",
                        // FIX: Warna teks menyesuaikan kondisi gambar
                        color = if (state.currentImageUrl.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 1. Input Nama Kelas
            FancyTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = "Class Name",
                placeholder = "Ex: Yoga Morning Flow",
                icon = Icons.Default.FitnessCenter,
                enabled = !state.isReadOnly
            )

            // 2. Input Deskripsi
            FancyTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                label = "Description",
                placeholder = "Describe the workout details...",
                icon = Icons.Outlined.Description,
                singleLine = false,
                minLines = 3,
                enabled = !state.isReadOnly
            )

            // 3. Row Durasi & Kapasitas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    FancyTextField(
                        value = state.durationMinutes,
                        onValueChange = viewModel::onDurationChange,
                        label = "Duration (Minutes)",
                        placeholder = "60",
                        icon = Icons.Outlined.Timer,
                        keyboardType = KeyboardType.Number,
                        enabled = !state.isReadOnly
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    FancyTextField(
                        value = state.capacity,
                        onValueChange = viewModel::onCapacityChange,
                        label = "Capacity",
                        placeholder = "20",
                        icon = Icons.Outlined.Groups,
                        keyboardType = KeyboardType.Number,
                        enabled = !state.isReadOnly
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            // FIX: Divider color
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // FIX: Text color
            Text("Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)

            // 4. Date Picker
            ClickableInputCard(
                label = "Start Date",
                value = state.dateDisplay.ifEmpty { "Select Date" },
                icon = Icons.Default.DateRange,
                onClick = {
                    showDatePicker(context, state.selectedDateMillis) { millis, str ->
                        viewModel.onDateSelected(millis, str)
                    }
                },
                enabled = !state.isReadOnly
            )

            // 5. Time Picker
            val timeDisplay = remember(state.selectedTimeMillis) {
                val totalSecs = state.selectedTimeMillis / 1000
                val h = totalSecs / 3600
                val m = (totalSecs % 3600) / 60
                String.format(Locale.getDefault(), "%02d:%02d", h, m)
            }

            ClickableInputCard(
                label = "Start Time",
                value = timeDisplay,
                icon = Icons.Default.Schedule,
                onClick = {
                    showTimePicker(context, state.selectedTimeMillis) { h, m ->
                        viewModel.onTimeSelected(h, m)
                    }
                },
                enabled = !state.isReadOnly
            )

            // Info Trainer
            Card(
                // FIX: Gunakan Secondary Container untuk info box
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Trainer will be automatically set to your account.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Error Message
            if (state.error != null) {
                Text(
                    text = state.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

// --- REUSABLE COMPONENTS (Disesuaikan dengan Tema) ---

@Composable
fun FancyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true
) {
    Column {
        // FIX: Label color
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp, start = 4.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            leadingIcon = { Icon(icon, contentDescription = null, tint = if (enabled) MaterialTheme.colorScheme.primary else Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            enabled = enabled,
            // FIX: Colors mengikuti tema
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
fun ClickableInputCard(
    label: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp, start = 4.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .then(if (enabled) Modifier.clickable { onClick() } else Modifier),
            shape = RoundedCornerShape(16.dp),
            // FIX: Colors mengikuti tema
            colors = CardDefaults.cardColors(containerColor = if (enabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = if (enabled) MaterialTheme.colorScheme.primary else Color.Gray)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                if (enabled) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// --- HELPER FUNCTIONS (Tetap Sama) ---

fun showDatePicker(context: Context, initialMillis: Long, onDateSelected: (Long, String) -> Unit) {
    val calendar = Calendar.getInstance()
    if (initialMillis > 0) calendar.timeInMillis = initialMillis

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth, 0, 0, 0)
            selectedCalendar.set(Calendar.MILLISECOND, 0)
            val format = java.text.SimpleDateFormat("EEEE, dd MMM yyyy", Locale.US)
            val dateString = format.format(selectedCalendar.time)
            onDateSelected(selectedCalendar.timeInMillis, dateString)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.show()
}

fun showTimePicker(context: Context, initialMillis: Long, onTimeSelected: (Int, Int) -> Unit) {
    val totalSeconds = initialMillis / 1000
    val initialHour = (totalSeconds / 3600).toInt()
    val initialMinute = ((totalSeconds % 3600) / 60).toInt()

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            onTimeSelected(hourOfDay, minute)
        },
        initialHour,
        initialMinute,
        true
    )
    timePickerDialog.show()
}

fun isDateValid(selectedDate: LocalDate): Boolean {
    val currentDate = LocalDate.now()
    return selectedDate >= currentDate
}