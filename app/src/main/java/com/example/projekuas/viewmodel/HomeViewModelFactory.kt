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

class HomeViewModelFactory(
    private val context: Context,
    private val authRepository: AuthRepository,
    private val workoutDataRepository: WorkoutDataRepository,
    private val profileRepository: ProfileRepository,
    private val classRepository: ClassRepository,
    private val adminRepository: AdminRepository,
    private val chatRepository: ChatRepository,
    private val notificationRepository: NotificationRepository = NotificationRepository(FirebaseFirestore.getInstance())
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        // 1. Logika untuk DashboardViewModel
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(authRepository, workoutDataRepository, profileRepository) as T
        }

        // 2. Logika untuk HomeViewModel
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
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

        // 4. Logika untuk WorkoutLogViewModel
        if (modelClass.isAssignableFrom(WorkoutLogViewModel::class.java)) {
            return WorkoutLogViewModel(
                workoutDataRepository = workoutDataRepository,
                authRepository = authRepository
            ) as T
        }

        // 5. Logika untuk WorkoutViewModel
        if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
            return WorkoutViewModel(
                workoutRepository = workoutDataRepository,
                authRepository = authRepository,
                profileRepository = profileRepository // <--- TAMBAHKAN BARIS INI
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

        // 10. Logika Membership
        if (modelClass.isAssignableFrom(MembershipViewModel::class.java)) {
            return MembershipViewModel(authRepository, profileRepository) as T
        }

        // 11. Logika Chat
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(chatRepository, authRepository, notificationRepository) as T
        }

        // 12. Logika Trainer List
        if (modelClass.isAssignableFrom(TrainerListViewModel::class.java)) {
            return TrainerListViewModel(profileRepository) as T
        }

        // [PERBAIKAN] 13. Logika SignUpViewModel (TAMBAHAN PENTING)
        if (modelClass.isAssignableFrom(SignUpViewModel::class.java)) {
            return SignUpViewModel(authRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}