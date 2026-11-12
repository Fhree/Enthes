package com.enthes.data.mapper

import com.enthes.database.EnthesQueries
import com.enthes.database.Historic_disease_activity
import com.enthes.domain.model.DiseaseActivity

fun Historic_disease_activity.toDomain(): DiseaseActivity {
    return DiseaseActivity(
        id = id,
        entryDate = entry_date,
        morningStiffnessMinutes = morning_stiffness_minutes?.toInt() ?: 0,
        globalPainLevel = global_pain_level.toInt(),
        fatigueLevel = fatigue_level?.toInt() ?: 0,
        hasActiveFlare = has_active_flare == 1L,
        createdAt = created_at,
    )
}

fun DiseaseActivity.toEntity(): Historic_disease_activity {
    return Historic_disease_activity(
        id = id,
        entry_date = entryDate,
        morning_stiffness_minutes = morningStiffnessMinutes.toLong(),
        global_pain_level = globalPainLevel.toLong(),
        fatigue_level = fatigueLevel.toLong(),
        has_active_flare = if (hasActiveFlare) 1L else 0L,
        created_at = createdAt,
    )
}

fun EnthesQueries.insertDiseaseActivity(activity: DiseaseActivity) {
    insertDiseaseActivity(
        id = activity.id,
        entry_date = activity.entryDate,
        morning_stiffness_minutes = activity.morningStiffnessMinutes.toLong(),
        global_pain_level = activity.globalPainLevel.toLong(),
        fatigue_level = activity.fatigueLevel.toLong(),
        has_active_flare = if (activity.hasActiveFlare) 1L else 0L,
        created_at = activity.createdAt,
    )
}
