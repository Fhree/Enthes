package com.enthes.di

import com.enthes.presentation.dashboard.DashboardViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::DashboardViewModel)
}
