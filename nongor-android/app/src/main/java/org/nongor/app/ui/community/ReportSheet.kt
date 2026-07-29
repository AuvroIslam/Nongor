package org.nongor.app.ui.community

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.Radio
import org.nongor.app.data.CommunityKinds
import org.nongor.app.ui.i18n.LocalBangla
import org.nongor.app.ui.i18n.tr
import org.nongor.app.ui.theme.ShapeMd
import org.nongor.app.ui.theme.ShapeSm
import org.nongor.app.ui.theme.TextSecondary

/**
 * Posting a report, in a sheet.
 *
 * This used to be a permanent card pinned to the top of the board, which meant the thing you
 * came to *read* started halfway down the screen. Reporting is a deliberate act — you reach
 * for it — so it belongs behind the button, and the board gets the whole screen.
 *
 * The type is picked as a tile rather than a dropdown because tapping a picture is something
 * you can do one-handed, in the rain, without reading.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReportSheet(
    canPost: Boolean,
    onPost: (kind: String, note: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val bangla = LocalBangla.current
    var selected by remember { mutableStateOf(CommunityKinds.ALL.first().id) }
    var note by remember { mutableStateOf("") }

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
                tr("Report what you see", "যা দেখছেন জানান"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                tr(
                    "It spreads to nearby phones over the mesh. No internet is used.",
                    "এটি মেশের মাধ্যমে কাছের ফোনে ছড়িয়ে পড়ে। ইন্টারনেট লাগে না।",
                ),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )

            Spacer(Modifier.height(16.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CommunityKinds.ALL.forEach { kind ->
                    val style = kindStyle(kind.id)
                    val isSelected = kind.id == selected
                    Row(
                        Modifier
                            .clip(ShapeSm)
                            .background(if (isSelected) style.bg else MaterialTheme.colorScheme.surface)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) style.fg else MaterialTheme.colorScheme.outline,
                                shape = ShapeSm,
                            )
                            .clickable { selected = kind.id }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            style.icon,
                            contentDescription = null,
                            tint = style.fg,
                            modifier = Modifier.size(17.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (bangla) kind.bn else kind.en,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) style.fg else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }

            // The note is required. A board full of bare tags — "danger", "danger", "danger" —
            // tells a rescuer nothing they can act on; the sentence is the report.
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                shape = ShapeMd,
                minLines = 3,
                label = { Text(tr("What is happening?", "কী ঘটছে?")) },
                placeholder = {
                    Text(
                        tr(
                            "Which road, which shelter, how many people",
                            "কোন রাস্তা, কোন আশ্রয়, কতজন মানুষ",
                        ),
                    )
                },
                supportingText = {
                    Text(
                        tr(
                            "Needed — one sentence a stranger could act on.",
                            "লাগবেই — এমন একটি বাক্য যাতে অচেনা কেউ কাজ করতে পারে।",
                        ),
                    )
                },
            )

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onPost(selected, note); note = "" },
                enabled = canPost && note.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = ShapeMd,
            ) {
                Icon(FeatherIcons.Radio, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(tr("Share with nearby phones", "কাছের ফোনে শেয়ার করুন"))
            }
            if (!canPost) {
                Spacer(Modifier.height(8.dp))
                Text(
                    tr(
                        "Waiting for the mesh radio to start…",
                        "মেশ রেডিও চালু হওয়ার অপেক্ষায়…",
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            } else if (note.isBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    tr(
                        "Write what you can see before sharing.",
                        "শেয়ার করার আগে যা দেখছেন তা লিখুন।",
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            Box(Modifier.height(4.dp))
        }
    }
}
