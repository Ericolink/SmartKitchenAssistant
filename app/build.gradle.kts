plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.smartkitchenassistant"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.smartkitchenassistant"
        minSdk = 24
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
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.compose.foundation.layout)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // SplashScreen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Navigation Compose
    implementation(libs.androidx.navigation.compose)

    // Firebase BOM
    implementation(platform(libs.firebase.bom))

    // Firebase Auth
    implementation(libs.firebase.auth)

    // Opcional: Firebase Performance Monitoring
    implementation("com.google.firebase:firebase-perf-ktx:20.5.0")

    //API de recetas
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("io.coil-kt:coil-compose:2.7.0")

    // All:
    implementation("com.cloudinary:cloudinary-android:3.1.2")
// Download + Preprocess:
    implementation("com.cloudinary:cloudinary-android-download:3.1.2")
    implementation("com.cloudinary:cloudinary-android-preprocess:3.1.2")

    //Tensor Flow Lite
    implementation("org.tensorflow:tensorflow-lite:+")

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
