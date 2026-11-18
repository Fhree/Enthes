package com.enthes.presentation.dashboard

import com.enthes.domain.model.DiseaseActivity

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data object Empty : DashboardUiState
    data class Success(
        val activities: List<DiseaseActivity>,
        val errorMessage: String? = null,
    ) : DashboardUiState
}
