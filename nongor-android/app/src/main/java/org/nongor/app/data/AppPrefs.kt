package org.nongor.app.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Device-local settings. Deliberately SharedPreferences and not a datastore migration
 * dance: this file must be readable the instant the process starts, because the home
 * screen has to render in Bangla on a cold launch during an emergency.
 */
class AppPrefs private constructor(context: Context) {

    private val sp: SharedPreferences =
        context.applicationContext.getSharedPreferences("nongor_prefs", Context.MODE_PRIVATE)

    private val _bangla = MutableStateFlow(sp.getBoolean(KEY_BANGLA, true))
    val bangla: StateFlow<Boolean> = _bangla

    private val _onboarded = MutableStateFlow(sp.getBoolean(KEY_ONBOARDED, false))
    val onboarded: StateFlow<Boolean> = _onboarded

    private val _displayName = MutableStateFlow(sp.getString(KEY_NAME, "") ?: "")
    val displayName: StateFlow<String> = _displayName

    private val _familyCode = MutableStateFlow(sp.getString(KEY_FAMILY, "") ?: "")
    val familyCode: StateFlow<String> = _familyCode

    private val _highContrast = MutableStateFlow(sp.getBoolean(KEY_CONTRAST, false))
    val highContrast: StateFlow<Boolean> = _highContrast

    fun setBangla(value: Boolean) {
        sp.edit().putBoolean(KEY_BANGLA, value).apply()
        _bangla.value = value
    }

    fun setOnboarded(value: Boolean) {
        sp.edit().putBoolean(KEY_ONBOARDED, value).apply()
        _onboarded.value = value
    }

    fun setDisplayName(value: String) {
        sp.edit().putString(KEY_NAME, value).apply()
        _displayName.value = value
    }

    fun setFamilyCode(value: String) {
        sp.edit().putString(KEY_FAMILY, value).apply()
        _familyCode.value = value
    }

    fun setHighContrast(value: Boolean) {
        sp.edit().putBoolean(KEY_CONTRAST, value).apply()
        _highContrast.value = value
    }

    companion object {
        private const val KEY_BANGLA = "bangla"
        private const val KEY_ONBOARDED = "onboarded"
        private const val KEY_NAME = "display_name"
        private const val KEY_FAMILY = "family_code"
        private const val KEY_CONTRAST = "high_contrast"

        @Volatile
        private var instance: AppPrefs? = null

        fun get(context: Context): AppPrefs =
            instance ?: synchronized(this) {
                instance ?: AppPrefs(context).also { instance = it }
            }
    }
}
