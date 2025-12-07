package com.example.projekuas.viewmodel

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekuas.data.AuthRepository
import com.example.projekuas.data.ProfileRepository
import com.example.projekuas.data.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class MembershipUiState(
    val userProfile: UserProfile = UserProfile(),
    val qrBitmap: Bitmap? = null,
    val isLoading: Boolean = false
)

class MembershipViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MembershipUiState())
    val uiState: StateFlow<MembershipUiState> = _uiState.asStateFlow()

    private val db = FirebaseFirestore.getInstance()

    // Simpan ID di awal, nullable
    private val currentUserIdString = authRepository.getCurrentUserId()

    init {
        fetchUserData()
    }

    private fun fetchUserData() {
        // FIX: Smart Cast variable local agar tidak error 'Type Mismatch'
        val uid = currentUserIdString ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // FIX: Sekarang .collect() akan berhasil karena Repository mengembalikan Flow
            profileRepository.getUserProfile(uid).collect { profile ->

                // Generate QR Code setiap ada perubahan data user (jika perlu)
                // Kita gunakan userId dari profile yang didapat, atau uid dari auth
                val qrCode = generateQrCode(profile.userId.ifBlank { uid })

                _uiState.value = _uiState.value.copy(
                    userProfile = profile,
                    qrBitmap = qrCode,
                    isLoading = false
                )
            }
        }
    }

    // Fungsi Update Membership
    fun updateMembership(planName: String) {
        val uid = currentUserIdString ?: return

        viewModelScope.launch {
            // Loading state sebentar (optimistic update UI akan ditangani Flow di atas)
            _uiState.value = _uiState.value.copy(isLoading = true)

            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 30)
            val expiryDate = calendar.timeInMillis

            val updates = mapOf(
                "membershipType" to planName,
                "membershipValidUntil" to expiryDate
            )

            // Update langsung ke Firestore
            // Tidak perlu update UI manual di sini, karena 'collect' di fetchUserData
            // akan otomatis mendeteksi perubahan di database dan mengupdate UI.
            
            // 1. Update Membership User
            db.collection("users").document(uid)
                .update(updates)
                .addOnSuccessListener {
                    // 2. [REVENUE LOGIC] Catat Transaksi Pembelian Membership
                    val price = when(planName.lowercase()) {
                        "elite" -> 999000.0
                        "premium" -> 599000.0
                        else -> 299000.0 // Basic
                    }
                    
                    val transactionData = hashMapOf(
                        "id" to db.collection("transactions").document().id,
                        "userId" to uid,
                        "userName" to (_uiState.value.userProfile.name),
                        "type" to "Membership",
                        "amount" to price,
                        "dateMillis" to System.currentTimeMillis(),
                        "description" to "Upgrade to $planName"
                    )
                    
                    db.collection("transactions").add(transactionData)
                }
                .addOnFailureListener {
                    // Handle error jika perlu, misal kembalikan loading ke false
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
        }
    }

    private fun generateQrCode(content: String): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }
}