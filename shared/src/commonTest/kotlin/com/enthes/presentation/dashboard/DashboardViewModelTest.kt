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
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
            entryDate = "2026-11-12",
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
                DashboardUiState.Success(activities = listOf(activity), errorMessage = null),
            ),
            states,
        )
        assertEquals(
            DashboardUiState.Success(activities = listOf(activity), errorMessage = null),
            vm.uiState.value,
        )
    }

    @Test
    fun onSaveCheckinSurfacesErrorOnDuplicateDate() = runTest(testDispatcher) {
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

        val repo = FakeDiseaseActivityRepository(listOf(activity1))
        val vm = DashboardViewModel(repo)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.uiState.collect {}
        }
        advanceUntilIdle()

        assertEquals(
            DashboardUiState.Success(activities = listOf(activity1), errorMessage = null),
            vm.uiState.value,
        )

        vm.onEvent(DashboardEvent.OnSaveCheckin(activity2))
        advanceUntilIdle()

        val currentState = vm.uiState.value
        assertTrue(currentState is DashboardUiState.Success)
        assertEquals(listOf(activity1), currentState.activities)
        assertEquals("A check-in for 2026-11-12 already exists.", currentState.errorMessage)
    }

    @Test
    fun onDismissErrorClearsErrorMessage() = runTest(testDispatcher) {
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

        val repo = FakeDiseaseActivityRepository(listOf(activity1))
        val vm = DashboardViewModel(repo)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.uiState.collect {}
        }
        advanceUntilIdle()

        vm.onEvent(DashboardEvent.OnSaveCheckin(activity2))
        advanceUntilIdle()

        val errorState = vm.uiState.value as DashboardUiState.Success
        assertNotNull(errorState.errorMessage)

        vm.onEvent(DashboardEvent.OnDismissError)
        advanceUntilIdle()

        val dismissedState = vm.uiState.value as DashboardUiState.Success
        assertNull(dismissedState.errorMessage)
    }

    @Test
    fun onSaveCheckinWithNewDateInsertsActivitySuccessfully() = runTest(testDispatcher) {
        val activity1 = DiseaseActivity(
            id = "1",
            entryDate = "2026-11-12",
            morningStiffnessMinutes = 30,
            globalPainLevel = 5,
            fatigueLevel = 4,
            hasActiveFlare = false,
            createdAt = 1710000000000L,
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()
        assertEquals(DashboardUiState.Empty, viewModel.uiState.value)

        viewModel.onEvent(DashboardEvent.OnSaveCheckin(activity1))
        advanceUntilIdle()

        assertEquals(
            DashboardUiState.Success(activities = listOf(activity1), errorMessage = null),
            viewModel.uiState.value,
        )
    }

    @Test
    fun onSaveCheckinDoesNotLeakRawErrorMessagesOnUnexpectedFailure() = runTest(testDispatcher) {
        val activity = DiseaseActivity(
            id = "1",
            entryDate = "2026-11-12",
            morningStiffnessMinutes = 20,
            globalPainLevel = 4,
            fatigueLevel = 3,
            hasActiveFlare = false,
            createdAt = 1710000000000L,
        )

        repository.shouldFailOnInsert = true
        repository.failureException = IllegalStateException("SQLITE_FULL: database or disk is full")

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(DashboardEvent.OnSaveCheckin(activity))
        advanceUntilIdle()

        val currentState = viewModel.uiState.value as DashboardUiState.Success
        assertEquals("Failed to save check-in. Please try again.", currentState.errorMessage)
    }
}
