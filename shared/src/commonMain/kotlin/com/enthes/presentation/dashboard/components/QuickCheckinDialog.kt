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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
fun QuickCheckinDialog(
    onDismissRequest: () -> Unit,
    onSaveCheckin: (DiseaseActivity) -> Unit,
    modifier: Modifier = Modifier,
) {
    val todayDate = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()).toString() }
    var entryDate by remember { mutableStateOf(todayDate) }
    var painLevel by remember { mutableFloatStateOf(3f) }
    var fatigueLevel by remember { mutableFloatStateOf(3f) }
    var stiffnessMinutes by remember { mutableStateOf("15") }
    var hasActiveFlare by remember { mutableStateOf(value = false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = {
            Text(
                text = "New Daily Check-in",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            QuickCheckinForm(
                entryDate = entryDate,
                onEntryDateChange = { entryDate = it },
                painLevel = painLevel,
                onPainLevelChange = { painLevel = it },
                fatigueLevel = fatigueLevel,
                onFatigueLevelChange = { fatigueLevel = it },
                stiffnessMinutes = stiffnessMinutes,
                onStiffnessMinutesChange = { input ->
                    if (input.all { it.isDigit() }) {
                        stiffnessMinutes = input
                    }
                },
                hasActiveFlare = hasActiveFlare,
                onHasActiveFlareChange = { hasActiveFlare = it },
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val activity = DiseaseActivity(
                        id = Uuid.random().toString(),
                        entryDate = entryDate.ifBlank { todayDate },
                        morningStiffnessMinutes = stiffnessMinutes.toIntOrNull() ?: 0,
                        globalPainLevel = painLevel.roundToInt().coerceIn(0, 10),
                        fatigueLevel = fatigueLevel.roundToInt().coerceIn(0, 10),
                        hasActiveFlare = hasActiveFlare,
                        createdAt = Clock.System.now().toEpochMilliseconds(),
                    )
                    onSaveCheckin(activity)
                },
            ) {
                Text("Save")
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
private fun QuickCheckinForm(
    entryDate: String,
    onEntryDateChange: (String) -> Unit,
    painLevel: Float,
    onPainLevelChange: (Float) -> Unit,
    fatigueLevel: Float,
    onFatigueLevelChange: (Float) -> Unit,
    stiffnessMinutes: String,
    onStiffnessMinutesChange: (String) -> Unit,
    hasActiveFlare: Boolean,
    onHasActiveFlareChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = entryDate,
            onValueChange = onEntryDateChange,
            label = { Text("Date (YYYY-MM-DD)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        CheckinSliderField(
            label = "Pain Level",
            value = painLevel,
            onValueChange = onPainLevelChange,
        )

        CheckinSliderField(
            label = "Fatigue Level",
            value = fatigueLevel,
            onValueChange = onFatigueLevelChange,
        )

        OutlinedTextField(
            value = stiffnessMinutes,
            onValueChange = onStiffnessMinutesChange,
            label = { Text("Morning Stiffness (minutes)") },
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
                text = "Active Flare",
                style = MaterialTheme.typography.bodyLarge,
            )
            Switch(
                checked = hasActiveFlare,
                onCheckedChange = onHasActiveFlareChange,
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
            text = "$label: ${value.roundToInt()}/10",
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
