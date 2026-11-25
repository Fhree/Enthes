package com.enthes

import androidx.compose.runtime.Composable
import com.enthes.presentation.navigation.EnthesNavHost
import com.enthes.presentation.theme.EnthesTheme

@Composable
fun App() {
    EnthesTheme {
        EnthesNavHost()
    }
}
