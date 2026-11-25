package com.enthes.data.mapper

import com.enthes.database.Historic_disease_activity
import com.enthes.domain.model.DiseaseActivity
import com.enthes.domain.model.PainZone
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DiseaseActivityMapperTest {

    @Test
    fun toDomainMapsFlareOneToTrue() {
        val entity = Historic_disease_activity(
            id = "test-1",
            entry_date = "2026-11-12",
            morning_stiffness_minutes = 45L,
            global_pain_level = 7L,
            fatigue_level = 6L,
            has_active_flare = 1L,
            created_at = 1710000000000L,
            pain_zones = "",
        )

        val domain = entity.toDomain()

        assertEquals("test-1", domain.id)
        assertEquals(LocalDate(2026, 11, 12), domain.entryDate)
        assertEquals(45, domain.morningStiffnessMinutes)
        assertEquals(7, domain.globalPainLevel)
        assertEquals(6, domain.fatigueLevel)
        assertTrue(domain.hasActiveFlare)
        assertEquals(1710000000000L, domain.createdAt)
        assertEquals(emptySet(), domain.painZones)
    }

    @Test
    fun toDomainMapsFlareZeroToFalse() {
        val entity = Historic_disease_activity(
            id = "test-2",
            entry_date = "2026-11-13",
            morning_stiffness_minutes = 20L,
            global_pain_level = 3L,
            fatigue_level = 2L,
            has_active_flare = 0L,
            created_at = 1710000050000L,
            pain_zones = "",
        )

        val domain = entity.toDomain()

        assertFalse(domain.hasActiveFlare)
    }

    @Test
    fun toDomainHandlesNullableFieldsAsDefaultZero() {
        val entity = Historic_disease_activity(
            id = "test-3",
            entry_date = "2026-11-14",
            morning_stiffness_minutes = null,
            global_pain_level = 0L,
            fatigue_level = null,
            has_active_flare = 0L,
            created_at = 1710000100000L,
            pain_zones = "",
        )

        val domain = entity.toDomain()

        assertEquals(0, domain.morningStiffnessMinutes)
        assertEquals(0, domain.fatigueLevel)
    }

    @Test
    fun toEntityMapsTrueFlareToOneLong() {
        val domain = DiseaseActivity(
            id = "test-4",
            entryDate = LocalDate(2026, 11, 15),
            morningStiffnessMinutes = 60,
            globalPainLevel = 8,
            fatigueLevel = 9,
            hasActiveFlare = true,
            createdAt = 1710000200000L,
        )

        val entity = domain.toEntity()

        assertEquals("test-4", entity.id)
        assertEquals("2026-11-15", entity.entry_date)
        assertEquals(60L, entity.morning_stiffness_minutes)
        assertEquals(8L, entity.global_pain_level)
        assertEquals(9L, entity.fatigue_level)
        assertEquals(1L, entity.has_active_flare)
        assertEquals(1710000200000L, entity.created_at)
        assertEquals("", entity.pain_zones)
    }

    @Test
    fun toEntityMapsFalseFlareToZeroLong() {
        val domain = DiseaseActivity(
            id = "test-5",
            entryDate = LocalDate(2026, 11, 16),
            morningStiffnessMinutes = 10,
            globalPainLevel = 2,
            fatigueLevel = 1,
            hasActiveFlare = false,
            createdAt = 1710000300000L,
        )

        val entity = domain.toEntity()

        assertEquals(0L, entity.has_active_flare)
    }

    @Test
    fun painZonesMappingHandlesEmptySingleAndMultipleZones() {
        val emptyActivity = createDomainActivity(painZones = emptySet())
        val emptyEntity = emptyActivity.toEntity()
        assertEquals("", emptyEntity.pain_zones)
        assertEquals(emptySet(), emptyEntity.toDomain().painZones)

        val singleActivity = createDomainActivity(painZones = setOf(PainZone.CERVICAL))
        val singleEntity = singleActivity.toEntity()
        assertEquals("CERVICAL", singleEntity.pain_zones)
        assertEquals(setOf(PainZone.CERVICAL), singleEntity.toDomain().painZones)

        val multipleZones = setOf(
            PainZone.SHOULDERS_ELBOWS,
            PainZone.LUMBAR,
            PainZone.KNEES,
            PainZone.FEET_HEELS,
        )
        val multipleActivity = createDomainActivity(painZones = multipleZones)
        val multipleEntity = multipleActivity.toEntity()
        assertEquals("LUMBAR,KNEES,FEET_HEELS,SHOULDERS_ELBOWS", multipleEntity.pain_zones)
        assertEquals(multipleZones, multipleEntity.toDomain().painZones)
    }

    @Test
    fun toDomainIgnoresUnknownAndBlankPainZoneTokensSafely() {
        val entity = Historic_disease_activity(
            id = "test-6",
            entry_date = "2026-11-17",
            morning_stiffness_minutes = 15L,
            global_pain_level = 4L,
            fatigue_level = 3L,
            has_active_flare = 0L,
            created_at = 1710000400000L,
            pain_zones = " , LUMBAR , UNKNOWN_ZONE , , HANDS , ",
        )

        val domain = entity.toDomain()

        assertEquals(setOf(PainZone.LUMBAR, PainZone.HANDS), domain.painZones)
    }

    private fun createDomainActivity(painZones: Set<PainZone>): DiseaseActivity = DiseaseActivity(
        id = "zone-test",
        entryDate = LocalDate(2026, 11, 18),
        morningStiffnessMinutes = 30,
        globalPainLevel = 5,
        fatigueLevel = 4,
        hasActiveFlare = false,
        createdAt = 1710000500000L,
        painZones = painZones,
    )
}
