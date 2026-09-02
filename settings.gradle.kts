pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "BitPerfectUSB"
include(":app")

// ── Multi-module extraction, in progress ────────────────────────────────
// core-model and core-analysis (pure Kotlin/JVM) were the cleanest possible
// starting slices — zero Android dependency, zero circular imports. core-usb
// is the first Android Library module in the split: it genuinely needs
// android.hardware.usb.* and Context, so it uses com.android.library instead
// of the JVM plugin, but the same verify-imports-before-moving discipline
// applied — see core-usb/build.gradle.kts's own note. The rest of the
// planned split is listed here so the target shape stays visible even before
// each slice is done:
//
//   core-model     [DONE] domain/model — pure data classes, zero deps
//   core-analysis  [DONE] domain/engine — depends only on core-model
//   core-usb       [DONE] usb/ — UsbDacManager, descriptor parsing, isochronous streamer
//   core-decoder   [ ] playback/*Decoder.kt + vendored jflac
//   core-verifier  [ ] playback/TransferVerifier, PcmContainerPacker
//   core-audio     [ ] playback/PlaybackController, PlaybackEngine + impls
//   data           [ ] data/ — Room, repositories, settings
//   benchmark      [ ] benchmark/ — ExperimentOrchestrator, DacRateBenchmark
//   feature-*      [ ] presentation/screen + viewmodel, split per screen
//   core-ui        [ ] presentation/components + theme — shared by every feature-* module
//
// Each remaining slice needs the same treatment as the ones done so far: grep
// every import in the target packages first to confirm the dependency
// direction is clean before moving files, since there's no Gradle sync
// available in this environment to catch a mistake — see the README's Module
// Restructuring section for the full writeup.
include(":core-model")
include(":core-analysis")
include(":core-usb")
