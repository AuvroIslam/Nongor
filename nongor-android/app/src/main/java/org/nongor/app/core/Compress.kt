package org.nongor.app.core

/**
 * Radio-uplink compression — Kotlin port of nongor/core/compress.py (MeshGemma-inspired).
 * Gemma squeezes many incidents into a <=200-byte digest; a deterministic builder guarantees the bound.
 */
object Compress {

    private val priShort = mapOf("critical" to "c", "high" to "h", "moderate" to "m", "low" to "l")
    private val rank = mapOf("critical" to 0, "high" to 1, "moderate" to 2, "low" to 3)

    private data class Rec(val i: String, val p: String, val l: String)

    private fun records(sos: List<SosReport>, triage: List<TriageResult>): List<Rec> {
        val byId = sos.associateBy { it.id }
        return triage.sortedWith(compareBy({ rank[it.priority] }, { -it.urgencyScore })).map { t ->
            val s = byId[t.msgId]
            val loc = if (s?.lat != null) "%.2f,%.2f".format(s.lat, s.lon) else ""
            Rec(t.msgId.take(8), priShort[t.priority] ?: "l", loc)
        }
    }

    private fun recJson(r: Rec) = "{\"i\":\"${r.i}\",\"p\":\"${r.p}\",\"l\":\"${r.l}\"}"

    private fun bytes(s: String) = s.toByteArray(Charsets.UTF_8).size

    fun buildRadioPayload(sos: List<SosReport>, triage: List<TriageResult>, maxBytes: Int = 200): String {
        val recs = records(sos, triage)
        val crit = triage.count { it.priority == "critical" }
        val high = triage.count { it.priority == "high" }
        var top = recs.take(3)
        while (true) {
            val blob = "{\"n\":${triage.size},\"c\":$crit,\"h\":$high,\"t\":[" +
                top.joinToString(",") { recJson(it) } + "]}"
            if (bytes(blob) <= maxBytes || top.isEmpty()) return blob
            top = top.dropLast(1)
        }
    }

    data class RadioResult(val payload: String, val bytes: Int, val ok: Boolean, val producedBy: String)

    fun compressForRadio(
        sos: List<SosReport>, triage: List<TriageResult>, gemma: LlmEngine? = null, maxBytes: Int = 200,
    ): RadioResult {
        val fallback = buildRadioPayload(sos, triage, maxBytes)
        if (gemma == null) return RadioResult(fallback, bytes(fallback), true, "deterministic")

        val user = "[" + records(sos, triage).joinToString(",") { recJson(it) } + "]"
        repeat(2) {
            val raw = gemma.generate(Prompts.COMPRESS_SYSTEM, user, temperature = 0.2, maxTokens = 200)
            val start = raw.indexOf('{'); val end = raw.lastIndexOf('}')
            if (start != -1 && end > start) {
                val blob = raw.substring(start, end + 1)
                if (bytes(blob) <= maxBytes && looksLikeJson(blob))
                    return RadioResult(blob, bytes(blob), true, "gemma")
            }
        }
        return RadioResult(fallback, bytes(fallback), true, "fallback")
    }

    private fun looksLikeJson(s: String): Boolean = try {
        com.google.gson.JsonParser.parseString(s).isJsonObject
    } catch (_: Exception) { false }
}
