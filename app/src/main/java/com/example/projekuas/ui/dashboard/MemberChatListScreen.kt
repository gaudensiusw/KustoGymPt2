package com.example.projekuas.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberChatListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    // ID Trainer Nelson (Sesuai data Anda sebelumnya atau hardcode untuk testing)
    // Anda bisa mengganti ini dengan mengambil list user where role = 'Trainer' dari Firestore nanti
    val trainerId = "s6qvrvkfaIWUnrZ5foZ6QptUQ2k1" // ID Nelson (Contoh dari log sebelumnya/gambar)
    val trainerName = "Nelson (Trainer)"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat with Trainer") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable { onNavigateToChat(trainerId) },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar Placeholder
                        Box(
                            modifier = Modifier.size(50.dp).background(Color(0xFF1976D2), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("N", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(trainerName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Available for consultation", fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}