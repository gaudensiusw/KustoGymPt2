package com.example.projekuas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekuas.data.ProfileRepository
import com.example.projekuas.data.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TrainerListViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _trainers = MutableStateFlow<List<UserProfile>>(emptyList())
    val trainers: StateFlow<List<UserProfile>> = _trainers

    init {
        fetchTrainers()
    }

    private fun fetchTrainers() {
        viewModelScope.launch {
            profileRepository.getTrainers().collect { list ->
                _trainers.value = list
            }
        }
    }
}