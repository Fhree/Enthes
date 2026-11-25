package com.enthes.di

import app.cash.sqldelight.db.SqlDriver
import com.enthes.database.createInMemorySqlDriver
import com.enthes.domain.model.DiseaseActivity
import com.enthes.domain.repository.DiseaseActivityRepository
import com.enthes.presentation.checkin.CheckinWizardViewModel
import com.enthes.presentation.dashboard.DashboardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class KoinModuleTest : KoinTest {

    private lateinit var sqlDriver: SqlDriver

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
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
        Dispatchers.resetMain()
    }

    @Test
    fun testKoinResolvesDiseaseActivityRepositoryWithInMemoryDriver() = runBlocking {
        val repository = get<DiseaseActivityRepository>()
        assertNotNull(repository)

        val activity = DiseaseActivity(
            id = "di-test-1",
            entryDate = LocalDate(2026, 11, 17),
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

    @Test
    fun testKoinResolvesClockAndCheckinWizardViewModel() {
        val clock = get<Clock>()
        assertNotNull(clock)

        val createViewModel = get<CheckinWizardViewModel>()
        assertNotNull(createViewModel)
        assertFalse(createViewModel.uiState.value.isEditMode)

        val editViewModel = get<CheckinWizardViewModel> { parametersOf("di-test-1") }
        assertNotNull(editViewModel)
        assertTrue(editViewModel.uiState.value.isEditMode)
    }
}
