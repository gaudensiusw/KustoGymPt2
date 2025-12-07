package com.example.projekuas.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- 1. MEMBER SCHEMES (UNGU) ---
private val MemberDarkScheme = darkColorScheme(
    primary = GymPurpleDark,      // Ungu Gelap
    secondary = GymOrange,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceContainer = SurfaceDark,
    onPrimary = TextWhite,
    onSurface = TextWhite
)

private val MemberLightScheme = lightColorScheme(
    primary = GymPurple,          // Ungu Terang
    secondary = GymOrange,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceContainer = SurfaceLight,
    onPrimary = TextWhite,
    onSurface = TextBlack
)

// --- 2. TRAINER SCHEMES (BIRU) ---
private val TrainerDarkScheme = darkColorScheme(
    primary = GymBlueDark,        // Biru Tua
    secondary = GymOrange,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceContainer = SurfaceDark,
    onPrimary = TextWhite,
    onSurface = TextWhite
)

private val TrainerLightScheme = lightColorScheme(
    primary = GymBlue,            // Biru Cerah
    secondary = GymOrange,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceContainer = SurfaceLight,
    onPrimary = TextWhite,
    onSurface = TextBlack
)

// --- 3. ADMIN SCHEMES (HIJAU) ---
private val AdminDarkScheme = darkColorScheme(
    primary = GymGreenDark,       // Hijau Tua
    secondary = GymOrange,
    background = BackgroundDark,
    surface = SurfaceDarkAdmin, // Make cards lighter in dark mode
    surfaceContainer = SurfaceDarkAdmin,
    onPrimary = TextWhite,
    onSurface = TextWhite
)

private val AdminLightScheme = lightColorScheme(
    primary = GymGreen,           // Hijau Segar
    secondary = GymOrange,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceContainer = SurfaceLight,
    onPrimary = TextWhite,
    onSurface = TextBlack
)

@Composable
fun ProjekUASTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    userRole: String? = "Member", // Parameter Kunci
    content: @Composable () -> Unit
) {
    // Logika Pemilihan Warna
    val colorScheme = when (userRole) {
        "Trainer" -> if (darkTheme) TrainerDarkScheme else TrainerLightScheme
        "Admin" -> if (darkTheme) AdminDarkScheme else AdminLightScheme
        else -> if (darkTheme) MemberDarkScheme else MemberLightScheme // Default: Member
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()

            // Ikon status bar: Putih jika dark mode / background gelap, Hitam jika light mode
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Pastikan Anda punya file Type.kt
        content = content
    )
}