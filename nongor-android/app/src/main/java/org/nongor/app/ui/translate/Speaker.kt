package org.nongor.app.ui.translate

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

/**
 * Reads a phrase out loud using whatever offline voice the phone already has.
 *
 * Speech is strictly a bonus here. Many cheap handsets have no Bangla voice installed and
 * none of the minority languages have one at all, so nothing in the conversation flow may
 * depend on this — [available] simply goes false and the screen keeps working silently.
 */
class Speaker(context: Context) {

    private var tts: TextToSpeech? = null
    private var banglaOk = false
    private var englishOk = false

    var available: Boolean = false
        private set

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val engine = tts ?: return@TextToSpeech
                banglaOk = engine.isLanguageAvailable(BANGLA) >= TextToSpeech.LANG_AVAILABLE
                englishOk = engine.isLanguageAvailable(Locale.UK) >= TextToSpeech.LANG_AVAILABLE
                available = banglaOk || englishOk
            } else {
                Log.i(TAG, "No text-to-speech engine on this device; staying silent.")
            }
        }
    }

    /** True when this exact language can be spoken — the button hides otherwise. */
    fun canSpeak(bangla: Boolean): Boolean = if (bangla) banglaOk else englishOk

    fun speak(text: String, bangla: Boolean) {
        val engine = tts ?: return
        if (!canSpeak(bangla)) return
        engine.language = if (bangla) BANGLA else Locale.UK
        engine.setSpeechRate(0.9f)
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "nongor")
    }

    fun stop() {
        tts?.stop()
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        available = false
    }

    private companion object {
        const val TAG = "NongorSpeaker"
        val BANGLA: Locale = Locale("bn", "BD")
    }
}
