package com.example.dosagecalc.data.model

import com.example.dosagecalc.domain.model.DrugInteraction
import com.example.dosagecalc.domain.model.InteractionRiskLevel
import kotlinx.serialization.Serializable

@Serializable
data class DrugInteractionDto(
    val drugId1: String,
    val drugId2: String,
    val riskLevel: String,
    val description: String,
) {
    fun toDomain(): DrugInteraction =
        DrugInteraction(
            drugId1 = drugId1,
            drugId2 = drugId2,
            riskLevel =
                runCatching { InteractionRiskLevel.valueOf(riskLevel.uppercase()) }
                    .getOrDefault(InteractionRiskLevel.LOW),
            description = description,
        )
}
