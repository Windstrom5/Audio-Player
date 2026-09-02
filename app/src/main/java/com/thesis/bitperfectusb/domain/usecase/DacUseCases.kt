package com.thesis.bitperfectusb.domain.usecase

import com.thesis.bitperfectusb.domain.model.DacProfile
import com.thesis.bitperfectusb.domain.repository.DacProfileRepository
import com.thesis.bitperfectusb.usb.DacCapabilityAnalyzer
import com.thesis.bitperfectusb.usb.DescriptorTreeBuilder
import com.thesis.bitperfectusb.usb.UsbDacManager
import com.thesis.bitperfectusb.usb.UsbDescriptorParser
import kotlinx.coroutines.flow.Flow

class ObserveDacProfilesUseCase(private val repository: DacProfileRepository) {
    operator fun invoke(): Flow<List<DacProfile>> = repository.observeProfiles()
}

/**
 * RO2 support: parses raw USB descriptors from the attached device (Section 3.3)
 * and persists the resulting capability map. Always re-analyzes and overwrites any
 * existing profile for the same vendor/product pair (DacProfileDao.insert uses
 * OnConflictStrategy.REPLACE for exactly this) rather than returning a cached
 * result — "Analyze Capabilities" is a manual, deliberate action, and silently
 * short-circuiting it on a cache hit meant a fix to the analyzer itself (e.g. the
 * containerBytes/subslot-size correction) would never actually reach an
 * already-profiled DAC no matter how many times the person re-ran it.
 */
class AnalyzeDacUseCase(
    private val usbDacManager: UsbDacManager,
    private val capabilityAnalyzer: DacCapabilityAnalyzer,
    private val repository: DacProfileRepository
) {
    suspend operator fun invoke(): Result<DacProfile> {
        val device = usbDacManager.currentDevice
            ?: return Result.failure(IllegalStateException("No USB DAC attached."))

        val connection = usbDacManager.openConnection(device)
            ?: return Result.failure(IllegalStateException("USB permission not granted or device busy."))

        return try {
            val profile = capabilityAnalyzer.analyze(device, connection)
            val id = repository.saveProfile(profile)
            Result.success(profile.copy(id = id))
        } catch (t: Throwable) {
            Result.failure(t)
        } finally {
            connection.close()
        }
    }
}

/**
 * The USB Descriptor Explorer's data source — the raw, un-folded descriptor tree
 * (Configuration > Interface > every descriptor within it), as opposed to
 * [AnalyzeDacUseCase]'s summarized capability matrix. Opens and closes its own
 * short-lived connection, same pattern as AnalyzeDacUseCase, so it's safe to call
 * independent of whether a profile has ever been analyzed.
 */
class FetchDescriptorTreeUseCase(
    private val usbDacManager: UsbDacManager,
    private val descriptorParser: UsbDescriptorParser
) {
    suspend operator fun invoke(): Result<DescriptorTreeBuilder.DescriptorNode> {
        val device = usbDacManager.currentDevice
            ?: return Result.failure(IllegalStateException("No USB DAC attached."))

        val connection = usbDacManager.openConnection(device)
            ?: return Result.failure(IllegalStateException("USB permission not granted or device busy."))

        return try {
            val raw = connection.rawDescriptors
                ?: return Result.failure(IllegalStateException("Device returned no descriptors."))
            val parsed = descriptorParser.parseAll(raw)
            Result.success(DescriptorTreeBuilder.build(parsed))
        } catch (t: Throwable) {
            Result.failure(t)
        } finally {
            connection.close()
        }
    }
}
