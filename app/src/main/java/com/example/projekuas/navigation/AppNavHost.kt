package com.example.projekuas.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// --- Import Data & Repositories ---
import com.example.projekuas.data.AdminRepositoryImpl
import com.example.projekuas.data.AuthRepositoryImpl
import com.example.projekuas.data.ChatRepository
import com.example.projekuas.data.ClassRepositoryImpl
import com.example.projekuas.data.ProfileRepositoryImpl
import com.example.projekuas.data.WorkoutDataRepositoryImpl
import com.example.projekuas.data.WorkoutLogDao

// --- Import Screens ---
import com.example.projekuas.ui.chat.ChatScreen
import com.example.projekuas.ui.classform.ClassFormScreen
import com.example.projekuas.ui.dashboard.TrainerReviewsScreen
import com.example.projekuas.ui.home.* // Import MemberDashboard & Components
import com.example.projekuas.ui.login.LoginScreen
import com.example.projekuas.ui.profile.MembershipScreen
import com.example.projekuas.ui.profile.ProfileScreen
import com.example.projekuas.ui.signup.SignUpScreen
import com.example.projekuas.ui.workoutlog.WorkoutLogScreen
import com.example.projekuas.ui.dashboard.AdminReportsScreen
import com.example.projekuas.ui.dashboard.TrainerListScreen
import com.example.projekuas.ui.workout.ActiveWorkoutScreen
import com.example.projekuas.ui.workout.ExerciseSelectionScreen

// Import Additional Screens
import com.example.projekuas.ui.booking.ClassBookingScreen
import com.example.projekuas.ui.components.FloatingBottomNavigation
import com.example.projekuas.ui.dashboard.AdminClassListScreen
import com.example.projekuas.ui.dashboard.AdminDashboardScreen
import com.example.projekuas.ui.dashboard.AdminMemberListScreen
import com.example.projekuas.ui.dashboard.AdminTrainerListScreen
import com.example.projekuas.ui.dashboard.MemberDetailScreen
import com.example.projekuas.ui.dashboard.NotificationScreen
import com.example.projekuas.ui.dashboard.TrainerDashboardScreen
import com.example.projekuas.ui.dashboard.TrainerMembersScreen
import com.example.projekuas.ui.dashboard.TrainerNotificationScreen
import com.example.projekuas.ui.dashboard.TrainerScheduleScreen
import com.example.projekuas.ui.theme.GymPurple
import com.example.projekuas.ui.workout.WorkoutTrackerScreen

// --- Import ViewModels ---
import com.example.projekuas.viewmodel.AdminViewModel
import com.example.projekuas.viewmodel.AuthViewModelFactory
import com.example.projekuas.viewmodel.ChatViewModel
import com.example.projekuas.viewmodel.ClassBookingViewModel
import com.example.projekuas.viewmodel.DashboardViewModel
import com.example.projekuas.viewmodel.HomeViewModelFactory
import com.example.projekuas.viewmodel.MembershipViewModel
import com.example.projekuas.viewmodel.ProfileViewModelFactory
import com.example.projekuas.viewmodel.ThemeViewModel
import com.example.projekuas.viewmodel.TrainerListViewModel
import com.example.projekuas.viewmodel.TrainerViewModel
import com.example.projekuas.viewmodel.WorkoutLogViewModelFactory
import com.example.projekuas.viewmodel.WorkoutViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage

@Composable
fun AppNavHost(
    workoutLogDao: WorkoutLogDao,
    themeViewModel: ThemeViewModel
){
    val context = LocalContext.current
    val navController = rememberNavController()
    
    // --- 0. State Navigasi untuk Bottom Bar ---
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Tentukan kapan Bottom Bar muncul
    // Muncul jika BUKAN Login, SignUp, atau ActiveWorkout (full screen)
    val showBottomBar = currentRoute != NavDestinations.LOGIN && 
                       currentRoute != NavDestinations.SIGN_UP &&
                       currentRoute != HomeNavDestinations.ActiveWorkout.route &&
                       // Sembunyikan Bottom Bar di halaman Form & Detail & Chat & Reviews agar tidak menumpuk
                       currentRoute?.startsWith(HomeNavDestinations.ClassForm.route) != true &&
                       currentRoute?.startsWith("member_detail") != true &&
                       currentRoute?.startsWith("chat_screen") != true &&
                       currentRoute != "trainer_reviews"

    // Inisialisasi Firebase
    val firebaseAuth = Firebase.auth
    val firebaseFirestore = Firebase.firestore
    val firebaseStorage = Firebase.storage

    // --- 1. Initialize Repositories ---
    val classRepository = remember { ClassRepositoryImpl(firebaseFirestore, context) }

    val profileRepository = remember {
        ProfileRepositoryImpl(
            context,
            auth = firebaseAuth,
            firestore = firebaseFirestore,
            storage = firebaseStorage
        )
    }

    val authRepository = remember { AuthRepositoryImpl(firebaseAuth, firebaseFirestore) }
    val adminRepository = remember { AdminRepositoryImpl(firebaseFirestore) }

    val workoutDataRepository = remember {
        WorkoutDataRepositoryImpl(firebaseFirestore)
    }

    val chatRepository = remember { ChatRepository(firebaseFirestore) }

    // --- 2. Initialize Factories ---
    val authFactory = remember {
        AuthViewModelFactory(
            authRepository,
            profileRepository = profileRepository,
            workoutDataRepository = workoutDataRepository
        )
    }

    val homeFactory = remember {
        HomeViewModelFactory(
            context,
            authRepository,
            workoutDataRepository,
            profileRepository,
            classRepository,
            adminRepository,
            chatRepository
        )
    }

    val profileFactory = remember {
        ProfileViewModelFactory(
            profileRepository = profileRepository,
            authRepository = authRepository
        )
    }

    val workoutLogFactory = remember {
        WorkoutLogViewModelFactory(workoutDataRepository, authRepository)
    }
    
    // ViewModel Global untuk Dashboard Logic
    // Kita inisialisasi di sini agar state userRole bisa diakses untuk routing
    val dashboardGlobalViewModel: DashboardViewModel = viewModel(factory = homeFactory)
    val dashboardState by dashboardGlobalViewModel.dashboardState.collectAsState()
    val userRole = dashboardState.userRole

    // --- 3. Definisi Aksi Navigasi Reuse ---
    val navigateToClassForm = { classId: String? ->
        if (classId != null) {
            navController.navigate("${HomeNavDestinations.ClassForm.route}?classId=$classId")
        } else {
            navController.navigate(HomeNavDestinations.ClassForm.route)
        }
    }
    
    // --- 4. Navigation Host dengan Scaffold ---
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                FloatingBottomNavigation(navController = navController, role = userRole)
            }
        }
    ) { innerPadding ->
        // Kita gunakan Box lagi jika butuh kontrol padding manual, 
        // tapi Scaffold sudah handle padding dasar. 
        // FloatingBottomNavigation kita "melayang" dan punya custom box sendiri.
        
        val startDestination = if (firebaseAuth.currentUser != null) {
            HomeNavDestinations.Dashboard.route
        } else {
            NavDestinations.LOGIN
        }

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize()
        ) {
            // --- Rute Login ---
            composable(NavDestinations.LOGIN) {
                LoginScreen(
                    viewModel = viewModel(factory = authFactory),
                    onNavigateToSignUp = { navController.navigate(NavDestinations.SIGN_UP) },
                    onLoginSuccess = {
                        // Setelah login sukses, arahkan ke Dashboard
                        navController.navigate(HomeNavDestinations.Dashboard.route) {
                            popUpTo(NavDestinations.LOGIN) { inclusive = true }
                        }
                    }
                )
            }
    
            // --- Rute Sign Up ---
            composable(NavDestinations.SIGN_UP) {
                SignUpScreen(
                    factory = homeFactory,
                    onNavigateToLogin = {
                        navController.navigate(NavDestinations.LOGIN) {
                            popUpTo(NavDestinations.SIGN_UP) { inclusive = true }
                        }
                    }
                )
            }
    
            // ===========================================
            // --- MAIN TABS (BOTTOM NAVIGATION) ---
            // ===========================================
            
            // 1. DASHBOARD (HOME)
            composable(HomeNavDestinations.Dashboard.route) {
                // Gunakan User Role untuk menentukan Dashboard mana yang muncul
                val adminViewModel: AdminViewModel = viewModel(factory = homeFactory)
                
                when (userRole) {
                    "Admin" -> {
                        AdminDashboardScreen(
                            viewModel = adminViewModel,
                            themeViewModel = themeViewModel,
                            name = dashboardState.name,
                            onNavigateToReports = { navController.navigate(HomeNavDestinations.AdminReports.route) },
                            onNavigateToTrainers = { navController.navigate("trainer_list") },
                            onNavigateToChat = { navController.navigate("member_chat_list") },
                            onNavigateToClasses = { navController.navigate("admin_class_list") }
                        )
                    }
                    "Trainer" -> {
                        TrainerDashboardScreen(
                            factory = homeFactory,
                            themeViewModel = themeViewModel,
                            onNavigateToClassForm = navigateToClassForm,
                            onNavigateToSchedule = {
                                navController.navigate(HomeNavDestinations.Kelas.route)
                            },
                            onNavigateToMembers = {
                                navController.navigate(TRAINER_MEMBERS_ROUTE)
                            },
                            onNavigateToReviews = {
                                navController.navigate("trainer_reviews")
                            },
                            onNavigateToNotifications = {
                                navController.navigate("trainer_notifications")
                            }
                        )
                    }
                    "Member" -> {
                        val classBookingViewModel: ClassBookingViewModel = viewModel(factory = homeFactory)
                        MemberDashboardScreen(
                            dashboardViewModel = dashboardGlobalViewModel,
                            classViewModel = classBookingViewModel,
                            themeViewModel = themeViewModel,
                            onNavigateToBooking = { navController.navigate(HomeNavDestinations.Kelas.route) },
                            onNavigateToWorkoutLog = { navController.navigate(HomeNavDestinations.Latihan.route) },
                            onNavigateToMembership = { navController.navigate(HomeNavDestinations.Membership.route) },
                            onNavigateToProfileTab = { navController.navigate(HomeNavDestinations.Profil.route) },
                            onNavigateToSelection = { mg -> navController.navigate("${HomeNavDestinations.ExerciseSelection.route}/$mg") },
                            onNavigateToActiveWorkout = { navController.navigate(HomeNavDestinations.ActiveWorkout.route) },
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
            
            // 2. KELAS (SCHEDULE/BOOKING)
            composable(HomeNavDestinations.Kelas.route) {
                // Logic role user untuk menampilkan Trainer Schedule atau Member Booking
                val currentRole = if (userRole.isNotBlank()) userRole else "Member" // Default fallback
                
                if (currentRole == "Trainer") {
                    TrainerScheduleScreen(factory = homeFactory, onNavigateToClassForm = navigateToClassForm)
                } else {
                    val classBookingViewModel: ClassBookingViewModel = viewModel(factory = homeFactory)
                    ClassBookingScreen(
                        viewModel = classBookingViewModel, 
                        // Back ke Dashboard jika tekan back system, tapi di bottom nav user bisa klik tab lain
                        onNavigateBack = { navController.navigate(HomeNavDestinations.Dashboard.route) }
                    )
                }
            }
            
            // 3. LATIHAN (WORKOUT TRACKER)
            composable(HomeNavDestinations.Latihan.route) {
                val workoutViewModel: WorkoutViewModel = viewModel(factory = homeFactory)
                WorkoutTrackerScreen(
                    viewModel = workoutViewModel, 
                    onNavigateBack = { navController.popBackStack() }, 
                    onNavigateToSelection = { mg -> navController.navigate("${HomeNavDestinations.ExerciseSelection.route}/$mg") }, 
                    onNavigateToActiveWorkout = { navController.navigate(HomeNavDestinations.ActiveWorkout.route) }
                )
            }
            
            // 4. PROFIL
            composable(HomeNavDestinations.Profil.route) {
                ProfileScreen(
                    viewModel = viewModel(factory = profileFactory), 
                    onLogout = {
                        navController.navigate(NavDestinations.LOGIN) {
                            popUpTo(HomeNavDestinations.Dashboard.route) { inclusive = true }
                        }
                    }, 
                    onNavigateBack = { navController.popBackStack() }
                )
            }
    
            // ===========================================
            // --- OTHER FEATURES ---
            // ===========================================
    
            // Membership Status
            composable(HomeNavDestinations.Membership.route) {
                val membershipViewModel: MembershipViewModel = viewModel(factory = homeFactory)
                MembershipScreen(
                    viewModel = membershipViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
    
            // Workout Log History (Mungkin berbeda dengan Latihan Tab)
            composable(NavDestinations.WORKOUT_LOG) {
                WorkoutLogScreen(
                    viewModel = viewModel(factory = workoutLogFactory),
                    onNavigateBack = { navController.popBackStack() },
                )
            }
    
            // Class Form (Add/Edit)
            composable(
                route = "${HomeNavDestinations.ClassForm.route}?classId={classId}",
                arguments = listOf(navArgument("classId") {
                    type = NavType.StringType; nullable = true; defaultValue = null
                })
            ) { backStackEntry ->
                ClassFormScreen(
                    factory = homeFactory,
                    classIdToEdit = backStackEntry.arguments?.getString("classId"),
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            // Active Workout
            composable(HomeNavDestinations.ActiveWorkout.route) {
                ActiveWorkoutScreen(
                    viewModel = viewModel(factory = homeFactory),
                    onFinish = {
                        navController.popBackStack()
                    }
                )
            }
            
            // Exercise Selection
            composable(
                route = "${HomeNavDestinations.ExerciseSelection.route}/{muscleGroup}",
                arguments = listOf(navArgument("muscleGroup") { type = NavType.StringType })
            ) { backStackEntry ->
                ExerciseSelectionScreen(
                    viewModel = viewModel(factory = homeFactory),
                    muscleGroup = backStackEntry.arguments?.getString("muscleGroup") ?: "",
                    onNavigateToActiveWorkout = {
                        navController.navigate(HomeNavDestinations.ActiveWorkout.route)
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
    
            // Notification
            composable("notifications") { 
                NotificationScreen(onNavigateBack = { navController.popBackStack() }) 
            }
            
            // Chat List (Member view of Trainers)
            composable("chat_list") {
                val trainerListViewModel: TrainerListViewModel = viewModel(factory = homeFactory)
                TrainerListScreen(
                    viewModel = trainerListViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToChat = { trainerId -> navController.navigate("chat_screen/$trainerId") }
                )
            }
            
            // Trainer Reviews
            composable("trainer_reviews") {
                TrainerReviewsScreen(
                     factory = homeFactory,
                     onNavigateBack = { navController.popBackStack() }
                )
            }

            // Chat Screen
            composable(
                route = "chat_screen/{trainerId}",
                arguments = listOf(navArgument("trainerId") { type = NavType.StringType })
            ) { backStackEntry ->
                val trainerId = backStackEntry.arguments?.getString("trainerId") ?: ""
                val chatViewModel: com.example.projekuas.viewmodel.ChatViewModel = viewModel(factory = homeFactory)
                ChatScreen(
                    viewModel = chatViewModel,
                    otherUserId = trainerId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Trainer's Member List
            composable(TRAINER_MEMBERS_ROUTE) {
                TrainerMembersScreen(
                    factory = homeFactory, 
                    onNavigateUp = { navController.popBackStack() }, 
                    onNavigateToDetail = { memberId -> 
                        if (memberId.isNotBlank()) navController.navigate("member_detail/$memberId") 
                    }
                )
            }
            
            // Member Detail (for Trainer)
            composable(MEMBER_DETAIL_ROUTE, arguments = listOf(navArgument("memberId") { type = NavType.StringType })) { backStackEntry ->
                val trainerViewModel: TrainerViewModel = viewModel(factory = homeFactory)
                val memberId = backStackEntry.arguments?.getString("memberId") ?: ""
                MemberDetailScreen(navController = navController, viewModel = trainerViewModel, memberId = memberId)
            }

            // Trainer Notifications
            composable("trainer_notifications") {
                TrainerNotificationScreen(onNavigateBack = { navController.popBackStack() })
            }
    
            // Admin Member List
            composable("member_chat_list") {
                val adminViewModel: AdminViewModel = viewModel(factory = homeFactory)
                AdminMemberListScreen(
                    viewModel = adminViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            // Admin Trainer List
            composable("trainer_list") {
                val adminViewModel: AdminViewModel = viewModel(factory = homeFactory)
                AdminTrainerListScreen(
                    viewModel = adminViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            // Admin Class List
            composable("admin_class_list") {
                val adminViewModel: AdminViewModel = viewModel(factory = homeFactory)
                AdminClassListScreen(
                    viewModel = adminViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToForm = navigateToClassForm
                )
            }

            // Admin Reports
            composable(HomeNavDestinations.AdminReports.route) {
                val adminViewModel: AdminViewModel = viewModel(factory = homeFactory)
                AdminReportsScreen(
                    viewModel = adminViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
