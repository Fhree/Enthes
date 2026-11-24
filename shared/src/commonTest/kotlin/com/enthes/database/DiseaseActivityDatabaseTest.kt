package com.enthes.database

import app.cash.sqldelight.db.SqlDriver
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DiseaseActivityDatabaseTest {

    private lateinit var sqlDriver: SqlDriver
    private lateinit var database: EnthesDatabase

    @BeforeTest
    fun setUp() {
        sqlDriver = createInMemorySqlDriver()
        database = EnthesDatabase(sqlDriver)
    }

    @AfterTest
    fun tearDown() {
        sqlDriver.close()
    }

    @Test
    fun testInsertAndSelectOrderedByEntryDateDesc() {
        val queries = database.enthesQueries

        queries.insertDiseaseActivity(
            id = "1",
            entry_date = "2026-03-01",
            morning_stiffness_minutes = 30L,
            global_pain_level = 4L,
            fatigue_level = 3L,
            has_active_flare = 0L,
            created_at = 1710000000000L
        )

        queries.insertDiseaseActivity(
            id = "2",
            entry_date = "2026-03-02",
            morning_stiffness_minutes = 45L,
            global_pain_level = 6L,
            fatigue_level = 5L,
            has_active_flare = 1L,
            created_at = 1710086400000L
        )

        val results = queries.selectAllDiseaseActivities().executeAsList()

        assertEquals(2, results.size)
        assertEquals("2026-03-02", results[0].entry_date)
        assertEquals("2026-03-01", results[1].entry_date)
    }

    @Test
    fun testUniqueEntryDateConstraintFails() {
        val queries = database.enthesQueries

        queries.insertDiseaseActivity(
            id = "1",
            entry_date = "2026-03-01",
            morning_stiffness_minutes = 30L,
            global_pain_level = 4L,
            fatigue_level = 3L,
            has_active_flare = 0L,
            created_at = 1710000000000L
        )

        assertFailsWith<Exception> {
            queries.insertDiseaseActivity(
                id = "2",
                entry_date = "2026-03-01",
                morning_stiffness_minutes = 15L,
                global_pain_level = 2L,
                fatigue_level = 2L,
                has_active_flare = 0L,
                created_at = 1710000050000L
            )
        }
    }

    @Test
    fun testDeleteByIdRemovesTargetRecordWithoutAffectingOthers() {
        val queries = database.enthesQueries

        queries.insertDiseaseActivity(
            id = "1",
            entry_date = "2026-03-01",
            morning_stiffness_minutes = 30L,
            global_pain_level = 4L,
            fatigue_level = 3L,
            has_active_flare = 0L,
            created_at = 1710000000000L,
        )

        queries.insertDiseaseActivity(
            id = "2",
            entry_date = "2026-03-02",
            morning_stiffness_minutes = 45L,
            global_pain_level = 6L,
            fatigue_level = 5L,
            has_active_flare = 1L,
            created_at = 1710086400000L,
        )

        queries.deleteById("1")

        val results = queries.selectAllDiseaseActivities().executeAsList()

        assertEquals(1, results.size)
        assertEquals("2", results[0].id)
        assertEquals("2026-03-02", results[0].entry_date)
    }

    @Test
    fun testUpdateActivityPersistsModifiedFields() {
        val queries = database.enthesQueries

        queries.insertDiseaseActivity(
            id = "1",
            entry_date = "2026-03-01",
            morning_stiffness_minutes = 30L,
            global_pain_level = 4L,
            fatigue_level = 3L,
            has_active_flare = 0L,
            created_at = 1710000000000L,
        )
        queries.insertDiseaseActivity(
            id = "2",
            entry_date = "2026-03-02",
            morning_stiffness_minutes = 45L,
            global_pain_level = 6L,
            fatigue_level = 5L,
            has_active_flare = 1L,
            created_at = 1710086400000L,
        )

        queries.updateActivity(
            entry_date = "2026-03-05",
            morning_stiffness_minutes = 60L,
            global_pain_level = 8L,
            fatigue_level = 7L,
            has_active_flare = 1L,
            id = "1",
        )

        val results = queries.selectAllDiseaseActivities().executeAsList()

        assertEquals(2, results.size)
        assertEquals("1", results[0].id)
        assertEquals("2026-03-05", results[0].entry_date)
        assertEquals(60L, results[0].morning_stiffness_minutes)
        assertEquals(8L, results[0].global_pain_level)
        assertEquals(7L, results[0].fatigue_level)
        assertEquals(1L, results[0].has_active_flare)
        assertEquals(1710000000000L, results[0].created_at)

        assertEquals("2", results[1].id)
        assertEquals("2026-03-02", results[1].entry_date)
    }

    @Test
    fun testUpdateActivityWithDuplicateEntryDateFails() {
        val queries = database.enthesQueries

        queries.insertDiseaseActivity(
            id = "1",
            entry_date = "2026-03-01",
            morning_stiffness_minutes = 30L,
            global_pain_level = 4L,
            fatigue_level = 3L,
            has_active_flare = 0L,
            created_at = 1710000000000L,
        )
        queries.insertDiseaseActivity(
            id = "2",
            entry_date = "2026-03-02",
            morning_stiffness_minutes = 45L,
            global_pain_level = 6L,
            fatigue_level = 5L,
            has_active_flare = 1L,
            created_at = 1710086400000L,
        )

        assertFailsWith<Exception> {
            queries.updateActivity(
                entry_date = "2026-03-02",
                morning_stiffness_minutes = 15L,
                global_pain_level = 2L,
                fatigue_level = 2L,
                has_active_flare = 0L,
                id = "1",
            )
        }
    }
}
