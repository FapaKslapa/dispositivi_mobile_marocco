package com.example.dosagecalc.presentation.calculator.screen

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.dosagecalc.domain.model.FormulaType
import com.example.dosagecalc.presentation.calculator.AddDataViewModel
import com.example.dosagecalc.presentation.ui.components.ExpressiveCard
import com.example.dosagecalc.presentation.ui.components.GradientBottomBar
import com.example.dosagecalc.presentation.ui.components.GradientScreenHeader
import com.example.dosagecalc.presentation.ui.components.PillButton
import com.example.dosagecalc.presentation.ui.components.RoundedTextField
import com.example.dosagecalc.presentation.ui.theme.LocalDosageShapes
import com.example.dosagecalc.presentation.ui.theme.spacing
import com.example.dosagecalc.presentation.ui.util.isCompactHeight
import com.example.dosagecalc.presentation.ui.util.responsiveContentWidth

@Composable
fun AddDataScreen(
    drugId: String? = null,
    viewModel: AddDataViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var indication by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var maxDose by remember { mutableStateOf("") }
    var alert by remember { mutableStateOf("") }
    var contraindications by remember { mutableStateOf("") }
    var sideEffects by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(com.example.dosagecalc.domain.model.DrugCategory.OTHER) }

    var expanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    val formulaOptions = listOf("Per Kg (es. mg/kg)", "Per Superficie (es. mg/m²)", "Dose Fissa")
    var selectedFormula by remember { mutableStateOf(formulaOptions[0]) }

    LaunchedEffect(drugId) {
        if (drugId != null) {
            viewModel.loadDrug(drugId) { drug ->
                name = drug.name
                indication = drug.indication
                unit = drug.unit
                dose = drug.unitDose.toString()
                maxDose = drug.unitDoseMax?.toString() ?: ""
                alert = drug.alert
                contraindications = drug.contraindications ?: ""
                sideEffects = drug.sideEffects ?: ""
                selectedCategory = drug.category
                selectedFormula =
                    when (drug.formulaType) {
                        FormulaType.PER_KG -> formulaOptions[0]
                        FormulaType.PER_M2 -> formulaOptions[1]
                        FormulaType.FIXED -> formulaOptions[2]
                        else -> formulaOptions[0]
                    }
            }
        }
    }

    val isCompact = isCompactHeight()
    val sp = MaterialTheme.spacing
    val shapes = LocalDosageShapes.current

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            GradientScreenHeader(
                colors =
                    listOf(
                        MaterialTheme.colorScheme.error,
                        MaterialTheme.colorScheme.errorContainer,
                    ),
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
                                tint = MaterialTheme.colorScheme.onError,
                            )
                        }
                        Text(
                            text = "Dashboard",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onError.copy(alpha = 0.8f),
                        )
                    }

                    Column(modifier = Modifier.padding(start = sp.xl, end = sp.xl, top = sp.xs)) {
                        Text(
                            text = if (drugId != null) "Modifica Farmaco" else "Aggiungi Farmaco",
                            style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Serif),
                            color = MaterialTheme.colorScheme.onError,
                        )
                        if (!isCompact) {
                            Spacer(modifier = Modifier.height(sp.sm))
                            Text(
                                text = if (drugId != null) "Modifica i dati del medicinale" else "Inserisci un nuovo medicinale",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onError.copy(alpha = 0.9f),
                            )
                        }
                        Spacer(modifier = Modifier.height(if (isCompact) sp.xs else sp.xl))
                    }
                }
            }

            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = sp.lg, vertical = sp.xl)
                        .padding(bottom = sp.bottomBarClearance),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier.responsiveContentWidth(),
                    verticalArrangement = Arrangement.spacedBy(sp.base),
                ) {
                    ExpressiveCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(sp.base),
                            verticalArrangement = Arrangement.spacedBy(sp.base),
                        ) {
                            Text(
                                text = "Dati Principali",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            RoundedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Nome Farmaco (es. Paracetamolo)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = shapes.card,
                            )
                            RoundedTextField(
                                value = indication,
                                onValueChange = { indication = it },
                                label = { Text("Indicazione Terapeutica") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = shapes.card,
                            )
                            ExposedDropdownMenuBox(
                                expanded = categoryExpanded,
                                onExpandedChange = { categoryExpanded = it },
                            ) {
                                OutlinedTextField(
                                    value = selectedCategory.label,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Categoria Farmaco") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                                    shape = shapes.card,
                                )
                                ExposedDropdownMenu(
                                    expanded = categoryExpanded,
                                    onDismissRequest = { categoryExpanded = false },
                                ) {
                                    com.example.dosagecalc.domain.model.DrugCategory.entries.forEach { category ->
                                        DropdownMenuItem(
                                            text = { Text(category.label) },
                                            onClick = {
                                                selectedCategory = category
                                                categoryExpanded = false
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    ExpressiveCard(modifier = Modifier.fillMaxWidth(), mirrored = true) {
                        Column(
                            modifier = Modifier.padding(sp.base),
                            verticalArrangement = Arrangement.spacedBy(sp.base),
                        ) {
                            Text(
                                text = "Regole di Dosaggio",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = it },
                            ) {
                                OutlinedTextField(
                                    value = selectedFormula,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Formula di Calcolo") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                                    shape = shapes.card,
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                ) {
                                    formulaOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                selectedFormula = option
                                                expanded = false
                                            },
                                        )
                                    }
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(sp.base)) {
                                RoundedTextField(
                                    value = dose,
                                    onValueChange = { dose = it },
                                    label = { Text("Dose Base") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = shapes.card,
                                )
                                RoundedTextField(
                                    value = unit,
                                    onValueChange = { unit = it },
                                    label = { Text("Unità (es. mg, ml)") },
                                    modifier = Modifier.weight(1f),
                                    shape = shapes.card,
                                )
                            }
                            RoundedTextField(
                                value = maxDose,
                                onValueChange = { maxDose = it },
                                label = { Text("Dose Massima (Tetto Sicurezza) - Opzionale") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = shapes.card,
                            )
                        }
                    }

                    ExpressiveCard(
                        modifier = Modifier.fillMaxWidth(),
                        asymmetric = false,
                    ) {
                        Column(
                            modifier = Modifier.padding(sp.base),
                            verticalArrangement = Arrangement.spacedBy(sp.base),
                        ) {
                            Text(
                                text = "Note Cliniche",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            RoundedTextField(
                                value = alert,
                                onValueChange = { alert = it },
                                label = { Text("Note Cliniche / Avvertenze (Opzionale)") },
                                modifier = Modifier.fillMaxWidth().height(100.dp),
                                maxLines = 3,
                                shape = shapes.card,
                            )
                            RoundedTextField(
                                value = contraindications,
                                onValueChange = { contraindications = it },
                                label = { Text("Controindicazioni (Opzionale)") },
                                modifier = Modifier.fillMaxWidth().height(100.dp),
                                maxLines = 3,
                                shape = shapes.card,
                            )
                            RoundedTextField(
                                value = sideEffects,
                                onValueChange = { sideEffects = it },
                                label = { Text("Effetti Collaterali (Opzionale)") },
                                modifier = Modifier.fillMaxWidth().height(100.dp),
                                maxLines = 3,
                                shape = shapes.card,
                            )
                        }
                    }
                }
            }
        }

        GradientBottomBar(modifier = Modifier.align(Alignment.BottomCenter)) {
            PillButton(
                onClick = {
                    viewModel.saveCustomDrug(
                        id = drugId,
                        name = name,
                        indication = indication,
                        category = selectedCategory,
                        formula = selectedFormula,
                        dose = dose,
                        unit = unit,
                        maxDose = maxDose,
                        alert = alert,
                        contraindications = contraindications,
                        sideEffects = sideEffects,
                        onSuccess = onNavigateBack,
                    )
                },
                label = "Salva",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
