package com.enthes.presentation.checkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enthes.domain.error.DuplicateEntryDateException
import com.enthes.domain.model.DiseaseActivity
import com.enthes.domain.model.PainZone
import com.enthes.domain.repository.DiseaseActivityRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CheckinWizardViewModel(
    private val repository: DiseaseActivityRepository,
    private val clock: Clock,
    activityId: String? = null,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    private val idGenerator: () -> String = ::generateActivityId,
) : ViewModel() {

    private var existingActivity: DiseaseActivity? = null

    private val _uiState = MutableStateFlow(
        CheckinWizardUiState(
            today = clock.todayIn(timeZone),
            currentStep = if (activityId != null) {
                CheckinWizardStep.SUMMARY
            } else {
                CheckinWizardStep.PAIN
            },
            isEditMode = activityId != null,
            isLoading = activityId != null,
        ),
    )
    val uiState: StateFlow<CheckinWizardUiState> = _uiState.asStateFlow()

    private val _effects = Channel<CheckinWizardEffect>(Channel.BUFFERED)
    val effects: Flow<CheckinWizardEffect> = _effects.receiveAsFlow()

    init {
        if (activityId != null) {
            loadExistingActivity(activityId)
        }
    }

    fun onEvent(event: CheckinWizardEvent) {
        when (event) {
            is CheckinWizardEvent.OnSelectPain -> selectPain(event.level)
            is CheckinWizardEvent.OnSelectFatigue -> selectFatigue(event.level)
            is CheckinWizardEvent.OnSelectStiffness -> selectStiffness(event.minutes)
            is CheckinWizardEvent.OnSetActiveFlare -> {
                _uiState.update { it.copy(hasActiveFlare = event.hasActiveFlare) }
            }
            is CheckinWizardEvent.OnTogglePainZone -> togglePainZone(event.zone)
            CheckinWizardEvent.OnProceedToSummary -> {
                _uiState.update {
                    it.copy(
                        currentStep = CheckinWizardStep.SUMMARY,
                        returnToSummaryOnSelect = false,
                    )
                }
            }
            is CheckinWizardEvent.OnJumpToStep -> {
                _uiState.update {
                    it.copy(
                        currentStep = event.step,
                        returnToSummaryOnSelect = true,
                    )
                }
            }
            is CheckinWizardEvent.OnSelectDate -> {
                _uiState.update { it.copy(entryDate = event.date, error = null) }
            }
            CheckinWizardEvent.OnBack -> navigateBack()
            CheckinWizardEvent.OnSave -> saveCheckin()
            CheckinWizardEvent.OnDismissError -> {
                _uiState.update { it.copy(error = null) }
            }
        }
    }

    private fun loadExistingActivity(id: String) {
        viewModelScope.launch {
            val loaded = repository.getActivityById(id)
            if (loaded == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = CheckinWizardError.LoadFailed,
                    )
                }
            } else {
                existingActivity = loaded
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentStep = CheckinWizardStep.SUMMARY,
                        entryDate = loaded.entryDate,
                        globalPainLevel = loaded.globalPainLevel,
                        fatigueLevel = loaded.fatigueLevel,
                        morningStiffnessMinutes = loaded.morningStiffnessMinutes,
                        hasActiveFlare = loaded.hasActiveFlare,
                        painZones = loaded.painZones,
                    )
                }
            }
        }
    }

    private fun selectPain(level: Int) {
        val validLevel = level.coerceIn(0..10)
        _uiState.update { state ->
            val nextStep = if (state.returnToSummaryOnSelect) {
                CheckinWizardStep.SUMMARY
            } else {
                CheckinWizardStep.FATIGUE
            }
            state.copy(
                globalPainLevel = validLevel,
                currentStep = nextStep,
                returnToSummaryOnSelect = false,
            )
        }
    }

    private fun selectFatigue(level: Int) {
        val validLevel = level.coerceIn(0..10)
        _uiState.update { state ->
            val nextStep = if (state.returnToSummaryOnSelect) {
                CheckinWizardStep.SUMMARY
            } else {
                CheckinWizardStep.STIFFNESS
            }
            state.copy(
                fatigueLevel = validLevel,
                currentStep = nextStep,
                returnToSummaryOnSelect = false,
            )
        }
    }

    private fun selectStiffness(minutes: Int) {
        val validMinutes = minutes.coerceAtLeast(0)
        _uiState.update { state ->
            val nextStep = if (state.returnToSummaryOnSelect) {
                CheckinWizardStep.SUMMARY
            } else {
                CheckinWizardStep.FLARE_AND_ZONES
            }
            state.copy(
                morningStiffnessMinutes = validMinutes,
                currentStep = nextStep,
                returnToSummaryOnSelect = false,
            )
        }
    }

    private fun togglePainZone(zone: PainZone) {
        _uiState.update { state ->
            val updatedZones = if (zone in state.painZones) {
                state.painZones - zone
            } else {
                state.painZones + zone
            }
            state.copy(painZones = updatedZones)
        }
    }

    private fun navigateBack() {
        val state = _uiState.value
        if (state.returnToSummaryOnSelect) {
            _uiState.update {
                it.copy(
                    currentStep = CheckinWizardStep.SUMMARY,
                    returnToSummaryOnSelect = false,
                )
            }
            return
        }

        val previousStep = when (state.currentStep) {
            CheckinWizardStep.PAIN -> null
            CheckinWizardStep.FATIGUE -> CheckinWizardStep.PAIN
            CheckinWizardStep.STIFFNESS -> CheckinWizardStep.FATIGUE
            CheckinWizardStep.FLARE_AND_ZONES -> CheckinWizardStep.STIFFNESS
            CheckinWizardStep.SUMMARY -> if (state.isEditMode) null else CheckinWizardStep.FLARE_AND_ZONES
        }

        if (previousStep == null) {
            _effects.trySend(CheckinWizardEffect.NavigateBack)
        } else {
            _uiState.update { it.copy(currentStep = previousStep) }
        }
    }

    private fun saveCheckin() {
        val state = _uiState.value
        val activity = buildActivityToPersist(state) ?: return
        if (state.isSaving) return

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            val result = if (state.isEditMode) {
                repository.updateActivity(activity)
            } else {
                repository.insertActivity(activity)
            }

            result.onSuccess {
                _uiState.update { it.copy(isSaving = false) }
                _effects.send(CheckinWizardEffect.NavigateBack)
            }.onFailure { failure ->
                val fallback = if (state.isEditMode) {
                    CheckinWizardError.UpdateFailed
                } else {
                    CheckinWizardError.SaveFailed
                }
                val mappedError = when (failure) {
                    is DuplicateEntryDateException -> CheckinWizardError.DuplicateDate(failure.entryDate)
                    else -> fallback
                }
                _uiState.update { it.copy(isSaving = false, error = mappedError) }
            }
        }
    }

    private fun buildActivityToPersist(state: CheckinWizardUiState): DiseaseActivity? {
        val pain = state.globalPainLevel
        val fatigue = state.fatigueLevel
        val stiffness = state.morningStiffnessMinutes
        val existing = existingActivity

        return if (pain != null && fatigue != null && stiffness != null) {
            DiseaseActivity(
                id = existing?.id ?: idGenerator(),
                entryDate = state.entryDate,
                morningStiffnessMinutes = stiffness,
                globalPainLevel = pain,
                fatigueLevel = fatigue,
                hasActiveFlare = state.hasActiveFlare,
                createdAt = existing?.createdAt ?: clock.now().toEpochMilliseconds(),
                painZones = state.painZones,
            )
        } else {
            null
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
private fun generateActivityId(): String = Uuid.random().toString()
