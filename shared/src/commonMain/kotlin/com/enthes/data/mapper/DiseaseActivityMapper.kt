package com.enthes.data.mapper

import com.enthes.database.EnthesQueries
import com.enthes.database.Historic_disease_activity
import com.enthes.domain.model.DiseaseActivity
import com.enthes.domain.model.PainZone
import kotlinx.datetime.LocalDate

private const val PAIN_ZONES_DELIMITER = ","

fun Historic_disease_activity.toDomain(): DiseaseActivity {
    return DiseaseActivity(
        id = id,
        entryDate = LocalDate.parse(entry_date),
        morningStiffnessMinutes = morning_stiffness_minutes?.toInt() ?: 0,
        globalPainLevel = global_pain_level.toInt(),
        fatigueLevel = fatigue_level?.toInt() ?: 0,
        hasActiveFlare = has_active_flare == 1L,
        createdAt = created_at,
        painZones = pain_zones.toPainZones(),
    )
}

fun DiseaseActivity.toEntity(): Historic_disease_activity {
    return Historic_disease_activity(
        id = id,
        entry_date = entryDate.toString(),
        morning_stiffness_minutes = morningStiffnessMinutes.toLong(),
        global_pain_level = globalPainLevel.toLong(),
        fatigue_level = fatigueLevel.toLong(),
        has_active_flare = if (hasActiveFlare) 1L else 0L,
        created_at = createdAt,
        pain_zones = painZones.toStorageString(),
    )
}

fun EnthesQueries.insertDiseaseActivity(activity: DiseaseActivity) {
    insertDiseaseActivity(
        id = activity.id,
        entry_date = activity.entryDate.toString(),
        morning_stiffness_minutes = activity.morningStiffnessMinutes.toLong(),
        global_pain_level = activity.globalPainLevel.toLong(),
        fatigue_level = activity.fatigueLevel.toLong(),
        has_active_flare = if (activity.hasActiveFlare) 1L else 0L,
        created_at = activity.createdAt,
        pain_zones = activity.painZones.toStorageString(),
    )
}

fun EnthesQueries.updateActivity(activity: DiseaseActivity) {
    updateActivity(
        entry_date = activity.entryDate.toString(),
        morning_stiffness_minutes = activity.morningStiffnessMinutes.toLong(),
        global_pain_level = activity.globalPainLevel.toLong(),
        fatigue_level = activity.fatigueLevel.toLong(),
        has_active_flare = if (activity.hasActiveFlare) 1L else 0L,
        pain_zones = activity.painZones.toStorageString(),
        id = activity.id,
    )
}

private fun Set<PainZone>.toStorageString(): String =
    sortedBy { it.ordinal }.joinToString(separator = PAIN_ZONES_DELIMITER) { it.name }

private fun String.toPainZones(): Set<PainZone> {
    if (isBlank()) return emptySet()
    return split(PAIN_ZONES_DELIMITER)
        .asSequence()
        .map(String::trim)
        .filter(String::isNotEmpty)
        .mapNotNull { token -> PainZone.entries.firstOrNull { it.name == token } }
        .toSet()
}
