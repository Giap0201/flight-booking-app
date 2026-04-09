plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.flight_booking_app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.flight_booking_app"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.core.ktx)
    implementation(libs.core)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // Thư viện Retrofit để gọi API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
// Thư viện chuyển đổi JSON sang Java Object (Gson)
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
// Thư viện OkHttp để ghi log (xem request/response trong Logcat)
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
// Thư viện ZXing để tạo mã QR cho Boarding Pass
    implementation("com.google.zxing:core:3.5.2")
}