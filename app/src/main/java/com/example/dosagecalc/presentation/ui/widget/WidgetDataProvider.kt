package com.example.dosagecalc.presentation.ui.widget

import android.content.Context
import com.example.dosagecalc.data.database.AppDatabase
import com.example.dosagecalc.domain.model.HistoryRecord
import com.example.dosagecalc.domain.model.Reminder
import com.example.dosagecalc.domain.model.ReminderInterval
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun appDatabase(): AppDatabase
}

object WidgetDataProvider {
    private fun db(context: Context): AppDatabase =
        EntryPointAccessors
            .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
            .appDatabase()

    suspend fun getLastDrug(context: Context): HistoryRecord? {
        val lastEntity =
            db(context)
                .historyDao()
                .getAllHistory()
                .first()
                .sortedByDescending { it.date }
                .firstOrNull() ?: return null

        return HistoryRecord(
            id = lastEntity.id,
            patientId = lastEntity.patientId,
            drugId = lastEntity.drugId,
            drugName = lastEntity.drugName,
            date = LocalDateTime.ofInstant(Instant.ofEpochMilli(lastEntity.date), ZoneId.systemDefault()),
            weightKg = lastEntity.weightKg,
            heightCm = lastEntity.heightCm,
            ageYears = lastEntity.ageYears,
            calculatedDose = lastEntity.calculatedDose,
            calculatedDoseMax = lastEntity.calculatedDoseMax,
            doseUnit = lastEntity.doseUnit,
            formulaUsed = lastEntity.formulaUsed,
            notes = lastEntity.notes,
        )
    }

    suspend fun getNextReminder(context: Context): Reminder? {
        val entities = db(context).reminderDao().getAllReminders().first()
        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val sorted =
            entities
                .map { e ->
                    Reminder(
                        id = e.id,
                        drugName = e.drugName,
                        interval = ReminderInterval.valueOf(e.interval),
                        daySelection = e.daySelection,
                        hour = e.hour,
                        minute = e.minute,
                        durationDays = e.durationDays,
                        timestamp = e.timestamp,
                    )
                }.sortedWith(compareBy({ it.hour }, { it.minute }))

        return sorted.firstOrNull { (it.hour * 60 + it.minute) > currentMinutes }
            ?: sorted.firstOrNull()
    }
}
