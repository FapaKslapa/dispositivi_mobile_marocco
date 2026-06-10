package com.example.dosagecalc.presentation.calculator.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dosagecalc.domain.model.DosageResult
import com.example.dosagecalc.presentation.ui.components.CardTone
import com.example.dosagecalc.presentation.ui.components.GradientScreenHeader
import com.example.dosagecalc.presentation.ui.components.OutlinedTintCard
import com.example.dosagecalc.presentation.ui.theme.LocalDosageShapes
import com.example.dosagecalc.presentation.ui.theme.spacing
import com.example.dosagecalc.presentation.ui.util.formatDose
import com.example.dosagecalc.presentation.ui.util.isCompactHeight

@Composable
fun SuccessHeader(
    result: DosageResult.Success,
    drugLabel: String?,
) {
    var startAnimation by remember(result.totalDose) { mutableStateOf(false) }
    val animatedFraction by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        label = "dose_count_up",
    )
    LaunchedEffect(result.totalDose) { startAnimation = true }

    val displayDose = result.totalDose * animatedFraction.toDouble()
    val displayDoseMax = result.totalDoseMax?.let { it * animatedFraction.toDouble() }

    val compact = isCompactHeight()
    val sp = MaterialTheme.spacing
    val shapes = LocalDosageShapes.current
    val verticalPad = if (compact) sp.md else sp.xl
    val doseFontSize = if (compact) 36.sp else 54.sp
    val doseFontLineHeight = if (compact) 42.sp else 60.sp

    GradientScreenHeader(
        colors =
            listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.primaryContainer,
            ),
        modifier = Modifier.padding(bottom = 0.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = sp.xl, vertical = verticalPad)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(sp.sm))
                Text(
                    text = drugLabel ?: "Dose Calcolata",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                )
            }

            Spacer(modifier = Modifier.height(if (compact) sp.sm else sp.base))

            val rangeText =
                if (displayDoseMax != null) {
                    "${displayDose.formatDose()} - ${displayDoseMax.formatDose()}"
                } else {
                    displayDose.formatDose()
                }

            Text(
                text = rangeText,
                fontSize = doseFontSize,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimary,
                lineHeight = doseFontLineHeight,
            )
            Text(
                text = result.unit,
                style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
            )

            if (result.cappedToMaxDose) {
                Spacer(modifier = Modifier.height(if (compact) sp.sm else sp.base))
                Surface(
                    shape = shapes.pill,
                    color = MaterialTheme.colorScheme.errorContainer,
                ) {
                    Text(
                        text = "⚠ Ridotta al massimo consentito",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = sp.base, vertical = sp.sm),
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorHeader(
    title: String,
    message: String,
) {
    val compact = isCompactHeight()
    val sp = MaterialTheme.spacing
    GradientScreenHeader(
        colors =
            listOf(
                MaterialTheme.colorScheme.error,
                MaterialTheme.colorScheme.errorContainer,
            ),
    ) {
        Column(modifier = Modifier.padding(horizontal = sp.xl, vertical = if (compact) sp.md else sp.xl)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(32.dp),
                )
                Spacer(modifier = Modifier.width(sp.md))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onError,
                )
            }
            Spacer(modifier = Modifier.height(sp.md))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}

@Composable
fun DetailsCard(result: DosageResult.Success) {
    val sp = MaterialTheme.spacing
    val shapes = LocalDosageShapes.current
    val cs = MaterialTheme.colorScheme

    OutlinedTintCard(modifier = Modifier.fillMaxWidth(), tone = CardTone.Primary) {
        Column(modifier = Modifier.padding(sp.lg)) {
            Text(
                text = "Formula applicata",
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
                    text = result.formula,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                        ),
                    color = cs.primary,
                    modifier = Modifier.padding(sp.base),
                )
            }

            if (result.totalCycleDose != null || result.totalTherapyDose != null) {
                Spacer(modifier = Modifier.height(sp.lg))
                HorizontalDivider(color = cs.primary.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(sp.base))

                Column(verticalArrangement = Arrangement.spacedBy(sp.sm)) {
                    result.totalCycleDose?.let { cycleDose ->
                        DoseRow(
                            label = "Dose per ciclo",
                            value = "${cycleDose.formatDose()} ${result.unit}",
                            color = cs.primary,
                        )
                    }
                    result.totalTherapyDose?.let { therapyDose ->
                        DoseRow(
                            label = "Dose totale terapia",
                            value = "${therapyDose.formatDose()} ${result.unit}",
                            color = cs.tertiary,
                        )
                    }
                }
            }

            if (result.source.isNotBlank()) {
                Spacer(modifier = Modifier.height(sp.base))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Surface(
                        shape = shapes.chip,
                        color = cs.primary.copy(alpha = 0.10f),
                    ) {
                        Text(
                            text = result.source,
                            style = MaterialTheme.typography.labelSmall,
                            color = cs.primary.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = sp.sm, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DoseRow(
    label: String,
    value: String,
    color: Color,
) {
    val sp = MaterialTheme.spacing
    val cs = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier =
                Modifier
                    .size(7.dp)
                    .background(color, CircleShape),
        )
        Spacer(modifier = Modifier.width(sp.sm))
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

@Composable
fun AlertCard(alert: String) {
    val sp = MaterialTheme.spacing
    val cs = MaterialTheme.colorScheme

    OutlinedTintCard(modifier = Modifier.fillMaxWidth(), tone = CardTone.Error) {
        Row(
            modifier = Modifier.padding(sp.lg),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = cs.error,
                modifier = Modifier.size(20.dp).padding(top = 2.dp),
            )
            Spacer(modifier = Modifier.width(sp.md))
            Column {
                Text(
                    text = "Avviso clinico",
                    style = MaterialTheme.typography.titleMedium,
                    color = cs.error,
                )
                Spacer(modifier = Modifier.height(sp.xs))
                Text(
                    text = alert,
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurface,
                )
            }
        }
    }
}

@Composable
fun DisclaimerCard() {
    val sp = MaterialTheme.spacing
    val cs = MaterialTheme.colorScheme
    val shapes = LocalDosageShapes.current

    Surface(
        shape = shapes.card,
        color = cs.onSurface.copy(alpha = 0.04f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(sp.lg),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier =
                    Modifier
                        .padding(top = 4.dp)
                        .size(4.dp)
                        .background(cs.onSurfaceVariant.copy(alpha = 0.4f), CircleShape),
            )
            Spacer(modifier = Modifier.width(sp.md))
            Column {
                Text(
                    text = "Disclaimer",
                    style = MaterialTheme.typography.labelMedium,
                    color = cs.onSurfaceVariant.copy(alpha = 0.7f),
                )
                Spacer(modifier = Modifier.height(sp.xs))
                Text(
                    text =
                        "Strumento a finalità esclusivamente didattiche. " +
                            "Non sostituisce la valutazione clinica del medico. " +
                            "Verificare sempre il dosaggio sulla scheda tecnica ufficiale (RCP/AIFA).",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant.copy(alpha = 0.65f),
                )
            }
        }
    }
}
