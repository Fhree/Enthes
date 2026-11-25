package com.enthes.presentation.checkin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enthes.presentation.checkin.components.CheckinSummaryStep
import com.enthes.presentation.checkin.components.EvaThermometerSelector
import com.enthes.presentation.checkin.components.FlareAndZonesStep
import com.enthes.presentation.checkin.components.StiffnessRangeSelector
import enthes.shared.generated.resources.Res
import enthes.shared.generated.resources.cd_navigate_back
import enthes.shared.generated.resources.error_duplicate_date
import enthes.shared.generated.resources.error_load_failed
import enthes.shared.generated.resources.error_save_failed
import enthes.shared.generated.resources.error_update_failed
import enthes.shared.generated.resources.eva_fatigue_anchor_high
import enthes.shared.generated.resources.eva_fatigue_anchor_low
import enthes.shared.generated.resources.eva_fatigue_anchor_mid
import enthes.shared.generated.resources.eva_pain_anchor_high
import enthes.shared.generated.resources.eva_pain_anchor_low
import enthes.shared.generated.resources.eva_pain_anchor_mid
import enthes.shared.generated.resources.ic_arrow_back
import enthes.shared.generated.resources.wizard_step_fatigue_title
import enthes.shared.generated.resources.wizard_step_flare_zones_title
import enthes.shared.generated.resources.wizard_step_indicator
import enthes.shared.generated.resources.wizard_step_pain_title
import enthes.shared.generated.resources.wizard_step_stiffness_title
import enthes.shared.generated.resources.wizard_step_summary_title_edit
import enthes.shared.generated.resources.wizard_step_summary_title_new
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private const val TOTAL_WIZARD_STEPS = 5

@Composable
fun CheckinWizardScreen(
    activityId: String?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CheckinWizardViewModel = koinViewModel(
        key = activityId ?: "new-checkin",
    ) { parametersOf(activityId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                CheckinWizardEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    CheckinWizardContent(
        state = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

@Composable
fun CheckinWizardContent(
    state: CheckinWizardUiState,
    onEvent: (CheckinWizardEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    if (state.error != null) {
        val errorMessage = state.error.toDisplayMessage()
        LaunchedEffect(state.error) {
            snackbarHostState.showSnackbar(message = errorMessage)
            onEvent(CheckinWizardEvent.OnDismissError)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CheckinWizardTopBar(
                state = state,
                onBackClick = { onEvent(CheckinWizardEvent.OnBack) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            CheckinWizardStepBody(
                state = state,
                onEvent = onEvent,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
        }
    }
}

@Composable
private fun CheckinWizardTopBar(
    state: CheckinWizardUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        TopAppBar(
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_arrow_back),
                        contentDescription = stringResource(Res.string.cd_navigate_back),
                    )
                }
            },
            title = {
                Column {
                    Text(
                        text = state.stepTitle(),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    if (!state.isEditMode) {
                        Text(
                            text = stringResource(
                                Res.string.wizard_step_indicator,
                                state.currentStep.stepNumber,
                                TOTAL_WIZARD_STEPS,
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            ),
        )
        if (!state.isEditMode) {
            LinearProgressIndicator(
                progress = { state.currentStep.stepNumber.toFloat() / TOTAL_WIZARD_STEPS },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CheckinWizardStepBody(
    state: CheckinWizardUiState,
    onEvent: (CheckinWizardEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state.currentStep) {
        CheckinWizardStep.PAIN -> {
            EvaThermometerSelector(
                lowAnchorLabel = stringResource(Res.string.eva_pain_anchor_low),
                midAnchorLabel = stringResource(Res.string.eva_pain_anchor_mid),
                highAnchorLabel = stringResource(Res.string.eva_pain_anchor_high),
                selectedLevel = state.globalPainLevel,
                onSelectLevel = { onEvent(CheckinWizardEvent.OnSelectPain(it)) },
                modifier = modifier,
            )
        }

        CheckinWizardStep.FATIGUE -> {
            EvaThermometerSelector(
                lowAnchorLabel = stringResource(Res.string.eva_fatigue_anchor_low),
                midAnchorLabel = stringResource(Res.string.eva_fatigue_anchor_mid),
                highAnchorLabel = stringResource(Res.string.eva_fatigue_anchor_high),
                selectedLevel = state.fatigueLevel,
                onSelectLevel = { onEvent(CheckinWizardEvent.OnSelectFatigue(it)) },
                modifier = modifier,
            )
        }

        CheckinWizardStep.STIFFNESS -> {
            StiffnessRangeSelector(
                selectedMinutes = state.morningStiffnessMinutes,
                onSelectMinutes = { onEvent(CheckinWizardEvent.OnSelectStiffness(it)) },
                modifier = modifier,
            )
        }

        CheckinWizardStep.FLARE_AND_ZONES -> {
            FlareAndZonesStep(
                hasActiveFlare = state.hasActiveFlare,
                selectedZones = state.painZones,
                onSetActiveFlare = { onEvent(CheckinWizardEvent.OnSetActiveFlare(it)) },
                onTogglePainZone = { onEvent(CheckinWizardEvent.OnTogglePainZone(it)) },
                onProceedToSummary = { onEvent(CheckinWizardEvent.OnProceedToSummary) },
                modifier = modifier,
            )
        }

        CheckinWizardStep.SUMMARY -> {
            CheckinSummaryStep(
                state = state,
                onSelectDate = { onEvent(CheckinWizardEvent.OnSelectDate(it)) },
                onEditStep = { onEvent(CheckinWizardEvent.OnJumpToStep(it)) },
                onSave = { onEvent(CheckinWizardEvent.OnSave) },
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun CheckinWizardUiState.stepTitle(): String = when (currentStep) {
    CheckinWizardStep.PAIN -> stringResource(Res.string.wizard_step_pain_title)
    CheckinWizardStep.FATIGUE -> stringResource(Res.string.wizard_step_fatigue_title)
    CheckinWizardStep.STIFFNESS -> stringResource(Res.string.wizard_step_stiffness_title)
    CheckinWizardStep.FLARE_AND_ZONES -> stringResource(Res.string.wizard_step_flare_zones_title)
    CheckinWizardStep.SUMMARY -> stringResource(
        if (isEditMode) {
            Res.string.wizard_step_summary_title_edit
        } else {
            Res.string.wizard_step_summary_title_new
        },
    )
}

@Composable
private fun CheckinWizardError.toDisplayMessage(): String = when (this) {
    is CheckinWizardError.DuplicateDate -> stringResource(Res.string.error_duplicate_date, date.toString())
    CheckinWizardError.SaveFailed -> stringResource(Res.string.error_save_failed)
    CheckinWizardError.UpdateFailed -> stringResource(Res.string.error_update_failed)
    CheckinWizardError.LoadFailed -> stringResource(Res.string.error_load_failed)
}
