package com.enthes.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DiseaseActivityTest {

    @Test
    fun validDiseaseActivityCreationSucceeds() {
        val activity = DiseaseActivity(
            id = "1",
            entryDate = "2026-11-12",
            morningStiffnessMinutes = 30,
            globalPainLevel = 5,
            fatigueLevel = 4,
            hasActiveFlare = true,
            createdAt = 1710000000000L,
        )

        assertEquals("1", activity.id)
        assertEquals("2026-11-12", activity.entryDate)
        assertEquals(30, activity.morningStiffnessMinutes)
        assertEquals(5, activity.globalPainLevel)
        assertEquals(4, activity.fatigueLevel)
        assertTrue(activity.hasActiveFlare)
        assertEquals(1710000000000L, activity.createdAt)
    }

    @Test
    fun boundaryValuesForPainAndFatigueAreAccepted() {
        val activityMin = DiseaseActivity(
            id = "min",
            entryDate = "2026-11-10",
            morningStiffnessMinutes = 0,
            globalPainLevel = 0,
            fatigueLevel = 0,
            hasActiveFlare = false,
            createdAt = 1710000000000L,
        )
        assertEquals(0, activityMin.globalPainLevel)
        assertEquals(0, activityMin.fatigueLevel)
        assertEquals(0, activityMin.morningStiffnessMinutes)

        val activityMax = DiseaseActivity(
            id = "max",
            entryDate = "2026-11-11",
            morningStiffnessMinutes = 300,
            globalPainLevel = 10,
            fatigueLevel = 10,
            hasActiveFlare = true,
            createdAt = 1710000000000L,
        )
        assertEquals(10, activityMax.globalPainLevel)
        assertEquals(10, activityMax.fatigueLevel)
    }

    @Test
    fun negativeGlobalPainLevelThrowsIllegalArgumentException() {
        assertFailsWith<IllegalArgumentException> {
            DiseaseActivity(
                id = "1",
                entryDate = "2026-11-12",
                morningStiffnessMinutes = 0,
                globalPainLevel = -1,
                fatigueLevel = 5,
                hasActiveFlare = false,
                createdAt = 1710000000000L,
            )
        }
    }

    @Test
    fun globalPainLevelAboveTenThrowsIllegalArgumentException() {
        assertFailsWith<IllegalArgumentException> {
            DiseaseActivity(
                id = "1",
                entryDate = "2026-11-12",
                morningStiffnessMinutes = 0,
                globalPainLevel = 11,
                fatigueLevel = 5,
                hasActiveFlare = false,
                createdAt = 1710000000000L,
            )
        }
    }

    @Test
    fun negativeFatigueLevelThrowsIllegalArgumentException() {
        assertFailsWith<IllegalArgumentException> {
            DiseaseActivity(
                id = "1",
                entryDate = "2026-11-12",
                morningStiffnessMinutes = 0,
                globalPainLevel = 5,
                fatigueLevel = -1,
                hasActiveFlare = false,
                createdAt = 1710000000000L,
            )
        }
    }

    @Test
    fun fatigueLevelAboveTenThrowsIllegalArgumentException() {
        assertFailsWith<IllegalArgumentException> {
            DiseaseActivity(
                id = "1",
                entryDate = "2026-11-12",
                morningStiffnessMinutes = 0,
                globalPainLevel = 5,
                fatigueLevel = 11,
                hasActiveFlare = false,
                createdAt = 1710000000000L,
            )
        }
    }

    @Test
    fun negativeMorningStiffnessThrowsIllegalArgumentException() {
        assertFailsWith<IllegalArgumentException> {
            DiseaseActivity(
                id = "1",
                entryDate = "2026-11-12",
                morningStiffnessMinutes = -1,
                globalPainLevel = 5,
                fatigueLevel = 5,
                hasActiveFlare = false,
                createdAt = 1710000000000L,
            )
        }
    }
}
