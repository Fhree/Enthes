package com.enthes.presentation.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.enthes.domain.model.DiseaseActivity
import enthes.shared.generated.resources.Res
import enthes.shared.generated.resources.action_cancel
import enthes.shared.generated.resources.action_save
import enthes.shared.generated.resources.action_update
import enthes.shared.generated.resources.checkin_date_error
import enthes.shared.generated.resources.checkin_date_label
import enthes.shared.generated.resources.checkin_dialog_title_edit
import enthes.shared.generated.resources.checkin_dialog_title_new
import enthes.shared.generated.resources.checkin_fatigue_level
import enthes.shared.generated.resources.checkin_pain_level
import enthes.shared.generated.resources.checkin_slider_value
import enthes.shared.generated.resources.checkin_stiffness_label
import enthes.shared.generated.resources.label_active_flare
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val MIN_LEVEL = 0
private const val MAX_LEVEL = 10
private const val DEFAULT_LEVEL = 3f
private const val DEFAULT_STIFFNESS_MINUTES = "15"

@Composable
fun QuickCheckinDialog(
    onDismissRequest: () -> Unit,
    onSaveCheckin: (DiseaseActivity) -> Unit,
    modifier: Modifier = Modifier,
    initialActivity: DiseaseActivity? = null,
) {
    val isEditMode = initialActivity != null
    val todayDate = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val formState = remember(initialActivity) {
        QuickCheckinFormState(initialActivity = initialActivity, fallbackDate = todayDate)
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = {
            Text(
                text = stringResource(
                    if (isEditMode) {
                        Res.string.checkin_dialog_title_edit
                    } else {
                        Res.string.checkin_dialog_title_new
                    },
                ),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = { QuickCheckinForm(formState = formState) },
        confirmButton = {
            TextButton(
                onClick = { formState.toDiseaseActivity()?.let(onSaveCheckin) },
                enabled = !formState.isDateError,
            ) {
                Text(
                    stringResource(
                        if (isEditMode) Res.string.action_update else Res.string.action_save,
                    ),
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

private class QuickCheckinFormState(
    private val initialActivity: DiseaseActivity?,
    private val fallbackDate: LocalDate,
) {
    var entryDateText by mutableStateOf((initialActivity?.entryDate ?: fallbackDate).toString())
    var painLevel by mutableFloatStateOf(initialActivity?.globalPainLevel?.toFloat() ?: DEFAULT_LEVEL)
    var fatigueLevel by mutableFloatStateOf(initialActivity?.fatigueLevel?.toFloat() ?: DEFAULT_LEVEL)
    var stiffnessMinutes by mutableStateOf(
        initialActivity?.morningStiffnessMinutes?.toString() ?: DEFAULT_STIFFNESS_MINUTES,
    )
    var hasActiveFlare by mutableStateOf(value = initialActivity?.hasActiveFlare ?: false)

    val parsedEntryDate: LocalDate?
        get() = if (entryDateText.isBlank()) {
            initialActivity?.entryDate ?: fallbackDate
        } else {
            runCatching { LocalDate.parse(entryDateText.trim()) }.getOrNull()
        }

    val isDateError: Boolean
        get() = parsedEntryDate == null

    fun updateStiffnessMinutes(input: String) {
        if (input.all { it.isDigit() }) {
            stiffnessMinutes = input
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun toDiseaseActivity(): DiseaseActivity? = parsedEntryDate?.let { validDate ->
        DiseaseActivity(
            id = initialActivity?.id ?: Uuid.random().toString(),
            entryDate = validDate,
            morningStiffnessMinutes = stiffnessMinutes.toIntOrNull() ?: 0,
            globalPainLevel = painLevel.roundToInt().coerceIn(MIN_LEVEL, MAX_LEVEL),
            fatigueLevel = fatigueLevel.roundToInt().coerceIn(MIN_LEVEL, MAX_LEVEL),
            hasActiveFlare = hasActiveFlare,
            createdAt = initialActivity?.createdAt ?: Clock.System.now().toEpochMilliseconds(),
        )
    }
}

@Composable
private fun QuickCheckinForm(
    formState: QuickCheckinFormState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = formState.entryDateText,
            onValueChange = { formState.entryDateText = it },
            label = { Text(stringResource(Res.string.checkin_date_label)) },
            isError = formState.isDateError,
            supportingText = if (formState.isDateError) {
                { Text(stringResource(Res.string.checkin_date_error)) }
            } else {
                null
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        CheckinSliderField(
            label = stringResource(Res.string.checkin_pain_level),
            value = formState.painLevel,
            onValueChange = { formState.painLevel = it },
        )

        CheckinSliderField(
            label = stringResource(Res.string.checkin_fatigue_level),
            value = formState.fatigueLevel,
            onValueChange = { formState.fatigueLevel = it },
        )

        OutlinedTextField(
            value = formState.stiffnessMinutes,
            onValueChange = formState::updateStiffnessMinutes,
            label = { Text(stringResource(Res.string.checkin_stiffness_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.label_active_flare),
                style = MaterialTheme.typography.bodyLarge,
            )
            Switch(
                checked = formState.hasActiveFlare,
                onCheckedChange = { formState.hasActiveFlare = it },
            )
        }
    }
}

@Composable
private fun CheckinSliderField(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(Res.string.checkin_slider_value, label, value.roundToInt()),
            style = MaterialTheme.typography.bodyMedium,
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..10f,
            steps = 9,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
