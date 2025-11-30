package com.example.projekuas.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.projekuas.data.UserProfile
import com.example.projekuas.ui.components.ConfirmationDialog
import com.example.projekuas.ui.components.RoleSelectionDialog
import com.example.projekuas.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTrainerListScreen(
    viewModel: AdminViewModel, // PENTING: Pakai AdminViewModel
    onNavigateBack: () -> Unit
) {
    val trainers by viewModel.trainerList.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRoleDialog by remember { mutableStateOf(false) } // Opsional: jika ingin ubah trainer jadi member/admin
    var selectedTrainer by remember { mutableStateOf<UserProfile?>(null) }
    if (showDeleteDialog && selectedTrainer != null) {
        ConfirmationDialog(
            title = "Hapus Trainer?",
            message = "Yakin hapus ${selectedTrainer?.name}? Akses trainer akan dicabut.",
            onConfirm = {
                selectedTrainer?.let { viewModel.deleteUser(it.userId) }
                showDeleteDialog = false
                selectedTrainer = null
            },
            onDismiss = { showDeleteDialog = false; selectedTrainer = null }
        )
    }

    // Opsional: Dialog ganti role untuk trainer
    if (showRoleDialog && selectedTrainer != null) {
        RoleSelectionDialog(
            currentRole = selectedTrainer?.role ?: "Trainer",
            onRoleSelected = { newRole ->
                selectedTrainer?.let { viewModel.updateUserRole(it.userId, newRole) }
                showRoleDialog = false
                selectedTrainer = null
            },
            onDismiss = { showRoleDialog = false; selectedTrainer = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manajemen Trainer", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(trainers) { trainer ->
                // Menggunakan AdminUserCard yang ada di MemberListScreen (atau copy fungsinya ke sini)
                AdminUserCard(
                    user = trainer,
                    iconColor = Color(0xFFFF9800),
                    onDeleteClick = {
                        selectedTrainer = trainer
                        showDeleteDialog = true
                    },
                    onEditRoleClick = {
                        selectedTrainer = trainer
                        showRoleDialog = true
                    }
                )
            }
        }
    }
}

// Pastikan AdminUserCard tersedia. Jika belum, tambahkan di file ini atau file terpisah:
@Composable
fun AdminUserCard(
    name: String,
    email: String,
    role: String,
    iconColor: Color,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = iconColor.copy(alpha = 0.2f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = iconColor)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name.ifBlank { "No Name" }, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(text = email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = role, style = MaterialTheme.typography.labelSmall, color = iconColor)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}