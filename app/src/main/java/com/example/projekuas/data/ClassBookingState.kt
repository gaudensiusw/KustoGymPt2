package com.example.projekuas.data

data class ClassBookingState(
    val classes: List<GymClass> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)