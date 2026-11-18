package com.enthes.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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

@Composable
fun DashboardContent(
    state: DashboardUiState,
    onEvent: (DashboardEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showCheckinDialog by remember { mutableStateOf(value = false) }

    if ((state is DashboardUiState.Success) && (state.errorMessage != null)) {
        LaunchedEffect(state.errorMessage) {
            snackbarHostState.showSnackbar(message = state.errorMessage)
            onEvent(DashboardEvent.OnDismissError)
        }
    }

    if (showCheckinDialog) {
        QuickCheckinDialog(
            onDismissRequest = { showCheckinDialog = false },
            onSaveCheckin = { activity ->
                showCheckinDialog = false
                onEvent(DashboardEvent.OnSaveCheckin(activity))
            },
        )
    }

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
                onClick = { showCheckinDialog = true },
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        when (state) {
            is DashboardUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is DashboardUiState.Empty -> {
                EmptyDashboardPlaceholder(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                )
            }

            is DashboardUiState.Success -> {
                if (state.activities.isEmpty()) {
                    EmptyDashboardPlaceholder(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            items = state.activities,
                            key = { (id) -> id },
                        ) { activity ->
                            ActivityItemCard(activity = activity)
                        }
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
                        entryDate = "2026-11-18",
                        morningStiffnessMinutes = 45,
                        globalPainLevel = 6,
                        fatigueLevel = 7,
                        hasActiveFlare = true,
                        createdAt = 1710000000000L,
                    ),
                    DiseaseActivity(
                        id = "2",
                        entryDate = "2026-11-17",
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
