package com.example.dosagecalc.domain.usecase

import com.example.dosagecalc.domain.repository.BsaFormulaType
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sqrt

private const val MAX_PHYSIOLOGICAL_WEIGHT = 500.0
private const val MAX_PHYSIOLOGICAL_HEIGHT = 300.0
private const val MOSTELLER_DIVISOR = 3600.0
private const val DU_BOIS_COEFFICIENT = 0.007184
private const val DU_BOIS_HEIGHT_POWER = 0.725
private const val DU_BOIS_WEIGHT_POWER = 0.425
private const val ROUNDING_MULTIPLIER = 10_000.0

class CalculateBsaUseCase
    @Inject
    constructor() {
        operator fun invoke(
            weightKg: Double,
            heightCm: Double,
            formula: BsaFormulaType = BsaFormulaType.MOSTELLER,
        ): Double {
            require(weightKg > 0) { "Il peso deve essere maggiore di 0 kg, ricevuto: $weightKg" }
            require(heightCm > 0) { "L'altezza deve essere maggiore di 0 cm, ricevuta: $heightCm" }
            require(weightKg <= MAX_PHYSIOLOGICAL_WEIGHT) { "Peso non fisiologico: $weightKg kg" }
            require(heightCm <= MAX_PHYSIOLOGICAL_HEIGHT) { "Altezza non fisiologica: $heightCm cm" }

            val bsa =
                when (formula) {
                    BsaFormulaType.MOSTELLER -> {
                        sqrt((heightCm * weightKg) / MOSTELLER_DIVISOR)
                    }
                    BsaFormulaType.DU_BOIS -> {
                        DU_BOIS_COEFFICIENT * heightCm.pow(DU_BOIS_HEIGHT_POWER) * weightKg.pow(DU_BOIS_WEIGHT_POWER)
                    }
                }

            return (bsa * ROUNDING_MULTIPLIER).roundToLong() / ROUNDING_MULTIPLIER
        }
    }
