package org.nongor.app.ui.mesh

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.Radio
import org.nongor.app.ui.i18n.tr
import org.nongor.app.ui.theme.TextSecondary

/** Which secondary action is open. */
enum class MeshSheet { NONE, COMPOSE, SMS }

/**
 * Write a detailed SOS.
 *
 * Kept off the main screen deliberately. The big red button is for the person who has seconds
 * and no free hand; this is for the volunteer who has a minute and wants to say *which* roof,
 * *how many* children. Mixing the two on one screen makes the urgent one harder to find.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeSosSheet(
    text: String,
    onTextChange: (String) -> Unit,
    onBroadcast: () -> Unit,
    onDismiss: () -> Unit,
) {
    val rooftop = tr(
        "Trapped on rooftop, water rising, need boat rescue.",
        "ছাদে আটকা, পানি বাড়ছে, নৌকায় উদ্ধার দরকার।",
    )
    val medical = tr(
        "Elderly man, heavy bleeding, need medic urgently.",
        "বয়স্ক ব্যক্তি, প্রচুর রক্তক্ষরণ, দ্রুত চিকিৎসক দরকার।",
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
                .navigationBarsPadding(),
        ) {
            Text(
                tr("Say exactly what is happening", "ঠিক কী ঘটছে লিখুন"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                tr(
                    "Where you are, how many people, and what you need. Short is fine.",
                    "কোথায় আছেন, কতজন আছেন, কী দরকার। ছোট হলেও চলবে।",
                ),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                label = { Text(tr("SOS message", "এসওএস বার্তা")) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4,
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = { onTextChange(rooftop) },
                    label = { Text(tr("rooftop SOS", "ছাদ এসওএস")) },
                )
                AssistChip(
                    onClick = { onTextChange(medical) },
                    label = { Text(tr("medical SOS", "চিকিৎসা এসওএস")) },
                )
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onBroadcast,
                enabled = text.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Icon(FeatherIcons.Radio, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(tr("Broadcast this SOS", "এই এসওএস সম্প্রচার করুন"))
            }
        }
    }
}

/** The feature-phone bridge, in a sheet. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsSheet(
    viewModel: MeshViewModel,
    sosText: String,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
                .navigationBarsPadding(),
        ) {
            SmsBridgeBody(
                viewModel = viewModel,
                sosText = sosText,
                reporterName = viewModel.reporterName,
            )
        }
    }
}
