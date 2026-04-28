import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

val localProperties = Properties().apply {
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        localFile.inputStream().use(::load)
    }
}

val geminiApiKey = (localProperties.getProperty("GEMINI_API_KEY")
    ?: providers.gradleProperty("GEMINI_API_KEY").orNull
    ?: "")

val imgbbApiKey = (localProperties.getProperty("IMGBB_API_KEY")
    ?: providers.gradleProperty("IMGBB_API_KEY").orNull
    ?: "")

val adminEmail = (localProperties.getProperty("ADMIN_EMAIL")
    ?: providers.gradleProperty("ADMIN_EMAIL").orNull
    ?: "admin@gmail.com")

android {
    namespace = "com.tommy.civictrack"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.tommy.civictrack"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
            buildConfigField("String", "IMGBB_API_KEY", "\"$imgbbApiKey\"")
            buildConfigField("String", "ADMIN_EMAIL", "\"$adminEmail\"")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
            buildConfigField("String", "IMGBB_API_KEY", "\"$imgbbApiKey\"")
            buildConfigField("String", "ADMIN_EMAIL", "\"$adminEmail\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.glide)
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.gms:play-services-auth:21.5.1")
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
}
