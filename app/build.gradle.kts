plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    namespace = "com.rrk.managesensors"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.rrk.managesensors"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    
    kotlinOptions {
        jvmTarget = "17"
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

    buildFeatures {
        aidl = true
        dataBinding = true
        viewBinding = true
    }

    sourceSets {
        getByName("main") {
            aidl {
                srcDirs("src/main/aidl")
            }
        }
    }
}

dependencies {
    implementation("androidx.core:core:1.10.1")
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("dev.rikka.shizuku:api:13.1.5")
    testImplementation("junit:junit:4.13.2")
    implementation("dev.rikka.shizuku:provider:13.1.5")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.databinding:databinding-runtime:8.2.2")
    implementation("androidx.databinding:databinding-common:8.2.2")

}