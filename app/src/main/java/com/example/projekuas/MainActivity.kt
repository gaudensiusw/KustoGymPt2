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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.projekuas.data.AppDatabase
import com.example.projekuas.navigation.AppNavHost
import com.example.projekuas.ui.theme.ProjekUASTheme
import com.example.projekuas.viewmodel.ThemeViewModel
import com.example.projekuas.worker.WorkoutSyncWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen() // Correct call with import
        super.onCreate(savedInstanceState)

        val dao = AppDatabase.getDatabase(applicationContext).workoutLogDao()
        enableEdgeToEdge()
        setContent {

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<WorkoutSyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "WorkoutSync",
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
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