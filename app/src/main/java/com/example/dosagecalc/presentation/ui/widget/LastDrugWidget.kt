package com.example.dosagecalc.presentation.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.dosagecalc.MainActivity
import com.example.dosagecalc.domain.model.HistoryRecord
import com.example.dosagecalc.presentation.ui.theme.WidgetLastDrugBackground
import java.time.format.DateTimeFormatter

class LastDrugWidget : GlanceAppWidget() {
    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val lastDrug = WidgetDataProvider.getLastDrug(context)
        provideContent { WidgetContent(lastDrug) }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun WidgetContent(record: HistoryRecord?) {
        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(WidgetLastDrugBackground)
                    .cornerRadius(20.dp)
                    .clickable(actionStartActivity<MainActivity>()),
            contentAlignment = Alignment.TopStart,
        ) {
            Box(
                modifier =
                    GlanceModifier
                        .fillMaxSize(),
                contentAlignment = Alignment.TopEnd,
            ) {
                Box(
                    modifier =
                        GlanceModifier
                            .size(90.dp)
                            .background(Color.White.copy(alpha = 0.07f))
                            .cornerRadius(45.dp),
                ) {}
            }

            Box(
                modifier =
                    GlanceModifier
                        .fillMaxSize(),
                contentAlignment = Alignment.BottomStart,
            ) {
                Box(
                    modifier =
                        GlanceModifier
                            .size(56.dp)
                            .background(Color.White.copy(alpha = 0.05f))
                            .cornerRadius(28.dp),
                ) {}
            }

            Column(
                modifier =
                    GlanceModifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                ) {
                    Text(
                        text = "ULTIMO CALCOLO",
                        modifier = GlanceModifier.defaultWeight(),
                        style =
                            TextStyle(
                                color = ColorProvider(Color.White.copy(alpha = 0.70f)),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                    )
                }

                Spacer(modifier = GlanceModifier.height(10.dp))

                if (record != null) {
                    Text(
                        text = record.drugName,
                        maxLines = 1,
                        style =
                            TextStyle(
                                color = ColorProvider(Color.White),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                            ),
                    )
                    Spacer(modifier = GlanceModifier.height(3.dp))
                    Text(
                        text = "${formatDose(record.calculatedDose)} ${record.doseUnit}",
                        style =
                            TextStyle(
                                color = ColorProvider(Color.White.copy(alpha = 0.88f)),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Text(
                        text = record.date.format(DateTimeFormatter.ofPattern("dd MMM · HH:mm")),
                        style =
                            TextStyle(
                                color = ColorProvider(Color.White.copy(alpha = 0.55f)),
                                fontSize = 10.sp,
                            ),
                    )
                } else {
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Text(
                        text = "Nessun calcolo",
                        style =
                            TextStyle(
                                color = ColorProvider(Color.White.copy(alpha = 0.50f)),
                                fontSize = 13.sp,
                            ),
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                }
            }
        }
    }

    private fun formatDose(dose: Double): String =
        if (dose == dose.toLong().toDouble()) {
            dose.toLong().toString()
        } else {
            String.format(java.util.Locale.US, "%.1f", dose)
        }
}

class LastDrugWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LastDrugWidget()
}
