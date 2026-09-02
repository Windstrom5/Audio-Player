# 🎙️ Demucs Neural Vocal Separation for BitPerfectUSB

This directory contains tools to use **Meta AI's Demucs (Hybrid Transformer Demucs / `htdemucs`)** to separate any song into 100% studio-grade **Instrumental (No Vocals)** and **Acapella (Solo Vocals)** tracks.

---

## 🚀 Quick Start: Separate Songs with Demucs

### 1. Install Dependencies
```bash
pip install demucs torch torchaudio soundfile
```
*(If you have an NVIDIA GPU, install PyTorch with CUDA for 10x faster separation).*

### 2. Separate a Song into Instrumental & Vocals
```bash
python tools/demucs_separator.py --input "D:/Music/Lisa_ADAMAS.flac"
```

This will produce:
- `separated_stems/htdemucs/Lisa_ADAMAS/no_vocals.flac` *(Pure Instrumental with 100% vocal elimination)*
- `separated_stems/htdemucs/Lisa_ADAMAS/vocals.flac` *(Pure Acapella)*

### 3. Automatically Push Instrumental Stems to Your Phone via ADB
```bash
python tools/demucs_separator.py --input "D:/Music/Lisa_ADAMAS.flac" --push-to-phone
```
This automatically separates the track with Demucs and transfers it to `/sdcard/Music/BitPerfect_Stems/` on your phone!

---

## ⚡ Real-Time On-Device TFLite Separation

To generate and bundle an on-device TensorFlow Lite model for real-time mobile inference:
```bash
python tools/convert_demucs_to_tflite.py --output app/src/main/assets/models/vocal_separator.tflite
```
BitPerfectUSB will automatically detect the bundled `.tflite` file and accelerate it with **XNNPACK (4 CPU threads)** or Mobile GPU.
