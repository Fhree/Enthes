package com.enthes.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.enthes.presentation.checkin.CheckinWizardScreen
import com.enthes.presentation.dashboard.DashboardScreen

@Composable
fun EnthesNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = DashboardRoute,
        modifier = modifier,
    ) {
        composable<DashboardRoute> {
            DashboardScreen(
                onAddCheckin = { navController.navigate(CheckinWizardRoute()) },
                onEditActivity = { activityId ->
                    navController.navigate(CheckinWizardRoute(activityId = activityId))
                },
            )
        }
        composable<CheckinWizardRoute> { backStackEntry ->
            val (activityId) = backStackEntry.toRoute<CheckinWizardRoute>()
            CheckinWizardScreen(
                activityId = activityId,
                onNavigateBack = { navController.navigateUp() },
            )
        }
    }
}
