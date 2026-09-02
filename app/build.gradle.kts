plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.thesis.bitperfectusb"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.thesis.bitperfectusb"
        minSdk = 26
        targetSdk = 35
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
        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    // NOTE: as of Kotlin 2.0+, the Compose compiler ships as part of the Kotlin
    // compiler itself and is configured via the `org.jetbrains.kotlin.plugin.compose`
    // Gradle plugin (applied above, pinned to the same version as Kotlin) — the old
    // `composeOptions { kotlinCompilerExtensionVersion = "..." }` block is obsolete
    // and intentionally removed here.

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    androidResources {
        noCompress += "tflite"
    }
}

dependencies {
    // ── Extracted modules ──────────────────────────────────────────────
    // See settings.gradle.kts for the full planned module graph and why
    // these two came first (zero Android deps, zero circular imports,
    // verified before moving anything).
    implementation(project(":core-model"))
    implementation(project(":core-analysis"))
    implementation(project(":core-usb"))

    // Core / Kotlin
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    // FLAC software-decode fallback (org.kc7bfi.jflac) is vendored directly under
    // app/src/main/java/org/kc7bfi/jflac — see that package's note for why. No
    // Maven dependency needed; it compiles as part of this module.
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.palette.ktx)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Room (local persistence for library, DAC profiles, sessions, benchmarks, experiments)
    // Uses KSP rather than kapt for annotation processing — faster and far less prone to
    // the Gradle-internal-API breakage that kapt has historically hit across Gradle upgrades.
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Koin (dependency injection)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // TensorFlow Lite AI Suite (On-Device Neural Engine)
    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.support)

    // Unit / instrumentation tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}
