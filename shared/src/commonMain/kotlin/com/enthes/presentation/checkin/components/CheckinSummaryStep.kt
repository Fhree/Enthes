package com.enthes.presentation.checkin.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.enthes.domain.model.PainZone
import com.enthes.presentation.checkin.CheckinWizardStep
import com.enthes.presentation.checkin.CheckinWizardUiState
import com.enthes.presentation.checkin.StiffnessOption
import com.enthes.presentation.checkin.labelRes
import com.enthes.presentation.theme.EnthesTheme
import com.enthes.presentation.theme.evaSeverityColorFor
import enthes.shared.generated.resources.Res
import enthes.shared.generated.resources.action_cancel
import enthes.shared.generated.resources.action_confirm
import enthes.shared.generated.resources.action_no
import enthes.shared.generated.resources.action_save
import enthes.shared.generated.resources.action_update
import enthes.shared.generated.resources.action_yes
import enthes.shared.generated.resources.date_other
import enthes.shared.generated.resources.date_today
import enthes.shared.generated.resources.date_yesterday
import enthes.shared.generated.resources.label_active_flare
import enthes.shared.generated.resources.metric_eva_value
import enthes.shared.generated.resources.metric_fatigue
import enthes.shared.generated.resources.metric_minutes_value
import enthes.shared.generated.resources.metric_pain
import enthes.shared.generated.resources.metric_stiffness
import enthes.shared.generated.resources.summary_pain_zones_none
import enthes.shared.generated.resources.summary_tap_to_edit
import enthes.shared.generated.resources.wizard_date_section
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

@Composable
fun CheckinSummaryStep(
    state: CheckinWizardUiState,
    onSelectDate: (LocalDate) -> Unit,
    onEditStep: (CheckinWizardStep) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        CheckinDatePickerDialog(
            initialDate = state.entryDate,
            onDismiss = { showDatePicker = false },
            onConfirmDate = { date ->
                showDatePicker = false
                onSelectDate(date)
            },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            QuickDateSelector(
                today = state.today,
                selectedDate = state.entryDate,
                onSelectDate = onSelectDate,
                onOpenDatePicker = { showDatePicker = true },
            )
            SummaryFieldsSection(
                state = state,
                onEditStep = onEditStep,
            )
        }

        SummarySaveButton(
            isEditMode = state.isEditMode,
            isSaving = state.isSaving,
            enabled = state.canSave,
            onClick = onSave,
        )
    }
}

@Composable
private fun SummaryFieldsSection(
    state: CheckinWizardUiState,
    onEditStep: (CheckinWizardStep) -> Unit,
) {
    SummaryFieldCard(
        label = stringResource(Res.string.metric_pain),
        value = state.globalPainLevel?.let {
            stringResource(Res.string.metric_eva_value, it)
        } ?: "-",
        evaBadgeLevel = state.globalPainLevel,
        onClick = { onEditStep(CheckinWizardStep.PAIN) },
    )

    SummaryFieldCard(
        label = stringResource(Res.string.metric_fatigue),
        value = state.fatigueLevel?.let {
            stringResource(Res.string.metric_eva_value, it)
        } ?: "-",
        evaBadgeLevel = state.fatigueLevel,
        onClick = { onEditStep(CheckinWizardStep.FATIGUE) },
    )

    SummaryFieldCard(
        label = stringResource(Res.string.metric_stiffness),
        value = formatStiffnessValue(state.morningStiffnessMinutes),
        onClick = { onEditStep(CheckinWizardStep.STIFFNESS) },
    )

    SummaryFieldCard(
        label = stringResource(Res.string.label_active_flare),
        value = buildFlareAndZonesSummary(
            hasActiveFlare = state.hasActiveFlare,
            painZones = state.painZones,
        ),
        onClick = { onEditStep(CheckinWizardStep.FLARE_AND_ZONES) },
    )
}

@Composable
private fun SummarySaveButton(
    isEditMode: Boolean,
    isSaving: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.5.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Text(
                text = stringResource(
                    if (isEditMode) Res.string.action_update else Res.string.action_save,
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun QuickDateSelector(
    today: LocalDate,
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    onOpenDatePicker: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val yesterday = remember(today) { today.minus(1, DateTimeUnit.DAY) }
    val isToday = selectedDate == today
    val isYesterday = selectedDate == yesterday
    val isCustomDate = !isToday && !isYesterday

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.wizard_date_section),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = selectedDate.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DateOptionChip(
                label = stringResource(Res.string.date_today),
                isSelected = isToday,
                onClick = { onSelectDate(today) },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
            )
            DateOptionChip(
                label = stringResource(Res.string.date_yesterday),
                isSelected = isYesterday,
                onClick = { onSelectDate(yesterday) },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
            )
            DateOptionChip(
                label = stringResource(Res.string.date_other),
                isSelected = isCustomDate,
                onClick = onOpenDatePicker,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
            )
        }
    }
}

@Composable
private fun DateOptionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        contentColor = contentColor,
        border = if (isSelected) {
            BorderStroke(width = 2.dp, color = contentColor)
        } else {
            BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SummaryFieldCard(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    evaBadgeLevel: Int? = null,
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 68.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (evaBadgeLevel != null) {
                val evaColor = evaSeverityColorFor(evaBadgeLevel)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = evaColor.container,
                    contentColor = evaColor.onContainer,
                ) {
                    Text(
                        text = evaBadgeLevel.toString(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            } else {
                Text(
                    text = stringResource(Res.string.summary_tap_to_edit),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckinDatePickerDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirmDate: (LocalDate) -> Unit,
) {
    val initialMillis = remember(initialDate) {
        initialDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
    }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis
                    if (selectedMillis != null) {
                        val selectedDate = Instant.fromEpochMilliseconds(selectedMillis)
                            .toLocalDateTime(TimeZone.UTC)
                            .date
                        onConfirmDate(selectedDate)
                    } else {
                        onDismiss()
                    }
                },
            ) {
                Text(stringResource(Res.string.action_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.action_cancel))
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun formatStiffnessValue(minutes: Int?): String {
    if (minutes == null) return "-"
    val matchingOption = StiffnessOption.entries.firstOrNull { it.minutes == minutes }
    return if (matchingOption != null) {
        stringResource(matchingOption.labelRes())
    } else {
        stringResource(Res.string.metric_minutes_value, minutes)
    }
}

@Composable
private fun buildFlareAndZonesSummary(
    hasActiveFlare: Boolean,
    painZones: Set<PainZone>,
): String {
    val flareText = stringResource(
        if (hasActiveFlare) Res.string.action_yes else Res.string.action_no,
    )
    if (painZones.isEmpty()) {
        return "$flareText • ${stringResource(Res.string.summary_pain_zones_none)}"
    }
    val zoneLabels = painZones
        .sortedBy { it.ordinal }
        .map { stringResource(it.labelRes()) }
        .joinToString(separator = ", ")
    return "$flareText • $zoneLabels"
}

@Preview
@Composable
private fun CheckinSummaryStepPreview() {
    EnthesTheme {
        CheckinSummaryStep(
            state = CheckinWizardUiState(
                today = LocalDate(2026, 11, 25),
                currentStep = CheckinWizardStep.SUMMARY,
                globalPainLevel = 6,
                fatigueLevel = 4,
                morningStiffnessMinutes = 45,
                hasActiveFlare = true,
                painZones = setOf(PainZone.LUMBAR, PainZone.KNEES),
            ),
            onSelectDate = {},
            onEditStep = {},
            onSave = {},
        )
    }
}
