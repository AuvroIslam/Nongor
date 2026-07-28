package org.nongor.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Family Reunion privacy contract: what goes on the air must identify the family to its own members
 * and be meaningless to everyone else.
 */
class FamilyCryptoTest {

    @Test fun sameCodeProducesSameTag_soRelativesRecogniseEachOther() {
        assertEquals(FamilyCrypto.tag("Rahman-2026"), FamilyCrypto.tag("Rahman-2026"))
    }

    @Test fun tagIgnoresCaseAndSurroundingSpace() {
        assertEquals(FamilyCrypto.tag("Rahman-2026"), FamilyCrypto.tag("  rahman-2026 "))
    }

    @Test fun differentFamiliesGetDifferentTags() {
        assertNotEquals(FamilyCrypto.tag("Rahman-2026"), FamilyCrypto.tag("Karim-2026"))
    }

    @Test fun tagDoesNotLeakTheCode() {
        val code = "Rahman-2026"
        val tag = FamilyCrypto.tag(code)
        assert(!tag.contains("rahman", ignoreCase = true)) { "the family code must not appear on the air" }
    }

    @Test fun familyMemberCanReadTheSealedName() {
        val sealed = FamilyCrypto.seal("Rahman-2026", "Ma")
        assertEquals("Ma", FamilyCrypto.open("Rahman-2026", sealed))
    }

    @Test fun strangerWithWrongCodeCannotReadTheName() {
        val sealed = FamilyCrypto.seal("Rahman-2026", "Ma")
        assertNull("a different family must not decrypt it", FamilyCrypto.open("Karim-2026", sealed))
    }

    @Test fun garbageCiphertextFailsClosed() {
        assertNull(FamilyCrypto.open("Rahman-2026", "not-real-base64-@@@"))
        assertNull(FamilyCrypto.open("Rahman-2026", ""))
    }

    @Test fun sealIsNondeterministic_soBeaconsCannotBeCorrelated() {
        val a = FamilyCrypto.seal("Rahman-2026", "Ma")
        val b = FamilyCrypto.seal("Rahman-2026", "Ma")
        assertNotEquals("random IV means two beacons never look identical", a, b)
        assertEquals("Ma", FamilyCrypto.open("Rahman-2026", a))
        assertEquals("Ma", FamilyCrypto.open("Rahman-2026", b))
    }
}
