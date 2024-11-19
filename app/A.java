plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.echoi"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.echoi"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Access API Key
        buildConfigField "String", "OPENAI_API_KEY", "\"${project.OPENAI_API_KEY}\""
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField "String", "OPENAI_API_KEY", "\"${project.OPENAI_API_KEY}\""
        }
        debug {
            buildConfigField "String", "OPENAI_API_KEY", "\"${project.OPENAI_API_KEY}\""
        }
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
    implementation(libs.androidx.core.animation)
    implementation(libs.androidx.animation.graphics.android)

    // OkHttp for HTTP requests
    implementation (libs.okhttp)

    // Gson for JSON parsing
    implementation (libs.gson)

    // Other dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
