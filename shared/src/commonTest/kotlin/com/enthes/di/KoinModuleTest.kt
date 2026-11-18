package com.enthes.di

import app.cash.sqldelight.db.SqlDriver
import com.enthes.database.createInMemorySqlDriver
import com.enthes.domain.model.DiseaseActivity
import com.enthes.domain.repository.DiseaseActivityRepository
import com.enthes.presentation.dashboard.DashboardViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class KoinModuleTest : KoinTest {

    private lateinit var sqlDriver: SqlDriver

    @BeforeTest
    fun setUp() {
        sqlDriver = createInMemorySqlDriver()
        startKoin {
            modules(
                dataModule,
                presentationModule,
                module {
                    single<SqlDriver> { sqlDriver }
                },
            )
        }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
        sqlDriver.close()
    }

    @Test
    fun testKoinResolvesDiseaseActivityRepositoryWithInMemoryDriver() = runBlocking {
        val repository = get<DiseaseActivityRepository>()
        assertNotNull(repository)

        val activity = DiseaseActivity(
            id = "di-test-1",
            entryDate = "2026-11-17",
            morningStiffnessMinutes = 20,
            globalPainLevel = 4,
            fatigueLevel = 3,
            hasActiveFlare = false,
            createdAt = 1710000000000L,
        )

        val result = repository.insertActivity(activity)
        assertTrue(result.isSuccess)

        val activities = repository.getAllActivities().first()
        assertEquals(1, activities.size)
        assertEquals(activity, activities[0])
    }

    @Test
    fun testKoinResolvesDashboardViewModel() {
        val viewModel = get<DashboardViewModel>()
        assertNotNull(viewModel)
    }
}
