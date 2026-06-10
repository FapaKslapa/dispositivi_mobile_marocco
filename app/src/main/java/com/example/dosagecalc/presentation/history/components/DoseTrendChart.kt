package com.example.dosagecalc.presentation.history.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dosagecalc.domain.model.HistoryRecord
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.fullWidth
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.core.cartesian.HorizontalLayout
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries

@Composable
fun DoseTrendChart(
    records: List<HistoryRecord>,
    modifier: Modifier = Modifier,
) {
    if (records.size < 2) return

    val modelProducer = remember { CartesianChartModelProducer() }
    val sortedRecords = remember(records) { records.sortedBy { it.date } }

    LaunchedEffect(sortedRecords) {
        modelProducer.runTransaction {
            lineSeries {
                series(sortedRecords.map { it.calculatedDose })
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth().padding(24.dp)) {
        val unit = records.firstOrNull()?.doseUnit ?: ""
        Text(
            text = "Trend Dosaggio ($unit)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Variazione calcolata per ${records.first().drugName}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))

        CartesianChartHost(
            chart =
                rememberCartesianChart(
                    rememberLineCartesianLayer(),
                    startAxis =
                        rememberStartAxis(
                            label =
                                rememberTextComponent(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textSize = 10.sp,
                                ),
                            guideline =
                                rememberLineComponent(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                    thickness = 1.dp,
                                ),
                        ),
                    bottomAxis =
                        rememberBottomAxis(
                            label =
                                rememberTextComponent(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textSize = 10.sp,
                                ),
                            guideline = null,
                        ),
                    horizontalLayout = HorizontalLayout.fullWidth(),
                ),
            modelProducer = modelProducer,
            modifier = Modifier.fillMaxWidth().height(220.dp),
        )
    }
}
