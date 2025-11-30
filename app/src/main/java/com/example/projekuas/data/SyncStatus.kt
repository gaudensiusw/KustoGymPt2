package com.example.projekuas.data

enum class SyncStatus(val code: Int) {
    PENDING(0), // Menunggu sinkronisasi
    SUCCESS(1), // Berhasil sinkron (Menggantikan SYNCED agar error hilang)
    FAILED(2);  // Gagal sinkron (Opsional, untuk error handling)

    companion object {
        fun fromCode(code: Int): SyncStatus {
            return entries.find { it.code == code } ?: PENDING
        }
    }
}