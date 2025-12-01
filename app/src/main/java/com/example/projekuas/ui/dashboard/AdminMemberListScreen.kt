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

// Warna Admin
val AdminGreen = Color(0xFF4CAF50)
val AdminLightGreen = Color(0xFFE8F5E9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMemberListScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit
) {
    // Ambil data langsung dari ViewModel (Sesuai kode AdminViewModel kamu)
    val members by viewModel.memberList.collectAsState()
    val trainers by viewModel.trainerList.collectAsState()

    // State Lokal
    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0 = Member, 1 = Trainer
    var searchQuery by remember { mutableStateOf("") }

    // State Dialog
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
        topBar = {
            Column(Modifier.background(Color.White)) {
                // 1. Top App Bar Custom
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
                        text = "Master Directory",
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

                // 3. Tab Row
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
        // Logic Filter Search
        val currentList = if (selectedTabIndex == 0) members else trainers
        val filteredList = currentList.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.email.contains(searchQuery, ignoreCase = true)
        }

        // KONTEN LIST
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF5F7FA)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredList) { user ->
                AdminDirectoryItem(
                    user = user,
                    isTrainerList = selectedTabIndex == 1,
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
fun AdminDirectoryItem(
    user: UserProfile,
    isTrainerList: Boolean,
    onEditRole: () -> Unit,
    onDelete: () -> Unit
) {
    val icon = if (isTrainerList) Icons.Default.FitnessCenter else Icons.Default.Person
    val iconBgColor = if (isTrainerList) Color(0xFFE3F2FD) else Color(0xFFF3E5F5) // Biru vs Ungu Muda
    val iconTint = if (isTrainerList) Color(0xFF1E88E5) else Color(0xFF9C27B0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint)
            }

            Spacer(Modifier.width(16.dp))

            // Info User
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name.ifBlank { "Unknown Name" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E1E1E)
                )
                Text(
                    text = user.email,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Tombol kecil "Ubah Role"
                Row(
                    modifier = Modifier
                        .clickable { onEditRole() }
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user.role.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = iconTint
                    )
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(12.dp).padding(start = 4.dp)
                    )
                }
            }

            // Delete Button
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE57373))
            }
        }
    }
}