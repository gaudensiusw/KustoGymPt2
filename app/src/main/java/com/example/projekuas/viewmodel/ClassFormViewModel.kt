package com.example.projekuas.viewmodel

import android.R
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekuas.data.ClassRepository
import com.example.projekuas.data.AuthRepository
import com.example.projekuas.data.GymClass
import com.example.projekuas.data.ProfileRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.text.SimpleDateFormat
import java.time.LocalDate

data class ClassFormState(
    val name: String = "",
    val description: String = "",
    val durationMinutes: String = "60",
    val capacity: String = "10",
    val selectedDateMillis: Long = System.currentTimeMillis(),
    val selectedTimeMillis: Long = 0L,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false,
    val dateDisplay: String = "",
    // Menyimpan ID kelas yang sedang di-edit (jika tidak null, ini mode EDIT)
    val classIdToEdit: String? = null,
    val currentImageUrl: String = "", // Untuk menyimpan string Base64 gambar
    val isReadOnly: Boolean = false
)

class ClassFormViewModel(
    private val classRepository: ClassRepository,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ClassFormState())
    val state: StateFlow<ClassFormState> = _state.asStateFlow()

    private val trainerId = authRepository.getCurrentUserId() ?: ""
    private var realTrainerName = ""

    // Handler Gambar Baru
    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) } // Tampilkan loading sebentar saat proses gambar
            val base64Image = classRepository.processClassImage(uri)
            if (base64Image != null) {
                _state.update { it.copy(currentImageUrl = base64Image, isSaving = false) }
            } else {
                _state.update { it.copy(error = "Gagal memproses gambar", isSaving = false) }
            }
        }
    }

    init {
        // Saat ViewModel dibuat, langsung ambil nama asli dari Firestore
        fetchTrainerProfile()
    }

    private fun fetchTrainerProfile() {
        viewModelScope.launch {
            if (trainerId.isNotEmpty()) {
                // PERBAIKAN: Menggunakan .first() karena getUserProfile mengembalikan Flow
                // first() mengambil nilai pertama yang tersedia dari Flow lalu berhenti
                try {
                    val userProfile = profileRepository.getUserProfile(trainerId).first()
                    // Pastikan userProfile tidak kosong (tergantung implementasi repository)
                    if (userProfile.userId.isNotEmpty()) {
                        realTrainerName = userProfile.name // Sekarang properti .name bisa diakses
                    }
                } catch (e: Exception) {
                    // Handle error jika perlu, atau biarkan default name
                }
            }
        }
    }

    // --- HANDLERS (Tidak Berubah) ---
    fun onNameChange(s: String) { _state.update { it.copy(name = s) } }
    fun onDescriptionChange(s: String) { _state.update { it.copy(description = s) } }
    fun onDurationChange(s: String) { _state.update { it.copy(durationMinutes = s.filter { it.isDigit() }) } }
    fun onCapacityChange(s: String) { _state.update { it.copy(capacity = s.filter { it.isDigit() }) } }

    // Handler Date Picker
    fun onDateSelected(millis: Long, dateString: String) {
        // Penting: Pastikan waktu (jam/menit) hari itu TIDAK direset oleh DatePicker
        // Kita hanya mengambil tanggalnya (year/month/day)
        val calendarDate = Calendar.getInstance().apply { timeInMillis = millis }
        val calendarTime = Calendar.getInstance().apply { timeInMillis = _state.value.selectedTimeMillis }

        calendarDate.set(Calendar.HOUR_OF_DAY, calendarTime.get(Calendar.HOUR_OF_DAY))
        calendarDate.set(Calendar.MINUTE, calendarTime.get(Calendar.MINUTE))
        calendarDate.set(Calendar.SECOND, 0)
        calendarDate.set(Calendar.MILLISECOND, 0)

        _state.update {
            it.copy(
                selectedDateMillis = millis,
                dateDisplay = dateString
            )
        }
    }

    // Handler Time Picker (menerima jam dan menit)
    fun onTimeSelected(hour: Int, minute: Int) {
        // Hanya menghitung milidetik dari awal hari (tanpa tanggal)
        val timeMillis = (hour * 3600L + minute * 60L) * 1000L
        _state.update { it.copy(selectedTimeMillis = timeMillis) }
    }

    // --- FUNGSI UNTUK EDIT ---

    /**
     * Memuat data GymClass lama ke dalam state form untuk mode Edit.
     */
    fun initializeFormForEdit(gymClass: GymClass) {
        // Konversi timestamp penuh ke Calendar untuk mendapatkan komponen tanggal dan waktu
        val fullCalendar = Calendar.getInstance(TimeZone.getDefault()).apply {
            timeInMillis = gymClass.startTimeMillis
        }

        // Dapatkan komponen tanggal (reset waktu ke 00:00)
        val dateCalendar = fullCalendar.clone() as Calendar
        dateCalendar.set(Calendar.HOUR_OF_DAY, 0)
        dateCalendar.set(Calendar.MINUTE, 0)
        dateCalendar.set(Calendar.SECOND, 0)
        dateCalendar.set(Calendar.MILLISECOND, 0)

        // Dapatkan komponen waktu (hanya jam, menit, detik)
        val timeMillisOnly = gymClass.startTimeMillis - dateCalendar.timeInMillis
        val dateString = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(Date(gymClass.startTimeMillis))

        _state.update {
            it.copy(
                classIdToEdit = gymClass.classId,
                name = gymClass.name,
                description = gymClass.description,
                durationMinutes = gymClass.durationMinutes.toString(),
                capacity = gymClass.capacity.toString(),
                selectedDateMillis = dateCalendar.timeInMillis,
                selectedTimeMillis = timeMillisOnly,
                dateDisplay = dateString,

                // LOAD GAMBAR
                currentImageUrl = gymClass.imageUrl,

                error = null,
                isSaved = false,
                // Check status. If not Upcoming, it's read-only.
                isReadOnly = gymClass.status != "Upcoming"
            )
        }
    }

    /**
     * Mengatur ulang semua state form ke nilai default.
     */
    fun resetForm() {
        _state.value = ClassFormState()
    }

    /**
     * Fungsi utama untuk menyimpan atau memperbarui kelas.
     */
    fun saveOrUpdateClass(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val currentState = _state.value

            // 1. Validasi
            if (currentState.name.isBlank() || currentState.durationMinutes.toIntOrNull() == null || trainerId.isBlank()) {
                _state.update { it.copy(error = "Nama, Durasi, dan ID Trainer wajib diisi.") }
                return@launch
            }

            // 2. Kalkulasi Final Timestamp (Gabungkan Tanggal dan Waktu)
            // Note: selectedDateMillis adalah 00:00:00 di tanggal yang dipilih.
            val totalTimeMillis = currentState.selectedDateMillis + currentState.selectedTimeMillis
            val finalTrainerName = if (realTrainerName.isNotBlank()) realTrainerName else "Trainer"

            val gymClass = GymClass(
                classId = currentState.classIdToEdit ?: UUID.randomUUID().toString(),
                name = currentState.name,
                description = currentState.description,
                trainerName = finalTrainerName, // SIMPAN NAMA ASLI DISINI
                trainerId = trainerId,          // SIMPAN ID DISINI
                durationMinutes = currentState.durationMinutes.toInt(),
                startTimeMillis = totalTimeMillis,
                capacity = currentState.capacity.toIntOrNull() ?: 10,
                currentBookings = 0,
                isAvailable = true,
                imageUrl = currentState.currentImageUrl
                )
            // 3. Simpan/Update ke Repository
            _state.update { it.copy(isSaving = true, error = null) }
            try {
                if (currentState.classIdToEdit != null) {
                    classRepository.updateClass(gymClass)
                } else {
                    classRepository.createClass(gymClass)
                }
                _state.update { it.copy(isSaving = false, isSaved = true) }
                onSuccess()
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = "Gagal: ${e.message}") }
            }
        }
    }

    fun loadClassData(classId: String) {
        // Cek agar tidak load berulang kali jika id sama
        if (_state.value.classIdToEdit == classId) return

        _state.update { it.copy(isSaving = true) } // Pakai loading indicator sebentar

        viewModelScope.launch {
            val gymClass = classRepository.getClassById(classId)
            if (gymClass != null) {
                // Panggil fungsi yang sudah Anda punya untuk mengisi state
                initializeFormForEdit(gymClass)
            } else {
                _state.update { it.copy(error = "Kelas tidak ditemukan") }
            }
            _state.update { it.copy(isSaving = false) }
        }
    }

    fun isDateValid(selectedDate: LocalDate): Boolean {
        val currentDate = LocalDate.now()
        // Tanggal yang valid adalah hari ini atau setelah hari ini
        return selectedDate >= currentDate
    }
}