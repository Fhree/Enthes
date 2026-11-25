package com.enthes.presentation.checkin

import com.enthes.domain.model.PainZone
import kotlinx.datetime.LocalDate

enum class CheckinWizardStep(val stepNumber: Int) {
    PAIN(1),
    FATIGUE(2),
    STIFFNESS(3),
    FLARE_AND_ZONES(4),
    SUMMARY(5),
}

enum class StiffnessOption(
    val minutes: Int,
    val severityLevel: Int,
) {
    NONE(minutes = 0, severityLevel = 0),
    UNDER_30_MIN(minutes = 15, severityLevel = 2),
    THIRTY_TO_SIXTY_MIN(minutes = 45, severityLevel = 5),
    ONE_TO_TWO_HOURS(minutes = 90, severityLevel = 8),
    OVER_TWO_HOURS(minutes = 150, severityLevel = 10),
}

sealed interface CheckinWizardError {
    data class DuplicateDate(val date: LocalDate) : CheckinWizardError
    data object SaveFailed : CheckinWizardError
    data object UpdateFailed : CheckinWizardError
    data object LoadFailed : CheckinWizardError
}

data class CheckinWizardUiState(
    val today: LocalDate,
    val currentStep: CheckinWizardStep = CheckinWizardStep.PAIN,
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val returnToSummaryOnSelect: Boolean = false,
    val entryDate: LocalDate = today,
    val globalPainLevel: Int? = null,
    val fatigueLevel: Int? = null,
    val morningStiffnessMinutes: Int? = null,
    val hasActiveFlare: Boolean = false,
    val painZones: Set<PainZone> = emptySet(),
    val error: CheckinWizardError? = null,
) {
    val canSave: Boolean
        get() = !isLoading &&
            !isSaving &&
            globalPainLevel != null &&
            fatigueLevel != null &&
            morningStiffnessMinutes != null
}
