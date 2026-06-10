package com.example.dosagecalc.presentation.calculator

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dosagecalc.domain.model.DosageResult
import com.example.dosagecalc.domain.model.Drug
import com.example.dosagecalc.domain.model.HepaticStage
import com.example.dosagecalc.domain.model.HistoryRecord
import com.example.dosagecalc.domain.model.Patient
import com.example.dosagecalc.domain.model.PatientData
import com.example.dosagecalc.domain.model.RenalStage
import com.example.dosagecalc.domain.repository.BsaFormulaType
import com.example.dosagecalc.domain.repository.DrugRepository
import com.example.dosagecalc.domain.repository.ThemeRepository
import com.example.dosagecalc.domain.usecase.CalculateDosageUseCase
import com.example.dosagecalc.domain.usecase.CheckDrugInteractionsUseCase
import com.example.dosagecalc.domain.usecase.ManageHistoryUseCase
import com.example.dosagecalc.domain.usecase.ManagePatientsUseCase
import com.example.dosagecalc.presentation.ui.widget.WidgetUpdateHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class CalculatorViewModel
    @Inject
    constructor(
        private val drugRepository: DrugRepository,
        private val themeRepository: ThemeRepository,
        private val calculateDosageUseCase: CalculateDosageUseCase,
        private val managePatientsUseCase: ManagePatientsUseCase,
        private val manageHistoryUseCase: ManageHistoryUseCase,
        private val checkDrugInteractionsUseCase: CheckDrugInteractionsUseCase,
        @ApplicationContext private val context: Context,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(CalculatorUiState())
        val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

        init {
            loadDrugs()
            loadPatients()
            observeSettings()
        }

        private fun observeSettings() {
            viewModelScope.launch {
                themeRepository.bsaFormula.collect { formula ->
                    _uiState.update { it.copy(bsaFormula = formula) }
                }
            }
        }

        fun onBsaFormulaChanged(formula: BsaFormulaType) {
            viewModelScope.launch {
                themeRepository.setBsaFormula(formula)
            }
        }

        private fun loadPatients() {
            viewModelScope.launch {
                managePatientsUseCase.getPatients().collect { list ->
                    _uiState.update { it.copy(savedPatients = list) }
                }
            }
        }

        private fun loadDrugs() {
            viewModelScope.launch {
                drugRepository
                    .getDrugs()
                    .catch { error ->
                        _uiState.update {
                            it.copy(
                                isLoadingDrugs = false,
                                loadError = "Impossibile caricare il catalogo farmaci: ${error.message}",
                            )
                        }
                    }.collect { drugs ->
                        _uiState.update {
                            it.copy(
                                isLoadingDrugs = false,
                                availableDrugs = drugs,
                                loadError = null,
                            )
                        }
                    }
            }
        }

        fun onSearchQueryChanged(query: String) {
            _uiState.update { it.copy(searchQuery = query) }
        }

        fun onDrugSelected(drug: Drug) {
            _uiState.update {
                it.copy(
                    selectedDrug = drug,
                    selectedPatient = null,
                    dosageResult = null,
                    weightError = null,
                    heightError = null,
                    ageError = null,
                    interactions = emptyList(),
                )
            }
        }

        fun onPatientSelected(patient: Patient?) {
            if (patient == null) {
                _uiState.update { it.copy(selectedPatient = null, interactions = emptyList()) }
            } else {
                _uiState.update {
                    it.copy(
                        selectedPatient = patient,
                        weightInput = patient.weightKg.toString(),
                        heightInput = patient.heightCm?.toString() ?: "",
                        ageInput = patient.ageYears.toString(),
                        renalStage = if (patient.hasRenalImpairment) RenalStage.G3 else RenalStage.NONE,
                        hepaticStage = if (patient.hasHepaticImpairment) HepaticStage.CHILD_B else HepaticStage.NONE,
                        weightError = null,
                        heightError = null,
                        ageError = null,
                        dosageResult = null,
                    )
                }
                checkInteractions(patient.id)
            }
        }

        private fun checkInteractions(patientId: String) {
            val drug = _uiState.value.selectedDrug ?: return
            viewModelScope.launch {
                manageHistoryUseCase.getHistoryForPatient(patientId).collect { history ->
                    val otherDrugIds = history.map { it.drugId }
                    val interactions = checkDrugInteractionsUseCase(drug.id, otherDrugIds)
                    _uiState.update { it.copy(interactions = interactions) }
                }
            }
        }

        fun onCategorySelected(category: com.example.dosagecalc.domain.model.DrugCategory?) {
            _uiState.update { it.copy(selectedCategory = category) }
        }

        fun onWeightChanged(value: String) {
            val error = validateNumericField(value, min = 1.0, max = 500.0, fieldName = "peso")
            _uiState.update {
                it.copy(weightInput = value, weightError = error, dosageResult = null, selectedPatient = null, interactions = emptyList())
            }
        }

        fun onHeightChanged(value: String) {
            val error = validateNumericField(value, min = 30.0, max = 280.0, fieldName = "altezza")
            _uiState.update {
                it.copy(heightInput = value, heightError = error, dosageResult = null, selectedPatient = null, interactions = emptyList())
            }
        }

        fun onAgeChanged(value: String) {
            val error =
                if (value.isBlank()) {
                    null
                } else {
                    val age = value.toIntOrNull()
                    when {
                        age == null -> "Inserire un numero intero"
                        age < 0 -> "Età non valida"
                        age > 120 -> "Età non valida (max 120)"
                        else -> null
                    }
                }
            _uiState.update {
                it.copy(
                    ageInput = value,
                    ageError = error,
                    dosageResult = null,
                    selectedPatient = null,
                    interactions = emptyList(),
                )
            }
        }

        fun deleteCustomDrug(id: String) {
            viewModelScope.launch {
                drugRepository.deleteCustomDrug(id)
            }
        }

        fun onRenalStageChanged(stage: com.example.dosagecalc.domain.model.RenalStage) {
            _uiState.update { it.copy(renalStage = stage, dosageResult = null) }
        }

        fun onHepaticStageChanged(stage: com.example.dosagecalc.domain.model.HepaticStage) {
            _uiState.update { it.copy(hepaticStage = stage, dosageResult = null) }
        }

        fun calculateDosage() {
            val state = _uiState.value
            val drug = state.selectedDrug ?: return

            viewModelScope.launch {
                _uiState.update { it.copy(isCalculating = true, dosageResult = null) }

                val result: DosageResult =
                    withContext(Dispatchers.Default) {
                        val patientData =
                            PatientData(
                                weightKg = state.weightInput.toDoubleOrNull(),
                                heightCm = state.heightInput.toDoubleOrNull(),
                                ageYears = state.ageInput.toIntOrNull(),
                                renalStage = state.renalStage,
                                hepaticStage = state.hepaticStage,
                                bsaFormula = state.bsaFormula,
                            )
                        calculateDosageUseCase(drug, patientData)
                    }

                if (result is DosageResult.Success) {
                    val record =
                        HistoryRecord(
                            id = "",
                            patientId = state.selectedPatient?.id,
                            drugId = drug.id,
                            drugName = drug.name,
                            date = LocalDateTime.now(),
                            weightKg = state.weightInput.toFloatOrNull() ?: 0f,
                            heightCm = state.heightInput.toFloatOrNull(),
                            ageYears = state.ageInput.toIntOrNull() ?: 0,
                            calculatedDose = result.totalDose,
                            calculatedDoseMax = result.totalDoseMax,
                            calculatedCycleDose = result.totalCycleDose,
                            calculatedTherapyDose = result.totalTherapyDose,
                            doseUnit = result.unit,
                            formulaUsed = result.formula,
                            notes = null,
                        )
                    manageHistoryUseCase.saveHistoryRecord(record)
                    WidgetUpdateHelper.updateAllWidgets(context)
                }

                _uiState.update {
                    it.copy(
                        isCalculating = false,
                        dosageResult = result,
                    )
                }
            }
        }

        fun clearResult() {
            _uiState.update { it.copy(dosageResult = null) }
        }

        fun resetCalculation() {
            _uiState.update {
                it.copy(
                    selectedDrug = null,
                    selectedPatient = null,
                    weightInput = "",
                    heightInput = "",
                    ageInput = "",
                    weightError = null,
                    heightError = null,
                    ageError = null,
                    dosageResult = null,
                    renalStage = com.example.dosagecalc.domain.model.RenalStage.NONE,
                    hepaticStage = com.example.dosagecalc.domain.model.HepaticStage.NONE,
                    interactions = emptyList(),
                )
            }
        }

        private fun validateNumericField(
            value: String,
            min: Double,
            max: Double,
            fieldName: String,
        ): String? {
            if (value.isBlank()) return null

            val number = value.toDoubleOrNull()
            return when {
                number == null -> "Inserire un numero valido"
                number < min -> "$fieldName troppo basso (min $min)"
                number > max -> "$fieldName troppo alto (max $max)"
                else -> null
            }
        }
    }
