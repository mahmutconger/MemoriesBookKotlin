plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.anlarsinsoftware.memoriesbook"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.anlarsinsoftware.memoriesbook"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true

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
    // TEMEL ANDROID KÜTÜPHANELERİ
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.compose.material:material-icons-extended")
    
    // COMPOSE - Bill of Materials (BoM) ile
    // BoM, tüm compose kütüphanelerinin uyumlu sürümlerini otomatik yönetir.
    implementation(platform("androidx.compose:compose-bom:2024.05.00")) // en güncel Compose BoM
    implementation("androidx.compose.material3:material3") // Sürüm belirtme, BoM yönetir
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)


    // NAVİGASYON
    implementation("androidx.navigation:navigation-compose:2.9.2")

    // VIEWMODEL
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.2")

    // FIREBASE - Bill of Materials (BoM) ile
    // BoM, tüm firebase kütüphanelerinin uyumlu sürümlerini otomatik yönetir.
    implementation(platform("com.google.firebase:firebase-bom:34.0.0")) // Versiyonu kontrol et, en günceli kullan
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-functions")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.android.gms:play-services-auth:21.4.0")

    // COROUTINES
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")

    // COIL (Resim ve Video Yükleme)
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.coil-kt:coil-video:2.7.0")
    implementation("io.coil-kt:coil-gif:2.7.0")

    // DATASTORE
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    // MEDIA3 (Video Oynatıcı)
    implementation("androidx.media3:media3-exoplayer:1.7.1")
    implementation("androidx.media3:media3-ui:1.7.1")


    // TEST KÜTÜPHANELERİ
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}