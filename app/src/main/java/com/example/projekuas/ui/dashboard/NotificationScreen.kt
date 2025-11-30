package com.example.projekuas.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projekuas.data.NotificationModel
import com.example.projekuas.data.NotificationRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(onNavigateBack: () -> Unit) {
    val repo = remember { NotificationRepository(Firebase.firestore) }
    val userId = Firebase.auth.currentUser?.uid ?: ""
    // Gunakan coroutine scope untuk aksi hapus
    val scope = rememberCoroutineScope()
    var notifications by remember { mutableStateOf<List<NotificationModel>>(emptyList()) }

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            repo.getNotifications(userId).collect { notifications = it }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    // TOMBOL CLEAR ALL
                    if (notifications.isNotEmpty()) {
                        TextButton(onClick = {
                            scope.launch {
                                repo.clearAllNotifications(userId)
                            }
                        }) {
                            Text("Clear All", color = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                // Teks mengikuti tema
                Text("Tidak ada notifikasi", color = MaterialTheme.colorScheme.onBackground)
            }
        } else {
            LazyColumn(Modifier.padding(padding)) {
                items(notifications) { notif ->
                    NotificationItem(notif)
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notif: NotificationModel) {
    val time = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(Date(notif.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        // FIX: Gunakan Surface (Putih di Light, Abu Gelap di Dark)
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(16.dp)) {
            // Icon menggunakan warna Primary
            Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column {
                // Teks Judul (onSurface)
                Text(
                    notif.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // Teks Pesan (onSurfaceVariant / agak pudar)
                Text(
                    notif.message,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                // Waktu
                Text(
                    time,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}