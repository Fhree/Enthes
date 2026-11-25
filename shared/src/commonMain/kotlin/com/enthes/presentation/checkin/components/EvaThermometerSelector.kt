package com.enthes.presentation.checkin.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.enthes.presentation.theme.EnthesTheme
import com.enthes.presentation.theme.MAX_EVA_LEVEL
import com.enthes.presentation.theme.MIN_EVA_LEVEL
import com.enthes.presentation.theme.evaSeverityColorFor

private const val MID_EVA_LEVEL = 5

@Composable
fun EvaThermometerSelector(
    lowAnchorLabel: String,
    midAnchorLabel: String,
    highAnchorLabel: String,
    selectedLevel: Int?,
    onSelectLevel: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        for (level in MIN_EVA_LEVEL..MAX_EVA_LEVEL) {
            val anchorLabel = when (level) {
                MIN_EVA_LEVEL -> lowAnchorLabel
                MID_EVA_LEVEL -> midAnchorLabel
                MAX_EVA_LEVEL -> highAnchorLabel
                else -> null
            }

            EvaLevelBar(
                level = level,
                anchorLabel = anchorLabel,
                isSelected = selectedLevel == level,
                onClick = { onSelectLevel(level) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
        }
    }
}

@Composable
private fun EvaLevelBar(
    level: Int,
    anchorLabel: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = evaSeverityColorFor(level)

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colors.container,
        contentColor = colors.onContainer,
        border = if (isSelected) {
            BorderStroke(width = 2.5.dp, color = colors.onContainer)
        } else {
            null
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = level.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
            )
            if (anchorLabel != null) {
                Text(
                    text = anchorLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Preview
@Composable
private fun EvaThermometerSelectorPreview() {
    EnthesTheme {
        EvaThermometerSelector(
            lowAnchorLabel = "No pain",
            midAnchorLabel = "Moderate pain",
            highAnchorLabel = "Unbearable pain",
            selectedLevel = 4,
            onSelectLevel = {},
        )
    }
}
