plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.campusrelay"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.campusrelay"
        minSdk = 37
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.firebase.bom)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.messaging)
    implementation(libs.material)

    // Fragments + Navigation
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Lifecycle / ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Networking (REQ-API-1, REQ-API-2)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // Local persistence / offline cache (REQ-OFF-1, REQ-OFF-2)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Offline sync engine (REQ-OFF-2)
    implementation(libs.androidx.work.runtime.ktx)

    // Settings / session persistence (REQ-SET-1, REQ-SET-2)
    implementation(libs.androidx.datastore.preferences)

    // Biometric authentication (REQ-BIO-1, REQ-BIO-2)
    implementation(libs.androidx.biometric)

    // Lists
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.swiperefreshlayout)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.android)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)


}
