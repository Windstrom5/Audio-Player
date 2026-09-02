#!/usr/bin/env python3
"""
Demucs Neural Audio Stem Separator for BitPerfectUSB.

Uses Meta AI's Demucs (Hybrid Transformer Demucs / HTDemucs) deep neural network
to separate audio tracks into studio-grade stems:
- Vocals (Acapella)
- Instrumental (No Vocals = Drums + Bass + Other)
- Drums
- Bass
- Other / Synth

Requirements:
    pip install demucs torch torchaudio soundfile

Usage:
    python tools/demucs_separator.py --input "path/to/song.flac" --output "./separated" --model htdemucs
    python tools/demucs_separator.py --input "path/to/song.flac" --push-to-phone
"""

import os
import sys
import argparse
import subprocess
from pathlib import Path

def check_dependencies():
    try:
        import torch
        import demucs.separate
        print(f"[✓] PyTorch {torch.__version__} and Demucs found.")
        if torch.cuda.is_available():
            print(f"[✓] GPU Acceleration Active: {torch.cuda.get_device_name(0)}")
        else:
            print("[!] Running on CPU (install CUDA PyTorch for faster inference).")
    except ImportError:
        print("[!] Demucs not installed. Installing required dependencies...")
        subprocess.check_call([sys.executable, "-m", "pip", "install", "demucs", "torch", "torchaudio", "soundfile"])

def separate_audio(input_file: str, output_dir: str = "./separated_stems", model: str = "htdemucs", two_stems: str = "vocals"):
    """
    Runs Demucs neural source separation on the input audio file.
    """
    input_path = Path(input_file).resolve()
    if not input_path.exists():
        print(f"[-] Error: Input audio file not found at '{input_path}'")
        return None

    out_path = Path(output_dir).resolve()
    out_path.mkdir(parents=True, exist_ok=True)

    print(f"\n[+] Starting Demucs Neural Stem Separation...")
    print(f"    Input File : {input_path.name}")
    print(f"    Model      : {model} (State-of-the-Art Hybrid Transformer)")
    print(f"    Output Dir : {out_path}\n")

    cmd = [
        sys.executable, "-m", "demucs.separate",
        "-n", model,
        "--out", str(out_path),
        "--flac"
    ]

    if two_stems:
        cmd.extend(["--two-stems", two_stems]) # Generates vocals and no_vocals (instrumental)

    cmd.append(str(input_path))

    result = subprocess.run(cmd)
    if result.returncode == 0:
        print(f"\n[✓] Separation Complete! High-definition stems saved in '{out_path}/{model}/{input_path.stem}/'")
        return out_path / model / input_path.stem
    else:
        print("[-] Demucs separation encountered an error.")
        return None

def push_to_phone(stem_dir: Path):
    """
    Pushes the separated instrumental and vocal tracks to the connected Android device via ADB.
    """
    print(f"\n[+] Checking ADB connection for Android device...")
    try:
        adb_check = subprocess.run(["adb", "devices"], capture_output=True, text=True)
        if "device\n" not in adb_check.stdout and "\tdevice" not in adb_check.stdout:
            print("[-] No Android device detected via ADB. Please connect your phone with USB Debugging enabled.")
            return

        phone_music_dir = "/sdcard/Music/BitPerfect_Stems"
        subprocess.run(["adb", "shell", "mkdir", "-p", phone_music_dir])

        for file in stem_dir.glob("*.flac"):
            print(f"    Pushing {file.name} -> {phone_music_dir}/{file.name} ...")
            subprocess.run(["adb", "push", str(file), f"{phone_music_dir}/{file.name}"])

        # Trigger Android media scanner
        subprocess.run(["adb", "shell", "am", "broadcast", "-a", "android.intent.action.MEDIA_SCANNER_SCAN_FILE", "-d", f"file://{phone_music_dir}"])
        print(f"[✓] Successfully transferred studio stems to phone: '{phone_music_dir}'!")
    except FileNotFoundError:
        print("[-] ADB not found in system PATH. Install Android platform-tools to use --push-to-phone.")

def main():
    parser = argparse.ArgumentParser(description="Demucs Neural Stem Separation for BitPerfectUSB")
    parser.add_argument("--input", "-i", required=True, help="Path to input audio file (.flac, .wav, .mp3)")
    parser.add_argument("--output", "-o", default="./separated_stems", help="Output directory for stems")
    parser.add_argument("--model", "-m", default="htdemucs", choices=["htdemucs", "htdemucs_ft", "mdx_extra_q"], help="Demucs neural model")
    parser.add_argument("--all-stems", action="store_true", help="Separate all 4 stems (vocals, drums, bass, other) instead of 2 stems")
    parser.add_argument("--push-to-phone", "-p", action="store_true", help="Push separated stems directly to phone via ADB")

    args = parser.parse_args()
    check_dependencies()

    two_stems_arg = None if args.all_stems else "vocals"
    stems_path = separate_audio(args.input, args.output, args.model, two_stems_arg)

    if stems_path and args.push_to_phone:
        push_to_phone(stems_path)

if __name__ == "__main__":
    main()
