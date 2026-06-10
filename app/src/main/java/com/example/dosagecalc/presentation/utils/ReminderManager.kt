package com.example.dosagecalc.presentation.utils
import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.dosagecalc.domain.model.ReminderInterval
import com.example.dosagecalc.presentation.utils.worker.ReminderWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

import com.example.dosagecalc.domain.model.Reminder

object ReminderManager {
    fun scheduleReminder(
        context: Context,
        reminder: Reminder,
    ) {
        val workManager = WorkManager.getInstance(context)
        val duration = reminder.durationDays
        for (i in 0 until duration) {
            val calendar = calculateReminderTime(reminder, i)
            var delay = calendar.timeInMillis - System.currentTimeMillis()
            if (delay < 0) {
                if (i == 0 && reminder.interval == ReminderInterval.DAILY) continue
                delay = 0
            }
            val inputData =
                Data
                    .Builder()
                    .putString("drug_name", reminder.drugName)
                    .putString("message", "è il momento di somministrare.")
                    .build()
            val workRequest =
                OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .addTag("DOSAGE_REMINDER")
                    .addTag(reminder.id)
                    .setInputData(inputData)
                    .build()
            workManager.enqueue(workRequest)
        }
    }

    private fun calculateReminderTime(reminder: Reminder, index: Int): Calendar {
        return Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, reminder.hour)
            set(Calendar.MINUTE, reminder.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            when (reminder.interval) {
                ReminderInterval.DAILY -> {
                    add(Calendar.DAY_OF_YEAR, index)
                }
                ReminderInterval.WEEKLY -> {
                    // daySelection is 1 (Sun) to 7 (Sat)
                    set(Calendar.DAY_OF_WEEK, reminder.daySelection)
                    add(Calendar.WEEK_OF_YEAR, index)
                    if (timeInMillis < System.currentTimeMillis() && index == 0) {
                        add(Calendar.WEEK_OF_YEAR, 1)
                    }
                }
                ReminderInterval.MONTHLY -> {
                    // daySelection is 1 to 31
                    set(Calendar.DAY_OF_MONTH, reminder.daySelection)
                    add(Calendar.MONTH, index)
                    if (timeInMillis < System.currentTimeMillis() && index == 0) {
                        add(Calendar.MONTH, 1)
                    }
                }
            }
        }
    }

    fun cancelReminderSeries(
        context: Context,
        seriesId: String,
    ) {
        WorkManager.getInstance(context).cancelAllWorkByTag(seriesId)
    }
}
