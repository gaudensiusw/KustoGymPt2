package com.example.projekuas.data

data class HistorySelectionState(
    val isSelectionMode: Boolean = false,
    val selectedIds: Set<String> = emptySet()
)