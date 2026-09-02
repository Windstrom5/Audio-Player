# Chapter 4: Results and Data Analysis

This chapter presents the experimental findings and statistical analyses of the adaptive bit-perfect USB audio engine. The architecture was evaluated across four distinct protocols (Experiments A through D) designed to isolate, measure, and analyze audio playback integrity, round-trip latency, resource utilization, and scheduling stability.

---

## 4.1 Playback Path and Integrity Analysis (Experiment A)

Experiment A directly addresses **Research Question 1 (RQ1)**: *Can a custom USB audio architecture preserve audio integrity better than Android default playback?*

Two configurations were compared under control variables (sample rate = 44.1kHz, bit-depth = 16-bit, buffer size = 4,096 bytes, stereo channel mapping):
1. **Android AudioTrack (Control)**: Standard user-space playback routed through the Android Audio Flinger mixer.
2. **Custom USB Engine (Treatment)**: User-space driver communicating directly with the DAC via USB bulk transfer protocols.

### 4.1.1 Descriptive Statistical Summary

| Configuration | Metric | Sample Size ($N$) | Mean ($\mu$) | Std. Dev. ($SD$) | 95% Confidence Interval (CI) | Dropouts (Total) | Integrity Score |
| :--- | :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| **AudioTrack** | Latency (ms) | 120 | 72.40 | 4.82 | [71.54, 73.26] | 0 | 0 / 100 |
| **AudioTrack** | CPU (%) | 120 | 3.20 | 0.45 | [3.12, 3.28] | - | - |
| **Custom Engine** | Latency (ms) | 120 | 12.10 | 1.15 | [11.89, 12.31] | 0 | 100 / 100 |
| **Custom Engine** | CPU (%) | 120 | 4.90 | 0.88 | [4.74, 5.06] | - | - |

### 4.1.2 Hypothesis Testing and Significance (Welch's T-Test)

To evaluate whether the custom USB architecture achieves statistically significant latency reductions, a Welch’s two-sample $t$-test was conducted.

*   **Null Hypothesis ($H_0$)**: $\mu_{\text{Latency, AudioTrack}} = \mu_{\text{Latency, Custom}}$ (No difference in latency).
*   **Alternative Hypothesis ($H_1$)**: $\mu_{\text{Latency, AudioTrack}} \neq \mu_{\text{Latency, Custom}}$ (Significant difference in latency).

#### Latency Welch $t$-Test Results
*   **$t$-statistic**: $132.84$
*   **Degrees of Freedom ($df$)**: $134$
*   **$p$-value**: $p < 0.0001$ ($\alpha = 0.05$)
*   **Decision**: Reject $H_0$. The custom architecture achieves a highly significant latency reduction ($72.4\text{ ms} \rightarrow 12.1\text{ ms}$).

#### CPU Overhead Welch $t$-Test Results
A secondary $t$-test evaluated the CPU overhead introduced by driving USB transfers in user-space.
*   **$t$-statistic**: $-18.63$
*   **Degrees of Freedom ($df$)**: $178$
*   **$p$-value**: $p < 0.0001$ ($\alpha = 0.05$)
*   **Decision**: Reject $H_0$. The custom engine introduces a statistically significant, yet operationally acceptable, CPU overhead of $1.7\%$ due to asynchronous USB transfer loops and poll routines.

---

## 4.2 Performance Scaling with Sample Rates (Experiment B)

Experiment B evaluates the impact of high-resolution sample rates (44.1kHz, 48kHz, 96kHz, and 192kHz) on the stability and resource demands of the custom user-space driver (24-bit depth, 4,096 bytes buffer).

### 4.2.1 Resource Scaling Summary

| Configuration | Sample Rate | Mean CPU (%) | Mean Memory (MB) | Mean Latency (ms) | Dropouts (Total) |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **exp_b_44100hz** | 44.1 kHz | 4.80 | 42.10 | 12.00 | 0 |
| **exp_b_48000hz** | 48.0 kHz | 5.10 | 42.10 | 11.20 | 0 |
| **exp_b_96000hz** | 96.0 kHz | 7.90 | 42.80 | 9.40 | 0 |
| **exp_b_192000hz** | 192.0 kHz | 14.80 | 43.50 | 8.10 | 2 |

### 4.2.2 Statistical Significance (One-Way ANOVA)

A One-way Analysis of Variance (ANOVA) was executed to determine whether CPU utilization changes significantly across sample rates.

*   **Null Hypothesis ($H_0$)**: $\mu_{\text{CPU, 44.1k}} = \mu_{\text{CPU, 48k}} = \mu_{\text{CPU, 96k}} = \mu_{\text{CPU, 192k}}$
*   **Alternative Hypothesis ($H_1$)**: At least one sample rate CPU utilization mean differs.

#### ANOVA Output
*   **$F$-statistic**: $158.42$
*   **Degrees of Freedom ($df$ Between, $df$ Within)**: $(3, 476)$
*   **$p$-value**: $p < 0.0001$ ($\alpha = 0.05$)
*   **Decision**: Reject $H_0$. Increasing the sample rate significantly scales CPU usage due to the geometric increase in the frequency of sub-millisecond asynchronous USB packet submissions.

---

## 4.3 Hardware DAC Controller Analysis (Experiment C)

Experiment C isolates the variance introduced by different USB DAC hardware controllers under standard playback parameters (44.1kHz, 16-bit, 4,096 bytes buffer).

### 4.3.1 DAC-Specific Benchmarks

| DAC Controller Profile | USB Vendor / Product ID | Mean CPU (%) | Mean Latency (ms) | Dropouts | UAC2 Compliant |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **Texas Instruments PCM2704** | `0x08BB / 0x2704` | 4.90 | 12.10 | 0 | No |
| **XMOS XU208 (Control UAC2)** | `0x20B1 / 0x000A` | 6.20 | 11.80 | 0 | Yes |
| **ESS ES9018K2M** | `0x249A / 0x9018` | 5.80 | 11.90 | 0 | Yes |

*Finding*: The XMOS controller and ESS DAC profiles demand slightly higher CPU (+$0.9\%$ to +$1.3\%$) than the simpler TI PCM2704. This is attributed to the UAC2 high-speed packet protocol handling, which operates at microframe intervals (125 microseconds) compared to UAC1 frames (1 millisecond).

---

## 4.4 Buffer Size and Latency Optimization (Experiment D)

Experiment D maps the latency-stability trade-off across a geometric progression of buffer sizes (1024, 2048, 4096, 8192, 16384, and 32768 Bytes).

### 4.4.1 Buffer Size Sweep Results

| Configuration | Buffer Size (Bytes) | Mean Latency (ms) | Latency SD (ms) | Dropouts (Total) | Playback Stability |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **exp_d_buf1024** | 1024 B | 3.10 | 4.82 | 41 | 82.5% (starved) |
| **exp_d_buf2048** | 2048 B | 6.20 | 2.15 | 8 | 96.6% (unstable) |
| **exp_d_buf4096** | 4096 B | 12.00 | 1.12 | 0 | 100.0% (optimal) |
| **exp_d_buf8192** | 8192 B | 24.10 | 0.82 | 0 | 100.0% (stable) |
| **exp_d_buf16384** | 16384 B | 48.20 | 0.51 | 0 | 100.0% (stable) |
| **exp_d_buf32768** | 32768 B | 96.40 | 0.32 | 0 | 100.0% (stable) |

### 4.4.2 Statistical Significance (One-Way ANOVA)

An ANOVA test evaluated whether buffer size choice significantly alters end-to-end latency.

#### ANOVA Output
*   **$F$-statistic**: $1412.30$
*   **Degrees of Freedom ($df_B, df_W$)**: $(5, 714)$
*   **$p$-value**: $p < 0.0001$ ($\alpha = 0.05$)
*   **Decision**: Reject $H_0$. Buffer size is the principal modifier of playback latency. While small buffers (1024B, 2048B) minimize latency (under 7ms), they introduce high latency variance (SD = 4.82ms) and packet dropouts, validating the need for the `AdaptiveOptimizationEngine`'s recommendation of a 4,096 Byte safety threshold.
