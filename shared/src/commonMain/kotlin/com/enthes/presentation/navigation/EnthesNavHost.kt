package com.enthes.presentation.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.enthes.presentation.dashboard.DashboardScreen
import enthes.shared.generated.resources.Res
import enthes.shared.generated.resources.action_cancel
import enthes.shared.generated.resources.checkin_dialog_title_edit
import enthes.shared.generated.resources.checkin_dialog_title_new
import org.jetbrains.compose.resources.stringResource

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
            DashboardScreen()
        }
        composable<CheckinWizardRoute> { backStackEntry ->
            val (activityId) = backStackEntry.toRoute<CheckinWizardRoute>()
            CheckinWizardPlaceholder(activityId = activityId) {
                navController.navigateUp()
            }
        }
    }
}

@Composable
private fun CheckinWizardPlaceholder(
    activityId: String?,
    onNavigateBack: () -> Unit,
) {
    val title = if (activityId == null) {
        stringResource(Res.string.checkin_dialog_title_new)
    } else {
        stringResource(Res.string.checkin_dialog_title_edit)
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
            TextButton(onClick = onNavigateBack) {
                Text(text = stringResource(Res.string.action_cancel))
            }
        }
    }
}
