package com.example.dosagecalc.presentation.calculator.screen

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.dosagecalc.domain.model.Drug
import com.example.dosagecalc.domain.model.FormulaType
import com.example.dosagecalc.presentation.calculator.AddDataViewModel
import com.example.dosagecalc.presentation.ui.components.CardTone
import com.example.dosagecalc.presentation.ui.components.GradientScreenHeader
import com.example.dosagecalc.presentation.ui.components.OutlinedTintCard
import com.example.dosagecalc.presentation.ui.theme.LocalDosageShapes
import com.example.dosagecalc.presentation.ui.theme.spacing
import com.example.dosagecalc.presentation.ui.util.responsiveContentWidth
import kotlinx.coroutines.delay

@Composable
fun DrugDetailScreen(
    drugId: String,
    viewModel: AddDataViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    var drug by remember { mutableStateOf<Drug?>(null) }
    var card1Visible by remember { mutableStateOf(false) }
    var card2Visible by remember { mutableStateOf(false) }
    var card3Visible by remember { mutableStateOf(false) }

    LaunchedEffect(drugId) {
        viewModel.loadDrug(drugId) { loadedDrug -> drug = loadedDrug }
    }
    LaunchedEffect(drug) {
        if (drug != null) {
            card1Visible = true
            delay(80)
            card2Visible = true
            delay(80)
            card3Visible = true
        }
    }

    val sp = MaterialTheme.spacing
    val shapes = LocalDosageShapes.current
    val cs = MaterialTheme.colorScheme
    val cardEnter =
        fadeIn(tween(250)) +
            slideInVertically(
                initialOffsetY = { it / 4 },
                animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow),
            )

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
                            text = "Scheda Tecnica",
                            style = MaterialTheme.typography.titleMedium,
                            color = cs.onPrimary.copy(alpha = 0.8f),
                        )
                    }
                    Column(modifier = Modifier.padding(start = sp.xl, end = sp.xl, top = sp.xs)) {
                        Text(
                            text = drug?.name ?: "Caricamento...",
                            style = MaterialTheme.typography.displayMedium.copy(fontFamily = FontFamily.Serif),
                            color = cs.onPrimary,
                        )
                        drug?.category?.let { category ->
                            Surface(
                                color = cs.onPrimary.copy(alpha = 0.2f),
                                shape = shapes.pill,
                                modifier = Modifier.padding(top = sp.sm),
                            ) {
                                Text(
                                    text = category.label,
                                    modifier = Modifier.padding(horizontal = sp.md, vertical = sp.xs),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = cs.onPrimary,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(sp.xl))
                    }
                }
            }

            if (drug != null) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(sp.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Column(modifier = Modifier.responsiveContentWidth(maxWidth = 720.dp)) {
                        AnimatedVisibility(visible = card1Visible, enter = cardEnter) {
                            DetailInfoSection(
                                title = "Indicazione",
                                content = drug!!.indication,
                                icon = Icons.Default.Info,
                                tone = CardTone.Primary,
                            )
                        }

                        Spacer(modifier = Modifier.height(sp.base))

                        AnimatedVisibility(visible = card2Visible, enter = cardEnter) {
                            DosageDetailCard(drug = drug!!)
                        }

                        if (drug!!.alert.isNotBlank()) {
                            Spacer(modifier = Modifier.height(sp.base))
                            AnimatedVisibility(visible = card3Visible, enter = cardEnter) {
                                DetailWarningSection(title = "Avvertenze", content = drug!!.alert)
                            }
                        }

                        drug!!.contraindications?.let { contra ->
                            Spacer(modifier = Modifier.height(sp.base))
                            AnimatedVisibility(visible = card3Visible, enter = cardEnter) {
                                DetailWarningSection(title = "Controindicazioni", content = contra)
                            }
                        }

                        drug!!.sideEffects?.let { side ->
                            Spacer(modifier = Modifier.height(sp.base))
                            AnimatedVisibility(visible = card3Visible, enter = cardEnter) {
                                DetailInfoSection(
                                    title = "Effetti Collaterali",
                                    content = side,
                                    icon = Icons.Default.Warning,
                                    tone = CardTone.Tertiary,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(sp.lg))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            Surface(shape = shapes.chip, color = cs.primary.copy(alpha = 0.10f)) {
                                Text(
                                    text = drug!!.source,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = cs.primary.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(horizontal = sp.sm, vertical = 4.dp),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(sp.xxxl))
                    }
                }
            }
        }
    }
}

private fun FormulaType.labelIt() =
    when (this) {
        FormulaType.PER_KG -> "per kg"
        FormulaType.PER_M2 -> "per m²"
        FormulaType.FIXED -> "dose fissa"
        FormulaType.BY_RANGE -> "per fascia di peso"
    }

@Composable
private fun DetailInfoSection(
    title: String,
    content: String,
    icon: ImageVector,
    tone: CardTone,
) {
    val sp = MaterialTheme.spacing
    val cs = MaterialTheme.colorScheme
    val color: Color =
        when (tone) {
            CardTone.Primary -> cs.primary
            CardTone.Secondary -> cs.secondary
            CardTone.Tertiary -> cs.tertiary
            CardTone.Error -> cs.error
        }
    OutlinedTintCard(modifier = Modifier.fillMaxWidth(), tone = tone) {
        Column(modifier = Modifier.padding(sp.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(sp.sm))
                Text(text = title, style = MaterialTheme.typography.labelMedium, color = color.copy(alpha = 0.8f))
            }
            Spacer(modifier = Modifier.height(sp.sm))
            Text(text = content, style = MaterialTheme.typography.bodyMedium, color = cs.onSurface)
        }
    }
}

@Composable
private fun DetailWarningSection(
    title: String,
    content: String,
) {
    val sp = MaterialTheme.spacing
    val cs = MaterialTheme.colorScheme
    OutlinedTintCard(modifier = Modifier.fillMaxWidth(), tone = CardTone.Error) {
        Column(modifier = Modifier.padding(sp.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = cs.error, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(sp.sm))
                Text(text = title, style = MaterialTheme.typography.labelMedium, color = cs.error.copy(alpha = 0.8f))
            }
            Spacer(modifier = Modifier.height(sp.sm))
            Text(text = content, style = MaterialTheme.typography.bodyMedium, color = cs.onSurface)
        }
    }
}

@Composable
private fun DosageDetailCard(drug: Drug) {
    val sp = MaterialTheme.spacing
    val cs = MaterialTheme.colorScheme
    val shapes = LocalDosageShapes.current

    OutlinedTintCard(modifier = Modifier.fillMaxWidth(), tone = CardTone.Primary) {
        Column(modifier = Modifier.padding(sp.lg)) {
            Text(
                text = "Dosaggio e Formula",
                style = MaterialTheme.typography.labelMedium,
                color = cs.primary.copy(alpha = 0.75f),
            )
            Spacer(modifier = Modifier.height(sp.sm))

            Surface(
                shape = shapes.tile,
                color = cs.primary.copy(alpha = 0.07f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "${drug.unitDose} ${drug.unit} · ${drug.formulaType.labelIt()}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    color = cs.primary,
                    modifier = Modifier.padding(sp.base),
                )
            }

            if (drug.daysPerCycle != null || drug.numberOfCycles != null) {
                Spacer(modifier = Modifier.height(sp.base))
                HorizontalDivider(color = cs.primary.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(sp.base))

                Column(verticalArrangement = Arrangement.spacedBy(sp.sm)) {
                    drug.daysPerCycle?.let {
                        DosageRow(label = "Giorni per ciclo", value = "$it gg", color = cs.primary)
                    }
                    drug.numberOfCycles?.let {
                        DosageRow(label = "Numero di cicli", value = "$it", color = cs.secondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun DosageRow(
    label: String,
    value: String,
    color: Color,
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = cs.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = color,
        )
    }
}
