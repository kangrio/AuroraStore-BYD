plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.kangrio.extension.stub"
    compileSdk {
        version = release(36)
    }
    buildFeatures {
        aidl = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    compileOnly("org.microg:safe-parcel:1.7.1")
    compileOnly("androidx.lifecycle:lifecycle-runtime:2.11.0")
    compileOnly("androidx.lifecycle:lifecycle-service:2.11.0")
}