package com.example.projekuas.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.projekuas.ui.theme.GymOrange

@Composable
fun RatingDialog(
    trainerName: String,
    initialRating: Int = 0,
    initialReview: String = "",
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var rating by remember { mutableIntStateOf(initialRating) }
    var review by remember { mutableStateOf(initialReview) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rate $trainerName") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("How was the class?", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))

                // Bintang 1-5
                Row {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "$i Star",
                            tint = GymOrange,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { rating = i }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = review,
                    onValueChange = { review = it },
                    label = { Text("Leave a review (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(rating, review) },
                enabled = rating > 0,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}