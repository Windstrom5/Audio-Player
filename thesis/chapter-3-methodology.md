# Chapter 3: Methodology and System Architecture

This chapter describes the system architecture and implementation details of the adaptive bit-perfect USB audio engine. The architecture is designed to bypass standard Android audio layers and communicate directly with USB DAC hardware while executing real-time integrity verification and adaptive buffer optimization.

---

## 3.1 Architectural Overview

The system is built using **Clean Architecture** principles to separate the presentation, domain, data, USB, and benchmark concerns.

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                   │
│         MainActivity, Compose Screens, ViewModels       │
└────────────────────────────┬────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────┐
│                       Domain Layer                      │
│            Use Cases, Models, Core Engines              │
│  (PlaybackIntegrityEngine, AdaptiveOptimizationEngine)   │
└────────────────────────────┬────────────────────────────┘
                             │
┌────────────────────────────┼────────────────────────────┐
│                            │                            │
┌────────────────────────────▼───┐ ┌──────────────────────▼───┐
│           Data Layer           │ │        USB Layer         │
│  Room DB, Repositories, Scanner│ │   UsbDacManager, Parser  │
└────────────────────────────────┘ └──────────────────────────┘
```

The layers are organized as follows:

1. **Presentation Layer**: Built using Jetpack Compose and MVVM. Exposes real-time dashboards for playback integrity, CPU/memory benchmarking, and research analysis.
2. **Domain Layer**: The mathematical and logical core. Contains use cases and domain engines (`PlaybackIntegrityEngine`, `AdaptiveOptimizationEngine`) that define the business rules of the thesis.
3. **Data Layer**: Manages the local SQLite database via Room, implements scanning logic, and provides repository implementations.
4. **USB Layer**: Houses low-level Android USB Host API classes (`UsbDeviceConnection`, `UsbInterface`) to parse descriptors and manage endpoints in user-space.
5. **Benchmark Layer**: Instruments the pipeline using non-invasive background monitors (`CpuMonitor`, `MemoryMonitor`, etc.) and serializes data to CSV.

---

## 3.2 Local Audio Library and Metadata Extraction

The data layer includes a local library subsystem comprising:

- **LocalAudioScanner**: Scans Android's `MediaStore` content providers to index WAV and FLAC files.
- **MetadataExtractor**: Parses raw file headers to read native sample rates, bit depths, and channel configurations, saving them to `AudioTrackEntity` rows in the local Room database (`AppDatabase`).

This metadata acts as the "source of truth" to match against the target hardware capabilities.

---

## 3.3 Direct USB DAC Communication and Enumeration

The USB layer handles user-space enumeration and capability matching:

- **UsbDacManager**: Registers dynamic broadcast receivers to detect `ACTION_USB_DEVICE_ATTACHED` and `ACTION_USB_DEVICE_DETACHED` events. Handles permission requests via `UsbManager.requestPermission()`.
- **DacCapabilityAnalyzer**: Parses binary USB descriptors directly from the device interfaces. By reading format type descriptors, it extracts the native supported sample rates (e.g. 44.1k, 48k, 96k, 192k) and bit depths (e.g. 16-bit, 24-bit) of the attached DAC.
- **DacProfileRepository**: Automatically saves parsed capabilities to the database as a `DacProfileEntity` for offline profiling.

---

## 3.4 Audio Engine and Playback Pipeline

To ensure absolute bit-perfect streaming, the custom audio engine runs entirely in user-space:

- **Audio Decoder Interface**: Defines `open(path)`, `read(buffer)`, and `close()` operations.
- **WavDecoder & FlacDecoder**: Implement decoding of lossless source files to raw PCM byte arrays.
- **PcmPipeline**: Manages PCM byte transfer between the decoder and the USB interface. It matches the source data structure to the DAC hardware interface structure without any software adjustments.
- **PlaybackController**: Controls the playback loop, starting an asynchronous playback thread on `Dispatchers.IO` using Kotlin Coroutines.
- **BufferManager**: Controls buffer arrays, feeding PCM blocks to the active USB isochronous endpoint.

---

## 3.5 Playback Integrity Verification Engine

The **PlaybackIntegrityEngine** evaluates whether the output stream has been modified. It takes the active `AudioTrack` metadata and matches it against the connected `DacProfile`:

$$\text{Integrity Score} = 100 - \text{Penalty}_{\text{SampleRate}} - \text{Penalty}_{\text{BitDepth}} - \text{Penalty}_{\text{Channels}}$$

### Penalty Criteria
- **Sample Rate Mismatch** (Penalty = 50): If the source rate is not natively supported by the DAC, resampling is required, breaking bit-perfection.
- **Bit Depth Mismatch** (Penalty = 20): If the source bit-depth is unsupported, padding or truncation will modify the sample values.
- **Channel Mismatch** (Penalty = 10): If channels do not match supported configurations, hardware downmixing/upmixing will alter the streams.

An Integrity Score of **100** represents absolute bit-perfect playback. Any score under 100 signals a compromised path.

---

## 3.6 Adaptive Optimization Engine

The **AdaptiveOptimizationEngine** runs in a feedback loop during playback to maintain stability while keeping latency low:

1. **Stability Scoring**: A `StabilityScore` is computed based on dropout occurrences:

$$\text{Stability Percentage} = 100 - (\text{Dropouts} \times 5)$$

2. **Buffer Recommendation Loop**:
   - If **Stability compromised** (dropouts > 0): Double the buffer size (up to 32,768 Bytes) to prevent buffer starvation.
   - If **Stable but Latency high** (> 100ms) and buffer size > 1,024 Bytes: Halve the buffer size to improve system responsiveness.
   - If **Optimal**: Maintain current buffer.

---

## 3.7 Non-Invasive Benchmark Framework

To gather research data, the system includes a dedicated instrumentation layer:

- **CpuMonitor**: Reads CPU statistics from proc files or OS runtime parameters.
- **MemoryMonitor**: Queries `ActivityManager.MemoryInfo` to fetch RAM footprint in Megabytes.
- **LatencyMonitor**: Tracks the round-trip delay between packet queue submissions and hardware buffer acknowledgments.
- **DropoutDetector**: Monitors underflow callbacks from the audio pipeline to count packet starvation events.
- **BenchmarkRunner**: Coordinates monitors, taking ticks at 500ms intervals, aggregating `BenchmarkSample`s, and triggering the `CsvExporter` to export timestamped files.

---

## 3.8 Experimental Protocols

The thesis orchestrator (`ExperimentOrchestrator`) runs four standardized tests to gather empirical research data:

### Experiment A: Playback Architecture Comparison
- Compares the standard Android `AudioTrack` path vs the custom user-space USB pipeline under control conditions (44.1kHz, 16-bit, 4,096 B buffer). This isolates the architecture as the single variable to answer **RQ1**.

### Experiment B: Sample Rate Performance Scaling
- Swings sample rates across 44.1kHz, 48kHz, 96kHz, and 192kHz (fixed 24-bit depth, 4,096 B buffer) to measure CPU load scaling and timing thresholds.

### Experiment C: Hardware DAC Variation
- Evaluates the custom pipeline across different connected DAC profiles (e.g. TI PCM2704 vs XMOS UAC2) to assess hardware controller compatibility and microframe timing impacts.

### Experiment D: Buffer Size Optimization
- Swings buffer sizes from 1,024 Bytes to 32,768 Bytes in a geometric progression to validate the stability-latency curve and test the optimization engine's recommendations.
