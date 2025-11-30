package com.example.projekuas.ui.signup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projekuas.viewmodel.SignUpViewModel

@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel = viewModel(),
    // Callback untuk kembali ke Login (jika user berubah pikiran)
    onNavigateBackToLogin: () -> Unit,
    // Callback setelah registrasi sukses (biasanya menuju Home/Profile Setup)
    onRegistrationSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    // Efek Samping: Cek apakah registrasi sukses, lalu navigasi
    LaunchedEffect(key1 = state.isRegistrationSuccessful) {
        if (state.isRegistrationSuccessful) {
            onRegistrationSuccess()
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Membuat konten bisa di-scroll
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "DAFTAR AKUN BARU",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // --- BAGIAN INPUT DATA (HANYA CONTOH AWAL) ---

            // Input Nama
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Nama Lengkap") },
                isError = state.error != null,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            // Input Email
            OutlinedTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Email") },
                isError = state.error != null,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            // Input Password
            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("Password (Min 6 Karakter)") },
                visualTransformation = PasswordVisualTransformation(),
                isError = state.error != null,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )

            // Anda harus mengulang pola ini untuk input:
            // - Username
            // - Tinggi (Height)
            // - Berat (Weight)
            // - Tanggal Lahir (DOB - gunakan Date Picker)
            // - Jenis Kelamin (Gender - gunakan Dropdown/Radio Button)
            // - Telepon (Phone)
            // - Alamat (Address)

            // --- BAGIAN TOMBOL DAN ERROR ---

            // Tombol Registrasi
            Button(
                onClick = viewModel::register,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp).padding(top = 16.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("DAFTAR SEKARANG")
                }
            }

            // Tampilkan Pesan Error
            state.error?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            // Tombol Kembali ke Login
            TextButton(onClick = onNavigateBackToLogin) {
                Text("Sudah punya akun? Masuk di sini.")
            }
        }
    }
}