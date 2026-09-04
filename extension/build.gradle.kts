plugins {
    alias(libs.plugins.android.application)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xno-param-assertions",
            "-Xno-call-assertions",
            "-Xno-receiver-assertions",
        )
    }
}

android {
    namespace = "com.kangrio.extension"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 23
        targetSdk = 36
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildTypes {
        debug {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation("org.lsposed.hiddenapibypass:hiddenapibypass:6.1")
    compileOnly("androidx.annotation:annotation:1.9.1")
    compileOnly(project(":extension:stub"))
}

abstract class BuildExtensionTask : DefaultTask() {

    @get:Inject
    abstract val fs: FileSystemOperations

    @TaskAction
    fun run() {
        fs.copy {
            from("build/intermediates/dex/release/minifyReleaseWithR8/classes.dex")
            into("../app/src/main/assets/patches/dex")
        }
        fs.copy {
            from("build/intermediates/merged_native_libs/release/mergeReleaseNativeLibs/out")
            into("../app/src/main/assets/patches")
        }
    }
}

tasks.register<BuildExtensionTask>("buildExtension") {
    dependsOn("assembleRelease")
}

tasks.named<Delete>("clean") {
    delete("../app/src/main/assets/patches")
}