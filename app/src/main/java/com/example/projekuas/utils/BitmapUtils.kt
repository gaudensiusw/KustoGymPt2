package com.example.projekuas.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

// Periksa kembali logika di utils/BitmapUtils.kt
@Composable
fun rememberBitmapFromBase64(base64String: String?): ImageBitmap? {
    return remember(base64String) {
        if (base64String.isNullOrEmpty()) return@remember null
        try {
            // Pastikan ini terimport: android.util.Base64
            // Hapus header "data:image/jpeg;base64," atau sejenisnya
            val cleanBase64 = base64String.substringAfter("base64,")

            val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)

            // Pastikan ini terimport: android.graphics.BitmapFactory
            // Pastikan ini terimport: androidx.compose.ui.graphics.asImageBitmap
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)?.asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}