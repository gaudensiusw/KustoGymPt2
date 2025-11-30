package com.example.projekuas.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.projekuas.data.AdminRepository
import com.example.projekuas.data.AppDatabase
import com.example.projekuas.data.AuthRepository
import com.example.projekuas.data.ChatRepository
import com.example.projekuas.data.ClassRepository
import com.example.projekuas.data.NotificationRepository
import com.example.projekuas.data.ProfileRepository
import com.example.projekuas.data.WorkoutDataRepository
import com.google.firebase.firestore.FirebaseFirestore
// Catatan: Anda tidak perlu mengimpor WorkoutRepository di sini, karena Anda tidak
// menggunakannya di semua ViewModel.

class HomeViewModelFactory(
    private val context: Context,
    private val authRepository: AuthRepository,
    private val workoutDataRepository: WorkoutDataRepository, // Repository Lama
    private val profileRepository: ProfileRepository,
    private val classRepository: ClassRepository,
    private val adminRepository: AdminRepository,
    private val chatRepository: ChatRepository, // <-- TAMBAHKAN INI
    private val notificationRepository: NotificationRepository = NotificationRepository(FirebaseFirestore.getInstance()) // Tambahkan default value biar ga error di tempat lain
) : ViewModelProvider.Factory {

    // --- INISIALISASI UNTUK WORKOUT TRACKER BARU ---
    // Karena kita akan menggunakan WorkoutDataRepository lama (yang sudah diinjeksikan),
    // bagian lazy initialization ini mungkin tidak diperlukan lagi
    private val appDatabase by lazy { AppDatabase.getDatabase(context) }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    // Repository baru yang menggabungkan Room (Offline) & Firestore (Online)
    // Hapus baris ini jika Anda tidak ingin menggunakan struktur WorkoutRepository baru
    // private val newWorkoutRepository by lazy {
    //     WorkoutRepository(appDatabase.workoutSessionDao(), firestore)
    // }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        // 1. Logika untuk DashboardViewModel
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            // FIX: Tambahkan workoutDataRepository yang dibutuhkan DashboardViewModel
            return DashboardViewModel(authRepository, workoutDataRepository, profileRepository) as T
        }

        // 2. Logika untuk HomeViewModel
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            // FIX: Tambahkan semua dependency yang dibutuhkan HomeViewModel
            return HomeViewModel(
                authRepository,
                workoutDataRepository,
                profileRepository,
                classRepository,
                adminRepository
            ) as T
        }

        // 3. Logika untuk ProfileViewModel
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(profileRepository, authRepository) as T
        }

        // 4. Logika untuk WorkoutLogViewModel (Fitur Lama: Input Set/Reps)
        if (modelClass.isAssignableFrom(WorkoutLogViewModel::class.java)) {
            return WorkoutLogViewModel(
                workoutDataRepository = workoutDataRepository,
                authRepository = authRepository
            ) as T
        }

        // 5. [PERBAIKAN UTAMA] Logika untuk WorkoutViewModel (Fitur Baru: Tracker Offline/Online)
        // KODE LAMA: return WorkoutViewModel(newWorkoutRepository) as T
        // ERROR: Tipe salah (WorkoutRepository vs WorkoutDataRepository) dan AuthRepository hilang.
        if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
            return WorkoutViewModel(
                // Menggunakan dependency yang sudah diinjeksi di Factory
                workoutRepository = workoutDataRepository,
                authRepository = authRepository // <-- HARUS ADA AGAR TIDAK ERROR
            ) as T
        }

        // 6. Logika ClassBooking
        if (modelClass.isAssignableFrom(ClassBookingViewModel::class.java)) {
            return ClassBookingViewModel(
                classRepository = classRepository,
                authRepository = authRepository
            ) as T
        }

        // 7. Logika Admin
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            return AdminViewModel(
                adminRepository = adminRepository
            ) as T
        }

        // 8. Logika Trainer
        if (modelClass.isAssignableFrom(TrainerViewModel::class.java)) {
            return TrainerViewModel(
                classRepository = classRepository,
                authRepository = authRepository,
                profileRepository = profileRepository
            ) as T
        }

        // 9. Logika ClassForm
        if (modelClass.isAssignableFrom(ClassFormViewModel::class.java)) {
            return ClassFormViewModel(
                classRepository,
                authRepository,
                profileRepository
            ) as T
        }

        // 10. Logika Membership (Tambahkan jika diperlukan, tapi belum didefinisikan)
        if (modelClass.isAssignableFrom(MembershipViewModel::class.java)) {
            return MembershipViewModel(authRepository, profileRepository) as T
        }
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(chatRepository, authRepository, notificationRepository) as T
        }
        else if (modelClass.isAssignableFrom(TrainerListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrainerListViewModel(profileRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}