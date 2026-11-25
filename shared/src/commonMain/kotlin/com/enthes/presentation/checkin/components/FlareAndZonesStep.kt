package com.enthes.presentation.checkin.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.enthes.domain.model.PainZone
import com.enthes.presentation.checkin.labelRes
import com.enthes.presentation.theme.EnthesTheme
import enthes.shared.generated.resources.Res
import enthes.shared.generated.resources.action_no
import enthes.shared.generated.resources.action_review_summary
import enthes.shared.generated.resources.action_yes
import enthes.shared.generated.resources.wizard_flare_question
import enthes.shared.generated.resources.wizard_pain_zones_section
import org.jetbrains.compose.resources.stringResource

@Composable
fun FlareAndZonesStep(
    hasActiveFlare: Boolean,
    selectedZones: Set<PainZone>,
    onSetActiveFlare: (Boolean) -> Unit,
    onTogglePainZone: (PainZone) -> Unit,
    onProceedToSummary: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(Res.string.wizard_flare_question),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ErgonomicToggleCard(
                    label = stringResource(Res.string.action_no),
                    isSelected = !hasActiveFlare,
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = { onSetActiveFlare(false) },
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                )
                ErgonomicToggleCard(
                    label = stringResource(Res.string.action_yes),
                    isSelected = hasActiveFlare,
                    selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                    selectedContentColor = MaterialTheme.colorScheme.onErrorContainer,
                    onClick = { onSetActiveFlare(true) },
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(Res.string.wizard_pain_zones_section),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            PainZoneGrid(
                selectedZones = selectedZones,
                onTogglePainZone = onTogglePainZone,
            )
        }

        Button(
            onClick = onProceedToSummary,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = stringResource(Res.string.action_review_summary),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun PainZoneGrid(
    selectedZones: Set<PainZone>,
    onTogglePainZone: (PainZone) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rows = PainZone.entries.chunked(2)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        rows.forEach { rowZones ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowZones.forEach { zone ->
                    ErgonomicToggleCard(
                        label = stringResource(zone.labelRes()),
                        isSelected = zone in selectedZones,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        onClick = { onTogglePainZone(zone) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 68.dp),
                    )
                }
                if (rowZones.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ErgonomicToggleCard(
    label: String,
    isSelected: Boolean,
    selectedContainerColor: Color,
    selectedContentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (isSelected) {
        selectedContainerColor
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (isSelected) {
        selectedContentColor
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
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
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview
@Composable
private fun FlareAndZonesStepPreview() {
    EnthesTheme {
        FlareAndZonesStep(
            hasActiveFlare = true,
            selectedZones = setOf(PainZone.LUMBAR, PainZone.FEET_HEELS),
            onSetActiveFlare = {},
            onTogglePainZone = {},
            onProceedToSummary = {},
        )
    }
}
