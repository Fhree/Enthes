package com.enthes.presentation.checkin.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.enthes.presentation.checkin.StiffnessOption
import com.enthes.presentation.theme.EnthesTheme
import com.enthes.presentation.theme.evaSeverityColorFor
import enthes.shared.generated.resources.Res
import enthes.shared.generated.resources.stiffness_1_to_2_hours
import enthes.shared.generated.resources.stiffness_30_to_60_min
import enthes.shared.generated.resources.stiffness_none
import enthes.shared.generated.resources.stiffness_over_2_hours
import enthes.shared.generated.resources.stiffness_under_30_min
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun StiffnessRangeSelector(
    selectedMinutes: Int?,
    onSelectMinutes: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StiffnessOption.entries.forEach { option ->
            val isSelected = selectedMinutes == option.minutes
            val colors = evaSeverityColorFor(option.severityLevel)

            Surface(
                onClick = { onSelectMinutes(option.minutes) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = colors.container,
                contentColor = colors.onContainer,
                border = if (isSelected) {
                    BorderStroke(width = 2.5.dp, color = colors.onContainer)
                } else {
                    null
                },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = stringResource(option.labelRes()),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

fun StiffnessOption.labelRes(): StringResource = when (this) {
    StiffnessOption.NONE -> Res.string.stiffness_none
    StiffnessOption.UNDER_30_MIN -> Res.string.stiffness_under_30_min
    StiffnessOption.THIRTY_TO_SIXTY_MIN -> Res.string.stiffness_30_to_60_min
    StiffnessOption.ONE_TO_TWO_HOURS -> Res.string.stiffness_1_to_2_hours
    StiffnessOption.OVER_TWO_HOURS -> Res.string.stiffness_over_2_hours
}

@Preview
@Composable
private fun StiffnessRangeSelectorPreview() {
    EnthesTheme {
        StiffnessRangeSelector(
            selectedMinutes = 45,
            onSelectMinutes = {},
        )
    }
}
