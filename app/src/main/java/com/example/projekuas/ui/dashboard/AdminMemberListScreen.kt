package com.example.projekuas.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.projekuas.data.UserProfile
import com.example.projekuas.ui.components.ConfirmationDialog
import com.example.projekuas.ui.components.RoleSelectionDialog
import com.example.projekuas.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMemberListScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit
) {
    val members by viewModel.memberList.collectAsState()

    // State untuk Dialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRoleDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<UserProfile?>(null) }

    // Jika delete dialog aktif
    if (showDeleteDialog && selectedUser != null) {
        ConfirmationDialog(
            title = "Hapus Member?",
            message = "Apakah Anda yakin ingin menghapus ${selectedUser?.name}? Data tidak dapat dikembalikan.",
            onConfirm = {
                selectedUser?.let { viewModel.deleteUser(it.userId) }
                showDeleteDialog = false
                selectedUser = null
            },
            onDismiss = {
                showDeleteDialog = false
                selectedUser = null
            }
        )
    }

    // Jika role dialog aktif
    if (showRoleDialog && selectedUser != null) {
        RoleSelectionDialog(
            currentRole = selectedUser?.role ?: "Member",
            onRoleSelected = { newRole ->
                selectedUser?.let { viewModel.updateUserRole(it.userId, newRole) }
                showRoleDialog = false
                selectedUser = null
            },
            onDismiss = {
                showRoleDialog = false
                selectedUser = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manajemen Member", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(members) { member ->
                AdminUserCard(
                    user = member,
                    iconColor = Color(0xFF2196F3),
                    onDeleteClick = {
                        selectedUser = member
                        showDeleteDialog = true
                    },
                    onEditRoleClick = {
                        selectedUser = member
                        showRoleDialog = true
                    }
                )
            }
        }
    }
}

// Kartu User yang diperbarui dengan tombol Edit Role
@Composable
fun AdminUserCard(
    user: UserProfile,
    iconColor: Color,
    onDeleteClick: () -> Unit,
    onEditRoleClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.2f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = iconColor)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.name.ifBlank { "No Name" }, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(text = user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                // Klik Text Role untuk ubah role
                Row(modifier = Modifier.clickable { onEditRoleClick() }.padding(top = 4.dp)) {
                    Text(text = user.role, style = MaterialTheme.typography.labelSmall, color = iconColor, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp), tint = iconColor)
                }
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}