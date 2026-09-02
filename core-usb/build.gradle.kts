// Android Library module — unlike core-model/core-analysis, this genuinely
// needs the Android platform (android.hardware.usb.*, BroadcastReceiver,
// Context), confirmed by grepping every import in usb/ before moving
// anything: android.app/content/hardware.usb/os, kotlinx.coroutines.flow,
// and com.thesis.bitperfectusb.domain.model (→ core-model). Nothing from
// domain.repository, domain.usecase, playback, data, or presentation.
//
// No AndroidManifest.xml needed: nothing here declares a manifest component
// (the USB BroadcastReceiver is registered dynamically via registerReceiver()
// in code, not declared in XML) or requires a manifest-merged permission
// beyond what app/src/main/AndroidManifest.xml already declares — the
// android.hardware.usb.host <uses-feature> and the USB-attach intent-filter
// are app-level concerns (what the *app* advertises/launches on), not
// something this module needs to restate.
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.thesis.bitperfectusb.usb"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core-model"))
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
}
