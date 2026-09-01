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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}