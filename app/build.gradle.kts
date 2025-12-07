plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.projekuas"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.projekuas"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Vico Charts
    implementation("com.patrykandpatrick.vico:compose-m3:1.12.0")
    implementation("com.patrykandpatrick.vico:core:1.12.0")

    implementation("com.google.firebase:firebase-messaging")
    implementation("androidx.compose.ui:ui-graphics")

    implementation("com.google.zxing:core:3.5.1")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("io.coil-kt.coil3:coil-compose:3.3.0")

    implementation("androidx.appcompat:appcompat:1.6.1")

    implementation("androidx.compose.material3:material3:1.4.0")    // implementation("androidx.compose.material3:material3-:1.4.0") // SALAH
    implementation("androidx.compose.material:material:1.9.5")
    // WAJIB: Dependency untuk Ikon Material Extended
    implementation("androidx.compose.material:material-icons-extended")
    // INI BENAR (Biarkan BoM 33.0.0 menentukan versinya)
    implementation("com.google.firebase:firebase-storage")
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.androidx.webkit)
    implementation(libs.play.services.games)


    // implementation(libs.firebase.database) // Duplikat dengan deklarasi di bawah, bisa dihapus jika libs.versions.toml sudah benar
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    // KRUSIAL: Panggil KSP di sini. Jika baris ini error, itu berarti KSP plugin belum di-apply
    ksp("androidx.room:room-compiler:$room_version")

    implementation(platform("com.google.firebase:firebase-bom:33.0.0")) // Cek versi terbaru

    // 2. Firebase Authentication (Untuk Login/Sign Up)
    implementation("com.google.firebase:firebase-auth-ktx")

    // 3. Cloud Firestore (Untuk menyimpan Log Latihan & Profil)
    implementation("com.google.firebase:firebase-firestore-ktx")

    // 4. Coroutine Support untuk Firebase (opsional, tapi sangat membantu)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
    // Baris berikut adalah duplikat dari deklarasi Room dan Firebase di atas. Sebaiknya dihapus untuk menghindari konflik.
    // implementation("androidx.room:room-runtime:2.6.1")
    // implementation(libs.firebase.firestore)
    // annotationProcessor("androidx.room:room-compiler:2.6.1") // annotationProcessor sudah tidak digunakan, pakai ksp
    // implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    // implementation(libs.androidx.room.ktx) // Duplikat dari deklarasi Room di atas
    implementation(libs.firebase.database) // Pindahkan ke sini untuk konsistensi jika menggunakan libs
    implementation(libs.firebase.firestore) // Pindahkan ke sini untuk konsistensi jika menggunakan libs
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

}