package com.enthes.presentation.dashboard

sealed interface DashboardEvent {
    data class OnDeleteActivity(val id: String) : DashboardEvent
    data object OnDismissError : DashboardEvent
}
