package com.example.projekuas.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Assessment // Contoh untuk laporan
import androidx.compose.ui.graphics.vector.ImageVector

// Data class sederhana untuk item navigasi
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

// Daftar menu untuk Admin
val AdminNavItems = listOf(
    BottomNavItem(
        label = "Home",
        icon = Icons.Default.Home,
        route = "admin_home"
    ),
    BottomNavItem(
        label = "Manage",
        icon = Icons.Default.List,
        route = "admin_manage_list" // Halaman daftar member/trainer
    ),
    BottomNavItem(
        label = "Profile",
        icon = Icons.Default.Person,
        route = "admin_profile"
    )
)

// Daftar menu untuk Trainer
val TrainerNavItems = listOf(
    BottomNavItem(
        label = "Home",
        icon = Icons.Default.Home,
        route = "trainer_home"
    ),
    BottomNavItem(
        label = "Schedules",
        icon = Icons.Default.List, // Atau icon Calendar
        route = "trainer_schedules"
    ),
    BottomNavItem(
        label = "Profile",
        icon = Icons.Default.Person,
        route = "trainer_profile"
    )
)