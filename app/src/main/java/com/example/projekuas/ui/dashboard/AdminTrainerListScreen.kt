package com.example.projekuas.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit
) {
    val trainers by viewModel.trainerList.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRoleDialog by remember { mutableStateOf(false) }
    var selectedTrainer by remember { mutableStateOf<UserProfile?>(null) }

    // Dialog Konfirmasi Hapus
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

    // Dialog Ganti Role
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
                AdminUserCard(
                    user = trainer,
                    iconColor = Color(0xFFFF9800), // Warna Orange untuk Trainer
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

// --- PERBAIKAN: Definisi AdminUserCard disesuaikan ---
@Composable
fun AdminUserCard(
    user: UserProfile,      // Menerima object UserProfile, bukan string terpisah
    iconColor: Color,
    onDeleteClick: () -> Unit, // Nama parameter disesuaikan
    onEditRoleClick: () -> Unit // Parameter baru ditambahkan
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
            // Icon Avatar
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

            // Info User
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name.ifBlank { "No Name" },
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Tombol kecil untuk Edit Role (Klik pada teks role)
                Row(
                    modifier = Modifier
                        .clickable { onEditRoleClick() }
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user.role,
                        style = MaterialTheme.typography.labelSmall,
                        color = iconColor,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Role",
                        tint = iconColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Tombol Hapus
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}