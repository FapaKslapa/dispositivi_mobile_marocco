package com.example.dosagecalc.domain.model

data class Drug(
    val id: String,
    val name: String,
    val indication: String,
    val formulaType: FormulaType,
    val unitDose: Double,
    val unitDoseMax: Double? = null,
    val unit: String,
    val minWeightKg: Double?,
    val maxWeightKg: Double?,
    val minAgeYears: Int?,
    val maxSingleDoseMcg: Double?,
    val alert: String,
    val source: String,
    val category: DrugCategory = DrugCategory.OTHER,
    val renalDoseMultiplier: Double? = null,
    val hepaticDoseMultiplier: Double? = null,
    val renalAlert: String? = null,
    val hepaticAlert: String? = null,
    val daysPerCycle: Int? = null,
    val numberOfCycles: Int? = null,
    val contraindications: String? = null,
    val sideEffects: String? = null,
)

enum class FormulaType {
    PER_KG,
    PER_M2,
    FIXED,
    BY_RANGE,
}

enum class RenalStage(
    val label: String,
    val gfrRange: String,
) {
    NONE("Normale (G1)", "GFR ≥ 90 ml/min"),
    G2("Lieve riduzione (G2)", "GFR 60–89 ml/min"),
    G3("IRC Lieve-Mod. (G3)", "GFR 30–59 ml/min"),
    G4("IRC Moderata-Grave (G4)", "GFR 15–29 ml/min"),
    G5("IRC Grave/Terminale (G5)", "GFR < 15 ml/min"),
}

enum class HepaticStage(
    val label: String,
    val description: String,
) {
    NONE("Nessuna (Normale)", ""),
    CHILD_A("Child-Pugh A", "Lieve - 5–6 punti"),
    CHILD_B("Child-Pugh B", "Moderata - 7–9 punti"),
    CHILD_C("Child-Pugh C", "Grave - 10–15 punti"),
}

enum class DrugCategory(
    val label: String,
) {
    DERMATOLOGY("Dermatologia"),
    ONCOLOGY("Oncologia"),
    INFECTIOUS("Infettivologia"),
    PEDIATRICS("Pediatria"),
    OTHER("Altro"),
}
