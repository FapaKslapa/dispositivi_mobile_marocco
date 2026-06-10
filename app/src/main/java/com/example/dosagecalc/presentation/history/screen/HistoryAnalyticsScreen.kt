package com.example.dosagecalc.presentation.history.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dosagecalc.domain.model.Patient
import com.example.dosagecalc.presentation.calculator.CalculatorViewModel
import com.example.dosagecalc.presentation.history.HistoryViewModel
import com.example.dosagecalc.presentation.history.components.CategoryDistributionCard
import com.example.dosagecalc.presentation.history.components.DoseTrendChart
import com.example.dosagecalc.presentation.history.components.FilterSection
import com.example.dosagecalc.presentation.history.components.InfoNote
import com.example.dosagecalc.presentation.history.components.StatsSummaryCard
import com.example.dosagecalc.presentation.ui.components.GradientScreenHeader
import com.example.dosagecalc.presentation.ui.theme.LocalDosageShapes
import com.example.dosagecalc.presentation.ui.theme.LocalElevation
import com.example.dosagecalc.presentation.ui.theme.spacing
import com.example.dosagecalc.presentation.ui.util.isCompactHeight
import com.example.dosagecalc.presentation.ui.util.isExpandedWidth

@Composable
fun HistoryAnalyticsScreen(
    historyViewModel: HistoryViewModel = hiltViewModel(),
    calculatorViewModel: CalculatorViewModel,
    initialPatientId: String? = null,
    onNavigateBack: () -> Unit,
) {
    val history by historyViewModel.getAllHistoryFlow().collectAsStateWithLifecycle(initialValue = emptyList())
    val drugsState by calculatorViewModel.uiState.collectAsStateWithLifecycle()

    var selectedDrugName by remember { mutableStateOf<String?>(null) }
    var selectedPatient by remember { mutableStateOf<Patient?>(null) }

    LaunchedEffect(initialPatientId, drugsState.savedPatients) {
        if (initialPatientId != null && selectedPatient == null) {
            selectedPatient = drugsState.savedPatients.find { it.id == initialPatientId }
        }
    }

    val filteredHistory =
        remember(history, selectedDrugName, selectedPatient) {
            history.filter { record ->
                val matchDrug = selectedDrugName == null || record.drugName == selectedDrugName
                val matchPatient = selectedPatient == null || record.patientId == selectedPatient?.id
                matchDrug && matchPatient
            }
        }

    val isCompact = isCompactHeight()
    val sp = MaterialTheme.spacing
    val shapes = LocalDosageShapes.current
    val elev = LocalElevation.current

    val categoryDist =
        remember(history, drugsState.availableDrugs) {
            historyViewModel.getCategoryDistribution(history, drugsState.availableDrugs)
        }

    val uniqueDrugs = remember(history) { historyViewModel.getUniqueDrugsInHistory(history) }
    val uniquePatients =
        remember(history, drugsState.savedPatients) {
            historyViewModel.getUniquePatientsInHistory(history, drugsState.savedPatients)
        }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            GradientScreenHeader(
                colors =
                    listOf(
                        MaterialTheme.colorScheme.tertiary,
                        MaterialTheme.colorScheme.tertiaryContainer,
                    ),
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = sp.sm),
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onTertiary,
                            )
                        }
                        Text(
                            text = "Analisi Dati",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.8f),
                        )
                    }

                    Column(
                        modifier =
                            Modifier.padding(
                                start = sp.xl,
                                end = sp.xl,
                                top = if (isCompact) sp.sm else sp.base,
                                bottom = if (isCompact) sp.sm else sp.xl,
                            ),
                    ) {
                        Text(
                            text = "Statistiche",
                            style =
                                if (isCompact) {
                                    MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Serif)
                                } else {
                                    MaterialTheme.typography.displaySmall.copy(fontFamily = FontFamily.Serif)
                                },
                            color = MaterialTheme.colorScheme.onTertiary,
                        )
                        if (!isCompact) {
                            Text(
                                text = if (selectedDrugName == null) "Panoramica globale" else "Trend: $selectedDrugName",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.9f),
                            )
                        }
                    }
                }
            }

            val expanded = isExpandedWidth()

            if (expanded) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = sp.lg, vertical = sp.base),
                    horizontalArrangement = Arrangement.spacedBy(sp.lg),
                ) {
                    Column(
                        modifier =
                            Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(sp.lg),
                    ) {
                        StatsSummaryCard(filteredHistory.size, categoryDist.size)
                        FilterSection(
                            drugs = uniqueDrugs,
                            patients = uniquePatients,
                            selectedDrug = selectedDrugName,
                            selectedPatient = selectedPatient,
                            onDrugSelected = { selectedDrugName = it },
                            onPatientSelected = { selectedPatient = it },
                        )
                        if (selectedDrugName != null && filteredHistory.size >= 2) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = shapes.cardLarge,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = elev.level1),
                            ) {
                                DoseTrendChart(records = filteredHistory)
                            }
                        } else if (selectedDrugName == null) {
                            InfoNote(
                                text = "Seleziona un farmaco specifico dai filtri per visualizzare l'andamento del dosaggio nel tempo.",
                            )
                        } else {
                            InfoNote(text = "Dati insufficienti per generare un grafico di trend per questo farmaco.")
                        }
                        Spacer(modifier = Modifier.height(sp.xxxl))
                    }
                    Column(
                        modifier =
                            Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        if (selectedDrugName == null && categoryDist.isNotEmpty()) {
                            CategoryDistributionCard(categoryDist)
                        }
                        Spacer(modifier = Modifier.height(sp.xxxl))
                    }
                }
            } else {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(sp.lg),
                    verticalArrangement = Arrangement.spacedBy(sp.lg),
                ) {
                    StatsSummaryCard(filteredHistory.size, categoryDist.size)

                    FilterSection(
                        drugs = uniqueDrugs,
                        patients = uniquePatients,
                        selectedDrug = selectedDrugName,
                        selectedPatient = selectedPatient,
                        onDrugSelected = { selectedDrugName = it },
                        onPatientSelected = { selectedPatient = it },
                    )

                    if (selectedDrugName != null && filteredHistory.size >= 2) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = shapes.cardLarge,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = elev.level1),
                        ) {
                            DoseTrendChart(records = filteredHistory)
                        }
                    } else if (selectedDrugName == null) {
                        InfoNote(text = "Seleziona un farmaco specifico dai filtri per visualizzare l'andamento del dosaggio nel tempo.")
                    } else {
                        InfoNote(text = "Dati insufficienti per generare un grafico di trend per questo farmaco.")
                    }

                    if (selectedDrugName == null && categoryDist.isNotEmpty()) {
                        CategoryDistributionCard(categoryDist)
                    }

                    Spacer(modifier = Modifier.height(sp.xxxl))
                }
            }
        }
    }
}
