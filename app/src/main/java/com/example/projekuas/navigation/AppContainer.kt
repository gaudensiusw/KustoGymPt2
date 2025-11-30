package com.example.projekuas.navigation

import android.content.Context
import com.example.projekuas.data.AdminRepositoryImpl
import com.example.projekuas.data.AuthRepositoryImpl
import com.example.projekuas.data.ChatRepository
import com.example.projekuas.data.ClassRepositoryImpl
import com.example.projekuas.data.ProfileRepositoryImpl
import com.example.projekuas.data.WorkoutDataRepositoryImpl
import com.example.projekuas.viewmodel.AuthViewModelFactory
import com.example.projekuas.viewmodel.HomeViewModelFactory
import com.example.projekuas.viewmodel.ProfileViewModelFactory
import com.example.projekuas.viewmodel.WorkoutLogViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

// Container untuk menyimpan semua dependency aplikasi
class AppContainer(context: Context) {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseFirestore = FirebaseFirestore.getInstance()
    private val firebaseStorage = FirebaseStorage.getInstance()

    // Repositories
    val authRepository = AuthRepositoryImpl(firebaseAuth, firebaseFirestore)
    val profileRepository =
        ProfileRepositoryImpl(context, firebaseAuth, firebaseFirestore, firebaseStorage)
    val workoutDataRepository = WorkoutDataRepositoryImpl(firebaseFirestore)
    val classRepository = ClassRepositoryImpl(firebaseFirestore, context)
    val adminRepository = AdminRepositoryImpl(firebaseFirestore)
    val chatRepository = ChatRepository(firebaseFirestore)

    // ViewModel Factories
    val authViewModelFactory =
        AuthViewModelFactory(authRepository, profileRepository, workoutDataRepository)
    val homeViewModelFactory = HomeViewModelFactory(
        context, authRepository, workoutDataRepository, profileRepository,
        classRepository, adminRepository, chatRepository
    )
    val profileViewModelFactory = ProfileViewModelFactory(profileRepository, authRepository)
    val workoutLogViewModelFactory =
        WorkoutLogViewModelFactory(workoutDataRepository, authRepository)
}