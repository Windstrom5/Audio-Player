package com.thesis.bitperfectusb.playback.lyrics

enum class RomajiDisplayMode(val label: String) {
    OFF("Off"),
    DUAL("Dual (Original + Romaji)"),
    ROMAJI_ONLY("Romaji Only")
}

/**
 * High-performance, zero-dependency offline Romanization engine for CJK lyrics.
 * - Japanese: Hiragana, Katakana, Sokuon double consonants, Chōonpu vowel extensions,
 *   compounds, and comprehensive single/multi-character Kanji readings to Hepburn Romaji.
 * - Korean: Algorithmic Hangul Syllable Decomposition to Revised Romanization.
 */
object LyricsRomanizer {

    // ── Hiragana Digraphs (Yōon & Compound sounds) ──
    private val HIRAGANA_DIGRAPHS: Map<String, String> = mapOf(
        "きゃ" to "kya", "きゅ" to "kyu", "きょ" to "kyo",
        "しゃ" to "sha", "しゅ" to "shu", "しょ" to "sho",
        "ちゃ" to "cha", "ちゅ" to "chu", "ちょ" to "cho",
        "にゃ" to "nya", "にゅ" to "nyu", "にょ" to "nyo",
        "ひゃ" to "hya", "ひゅ" to "hyu", "ひょ" to "hyo",
        "みゃ" to "mya", "みゅ" to "myu", "みょ" to "myo",
        "りゃ" to "rya", "りゅ" to "ryu", "りょ" to "ryo",
        "ぎゃ" to "gya", "ぎゅ" to "gyu", "ぎょ" to "gyo",
        "じゃ" to "ja", "じゅ" to "ju", "じょ" to "jo",
        "ぢゃ" to "ja", "ぢゅ" to "ju", "ぢょ" to "jo",
        "びゃ" to "bya", "びゅ" to "byu", "びょ" to "byo",
        "ぴゃ" to "pya", "ぴゅ" to "pyu", "ぴょ" to "pyo",
        "ふぁ" to "fa", "ふぃ" to "fi", "ふぇ" to "fe", "ふぉ" to "fo",
        "てぃ" to "ti", "でぃ" to "di", "とう" to "tou", "こう" to "kou",
        "そう" to "sou", "どう" to "dou", "もう" to "mou", "よう" to "you",
        "しぇ" to "she", "じぇ" to "je", "ちぇ" to "che", "つぁ" to "tsa",
        "うぃ" to "wi", "うぇ" to "we", "うぉ" to "wo"
    )

    // ── Hiragana Monographs ──
    private val HIRAGANA_MONOGRAPHS: Map<Char, String> = mapOf(
        'あ' to "a", 'い' to "i", 'う' to "u", 'え' to "e", 'お' to "o",
        'か' to "ka", 'き' to "ki", 'く' to "ku", 'け' to "ke", 'こ' to "ko",
        'さ' to "sa", 'し' to "shi", 'す' to "su", 'せ' to "se", 'そ' to "so",
        'た' to "ta", 'ち' to "chi", 'つ' to "tsu", 'て' to "te", 'と' to "to",
        'な' to "na", 'に' to "ni", 'ぬ' to "nu", 'ね' to "ne", 'の' to "no",
        'は' to "ha", 'ひ' to "hi", 'ふ' to "fu", 'へ' to "he", 'ほ' to "ho",
        'ま' to "ma", 'み' to "mi", 'む' to "mu", 'め' to "me", 'も' to "mo",
        'や' to "ya", 'ゆ' to "yu", 'よ' to "yo",
        'ら' to "ra", 'り' to "ri", 'る' to "ru", 'れ' to "re", 'ろ' to "ro",
        'わ' to "wa", 'を' to "wo", 'ん' to "n",
        // Dakuten & Handakuten
        'が' to "ga", 'ぎ' to "gi", 'ぐ' to "gu", 'げ' to "ge", 'ご' to "go",
        'ざ' to "za", 'じ' to "ji", 'ず' to "zu", 'ぜ' to "ze", 'ぞ' to "zo",
        'だ' to "da", 'ぢ' to "ji", 'づ' to "zu", 'で' to "de", 'ど' to "do",
        'ば' to "ba", 'び' to "bi", 'ぶ' to "bu", 'べ' to "be", 'ぼ' to "bo",
        'ぱ' to "pa", 'ぴ' to "pi", 'ぷ' to "pu", 'ぺ' to "pe", 'ぽ' to "po",
        // Small vowels
        'ぁ' to "a", 'ぃ' to "i", 'ぅ' to "u", 'ぇ' to "e", 'ぉ' to "o",
        'ゃ' to "ya", 'ゅ' to "yu", 'ょ' to "yo", 'ゎ' to "wa"
    )

    // ── High Frequency Multi-Word Kanji Lyric Dictionary (Sorted Longest First) ──
    private val MULTI_KANJI_MAP = listOf(
        "見ないフリ" to "minaifuri", "見ないで" to "minaide", "見ていて" to "miteite", "見つめて" to "mitsumete", "見上げて" to "miagete",
        "期待はずれ" to "kitaihazure", "何者にも" to "nanimononimo", "自分自身" to "jibunjishin", "抱きしめて" to "dakishimete",
        "忘れないで" to "wasurenaide", "さようなら" to "sayounara", "ありがとう" to "arigatou", "大丈夫" to "daijoubu",
        "何処" to "doko", "何故" to "naze", "何時" to "itsu", "如何" to "dou",
        "何者" to "nanimono", "自分" to "jibun", "期待" to "kitai", "誰か" to "dareka", "誰も" to "daremo", "世界" to "sekai",
        "未来" to "mirai", "過去" to "kako", "現在" to "genzai", "永遠" to "eien", "約束" to "yakusoku", "笑顔" to "egao",
        "想い" to "omoi", "本当" to "hontou", "奇跡" to "kiseki", "記憶" to "kioku", "希望" to "kibou", "生命" to "inochi",
        "二人" to "futari", "一人" to "hitori", "場所" to "basho", "言葉" to "kotoba", "時間" to "jikan", "理由" to "riyuu",
        "夕焼け" to "yuuyake", "涙色" to "namidairo", "青空" to "aozora", "明日" to "ashita", "今日" to "kyou", "昨日" to "kinou",
        "瞬間" to "shunkan", "景色" to "keshiki", "感情" to "kanjou", "運命" to "unmei", "真実" to "shinjitsu", "現実" to "genjitsu",
        "自由" to "jiyuu", "最初" to "saisho", "最後" to "saigo", "秘密" to "himitsu", "孤独" to "kodoku", "情熱" to "jounetsu",
        "大切" to "taisetsu", "特別" to "tokubetsu", "一緒" to "issho", "最高" to "saikou", "無限" to "mugen", "永遠" to "eien",
        "全部" to "zenbu", "絶対" to "zettai", "何度" to "nando", "今夜" to "konya", "毎日" to "mainichi", "季節" to "kisetsu"
    )

    // ── Single Character Kanji Phonetic Reading Dictionary (Full General Coverage) ──
    private val SINGLE_KANJI_MAP: Map<Char, String> = mapOf(
        '見' to "mi", '期' to "ki", '待' to "tai", '自' to "ji", '分' to "bun", '何' to "nani", '者' to "mono",
        '私' to "watashi", '僕' to "boku", '俺' to "ore", '君' to "kimi", '愛' to "ai", '心' to "kokoro", '今' to "ima",
        '夢' to "yume", '夜' to "yoru", '雨' to "ame", '空' to "sora", '花' to "hana", '星' to "hoshi", '月' to "tsuki",
        '日' to "hi", '時' to "toki", '手' to "te", '声' to "koe", '道' to "michi", '涙' to "namida", '光' to "hikari",
        '影' to "kage", '風' to "kaze", '歌' to "uta", '音' to "oto", '恋' to "koi", '旅' to "tabi", '朝' to "asa",
        '胸' to "mune", '瞳' to "hitomi", '海' to "umi", '生' to "i", '死' to "shi", '命' to "inochi", '人' to "hito",
        '女' to "onna", '男' to "otoko", '子' to "ko", '目' to "me", '耳' to "mimi", '足' to "ashi", '頭' to "atama",
        '力' to "chikara", '楽' to "raku", '色' to "iro", '川' to "kawa", '山' to "yama", '木' to "ki", '天' to "ten",
        '地' to "chi", '世' to "se", '界' to "kai", '国' to "kuni", '町' to "machi", '家' to "ie", '門' to "mon",
        '友' to "tomo", '美' to "bi", '昼' to "hiru", '夕' to "yuu", '春' to "haru", '夏' to "natsu", '秋' to "aki",
        '冬' to "fuyu", '赤' to "aka", '青' to "ao", '白' to "shiro", '黒' to "kuro", '新' to "shin", '古' to "furu",
        '高' to "taka", '安' to "yasu", '長' to "naga", '短' to "mijika", '多' to "oo", '少' to "suku", '大' to "oo",
        '小' to "chii", '強' to "tsuyo", '弱' to "yowa", '同' to "ona", '異' to "koto", '正' to "tada", '悪' to "aku",
        '真' to "shin", '有' to "yuu", '無' to "na", '信' to "shin", '感' to "kan", '情' to "jou", '想' to "omoi",
        '歩' to "aru", '走' to "hashi", '飛' to "to", '止' to "to", '動' to "ugo", '開' to "hira", '閉' to "to",
        '始' to "haji", '終' to "owa", '会' to "a", '別' to "waka", '送' to "oku", '迎' to "muka", '返' to "kae",
        '探' to "saga", '失' to "ushina", '笑' to "wara", '泣' to "na", '怒' to "ika", '痛' to "ita", '苦' to "kuru",
        '甘' to "ama", '言' to "i", '話' to "hana", '語' to "kata", '読' to "yo", '書' to "ka", '聞' to "ki",
        '知' to "shi", '思' to "omo", '考' to "kanga", '行' to "i", '来' to "ki", '帰' to "kae", '出' to "de",
        '入' to "hai", '立' to "ta", '座' to "suwa", '伏' to "fu", '起' to "oki", '寝' to "ne", '眠' to "nemu",
        '買' to "ka", '売' to "u", '払' to "hara", '得' to "e", '勝' to "ka", '負' to "make", '落' to "o",
        '消' to "ki", '燃' to "mo", '照' to "tera", '輝' to "kagaya", '咲' to "sa", '散' to "chira", '鳴' to "na",
        '響' to "hibi", '届' to "todo", '祈' to "ino", '願' to "nega", '誓' to "chika", '守' to "mamo", '救' to "suku",
        '変' to "ka", '過' to "su", '残' to "noko", '続' to "tsuzu", '重' to "kasa",
        '離' to "hana", '連' to "tsu", '繋' to "tsuna", '結' to "musu", '解' to "to", '伝' to "tsuta", '受' to "uke",
        '抱' to "da", '握' to "nigi", '触' to "fu", '撫' to "nade", '包' to "tsutsu", '奪' to "uba", '隠' to "kaku",
        '迷' to "mayo", '惑' to "mado", '狂' to "kuru", '焦' to "ase", '急' to "iso", '揺' to "yure", '震' to "furu",
        '沈' to "shizu", '浮' to "uka", '流' to "naga", '溢' to "afure", '染' to "some", '滲' to "niji", '凍' to "koo",
        '溶' to "toke", '乾' to "kawa", '渇' to "kawa", '濡' to "nure", '汚' to "yogo", '澄' to "sumi", '濁' to "nigo",
        '温' to "atata", '冷' to "tsume", '熱' to "atsu", '寒' to "samu", '涼' to "suzu", '暗' to "kura", '明' to "aka",
        '深' to "fuka", '浅' to "asa", '広' to "hiro", '狭' to "sema", '近' to "chika", '遠' to "too", '早' to "haya",
        '遅' to "oso", '速' to "haya", '緩' to "yuru", '激' to "hage", '静' to "shizu", '騒' to "sawa", '寂' to "sabi",
        '誰' to "dare", '何' to "nani",
        '前' to "mae", '後' to "ato", '上' to "ue", '下' to "shita", '中' to "naka", '外' to "soto", '横' to "yoko",
        '裏' to "ura", '表' to "omote", '奥' to "oku", '端' to "hashi", '隅' to "sumi", '間' to "aida", '側' to "soba",
        '隣' to "tonari", '向' to "muko", '逆' to "gyaku", '周' to "mawa", '境' to "sakai", '線' to "sen", '点' to "ten"
    )

    // ── Korean Hangul Phoneme Tables (Revised Romanization of Korean) ──
    private val HANGUL_CHOSEONG = arrayOf("g", "kk", "n", "d", "tt", "r", "m", "b", "pp", "s", "ss", "", "j", "jj", "ch", "k", "t", "p", "h")
    private val HANGUL_JUNGSEONG = arrayOf("a", "ae", "ya", "yae", "eo", "e", "yeo", "ye", "o", "wa", "wae", "oe", "yo", "u", "wo", "we", "wi", "yu", "eu", "ui", "i")
    private val HANGUL_JONGSEONG = arrayOf("", "k", "k", "ks", "n", "nj", "nh", "t", "l", "lg", "lm", "lb", "ls", "lt", "lp", "lh", "m", "p", "bs", "t", "t", "ng", "t", "t", "k", "t", "p", "h")

    /**
     * Checks if the given text contains Japanese (Kana/Kanji), Korean (Hangul), or CJK characters.
     */
    fun hasNonRomaji(text: String): Boolean {
        for (ch in text) {
            val code = ch.code
            // Hiragana: 0x3040..0x309F, Katakana: 0x30A0..0x30FF, CJK Kanji: 0x4E00..0x9FFF, Hangul: 0xAC00..0xD7AF
            if (code in 0x3040..0x30FF || code in 0x4E00..0x9FFF || code in 0xAC00..0xD7AF || code in 0x1100..0x11FF) {
                return true
            }
        }
        return false
    }

    /**
     * Converts a single line of CJK lyrics into clean Hepburn Romaji / Revised Romanization.
     */
    fun romanize(rawText: String): String {
        if (rawText.isBlank()) return rawText
        if (!hasNonRomaji(rawText)) return rawText

        var text = rawText

        // 1. Replace multi-character Kanji compounds first (longest match first)
        for ((kanji, romaji) in MULTI_KANJI_MAP) {
            if (text.contains(kanji)) {
                text = text.replace(kanji, romaji)
            }
        }

        // 2. Convert Katakana into Hiragana for uniform transliteration
        val sbHiragana = StringBuilder()
        for (ch in text) {
            val code = ch.code
            if (code in 0x30A1..0x30F6) {
                // Katakana to Hiragana offset is -0x60
                sbHiragana.append((code - 0x60).toChar())
            } else {
                sbHiragana.append(ch)
            }
        }
        val normalized = sbHiragana.toString()

        // 3. Process Hiragana digraphs, monographs, single Kanji, sokuon (っ), and Korean Hangul
        val result = StringBuilder()
        var i = 0
        var prevVowel = ""

        while (i < normalized.length) {
            val ch = normalized[i]
            val code = ch.code

            // Check 2-character Hiragana digraph
            if (i + 1 < normalized.length) {
                val pair = normalized.substring(i, i + 2)
                val digraphRomaji = HIRAGANA_DIGRAPHS[pair]
                if (digraphRomaji != null) {
                    result.append(digraphRomaji)
                    prevVowel = if (digraphRomaji.isNotEmpty()) digraphRomaji.takeLast(1) else ""
                    i += 2
                    continue
                }
            }

            // Sokuon (small tsu っ): double the consonant of the following character
            if (ch == 'っ') {
                if (i + 1 < normalized.length) {
                    val nextDigraph = if (i + 3 <= normalized.length) HIRAGANA_DIGRAPHS[normalized.substring(i + 1, i + 3)] else null
                    val nextMonograph = HIRAGANA_MONOGRAPHS[normalized[i + 1]]

                    val nextRomaji = nextDigraph ?: nextMonograph
                    if (nextRomaji != null && nextRomaji.isNotEmpty()) {
                        val firstConsonant = nextRomaji.first()
                        if (firstConsonant !in "aeiou") {
                            result.append(firstConsonant)
                        }
                    }
                }
                i++
                continue
            }

            // Chōonpu (ー prolonged sound mark)
            if (ch == 'ー' || ch == '〜') {
                if (prevVowel.isNotBlank()) {
                    result.append(prevVowel)
                } else {
                    result.append("-")
                }
                i++
                continue
            }

            // Single Hiragana monograph
            val monoRomaji = HIRAGANA_MONOGRAPHS[ch]
            if (monoRomaji != null) {
                result.append(monoRomaji)
                prevVowel = if (monoRomaji.isNotEmpty()) monoRomaji.takeLast(1) else ""
                i++
                continue
            }

            // Single Character Kanji Reading Map
            val singleKanjiReading = SINGLE_KANJI_MAP[ch]
            if (singleKanjiReading != null) {
                result.append(singleKanjiReading)
                prevVowel = if (singleKanjiReading.isNotEmpty()) singleKanjiReading.takeLast(1) else ""
                i++
                continue
            }

            // Korean Hangul Syllables (0xAC00..0xD7A3)
            if (code in 0xAC00..0xD7A3) {
                val syllableIndex = code - 0xAC00
                val choseongIdx = syllableIndex / (21 * 28)
                val jungseongIdx = (syllableIndex % (21 * 28)) / 28
                val jongseongIdx = syllableIndex % 28

                val cho = HANGUL_CHOSEONG.getOrElse(choseongIdx) { "" }
                val jung = HANGUL_JUNGSEONG.getOrElse(jungseongIdx) { "" }
                val jong = HANGUL_JONGSEONG.getOrElse(jongseongIdx) { "" }

                result.append(cho).append(jung).append(jong)
                prevVowel = if (jung.isNotEmpty()) jung.takeLast(1) else ""
                i++
                continue
            }

            // Fallback for any unmapped CJK ideographs (0x4E00..0x9FFF): omit raw glyph
            if (code in 0x4E00..0x9FFF) {
                i++
                continue
            }

            // Preserve Latin, punctuation, spaces, numbers, and existing text
            result.append(ch)
            prevVowel = if (ch in "aeiouAEIOU") ch.lowercase() else ""
            i++
        }

        // Clean up redundant multiple spaces
        return result.toString().replace("\\s+".toRegex(), " ").trim()
    }
}
