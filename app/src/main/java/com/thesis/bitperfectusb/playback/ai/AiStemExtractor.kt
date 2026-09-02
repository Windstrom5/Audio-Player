package com.thesis.bitperfectusb.playback.ai

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.util.Log
import com.thesis.bitperfectusb.domain.model.AudioFileFormat
import com.thesis.bitperfectusb.domain.model.AudioTrackModel
import com.thesis.bitperfectusb.domain.model.KaraokeModeType
import com.thesis.bitperfectusb.domain.repository.AudioLibraryRepository
import com.thesis.bitperfectusb.playback.FlacDecoder
import com.thesis.bitperfectusb.playback.WavDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile

data class StemExtractionProgress(
    val progressPercent: Int = 0,
    val statusMessage: String = "",
    val isCompleted: Boolean = false,
    val outputFile: File? = null,
    val error: String? = null
)

/**
 * On-device neural stem separator and instrumental track generator.
 * Processes audio files on the phone's CPU with multi-threading without requiring any PC intervention.
 */
class AiStemExtractor(
    private val context: Context? = null,
    private val audioLibraryRepository: AudioLibraryRepository
) {

    private val vocalIsolator = AiVocalIsolator(context)

    /**
     * Extracts an instrumental or acapella stem from the given track directly on the device.
     */
    fun extractStem(
        track: AudioTrackModel,
        mode: KaraokeModeType = KaraokeModeType.INSTRUMENTAL_ONLY,
        strength: Float = 0.95f
    ): Flow<StemExtractionProgress> = flow {
        emit(StemExtractionProgress(0, "Initializing on-device neural stem extractor..."))

        val ctx = context
        if (ctx == null) {
            emit(StemExtractionProgress(0, "Android context required for decoding", isCompleted = true, error = "Context is null"))
            return@flow
        }

        val decoder = when (track.format) {
            AudioFileFormat.FLAC -> FlacDecoder(ctx)
            AudioFileFormat.WAV -> WavDecoder(ctx)
        }

        val pcmFormat = try {
            decoder.open(track.filePath)
        } catch (e: Exception) {
            emit(StemExtractionProgress(0, "Failed to open track: ${e.message}", isCompleted = true, error = e.message))
            return@flow
        }

        val sampleRate = pcmFormat.sampleRateHz
        val channels = pcmFormat.channels
        val bitDepth = pcmFormat.bitDepth

        // Prepare app cache directory (kept in private cache, not in music folder)
        val outputDir = File(ctx.externalCacheDir ?: ctx.cacheDir, "stem_cache").apply {
            if (!exists()) mkdirs()
        }
        val prefix = if (mode == KaraokeModeType.INSTRUMENTAL_ONLY) "instrumental" else "acapella"
        val outputFile = File(outputDir, "stem_${track.id}_$prefix.wav")
        val tempFile = File(outputDir, "stem_${track.id}_$prefix.tmp")

        emit(StemExtractionProgress(5, "Rendering to high-speed audio cache..."))

        var totalBytesWritten = 0L
        val fos = try {
            FileOutputStream(tempFile)
        } catch (e: Exception) {
            decoder.close()
            emit(StemExtractionProgress(0, "Failed to create cache file: ${e.message}", isCompleted = true, error = e.message))
            return@flow
        }
        writeWavHeaderPlaceholder(fos)

        val bytesPerSample = (bitDepth / 8).coerceAtLeast(2)
        val frameBytes = channels * bytesPerSample
        val bufferSize = 8192 * frameBytes
        val readBuffer = ByteArray(bufferSize)

        val totalFrames = if (track.durationMs > 0) (track.durationMs * sampleRate) / 1000L else 1000000L
        var processedFrames = 0L

        try {
            while (true) {
                val n = decoder.read(readBuffer)
                if (n <= 0) break

                // Convert to float samples
                val sampleCount = n / bytesPerSample
                val floats = FloatArray(sampleCount)
                when (bitDepth) {
                    24 -> {
                        val factor = 1.0f / 8388608.0f
                        for (i in 0 until sampleCount) {
                            val offset = i * 3
                            val b0 = readBuffer[offset].toInt() and 0xFF
                            val b1 = readBuffer[offset + 1].toInt() and 0xFF
                            val b2 = readBuffer[offset + 2].toInt()
                            val sampleInt = (b2 shl 16) or (b1 shl 8) or b0
                            floats[i] = (sampleInt * factor).coerceIn(-1.0f, 1.0f)
                        }
                    }
                    32 -> {
                        val factor = 1.0f / 2147483648.0f
                        for (i in 0 until sampleCount) {
                            val offset = i * 4
                            val b0 = readBuffer[offset].toInt() and 0xFF
                            val b1 = readBuffer[offset + 1].toInt() and 0xFF
                            val b2 = readBuffer[offset + 2].toInt() and 0xFF
                            val b3 = readBuffer[offset + 3].toInt()
                            val sampleInt = (b3 shl 24) or (b2 shl 16) or (b1 shl 8) or b0
                            floats[i] = (sampleInt * factor).coerceIn(-1.0f, 1.0f)
                        }
                    }
                    else -> { // 16-bit
                        val factor = 1.0f / 32768.0f
                        for (i in 0 until sampleCount) {
                            val offset = i * 2
                            val low = readBuffer[offset].toInt() and 0xFF
                            val high = readBuffer[offset + 1].toInt() shl 8
                            val sample = (high or low).toShort()
                            floats[i] = (sample * factor).coerceIn(-1.0f, 1.0f)
                        }
                    }
                }

                // Apply vocal suppression/isolation
                vocalIsolator.processFloats(
                    floats = floats,
                    sampleRate = sampleRate,
                    mode = mode,
                    strength = strength,
                    preserveBass = true,
                    keyShiftSemitones = 0
                )

                // Pack back to 16-bit PCM for universal WAV compatibility
                val outBytes = ByteArray(floats.size * 2)
                for (i in floats.indices) {
                    val s = (floats[i] * 32767.0f).coerceIn(-32768.0f, 32767.0f).toInt().toShort()
                    val dst = i * 2
                    outBytes[dst] = (s.toInt() and 0xFF).toByte()
                    outBytes[dst + 1] = ((s.toInt() ushr 8) and 0xFF).toByte()
                }

                fos.write(outBytes)
                totalBytesWritten += outBytes.size

                processedFrames += (sampleCount / channels)
                val percent = minOf(95, 5 + ((processedFrames.toFloat() / totalFrames) * 90).toInt())
                emit(StemExtractionProgress(percent, "Extracting stems on-device... $percent%"))
            }

            fos.flush()
            fos.close()
            decoder.close()

            // Finalize WAV Header with exact file sizes on temp file
            finalizeWavHeader(tempFile, totalBytesWritten, sampleRate, channels, 16)

            // Atomic rename
            if (outputFile.exists()) {
                outputFile.delete()
            }
            tempFile.renameTo(outputFile)

            emit(
                StemExtractionProgress(
                    progressPercent = 100,
                    statusMessage = "Cached Successfully! (Ready for Instant Playback)",
                    isCompleted = true,
                    outputFile = outputFile
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Stem extraction error: ${e.message}", e)
            try { fos.close() } catch (_: Exception) {}
            try { decoder.close() } catch (_: Exception) {}
            try { if (tempFile.exists()) tempFile.delete() } catch (_: Exception) {}
            emit(StemExtractionProgress(0, "Extraction failed: ${e.message}", isCompleted = true, error = e.message))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Checks if an instrumental or acapella stem is already cached for the given track.
     */
    fun getCachedStem(trackId: Long, mode: KaraokeModeType): File? {
        val ctx = context ?: return null
        val outputDir = File(ctx.externalCacheDir ?: ctx.cacheDir, "stem_cache")
        val prefix = if (mode == KaraokeModeType.INSTRUMENTAL_ONLY) "instrumental" else "acapella"
        val file = File(outputDir, "stem_${trackId}_$prefix.wav")
        return if (file.exists() && file.length() > 44) file else null
    }

    /**
     * Calculates total bytes consumed by all cached stems.
     */
    fun getCacheSizeBytes(): Long {
        val ctx = context ?: return 0L
        val outputDir = File(ctx.externalCacheDir ?: ctx.cacheDir, "stem_cache")
        if (!outputDir.exists()) return 0L
        return outputDir.listFiles()?.sumOf { it.length() } ?: 0L
    }

    /**
     * Deletes all cached stems to free up storage.
     */
    fun clearStemCache(): Boolean {
        val ctx = context ?: return false
        val outputDir = File(ctx.externalCacheDir ?: ctx.cacheDir, "stem_cache")
        if (!outputDir.exists()) return true
        return outputDir.listFiles()?.all { it.delete() } ?: true
    }

    private fun writeWavHeaderPlaceholder(fos: FileOutputStream) {
        val dummy = ByteArray(44)
        fos.write(dummy)
    }

    private fun finalizeWavHeader(file: File, pcmDataLength: Long, sampleRate: Int, channels: Int, bitDepth: Int) {
        val raf = RandomAccessFile(file, "rw")
        val totalDataLen = pcmDataLength + 36
        val byteRate = sampleRate * channels * (bitDepth / 8)

        raf.seek(0)
        raf.writeBytes("RIFF")
        raf.write(intToLittleEndian(totalDataLen.toInt()))
        raf.writeBytes("WAVE")
        raf.writeBytes("fmt ")
        raf.write(intToLittleEndian(16)) // Subchunk1Size for PCM
        raf.write(shortToLittleEndian(1)) // AudioFormat 1 = PCM
        raf.write(shortToLittleEndian(channels.toShort()))
        raf.write(intToLittleEndian(sampleRate))
        raf.write(intToLittleEndian(byteRate))
        raf.write(shortToLittleEndian((channels * (bitDepth / 8)).toShort())) // BlockAlign
        raf.write(shortToLittleEndian(bitDepth.toShort()))
        raf.writeBytes("data")
        raf.write(intToLittleEndian(pcmDataLength.toInt()))
        raf.close()
    }

    private fun intToLittleEndian(value: Int): ByteArray = byteArrayOf(
        (value and 0xFF).toByte(),
        ((value ushr 8) and 0xFF).toByte(),
        ((value ushr 16) and 0xFF).toByte(),
        ((value ushr 24) and 0xFF).toByte()
    )

    private fun shortToLittleEndian(value: Short): ByteArray = byteArrayOf(
        (value.toInt() and 0xFF).toByte(),
        ((value.toInt() ushr 8) and 0xFF).toByte()
    )

    companion object {
        private const val TAG = "AiStemExtractor"
    }
}
