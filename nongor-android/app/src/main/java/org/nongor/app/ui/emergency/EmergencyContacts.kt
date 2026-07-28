package org.nongor.app.ui.emergency

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/**
 * One official Bangladesh emergency short-code, with a bilingual label and a one-line note on when
 * to use it. These are stable government hotlines (999, 1090, 333, …), so they are bundled as
 * offline data rather than fetched at runtime — the whole point of Nongor is that it keeps working
 * with no network, exactly when floods knock out connectivity.
 *
 * Sources (verified July 2026): National Emergency Service (999.gov.bd), 333.gov.bd, and the
 * Bangladesh government helpline directory. Ordered by relevance to a flood emergency.
 */
data class EmergencyContact(
    val number: String,
    val titleEn: String,
    val titleBn: String,
    val descEn: String,
    val descBn: String,
    /** The one number every user should try first — rendered as the large primary call button. */
    val primary: Boolean = false,
)

val EMERGENCY_CONTACTS: List<EmergencyContact> = listOf(
    EmergencyContact(
        "999", "National Emergency", "জাতীয় জরুরি সেবা",
        "Police, Fire & Ambulance — one free 24/7 line", "পুলিশ, ফায়ার ও অ্যাম্বুলেন্স — একটি ফ্রি ২৪/৭ লাইন",
        primary = true,
    ),
    EmergencyContact(
        "1090", "Flood & Disaster Warning", "বন্যা ও দুর্যোগ সতর্কবার্তা",
        "Free recorded weather & flood alerts", "ফ্রি আবহাওয়া ও বন্যা সতর্কবার্তা (রেকর্ডেড)",
    ),
    EmergencyContact(
        "16111", "Coast Guard", "কোস্ট গার্ড",
        "Water rescue on rivers and coast", "নদী ও উপকূলে পানিতে উদ্ধার",
    ),
    EmergencyContact(
        "333", "Govt Help & Relief", "সরকারি সহায়তা ও ত্রাণ",
        "District help, relief and information", "জেলা সহায়তা, ত্রাণ ও তথ্য",
    ),
    EmergencyContact(
        "102", "Ambulance", "অ্যাম্বুলেন্স",
        "Medical transport", "চিকিৎসা পরিবহন",
    ),
    EmergencyContact(
        "16263", "Health Helpline", "স্বাস্থ্য হটলাইন",
        "DGHS medical advice", "ডিজিএইচএস চিকিৎসা পরামর্শ",
    ),
    EmergencyContact(
        "109", "Women & Children", "নারী ও শিশু সহায়তা",
        "Help for women and children in danger", "বিপদে নারী ও শিশুর সহায়তা",
    ),
    EmergencyContact(
        "1098", "Child Helpline", "শিশু হেল্পলাইন",
        "Support for children at risk", "ঝুঁকিতে থাকা শিশুর জন্য সহায়তা",
    ),
)

/**
 * Opens the phone dialer with [number] pre-filled. Uses ACTION_DIAL, never ACTION_CALL, so it needs
 * no CALL_PHONE permission and never places a call without the user tapping the dialer — critical
 * for an emergency tool where an accidental call must not tie up a real hotline.
 */
fun dialNumber(context: Context, number: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
    runCatching { context.startActivity(intent) }
        .onFailure {
            Toast.makeText(context, "Dial $number", Toast.LENGTH_LONG).show()
        }
}
