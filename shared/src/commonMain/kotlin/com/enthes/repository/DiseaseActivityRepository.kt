package com.enthes.repository

import com.enthes.database.DatabaseDriverFactory
import com.enthes.database.EnthesDatabase

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

    fun getAllActivities() = queries.selectAllDiseaseActivities().executeAsList()
}