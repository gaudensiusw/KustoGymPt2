package com.example.projekuas.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
    onLoginSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var passwordVisibility by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = state.isLoginSuccessful) {
        if (state.isLoginSuccessful) {
            onLoginSuccess()
        }
    }

    // Menggunakan Box untuk menumpuk elemen (background, shape, dan konten)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF5A5A5A)) // Warna latar belakang utama abu-abu gelap
    ) {
        // Shape abu-abu melengkung di bagian bawah
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f) // Tinggi shape bawah ~70% dari layar
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 60.dp, topEnd = 60.dp)) // Bentuk melengkung di atas
                .background(Color(0xFF8A8A8A)) // Warna abu-abu yang lebih terang untuk shape bawah
        )

        // Konten utama layar Login (Menggunakan Column non-scrollable)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top // Elemen dimulai dari atas
        ) {
            // Logo (Ukuran Gede)
            Image(
                painter = painterResource(id = R.drawable.logo_kusto_gym), // Pastikan nama file dan path benar
                contentDescription = "Kusto Gym Logo",
                modifier = Modifier
                    .size(250.dp) // FIX: Ukuran logo besar (250dp)
                    .padding(top = 40.dp, bottom = 20.dp),
                contentScale = ContentScale.Fit
            )

            // Teks "Login"
            Text(
                text = "Login",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = Color.White
                ),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // FIX PENTING: SPACER ELASTIS
            // Spacer ini mengambil semua ruang yang tersisa, mendorong elemen di bawahnya
            // (input fields, button) ke bagian bawah layar.
            Spacer(modifier = Modifier.weight(1f))

            // --- BAGIAN INPUT DAN TOMBOL (DIDORONG KE BAWAH) ---

            // Username Field
            OutlinedTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Username", color = Color.White) },
                placeholder = { Text("Masukkan username", color = Color.LightGray) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next // Tombol keyboard jadi "Next" ➡️
                ),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.LightGray,
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("Password", color = Color.White) },
                placeholder = { Text("Masukkan password", color = Color.LightGray) },
                singleLine = true,
                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done // Tombol keyboard jadi "Centang/Done" ✅
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        // Ketika user tekan Done di keyboard, langsung jalankan fungsi Login
                        if (!state.isLoading) viewModel.login()
                    }
                ),
                trailingIcon = {
                    val image = if (passwordVisibility)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    IconButton(onClick = {
                        passwordVisibility = !passwordVisibility
                    }) {
                        Icon(imageVector = image, "Toggle password visibility", tint = Color.White)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.LightGray,
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Login Button
            Button(
                onClick = viewModel::login,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(8.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF333333),
                    contentColor = Color.White
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Login",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            // Tampilkan error jika ada
            state.error?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            // TextButton untuk SignUp (jika belum punya akun)
            TextButton(
                onClick = onNavigateToSignUp,
                modifier = Modifier.padding(top = 16.dp, bottom = 40.dp) // Padding bawah untuk tombol
            ) {
                Text(
                    text = "Belum punya akun? Daftar di sini.",
                    color = Color.White
                )
            }
        }
    }
}