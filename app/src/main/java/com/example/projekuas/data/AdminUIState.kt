package com.example.projekuas.data

data class AdminUiState(
    val users: List<UserProfile> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)