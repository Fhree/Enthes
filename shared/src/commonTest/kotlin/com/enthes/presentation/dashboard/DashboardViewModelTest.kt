package com.enthes.presentation.dashboard

import com.enthes.data.repository.FakeDiseaseActivityRepository
import com.enthes.domain.model.DiseaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeDiseaseActivityRepository
    private lateinit var viewModel: DashboardViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeDiseaseActivityRepository()
        viewModel = DashboardViewModel(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateIsLoadingThenTransitionsToEmptyWhenNoActivities() = runTest(testDispatcher) {
        assertEquals(DashboardUiState.Loading, viewModel.uiState.value)

        val states = mutableListOf<DashboardUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { states.add(it) }
        }

        advanceUntilIdle()

        assertEquals(listOf(DashboardUiState.Loading, DashboardUiState.Empty), states)
        assertEquals(DashboardUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun stateTransitionsFromLoadingToSuccessWhenActivitiesExist() = runTest(testDispatcher) {
        val activity = DiseaseActivity(
            id = "1",
            entryDate = LocalDate(2026, 11, 12),
            morningStiffnessMinutes = 30,
            globalPainLevel = 5,
            fatigueLevel = 4,
            hasActiveFlare = true,
            createdAt = 1710000000000L,
        )
        val repoWithActivities = FakeDiseaseActivityRepository(listOf(activity))
        val vm = DashboardViewModel(repoWithActivities)

        assertEquals(DashboardUiState.Loading, vm.uiState.value)

        val states = mutableListOf<DashboardUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.uiState.collect { states.add(it) }
        }

        advanceUntilIdle()

        assertEquals(
            listOf(
                DashboardUiState.Loading,
                DashboardUiState.Success(activities = listOf(activity), error = null),
            ),
            states,
        )
        assertEquals(
            DashboardUiState.Success(activities = listOf(activity), error = null),
            vm.uiState.value,
        )
    }

    @Test
    fun onDeleteActivityRemovesRecordAndTransitionsToEmptyWhenLastDeleted() = runTest(testDispatcher) {
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

        val repo = FakeDiseaseActivityRepository(listOf(activity1, activity2))
        val vm = DashboardViewModel(repo)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.uiState.collect {}
        }
        advanceUntilIdle()

        vm.onEvent(DashboardEvent.OnDeleteActivity(activity1.id))
        advanceUntilIdle()

        assertEquals(
            DashboardUiState.Success(activities = listOf(activity2), error = null),
            vm.uiState.value,
        )

        vm.onEvent(DashboardEvent.OnDeleteActivity(activity2.id))
        advanceUntilIdle()

        assertEquals(DashboardUiState.Empty, vm.uiState.value)
    }

    @Test
    fun onDeleteActivitySurfacesCleanErrorMessageOnFailure() = runTest(testDispatcher) {
        val activity = DiseaseActivity(
            id = "1",
            entryDate = LocalDate(2026, 11, 12),
            morningStiffnessMinutes = 20,
            globalPainLevel = 4,
            fatigueLevel = 3,
            hasActiveFlare = false,
            createdAt = 1710000000000L,
        )

        val repo = FakeDiseaseActivityRepository(listOf(activity))
        repo.shouldFailOnDelete = true
        repo.deleteFailureException = IllegalStateException("SQLITE_IOERR: disk I/O error")
        val vm = DashboardViewModel(repo)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.uiState.collect {}
        }
        advanceUntilIdle()

        vm.onEvent(DashboardEvent.OnDeleteActivity(activity.id))
        advanceUntilIdle()

        val currentState = vm.uiState.value as DashboardUiState.Success
        assertEquals(listOf(activity), currentState.activities)
        assertEquals(DashboardError.DeleteFailed, currentState.error)
    }

    @Test
    fun onDismissErrorClearsDeleteError() = runTest(testDispatcher) {
        val activity = DiseaseActivity(
            id = "1",
            entryDate = LocalDate(2026, 11, 12),
            morningStiffnessMinutes = 30,
            globalPainLevel = 5,
            fatigueLevel = 4,
            hasActiveFlare = false,
            createdAt = 1710000000000L,
        )

        val repo = FakeDiseaseActivityRepository(listOf(activity))
        repo.shouldFailOnDelete = true
        val vm = DashboardViewModel(repo)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.uiState.collect {}
        }
        advanceUntilIdle()

        vm.onEvent(DashboardEvent.OnDeleteActivity(activity.id))
        advanceUntilIdle()

        val errorState = vm.uiState.value as DashboardUiState.Success
        assertNotNull(errorState.error)

        vm.onEvent(DashboardEvent.OnDismissError)
        advanceUntilIdle()

        val dismissedState = vm.uiState.value as DashboardUiState.Success
        assertNull(dismissedState.error)
    }
}
