package com.example.dosagecalc.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.dosagecalc.domain.repository.BsaFormulaType
import com.example.dosagecalc.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ThemeRepositoryImpl
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) : ThemeRepository {
        private val isDarkKey = booleanPreferencesKey("is_dark_theme")
        private val bsaFormulaKey = stringPreferencesKey("bsa_formula")

        override val isDarkTheme: Flow<Boolean> =
            dataStore.data.map { prefs ->
                prefs[isDarkKey] ?: false
            }

        override suspend fun setDarkTheme(dark: Boolean) {
            dataStore.edit { it[isDarkKey] = dark }
        }

        @Suppress("SwallowedException")
        override val bsaFormula: Flow<BsaFormulaType> =
            dataStore.data.map { prefs ->
                val name = prefs[bsaFormulaKey] ?: BsaFormulaType.MOSTELLER.name
                try {
                    BsaFormulaType.valueOf(name)
                } catch (e: IllegalArgumentException) {
                    BsaFormulaType.MOSTELLER
                }
            }

        override suspend fun setBsaFormula(formula: BsaFormulaType) {
            dataStore.edit { it[bsaFormulaKey] = formula.name }
        }
    }
