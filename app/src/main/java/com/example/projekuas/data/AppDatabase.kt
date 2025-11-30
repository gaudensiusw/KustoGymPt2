package com.example.projekuas.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// PENTING:
// 1. Entities: Gabungkan entity lama (WorkoutLogEntity) dan baru (WorkoutSessionEntity)
// 2. Version: Dinaikkan jadi 2 karena ada perubahan struktur
@Database(
    entities = [
        WorkoutLogEntity::class,     // Entity lama (untuk log set/reps)
        WorkoutSessionEntity::class  // Entity baru (untuk tracker durasi/kalori)
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // DAO Lama
    abstract fun workoutLogDao(): WorkoutLogDao

    // DAO Baru (Wajib ada agar WorkoutRepository bisa mengaksesnya)
    abstract fun workoutSessionDao(): WorkoutSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gym_app_database"
                )
                    // PENTING: Tambahkan ini agar saat struktur DB berubah (versi naik),
                    // data lama dihapus & dibuat ulang. Ini mencegah crash "Migration not found".
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}