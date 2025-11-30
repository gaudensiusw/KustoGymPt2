package com.example.projekuas.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projekuas.viewmodel.HomeViewModelFactory
import com.example.projekuas.viewmodel.TrainerViewModel
import java.text.SimpleDateFormat
import java.util.*

// Model data sementara untuk UI (seharusnya dari Booking/Review data)
data class ReviewItem(
    val memberName: String,
    val rating: Int,
    val comment: String,
    val date: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerReviewsScreen(
    factory: HomeViewModelFactory, // Gunakan factory yang ada
    onNavigateBack: () -> Unit
) {
    // Kita bisa reuse TrainerViewModel atau buat baru.
    // Untuk efisiensi, asumsikan TrainerViewModel punya fungsi getReviews()
    val viewModel: TrainerViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsState()

    // TODO: Anda perlu menambahkan 'reviews' ke dalam TrainerUiState
    // val reviews = state.reviews

    // DUMMY DATA (Sampai Anda update ViewModel & Repo untuk fetch 'bookings' where trainerId == me & rating > 0)
    val dummyReviews = listOf(
        ReviewItem("Eric Kustanto", 5, "Kelasnya seru banget! Penjelasannya jelas.", System.currentTimeMillis()),
        ReviewItem("Budi Santoso", 4, "Lumayan capek, tapi worth it.", System.currentTimeMillis() - 86400000)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Student Reviews", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Summary Rating
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Average Rating", fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                "4.8", // Ambil dari state.performanceRate atau field baru
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Icon(
                            Icons.Default.Star,
                            null,
                            tint = Color(0xFFFFC107), // Emas
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            item {
                Text("Recent Feedback", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(vertical = 8.dp))
            }

            items(dummyReviews) { review ->
                ReviewCard(review)
            }
        }
    }
}

@Composable
fun ReviewCard(review: ReviewItem) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar Inisial
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(review.memberName.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(review.memberName, fontWeight = FontWeight.Bold)
                    Text(dateFormat.format(Date(review.date)), fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(Modifier.weight(1f))
                // Bintang Kecil
                Row {
                    Text(review.rating.toString(), fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(review.comment, style = MaterialTheme.typography.bodyMedium)
        }
    }
}