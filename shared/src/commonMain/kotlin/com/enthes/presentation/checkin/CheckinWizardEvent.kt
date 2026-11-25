package com.enthes.presentation.checkin

import com.enthes.domain.model.PainZone
import kotlinx.datetime.LocalDate

sealed interface CheckinWizardEvent {
    data class OnSelectPain(val level: Int) : CheckinWizardEvent
    data class OnSelectFatigue(val level: Int) : CheckinWizardEvent
    data class OnSelectStiffness(val minutes: Int) : CheckinWizardEvent
    data class OnSetActiveFlare(val hasActiveFlare: Boolean) : CheckinWizardEvent
    data class OnTogglePainZone(val zone: PainZone) : CheckinWizardEvent
    data object OnProceedToSummary : CheckinWizardEvent
    data class OnJumpToStep(val step: CheckinWizardStep) : CheckinWizardEvent
    data class OnSelectDate(val date: LocalDate) : CheckinWizardEvent
    data object OnBack : CheckinWizardEvent
    data object OnSave : CheckinWizardEvent
    data object OnDismissError : CheckinWizardEvent
}
