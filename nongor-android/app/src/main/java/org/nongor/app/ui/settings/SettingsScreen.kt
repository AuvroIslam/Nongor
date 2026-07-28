package org.nongor.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.nongor.app.data.AppPrefs
import org.nongor.app.ui.components.NongorScaffold
import org.nongor.app.ui.components.NoteBlock
import org.nongor.app.ui.components.SectionCard
import org.nongor.app.ui.i18n.t
import org.nongor.app.ui.theme.NongorColors

@Composable
fun SettingsScreen(prefs: AppPrefs, onBack: () -> Unit) {
    val bangla by prefs.bangla.collectAsState()
    val name by prefs.displayName.collectAsState()

    NongorScaffold(title = t("Settings", "সেটিংস"), onBack = onBack) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(t("Bangla", "বাংলা"), style = MaterialTheme.typography.titleMedium)
                        Text(
                            t("Show the whole app in Bangla", "পুরো অ্যাপ বাংলায় দেখান"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(checked = bangla, onCheckedChange = { prefs.setBangla(it) })
                }
            }

            SectionCard {
                Text(t("Your name", "আপনার নাম"), style = MaterialTheme.typography.titleMedium)
                Text(
                    t(
                        "Attached to SOS and area reports you send, so rescuers know who to look for.",
                        "আপনার পাঠানো SOS ও এলাকা রিপোর্টে যুক্ত হয়, যাতে উদ্ধারকারীরা জানে কাকে খুঁজতে হবে।",
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { prefs.setDisplayName(it.take(40)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    placeholder = { Text(t("e.g. Rahim, Ward 3", "যেমন রহিম, ওয়ার্ড ৩")) },
                )
            }

            SectionCard(accent = NongorColors.Safe) {
                Text(t("Privacy", "গোপনীয়তা"), style = MaterialTheme.typography.titleMedium)
                Text(
                    t(
                        "Nongor has no account, no server and no analytics. Reports, your name and " +
                            "your mesh key stay in this app's storage on this phone. Nothing is uploaded.",
                        "নোঙরে কোনো অ্যাকাউন্ট, সার্ভার বা অ্যানালিটিক্স নেই। রিপোর্ট, আপনার নাম ও মেশ কী " +
                            "শুধু এই ফোনেই থাকে। কিছুই আপলোড হয় না।",
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            NoteBlock(
                t(
                    "Nongor is a preparedness tool, not a replacement for 999 or a doctor. " +
                        "If a call can get through, call.",
                    "নোঙর প্রস্তুতির জন্য, ৯৯৯ বা ডাক্তারের বিকল্প নয়। কল করা গেলে কল করুন।",
                ),
                color = NongorColors.Caution,
            )
        }
    }
}
