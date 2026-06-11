package com.example.dosagecalc.presentation.calculator.screen

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dosagecalc.domain.model.DosageResult
import com.example.dosagecalc.presentation.calculator.CalculatorViewModel
import com.example.dosagecalc.presentation.calculator.components.AlertCard
import com.example.dosagecalc.presentation.calculator.components.DetailsCard
import com.example.dosagecalc.presentation.calculator.components.DisclaimerCard
import com.example.dosagecalc.presentation.calculator.components.ErrorHeader
import com.example.dosagecalc.presentation.calculator.components.InteractionsCard
import com.example.dosagecalc.presentation.calculator.components.RemindersSheet
import com.example.dosagecalc.presentation.calculator.components.SuccessHeader
import com.example.dosagecalc.presentation.ui.components.GradientBottomBar
import com.example.dosagecalc.presentation.ui.components.OutlinedPillButton
import com.example.dosagecalc.presentation.ui.components.PillButton
import com.example.dosagecalc.presentation.ui.theme.LocalDosageShapes
import com.example.dosagecalc.presentation.ui.theme.spacing
import com.example.dosagecalc.presentation.ui.util.responsiveContentWidth
import com.example.dosagecalc.presentation.utils.PdfManager
import kotlinx.coroutines.delay

@Composable
fun DosageResultScreen(
    viewModel: CalculatorViewModel,
    onNewCalculation: () -> Unit,
    onNavigateBackToInput: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val result = uiState.dosageResult ?: return

    val context = LocalContext.current
    val canExportPdf = result is DosageResult.Success && uiState.selectedDrug != null

    var showReminderSheet by remember { mutableStateOf(false) }
    var detailsVisible by remember(result) { mutableStateOf(false) }
    var alertVisible by remember(result) { mutableStateOf(false) }
    var disclaimerVisible by remember(result) { mutableStateOf(false) }
    LaunchedEffect(result) {
        delay(80)
        detailsVisible = true
        delay(80)
        alertVisible = true
        delay(80)
        disclaimerVisible = true
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted) {
                showReminderSheet = true
            } else {
                Toast.makeText(context, "Permesso notifiche negato", Toast.LENGTH_SHORT).show()
            }
        }

    val sp = MaterialTheme.spacing
    val shapes = LocalDosageShapes.current

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
            ) {
                when (result) {
                    is DosageResult.Success -> SuccessHeader(result, uiState.selectedDrug?.let { "${it.name} - ${it.indication}" })
                    is DosageResult.ValidationError -> ErrorHeader(title = "Farmaco Non Indicato", message = result.reason)
                    is DosageResult.Error -> ErrorHeader(title = "Errore di Calcolo", message = result.message)
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Column(
                        modifier =
                            Modifier
                                .responsiveContentWidth(maxWidth = 720.dp)
                                .padding(horizontal = sp.lg)
                                .padding(bottom = sp.bottomBarClearance),
                    ) {
                        Spacer(modifier = Modifier.height(sp.lg))

                        if (result is DosageResult.Success) {
                            val ctx = LocalContext.current

                            if (uiState.interactions.isNotEmpty()) {
                                InteractionsCard(interactions = uiState.interactions)
                                Spacer(modifier = Modifier.height(sp.base))
                            }

                            Row(modifier = Modifier.fillMaxWidth().padding(bottom = sp.base)) {
                                if (uiState.selectedPatient != null) {
                                    OutlinedButton(
                                        onClick = {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                            } else {
                                                showReminderSheet = true
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = shapes.pill,
                                    ) {
                                        Icon(Icons.Default.DateRange, contentDescription = "Promemoria")
                                        Spacer(modifier = Modifier.width(sp.sm))
                                        Text("Promemoria")
                                    }
                                }

                                if (canExportPdf) {
                                    if (uiState.selectedPatient != null) {
                                        Spacer(modifier = Modifier.width(sp.md))
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            PdfManager.generateAndSharePdf(ctx, uiState.selectedDrug!!, uiState.selectedPatient, result)
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = shapes.pill,
                                    ) {
                                        Icon(Icons.Filled.Share, contentDescription = "PDF")
                                        Spacer(modifier = Modifier.width(sp.sm))
                                        Text("Esporta PDF")
                                    }
                                }
                            }

                            AnimatedVisibility(
                                visible = detailsVisible,
                                enter =
                                    fadeIn(tween(250)) +
                                        slideInVertically(
                                            initialOffsetY = { it / 3 },
                                            animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow),
                                        ),
                            ) { DetailsCard(result) }

                            if (result.alert.isNotBlank()) {
                                Spacer(modifier = Modifier.height(sp.base))
                                AnimatedVisibility(
                                    visible = alertVisible,
                                    enter =
                                        fadeIn(tween(250)) +
                                            slideInVertically(
                                                initialOffsetY = { it / 3 },
                                                animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow),
                                            ),
                                ) { AlertCard(result.alert) }
                            }
                            Spacer(modifier = Modifier.height(sp.base))
                        }

                        AnimatedVisibility(
                            visible = disclaimerVisible,
                            enter =
                                fadeIn(tween(250)) +
                                    slideInVertically(
                                        initialOffsetY = { it / 3 },
                                        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow),
                                    ),
                        ) { DisclaimerCard() }
                    }
                }
            }

            if (showReminderSheet) {
                RemindersSheet(
                    context = context,
                    drugName = uiState.selectedDrug?.name,
                    onDismissRequest = { showReminderSheet = false },
                )
            }

            GradientBottomBar(
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(sp.md),
                ) {
                    OutlinedPillButton(
                        onClick = {
                            viewModel.clearResult()
                            onNavigateBackToInput()
                        },
                        label = "Modifica",
                        leadingIcon = Icons.Default.Edit,
                        modifier = Modifier.weight(1f),
                    )

                    PillButton(
                        onClick = onNewCalculation,
                        label = "Torna alla Home",
                        leadingIcon = Icons.Default.Home,
                        modifier = Modifier.weight(1.3f),
                    )
                }
            }
        }
    }
}
