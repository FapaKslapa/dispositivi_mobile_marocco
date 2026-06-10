package com.example.dosagecalc.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.dosagecalc.data.datasource.CustomDrugDao
import com.example.dosagecalc.data.datasource.HistoryDao
import com.example.dosagecalc.data.datasource.PatientDao
import com.example.dosagecalc.data.datasource.ReminderDao
import com.example.dosagecalc.data.model.CustomDrugEntity
import com.example.dosagecalc.data.model.HistoryEntity
import com.example.dosagecalc.data.model.PatientEntity
import com.example.dosagecalc.data.model.ReminderEntity

@Database(
    entities = [PatientEntity::class, HistoryEntity::class, ReminderEntity::class, CustomDrugEntity::class],
    version = 8,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao

    abstract fun historyDao(): HistoryDao

    abstract fun reminderDao(): ReminderDao

    abstract fun customDrugDao(): CustomDrugDao
}
