package com.enthes.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enthes.domain.model.DiseaseActivity
import com.enthes.presentation.dashboard.components.ActivityItemCard
import com.enthes.presentation.dashboard.components.EmptyDashboardPlaceholder
import com.enthes.presentation.dashboard.components.QuickCheckinDialog
import com.enthes.presentation.theme.EnthesTheme
import enthes.shared.generated.resources.Res
import enthes.shared.generated.resources.action_cancel
import enthes.shared.generated.resources.action_delete
import enthes.shared.generated.resources.dashboard_title
import enthes.shared.generated.resources.delete_dialog_message
import enthes.shared.generated.resources.delete_dialog_title
import enthes.shared.generated.resources.error_delete_failed
import enthes.shared.generated.resources.error_duplicate_date
import enthes.shared.generated.resources.error_save_failed
import enthes.shared.generated.resources.error_update_failed
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardScreen(
    onAddCheckin: () -> Unit,
    onEditActivity: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardContent(
        state = uiState,
        onEvent = viewModel::onEvent,
        onAddCheckin = onAddCheckin,
        onEditActivity = onEditActivity,
        modifier = modifier,
    )
}

private sealed interface DashboardDialogState {
    data object None : DashboardDialogState
    data object CreateCheckin : DashboardDialogState
    data class EditCheckin(val activity: DiseaseActivity) : DashboardDialogState
    data class ConfirmDelete(val activity: DiseaseActivity) : DashboardDialogState
}

@Composable
fun DashboardContent(
    state: DashboardUiState,
    onEvent: (DashboardEvent) -> Unit,
    onAddCheckin: () -> Unit,
    onEditActivity: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var dialogState by remember { mutableStateOf<DashboardDialogState>(DashboardDialogState.None) }

    if ((state is DashboardUiState.Success) && (state.error != null)) {
        val errorMessage = state.error.toDisplayMessage()
        LaunchedEffect(state.error) {
            snackbarHostState.showSnackbar(message = errorMessage)
            onEvent(DashboardEvent.OnDismissError)
        }
    }

    DashboardDialogs(
        dialogState = dialogState,
        onDismiss = { dialogState = DashboardDialogState.None },
    ) { event ->
        dialogState = DashboardDialogState.None
        onEvent(event)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.dashboard_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCheckin,
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        DashboardBody(
            state = state,
            onEditActivity = { onEditActivity(it.id) },
            onDeleteActivity = { dialogState = DashboardDialogState.ConfirmDelete(it) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        )
    }
}

@Composable
private fun DashboardDialogs(
    dialogState: DashboardDialogState,
    onDismiss: () -> Unit,
    onEvent: (DashboardEvent) -> Unit,
) {
    when (dialogState) {
        DashboardDialogState.None -> Unit
        DashboardDialogState.CreateCheckin -> {
            QuickCheckinDialog(
                onDismissRequest = onDismiss,
                onSaveCheckin = { activity -> onEvent(DashboardEvent.OnSaveCheckin(activity)) },
            )
        }

        is DashboardDialogState.EditCheckin -> {
            QuickCheckinDialog(
                initialActivity = dialogState.activity,
                onDismissRequest = onDismiss,
                onSaveCheckin = { activity -> onEvent(DashboardEvent.OnUpdateCheckin(activity)) },
            )
        }

        is DashboardDialogState.ConfirmDelete -> {
            DeleteActivityConfirmationDialog(
                entryDate = dialogState.activity.entryDate,
                onDismissRequest = onDismiss,
            ) {
                onEvent(DashboardEvent.OnDeleteActivity(dialogState.activity.id))
            }
        }
    }
}

@Composable
private fun DeleteActivityConfirmationDialog(
    entryDate: LocalDate,
    onDismissRequest: () -> Unit,
    onConfirmDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(Res.string.delete_dialog_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Text(
                text = stringResource(Res.string.delete_dialog_message, entryDate.toString()),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmDelete) {
                Text(
                    text = stringResource(Res.string.action_delete),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.action_cancel))
            }
        },
    )
}

@Composable
private fun DashboardBody(
    state: DashboardUiState,
    onEditActivity: (DiseaseActivity) -> Unit,
    onDeleteActivity: (DiseaseActivity) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is DashboardUiState.Loading -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        is DashboardUiState.Empty -> {
            EmptyDashboardPlaceholder(modifier = modifier)
        }

        is DashboardUiState.Success -> {
            if (state.activities.isEmpty()) {
                EmptyDashboardPlaceholder(modifier = modifier)
            } else {
                LazyColumn(
                    modifier = modifier,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        items = state.activities,
                        key = { (id) -> id },
                    ) { activity ->
                        ActivityItemCard(
                            activity = activity,
                            onEditClick = { onEditActivity(activity) },
                            onDeleteClick = { onDeleteActivity(activity) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardError.toDisplayMessage(): String = when (this) {
    is DashboardError.DuplicateDate -> stringResource(Res.string.error_duplicate_date, date.toString())
    DashboardError.SaveFailed -> stringResource(Res.string.error_save_failed)
    DashboardError.UpdateFailed -> stringResource(Res.string.error_update_failed)
    DashboardError.DeleteFailed -> stringResource(Res.string.error_delete_failed)
}

@Preview
@Composable
private fun DashboardContentEmptyPreview() {
    EnthesTheme {
        DashboardContent(
            state = DashboardUiState.Empty,
            onEvent = {},
            onAddCheckin = {},
            onEditActivity = {},
        )
    }
}

@Preview
@Composable
private fun DashboardContentSuccessPreview() {
    EnthesTheme {
        DashboardContent(
            state = DashboardUiState.Success(
                activities = listOf(
                    DiseaseActivity(
                        id = "1",
                        entryDate = LocalDate(2026, 11, 18),
                        morningStiffnessMinutes = 45,
                        globalPainLevel = 6,
                        fatigueLevel = 7,
                        hasActiveFlare = true,
                        createdAt = 1710000000000L,
                    ),
                    DiseaseActivity(
                        id = "2",
                        entryDate = LocalDate(2026, 11, 17),
                        morningStiffnessMinutes = 15,
                        globalPainLevel = 2,
                        fatigueLevel = 3,
                        hasActiveFlare = false,
                        createdAt = 1709913600000L,
                    ),
                ),
            ),
            onEvent = {},
            onAddCheckin = {},
            onEditActivity = {},
        )
    }
}
