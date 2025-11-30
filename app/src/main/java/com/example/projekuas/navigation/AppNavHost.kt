package com.example.projekuas.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
import com.example.projekuas.ui.chat.ChatScreen

// --- Import Screens ---
import com.example.projekuas.ui.classform.ClassFormScreen
import com.example.projekuas.ui.home.HomeScreen
import com.example.projekuas.ui.login.LoginScreen
import com.example.projekuas.ui.profile.MembershipScreen
import com.example.projekuas.ui.profile.ProfileScreen
import com.example.projekuas.ui.signup.SignUpScreen
import com.example.projekuas.ui.workoutlog.WorkoutLogScreen
import com.example.projekuas.ui.dashboard.AdminReportsScreen
import com.example.projekuas.ui.dashboard.TrainerListScreen
// MemberDetailScreen tidak diimport disini karena sudah ada di HomeNavHost (Nested)
import com.example.projekuas.ui.workout.ActiveWorkoutScreen
import com.example.projekuas.ui.workout.ExerciseSelectionScreen

// --- Import ViewModels ---
import com.example.projekuas.viewmodel.AuthViewModelFactory
import com.example.projekuas.viewmodel.ChatViewModel
import com.example.projekuas.viewmodel.HomeViewModelFactory
import com.example.projekuas.viewmodel.MembershipViewModel
import com.example.projekuas.viewmodel.ProfileViewModelFactory
import com.example.projekuas.viewmodel.ThemeViewModel
import com.example.projekuas.viewmodel.WorkoutLogViewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

// --- Import Firebase ---
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage

@Composable
fun AppNavHost(
    workoutLogDao: WorkoutLogDao,
    themeViewModel: ThemeViewModel
){
    val context = LocalContext.current
    val navController = rememberNavController()

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

    // FIX 1: Inisialisasi ChatRepository (Ini yang sebelumnya hilang)
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
            chatRepository // Sekarang ini tidak error karena chatRepository sudah dibuat di atas
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

    // --- 3. Definisi Aksi Navigasi (Navigation Actions) ---
    val navigateToClassForm = { classId: String? ->
        if (classId != null) {
            navController.navigate("${HomeNavDestinations.ClassForm.route}?classId=$classId")
        } else {
            navController.navigate(HomeNavDestinations.ClassForm.route)
        }
    }

    val navigateToActiveWorkout = {
        navController.navigate(HomeNavDestinations.ActiveWorkout.route)
    }

    val navigateToSelection = { muscleGroup: String ->
        navController.navigate("${HomeNavDestinations.ExerciseSelection.route}/$muscleGroup")
    }

    val navigateToAdminReports = {
        navController.navigate(HomeNavDestinations.AdminReports.route)
    }

    val navigateToMembership = {
        navController.navigate(HomeNavDestinations.Membership.route)
    }

    // --- 4. Navigation Host ---
    NavHost(
        navController = navController,
        startDestination = NavDestinations.LOGIN
    ) {
        // --- Rute Login ---
        composable(NavDestinations.LOGIN) {
            LoginScreen(
                viewModel = viewModel(factory = authFactory),
                onNavigateToSignUp = { navController.navigate(NavDestinations.SIGN_UP) },
                onLoginSuccess = {
                    navController.navigate(NavDestinations.HOME) {
                        popUpTo(NavDestinations.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // --- Rute Sign Up ---
        composable(NavDestinations.SIGN_UP) {
            SignUpScreen(
                viewModel = viewModel(factory = authFactory),
                onNavigateBackToLogin = { navController.popBackStack() },
                onRegistrationSuccess = {
                    navController.navigate(NavDestinations.LOGIN) {
                        popUpTo(NavDestinations.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // --- Rute HOME (Dashboard Utama) ---
        composable(NavDestinations.HOME) {
            HomeScreen(
                factory = homeFactory,
                themeViewModel = themeViewModel, // <-- Teruskan ini!
                onNavigateToProfile = { navController.navigate(NavDestinations.PROFILE) },
                onNavigateToWorkoutLog = { navController.navigate(NavDestinations.WORKOUT_LOG) },
                onNavigateToClassForm = navigateToClassForm,
                onNavigateToAdminReports = { navController.navigate(HomeNavDestinations.AdminReports.route) },
                onNavigateToActiveWorkout = { navController.navigate(HomeNavDestinations.ActiveWorkout.route) },
                onNavigateToSelection = { mg -> navController.navigate("${HomeNavDestinations.ExerciseSelection.route}/$mg") },
                onNavigateToMembership = { navController.navigate(HomeNavDestinations.Membership.route) },
                onLogout = {
                    navController.navigate(NavDestinations.LOGIN) {
                        popUpTo(NavDestinations.HOME) {
                            inclusive = true
                        }
                    }
                }
            )
        }



        // --- Rute Profile ---
        composable(NavDestinations.PROFILE) {
            ProfileScreen(
                viewModel = viewModel(factory = profileFactory),
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(NavDestinations.LOGIN) {
                        popUpTo(NavDestinations.HOME) { inclusive = true }
                    }
                },
            )
        }

        // --- Rute Membership ---
        composable(HomeNavDestinations.Membership.route) {
            val membershipViewModel: MembershipViewModel = viewModel(factory = homeFactory)
            MembershipScreen(
                viewModel = membershipViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- Rute Workout Log ---
        composable(NavDestinations.WORKOUT_LOG) {
            WorkoutLogScreen(
                viewModel = viewModel(factory = workoutLogFactory),
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // --- Rute Class Form (Add/Edit Class) ---
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
                    navController.navigate(NavDestinations.HOME) {
                        popUpTo(NavDestinations.HOME) { inclusive = true }
                    }
                }
            )
        }

        // --- Rute Admin Reports ---
        composable(HomeNavDestinations.AdminReports.route) {
            AdminReportsScreen(
                factory = homeFactory,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Rute Exercise Selection
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
                // INI WAJIB DITAMBAHKAN:
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Rute TRAINER LIST (Daftar Pelatih)
        /*composable("trainer_list") {
            TrainerListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChat = { trainerId ->
                    navController.navigate("chat_screen/$trainerId")
                }
            )
        *///}

        // --- Rute Chat Screen (REALTIME) ---
        composable(
            route = "chat_screen/{otherUserId}",
            arguments = listOf(navArgument("otherUserId") { type = NavType.StringType })
        ) { backStackEntry ->

            // FIX 2: Menggunakan 'homeFactory' (nama variabel yang benar), bukan 'factory'
            val chatViewModel: ChatViewModel = viewModel(factory = homeFactory)

            val otherUserId = backStackEntry.arguments?.getString("otherUserId") ?: ""

            ChatScreen(
                viewModel = chatViewModel,
                otherUserId = otherUserId,
                onNavigateBack = {
                    // Kembali ke layar sebelumnya
                    navController.popBackStack()
                }
            )
        }

        // --- Rute Active Workout ---
        composable(HomeNavDestinations.ActiveWorkout.route) {
            ActiveWorkoutScreen(
                viewModel = viewModel(factory = homeFactory),
                onFinish = {
                    navController.popBackStack()
                }
            )
        }
    }
}