package com.enthes.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
data object DashboardRoute

@Serializable
data class CheckinWizardRoute(
    val activityId: String? = null,
)
