package com.example.dosagecalc.presentation.ui.util

import com.example.dosagecalc.domain.model.FormulaType
import java.util.Locale

internal fun FormulaType.label(): String =
    when (this) {
        FormulaType.PER_KG -> "per kg"
        FormulaType.PER_M2 -> "per m²"
        FormulaType.FIXED -> "dose fissa"
        FormulaType.BY_RANGE -> "per fascia"
    }

internal fun Double.formatDose(): String =
    if (this == toLong().toDouble()) {
        toLong().toString()
    } else {
        String.format(Locale.US, "%.2f", this)
    }
