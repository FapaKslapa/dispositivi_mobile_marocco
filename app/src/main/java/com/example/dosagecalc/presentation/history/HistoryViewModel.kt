package com.example.dosagecalc.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.dosagecalc.domain.model.Drug
import com.example.dosagecalc.domain.model.HistoryRecord
import com.example.dosagecalc.domain.model.Patient
import com.example.dosagecalc.domain.usecase.ManageHistoryUseCase
import com.example.dosagecalc.domain.usecase.ManagePatientsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val records: List<HistoryRecord> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class HistoryViewModel
    @Inject
    constructor(
        private val manageHistoryUseCase: ManageHistoryUseCase,
        private val managePatientsUseCase: ManagePatientsUseCase,
    ) : ViewModel() {
        private val _searchQuery = MutableStateFlow("")
        val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

        private val patientIdFilter = MutableStateFlow<String?>(null)

        private val _filteredPatientName = MutableStateFlow<String?>(null)
        val filteredPatientName: StateFlow<String?> = _filteredPatientName.asStateFlow()

        private val _uiState = MutableStateFlow(HistoryUiState())
        val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

        @OptIn(ExperimentalCoroutinesApi::class)
        val historyPaged: Flow<PagingData<HistoryRecord>> =
            combine(_searchQuery, patientIdFilter) { query, patientId ->
                if (patientId != null) {
                    manageHistoryUseCase.getHistoryForPatientPaged(patientId, query)
                } else {
                    manageHistoryUseCase.getAllHistoryPaged(query)
                }
            }.flatMapLatest { it }
                .cachedIn(viewModelScope)

        init {
            _uiState.update { it.copy(isLoading = false) }
        }

        fun setFilterPatientId(patientId: String?) {
            patientIdFilter.value = patientId
            if (patientId != null) {
                viewModelScope.launch {
                    val patient = managePatientsUseCase.getPatientById(patientId)
                    _filteredPatientName.value = patient?.let { "${it.name} ${it.surname}" }
                }
            } else {
                _filteredPatientName.value = null
            }
        }

        fun updateSearchQuery(query: String) {
            _searchQuery.value = query
        }

        fun deleteRecord(recordId: String) {
            viewModelScope.launch {
                manageHistoryUseCase.deleteHistoryRecord(recordId)
            }
        }

        fun getAllHistory(onResult: (List<HistoryRecord>) -> Unit) {
            viewModelScope.launch {
                manageHistoryUseCase.getAllHistory().collect { list ->
                    onResult(list)
                }
            }
        }

        fun getAllHistoryFlow(): Flow<List<HistoryRecord>> = manageHistoryUseCase.getAllHistory()

        fun getHistoryForPatient(patientId: String): Flow<List<HistoryRecord>> = manageHistoryUseCase.getHistoryForPatient(patientId)

        fun getCategoryDistribution(
            records: List<HistoryRecord>,
            allDrugs: List<Drug>,
        ): Map<String, Int> =
            records
                .mapNotNull { record ->
                    allDrugs.find { it.id == record.drugId }?.category?.label
                }.groupingBy { it }
                .eachCount()

        fun getUniqueDrugsInHistory(records: List<HistoryRecord>): List<String> = records.map { it.drugName }.distinct().sorted()

        fun getUniquePatientsInHistory(
            records: List<HistoryRecord>,
            allPatients: List<Patient>,
        ): List<Patient> {
            val patientIds = records.mapNotNull { it.patientId }.distinct()
            return allPatients.filter { it.id in patientIds }.sortedBy { it.surname }
        }
    }
