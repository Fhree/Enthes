package com.enthes.presentation.dashboard

sealed interface DashboardError {
    data object DeleteFailed : DashboardError
}
