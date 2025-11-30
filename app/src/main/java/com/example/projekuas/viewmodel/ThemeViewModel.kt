package com.example.projekuas.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ThemeViewModel : ViewModel() {
    var isDarkMode by mutableStateOf(false)
        private set

    // Flag untuk memastikan kita hanya set auto sekali saat launch
    var isFirstLaunch = true

    fun toggleTheme() {
        isDarkMode = !isDarkMode
    }

    // Fungsi baru untuk set sesuai sistem
    fun setSystemTheme(isSystemDark: Boolean) {
        isDarkMode = isSystemDark
        isFirstLaunch = false
    }
}