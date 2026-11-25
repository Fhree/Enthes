package com.enthes.presentation.checkin

import com.enthes.data.repository.FakeDiseaseActivityRepository
import com.enthes.domain.model.DiseaseActivity
import com.enthes.domain.model.PainZone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

private class FixedClock(private val instant: Instant) : Clock {
    override fun now(): Instant = instant
}

@OptIn(ExperimentalCoroutinesApi::class)
class CheckinWizardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fixedInstant = Instant.parse("2026-11-25T09:00:00Z")
    private val fixedDate = LocalDate(2026, 11, 25)
    private val fixedClock = FixedClock(fixedInstant)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun createModeAutoAdvancesThroughAllFiveStepsAndRecordsValues() = runTest(testDispatcher) {
        val repository = FakeDiseaseActivityRepository()
        val viewModel = CheckinWizardViewModel(
            repository = repository,
            clock = fixedClock,
            timeZone = TimeZone.UTC,
            idGenerator = { "generated-id-1" },
        )

        assertEquals(CheckinWizardStep.PAIN, viewModel.uiState.value.currentStep)
        assertEquals(fixedDate, viewModel.uiState.value.entryDate)
        assertFalse(viewModel.uiState.value.isEditMode)
        assertFalse(viewModel.uiState.value.hasActiveFlare)
        assertEquals(emptySet(), viewModel.uiState.value.painZones)

        viewModel.onEvent(CheckinWizardEvent.OnSelectPain(7))
        assertEquals(7, viewModel.uiState.value.globalPainLevel)
        assertEquals(CheckinWizardStep.FATIGUE, viewModel.uiState.value.currentStep)

        viewModel.onEvent(CheckinWizardEvent.OnSelectFatigue(5))
        assertEquals(5, viewModel.uiState.value.fatigueLevel)
        assertEquals(CheckinWizardStep.STIFFNESS, viewModel.uiState.value.currentStep)

        viewModel.onEvent(CheckinWizardEvent.OnSelectStiffness(45))
        assertEquals(45, viewModel.uiState.value.morningStiffnessMinutes)
        assertEquals(CheckinWizardStep.FLARE_AND_ZONES, viewModel.uiState.value.currentStep)

        viewModel.onEvent(CheckinWizardEvent.OnSetActiveFlare(true))
        viewModel.onEvent(CheckinWizardEvent.OnTogglePainZone(PainZone.LUMBAR))
        viewModel.onEvent(CheckinWizardEvent.OnTogglePainZone(PainZone.KNEES))
        viewModel.onEvent(CheckinWizardEvent.OnTogglePainZone(PainZone.KNEES))
        viewModel.onEvent(CheckinWizardEvent.OnTogglePainZone(PainZone.FEET_HEELS))
        assertTrue(viewModel.uiState.value.hasActiveFlare)
        assertEquals(setOf(PainZone.LUMBAR, PainZone.FEET_HEELS), viewModel.uiState.value.painZones)

        viewModel.onEvent(CheckinWizardEvent.OnProceedToSummary)
        assertEquals(CheckinWizardStep.SUMMARY, viewModel.uiState.value.currentStep)
        assertTrue(viewModel.uiState.value.canSave)
    }

    @Test
    fun createModeBackNavigationStepsBackwardsAndEmitsNavigateBackOnFirstStep() = runTest(testDispatcher) {
        val repository = FakeDiseaseActivityRepository()
        val viewModel = CheckinWizardViewModel(
            repository = repository,
            clock = fixedClock,
            timeZone = TimeZone.UTC,
        )
        val effects = mutableListOf<CheckinWizardEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects.add(it) }
        }

        viewModel.onEvent(CheckinWizardEvent.OnSelectPain(3))
        viewModel.onEvent(CheckinWizardEvent.OnSelectFatigue(4))
        viewModel.onEvent(CheckinWizardEvent.OnSelectStiffness(15))
        viewModel.onEvent(CheckinWizardEvent.OnProceedToSummary)
        assertEquals(CheckinWizardStep.SUMMARY, viewModel.uiState.value.currentStep)

        viewModel.onEvent(CheckinWizardEvent.OnBack)
        assertEquals(CheckinWizardStep.FLARE_AND_ZONES, viewModel.uiState.value.currentStep)

        viewModel.onEvent(CheckinWizardEvent.OnBack)
        assertEquals(CheckinWizardStep.STIFFNESS, viewModel.uiState.value.currentStep)

        viewModel.onEvent(CheckinWizardEvent.OnBack)
        assertEquals(CheckinWizardStep.FATIGUE, viewModel.uiState.value.currentStep)

        viewModel.onEvent(CheckinWizardEvent.OnBack)
        assertEquals(CheckinWizardStep.PAIN, viewModel.uiState.value.currentStep)
        assertTrue(effects.isEmpty())

        viewModel.onEvent(CheckinWizardEvent.OnBack)
        advanceUntilIdle()
        assertEquals(listOf<CheckinWizardEffect>(CheckinWizardEffect.NavigateBack), effects)
    }

    @Test
    fun jumpingFromSummaryReturnsToSummaryOnSelectionOrBack() = runTest(testDispatcher) {
        val repository = FakeDiseaseActivityRepository()
        val viewModel = CheckinWizardViewModel(
            repository = repository,
            clock = fixedClock,
            timeZone = TimeZone.UTC,
        )

        viewModel.onEvent(CheckinWizardEvent.OnSelectPain(3))
        viewModel.onEvent(CheckinWizardEvent.OnSelectFatigue(4))
        viewModel.onEvent(CheckinWizardEvent.OnSelectStiffness(15))
        viewModel.onEvent(CheckinWizardEvent.OnProceedToSummary)

        viewModel.onEvent(CheckinWizardEvent.OnJumpToStep(CheckinWizardStep.PAIN))
        assertEquals(CheckinWizardStep.PAIN, viewModel.uiState.value.currentStep)
        viewModel.onEvent(CheckinWizardEvent.OnSelectPain(8))
        assertEquals(8, viewModel.uiState.value.globalPainLevel)
        assertEquals(CheckinWizardStep.SUMMARY, viewModel.uiState.value.currentStep)

        viewModel.onEvent(CheckinWizardEvent.OnJumpToStep(CheckinWizardStep.STIFFNESS))
        assertEquals(CheckinWizardStep.STIFFNESS, viewModel.uiState.value.currentStep)
        viewModel.onEvent(CheckinWizardEvent.OnBack)
        assertEquals(15, viewModel.uiState.value.morningStiffnessMinutes)
        assertEquals(CheckinWizardStep.SUMMARY, viewModel.uiState.value.currentStep)
    }

    @Test
    fun createModeSavePersistsActivityWithClockAndUuidAndEmitsNavigateBack() = runTest(testDispatcher) {
        val repository = FakeDiseaseActivityRepository()
        val viewModel = CheckinWizardViewModel(
            repository = repository,
            clock = fixedClock,
            timeZone = TimeZone.UTC,
            idGenerator = { "uuid-42" },
        )
        val effects = mutableListOf<CheckinWizardEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects.add(it) }
        }

        viewModel.onEvent(CheckinWizardEvent.OnSelectPain(6))
        viewModel.onEvent(CheckinWizardEvent.OnSelectFatigue(7))
        viewModel.onEvent(CheckinWizardEvent.OnSelectStiffness(90))
        viewModel.onEvent(CheckinWizardEvent.OnSetActiveFlare(true))
        viewModel.onEvent(CheckinWizardEvent.OnTogglePainZone(PainZone.CERVICAL))
        viewModel.onEvent(CheckinWizardEvent.OnProceedToSummary)
        viewModel.onEvent(CheckinWizardEvent.OnSave)
        advanceUntilIdle()

        val savedActivities = repository.getAllActivities().first()
        assertEquals(
            listOf(
                DiseaseActivity(
                    id = "uuid-42",
                    entryDate = fixedDate,
                    morningStiffnessMinutes = 90,
                    globalPainLevel = 6,
                    fatigueLevel = 7,
                    hasActiveFlare = true,
                    createdAt = fixedInstant.toEpochMilliseconds(),
                    painZones = setOf(PainZone.CERVICAL),
                ),
            ),
            savedActivities,
        )
        assertEquals(listOf<CheckinWizardEffect>(CheckinWizardEffect.NavigateBack), effects)
    }

    @Test
    fun duplicateDateOnSaveRetainsFormStateAndAllowsRetryWithNewDate() = runTest(testDispatcher) {
        val existing = DiseaseActivity(
            id = "existing-1",
            entryDate = fixedDate,
            morningStiffnessMinutes = 15,
            globalPainLevel = 2,
            fatigueLevel = 3,
            hasActiveFlare = false,
            createdAt = 1710000000000L,
        )
        val repository = FakeDiseaseActivityRepository(listOf(existing))
        val viewModel = CheckinWizardViewModel(
            repository = repository,
            clock = fixedClock,
            timeZone = TimeZone.UTC,
            idGenerator = { "new-2" },
        )
        val effects = mutableListOf<CheckinWizardEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects.add(it) }
        }

        viewModel.onEvent(CheckinWizardEvent.OnSelectPain(9))
        viewModel.onEvent(CheckinWizardEvent.OnSelectFatigue(8))
        viewModel.onEvent(CheckinWizardEvent.OnSelectStiffness(150))
        viewModel.onEvent(CheckinWizardEvent.OnTogglePainZone(PainZone.HANDS))
        viewModel.onEvent(CheckinWizardEvent.OnProceedToSummary)
        viewModel.onEvent(CheckinWizardEvent.OnSave)
        advanceUntilIdle()

        val errorState = viewModel.uiState.value
        assertEquals(CheckinWizardError.DuplicateDate(fixedDate), errorState.error)
        assertFalse(errorState.isSaving)
        assertEquals(9, errorState.globalPainLevel)
        assertEquals(8, errorState.fatigueLevel)
        assertEquals(150, errorState.morningStiffnessMinutes)
        assertEquals(setOf(PainZone.HANDS), errorState.painZones)
        assertTrue(effects.isEmpty())

        val yesterday = LocalDate(2026, 11, 24)
        viewModel.onEvent(CheckinWizardEvent.OnSelectDate(yesterday))
        assertNull(viewModel.uiState.value.error)

        viewModel.onEvent(CheckinWizardEvent.OnSave)
        advanceUntilIdle()

        assertEquals(2, repository.getAllActivities().first().size)
        assertEquals(yesterday, repository.getActivityById("new-2")?.entryDate)
        assertEquals(listOf<CheckinWizardEffect>(CheckinWizardEffect.NavigateBack), effects)
    }

    @Test
    fun editModeLoadsExistingRecordLandsOnSummaryAndPreservesIdAndCreatedAt() = runTest(testDispatcher) {
        val original = DiseaseActivity(
            id = "edit-1",
            entryDate = LocalDate(2026, 11, 20),
            morningStiffnessMinutes = 45,
            globalPainLevel = 5,
            fatigueLevel = 6,
            hasActiveFlare = false,
            createdAt = 1710000000000L,
            painZones = setOf(PainZone.LUMBAR),
        )
        val repository = FakeDiseaseActivityRepository(listOf(original))
        val viewModel = CheckinWizardViewModel(
            repository = repository,
            clock = fixedClock,
            activityId = original.id,
            timeZone = TimeZone.UTC,
        )
        val effects = mutableListOf<CheckinWizardEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects.add(it) }
        }

        advanceUntilIdle()

        val loadedState = viewModel.uiState.value
        assertTrue(loadedState.isEditMode)
        assertFalse(loadedState.isLoading)
        assertEquals(CheckinWizardStep.SUMMARY, loadedState.currentStep)
        assertEquals(original.entryDate, loadedState.entryDate)
        assertEquals(original.globalPainLevel, loadedState.globalPainLevel)
        assertEquals(original.fatigueLevel, loadedState.fatigueLevel)
        assertEquals(original.morningStiffnessMinutes, loadedState.morningStiffnessMinutes)
        assertEquals(original.hasActiveFlare, loadedState.hasActiveFlare)
        assertEquals(original.painZones, loadedState.painZones)

        viewModel.onEvent(CheckinWizardEvent.OnJumpToStep(CheckinWizardStep.FATIGUE))
        viewModel.onEvent(CheckinWizardEvent.OnSelectFatigue(2))
        assertEquals(CheckinWizardStep.SUMMARY, viewModel.uiState.value.currentStep)

        viewModel.onEvent(CheckinWizardEvent.OnSave)
        advanceUntilIdle()

        val updated = repository.getActivityById(original.id)
        assertEquals(original.copy(fatigueLevel = 2), updated)
        assertEquals(listOf<CheckinWizardEffect>(CheckinWizardEffect.NavigateBack), effects)
    }

    @Test
    fun editModeBackOnSummaryEmitsNavigateBack() = runTest(testDispatcher) {
        val original = DiseaseActivity(
            id = "edit-2",
            entryDate = LocalDate(2026, 11, 20),
            morningStiffnessMinutes = 15,
            globalPainLevel = 2,
            fatigueLevel = 2,
            hasActiveFlare = false,
            createdAt = 1710000000000L,
        )
        val repository = FakeDiseaseActivityRepository(listOf(original))
        val viewModel = CheckinWizardViewModel(
            repository = repository,
            clock = fixedClock,
            activityId = original.id,
            timeZone = TimeZone.UTC,
        )
        val effects = mutableListOf<CheckinWizardEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects.add(it) }
        }

        advanceUntilIdle()
        viewModel.onEvent(CheckinWizardEvent.OnBack)
        advanceUntilIdle()

        assertEquals(listOf<CheckinWizardEffect>(CheckinWizardEffect.NavigateBack), effects)
    }

    @Test
    fun editModeSurfacesLoadFailedWhenRecordDoesNotExist() = runTest(testDispatcher) {
        val repository = FakeDiseaseActivityRepository()
        val viewModel = CheckinWizardViewModel(
            repository = repository,
            clock = fixedClock,
            activityId = "missing-id",
            timeZone = TimeZone.UTC,
        )

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(CheckinWizardError.LoadFailed, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.canSave)
    }

    @Test
    fun unexpectedSaveFailureSurfacesTypedErrorAndOnDismissClearsIt() = runTest(testDispatcher) {
        val repository = FakeDiseaseActivityRepository().apply {
            shouldFailOnInsert = true
            failureException = IllegalStateException("Disk full")
        }
        val viewModel = CheckinWizardViewModel(
            repository = repository,
            clock = fixedClock,
            timeZone = TimeZone.UTC,
        )
        val effects = mutableListOf<CheckinWizardEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects.add(it) }
        }

        viewModel.onEvent(CheckinWizardEvent.OnSelectPain(4))
        viewModel.onEvent(CheckinWizardEvent.OnSelectFatigue(4))
        viewModel.onEvent(CheckinWizardEvent.OnSelectStiffness(0))
        viewModel.onEvent(CheckinWizardEvent.OnProceedToSummary)
        viewModel.onEvent(CheckinWizardEvent.OnSave)
        advanceUntilIdle()

        assertEquals(CheckinWizardError.SaveFailed, viewModel.uiState.value.error)
        assertTrue(effects.isEmpty())

        viewModel.onEvent(CheckinWizardEvent.OnDismissError)
        assertNull(viewModel.uiState.value.error)
    }
}
