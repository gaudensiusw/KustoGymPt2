package com.example.projekuas.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.projekuas.data.GymClass
import com.example.projekuas.ui.components.ConfirmationDialog
import com.example.projekuas.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminClassListScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit
) {
    val classes by viewModel.classList.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedClass by remember { mutableStateOf<GymClass?>(null) }

    if (showDeleteDialog && selectedClass != null) {
        ConfirmationDialog(
            title = "Hapus Kelas?",
            message = "Kelas '${selectedClass?.name}' akan dihapus permanen.",
            onConfirm = {
                selectedClass?.let { viewModel.deleteClass(it.classId) }
                showDeleteDialog = false
                selectedClass = null
            },
            onDismiss = { showDeleteDialog = false; selectedClass = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manajemen Kelas", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = {
                    // Tombol Tambah (Logic navigasi bisa ditambahkan nanti)
                    IconButton(onClick = { /* TODO: Navigasi ke Form Tambah */ }) {
                        Icon(Icons.Default.Add, "Add Class")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(classes) { gymClass ->
                AdminClassCard(
                    gymClass = gymClass,
                    onDeleteClick = {
                        selectedClass = gymClass
                        showDeleteDialog = true
                    }
                )
            }
        }
    }
}

@Composable
fun AdminClassCard(gymClass: GymClass, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Box(modifier = Modifier.height(150.dp).fillMaxWidth().background(Color.LightGray)) {
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
                // Tombol Hapus di pojok kanan atas gambar
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(Color.White.copy(0.7f), androidx.compose.foundation.shape.CircleShape)
                ) {
                    Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
                }
            }

            // Info Kelas (Sama seperti sebelumnya)
            Column(modifier = Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(gymClass.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Cap: ${gymClass.currentBookings}/${gymClass.capacity}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(gymClass.trainerName, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}