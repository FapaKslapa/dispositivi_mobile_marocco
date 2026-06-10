package com.example.dosagecalc.presentation.calculator.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dosagecalc.domain.model.Drug
import com.example.dosagecalc.presentation.calculator.CalculatorViewModel
import com.example.dosagecalc.presentation.calculator.components.DashboardShortcuts
import com.example.dosagecalc.presentation.calculator.components.DrugCategoryFilterRow
import com.example.dosagecalc.presentation.calculator.components.DrugPreviewCard
import com.example.dosagecalc.presentation.calculator.components.DrugSelectionCard
import com.example.dosagecalc.presentation.ui.components.GradientBottomBar
import com.example.dosagecalc.presentation.ui.components.GradientScreenHeader
import com.example.dosagecalc.presentation.ui.theme.LocalDosageShapes
import com.example.dosagecalc.presentation.ui.theme.spacing
import com.example.dosagecalc.presentation.ui.util.isCompactHeight
import com.example.dosagecalc.presentation.ui.util.isMediumOrExpandedWidth
import kotlinx.coroutines.delay

@Composable
fun DrugSelectionScreen(
    viewModel: CalculatorViewModel,
    onNavigateToInput: () -> Unit,
    onNavigateToPatients: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToAddData: (String?) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onToggleTheme: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isWide = isMediumOrExpandedWidth()
    val isCompact = isCompactHeight()
    val sp = MaterialTheme.spacing
    val shapes = LocalDosageShapes.current

    var headerVisible by remember { mutableStateOf(false) }
    var searchVisible by remember { mutableStateOf(false) }
    var listVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        headerVisible = true
        delay(80)
        searchVisible = true
        delay(80)
        listVisible = true
    }
    var drugToDelete by remember { mutableStateOf<Drug?>(null) }
    val activity = androidx.activity.compose.LocalActivity.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    BackHandler { activity?.finish() }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize(),
        ) {
            GradientScreenHeader(
                colors =
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer,
                    ),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(100.dp)
                            .align(Alignment.BottomStart)
                            .offset(x = (-30).dp, y = 36.dp)
                            .background(Color.White.copy(alpha = 0.06f), CircleShape),
                )
                IconButton(
                    onClick = onToggleTheme,
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 8.dp),
                ) {
                    Icon(
                        imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = if (isDark) "Passa a tema chiaro" else "Passa a tema scuro",
                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    )
                }

                Column(modifier = Modifier.padding(start = sp.xl, top = if (isCompact) sp.xs else sp.xxxl)) {
                    Text(
                        text = if (isCompact) "Bentornato, Dottore." else "Bentornato,\nDottore.",
                        style =
                            MaterialTheme.typography.displayLarge.copy(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                fontSize = if (isCompact) 22.sp else 32.sp,
                            ),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    if (!isCompact) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Cosa desideri fare oggi?",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        )
                    }
                }
            }

            if (!isCompact) {
                DashboardShortcuts(
                    modifier = Modifier.offset(y = (-12).dp),
                    onNavigateToPatients = onNavigateToPatients,
                    onNavigateToHistory = onNavigateToHistory,
                    onNavigateToReminders = onNavigateToReminders,
                    onNavigateToAddData = { onNavigateToAddData(null) },
                )
            }

            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 0.dp)
                        .padding(bottom = sp.bottomBarClearance),
            ) {
                if (isCompact) {
                    DashboardShortcuts(
                        modifier = Modifier.padding(vertical = 12.dp),
                        onNavigateToPatients = onNavigateToPatients,
                        onNavigateToHistory = onNavigateToHistory,
                        onNavigateToReminders = onNavigateToReminders,
                        onNavigateToAddData = { onNavigateToAddData(null) },
                    )
                }
                AnimatedVisibility(
                    visible = headerVisible,
                    enter =
                        fadeIn(tween(240)) +
                            slideInVertically(
                                initialOffsetY = { it / 4 },
                                animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow),
                            ),
                ) {
                    Text(
                        text = "Schede Farmaci",
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Serif),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = sp.lg),
                    )
                }
                Spacer(modifier = Modifier.height(sp.sm))

                AnimatedVisibility(
                    visible = searchVisible,
                    enter =
                        fadeIn(tween(240)) +
                            slideInVertically(
                                initialOffsetY = { it / 4 },
                                animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow),
                            ),
                ) {
                    Column {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            placeholder = { Text("Cerca farmaco...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cerca") },
                            colors =
                                OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                ),
                            shape = shapes.card,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = sp.lg)
                                    .height(52.dp),
                            singleLine = true,
                        )
                        Spacer(modifier = Modifier.height(sp.md))
                    }
                }

                DrugCategoryFilterRow(
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = viewModel::onCategorySelected,
                )
                AnimatedVisibility(
                    visible = listVisible,
                    enter = fadeIn(tween(280)),
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(sp.base))

                        when {
                            uiState.isLoadingDrugs -> {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .height(56.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                }
                            }
                            uiState.loadError != null -> {
                                Text(
                                    text = uiState.loadError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                )
                            }
                            else -> {
                                val filteredDrugs = uiState.filteredDrugs

                                if (isWide) {
                                    FlowRow(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = sp.lg),
                                        horizontalArrangement = Arrangement.spacedBy(sp.base),
                                        verticalArrangement = Arrangement.spacedBy(sp.base),
                                    ) {
                                        filteredDrugs.forEach { drug ->
                                            DrugSelectionCard(
                                                drug = drug,
                                                isSelected = uiState.selectedDrug == drug,
                                                onClick = { viewModel.onDrugSelected(drug) },
                                                onInfoClick = { onNavigateToDetail(drug.id) },
                                                onDeleteClick =
                                                    if (drug.id.startsWith("custom_")) {
                                                        { drugToDelete = drug }
                                                    } else {
                                                        null
                                                    },
                                                onEditClick =
                                                    if (drug.id.startsWith("custom_")) {
                                                        { onNavigateToAddData(drug.id) }
                                                    } else {
                                                        null
                                                    },
                                            )
                                        }
                                    }
                                } else {
                                    LazyRow(modifier = Modifier.fillMaxWidth()) {
                                        item { Spacer(modifier = Modifier.width(20.dp)) }
                                        items(filteredDrugs) { drug ->
                                            DrugSelectionCard(
                                                drug = drug,
                                                isSelected = uiState.selectedDrug == drug,
                                                onClick = { viewModel.onDrugSelected(drug) },
                                                onInfoClick = { onNavigateToDetail(drug.id) },
                                                onDeleteClick =
                                                    if (drug.id.startsWith("custom_")) {
                                                        { drugToDelete = drug }
                                                    } else {
                                                        null
                                                    },
                                                onEditClick =
                                                    if (drug.id.startsWith("custom_")) {
                                                        { onNavigateToAddData(drug.id) }
                                                    } else {
                                                        null
                                                    },
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                        }
                                        item { Spacer(modifier = Modifier.width(4.dp)) }
                                    }
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = uiState.selectedDrug != null,
                    enter =
                        fadeIn(animationSpec = tween(250)) +
                            slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec =
                                    spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMediumLow,
                                    ),
                            ),
                    exit =
                        fadeOut(animationSpec = tween(180)) +
                            slideOutVertically(
                                targetOffsetY = { it / 3 },
                                animationSpec = tween(180),
                            ),
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(sp.xl))
                        Box(modifier = Modifier.padding(horizontal = sp.lg)) {
                            uiState.selectedDrug?.let { DrugPreviewCard(drug = it) }
                        }
                    }
                }
            }
        }

        drugToDelete?.let { drug ->
            DeleteDrugDialog(
                drugName = drug.name,
                onConfirm = {
                    viewModel.deleteCustomDrug(drug.id)
                    drugToDelete = null
                },
                onDismiss = { drugToDelete = null },
            )
        }

        GradientBottomBar(
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Button(
                onClick = onNavigateToInput,
                enabled = uiState.selectedDrug != null && !uiState.isLoadingDrugs,
                shape = shapes.pill,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
            ) {
                Text(
                    text = "Avanti: Dati Paziente",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
private fun DeleteDrugDialog(
    drugName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val shapes = LocalDosageShapes.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminare $drugName?") },
        text = { Text("Sei sicuro di voler eliminare definitivamente questo farmaco personalizzato?") },
        confirmButton = {
            TextButton(onClick = onConfirm, shape = shapes.pill) {
                Text("Elimina", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shape = shapes.pill) {
                Text("Annulla")
            }
        },
    )
}
