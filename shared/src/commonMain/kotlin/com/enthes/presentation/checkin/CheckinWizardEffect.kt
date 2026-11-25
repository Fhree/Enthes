package com.enthes.presentation.checkin

sealed interface CheckinWizardEffect {
    data object NavigateBack : CheckinWizardEffect
}
