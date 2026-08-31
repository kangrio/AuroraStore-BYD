plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.kangrio.extension.stub"
    compileSdk {
        version = release(36)
    }
    defaultConfig {
        minSdk = 24
    }
    buildFeatures {
        aidl = true
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}