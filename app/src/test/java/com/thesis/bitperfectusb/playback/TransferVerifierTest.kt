package com.thesis.bitperfectusb.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TransferVerifierTest {

    @Test
    fun `identical bytes at equal counts verify true`() {
        val verifier = TransferVerifier()
        val data = byteArrayOf(1, 2, 3, 4, 5)
        verifier.recordSourceBytes(data)
        verifier.recordTransmittedBytes(data)
        val snapshot = verifier.snapshot()
        assertEquals(true, snapshot.verified)
        assertEquals(snapshot.sourceChecksum, snapshot.transmittedChecksum)
    }

    @Test
    fun `modified bytes at equal counts verify false`() {
        val verifier = TransferVerifier()
        verifier.recordSourceBytes(byteArrayOf(1, 2, 3, 4, 5))
        verifier.recordTransmittedBytes(byteArrayOf(1, 2, 3, 4, 6)) // last byte differs
        val snapshot = verifier.snapshot()
        assertEquals(false, snapshot.verified)
    }

    @Test
    fun `unequal byte counts are not yet comparable`() {
        val verifier = TransferVerifier()
        verifier.recordSourceBytes(byteArrayOf(1, 2, 3, 4, 5))
        verifier.recordTransmittedBytes(byteArrayOf(1, 2, 3)) // transmission still catching up
        assertNull(verifier.snapshot().verified)
    }

    @Test
    fun `re-chunked but otherwise identical byte stream still verifies true`() {
        // Mirrors the real pipeline: source read in one chunk, transmitted in several smaller packets.
        val verifier = TransferVerifier()
        val fullStream = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        verifier.recordSourceBytes(fullStream)

        // Transmitted as 3 separate smaller packets instead of one chunk
        verifier.recordTransmittedBytes(fullStream.copyOfRange(0, 4))
        verifier.recordTransmittedBytes(fullStream.copyOfRange(4, 7))
        verifier.recordTransmittedBytes(fullStream.copyOfRange(7, 10))

        val snapshot = verifier.snapshot()
        assertEquals(10L, snapshot.sourceBytes)
        assertEquals(10L, snapshot.transmittedBytes)
        assertTrue(snapshot.verified == true)
    }

    @Test
    fun `reset clears all accumulated state`() {
        val verifier = TransferVerifier()
        verifier.recordSourceBytes(byteArrayOf(1, 2, 3))
        verifier.recordTransmittedBytes(byteArrayOf(1, 2, 3))
        verifier.reset()
        val snapshot = verifier.snapshot()
        assertEquals(0L, snapshot.sourceBytes)
        assertEquals(0L, snapshot.transmittedBytes)
        assertNull(snapshot.verified) // 0 source bytes never counts as a verified comparison
    }
}
