plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)

    // Add the Google Services plugin
    id("com.google.gms.google-services")
}


android {
    namespace = "com.muriithi.dekutcallforhelp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.muriithi.dekutcallforhelp"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Import the Firebase Libraries
    implementation(platform(libs.firebase.bom)) // Import the BoM
    implementation(libs.firebase.analytics) // Import the Firebase SDK for Google Analytics
    implementation (libs.firebase.messaging) // Import the Firebase SDK for Cloud Messaging
    implementation(libs.firebase.database.ktx) // Import the Firebase SDK for Realtime Database
    implementation(libs.firebase.auth.ktx) // Import the Firebase SDK for Authentication
    implementation(libs.firebase.storage.ktx) // Import the Firebase SDK for Cloud Storage
    implementation(libs.firebase.firestore.ktx) // Import the Firebase SDK for Cloud Firestore
    implementation(libs.firebase.messaging.ktx) // Import the Firebase SDK for Cloud Messaging

    // Add Material 3 support and other dependencies
    implementation(libs.material.v150alpha01)
    implementation(libs.picasso)

    // Add the Glide library
    implementation(libs.glide)


}