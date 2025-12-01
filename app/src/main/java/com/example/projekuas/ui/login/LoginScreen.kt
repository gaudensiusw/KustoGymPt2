package com.example.projekuas.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projekuas.R
import com.example.projekuas.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onNavigateToSignUp: () -> Unit,
    onLoginSuccess: () -> Unit,
    onGoogleSignInClick: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    var passwordVisibility by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = state.isLoginSuccessful) {
        if (state.isLoginSuccessful) {
            onLoginSuccess()
        }
    }

    // --- STRUKTUR UI UTAMA ---
    // Gunakan Box untuk menumpuk elemen
    Box(
        modifier = Modifier.fillMaxSize()
        // CATATAN: .background(MaterialTheme.colorScheme.primary) DIHAPUS dari sini
    ) {
        // =====================================================================
        // LAYER 1: GAMBAR LATAR BELAKANG (Paling Bawah)
        // =====================================================================
        Image(
            // GANTI 'login_bg_placeholder' DENGAN NAMA FILE GAMBAR ANDA DI DRAWABLE
            painter = painterResource(id = R.drawable.image3),
            contentDescription = null, // Background dekoratif tidak butuh deskripsi
            contentScale = ContentScale.Crop, // Memenuhi layar, memotong jika perlu
            modifier = Modifier.fillMaxSize()
        )

        // =====================================================================
        // LAYER 2: OVERLAY WARNA (Opsional tapi Sangat Disarankan)
        // Memberi lapisan transparan warna primary (ungu) di atas gambar
        // agar teks putih tetap mudah dibaca.
        // =====================================================================
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Ubah nilai alpha (0.0f - 1.0f) untuk mengatur transparansi.
                // 0.7f berarti 70% warna ungu, 30% gambar terlihat.
                .background(Color.Gray.copy(alpha = 0.3f))
        )

        // =====================================================================
        // LAYER 3: KONTEN SCREEN (Header, Card, Footer)
        // =====================================================================

        // 1. HEADER (Logo & Teks Welcome) - Posisi Tengah Atas
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // LOGO APP
            Image(
                painter = painterResource(id = R.drawable.logo_kusto_gym),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(240.dp)
                    .padding(bottom = 8.dp),
                contentScale = ContentScale.Fit
            )

            // Teks Header
            Text(
                text = "Welcome Back!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    // Warna teks putih agar kontras dengan overlay ungu
                    color = MaterialTheme.colorScheme.onPrimary
                )
            )
        }

        // 2. KARTU LOGIN (Tengah Layar)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 140.dp)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // --- INPUTS & BUTTONS (Kode sama seperti sebelumnya) ---
                    LoginInputLabel(text = "Email")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = viewModel::onEmailChange,
                        placeholder = { Text("your.email@example.com", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)) },
                        leadingIcon = { Icon(Icons.Outlined.Email, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LoginInputLabel(text = "Password")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        placeholder = { Text("Enter your password", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)) },
                        leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        trailingIcon = {
                            val image = if (passwordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                                Icon(image, "Toggle visibility", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = viewModel::login,
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        } else {
                            Text(text = "Login", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    state.error?.let { errorMessage ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // 3. FOOTER (Sign Up Text) - Di bawah Kartu
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Don't have an account? ",
                    // Warna teks putih agar kontras dengan overlay ungu
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                Text(
                    text = "Sign Up",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onNavigateToSignUp() }
                )
            }
        }
    }
}

// Komponen Helper untuk Label Input (Tidak berubah)
@Composable
fun LoginInputLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    )
}