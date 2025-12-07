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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
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
    var searchQuery by remember { mutableStateOf("") } // Search State

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRoleDialog by remember { mutableStateOf(false) }
    var selectedTrainer by remember { mutableStateOf<UserProfile?>(null) }

    // Filter Logic
    val filteredTrainers = trainers.filter { 
        it.name.contains(searchQuery, ignoreCase = true) || it.email.contains(searchQuery, ignoreCase = true)
    }

    // Dialog Konfirmasi Hapus
    if (showDeleteDialog && selectedTrainer != null) {
        ConfirmationDialog(
            title = "Delete Trainer?",
            message = "Are you sure you want to delete ${selectedTrainer?.name}? Access will be revoked.",
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF16A34A), Color(0xFF166534))
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text("Manage Trainers", color = Color.White, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search by name or email") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1976D2), // Trainer Blue
                    cursorColor = Color(0xFF1976D2)
                )
            )

            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(filteredTrainers) { trainer ->
                    AdminUserCard(
                        user = trainer,
                        iconColor = Color(0xFF1976D2), // Keep Blue for Trainers distinguishing
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
}

// Reuse AdminUserCard from AdminMemberListScreen (or imported if shared)
// If AdminUserCard is not shared, we need to ensure it's available or define a similar one here.
// Assuming we are updating the file where AdminTrainerCard used to be, we can just replace it or ensure consistency.
// Since we used AdminUserCard in AdminMemberListScreen, let's assume we want consistency.
// However, the original code had AdminUserCard appearing in AdminMemberListScreen.
// If they are in the same package (com.example.projekuas.ui.dashboard), AdminUserCard from MemberList IS visible here IF it is top-level. 
// It was top-level in MemberList.
// So we can just call AdminUserCard. But wait, I should verify if it's visible. 
// If it's in the same package, yes.
