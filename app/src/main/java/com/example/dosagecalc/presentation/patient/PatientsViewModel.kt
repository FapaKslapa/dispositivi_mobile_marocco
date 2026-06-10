package com.example.dosagecalc.presentation.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.dosagecalc.domain.model.Patient
import com.example.dosagecalc.domain.usecase.ManagePatientsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class PatientsUiState(
    val patients: List<Patient> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class PatientsViewModel
    @Inject
    constructor(
        private val managePatientsUseCase: ManagePatientsUseCase,
    ) : ViewModel() {
        private val _searchQuery = MutableStateFlow("")
        val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

        private val _uiState = MutableStateFlow(PatientsUiState())
        val uiState: StateFlow<PatientsUiState> = _uiState.asStateFlow()

        @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
        val patientsPaged: Flow<PagingData<Patient>> =
            _searchQuery
                .flatMapLatest { query -> managePatientsUseCase.getPatientsPaged(query) }
                .cachedIn(viewModelScope)

        init {
            _uiState.update { it.copy(isLoading = false) }
        }

        fun updateSearchQuery(query: String) {
            _searchQuery.value = query
        }

        fun savePatient(
            name: String,
            surname: String,
            weightKg: String,
            heightCm: String?,
            ageYears: String,
            hasRenalImpairment: Boolean = false,
            hasHepaticImpairment: Boolean = false,
        ) {
            val weight = weightKg.toFloatOrNull() ?: return
            val height = heightCm?.toFloatOrNull()
            val age = ageYears.toIntOrNull() ?: 0

            val newPatient =
                Patient(
                    id = "",
                    name = name,
                    surname = surname,
                    birthDate = LocalDateTime.now(),
                    weightKg = weight,
                    heightCm = height,
                    ageYears = age,
                    hasRenalImpairment = hasRenalImpairment,
                    hasHepaticImpairment = hasHepaticImpairment,
                )

            viewModelScope.launch {
                managePatientsUseCase.savePatient(newPatient)
            }
        }

        fun updatePatient(
            original: Patient,
            name: String,
            surname: String,
            weightKg: String,
            heightCm: String?,
            ageYears: String,
            hasRenalImpairment: Boolean,
            hasHepaticImpairment: Boolean,
        ) {
            val weight = weightKg.toFloatOrNull() ?: return
            val height = heightCm?.toFloatOrNull()
            val age = ageYears.toIntOrNull() ?: 0
            val updated =
                original.copy(
                    name = name,
                    surname = surname,
                    weightKg = weight,
                    heightCm = height,
                    ageYears = age,
                    hasRenalImpairment = hasRenalImpairment,
                    hasHepaticImpairment = hasHepaticImpairment,
                )
            viewModelScope.launch {
                managePatientsUseCase.savePatient(updated)
            }
        }

        fun deletePatient(patientId: String) {
            viewModelScope.launch {
                managePatientsUseCase.deletePatient(patientId)
            }
        }

        fun getAllPatients(onResult: (List<Patient>) -> Unit) {
            viewModelScope.launch {
                managePatientsUseCase.getPatients().collect { list ->
                    onResult(list)
                }
            }
        }
    }
