package org.nongor.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The family-presence handshake, end to end without a radio.
 *
 * Two phones that agreed a code must derive the same tag and be able to read each other's
 * sealed names; a phone with any other code must be able to do neither.
 */
class FamilyPresenceTest {

    @Test fun twoPhonesWithTheSameCodeDeriveTheSameTag() {
        assertEquals(FamilyCrypto.tag("1234"), FamilyCrypto.tag("1234"))
    }

    @Test fun tagIgnoresSurroundingSpaceAndCase() {
        // One relative types it with a trailing space, another in capitals. Same family.
        assertEquals(FamilyCrypto.tag("1234"), FamilyCrypto.tag(" 1234 "))
        assertEquals(FamilyCrypto.tag("abcd"), FamilyCrypto.tag("ABCD"))
    }

    @Test fun differentCodesAreDifferentFamilies() {
        assertNotEquals(FamilyCrypto.tag("1234"), FamilyCrypto.tag("1235"))
    }

    @Test fun tagLeaksNothingAboutTheCode() {
        val tag = FamilyCrypto.tag("1234")
        assertTrue("the tag must not contain the code itself", !tag.contains("1234"))
        assertEquals("stable length regardless of code", 16, tag.length)
        assertEquals(16, FamilyCrypto.tag("a-very-long-family-code-indeed").length)
    }

    @Test fun aRelativeCanReadTheSealedName() {
        val sealed = FamilyCrypto.seal("1234", "Rahim")
        assertEquals("Rahim", FamilyCrypto.open("1234", sealed))
    }

    @Test fun aStrangerCannotReadTheSealedName() {
        val sealed = FamilyCrypto.seal("1234", "Rahim")
        assertNull("wrong code must not decrypt", FamilyCrypto.open("9999", sealed))
    }

    @Test fun sealingTwiceProducesDifferentCiphertext() {
        // A fresh IV each time, so someone watching the air cannot tell that the same phone
        // re-broadcast the same name.
        val a = FamilyCrypto.seal("1234", "Rahim")
        val b = FamilyCrypto.seal("1234", "Rahim")
        assertNotEquals(a, b)
        assertEquals("Rahim", FamilyCrypto.open("1234", a))
        assertEquals("Rahim", FamilyCrypto.open("1234", b))
    }

    @Test fun banglaNamesSurviveTheRoundTrip() {
        val sealed = FamilyCrypto.seal("1234", "রহিম")
        assertEquals("রহিম", FamilyCrypto.open("1234", sealed))
    }

    @Test fun garbageDoesNotCrashTheReceiver() {
        assertNull(FamilyCrypto.open("1234", ""))
        assertNull(FamilyCrypto.open("1234", "not-base64!!"))
        assertNull(FamilyCrypto.open("1234", "c2hvcnQ="))   // valid base64, shorter than the IV
    }

    /**
     * The full exchange as the hub performs it: phone A seals, phone B checks the tag then
     * opens. This is the path that decides whether a relative appears on the radar at all.
     */
    @Test fun theFullHandshakeBetweenTwoRelatives() {
        val code = "1234"

        // Phone A broadcasts.
        val airTag = FamilyCrypto.tag(code)
        val airName = FamilyCrypto.seal(code, "Maruf")

        // Phone B, same family.
        assertEquals("B must recognise the family", FamilyCrypto.tag(code), airTag)
        assertEquals("Maruf", FamilyCrypto.open(code, airName))

        // Phone C, a stranger in radio range.
        assertNotEquals(FamilyCrypto.tag("5678"), airTag)
        assertNull(FamilyCrypto.open("5678", airName))
    }
}
