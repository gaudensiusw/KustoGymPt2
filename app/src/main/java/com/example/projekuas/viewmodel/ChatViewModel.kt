package com.example.projekuas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekuas.data.AuthRepository
import com.example.projekuas.data.ChatRepository
import com.example.projekuas.data.Message
import com.example.projekuas.data.NotificationRepository // Import ini
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository // Tambah ini
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    val currentUserId = authRepository.getCurrentUserId() ?: ""

    fun loadMessages(otherUserId: String) {
        if (currentUserId.isBlank()) return
        viewModelScope.launch {
            chatRepository.getMessages(currentUserId, otherUserId).collect {
                _messages.value = it
            }
        }
    }

    fun sendMessage(otherUserId: String, text: String) {
        if (text.isBlank() || currentUserId.isBlank()) return
        viewModelScope.launch {
            // 1. Kirim Pesan Chat
            chatRepository.sendMessage(currentUserId, otherUserId, text)

            // 2. Kirim Notifikasi ke Penerima
            notificationRepository.sendNotification(
                toUserId = otherUserId,
                title = "Pesan Baru",
                message = text,
                type = "chat"
            )
        }
    }
}