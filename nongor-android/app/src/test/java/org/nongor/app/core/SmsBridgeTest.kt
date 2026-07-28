package org.nongor.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The SMS bridge is the one path that reaches a phone Nongor is not installed on, so the
 * wire format is pinned here: a code produced by one build must still decode on another.
 */
class SmsBridgeTest {

    @Test
    fun `round trips every field`() {
        val code = SmsBridge.encode(
            priority = "critical",
            lat = 24.8901,
            lon = 91.8712,
            peopleCount = 4,
            signals = listOf("trapped", "heavy_bleeding"),
            name = "Rahim",
        )
        val back = SmsBridge.decode(code)!!
        assertEquals("critical", back.priority)
        assertEquals(24.8901, back.lat!!, 1e-6)
        assertEquals(91.8712, back.lon!!, 1e-6)
        assertEquals(4, back.peopleCount)
        assertEquals(listOf("trapped", "heavy_bleeding"), back.signals)
        assertEquals("Rahim", back.name)
    }

    @Test
    fun `a full sos still fits in one sms`() {
        val code = SmsBridge.encode(
            priority = "critical",
            lat = 24.8901,
            lon = 91.8712,
            peopleCount = 12,
            signals = RISK_SIGNALS,
            name = "Abdul Karim",
        )
        assertTrue("was ${code.length} chars: $code", code.length <= 160)
        assertEquals(1, SmsBridge.segments(code))
    }

    /** Anything non-Latin flips the whole SMS to UCS-2 and costs more segments. */
    @Test
    fun `encoding stays gsm7 even when the name is bangla`() {
        val code = SmsBridge.encode("high", 24.0, 91.0, 2, emptyList(), name = "রহিম")
        assertTrue(SmsBridge.isGsm7(code))
        assertEquals(1, SmsBridge.segments(code))
    }

    @Test
    fun `segment counting matches the gsm and ucs2 limits`() {
        assertEquals(0, SmsBridge.segments(""))
        assertEquals(1, SmsBridge.segments("a".repeat(160)))
        assertEquals(2, SmsBridge.segments("a".repeat(161)))
        assertEquals(1, SmsBridge.segments("রহিম"))
        assertEquals(2, SmsBridge.segments("র".repeat(71)))
    }

    /** Forwarded messages arrive wrapped in other people's text. */
    @Test
    fun `finds the code inside a forwarded message`() {
        val raw = "Fwd from Karim bhai:\nNGR1 H 22.3569,91.7832 P3 F:chd\nplease send boat"
        val back = SmsBridge.decode(raw)!!
        assertEquals("high", back.priority)
        assertEquals(3, back.peopleCount)
        assertEquals(listOf("child"), back.signals)
    }

    @Test
    fun `unrelated text decodes to nothing rather than a guess`() {
        assertNull(SmsBridge.decode("water is rising near the school, send help"))
        assertNull(SmsBridge.decode(""))
    }

    @Test
    fun `a code with no location is still usable`() {
        val code = SmsBridge.encode("moderate", null, null, 2, listOf("no_food_water"))
        val back = SmsBridge.decode(code)!!
        assertNull(back.lat)
        assertEquals(2, back.peopleCount)
        assertEquals(listOf("no_food_water"), back.signals)
    }

    /** A corrupted coordinate must not put a rescue team in the Atlantic. */
    @Test
    fun `impossible coordinates are dropped not trusted`() {
        val back = SmsBridge.decode("NGR1 C 999.0,-999.0 P1")!!
        assertNull(back.lat)
        assertNull(back.lon)
        assertEquals("critical", back.priority)
    }

    @Test
    fun `an unknown priority letter is rejected outright`() {
        assertNull(SmsBridge.decode("NGR1 Z 24.0,91.0 P1"))
    }

    @Test
    fun `unknown signal codes are ignored and the rest survives`() {
        val back = SmsBridge.decode("NGR1 H 24.0,91.0 P2 F:trp,zzz,chd")!!
        assertEquals(listOf("trapped", "child"), back.signals)
    }

    /** Every signal the triage engine can emit must survive the round trip. */
    @Test
    fun `every risk signal has a distinct code`() {
        val codes = RISK_SIGNALS.map { signal ->
            val back = SmsBridge.decode(SmsBridge.encode("low", null, null, 1, listOf(signal)))!!
            assertEquals(listOf(signal), back.signals)
            back.signals.first()
        }
        assertEquals(RISK_SIGNALS.size, codes.toSet().size)
    }

    @Test
    fun `people count never decodes below one`() {
        val code = SmsBridge.encode("low", null, null, 0, emptyList())
        assertEquals(1, SmsBridge.decode(code)!!.peopleCount)
    }

    @Test
    fun `describe reads back the values it was given`() {
        val decoded = SmsBridge.Decoded("critical", 24.8901, 91.8712, 4, listOf("trapped"), "Rahim")
        val text = SmsBridge.describe(decoded, bangla = false)
        assertTrue(text.contains("critical"))
        assertTrue(text.contains("Rahim"))
        assertTrue(text.contains("4"))
        assertTrue(text.contains("24.8901"))
        assertNotNull(SmsBridge.describe(decoded, bangla = true))
    }
}
