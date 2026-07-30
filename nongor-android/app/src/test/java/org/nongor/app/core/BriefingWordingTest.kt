package org.nongor.app.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * What the briefing is allowed to claim about roads.
 *
 * The routing engine counts graph segments that intersect an illustrative flood polygon. That is
 * not the same as a road being blocked, and the difference is operational: a responder who reads
 * "455 blocked roads" will route around roads that are perfectly passable, in a situation where
 * the long way round costs time nobody has.
 *
 * This shipped wrong once — the offline briefing said "455 segments blocked" while the caption
 * directly beneath it on the same screen said "cross the flood layer". These tests stop it
 * coming back.
 */
class BriefingWordingTest {

    private fun stats(crossings: Int) = Summary.Stats(
        totalSos = 2,
        newSos = 2,
        critical = 1,
        high = 0,
        moderate = 0,
        low = 1,
        top5 = emptyList(),
        shortages = emptyMap(),
        shelterPressure = emptyList(),
        blockedRoads = List(crossings) { "seg$it" },
    )

    @Test fun theOfflineBriefingNeverCallsAFloodCrossingABlockedRoad() {
        val text = Summary.deterministicBriefing(stats(455)).lowercase()
        assertFalse(
            "the briefing must not claim roads are blocked: $text",
            text.contains("blocked"),
        )
    }

    @Test fun theOfflineBriefingSaysWhatTheNumberActuallyMeans() {
        val text = Summary.deterministicBriefing(stats(455))
        assertTrue("should report the count", text.contains("455"))
        assertTrue("should name the flood layer", text.contains("flood layer"))
        assertTrue(
            "should mark the layer as illustrative rather than live",
            text.contains("illustrative"),
        )
    }

    @Test fun zeroCrossingsStillReadsHonestly() {
        val text = Summary.deterministicBriefing(stats(0)).lowercase()
        assertFalse(text.contains("blocked"))
        assertTrue(text.contains("0 road segments"))
    }

    /**
     * The model is handed a JSON blob of counts. A field called "blockedRoads" is an invitation
     * to write "blocked roads" back out, which is how the overclaim reached the screen in the
     * first place — so the key itself has to describe the measurement.
     */
    @Test fun theCountsHandedToTheModelAreNamedForWhatTheyMeasure() {
        val json = Summary.statsToJson(stats(455))
        assertFalse("json key must not say blocked: $json", json.contains("blockedRoads"))
        assertTrue(json.contains("floodCrossingRoadSegments"))
    }
}
