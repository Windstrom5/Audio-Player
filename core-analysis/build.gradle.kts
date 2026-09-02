// Pure Kotlin/JVM module. domain/engine's only internal dependency is
// domain/model (grepped every import in every file here before moving
// anything, to confirm this before committing to the module boundary) —
// everything else is kotlin.math.* / java.util.* / java.text.*, all part of
// the standard library any JVM module gets for free.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core-model"))
    testImplementation(libs.junit)
}
