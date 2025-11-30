package com.example.projekuas.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projekuas.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    otherUserId: String, // ID Lawan Bicara
    onNavigateBack: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    var text by remember { mutableStateOf("") }
    val currentUserId = viewModel.currentUserId

    // Load pesan saat layar dibuka
    LaunchedEffect(otherUserId) {
        viewModel.loadMessages(otherUserId)
    }

    // GUNAKAN COLUMN SEBAGAI ROOT (Agar aman dari nested scaffold issue)
    Column(
        modifier = Modifier
            .fillMaxSize()
            // FIX: Background mengikuti tema (Hitam di Dark, Putih/Abu di Light)
            .background(MaterialTheme.colorScheme.background)
            // Tambahkan systemBarsPadding agar tidak tertutup status bar
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        // --- 1. HEADER (Custom Top Bar) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // FIX: Background Header menggunakan Primary Color (Ungu)
                .background(MaterialTheme.colorScheme.primary)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                // Icon di atas primary color selalu onPrimary (Putih)
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Chat Room",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                // Opsional: Tampilkan ID
                Text(
                    text = "ID: ${otherUserId.take(6)}...",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }

        // --- 2. LIST PESAN (Isi Tengah) ---
        Box(
            modifier = Modifier
                .weight(1f)
                // FIX: Background area chat mengikuti tema
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (messages.isEmpty()) {
                // Empty State
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada pesan.\nMulai obrolan sekarang!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant, // Warna teks abu-abu yang sesuai tema
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    reverseLayout = true,
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(messages.reversed()) { msg ->
                        val isMe = msg.senderId == currentUserId
                        ChatBubble(isMe = isMe, text = msg.text)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // --- 3. INPUT FIELD (Bawah) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // FIX: Background area input (Surface color)
                .background(MaterialTheme.colorScheme.surface)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ketik pesan...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                shape = RoundedCornerShape(24.dp),
                // FIX: Warna text field mengikuti tema
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp))

            // Tombol Kirim
            IconButton(
                onClick = {
                    if (text.isNotBlank()) {
                        viewModel.sendMessage(otherUserId, text)
                        text = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun ChatBubble(isMe: Boolean, text: String) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            // FIX:
            // Jika saya (isMe) -> Warna Primary (Ungu)
            // Jika orang lain -> Warna SurfaceVariant (Abu-abu terang/gelap sesuai mode)
            color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 0.dp,
                bottomEnd = if (isMe) 0.dp else 16.dp
            ),
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(12.dp),
                // FIX: Warna teks menyesuaikan background bubble
                color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 15.sp
            )
        }
    }
}