package com.thesis.bitperfectusb.playback.lyrics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LyricsRomanizerTest {

    @Test
    fun testDetectsNonRomaji() {
        assertTrue(LyricsRomanizer.hasNonRomaji("君の心へ届くまで"))
        assertTrue(LyricsRomanizer.hasNonRomaji("ありがとう"))
        assertTrue(LyricsRomanizer.hasNonRomaji("사랑해요"))
        assertFalse(LyricsRomanizer.hasNonRomaji("Hello World 123!"))
    }

    @Test
    fun testJapaneseHiraganaRomanization() {
        val romanized = LyricsRomanizer.romanize("ありがとう")
        assertEquals("arigatou", romanized)
    }

    @Test
    fun testJapaneseKatakanaRomanization() {
        val romanized = LyricsRomanizer.romanize("メロディー")
        assertEquals("merodii", romanized)
    }

    @Test
    fun testJapaneseSokuonDoubleConsonants() {
        val romanized = LyricsRomanizer.romanize("ちょっとまって")
        assertEquals("chottomatte", romanized)
    }

    @Test
    fun testJapaneseKanjiLyricMap() {
        val romanized = LyricsRomanizer.romanize("私の夢と愛")
        assertTrue(romanized.contains("watashi"))
        assertTrue(romanized.contains("yume"))
        assertTrue(romanized.contains("ai"))
    }

    @Test
    fun testKoreanHangulRomanization() {
        val romanized = LyricsRomanizer.romanize("사랑해")
        assertEquals("saranghae", romanized)
    }

    @Test
    fun testSongLyricFullKanjiTransliteration() {
        val line1 = LyricsRomanizer.romanize("見ないフリしていた")
        assertEquals("minaifurishiteita", line1)

        val line2 = LyricsRomanizer.romanize("期待はずれにうんざりだよ")
        assertEquals("kitaihazureniunzaridayo", line2)

        val line3 = LyricsRomanizer.romanize("自分がいちばんわかってる")
        assertEquals("jibungaichibanwakatteru", line3)

        val line4 = LyricsRomanizer.romanize("何者にもなれないって")
        assertEquals("nanimononimonarenaitte", line4)
    }
}
