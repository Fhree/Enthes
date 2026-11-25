package com.enthes.presentation.dashboard

import kotlinx.datetime.LocalDate

sealed interface DashboardError {
    data class DuplicateDate(val date: LocalDate) : DashboardError
    data object SaveFailed : DashboardError
    data object UpdateFailed : DashboardError
    data object DeleteFailed : DashboardError
}
