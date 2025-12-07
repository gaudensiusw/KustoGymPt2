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
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projekuas.data.Booking
import com.example.projekuas.viewmodel.HomeViewModelFactory
import com.example.projekuas.viewmodel.TrainerViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerReviewsScreen(
    factory: HomeViewModelFactory,
    onNavigateBack: () -> Unit
) {
    val viewModel: TrainerViewModel = viewModel(factory = factory)
    // Gunakan state flow
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color.White, // Cleaner look
        topBar = {
            TopAppBar(
                title = { Text("My Reviews", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Header Stats
            ReviewSummaryCard(
                rating = state.rating,
                count = state.ratingCount,
                modifier = Modifier.padding(bottom = 24.dp, top = 8.dp)
            )

            Text(
                "Recent Feedback",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (state.reviews.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No reviews yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (state.isLoading) {
                            Spacer(Modifier.height(8.dp))
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(state.reviews) { review ->
                        ReviewItemCard(review)
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewSummaryCard(rating: Double, count: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Overall Rating", fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.7f))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        String.format(Locale.getDefault(), "%.1f", rating),
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold, // Extra Bold
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("/ 5.0", fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.7f), modifier = Modifier.padding(bottom = 8.dp))
                }
                StarRatingBar(rating = rating.toInt(), starSize = 20.dp, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                 Text(
                    "$count",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text("Total Reviews", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.7f))
            }
        }
    }
}

@Composable
fun ReviewItemCard(review: Booking) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Initial Avatar
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            review.userName.take(1).uppercase().ifBlank { "M" },
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(Modifier.width(12.dp))
                
                Column {
                    Text(
                        review.userName.ifBlank { "Gym Member" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(review.bookingTimeMillis)),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.weight(1f))
                StarRatingBar(rating = review.rating, starSize = 14.dp, tint = Color(0xFFFFC107))
            }
            
            if (review.review.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    review.review,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun StarRatingBar(rating: Int, starSize: androidx.compose.ui.unit.Dp, tint: Color) {
    Row {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = if (i <= rating) tint else Color.Gray.copy(alpha = 0.3f),
                modifier = Modifier.size(starSize)
            )
        }
    }
}