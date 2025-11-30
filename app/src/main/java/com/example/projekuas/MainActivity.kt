package com.example.projekuas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.projekuas.data.AppDatabase
import com.example.projekuas.navigation.AppNavHost
import com.example.projekuas.ui.theme.ProjekUASTheme
import com.example.projekuas.viewmodel.ThemeViewModel

class MainActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dao = AppDatabase.getDatabase(applicationContext).workoutLogDao()
        enableEdgeToEdge()
        setContent {
            // Deteksi settingan HP pengguna
            val systemDark = isSystemInDarkTheme()

            // Set state awal ViewModel agar mengikuti sistem (hanya sekali saat start)
            // Kita gunakan SideEffect atau logika inisialisasi sederhana
            if (themeViewModel.isFirstLaunch) {
                themeViewModel.setSystemTheme(systemDark)
            }

            ProjekUASTheme(darkTheme = themeViewModel.isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHost(
                        workoutLogDao = dao,
                        themeViewModel = themeViewModel
                    )
                }
            }
        }
    }
}