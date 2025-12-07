package com.example.projekuas.data

data class ClassBookingState(
    val classes: List<GymClass> = emptyList(),
    val myBookings: List<GymClass> = emptyList(),
    val bookedClassIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val selectedDate: Long = System.currentTimeMillis(),
    val searchQuery: String = "",
    val error: String? = null,
    val classDates: Set<Long> = emptySet()
)