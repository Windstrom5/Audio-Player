// Pure Kotlin/JVM module — no Android dependency. Every file under
// domain/model was confirmed (by grepping every import before moving
// anything here) to depend on nothing beyond the Kotlin stdlib, so this
// doesn't need the Android library plugin, a manifest, or resources — just
// plain JVM compilation, which is both simpler and faster to build than an
// Android library module would be for content with no Android API surface.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}
