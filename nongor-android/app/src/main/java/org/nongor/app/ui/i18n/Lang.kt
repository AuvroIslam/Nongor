package org.nongor.app.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf

/** True when the app language is Bangla. Provided at the root from persisted app prefs. */
val LocalBangla = compositionLocalOf { false }

/** Pick the Bangla or English string for the current language. */
@Composable
@ReadOnlyComposable
fun tr(en: String, bn: String): String = if (LocalBangla.current) bn else en

/** Short alias for [tr]. Reads better inline in dense screens. */
@Composable
@ReadOnlyComposable
fun t(en: String, bn: String): String = tr(en, bn)

/** Non-composable variant, for view models and engines that already know the language. */
fun pick(bangla: Boolean, en: String, bn: String): String = if (bangla) bn else en

/**
 * Bangla-Indic digits. Numbers matter in a crisis — doses, capacities, distances, people
 * counts — and a Bangla reader should not have to parse Latin digits. This is a pure glyph
 * substitution: it never rounds, reformats or otherwise changes the value.
 */
private val BN_DIGITS = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

fun localiseDigits(text: String, bangla: Boolean): String {
    if (!bangla) return text
    val out = StringBuilder(text.length)
    for (c in text) out.append(if (c in '0'..'9') BN_DIGITS[c - '0'] else c)
    return out.toString()
}

@Composable
@ReadOnlyComposable
fun num(value: Any): String = localiseDigits(value.toString(), LocalBangla.current)
