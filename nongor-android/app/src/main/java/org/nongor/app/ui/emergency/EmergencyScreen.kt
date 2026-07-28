package org.nongor.app.ui.emergency

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import compose.icons.feathericons.Phone
import compose.icons.feathericons.PhoneCall
import compose.icons.feathericons.Radio
import org.nongor.app.ui.i18n.tr
import org.nongor.app.ui.theme.BrandBlue
import org.nongor.app.ui.theme.BgCard
import org.nongor.app.ui.theme.TextPrimary
import org.nongor.app.ui.theme.TextSecondary
import org.nongor.app.ui.theme.ShapeMd
import androidx.compose.foundation.BorderStroke
import org.nongor.app.ui.theme.GlassBorder
import org.nongor.app.ui.theme.Stroke
import org.nongor.app.ui.theme.ErrorRed

private val EmergencyRed = ErrorRed
private val EmergencyRedSoft = Color(0xFFFDECEA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(onBack: () -> Unit, onMesh: () -> Unit = {}) {
    val context = LocalContext.current
    val primary = EMERGENCY_CONTACTS.first { it.primary }
    val others = EMERGENCY_CONTACTS.filter { !it.primary }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("Emergency Numbers", "জরুরি নম্বর")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(FeatherIcons.ArrowLeft, contentDescription = "Back")
                    }
                },
            )
        },
    ) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp).padding(top = 6.dp, bottom = 24.dp),
        ) {
            // ---- Primary: big Call 999 button ----
            Card(
                modifier = Modifier.fillMaxWidth()
                    .clickable { dialNumber(context, primary.number) },
                colors = CardDefaults.cardColors(containerColor = EmergencyRed),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(Stroke.hairline, GlassBorder),
                shape = ShapeMd,
            ) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(56.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center) {
                        Icon(FeatherIcons.PhoneCall, null, tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(tr("Call 999", "৯৯৯ কল করুন"), color = Color.White,
                            fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, lineHeight = 26.sp)
                        Text(primary.let { tr(it.descEn, it.descBn) },
                            color = Color.White.copy(alpha = 0.92f),
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                tr("Tap a number to open your dialer — you confirm the call.",
                    "নম্বরে চাপ দিলে ডায়ালার খুলবে — কল আপনি নিশ্চিত করবেন।"),
                color = TextSecondary, style = MaterialTheme.typography.bodySmall,
            )

            // ---- Curated list, most useful in a flood first ----
            Spacer(Modifier.height(18.dp))
            Text(tr("Helpful in a flood", "বন্যায় কাজে লাগে"), color = TextPrimary,
                fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))
            others.forEach { c ->
                ContactRow(
                    number = c.number,
                    title = tr(c.titleEn, c.titleBn),
                    desc = tr(c.descEn, c.descBn),
                    onCall = { dialNumber(context, c.number) },
                )
                Spacer(Modifier.height(10.dp))
            }

            // ---- Honest caveat: a call still needs a working phone signal ----
            Spacer(Modifier.height(8.dp))
            Card(
                Modifier.fillMaxWidth().clickable(onClick = onMesh),
                colors = CardDefaults.cardColors(containerColor = BrandBlue.copy(alpha = 0.08f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(Stroke.hairline, GlassBorder),
                shape = ShapeMd,
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(BrandBlue.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center) {
                        Icon(FeatherIcons.Radio, null, tint = BrandBlue, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(tr("No signal to call?", "কল করার সিগন্যাল নেই?"), color = TextPrimary,
                            fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text(
                            tr("A phone call needs a mobile network. If it's down, use Mesh SOS to reach nearby phones.",
                                "কলের জন্য মোবাইল নেটওয়ার্ক লাগে। বন্ধ থাকলে কাছের ফোনে পৌঁছাতে মেশ এসওএস ব্যবহার করুন।"),
                            color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Text(
                tr("Official Bangladesh government hotlines. Most are toll-free.",
                    "বাংলাদেশ সরকারের অফিসিয়াল হটলাইন। বেশিরভাগ টোল-ফ্রি।"),
                color = TextSecondary, style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun ContactRow(number: String, title: String, desc: String, onCall: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onCall),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(Stroke.hairline, GlassBorder),
        shape = ShapeMd,
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(58.dp).clip(ShapeMd).background(EmergencyRedSoft),
                contentAlignment = Alignment.Center) {
                Text(number, color = EmergencyRed, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall)
                Text(desc, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.width(10.dp))
            Box(Modifier.size(42.dp).clip(CircleShape).background(EmergencyRed),
                contentAlignment = Alignment.Center) {
                Icon(FeatherIcons.Phone, contentDescription = "Call $number",
                    tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}
