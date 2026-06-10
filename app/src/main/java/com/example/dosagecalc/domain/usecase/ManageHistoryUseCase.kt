package com.example.dosagecalc.domain.usecase

import androidx.paging.PagingData
import com.example.dosagecalc.domain.model.HistoryRecord
import com.example.dosagecalc.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class ManageHistoryUseCase
    @Inject
    constructor(
        private val historyRepository: HistoryRepository,
    ) {
        fun getAllHistory(): Flow<List<HistoryRecord>> = historyRepository.getAllHistory()

        fun getAllHistoryPaged(query: String): Flow<PagingData<HistoryRecord>> = historyRepository.getAllHistoryPaged(query)

        fun getHistoryForPatient(patientId: String): Flow<List<HistoryRecord>> = historyRepository.getHistoryForPatient(patientId)

        fun getHistoryForPatientPaged(
            patientId: String,
            query: String,
        ): Flow<PagingData<HistoryRecord>> = historyRepository.getHistoryForPatientPaged(patientId, query)

        suspend fun saveHistoryRecord(record: HistoryRecord) {
            val toSave =
                if (record.id.isBlank()) {
                    record.copy(id = UUID.randomUUID().toString())
                } else {
                    record
                }
            historyRepository.saveHistoryRecord(toSave)
        }

        suspend fun deleteHistoryRecord(recordId: String) {
            historyRepository.deleteHistoryRecord(recordId)
        }
    }
