package com.enthes.data.repository

import app.cash.sqldelight.db.SqlDriver
import com.enthes.database.EnthesDatabase
import com.enthes.database.createInMemorySqlDriver
import com.enthes.domain.model.DiseaseActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
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
            entryDate = "2026-11-12",
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
            entryDate = "2026-11-12",
            morningStiffnessMinutes = 30,
            globalPainLevel = 5,
            fatigueLevel = 4,
            hasActiveFlare = false,
            createdAt = 1710000000000L,
        )

        val activity2 = DiseaseActivity(
            id = "2",
            entryDate = "2026-11-12",
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
    }
}
