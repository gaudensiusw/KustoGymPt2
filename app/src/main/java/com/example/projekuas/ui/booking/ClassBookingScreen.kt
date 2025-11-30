package com.example.projekuas.ui.booking

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projekuas.R
import com.example.projekuas.data.GymClass
import com.example.projekuas.ui.theme.GymOrange
import com.example.projekuas.ui.theme.GymPurple
import com.example.projekuas.ui.theme.GymPurpleDark
import com.example.projekuas.viewmodel.ClassBookingViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue


val ClassImages = listOf(
    R.drawable.image3,
    R.drawable.image4,
    R.drawable.image5,
    R.drawable.image7,
    R.drawable.image9
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassBookingScreen(
    viewModel: ClassBookingViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.state.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    // --- STATE UTAMA DI DEKLARASI DI SINI ---
    var showRatingDialog by remember { mutableStateOf(false) }
    var classToRate by remember { mutableStateOf<GymClass?>(null) } // Menyimpan objek GymClass lengkap
    // --- AKHIR STATE UTAMA ---

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            // Header Gradient (Tetap Ungu sebagai Branding)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                    .background(Brush.verticalGradient(listOf(GymPurple, GymPurpleDark)))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Text("Book Classes", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    // Search Bar
                    TextField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::onSearchQueryChanged,
                        placeholder = { Text("Search classes...", color = Color.White.copy(0.7f), fontSize = 14.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(25.dp))
                            .background(Color.White.copy(0.2f)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White.copy(0.7f)) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize()) {
                BookingTabsSection(
                    bookingCount = uiState.myBookings.size,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )

                if (selectedTab == 0) {
                    // TAB 1: AVAILABLE CLASSES
                    AvailableClassesContent(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                } else {
                    // TAB 2: MY BOOKINGS
                    MyBookingsContent(
                        bookings = uiState.myBookings,
                        onBrowseClick = { selectedTab = 0 },
                        onCancelClick = { classId -> viewModel.onCancelBooking(classId) },
                        // CALLBACK SAAT KLIK RATE (LANGSUNG SIMPAN OBJEK KELAS)
                        onRateClick = { gymClass ->
                            classToRate = gymClass
                            showRatingDialog = true // Memicu dialog
                        }
                    )
                }
            }

            // --- DIALOG RATING ---
            if (showRatingDialog && classToRate != null) {
                RatingDialog(
                    trainerName = classToRate!!.trainerName,
                    onDismiss = { classToRate = null }, // Tutup dialog dengan reset state
                    onSubmit = { rating, review ->
                        // Kirim rating menggunakan objek GymClass lengkap
                        viewModel.giveRating(
                            gymClass = classToRate!!,
                            rating = rating,
                            review = review
                        )
                        classToRate = null // Tutup dialog setelah submit
                    }
                )
            }
        }
    }
}

// --- TAB CONTENT: AVAILABLE CLASSES ---
@Composable
fun AvailableClassesContent(
    uiState: com.example.projekuas.viewmodel.ClassBookingState,
    viewModel: ClassBookingViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        // Date Strip
        item {
            DateStripSection(
                selectedDate = Date(uiState.selectedDate),
                onDateSelected = { date -> viewModel.onDateSelected(date.time) }
            )
        }

        // Loading & Empty States
        if (uiState.isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        } else if (uiState.classes.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("Tidak ada kelas tersedia.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            // List Kelas
            items(uiState.classes) { gymClass ->
                val isBooked = uiState.bookedClassIds.contains(gymClass.classId)
                ClassItemCard(
                    gymClass = gymClass,
                    isBooked = isBooked,
                    onBookClick = { viewModel.onBookClass(gymClass.classId) },
                    onCancelClick = { viewModel.onCancelBooking(gymClass.classId) },
                    onRateClick = { } // Tidak ada aksi rate di tab Available
                )
            }
        }
    }
}

// --- TAB CONTENT: MY BOOKINGS ---
@Composable
fun MyBookingsContent(
    bookings: List<GymClass>,
    onBrowseClick: () -> Unit,
    onCancelClick: (String) -> Unit,
    onRateClick: (GymClass) -> Unit // FIX: Menerima objek GymClass
) {
    if (bookings.isEmpty()) {
        // --- EMPTY STATE ---
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.EventNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No bookings yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Browse available classes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onBrowseClick() }
            )
        }
    } else {
        // --- LIST BOOKING ---
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 20.dp, top = 10.dp)
        ) {
            items(bookings) { gymClass ->
                ClassItemCard(
                    gymClass = gymClass,
                    isBooked = true,
                    onBookClick = { },
                    onCancelClick = { onCancelClick(gymClass.classId) },
                    // PANGGIL CALLBACK DENGAN OBJEK GYMCLASS
                    onRateClick = { onRateClick(gymClass) }
                )
            }
        }
    }
}

// --- DIALOG RATING LENGKAP ---
@Composable
fun RatingDialog(
    trainerName: String,
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var rating by remember { mutableIntStateOf(0) }
    var review by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text("Rate $trainerName", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("How was the session?", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))

                // Bintang Interaktif
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "$i Star",
                            tint = Color(0xFFFF9800),
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { rating = i }
                                .padding(4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = review,
                    onValueChange = { review = it },
                    label = { Text("Review (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(rating, review) },
                enabled = rating > 0,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Submit", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

// --- CLASS ITEM CARD ---
@Composable
fun ClassItemCard(
    gymClass: GymClass,
    isBooked: Boolean,
    onBookClick: () -> Unit,
    onCancelClick: () -> Unit,
    onRateClick: () -> Unit
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val spotsLeft = gymClass.capacity - gymClass.currentBookings
    val isFull = gymClass.currentBookings >= gymClass.capacity

    val isPastClass = gymClass.startTimeMillis < System.currentTimeMillis()
    val hasBeenRated = gymClass.rating > 0

    val imageIndex = gymClass.classId.hashCode().absoluteValue % ClassImages.size
    val selectedImageRes = ClassImages[imageIndex]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = selectedImageRes),
                    contentDescription = "Class Image",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(gymClass.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text("with ${gymClass.trainerName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(timeFormat.format(Date(gymClass.startTimeMillis)), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(if(isFull) "Full" else "$spotsLeft spots left", fontSize = 12.sp, color = if(isFull) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            // Tombol
            Button(
                onClick = {
                    when {
                        // FIX: Logic ini harus memanggil onRateClick() agar dialog muncul
                        isPastClass && isBooked && !hasBeenRated -> onRateClick()
                        isBooked -> onCancelClick()
                        !isBooked && !isFull -> onBookClick()
                        else -> {}
                    }
                },
                enabled = isBooked || (!isBooked && !isFull),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp)
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        isBooked && isPastClass && !hasBeenRated -> Color(0xFFFF9800) // Orange
                        isBooked && isPastClass && hasBeenRated -> Color.Gray // Sudah dirating
                        isBooked -> MaterialTheme.colorScheme.surfaceVariant // Abu (Cancel)
                        else -> MaterialTheme.colorScheme.primary // Ungu (Book)
                    },
                    contentColor = when {
                        isBooked && isPastClass && !hasBeenRated -> Color.White
                        isBooked -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.onPrimary
                    }
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = when {
                        isBooked && isPastClass && !hasBeenRated -> "Rate Trainer"
                        isBooked && isPastClass && hasBeenRated -> "Completed"
                        isBooked -> "Cancel Booking"
                        isFull -> "Full"
                        else -> "Book Now"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// --- HELPER FUNCTIONS ---
fun isSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

// --- COMPONENTS ---

@Composable
fun BookingTabsSection(bookingCount: Int, selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp).height(50.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(25.dp)).padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
        TabButton("Available Classes", selectedTab == 0, Modifier.weight(1f)) { onTabSelected(0) }
        TabButton("My Bookings ($bookingCount)", selectedTab == 1, Modifier.weight(1f)) { onTabSelected(1) }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(modifier = modifier.fillMaxHeight().clip(RoundedCornerShape(25.dp)).background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Text(text, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
    }
}

@Composable
fun DateStripSection(selectedDate: Date, onDateSelected: (Date) -> Unit) {
    val dates = remember { val c = Calendar.getInstance(); val l = mutableListOf<Date>(); repeat(14) { l.add(c.time); c.add(Calendar.DAY_OF_YEAR, 1) }; l }
    val df = SimpleDateFormat("EEE", Locale("id", "ID")); val dF = SimpleDateFormat("dd", Locale.getDefault())
    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 8.dp)) {
        items(dates) { date ->
            val isSelected = isSameDay(date, selectedDate)
            Column(modifier = Modifier.width(60.dp).height(80.dp).clip(RoundedCornerShape(16.dp)).background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface).clickable { onDateSelected(date) }.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(df.format(date), color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(0.8f) else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Text(dF.format(date), color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}