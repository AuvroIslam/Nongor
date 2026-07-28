package org.nongor.app.core

/**
 * The feature-phone bridge.
 *
 * After a flood the data network is usually the first thing to go and the last to come back,
 * but voice and SMS often keep working on the same tower — and half the handsets in a haor
 * village are button phones that will never run this app at all.
 *
 * So an SOS can also leave Nongor as a plain text message: a single line, short enough for
 * one SMS, that a person can read with their own eyes on any phone made in the last twenty
 * years, and that another Nongor phone can paste back in to recover the exact coordinates
 * and priority. No app, no data, no account on the far end.
 *
 *     NGR1 C 24.8901,91.8712 P4 F:trp,bld N:Rahim
 *
 * Pure Kotlin with no Android types, so the wire format is pinned by unit tests.
 */
object SmsBridge {

    const val MAGIC = "NGR1"

    /**
     * Risk signals shortened to three letters.
     *
     * A fixed table rather than a truncation rule: truncating would silently collide
     * (`chronic_illness` and `child` both start "chi") and an SOS that says the wrong thing
     * is worse than one that says less.
     */
    private val SIGNAL_CODE = mapOf(
        "not_breathing" to "brt",
        "unconscious" to "unc",
        "heavy_bleeding" to "bld",
        "severe_injury" to "inj",
        "trapped" to "trp",
        "rising_water" to "wtr",
        "child" to "chd",
        "elderly" to "eld",
        "pregnant" to "prg",
        "chronic_illness" to "ill",
        "no_food_water" to "fdw",
        "medication_needed" to "med",
    )

    private val CODE_SIGNAL = SIGNAL_CODE.entries.associate { (k, v) -> v to k }

    private val PRIORITY_CODE = mapOf(
        "critical" to "C", "high" to "H", "moderate" to "M", "low" to "L",
    )

    private val CODE_PRIORITY = PRIORITY_CODE.entries.associate { (k, v) -> v to k }

    data class Decoded(
        val priority: String,
        val lat: Double?,
        val lon: Double?,
        val peopleCount: Int,
        val signals: List<String>,
        val name: String?,
    )

    /**
     * Build the one-line message.
     *
     * Coordinates are cut to four decimals — about 11 m, which is far tighter than anyone
     * can search a flooded field anyway, and it saves characters that the signal list needs.
     */
    fun encode(
        priority: String,
        lat: Double?,
        lon: Double?,
        peopleCount: Int,
        signals: List<String>,
        name: String? = null,
    ): String {
        val parts = mutableListOf(MAGIC, PRIORITY_CODE[priority] ?: "L")
        if (lat != null && lon != null) {
            parts += "%.4f,%.4f".format(lat, lon)
        } else {
            parts += "?"
        }
        parts += "P${peopleCount.coerceAtLeast(1)}"

        val codes = signals.mapNotNull { SIGNAL_CODE[it] }.distinct()
        if (codes.isNotEmpty()) parts += "F:" + codes.joinToString(",")

        // The name is ASCII-only on purpose: a single Bangla character would flip the whole
        // message to UCS-2 encoding and cut the SMS from 160 characters to 70.
        val ascii = name?.filter { it.code in 32..126 }?.trim()?.take(16)
        if (!ascii.isNullOrBlank()) parts += "N:$ascii"

        return parts.joinToString(" ")
    }

    /**
     * Read a code back.
     *
     * Tolerant by design: messages get forwarded with "fwd:" prefixes, quoted replies and
     * chat noise wrapped around them, so this looks for the magic word anywhere in the text
     * and reads to the end of that line. Returns null rather than guessing when the message
     * is not one of ours.
     */
    fun decode(raw: String): Decoded? {
        val start = raw.indexOf(MAGIC)
        if (start < 0) return null
        val line = raw.substring(start).substringBefore('\n').trim()
        val tokens = line.split(' ').filter { it.isNotBlank() }
        if (tokens.size < 2 || tokens[0] != MAGIC) return null

        val priority = CODE_PRIORITY[tokens[1]] ?: return null

        var lat: Double? = null
        var lon: Double? = null
        var people = 1
        var signals = emptyList<String>()
        var name: String? = null

        for (token in tokens.drop(2)) {
            when {
                token.startsWith("P") && token.drop(1).all { it.isDigit() } ->
                    people = token.drop(1).toIntOrNull() ?: 1

                token.startsWith("F:") ->
                    signals = token.removePrefix("F:").split(',')
                        .mapNotNull { CODE_SIGNAL[it.trim()] }

                token.startsWith("N:") ->
                    name = token.removePrefix("N:").ifBlank { null }

                token.contains(',') -> {
                    val (a, b) = token.split(',', limit = 2).let { it[0] to it.getOrElse(1) { "" } }
                    val pLat = a.toDoubleOrNull()
                    val pLon = b.toDoubleOrNull()
                    // Reject anything off the planet rather than dropping a rescue team on it.
                    if (pLat != null && pLon != null &&
                        pLat in -90.0..90.0 && pLon in -180.0..180.0
                    ) {
                        lat = pLat
                        lon = pLon
                    }
                }
            }
        }
        return Decoded(priority, lat, lon, people, signals, name)
    }

    /** Characters GSM-7 can carry. Anything else forces the message into UCS-2. */
    private val GSM7_EXTRA = "@£\$¥èéùìòÇØøÅåΔ_ΦΓΛΩΠΨΣΘΞÆæßÉ !\"#¤%&'()*+,-./:;<=>?¡ÄÖÑÜ§¿äöñüà\n\r"

    fun isGsm7(text: String): Boolean = text.all { c ->
        c in 'A'..'Z' || c in 'a'..'z' || c in '0'..'9' || c in GSM7_EXTRA
    }

    /**
     * How many SMS this text will actually cost to send.
     *
     * Worth showing the user: a message with one Bangla character costs three times as many
     * segments as the same message in Latin script, and on a prepaid balance during a
     * disaster that difference is real money.
     */
    fun segments(text: String): Int {
        if (text.isEmpty()) return 0
        val gsm = isGsm7(text)
        val single = if (gsm) 160 else 70
        val multi = if (gsm) 153 else 67
        return if (text.length <= single) 1 else (text.length + multi - 1) / multi
    }

    /** Render a decoded code as something a responder can read. */
    fun describe(decoded: Decoded, bangla: Boolean): String {
        val sb = StringBuilder()
        sb.append(
            if (bangla) "অগ্রাধিকার: ${priorityBn(decoded.priority)}" else "Priority: ${decoded.priority}",
        )
        decoded.name?.let { sb.append(if (bangla) "\nনাম: $it" else "\nName: $it") }
        sb.append(if (bangla) "\nমানুষ: ${decoded.peopleCount}" else "\nPeople: ${decoded.peopleCount}")
        if (decoded.lat != null && decoded.lon != null) {
            sb.append("\n%.4f, %.4f".format(decoded.lat, decoded.lon))
        } else {
            sb.append(if (bangla) "\nঅবস্থান জানা নেই" else "\nNo location in message")
        }
        if (decoded.signals.isNotEmpty()) {
            sb.append(
                if (bangla) "\nঝুঁকি: " else "\nSignals: ",
            ).append(decoded.signals.joinToString(", "))
        }
        return sb.toString()
    }

    private fun priorityBn(priority: String): String = when (priority) {
        "critical" -> "সংকটাপন্ন"
        "high" -> "জরুরি"
        "moderate" -> "মাঝারি"
        else -> "কম"
    }
}
