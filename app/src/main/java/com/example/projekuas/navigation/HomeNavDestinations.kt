package com.example.projekuas.navigation

// Anggap Anda menggunakan Icons.Default.* dan ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

// Ubah sealed class agar hanya memiliki parameter yang benar-benar dibutuhkan oleh setiap object
// Asumsi: Title dan Icon TIDAK WAJIB untuk semua rute (hanya wajib untuk Bottom Nav items)
sealed class HomeNavDestinations(
    val route: String,
    val title: String? = null,
    val icon: ImageVector? = null
) {
    // --- BOTTOM NAVIGATION ITEMS (Contoh, biasanya wajib punya Title & Icon) ---
    // Pastikan ini sesuai dengan apa yang Anda gunakan di Bottom Nav
    object Dashboard : HomeNavDestinations("dashboard", "Home", Icons.Default.Home)
    object Profil : HomeNavDestinations("profile", "Profil", Icons.Default.Person)

    // Object A (Diambil dari kode Anda) - Jika ini adalah Form Kelas
    object ClassForm : HomeNavDestinations("class_form", "Form Kelas", Icons.Default.Build)
    object ActiveWorkout : HomeNavDestinations("active_workout")
    // Object E (Seleksi Latihan) - Tidak perlu Title/Icon
    object ExerciseSelection : HomeNavDestinations("exercise_selection/{muscleGroup}")
    // --- Rute Tambahan ---
    // Tambahkan ini agar tidak error di HomeScreen
    object WorkoutLog : HomeNavDestinations("workout_log_history")
    // --- MENU MEMBER ---
    object Kelas : HomeNavDestinations("class_booking", "Kelas", Icons.Default.Schedule)
    object Latihan : HomeNavDestinations("workout_log", "Latihan", Icons.Default.FitnessCenter)

    // --- MENU TRAINER ---
    // Gunakan 'People' untuk daftar klien, 'DateRange' untuk jadwal mengajar
    object TrainerClients : HomeNavDestinations("trainer_clients", "Klien", Icons.Default.People)
    object TrainerSchedule : HomeNavDestinations("trainer_schedule", "Jadwal", Icons.Default.DateRange)

    // --- MENU ADMIN ---
    object AdminReports : HomeNavDestinations("admin_reports", "Laporan", Icons.Default.Analytics) // Analytics/Assessment
    object AdminUsers : HomeNavDestinations("admin_users", "User", Icons.Default.ManageAccounts)

    // --- SUB-MENU LAIN ---
    object Notifications : HomeNavDestinations("notifications") // Rute Notifikasi
    object Membership : HomeNavDestinations("membership", "Membership Status", Icons.Default.Star)
}