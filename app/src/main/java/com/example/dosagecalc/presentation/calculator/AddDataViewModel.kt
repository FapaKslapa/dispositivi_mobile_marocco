package com.example.dosagecalc.presentation.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dosagecalc.domain.model.Drug
import com.example.dosagecalc.domain.model.FormulaType
import com.example.dosagecalc.domain.repository.DrugRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddDataViewModel
    @Inject
    constructor(
        private val drugRepository: DrugRepository,
    ) : ViewModel() {
        fun loadDrug(
            id: String,
            onLoaded: (Drug) -> Unit,
        ) {
            viewModelScope.launch {
                val drug = drugRepository.getDrugById(id)
                if (drug != null) {
                    onLoaded(drug)
                }
            }
        }

        @Suppress("LongParameterList")
        fun saveCustomDrug(
            id: String? = null,
            name: String,
            indication: String,
            category: com.example.dosagecalc.domain.model.DrugCategory,
            formula: String,
            dose: String,
            unit: String,
            maxDose: String,
            alert: String,
            contraindications: String,
            sideEffects: String,
            onSuccess: () -> Unit,
        ) {
            val parsedDose = dose.replace(",", ".").toDoubleOrNull() ?: 0.0
            val parsedMaxDose = maxDose.replace(",", ".").toDoubleOrNull()

            val mappedFormula =
                when {
                    formula.contains("Kg", ignoreCase = true) -> FormulaType.PER_KG
                    formula.contains("Superficie", ignoreCase = true) -> FormulaType.PER_M2
                    else -> FormulaType.FIXED
                }

            val finalId = id ?: ("custom_" + UUID.randomUUID().toString())

            val newDrug =
                Drug(
                    id = finalId,
                    name = name,
                    indication = indication,
                    formulaType = mappedFormula,
                    unitDose = parsedDose,
                    unitDoseMax = parsedMaxDose,
                    unit = unit,
                    alert = alert,
                    source = "Utente",
                    category = category,
                    minWeightKg = null,
                    maxWeightKg = null,
                    minAgeYears = null,
                    maxSingleDoseMcg = parsedMaxDose,
                    contraindications = contraindications.ifBlank { null },
                    sideEffects = sideEffects.ifBlank { null },
                )

            viewModelScope.launch {
                drugRepository.addCustomDrug(newDrug)
                onSuccess()
            }
        }

        fun deleteCustomDrug(id: String) {
            viewModelScope.launch {
                drugRepository.deleteCustomDrug(id)
            }
        }
    }
