@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.projekuas.ui.home

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.projekuas.R
import com.example.projekuas.data.GymClass
import com.example.projekuas.navigation.HomeNavDestinations
import com.example.projekuas.ui.booking.ClassBookingScreen
import com.example.projekuas.ui.chat.ChatScreen
import com.example.projekuas.ui.dashboard.AdminDashboardScreen
import com.example.projekuas.ui.dashboard.MemberDetailScreen
import com.example.projekuas.ui.dashboard.NotificationScreen
import com.example.projekuas.ui.dashboard.TrainerDashboardScreen
import com.example.projekuas.ui.dashboard.TrainerListScreen
import com.example.projekuas.ui.dashboard.TrainerMembersScreen
import com.example.projekuas.ui.dashboard.TrainerScheduleScreen
import com.example.projekuas.ui.profile.MembershipScreen
import com.example.projekuas.ui.profile.ProfileScreen
import com.example.projekuas.ui.workout.WorkoutTrackerScreen
import com.example.projekuas.utils.rememberBitmapFromBase64
import com.example.projekuas.viewmodel.AdminViewModel
import com.example.projekuas.viewmodel.ChatViewModel
import com.example.projekuas.viewmodel.ClassBookingViewModel
import com.example.projekuas.viewmodel.DashboardViewModel
import com.example.projekuas.viewmodel.HomeViewModelFactory
import com.example.projekuas.viewmodel.MembershipViewModel
import com.example.projekuas.viewmodel.ThemeViewModel
import com.example.projekuas.viewmodel.TrainerListViewModel
import com.example.projekuas.viewmodel.TrainerViewModel
import com.example.projekuas.viewmodel.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalDensity
import com.example.projekuas.ui.dashboard.AdminClassListScreen
import com.example.projekuas.ui.dashboard.AdminMemberListScreen
import com.example.projekuas.ui.dashboard.AdminReportsScreen
import com.example.projekuas.ui.dashboard.AdminTrainerListScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.projekuas.ui.theme.GymOrange
import com.example.projekuas.ui.theme.GymPurple


// Konstanta Rute
const val TRAINER_MEMBERS_ROUTE = "trainer_members_route"
const val MEMBER_DETAIL_ROUTE = "member_detail/{memberId}"

val HomeClassImages = listOf(
    R.drawable.image3,
    R.drawable.image4,
    R.drawable.image5,
    R.drawable.image7,
    R.drawable.image9
)

val bottomNavItems = listOf(
    HomeNavDestinations.Dashboard,
    HomeNavDestinations.Kelas,
    HomeNavDestinations.Latihan,
    HomeNavDestinations.Profil
)

fun base64ToBitmapHome(base64String: String): android.graphics.Bitmap? {
    return try {
        val pureBase64Encoded = if (base64String.contains(",")) {
            base64String.substringAfter(",")
        } else {
            base64String
        }
        val decodedBytes = Base64.decode(pureBase64Encoded, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// ==========================================
// --- 1. MEMBER DASHBOARD SCREEN (RESTORED UI + DARK MODE FIX) ---
// ==========================================

@OptIn(ExperimentalMaterial3Api::class) // Pastikan opt-in ini ada
@Composable
fun MemberDashboardScreen(
    dashboardViewModel: DashboardViewModel,
    classViewModel: ClassBookingViewModel,
    themeViewModel: ThemeViewModel,
    onNavigateToBooking: () -> Unit,
    onNavigateToWorkoutLog: () -> Unit,
    onNavigateToMembership: () -> Unit,
    onNavigateToProfileTab: () -> Unit,
    onNavigateToSelection: (String) -> Unit,
    onNavigateToActiveWorkout: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToChatList: () -> Unit
) {
    val state by dashboardViewModel.dashboardState.collectAsState()
    val bookingState by classViewModel.state.collectAsState()

    // State Refresh
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Fungsi Refresh
    fun onRefresh() {
        isRefreshing = true
        scope.launch {
            // TODO: Panggil fungsi refresh data dari ViewModel di sini
            // Contoh:
            // dashboardViewModel.refreshData()
            // classViewModel.refreshClasses()

            // Simulasi delay (hapus jika sudah ada data fetching asli)
            delay(1500)
            isRefreshing = false
        }
    }

    // Upcoming Classes Logic
    val upcomingClasses = remember(bookingState.classes) {
        val now = System.currentTimeMillis()
        bookingState.classes
            .filter { it.startTimeMillis > now }
            .sortedBy { it.startTimeMillis }
            .take(3)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        // --- PERUBAHAN UTAMA DI SINI ---
        // LazyColumn dibungkus oleh PullToRefreshBox
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { onRefresh() },
            modifier = Modifier.padding(padding) // Padding Scaffold dipindah ke Box ini
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(), // Hapus padding(padding) dari sini
                contentPadding = PaddingValues(bottom = 20.dp) // Tambahan padding bawah agar konten terbawah tidak kepotong
            ) {
                // 1. HEADER UNGU
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                                ),
                                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                            )
                            .padding(24.dp)
                    ) {
                        Column {
                            // Top Row (Profile & Notif)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val bitmap = rememberBitmapFromBase64(state.profilePictureUrl)
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap,
                                            contentDescription = null,
                                            modifier = Modifier.size(50.dp).clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Surface(modifier = Modifier.size(50.dp), shape = CircleShape, color = Color.White.copy(0.3f)) {
                                            Icon(Icons.Default.Person, null, tint = Color.White)
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text("Welcome back,", color = Color.White.copy(0.8f), fontSize = 12.sp)
                                        Text(
                                            state.name.ifBlank { "Member" },
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    }
                                }

                                // Tombol Kanan
                                Row {
                                    IconButton(onClick = { themeViewModel.toggleTheme() }) {
                                        Icon(
                                            imageVector = if (themeViewModel.isDarkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                                            contentDescription = "Theme",
                                            tint = Color.White
                                        )
                                    }
                                    IconButton(onClick = onNavigateToNotifications) {
                                        Icon(Icons.Outlined.Notifications, null, tint = Color.White)
                                    }
                                }
                            }

                            Spacer(Modifier.height(32.dp))

                            // Stats Cards Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatCardHeader(icon = Icons.Default.FitnessCenter, value = "3", label = "Workouts")
                                StatCardHeader(icon = Icons.Default.AccessTime, value = "36h", label = "Hours")
                                StatCardHeader(icon = Icons.Default.LocalFireDepartment, value = "12k", label = "Calories")
                            }
                        }
                    }
                }

                // 2. GRID MENU
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MenuButton(Icons.Default.FitnessCenter, "Workout", onNavigateToWorkoutLog)
                            MenuButton(Icons.Default.CalendarMonth, "Book Class", onNavigateToBooking)
                            MenuButton(Icons.Default.QrCode, "QR Code", onNavigateToMembership)
                            MenuButton(Icons.Default.Person, "Profile", onNavigateToProfileTab)
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            MenuButton(Icons.Default.Chat, "Consultation", onNavigateToChatList)
                        }
                    }
                }

                // 3. UPCOMING CLASSES
                item {
                    if (upcomingClasses.isNotEmpty()) {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            SectionTitle(title = "Upcoming Classes", onSeeAll = onNavigateToBooking)
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(upcomingClasses) { gymClass ->
                                    PopularClassItem(gymClass = gymClass, onClick = onNavigateToBooking)
                                }
                            }
                        }
                    }
                }

                // 4. BANNER MEMBERSHIP
                item {
                    Spacer(Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(100.dp)
                            .clickable { onNavigateToMembership() },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF212121)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(Modifier.fillMaxSize().padding(16.dp)) {
                            Column(Modifier.align(Alignment.CenterStart)) {
                                Text("Membership Status", color = GymOrange, fontSize = 12.sp)
                                Text("Elite", color = GymOrange, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("View Details >", color = GymOrange, fontSize = 12.sp, modifier = Modifier.align(Alignment.BottomEnd))
                        }
                    }
                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }
}

// --- KOMPONEN PENDUKUNG (Disesuaikan untuk Dark Mode) ---

@Composable
fun StatCardHeader(icon: ImageVector, value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(Color.White.copy(alpha = 0.15f)) // Transparan Putih
            .padding(vertical = 15.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
    }
}

@Composable
fun MenuButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Surface(
            modifier = Modifier.size(70.dp),
            shape = RoundedCornerShape(16.dp),
            // FIX: Warna Surface mengikuti tema (Putih/Abu Gelap)
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                // FIX: Icon mengikuti warna Primary
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(Modifier.height(8.dp))
        // FIX: Teks mengikuti warna onBackground (Hitam/Putih)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun SectionTitle(title: String, onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // FIX: Warna Teks Judul
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        TextButton(onClick = onSeeAll) {
            Text(
                text = "See All",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun PopularClassItem(gymClass: GymClass, onClick: () -> Unit) {
    val imageIndex = kotlin.math.abs(gymClass.classId.hashCode()) % HomeClassImages.size
    val selectedImage = HomeClassImages[imageIndex]
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    val timeString = try { timeFormat.format(Date(gymClass.startTimeMillis)) } catch (e:Exception) { "00:00" }
    val dateString = try { dateFormat.format(Date(gymClass.startTimeMillis)) } catch (e:Exception) { "" }

    Card(
        modifier = Modifier
            .width(220.dp)
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        // FIX: Warna Kartu mengikuti tema
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Box(modifier = Modifier.height(90.dp).fillMaxWidth()) {
                Image(
                    painter = painterResource(id = selectedImage),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    modifier = Modifier.padding(8.dp).align(Alignment.TopStart),
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("$dateString, $timeString", color = MaterialTheme.colorScheme.onPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal=6.dp, vertical=2.dp))
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                // FIX: Warna Teks
                Text(gymClass.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(gymClass.trainerName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
        }
    }
}

// --- ADMIN & USER MANAGEMENT ---


// --- MAIN HOMESCREEN WRAPPER ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    factory: HomeViewModelFactory,
    themeViewModel: ThemeViewModel,
    onNavigateToProfile: () -> Unit,
    onNavigateToWorkoutLog: () -> Unit,
    onNavigateToClassForm: (String?) -> Unit,
    onNavigateToAdminReports: () -> Unit,
    onNavigateToSelection: (String) -> Unit,
    onNavigateToActiveWorkout: () -> Unit,
    onNavigateToMembership: () -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    // Hapus variabel navBackStackEntry dan currentDestination di sini
    // karena sudah dipindahkan ke dalam FloatingBottomNavigation agar lebih bersih

    val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
    val dashboardState by dashboardViewModel.dashboardState.collectAsState()
    val userRole = dashboardState.userRole
    val density = LocalDensity.current
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background, // Warna dasar aplikasi (Hitam/Putih)
        // HAPUS parameter bottomBar dari sini! Kita pindahkan ke dalam Box.
    ) { innerPadding ->

        // GUNAKAN BOX UNTUK MENUMPUK KONTEN & NAVIGASI
        Box(
            modifier = Modifier
                .fillMaxSize()
            // PENTING: Jangan pakai padding innerPadding mentah-mentah ke semua sisi
            // Kita hanya ambil padding bawah sistem jika diperlukan, tapi konten harus full screen
        ) {
            HomeNavHost(
                navController = navController,
                modifier = Modifier
                    .fillMaxSize()
                    // Tambahkan padding bawah manual agar item paling bawah
                    // di scrollable list (misal jadwal) tidak ketutup navigasi.
                    // 100.dp adalah estimasi tinggi navigasi + jarak
                    .padding(bottom = 100.dp),
                factory = factory,
                themeViewModel = themeViewModel,
                userRole = dashboardState.userRole,
                onLogout = onLogout,
                onNavigateToWorkoutLog = onNavigateToWorkoutLog,
                onNavigateToClassForm = onNavigateToClassForm,
                onNavigateToSelection = onNavigateToSelection,
                onNavigateToActiveWorkout = onNavigateToActiveWorkout,
                onNavigateToMembership = onNavigateToMembership,
                onNavigateToAdminReports = onNavigateToAdminReports
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter) // Taruh di bawah
                    .padding(bottom = bottomInset) // Hindari garis gesture HP (garis putih bawah iPhone/Android)
            ) {
                FloatingBottomNavigation(navController = navController)
            }
        }
    }
}

@Composable
fun FloatingBottomNavigation(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // List menu manual agar lebih fleksibel
    val items = listOf(
        HomeNavDestinations.Dashboard,
        HomeNavDestinations.Kelas,
        HomeNavDestinations.Latihan,
        HomeNavDestinations.Profil
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp) // Mengatur posisi melayang dari bawah
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer, // Warna Pill (Ungu gelap/Putih)
            tonalElevation = 3.dp,
            shadowElevation = 10.dp,
            shape = RoundedCornerShape(50.dp), // Bentuk Pill Oval Sempurna
            modifier = Modifier.height(70.dp).fillMaxWidth() // Tinggi kotak oval
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly, // Jarak antar item rata
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { screen ->
                    val isSelected = currentDestination?.route == screen.route

                    // Animasi Naik Turun Ikon
                    val offsetY by animateDpAsState(
                        targetValue = if (isSelected) (-8).dp else 0.dp, // Kalau aktif, naik 8dp
                        animationSpec = tween(durationMillis = 300),
                        label = "offsetAnimation"
                    )

                    // Warna Ikon & Teks
                    val activeColor = MaterialTheme.colorScheme.primary
                    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .offset(y = offsetY) // Terapkan animasi naik
                            .clip(CircleShape) // Efek ripple bulat
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null // Hilangkan ripple abu-abu standar agar bersih
                            ) {
                                if (currentDestination?.route != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                            .padding(8.dp) // Area sentuh
                    ) {
                        // IKON
                        Icon(
                            imageVector = when (screen) {
                                HomeNavDestinations.Dashboard -> Icons.Filled.Home
                                HomeNavDestinations.Kelas -> Icons.Filled.Schedule
                                HomeNavDestinations.Latihan -> Icons.Filled.FitnessCenter
                                HomeNavDestinations.Profil -> Icons.Filled.Person
                                // Gunakan else jika perlu untuk memuaskan compiler
                                else -> Icons.Filled.Home
                            },
                            contentDescription = screen.title,
                            tint = if (isSelected) activeColor else inactiveColor,
                            modifier = Modifier.size(26.dp)
                        )

                        // TEKS (Hanya muncul jika aktif)
                        AnimatedVisibility(visible = isSelected) {
                            Text(
                                text = screen.title ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = activeColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        // Opsional: Titik kecil di bawah teks untuk penanda lebih jelas
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(activeColor)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    factory: HomeViewModelFactory,
    themeViewModel: ThemeViewModel,
    userRole: String,
    onLogout: () -> Unit,
    onNavigateToWorkoutLog: () -> Unit,
    onNavigateToClassForm: (String?) -> Unit,
    onNavigateToSelection: (String) -> Unit,
    onNavigateToActiveWorkout: () -> Unit,
    onNavigateToMembership: () -> Unit,
    onNavigateToAdminReports: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = HomeNavDestinations.Dashboard.route,
        modifier = modifier
    ) {
        // --- 1. DASHBOARD ---
        composable(HomeNavDestinations.Dashboard.route) {
            val dashboardVM: DashboardViewModel = viewModel(factory = factory)
            val adminViewModel: AdminViewModel = viewModel(factory = factory)
            when (userRole) {
                "Admin" -> {
                    AdminDashboardScreen(
                        viewModel = adminViewModel,
                        onNavigateToReports = { navController.navigate("admin_reports") },
                        onNavigateToTrainers = { navController.navigate("trainer_list") },
                        onNavigateToChat = { navController.navigate("member_chat_list") },
                        onNavigateToClasses = { navController.navigate("admin_class_list") } // Route Baru
                    )
                }
                "Trainer" -> {
                    TrainerDashboardScreen(
                        factory = factory,
                        onNavigateToClassForm = onNavigateToClassForm,
                        onNavigateToSchedule = {
                            navController.navigate(HomeNavDestinations.Kelas.route) { launchSingleTop = true }
                        },
                        onNavigateToMembers = {
                            navController.navigate(TRAINER_MEMBERS_ROUTE) { launchSingleTop = true }
                        }
                    )
                }
                "Member" -> {
                    val classBookingViewModel: ClassBookingViewModel = viewModel(factory = factory)
                    MemberDashboardScreen(
                        dashboardViewModel = dashboardVM,
                        classViewModel = classBookingViewModel,
                        themeViewModel = themeViewModel,
                        onNavigateToBooking = { navController.navigate(HomeNavDestinations.Kelas.route) },
                        onNavigateToWorkoutLog = { navController.navigate(HomeNavDestinations.Latihan.route) },
                        onNavigateToMembership = { navController.navigate(HomeNavDestinations.Membership.route) },
                        onNavigateToProfileTab = { navController.navigate(HomeNavDestinations.Profil.route) },
                        onNavigateToSelection = onNavigateToSelection,
                        onNavigateToActiveWorkout = onNavigateToActiveWorkout,
                        onNavigateToNotifications = { navController.navigate("notifications") },
                        onNavigateToChatList = { navController.navigate("chat_list") }
                    )
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GymPurple)
                    }
                }
            }
        }

        // --- Rute Lain (SAMA DENGAN SEBELUMNYA) ---
        composable(HomeNavDestinations.Kelas.route) {
            val dashboardVM: DashboardViewModel = viewModel(factory = factory)
            val dashState by dashboardVM.dashboardState.collectAsState()
            val currentRole = if (dashState.userRole.isNotBlank()) dashState.userRole else userRole
            if (currentRole == "Trainer") {
                TrainerScheduleScreen(factory = factory, onNavigateToClassForm = onNavigateToClassForm)
            } else {
                val classBookingViewModel: ClassBookingViewModel = viewModel(factory = factory)
                ClassBookingScreen(viewModel = classBookingViewModel, onNavigateBack = { navController.navigate(HomeNavDestinations.Dashboard.route) })
            }
        }

        composable(HomeNavDestinations.Latihan.route) {
            val workoutViewModel: WorkoutViewModel = viewModel(factory = factory)
            WorkoutTrackerScreen(viewModel = workoutViewModel, onNavigateBack = { navController.popBackStack() }, onNavigateToSelection = onNavigateToSelection, onNavigateToActiveWorkout = onNavigateToActiveWorkout)
        }

        composable(HomeNavDestinations.Profil.route) {
            ProfileScreen(viewModel = viewModel(factory = factory), onLogout = onLogout, onNavigateBack = { navController.popBackStack() })
        }

        composable(TRAINER_MEMBERS_ROUTE) {
            TrainerMembersScreen(factory = factory, onNavigateUp = { navController.popBackStack() }, onNavigateToDetail = { memberId -> if (memberId.isNotBlank()) navController.navigate("member_detail/$memberId") })
        }

        composable(MEMBER_DETAIL_ROUTE, arguments = listOf(navArgument("memberId") { type = NavType.StringType })) { backStackEntry ->
            val trainerViewModel: TrainerViewModel = viewModel(factory = factory)
            val memberId = backStackEntry.arguments?.getString("memberId") ?: ""
            MemberDetailScreen(navController = navController, viewModel = trainerViewModel, memberId = memberId)
        }

        composable("notifications") { NotificationScreen(onNavigateBack = { navController.popBackStack() }) }

        // Route Chat List
        composable("chat_list") {
            val trainerListViewModel: TrainerListViewModel = viewModel(factory = factory)
            TrainerListScreen(
                viewModel = trainerListViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChat = { trainerId -> navController.navigate("chat_screen/$trainerId") }
            )
        }

        composable("chat_screen/{otherUserId}", arguments = listOf(navArgument("otherUserId") { type = NavType.StringType })) { backStackEntry ->
            val chatViewModel: ChatViewModel = viewModel(factory = factory)
            val otherUserId = backStackEntry.arguments?.getString("otherUserId") ?: ""
            ChatScreen(viewModel = chatViewModel, otherUserId = otherUserId, onNavigateBack = { navController.popBackStack() })
        }

        composable(HomeNavDestinations.Membership.route) {
            val membershipViewModel: MembershipViewModel = viewModel(factory = factory)
            MembershipScreen(viewModel = membershipViewModel, onNavigateBack = { navController.popBackStack() })
        }

        // 1. MEMBER LIST (ADMIN)
        composable("member_chat_list") {
            val adminViewModel: AdminViewModel = viewModel(factory = factory)
            AdminMemberListScreen(
                viewModel = adminViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 2. TRAINER LIST (ADMIN)
        composable("trainer_list") {
            val adminViewModel: AdminViewModel = viewModel(factory = factory)

            // [FIX] Gunakan AdminTrainerListScreen, JANGAN TrainerListScreen
            AdminTrainerListScreen(
                viewModel = adminViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 3. ADMIN REPORTS
        composable("admin_reports") {
            val adminViewModel: AdminViewModel = viewModel(factory = factory)
            AdminReportsScreen(
                viewModel = adminViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 4. CLASS LIST
        composable("admin_class_list") {
            val adminViewModel: AdminViewModel = viewModel(factory = factory)
            AdminClassListScreen(
                viewModel = adminViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}