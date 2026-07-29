package org.nongor.app.ui.mesh

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.ChevronDown
import compose.icons.feathericons.ChevronRight
import compose.icons.feathericons.MessageSquare
import org.nongor.app.core.SmsBridge
import org.nongor.app.ui.i18n.localiseDigits
import org.nongor.app.ui.i18n.tr
import org.nongor.app.ui.theme.TextSecondary

/**
 * The feature-phone bridge, on screen.
 *
 * Mesh only reaches phones running Nongor that are physically within a few hundred metres.
 * When the data network is down but the tower still carries voice and SMS — which is the
 * common case after a flood, not the rare one — this hands the same SOS to the built-in
 * messaging app as one short line of text that any handset can display.
 *
 * Nongor never sends the message itself. It fills in the SMS app and the person presses
 * send, which is why the app asks for no SMS permission at all.
 */
@Composable
fun SmsBridgeBody(
    viewModel: MeshViewModel,
    sosText: String,
    reporterName: String?,
) {
    val context = LocalContext.current
    var code by remember { mutableStateOf("") }
    var pasted by remember { mutableStateOf("") }

    val decoded = remember(pasted) { SmsBridge.decode(pasted) }

    Column(Modifier.fillMaxWidth()) {
        run {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    FeatherIcons.MessageSquare,
                    contentDescription = null,
                    modifier = Modifier.width(18.dp).height(18.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        tr("No one in range? Send it as an SMS", "কাছে কেউ নেই? এসএমএস করে পাঠান"),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        tr(
                            "Works on any phone, including button phones",
                            "যেকোনো ফোনে চলে, বাটন ফোনেও",
                        ),
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // ---- Outgoing ----
            OutlinedButton(
                onClick = {
                    viewModel.buildSmsCode(sosText, reporterName) { code = it }
                },
                enabled = sosText.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(tr("Make an SMS code from my SOS", "আমার এসওএস থেকে এসএমএস কোড বানান"))
            }

            if (code.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    code,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    localiseDigits(
                        tr(
                            "${code.length} characters · ${SmsBridge.segments(code)} SMS",
                            "${code.length} অক্ষর · ${SmsBridge.segments(code)} এসএমএস",
                        ),
                        org.nongor.app.ui.i18n.LocalBangla.current,
                    ),
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { openSmsApp(context, code) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(tr("Open messages with this filled in", "মেসেজ অ্যাপে এটি নিয়ে যান"))
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Incoming ----
            Text(
                tr("Got a code by SMS? Paste it here", "এসএমএসে কোড পেয়েছেন? এখানে পেস্ট করুন"),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = pasted,
                onValueChange = { pasted = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("NGR1 C 24.8901,91.8712 P4 F:trp") },
                maxLines = 3,
            )
            if (pasted.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                if (decoded == null) {
                    Text(
                        tr(
                            "That is not a Nongor code.",
                            "এটি নোঙরের কোড নয়।",
                        ),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            SmsBridge.describe(decoded, org.nongor.app.ui.i18n.LocalBangla.current),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        if (decoded.lat != null && decoded.lon != null) {
                            OutlinedButton(
                                onClick = { openMap(context, decoded.lat!!, decoded.lon!!) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(tr("Show this place on a map", "এই জায়গা মানচিত্রে দেখুন"))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Hand the code to whichever messaging app the user has, with no recipient filled in so
 * they choose who to send it to. ACTION_SENDTO with an `smsto:` URI needs no permission and
 * cannot send anything without an explicit press.
 */
private fun openSmsApp(context: Context, body: String) {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")).apply {
        putExtra("sms_body", body)
    }
    runCatching { context.startActivity(intent) }
}

/**
 * A `geo:` URI opens any installed map. Nothing is fetched — if the phone has no map app
 * or no data, the SMS bridge has already done its job by printing the coordinates.
 */
private fun openMap(context: Context, lat: Double, lon: Double) {
    val uri = Uri.parse("geo:$lat,$lon?q=$lat,$lon(SOS)")
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
}
