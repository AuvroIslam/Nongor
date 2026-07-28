package org.nongor.app.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Nongor is bilingual everywhere, not just on a settings page: the people worst hit by a
 * flood read Bangla, and the volunteers coordinating them often read English.
 *
 * Rather than maintaining a key/value resource table (which drifts the moment one language
 * gets a new string), both languages sit side by side at the call site:
 *
 *     Text(t("Safe shelter", "নিরাপদ আশ্রয়"))
 *
 * A missing translation is then impossible to ship — it would not compile.
 */
val LocalBangla = staticCompositionLocalOf { false }

@Composable
@ReadOnlyComposable
fun t(en: String, bn: String): String = if (LocalBangla.current) bn else en

/** Non-composable variant for view models and engines. */
fun tr(bangla: Boolean, en: String, bn: String): String = if (bangla) bn else en

/**
 * Bangla-Indic digits. Numbers matter in a crisis (doses, capacities, distances) and a
 * Bangla reader should not have to parse Latin digits, but the *value* must never change —
 * this is a pure glyph substitution, never a rounding or formatting step.
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
