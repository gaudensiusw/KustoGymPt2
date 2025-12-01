package com.example.projekuas.ui.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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

// Warna Tema Admin
val AdminBg = Color(0xFFF5F7FA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDirectoryScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit
) {
    // Mengambil data Member dan Trainer dari ViewModel
    val members by viewModel.memberList.collectAsState()
    val trainers by viewModel.trainerList.collectAsState()

    // State untuk Tab (0 = Member, 1 = Trainer)
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // State untuk Pencarian
    var searchQuery by remember { mutableStateOf("") }

    // State untuk Dialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRoleDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<UserProfile?>(null) }

    // --- DIALOG LOGIC ---
    if (showDeleteDialog && selectedUser != null) {
        ConfirmationDialog(
            title = "Hapus User?",
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
        containerColor = AdminBg,
        topBar = {
            Column(Modifier.background(Color.White)) {
                // 1. Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = "User Directory",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E1E),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // 2. Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search name or email...") },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AdminGreen,
                        unfocusedBorderColor = Color.LightGray,
                        focusedContainerColor = Color(0xFFFAFAFA),
                        unfocusedContainerColor = Color(0xFFFAFAFA)
                    ),
                    singleLine = true
                )

                // 3. Tab Row (Members | Trainers)
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.White,
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
                        text = { Text("Members (${members.size})") }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("Trainers (${trainers.size})") }
                    )
                }
            }
        }
    ) { padding ->

        // Logic Filter List berdasarkan Tab & Search
        val currentList = if (selectedTabIndex == 0) members else trainers
        val filteredList = currentList.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.email.contains(searchQuery, ignoreCase = true)
        }

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredList) { user ->
                AdminUserCard(
                    user = user,
                    isTrainer = (selectedTabIndex == 1),
                    onEditRole = {
                        selectedUser = user
                        showRoleDialog = true
                    },
                    onDelete = {
                        selectedUser = user
                        showDeleteDialog = true
                    }
                )
            }
        }
    }
}

@Composable
fun AdminUserCard(
    user: UserProfile,
    isTrainer: Boolean,
    onEditRole: () -> Unit,
    onDelete: () -> Unit
) {
    // Warna Ikon berbeda untuk Member (Ungu/Biru) dan Trainer (Orange/Kuning)
    val iconColor = if (isTrainer) Color(0xFFFF9800) else Color(0xFF2196F3)
    val iconVector = if (isTrainer) Icons.Default.FitnessCenter else Icons.Default.Person

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF1E1E1E)
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Role Badge (Klik untuk edit role)
                Row(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onEditRole() }
                        .padding(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user.role.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = iconColor,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Role",
                        tint = iconColor,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            // Tombol Hapus
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}