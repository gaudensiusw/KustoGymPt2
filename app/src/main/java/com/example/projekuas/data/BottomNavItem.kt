package com.example.projekuas.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Assessment // Contoh untuk laporan
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.People
import androidx.compose.ui.graphics.vector.ImageVector

// Data class sederhana untuk item navigasi
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

// Menu untuk Admin
val AdminNavItems = listOf(
    BottomNavItem("Home", Icons.Default.Home, "admin_home"),
    BottomNavItem("Users", Icons.Default.People, "admin_users"), // Halaman Direktori
    BottomNavItem("Profile", Icons.Default.Person, "profile")
)

// Menu untuk Trainer
val TrainerNavItems = listOf(
    BottomNavItem("Home", Icons.Default.Home, "trainer_home"),
    BottomNavItem("Schedule", Icons.Default.DateRange, "trainer_schedule"), // Halaman Kalender
    BottomNavItem("Profile", Icons.Default.Person, "profile")
)
