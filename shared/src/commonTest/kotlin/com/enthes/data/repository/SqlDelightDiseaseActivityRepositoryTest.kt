package com.enthes.data.repository

import app.cash.sqldelight.db.SqlDriver
import com.enthes.database.EnthesDatabase
import com.enthes.database.createInMemorySqlDriver
import com.enthes.domain.error.DuplicateEntryDateException
import com.enthes.domain.model.DiseaseActivity
import com.enthes.domain.model.PainZone
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SqlDelightDiseaseActivityRepositoryTest {

    private lateinit var sqlDriver: SqlDriver
    private lateinit var database: EnthesDatabase
    private lateinit var repository: SqlDelightDiseaseActivityRepository

    @BeforeTest
    fun setUp() {
        sqlDriver = createInMemorySqlDriver()
        database = EnthesDatabase(sqlDriver)
        repository = SqlDelightDiseaseActivityRepository(database)
    }

    @AfterTest
    fun tearDown() {
        sqlDriver.close()
    }

    @Test
    fun insertAndGetAllActivitiesSucceeds() = runBlocking {
        val activity = DiseaseActivity(
            id = "1",
            entryDate = LocalDate(2026, 11, 12),
            morningStiffnessMinutes = 30,
            globalPainLevel = 5,
            fatigueLevel = 4,
            hasActiveFlare = true,
            createdAt = 1710000000000L,
        )

        val result = repository.insertActivity(activity)
        assertTrue(result.isSuccess)

        val activities = repository.getAllActivities().first()
        assertEquals(1, activities.size)
        assertEquals(activity, activities[0])
    }

    @Test
    fun insertDuplicateEntryDateReturnsFailureResult() = runBlocking {
        val activity1 = DiseaseActivity(
            id = "1",
            entryDate = LocalDate(2026, 11, 12),
            morningStiffnessMinutes = 30,
            globalPainLevel = 5,
            fatigueLevel = 4,
            hasActiveFlare = false,
            createdAt = 1710000000000L,
        )

        val activity2 = DiseaseActivity(
            id = "2",
            entryDate = LocalDate(2026, 11, 12),
            morningStiffnessMinutes = 15,
            globalPainLevel = 2,
            fatigueLevel = 1,
            hasActiveFlare = true,
            createdAt = 1710000050000L,
        )

        val result1 = repository.insertActivity(activity1)
        assertTrue(result1.isSuccess)

        val result2 = repository.insertActivity(activity2)
        assertTrue(result2.isFailure)
        val exception = assertIs<DuplicateEntryDateException>(result2.exceptionOrNull())
        assertEquals(LocalDate(2026, 11, 12), exception.entryDate)
    }

    @Test
    fun deleteActivityRemovesOnlyTargetRecordAndUpdatesFlow() = runBlocking {
        val activity1 = DiseaseActivity(
            id = "1",
            entryDate = LocalDate(2026, 11, 12),
            morningStiffnessMinutes = 30,
            globalPainLevel = 5,
            fatigueLevel = 4,
            hasActiveFlare = false,
            createdAt = 1710000000000L,
        )
        val activity2 = DiseaseActivity(
            id = "2",
            entryDate = LocalDate(2026, 11, 13),
            morningStiffnessMinutes = 15,
            globalPainLevel = 2,
            fatigueLevel = 1,
            hasActiveFlare = true,
            createdAt = 1710086400000L,
        )

        assertTrue(repository.insertActivity(activity1).isSuccess)
        assertTrue(repository.insertActivity(activity2).isSuccess)

        val deleteResult = repository.deleteActivity(activity1.id)
        assertTrue(deleteResult.isSuccess)

        val remainingActivities = repository.getAllActivities().first()
        assertEquals(listOf(activity2), remainingActivities)
    }

    @Test
    fun updateActivityPersistsModifiedFieldsAndUpdatesFlow() = runBlocking {
        val activity1 = DiseaseActivity(
            id = "1",
            entryDate = LocalDate(2026, 11, 12),
            morningStiffnessMinutes = 30,
            globalPainLevel = 5,
            fatigueLevel = 4,
            hasActiveFlare = false,
            createdAt = 1710000000000L,
        )
        val activity2 = DiseaseActivity(
            id = "2",
            entryDate = LocalDate(2026, 11, 13),
            morningStiffnessMinutes = 15,
            globalPainLevel = 2,
            fatigueLevel = 1,
            hasActiveFlare = true,
            createdAt = 1710086400000L,
        )

        assertTrue(repository.insertActivity(activity1).isSuccess)
        assertTrue(repository.insertActivity(activity2).isSuccess)

        val updatedActivity1 = activity1.copy(
            entryDate = LocalDate(2026, 11, 15),
            morningStiffnessMinutes = 60,
            globalPainLevel = 8,
            fatigueLevel = 7,
            hasActiveFlare = true,
        )

        val updateResult = repository.updateActivity(updatedActivity1)
        assertTrue(updateResult.isSuccess)

        val activities = repository.getAllActivities().first()
        assertEquals(listOf(updatedActivity1, activity2), activities)
    }

    @Test
    fun updateActivityWithDuplicateEntryDateReturnsFailureResult() = runBlocking {
        val activity1 = DiseaseActivity(
            id = "1",
            entryDate = LocalDate(2026, 11, 12),
            morningStiffnessMinutes = 30,
            globalPainLevel = 5,
            fatigueLevel = 4,
            hasActiveFlare = false,
            createdAt = 1710000000000L,
        )
        val activity2 = DiseaseActivity(
            id = "2",
            entryDate = LocalDate(2026, 11, 13),
            morningStiffnessMinutes = 15,
            globalPainLevel = 2,
            fatigueLevel = 1,
            hasActiveFlare = true,
            createdAt = 1710086400000L,
        )

        assertTrue(repository.insertActivity(activity1).isSuccess)
        assertTrue(repository.insertActivity(activity2).isSuccess)

        val conflictingUpdate = activity2.copy(entryDate = LocalDate(2026, 11, 12))
        val updateResult = repository.updateActivity(conflictingUpdate)

        assertTrue(updateResult.isFailure)
        val exception = assertIs<DuplicateEntryDateException>(updateResult.exceptionOrNull())
        assertEquals(LocalDate(2026, 11, 12), exception.entryDate)

        val activities = repository.getAllActivities().first()
        assertEquals(listOf(activity2, activity1), activities)
    }

    @Test
    fun getActivityByIdReturnsPersistedActivityOrNull() = runBlocking {
        val activity = DiseaseActivity(
            id = "lookup-1",
            entryDate = LocalDate(2026, 11, 20),
            morningStiffnessMinutes = 40,
            globalPainLevel = 6,
            fatigueLevel = 5,
            hasActiveFlare = true,
            createdAt = 1710100000000L,
            painZones = setOf(PainZone.LUMBAR, PainZone.FEET_HEELS),
        )

        assertTrue(repository.insertActivity(activity).isSuccess)

        assertEquals(activity, repository.getActivityById("lookup-1"))
        assertNull(repository.getActivityById("unknown-id"))
    }

    @Test
    fun crudPersistencePreservesPainZonesAcrossInsertUpdateAndDelete() = runBlocking {
        val initial = DiseaseActivity(
            id = "zones-1",
            entryDate = LocalDate(2026, 11, 21),
            morningStiffnessMinutes = 25,
            globalPainLevel = 4,
            fatigueLevel = 3,
            hasActiveFlare = false,
            createdAt = 1710200000000L,
            painZones = setOf(PainZone.KNEES),
        )

        assertTrue(repository.insertActivity(initial).isSuccess)
        assertEquals(setOf(PainZone.KNEES), repository.getActivityById(initial.id)?.painZones)

        val updatedMultiple = initial.copy(
            painZones = setOf(PainZone.CERVICAL, PainZone.HANDS, PainZone.SHOULDERS_ELBOWS),
        )
        assertTrue(repository.updateActivity(updatedMultiple).isSuccess)
        assertEquals(updatedMultiple, repository.getActivityById(initial.id))

        val clearedZones = updatedMultiple.copy(painZones = emptySet())
        assertTrue(repository.updateActivity(clearedZones).isSuccess)
        assertEquals(emptySet(), repository.getActivityById(initial.id)?.painZones)

        assertTrue(repository.deleteActivity(initial.id).isSuccess)
        assertNull(repository.getActivityById(initial.id))
    }
}
