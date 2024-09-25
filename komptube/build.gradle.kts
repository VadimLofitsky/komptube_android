plugins {
    id("com.android.application")// version "8.2.2" apply false
    id("org.jetbrains.kotlin.android")// version "1.9.22" apply false
}

android {
    namespace = "lofitsky.android.komptube"
    compileSdk = 34

    defaultConfig {
        applicationId = "lofitsky.android.komptube"
        minSdk = 33
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_10
        targetCompatibility = JavaVersion.VERSION_1_10
    }

    kotlinOptions {
        jvmTarget = "10"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    implementation("org.nanohttpd:nanohttpd:2.2.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    implementation("lofitsky:komptube-common:1.0")
}
