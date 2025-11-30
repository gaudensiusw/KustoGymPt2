package com.example.projekuas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekuas.data.AdminRepository
import com.example.projekuas.data.UserProfile
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AdminUiState(
    val users: List<UserProfile> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class AdminViewModel(
    private val adminRepository: AdminRepository
) : ViewModel() {

    // Amati daftar semua pengguna secara real-time
    val uiState: StateFlow<AdminUiState> = adminRepository.getAllUsersStream()
        .map { userList ->
            AdminUiState(users = userList, isLoading = false)
        }
        .catch { e ->
            emit(AdminUiState(isLoading = false, error = e.message))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AdminUiState()
        )

    // Kontrol: Mengubah peran pengguna
    fun onUpdateUserRole(userId: String, newRole: String) {
        viewModelScope.launch {
            try {
                adminRepository.updateUserRole(userId, newRole)
                // TODO: Beri feedback sukses
            } catch (e: Exception) {
                // TODO: Beri feedback error
            }
        }
    }

    // Kontrol: Menghapus pengguna
    fun onDeleteUser(userId: String) {
        viewModelScope.launch {
            // TODO: Tambahkan konfirmasi UI sebelum memanggil delete
            adminRepository.deleteUser(userId)
        }
    }
}