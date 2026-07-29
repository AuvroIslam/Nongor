package org.nongor.app.core

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.VibratorManager
import android.util.Log
import kotlin.math.PI
import kotlin.math.sin

/**
 * The loudest noise the phone can make.
 *
 * Search and rescue teams find people by *hearing* them. Someone trapped under a collapsed
 * roof or on the far side of a flooded field has a voice that gives out in minutes and a
 * torch nobody can see in daylight — but a phone can scream for hours. This is the digital
 * version of the whistle every rescue kit contains, and it is the one feature here that works
 * with no network, no GPS, no second phone and no literacy at all.
 *
 * Two design rules, both about not making things worse:
 *
 *  * **It is always stoppable, instantly.** A siren you cannot switch off is a liability —
 *    it drowns out the rescuer who is trying to talk to you, and it terrifies children.
 *  * **The volume it borrowed is given back.** [start] raises the alarm stream to maximum and
 *    [stop] restores whatever it was, so Nongor does not silently leave the phone set to
 *    deafening for the next morning's alarm.
 *
 * The tone is synthesised rather than shipped as an audio file: it costs the APK nothing, and
 * a two-tone sweep carries further through walls and water than a single pitch.
 */
class Siren(context: Context) {

    private val appContext = context.applicationContext
    private val audioManager =
        appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var track: AudioTrack? = null
    private var previousVolume: Int? = null

    var isPlaying: Boolean = false
        private set

    /**
     * Start the siren and the vibration pattern.
     *
     * Safe to call twice — a second press should not stack two tracks on top of each other.
     */
    fun start() {
        if (isPlaying) return
        runCatching {
            borrowMaxAlarmVolume()
            track = buildTrack().apply { play() }
            startVibration()
            isPlaying = true
        }.onFailure {
            Log.w(TAG, "Could not start the siren", it)
            stop()
        }
    }

    fun stop() {
        runCatching { track?.pause(); track?.flush(); track?.release() }
        track = null
        runCatching { stopVibration() }
        restoreVolume()
        isPlaying = false
    }

    fun toggle() = if (isPlaying) stop() else start()

    // ---- audio ------------------------------------------------------------------------

    /**
     * One full siren cycle, looped by the hardware.
     *
     * Alternating 800 Hz and 1200 Hz in half-second steps: the change in pitch is what makes
     * it read as an alarm rather than as background noise, and it stays audible when one of
     * the two frequencies happens to fall in a dead spot of the room.
     */
    private fun buildTrack(): AudioTrack {
        val cycleSeconds = 1.0
        val frames = (SAMPLE_RATE * cycleSeconds).toInt()
        val samples = ShortArray(frames)
        for (i in 0 until frames) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = if (t < cycleSeconds / 2) LOW_HZ else HIGH_HZ
            // A short fade at each edge stops the loop clicking on every repeat.
            val edge = minOf(i, frames - 1 - i) / (SAMPLE_RATE * 0.005)
            val gain = edge.coerceIn(0.0, 1.0)
            samples[i] = (sin(2.0 * PI * freq * t) * Short.MAX_VALUE * 0.92 * gain).toInt().toShort()
        }

        val bytes = ByteArray(samples.size * 2)
        for (i in samples.indices) {
            bytes[i * 2] = (samples[i].toInt() and 0xFF).toByte()
            bytes[i * 2 + 1] = ((samples[i].toInt() shr 8) and 0xFF).toByte()
        }

        return AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    // USAGE_ALARM so it plays through Do Not Disturb and silent mode. If the
                    // phone is on silent in a collapsed building, that is not a preference
                    // the user would want honoured.
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(bytes.size)
            .build()
            .apply {
                write(bytes, 0, bytes.size)
                setLoopPoints(0, frames, -1)   // -1 = loop forever
            }
    }

    private fun borrowMaxAlarmVolume() {
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        previousVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        runCatching { audioManager.setStreamVolume(AudioManager.STREAM_ALARM, max, 0) }
    }

    private fun restoreVolume() {
        val previous = previousVolume ?: return
        runCatching { audioManager.setStreamVolume(AudioManager.STREAM_ALARM, previous, 0) }
        previousVolume = null
    }

    // ---- vibration --------------------------------------------------------------------

    private fun vibratorManager(): VibratorManager? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        } else {
            null
        }

    /** Long-short-long, repeating — distinct from any notification pattern. */
    private fun startVibration() {
        val vm = vibratorManager() ?: return
        val pattern = longArrayOf(0, 600, 200, 600, 400)
        val effect = VibrationEffect.createWaveform(pattern, 0)
        runCatching { vm.vibrate(CombinedVibration.createParallel(effect)) }
    }

    private fun stopVibration() {
        vibratorManager()?.cancel()
    }

    private companion object {
        const val TAG = "NongorSiren"
        const val SAMPLE_RATE = 44100
        const val LOW_HZ = 800.0
        const val HIGH_HZ = 1200.0
    }
}
