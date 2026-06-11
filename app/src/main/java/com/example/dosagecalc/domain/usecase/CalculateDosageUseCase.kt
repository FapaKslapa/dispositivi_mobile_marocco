package com.example.dosagecalc.domain.usecase

import com.example.dosagecalc.domain.model.DosageResult
import com.example.dosagecalc.domain.model.Drug
import com.example.dosagecalc.domain.model.FormulaType
import com.example.dosagecalc.domain.model.HepaticStage
import com.example.dosagecalc.domain.model.PatientData
import com.example.dosagecalc.domain.model.RenalStage
import javax.inject.Inject
import kotlin.math.roundToInt

private const val PERCENTAGE_FACTOR = 100
private const val RENAL_STAGE_G2_FACTOR = 0.10
private const val RENAL_STAGE_G3_FACTOR = 0.35
private const val RENAL_STAGE_G4_FACTOR = 0.75
private const val HEPATIC_CHILD_A_FACTOR = 0.25
private const val HEPATIC_CHILD_B_FACTOR = 0.65

@Suppress("TooManyFunctions")
class CalculateDosageUseCase
    @Inject
    constructor(
        private val validateInputUseCase: ValidateInputUseCase,
        private val calculateBsaUseCase: CalculateBsaUseCase,
    ) {
        operator fun invoke(
            drug: Drug,
            patientData: PatientData,
        ): DosageResult {
            val validation = validateInputUseCase(drug, patientData)
            if (validation is ValidationResult.Invalid) {
                return DosageResult.ValidationError(
                    reason = validation.errors.joinToString(separator = "\n• ", prefix = "• "),
                )
            }

            return when (drug.formulaType) {
                FormulaType.PER_KG -> calculatePerKg(drug, patientData)
                FormulaType.PER_M2 -> calculatePerM2(drug, patientData)
                FormulaType.FIXED -> calculateFixed(drug, patientData)
                FormulaType.BY_RANGE -> calculateByRange(drug, patientData)
            }
        }

        private fun calculatePerKg(
            drug: Drug,
            patientData: PatientData,
        ): DosageResult {
            val weight = patientData.weightKg!!
            val rawDose = drug.unitDose * weight

            val (impairedDose, impAlert) = applyImpairments(rawDose, drug, patientData)
            val (finalDose, capped) = applyCeiling(impairedDose, drug.maxSingleDoseMcg)

            var formula = "${drug.unitDose} ${drug.unit}/kg × $weight kg = $rawDose ${drug.unit}"
            if (impAlert.isNotBlank()) formula += "\n(Adjustment for pathology applied)"
            if (capped) formula += "\n(Limited to maximum allowed: ${drug.maxSingleDoseMcg})"

            val fullAlert = listOf(drug.alert, impAlert).filter { it.isNotBlank() }.joinToString("\n\n")

            val cycleDose = if (drug.daysPerCycle != null) finalDose * drug.daysPerCycle else null
            val therapyDose =
                if (cycleDose != null && drug.numberOfCycles != null) {
                    cycleDose * drug.numberOfCycles
                } else {
                    null
                }

            return DosageResult.Success(
                totalDose = finalDose,
                totalCycleDose = cycleDose,
                totalTherapyDose = therapyDose,
                unit = drug.unit,
                formula = formula,
                alert = fullAlert,
                source = drug.source,
                cappedToMaxDose = capped,
            )
        }

        private fun calculatePerM2(
            drug: Drug,
            patientData: PatientData,
        ): DosageResult {
            val weight = patientData.weightKg!!
            val height = patientData.heightCm!!

            val bsa = calculateBsaUseCase(weight, height, patientData.bsaFormula)
            val rawDose = drug.unitDose * bsa

            val (impairedDose, impAlert) = applyImpairments(rawDose, drug, patientData)
            val (finalDose, capped) = applyCeiling(impairedDose, drug.maxSingleDoseMcg)

            var formula =
                "${drug.unitDose} ${drug.unit}/m² × %.2f m² (BSA ${patientData.bsaFormula.name.lowercase().replaceFirstChar {
                    it
                        .uppercase()
                }}) = %.2f ${drug.unit}".format(
                    bsa,
                    rawDose,
                )
            if (impAlert.isNotBlank()) formula += "\n(Adjustment for pathology applied)"
            if (capped) formula += "\n(Limited to maximum allowed)"

            val fullAlert = listOf(drug.alert, impAlert).filter { it.isNotBlank() }.joinToString("\n\n")

            val cycleDose = if (drug.daysPerCycle != null) finalDose * drug.daysPerCycle else null
            val therapyDose =
                if (cycleDose != null && drug.numberOfCycles != null) {
                    cycleDose * drug.numberOfCycles
                } else {
                    null
                }

            return DosageResult.Success(
                totalDose = finalDose,
                totalCycleDose = cycleDose,
                totalTherapyDose = therapyDose,
                unit = drug.unit,
                formula = formula,
                alert = fullAlert,
                source = drug.source,
                cappedToMaxDose = capped,
            )
        }

        private fun calculateFixed(
            drug: Drug,
            patientData: PatientData,
        ): DosageResult {
            val rawDose = drug.unitDose
            val (impairedDose, impAlert) = applyImpairments(rawDose, drug, patientData)

            val formula = "Fixed dose: ${drug.unitDose} ${drug.unit}"
            val fullAlert = listOf(drug.alert, impAlert).filter { it.isNotBlank() }.joinToString("\n\n")

            val cycleDose = if (drug.daysPerCycle != null) impairedDose * drug.daysPerCycle else null
            val therapyDose =
                if (cycleDose != null && drug.numberOfCycles != null) {
                    cycleDose * drug.numberOfCycles
                } else {
                    null
                }

            return DosageResult.Success(
                totalDose = impairedDose,
                totalCycleDose = cycleDose,
                totalTherapyDose = therapyDose,
                unit = drug.unit,
                formula = formula,
                alert = fullAlert,
                source = drug.source,
                cappedToMaxDose = false,
            )
        }

        private fun calculateByRange(
            drug: Drug,
            patientData: PatientData,
        ): DosageResult {
            val weight = patientData.weightKg!!
            val minDose = drug.unitDose
            val maxDose = drug.unitDoseMax ?: drug.unitDose

            val rawMin = minDose * weight
            val rawMax = maxDose * weight

            val (impairedMin, impAlert) = applyImpairments(rawMin, drug, patientData)
            val (impairedMax, _) = applyImpairments(rawMax, drug, patientData)

            val (finalMin, cappedMin) = applyCeiling(impairedMin, drug.maxSingleDoseMcg)
            val (finalMax, cappedMax) = applyCeiling(impairedMax, drug.maxSingleDoseMcg)

            val capped = cappedMin || cappedMax
            var formula = "Range: $minDose–$maxDose ${drug.unit}/kg × $weight kg = $rawMin–$rawMax ${drug.unit}"
            if (impAlert.isNotBlank()) formula += "\n(Adjustment for pathology applied)"
            if (capped) formula += "\n(Limited to maximum allowed: ${drug.maxSingleDoseMcg})"

            val fullAlert = listOf(drug.alert, impAlert).filter { it.isNotBlank() }.joinToString("\n\n")

            val cycleDoseMin = if (drug.daysPerCycle != null) finalMin * drug.daysPerCycle else null
            val therapyDoseMin =
                if (cycleDoseMin != null && drug.numberOfCycles != null) {
                    cycleDoseMin * drug.numberOfCycles
                } else {
                    null
                }

            return DosageResult.Success(
                totalDose = finalMin,
                totalDoseMax = finalMax,
                totalCycleDose = cycleDoseMin,
                totalTherapyDose = therapyDoseMin,
                unit = drug.unit,
                formula = formula,
                alert = fullAlert,
                source = drug.source,
                cappedToMaxDose = capped,
            )
        }

        private fun applyImpairments(
            rawDose: Double,
            drug: Drug,
            patientData: PatientData,
        ): Pair<Double, String> {
            var dose = rawDose
            val alerts = mutableListOf<String>()

            dose = applyRenalAdjustment(dose, drug, patientData, alerts)
            dose = applyHepaticAdjustment(dose, drug, patientData, alerts)

            return Pair(dose, alerts.joinToString("\n\n"))
        }

        private fun applyRenalAdjustment(
            dose: Double,
            drug: Drug,
            patientData: PatientData,
            alerts: MutableList<String>,
        ): Double {
            if (!patientData.hasRenalImpairment) return dose
            val stage = patientData.renalStage
            return if (drug.renalDoseMultiplier != null) {
                val factor = renalFactor(stage, drug.renalDoseMultiplier)
                val pct = ((1.0 - factor) * PERCENTAGE_FACTOR).roundToInt()
                if (pct > 0) {
                    alerts += "⚠ Dose reduced by $pct% - ${stage.label} (${stage.gfrRange})." +
                        if (drug.renalAlert != null) "\n${drug.renalAlert}" else ""
                }
                dose * factor
            } else {
                if (stage != RenalStage.NONE) {
                    alerts += "⚠ ${stage.label} (${stage.gfrRange}) - no adjustment " +
                        "defined in official documentation for this drug. Proceed with caution."
                }
                dose
            }
        }

        private fun applyHepaticAdjustment(
            dose: Double,
            drug: Drug,
            patientData: PatientData,
            alerts: MutableList<String>,
        ): Double {
            if (!patientData.hasHepaticImpairment) return dose
            val stage = patientData.hepaticStage
            return if (drug.hepaticDoseMultiplier != null) {
                val factor = hepaticFactor(stage, drug.hepaticDoseMultiplier)
                val pct = ((1.0 - factor) * PERCENTAGE_FACTOR).roundToInt()
                if (pct > 0) {
                    alerts += "⚠ Dose reduced by $pct% - ${stage.label}: ${stage.description}." +
                        if (drug.hepaticAlert != null) "\n${drug.hepaticAlert}" else ""
                }
                dose * factor
            } else {
                if (stage != HepaticStage.NONE) {
                    alerts += "⚠ ${stage.label} - no adjustment defined in " +
                        "official documentation for this drug. Proceed with caution."
                }
                dose
            }
        }

        private fun renalFactor(
            stage: RenalStage,
            drugMultiplier: Double,
        ): Double =
            when (stage) {
                RenalStage.NONE -> 1.0
                RenalStage.G2 -> lerp(1.0, drugMultiplier, RENAL_STAGE_G2_FACTOR)
                RenalStage.G3 -> lerp(1.0, drugMultiplier, RENAL_STAGE_G3_FACTOR)
                RenalStage.G4 -> lerp(1.0, drugMultiplier, RENAL_STAGE_G4_FACTOR)
                RenalStage.G5 -> drugMultiplier
            }

        private fun hepaticFactor(
            stage: HepaticStage,
            drugMultiplier: Double,
        ): Double =
            when (stage) {
                HepaticStage.NONE -> 1.0
                HepaticStage.CHILD_A -> lerp(1.0, drugMultiplier, HEPATIC_CHILD_A_FACTOR)
                HepaticStage.CHILD_B -> lerp(1.0, drugMultiplier, HEPATIC_CHILD_B_FACTOR)
                HepaticStage.CHILD_C -> drugMultiplier
            }

        private fun lerp(
            a: Double,
            b: Double,
            t: Double,
        ) = a + (b - a) * t

        private fun applyCeiling(
            dose: Double,
            maxDose: Double?,
        ): Pair<Double, Boolean> {
            if (maxDose == null) return Pair(dose, false)
            return if (dose > maxDose) {
                Pair(maxDose, true)
            } else {
                Pair(dose, false)
            }
        }
    }
