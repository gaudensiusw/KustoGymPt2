package com.example.projekuas.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projekuas.data.UserProfile
import com.example.projekuas.ui.components.ConfirmationDialog
import com.example.projekuas.ui.components.RoleSelectionDialog
import com.example.projekuas.viewmodel.AdminViewModel

val AdminGreen = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDirectoryScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit
) {
    val members by viewModel.memberList.collectAsState()
    val trainers by viewModel.trainerList.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRoleDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<UserProfile?>(null) }

    // --- DIALOGS (Warna Dialog otomatis mengikuti tema Material3) ---
    if (showDeleteDialog && selectedUser != null) {
        ConfirmationDialog(
            title = "Hapus User?",
            message = "Apakah Anda yakin ingin menghapus ${selectedUser?.name}? Data tidak dapat dikembalikan.",
            onConfirm = {
                selectedUser?.let { viewModel.deleteUser(it.userId) }
                showDeleteDialog = false; selectedUser = null
            },
            onDismiss = { showDeleteDialog = false; selectedUser = null }
        )
    }

    if (showRoleDialog && selectedUser != null) {
        RoleSelectionDialog(
            currentRole = selectedUser?.role ?: "Member",
            onRoleSelected = { newRole ->
                selectedUser?.let { viewModel.updateUserRole(it.userId, newRole) }
                showRoleDialog = false; selectedUser = null
            },
            onDismiss = { showRoleDialog = false; selectedUser = null }
        )
    }

    Scaffold(
        // FIX: Background adaptif
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            // FIX: Background TopBar adaptif
            Column(Modifier.background(MaterialTheme.colorScheme.surface)) {
                // 1. Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        // FIX: Icon color
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Text(
                        text = "User Directory",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface, // FIX: Text color
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // 2. Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search name or email...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        // FIX: Colors adaptif
                        focusedBorderColor = AdminGreen,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true
                )

                // 3. Tab Row
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface, // FIX
                    contentColor = AdminGreen,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = AdminGreen
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Members (${members.size})") },
                        // FIX: Warna teks tab tidak aktif
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("Trainers (${trainers.size})") },
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    ) { padding ->
        val currentList = if (selectedTabIndex == 0) members else trainers
        val filteredList = currentList.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.email.contains(searchQuery, ignoreCase = true)
        }

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filteredList.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 50.dp), contentAlignment = Alignment.Center) {
                        Text("Tidak ada data ditemukan", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(filteredList) { user ->
                    AdminUserCardItem(
                        user = user,
                        isTrainer = (selectedTabIndex == 1),
                        onEditRole = { selectedUser = user; showRoleDialog = true },
                        onDelete = { selectedUser = user; showDeleteDialog = true }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminUserCardItem(
    user: UserProfile,
    isTrainer: Boolean,
    onEditRole: () -> Unit,
    onDelete: () -> Unit
) {
    val iconColor = if (isTrainer) Color(0xFFFF9800) else Color(0xFF2196F3)
    val iconVector = if (isTrainer) Icons.Default.FitnessCenter else Icons.Default.Person

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        // FIX: Warna Card adaptif
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Circle
            Surface(
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(iconVector, null, tint = iconColor)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info User
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name.ifBlank { "No Name" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface // FIX
                )
                Text(
                    text = user.email,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, // FIX
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier
                        .clickable { onEditRole() }
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user.role.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = iconColor,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Role",
                        tint = iconColor,
                        modifier = Modifier.size(12.dp).padding(start = 4.dp)
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f) // FIX: Error color
                )
            }
        }
    }
}