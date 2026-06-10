package com.example.dosagecalc.presentation.calculator.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dosagecalc.domain.model.Reminder
import com.example.dosagecalc.domain.model.ReminderInterval
import com.example.dosagecalc.presentation.calculator.RemindersViewModel
import com.example.dosagecalc.presentation.ui.components.EmptyStateView
import com.example.dosagecalc.presentation.ui.components.GradientScreenHeader
import com.example.dosagecalc.presentation.ui.theme.LocalDosageShapes
import com.example.dosagecalc.presentation.ui.theme.spacing
import com.example.dosagecalc.presentation.utils.ReminderManager

@Composable
fun RemindersScreen(
    viewModel: RemindersViewModel,
    onNavigateBack: () -> Unit,
) {
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sp = MaterialTheme.spacing
    val cs = MaterialTheme.colorScheme

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(cs.background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            GradientScreenHeader(
                colors = listOf(cs.primary, cs.primaryContainer),
                modifier = Modifier.padding(bottom = 0.dp),
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = sp.sm),
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Indietro",
                                tint = cs.onPrimary,
                            )
                        }
                        Text(
                            text = "Calendario",
                            style = MaterialTheme.typography.titleMedium,
                            color = cs.onPrimary.copy(alpha = 0.8f),
                        )
                    }
                    Column(modifier = Modifier.padding(start = sp.xl, end = sp.xl, top = sp.xs)) {
                        Text(
                            text = "Promemoria",
                            style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Serif),
                            color = cs.onPrimary,
                        )
                        Spacer(modifier = Modifier.height(sp.sm))
                        Text(
                            text = "Gestisci i tuoi allarmi attivi",
                            style = MaterialTheme.typography.bodyMedium,
                            color = cs.onPrimary.copy(alpha = 0.9f),
                        )
                        Spacer(modifier = Modifier.height(sp.xl))
                    }
                }
            }

            if (reminders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyStateView(
                        icon = Icons.Default.AlarmOff,
                        title = "Nessun promemoria",
                        subtitle = "Crea un promemoria dalla schermata risultato dopo aver calcolato una dose",
                    )
                }
            } else {
                val paired = reminders.chunked(2)

                LazyColumn(
                    contentPadding =
                        PaddingValues(
                            horizontal = sp.base,
                            vertical = sp.xl,
                        ),
                    verticalArrangement = Arrangement.spacedBy(sp.md),
                ) {
                    itemsIndexed(paired) { rowIndex, pair ->
                        val featuredFirst = rowIndex % 2 == 0

                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Max),
                            horizontalArrangement = Arrangement.spacedBy(sp.md),
                        ) {
                            ReminderBentoCard(
                                reminder = pair[0],
                                featured = featuredFirst,
                                context = context,
                                viewModel = viewModel,
                                modifier =
                                    Modifier
                                        .weight(if (featuredFirst) 1.35f else 1f)
                                        .fillMaxHeight(),
                            )

                            if (pair.size > 1) {
                                ReminderBentoCard(
                                    reminder = pair[1],
                                    featured = !featuredFirst,
                                    context = context,
                                    viewModel = viewModel,
                                    modifier =
                                        Modifier
                                            .weight(if (!featuredFirst) 1.35f else 1f)
                                            .fillMaxHeight(),
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(if (!featuredFirst) 1.35f else 1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderBentoCard(
    reminder: Reminder,
    featured: Boolean,
    context: android.content.Context,
    viewModel: RemindersViewModel,
    modifier: Modifier = Modifier,
) {
    val sp = MaterialTheme.spacing
    val cs = MaterialTheme.colorScheme
    val shapes = LocalDosageShapes.current

    var showDeleteDialog by remember { mutableStateOf(false) }

    val timeString = String.format(java.util.Locale.getDefault(), "%02d:%02d", reminder.hour, reminder.minute)
    val frequencyText =
        when (reminder.interval) {
            ReminderInterval.DAILY -> "Giornaliero"
            ReminderInterval.WEEKLY -> "Sett. · G${reminder.daySelection}"
            ReminderInterval.MONTHLY -> "Mens. · G${reminder.daySelection}"
        }

    val bgColor =
        if (featured) {
            cs.secondaryContainer.copy(alpha = 0.75f)
        } else {
            cs.surfaceVariant.copy(alpha = 0.8f)
        }
    val timeColor = if (featured) cs.secondary else cs.onSurface
    val timeFontSize = if (featured) 44.sp else 34.sp
    val chipBg = if (featured) cs.secondary.copy(alpha = 0.13f) else cs.surface

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Elimina Promemoria") },
            text = { Text("Vuoi eliminare il promemoria per ${reminder.drugName} alle $timeString?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        ReminderManager.cancelReminderSeries(context, reminder.id)
                        viewModel.deleteReminder(reminder.id)
                        Toast.makeText(context, "Promemoria cancellato", Toast.LENGTH_SHORT).show()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = cs.error),
                ) { Text("Elimina") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Annulla") }
            },
        )
    }

    Card(
        modifier = modifier,
        shape = shapes.cardLarge,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border =
            BorderStroke(
                0.5.dp,
                if (featured) cs.secondary.copy(alpha = 0.3f) else cs.outlineVariant.copy(alpha = 0.35f),
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(sp.base),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = timeString,
                    style =
                        MaterialTheme.typography.displayMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = timeFontSize,
                            lineHeight = timeFontSize * 1.05f,
                        ),
                    color = timeColor,
                )

                Spacer(modifier = Modifier.height(sp.xs))

                Text(
                    text = reminder.drugName,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = if (featured) cs.onSecondaryContainer else cs.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Column {
                Surface(shape = shapes.chip, color = chipBg) {
                    Text(
                        text = frequencyText,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (featured) cs.secondary else cs.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = sp.sm, vertical = 3.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = { showDeleteDialog = true },
                        shape = shapes.chip,
                        colors = ButtonDefaults.textButtonColors(contentColor = cs.error),
                        contentPadding = PaddingValues(horizontal = sp.sm, vertical = 4.dp),
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Elimina",
                            modifier = Modifier.size(13.dp),
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Elimina", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
