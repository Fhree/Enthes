package com.enthes.repository

import com.enthes.database.DatabaseDriverFactory
import com.enthes.database.EnthesDatabase
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import com.enthes.database.Historic_disease_activity

class DiseaseActivityRepository(driverFactory: DatabaseDriverFactory) {

    private val database = EnthesDatabase(driverFactory.createDriver())
    private val queries = database.enthesQueries

    fun insertActivity(
        id: String,
        entryDate: String,
        morningStiffnessMinutes: Long?,
        globalPainLevel: Long,
        fatigueLevel: Long?,
        hasActiveFlare: Boolean,
        createdAt: Long
    ) {
        queries.insertDiseaseActivity(
            id = id,
            entry_date = entryDate,
            morning_stiffness_minutes = morningStiffnessMinutes,
            global_pain_level = globalPainLevel,
            fatigue_level = fatigueLevel,
            has_active_flare = if (hasActiveFlare) 1L else 0L,
            created_at = createdAt
        )
    }

    fun getAllActivitiesAsFlow(): Flow<List<Historic_disease_activity>> {
        return queries.selectAllDiseaseActivities()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }
}