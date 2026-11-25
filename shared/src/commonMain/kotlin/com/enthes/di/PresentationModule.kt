package com.enthes.di

import com.enthes.presentation.checkin.CheckinWizardViewModel
import com.enthes.presentation.dashboard.DashboardViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::DashboardViewModel)
    viewModel { params ->
        CheckinWizardViewModel(
            repository = get(),
            clock = get(),
            activityId = params.getOrNull(),
        )
    }
}
