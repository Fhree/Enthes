package com.enthes.presentation.dashboard

import com.enthes.domain.model.DiseaseActivity

sealed interface DashboardEvent {
    data class OnSaveCheckin(val activity: DiseaseActivity) : DashboardEvent
    data class OnDeleteActivity(val id: String) : DashboardEvent
    data object OnDismissError : DashboardEvent
}
