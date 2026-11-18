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
import kotlin.math.roundToInt
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
fun QuickCheckinDialog(
    onDismissRequest: () -> Unit,
    onSaveCheckin: (DiseaseActivity) -> Unit,
    modifier: Modifier = Modifier,
) {
    var entryDate by remember { mutableStateOf("2026-11-18") }
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = entryDate,
                    onValueChange = { entryDate = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Column {
                    Text(
                        text = "Pain Level: ${painLevel.roundToInt()}/10",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Slider(
                        value = painLevel,
                        onValueChange = { painLevel = it },
                        valueRange = 0f..10f,
                        steps = 9,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Column {
                    Text(
                        text = "Fatigue Level: ${fatigueLevel.roundToInt()}/10",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Slider(
                        value = fatigueLevel,
                        onValueChange = { fatigueLevel = it },
                        valueRange = 0f..10f,
                        steps = 9,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                OutlinedTextField(
                    value = stiffnessMinutes,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            stiffnessMinutes = input
                        }
                    },
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
                        onCheckedChange = { hasActiveFlare = it },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val activity = DiseaseActivity(
                        id = Uuid.random().toString(),
                        entryDate = entryDate.ifBlank { "2026-11-18" },
                        morningStiffnessMinutes = stiffnessMinutes.toIntOrNull() ?: 0,
                        globalPainLevel = painLevel.roundToInt().coerceIn(0, 10),
                        fatigueLevel = fatigueLevel.roundToInt().coerceIn(0, 10),
                        hasActiveFlare = hasActiveFlare,
                        createdAt = kotlin.time.Clock.System.now().toEpochMilliseconds(),
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
