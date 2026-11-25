package com.enthes

import androidx.compose.runtime.Composable
import com.enthes.presentation.dashboard.DashboardScreen
import com.enthes.presentation.theme.EnthesTheme

@Composable
fun App() {
    EnthesTheme {
        DashboardScreen()
    }
}
