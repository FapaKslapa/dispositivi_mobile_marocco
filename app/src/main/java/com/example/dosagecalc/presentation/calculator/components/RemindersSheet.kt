package com.example.dosagecalc.presentation.calculator.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dosagecalc.domain.model.Reminder
import com.example.dosagecalc.domain.model.ReminderInterval
import com.example.dosagecalc.presentation.calculator.RemindersViewModel
import com.example.dosagecalc.presentation.ui.components.PillButton
import com.example.dosagecalc.presentation.ui.components.RoundedTextField
import com.example.dosagecalc.presentation.ui.theme.LocalDosageShapes
import com.example.dosagecalc.presentation.ui.theme.spacing
import com.example.dosagecalc.presentation.ui.util.isCompactHeight
import com.example.dosagecalc.presentation.utils.ReminderManager
import java.util.Locale
import java.util.UUID

@Composable
fun RemindersSheet(
    context: Context,
    drugName: String?,
    onDismissRequest: () -> Unit,
    viewModel: RemindersViewModel = hiltViewModel(),
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val isCompact = isCompactHeight()
    val shapes = LocalDosageShapes.current
    val sp = MaterialTheme.spacing
    val cs = MaterialTheme.colorScheme

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = cs.surface,
        shape = shapes.sheet,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = sp.xl),
        ) {
            ActiveRemindersSection(reminders = reminders, context = context, viewModel = viewModel)

            Spacer(modifier = Modifier.height(sp.lg))

            Column(modifier = Modifier.padding(horizontal = sp.xl)) {
                Text(
                    text = "Nuovo Promemoria",
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Serif),
                    color = cs.onSurface,
                )
                Spacer(modifier = Modifier.height(sp.base))

                if (drugName == null) {
                    Surface(
                        shape = shapes.card,
                        color = cs.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "Esegui prima un calcolo per un farmaco.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = cs.onSurfaceVariant,
                            modifier = Modifier.padding(sp.lg),
                        )
                    }
                } else {
                    var selectedInterval by remember { mutableStateOf(ReminderInterval.DAILY) }
                    val timePickerState = rememberTimePickerState(initialHour = 8, initialMinute = 0, is24Hour = true)
                    var durationDays by remember { mutableStateOf("5") }
                    var daySelection by remember { mutableStateOf("1") }

                    Text(
                        text = "Frequenza",
                        style = MaterialTheme.typography.labelLarge,
                        color = cs.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(sp.sm))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(sp.sm),
                    ) {
                        ReminderInterval.entries.forEach { interval ->
                            val isSelected = selectedInterval == interval
                            val label =
                                when (interval) {
                                    ReminderInterval.DAILY -> "Giornaliero"
                                    ReminderInterval.WEEKLY -> "Settimanale"
                                    ReminderInterval.MONTHLY -> "Mensile"
                                }
                            Surface(
                                shape = shapes.chip,
                                color = if (isSelected) cs.primary else cs.surfaceVariant,
                                border =
                                    BorderStroke(
                                        1.dp,
                                        if (isSelected) cs.primary else cs.outlineVariant,
                                    ),
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .clickable { selectedInterval = interval },
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) cs.onPrimary else cs.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = sp.sm, horizontal = 2.dp),
                                    maxLines = 1,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(sp.base))

                    Text(
                        text = "Orario della somministrazione",
                        style = MaterialTheme.typography.labelLarge,
                        color = cs.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(sp.sm))

                    val timePickerColors =
                        TimePickerDefaults.colors(
                            clockDialSelectedContentColor = cs.onPrimary,
                            selectorColor = cs.primary,
                            timeSelectorSelectedContainerColor = cs.primaryContainer,
                            timeSelectorSelectedContentColor = cs.onPrimaryContainer,
                            periodSelectorSelectedContainerColor = cs.primaryContainer,
                            periodSelectorSelectedContentColor = cs.onPrimaryContainer,
                        )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = shapes.card,
                        colors =
                            CardDefaults.cardColors(
                                containerColor = cs.surfaceVariant.copy(alpha = 0.7f),
                            ),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(sp.base),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isCompact) {
                                TimeInput(state = timePickerState, colors = timePickerColors)
                            } else {
                                TimePicker(state = timePickerState, colors = timePickerColors)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(sp.base))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(sp.base),
                    ) {
                        RoundedTextField(
                            value = durationDays,
                            onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) durationDays = it },
                            label = {
                                Text(
                                    when (selectedInterval) {
                                        ReminderInterval.DAILY -> "Durata (Giorni)"
                                        ReminderInterval.WEEKLY -> "Durata (Settimane)"
                                        ReminderInterval.MONTHLY -> "Durata (Mesi)"
                                    },
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = shapes.card,
                        )
                        if (selectedInterval == ReminderInterval.MONTHLY) {
                            RoundedTextField(
                                value = daySelection,
                                onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) daySelection = it },
                                label = { Text("Giorno (1-31)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = shapes.card,
                            )
                        }
                    }

                    if (selectedInterval == ReminderInterval.WEEKLY) {
                        Spacer(modifier = Modifier.height(sp.base))
                        Text(
                            text = "Giorno della settimana",
                            style = MaterialTheme.typography.labelLarge,
                            color = cs.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(sp.sm))
                        val daysOfWeek = listOf("L", "M", "M", "G", "V", "S", "D")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            daysOfWeek.forEachIndexed { index, day ->
                                val dayNumber = index + 1
                                val isSelected = (daySelection.toIntOrNull() ?: 1) == dayNumber
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { daySelection = dayNumber.toString() },
                                    label = { Text(day, style = MaterialTheme.typography.bodyMedium) },
                                    shape = shapes.chip,
                                    colors =
                                        FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = cs.primaryContainer,
                                            selectedLabelColor = cs.onPrimaryContainer,
                                        ),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(sp.lg))
                    PillButton(
                        onClick = {
                            val seriesId = UUID.randomUUID().toString()
                            val dur = durationDays.toIntOrNull() ?: 1
                            val ds = daySelection.toIntOrNull() ?: 1

                            val reminder =
                                Reminder(
                                    id = seriesId,
                                    drugName = drugName,
                                    interval = selectedInterval,
                                    daySelection = if (selectedInterval == ReminderInterval.DAILY) 0 else ds,
                                    hour = timePickerState.hour,
                                    minute = timePickerState.minute,
                                    durationDays = dur,
                                )
                            viewModel.addReminder(reminder)
                            ReminderManager.scheduleReminder(
                                context = context,
                                reminder = reminder,
                            )
                            Toast.makeText(context, "Promemoria impostato!", Toast.LENGTH_SHORT).show()
                            onDismissRequest()
                        },
                        label = "Salva Promemoria",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveRemindersSection(
    reminders: List<Reminder>,
    context: Context,
    viewModel: RemindersViewModel,
) {
    val shapes = LocalDosageShapes.current
    val sp = MaterialTheme.spacing
    val cs = MaterialTheme.colorScheme

    Column(modifier = Modifier.padding(horizontal = sp.xl)) {
        Text(
            text = "Promemoria Attivi",
            style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Serif),
            color = cs.onSurface,
        )
        Spacer(modifier = Modifier.height(sp.sm))
    }

    if (reminders.isEmpty()) {
        Surface(
            shape = shapes.card,
            color = cs.surfaceVariant.copy(alpha = 0.45f),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = sp.xl),
        ) {
            Text(
                text = "Nessun promemoria attivo.",
                style = MaterialTheme.typography.bodyMedium,
                color = cs.onSurfaceVariant,
                modifier = Modifier.padding(sp.lg),
            )
        }
    } else {
        LazyRow(
            contentPadding = PaddingValues(horizontal = sp.xl),
            horizontalArrangement = Arrangement.spacedBy(sp.base),
        ) {
            items(reminders, key = { it.id }) { reminder ->
                ReminderCard(reminder = reminder, context = context, viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun ReminderCard(
    reminder: Reminder,
    context: Context,
    viewModel: RemindersViewModel,
) {
    val shapes = LocalDosageShapes.current
    val sp = MaterialTheme.spacing
    val cs = MaterialTheme.colorScheme

    val timeString = String.format(Locale.US, "%02d:%02d", reminder.hour, reminder.minute)
    val intervalText =
        when (reminder.interval) {
            ReminderInterval.DAILY -> "Giornaliero"
            ReminderInterval.WEEKLY -> "Sett. · G${reminder.daySelection}"
            ReminderInterval.MONTHLY -> "Mens. · G${reminder.daySelection}"
        }

    Card(
        modifier = Modifier.width(180.dp),
        shape = shapes.card,
        colors =
            CardDefaults.cardColors(
                containerColor = cs.secondaryContainer.copy(alpha = 0.5f),
            ),
        border = BorderStroke(0.5.dp, cs.secondary.copy(alpha = 0.25f)),
    ) {
        Column(modifier = Modifier.padding(sp.base)) {
            Surface(shape = shapes.chip, color = cs.secondary.copy(alpha = 0.13f)) {
                Text(
                    text = timeString,
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Medium,
                        ),
                    color = cs.secondary,
                    modifier = Modifier.padding(horizontal = sp.sm, vertical = 4.dp),
                )
            }
            Spacer(modifier = Modifier.height(sp.xs))
            Text(
                text = reminder.drugName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = cs.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(sp.xs))
            Surface(shape = shapes.chip, color = cs.surface) {
                Text(
                    text = intervalText,
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = sp.sm, vertical = 3.dp),
                )
            }
            Spacer(modifier = Modifier.height(sp.sm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(
                    onClick = {
                        ReminderManager.cancelReminderSeries(context, reminder.id)
                        viewModel.deleteReminder(reminder.id)
                        Toast.makeText(context, "Promemoria eliminato", Toast.LENGTH_SHORT).show()
                    },
                    shape = shapes.chip,
                    colors = ButtonDefaults.textButtonColors(contentColor = cs.error),
                    contentPadding = PaddingValues(horizontal = sp.sm, vertical = 4.dp),
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Elimina",
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Elimina", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
