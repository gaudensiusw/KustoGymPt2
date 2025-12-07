package com.example.projekuas.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.projekuas.data.GymClass
import com.example.projekuas.ui.components.ConfirmationDialog
import com.example.projekuas.viewmodel.AdminViewModel

// Use Report Colors for consistency or Admin specific green
val AdminHeaderGreen = Color(0xFF16A34A) 
val AdminHeaderGreenDark = Color(0xFF166534)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminClassListScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToForm: (String?) -> Unit
) {
    val classes by viewModel.classList.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedClass by remember { mutableStateOf<GymClass?>(null) }
    
    if (showDeleteDialog && selectedClass != null) {
        ConfirmationDialog(
            title = "Delete Class?",
            message = "Class '${selectedClass?.name}' will be permanently deleted.",
            onConfirm = {
                selectedClass?.let { viewModel.deleteClass(it.classId) }
                showDeleteDialog = false
                selectedClass = null
            },
            onDismiss = { showDeleteDialog = false; selectedClass = null }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
             Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(AdminHeaderGreen, AdminHeaderGreenDark)
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text("Manage Classes", color = Color.White, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // "Naikkan tombol tambah kelas" - Placing it as the first item or Fixed at top?
            // "Naikkan" implies explicit visibility at top.
            item {
                Button(
                    onClick = { onNavigateToForm(null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AdminHeaderGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Add New Class", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            items(classes) { gymClass ->
                AdminClassCard(
                    gymClass = gymClass,
                    onDeleteClick = {
                        selectedClass = gymClass
                        showDeleteDialog = true
                    },
                    onEditClick = {
                        onNavigateToForm(gymClass.classId)
                    }
                )
            }
        }
    }
}

@Composable
fun AdminClassCard(
    gymClass: GymClass, 
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Box(modifier = Modifier.height(160.dp).fillMaxWidth().background(Color.LightGray)) {
                if (gymClass.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = gymClass.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Image, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    }
                }
                
                // Dim overlay for better button visibility if needed, or just specific buttons
                Box(Modifier.fillMaxSize().background(Color.Black.copy(0.1f)))

                // Status Chip (Upcoming/Finished)
                val isPast = gymClass.startTimeMillis < System.currentTimeMillis()
                Surface(
                    modifier = Modifier.padding(12.dp).align(Alignment.TopStart),
                    color = if(isPast) Color.DarkGray.copy(0.8f) else AdminHeaderGreen.copy(0.9f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if(isPast) "Finished" else "Upcoming",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                // Action Buttons (Edit & Delete)
                Row(
                   modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                   horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Edit Button
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.background(Color.White, CircleShape).size(36.dp)
                    ) {
                        Icon(Icons.Default.Edit, "Edit", tint = Color(0xFF1976D2), modifier = Modifier.size(20.dp))
                    }
                    
                    // Delete Button
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.background(Color.White, CircleShape).size(36.dp)
                    ) {
                        Icon(Icons.Default.Delete, "Delete", tint = Color.Red, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(gymClass.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(gymClass.trainerName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(4.dp))
                        val dateStr = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(gymClass.startTimeMillis))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(dateStr, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Capacity Badge
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${gymClass.currentBookings}/${gymClass.capacity}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (gymClass.currentBookings >= gymClass.capacity) Color.Red else AdminHeaderGreen
                        )
                         Text("Registered", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }
        }
    }
}