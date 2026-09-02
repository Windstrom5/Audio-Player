package com.thesis.bitperfectusb.playback.autoeq

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoEqRepositoryTest {

    @Test
    fun testAllProfilesLoaded() {
        val profiles = AutoEqRepository.getAllProfiles()
        assertTrue("Profiles list should contain at least 25 entries", profiles.size >= 25)

        for (p in profiles) {
            assertTrue("Profile brand should not be blank", p.brand.isNotBlank())
            assertTrue("Profile model should not be blank", p.model.isNotBlank())
            assertEquals("Profile must have exactly 10 EQ bands", 10, p.gains.size)
            for (gain in p.gains) {
                assertTrue("Gain should be within standard dB range [-12, +12]", gain in -12.0f..12.0f)
            }
        }
    }

    @Test
    fun testSearchProfiles() {
        val sennheiser = AutoEqRepository.searchProfiles("HD 600")
        assertEquals(1, sennheiser.size)
        assertEquals("HD 600", sennheiser.first().model)
        assertEquals("Sennheiser", sennheiser.first().brand)

        val moondropIems = AutoEqRepository.searchProfiles("Moondrop")
        assertTrue("Should find multiple Moondrop models", moondropIems.size >= 5)

        val sonyResults = AutoEqRepository.searchProfiles("XM4")
        assertTrue("Should find XM4 headphones/earbuds", sonyResults.size >= 2)

        val tanchjimBunny = AutoEqRepository.searchProfiles("Bunny")
        assertTrue("Should find Tanchjim Bunny", tanchjimBunny.isNotEmpty())
        assertEquals("Tanchjim", tanchjimBunny.first().brand)

        val tanchjimOla2 = AutoEqRepository.searchProfiles("Ola 2")
        assertTrue("Should find Tanchjim Ola 2", tanchjimOla2.isNotEmpty())

        val celestWyvern = AutoEqRepository.searchProfiles("Wyvern")
        assertTrue("Should find multiple Celest Wyvern variants", celestWyvern.size >= 3)

        val celestWyvernAbyss = AutoEqRepository.searchProfiles("Abyss")
        assertTrue("Should find Wyvern Abyss", celestWyvernAbyss.isNotEmpty())

        val celestPhoenixCall = AutoEqRepository.searchProfiles("PhoenixCall")
        assertTrue("Should find Celest PhoenixCall", celestPhoenixCall.isNotEmpty())

        val kineraLoki = AutoEqRepository.searchProfiles("Loki")
        assertTrue("Should find Kinera Loki", kineraLoki.isNotEmpty())

        val kineraCelestWyvern = AutoEqRepository.searchProfiles("kinera celest wyvern")
        assertTrue("Should find Celest Wyvern models even when searching with parent brand 'kinera celest wyvern'", kineraCelestWyvern.isNotEmpty())

        val kineraWyvernAbyss = AutoEqRepository.searchProfiles("kinera wyvern abyss")
        assertTrue("Should find Wyvern Abyss with 'kinera wyvern abyss'", kineraWyvernAbyss.isNotEmpty())
        assertEquals("Wyvern Abyss", kineraWyvernAbyss.first().model)

        val kineraQoaVesper = AutoEqRepository.searchProfiles("kinera qoa vesper")
        assertTrue("Should find QoA Vesper with 'kinera qoa vesper'", kineraQoaVesper.isNotEmpty())
    }
}
