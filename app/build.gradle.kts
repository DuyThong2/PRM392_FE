plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.prm392fe"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.prm392fe"
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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    //   Là 1 thư viện SDK để giao tiếp với google play service để lấy id-token phía android user
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Nhớ chuyển sang android mode thêm permission INTERNET
    implementation("com.android.volley:volley:1.2.1")
    // api retrofit + gson
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-urlconnection:4.12.0")

    implementation(libs.core.ktx)
    // Use a stable Glide 4.x release (package is com.bumptech.glide)
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.preference)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.30") // Kiểm tra phiên bản mới nhất
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    // Glide annotation processor for generated API (keep with annotationProcessor since kapt not applied)
    // WebSocket (Java-WebSocket là transport layer)
    implementation("org.java-websocket:Java-WebSocket:1.5.3")

    // STOMP client for Android
    // NOTE: Thư viện này dựa trên RxJava2, nên cần thêm RxJava
    implementation("com.github.NaikSoftware:StompProtocolAndroid:1.6.6")

    // RxJava 2 dependencies (Cần thiết cho StompProtocolAndroid)
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")

    // Dùng cho OpenStreetMap
    implementation("org.osmdroid:osmdroid-android:6.1.18")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

}