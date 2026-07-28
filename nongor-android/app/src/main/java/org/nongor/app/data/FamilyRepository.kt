package org.nongor.app.data

import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * One family member's most recent "encounter" — the last time this phone heard theirs over the
 * mesh. There is no server and no GPS network involved: two phones simply have to come within
 * radio range once, and that opportunistic meeting is what gets recorded.
 */
data class FamilyMember(
    val name: String,
    val lastSeenTs: Long,
    val lat: Double? = null,
    val lon: Double? = null,
    val hops: Int = 0,
)

/** Persisted log of family encounters, newest per member. */
class FamilyRepository(private val persistFile: File? = null) {

    private val gson = Gson()

    private val _members = MutableStateFlow<List<FamilyMember>>(emptyList())
    val members: StateFlow<List<FamilyMember>> = _members

    init {
        val f = persistFile
        if (f != null && f.exists()) {
            runCatching { gson.fromJson(f.readText(), Persisted::class.java) }.getOrNull()?.let {
                _members.value = it.members
            }
        }
    }

    private data class Persisted(val members: List<FamilyMember> = emptyList())

    /** Upsert by name, always keeping the most recent sighting. */
    fun record(m: FamilyMember) {
        val existing = _members.value.firstOrNull { it.name.equals(m.name, ignoreCase = true) }
        if (existing != null && existing.lastSeenTs >= m.lastSeenTs) return
        _members.value = (_members.value.filterNot { it.name.equals(m.name, ignoreCase = true) } + m)
            .sortedByDescending { it.lastSeenTs }
        persist()
    }

    fun clear() {
        _members.value = emptyList(); persist()
    }

    private fun persist() {
        val f = persistFile ?: return
        runCatching { f.writeText(gson.toJson(Persisted(_members.value))) }
    }
}
