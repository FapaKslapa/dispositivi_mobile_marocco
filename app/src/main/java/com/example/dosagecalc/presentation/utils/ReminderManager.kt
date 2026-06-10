package com.example.dosagecalc.presentation.utils
import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.dosagecalc.domain.model.ReminderInterval
import com.example.dosagecalc.presentation.utils.worker.ReminderWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ReminderManager {
    fun scheduleReminder(
        context: Context,
        seriesId: String,
        drugName: String,
        interval: ReminderInterval,
        daySelection: Int,
        hour: Int,
        minute: Int,
        duration: Int,
    ) {
        val workManager = WorkManager.getInstance(context)
        for (i in 0 until duration) {
            val calendar =
                Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    when (interval) {
                        ReminderInterval.DAILY -> {
                            add(Calendar.DAY_OF_YEAR, i)
                        }
                        ReminderInterval.WEEKLY -> {
                            // daySelection is 1 (Sun) to 7 (Sat)
                            set(Calendar.DAY_OF_WEEK, daySelection)
                            add(Calendar.WEEK_OF_YEAR, i)
                            if (timeInMillis < System.currentTimeMillis() && i == 0) {
                                add(Calendar.WEEK_OF_YEAR, 1)
                            }
                        }
                        ReminderInterval.MONTHLY -> {
                            // daySelection is 1 to 31
                            set(Calendar.DAY_OF_MONTH, daySelection)
                            add(Calendar.MONTH, i)
                            if (timeInMillis < System.currentTimeMillis() && i == 0) {
                                add(Calendar.MONTH, 1)
                            }
                        }
                    }
                }
            var delay = calendar.timeInMillis - System.currentTimeMillis()
            if (delay < 0) {
                if (i == 0 && interval == ReminderInterval.DAILY) continue
                delay = 0
            }
            val inputData =
                Data
                    .Builder()
                    .putString("drug_name", drugName)
                    .putString("message", "è il momento di somministrare.")
                    .build()
            val workRequest =
                OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .addTag("DOSAGE_REMINDER")
                    .addTag(seriesId)
                    .setInputData(inputData)
                    .build()
            workManager.enqueue(workRequest)
        }
    }

    fun cancelReminderSeries(
        context: Context,
        seriesId: String,
    ) {
        WorkManager.getInstance(context).cancelAllWorkByTag(seriesId)
    }
}
