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
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardContent(
        state = uiState,
        onEvent = viewModel::onEvent,
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
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var dialogState by remember { mutableStateOf<DashboardDialogState>(DashboardDialogState.None) }

    if ((state is DashboardUiState.Success) && (state.errorMessage != null)) {
        LaunchedEffect(state.errorMessage) {
            snackbarHostState.showSnackbar(message = state.errorMessage)
            onEvent(DashboardEvent.OnDismissError)
        }
    }

    DashboardDialogs(
        dialogState = dialogState,
        onDismiss = { dialogState = DashboardDialogState.None },
        onEvent = { event ->
            dialogState = DashboardDialogState.None
            onEvent(event)
        },
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Disease Activity") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { dialogState = DashboardDialogState.CreateCheckin },
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
            onEditActivity = { dialogState = DashboardDialogState.EditCheckin(it) },
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
                onConfirmDelete = { onEvent(DashboardEvent.OnDeleteActivity(dialogState.activity.id)) },
            )
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
                text = "Delete Check-in",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete the check-in for $entryDate?",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmDelete) {
                Text(
                    text = "Delete",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
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

@Preview
@Composable
private fun DashboardContentEmptyPreview() {
    MaterialTheme {
        DashboardContent(
            state = DashboardUiState.Empty,
            onEvent = {},
        )
    }
}

@Preview
@Composable
private fun DashboardContentSuccessPreview() {
    MaterialTheme {
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
        )
    }
}
