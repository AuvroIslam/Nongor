package org.nongor.app.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Small persisted app settings shared across screens: interface language and which offline
 * region packs are installed / active. Backed by SharedPreferences, exposed as StateFlows so
 * Compose recomposes when they change.
 */
class AppPrefs(context: Context) {

    private val sp = context.applicationContext.getSharedPreferences("nongor_prefs", Context.MODE_PRIVATE)

    private val _language = MutableStateFlow(sp.getString(KEY_LANG, "en") ?: "en")
    val language: StateFlow<String> = _language.asStateFlow()
    val isBangla: Boolean get() = _language.value == "bn"

    private val _installedRegions = MutableStateFlow(
        sp.getStringSet(KEY_INSTALLED, setOf(DEFAULT_REGION))?.toSet() ?: setOf(DEFAULT_REGION),
    )
    val installedRegions: StateFlow<Set<String>> = _installedRegions.asStateFlow()

    private val _activeRegion = MutableStateFlow(sp.getString(KEY_ACTIVE, DEFAULT_REGION) ?: DEFAULT_REGION)
    val activeRegion: StateFlow<String> = _activeRegion.asStateFlow()

    /**
     * Whether the intro has been seen at all.
     *
     * Kept separate from "is the model downloaded", because the optional AI model must never
     * gate the app: someone who declines the 2.5 GB download has made a valid choice and
     * should land straight on the home screen every launch after that, not be asked again
     * during an emergency.
     */
    private val _introSeen = MutableStateFlow(sp.getBoolean(KEY_INTRO_SEEN, false))
    val introSeen: StateFlow<Boolean> = _introSeen.asStateFlow()

    fun markIntroSeen() {
        _introSeen.value = true
        sp.edit().putBoolean(KEY_INTRO_SEEN, true).apply()
    }

    // First-run coaching balloon on Home — shown once, then dismissed for good.
    private val _coachSeen = MutableStateFlow(sp.getBoolean(KEY_COACH_SEEN, false))
    val coachSeen: StateFlow<Boolean> = _coachSeen.asStateFlow()

    fun markCoachSeen() {
        _coachSeen.value = true
        sp.edit().putBoolean(KEY_COACH_SEEN, true).apply()
    }

    // Family Reunion: the shared family code (never broadcast in clear) + this phone's member name.
    private val _familyCode = MutableStateFlow(sp.getString(KEY_FAMILY_CODE, "") ?: "")
    val familyCode: StateFlow<String> = _familyCode.asStateFlow()

    private val _memberName = MutableStateFlow(sp.getString(KEY_MEMBER_NAME, "") ?: "")
    val memberName: StateFlow<String> = _memberName.asStateFlow()

    fun setFamily(code: String, name: String) {
        _familyCode.value = code.trim()
        _memberName.value = name.trim()
        sp.edit().putString(KEY_FAMILY_CODE, code.trim())
            .putString(KEY_MEMBER_NAME, name.trim()).apply()
    }

    fun clearFamily() {
        _familyCode.value = ""; _memberName.value = ""
        sp.edit().remove(KEY_FAMILY_CODE).remove(KEY_MEMBER_NAME).apply()
    }

    fun setLanguage(lang: String) {
        _language.value = lang
        sp.edit().putString(KEY_LANG, lang).apply()
    }

    fun installRegion(id: String) {
        val next = _installedRegions.value + id
        _installedRegions.value = next
        sp.edit().putStringSet(KEY_INSTALLED, next).apply()
        setActiveRegion(id)
    }

    fun setActiveRegion(id: String) {
        _activeRegion.value = id
        sp.edit().putString(KEY_ACTIVE, id).apply()
    }

    companion object {
        const val DEFAULT_REGION = "chattogram"
        private const val KEY_LANG = "language"
        private const val KEY_INSTALLED = "installed_regions"
        private const val KEY_ACTIVE = "active_region"
        private const val KEY_COACH_SEEN = "coach_seen"
        private const val KEY_INTRO_SEEN = "intro_seen"
        private const val KEY_FAMILY_CODE = "family_code"
        private const val KEY_MEMBER_NAME = "member_name"
    }
}
