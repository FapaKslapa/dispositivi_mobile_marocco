package com.example.dosagecalc.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.dosagecalc.data.datasource.HistoryDao
import com.example.dosagecalc.data.model.HistoryEntity
import com.example.dosagecalc.domain.model.HistoryRecord
import com.example.dosagecalc.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class HistoryRepositoryImpl(
    private val historyDao: HistoryDao,
) : HistoryRepository {
    override fun getAllHistory(): Flow<List<HistoryRecord>> = historyDao.getAllHistory().map { list -> list.map { it.toDomain() } }

    override fun getAllHistoryPaged(query: String): Flow<PagingData<HistoryRecord>> =
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { historyDao.getAllHistoryPaged(query) },
        ).flow.map { pagingData -> pagingData.map { it.toDomain() } }

    override fun getHistoryForPatient(patientId: String): Flow<List<HistoryRecord>> =
        historyDao.getHistoryForPatient(patientId).map { list ->
            list.map {
                it.toDomain()
            }
        }

    override fun getHistoryForPatientPaged(
        patientId: String,
        query: String,
    ): Flow<PagingData<HistoryRecord>> =
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { historyDao.getHistoryForPatientPaged(patientId, query) },
        ).flow.map { pagingData -> pagingData.map { it.toDomain() } }

    override suspend fun saveHistoryRecord(record: HistoryRecord) {
        historyDao.insertHistoryRecord(record.toEntity())
    }

    override suspend fun deleteHistoryRecord(recordId: String) {
        historyDao.deleteHistoryRecord(recordId)
    }

    private fun HistoryEntity.toDomain(): HistoryRecord =
        HistoryRecord(
            id = id,
            patientId = patientId,
            drugId = drugId,
            drugName = drugName,
            date = LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.systemDefault()),
            weightKg = weightKg,
            heightCm = heightCm,
            ageYears = ageYears,
            calculatedDose = calculatedDose,
            calculatedDoseMax = calculatedDoseMax,
            calculatedCycleDose = calculatedCycleDose,
            calculatedTherapyDose = calculatedTherapyDose,
            doseUnit = doseUnit,
            formulaUsed = formulaUsed,
            notes = notes,
        )

    private fun HistoryRecord.toEntity(): HistoryEntity =
        HistoryEntity(
            id = id,
            patientId = patientId,
            drugId = drugId,
            drugName = drugName,
            date = date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            weightKg = weightKg,
            heightCm = heightCm,
            ageYears = ageYears,
            calculatedDose = calculatedDose,
            calculatedDoseMax = calculatedDoseMax,
            calculatedCycleDose = calculatedCycleDose,
            calculatedTherapyDose = calculatedTherapyDose,
            doseUnit = doseUnit,
            formulaUsed = formulaUsed,
            notes = notes,
        )
}
