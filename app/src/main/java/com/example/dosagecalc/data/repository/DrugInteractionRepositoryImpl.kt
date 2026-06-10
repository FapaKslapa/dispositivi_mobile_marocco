package com.example.dosagecalc.data.repository

import android.content.Context
import com.example.dosagecalc.data.model.DrugInteractionDto
import com.example.dosagecalc.domain.model.DrugInteraction
import com.example.dosagecalc.domain.repository.DrugInteractionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject

class DrugInteractionRepositoryImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val json: Json,
    ) : DrugInteractionRepository {
        private val fileName = "interactions.json"

        override fun getInteractions(): Flow<List<DrugInteraction>> =
            flow {
                val interactions = loadInteractions()
                emit(interactions.map { it.toDomain() })
            }

        override suspend fun checkInteraction(
            drugId1: String,
            drugId2: String,
        ): DrugInteraction? =
            loadInteractions()
                .map { it.toDomain() }
                .find {
                    (it.drugId1 == drugId1 && it.drugId2 == drugId2) ||
                        (it.drugId1 == drugId2 && it.drugId2 == drugId1)
                }

        @Suppress("SwallowedException")
        private fun loadInteractions(): List<DrugInteractionDto> =
            try {
                val jsonString = context.assets.open(fileName).use { it.bufferedReader().readText() }
                json.decodeFromString<List<DrugInteractionDto>>(jsonString)
            } catch (e: IOException) {
                emptyList()
            } catch (e: SerializationException) {
                emptyList()
            }
    }
