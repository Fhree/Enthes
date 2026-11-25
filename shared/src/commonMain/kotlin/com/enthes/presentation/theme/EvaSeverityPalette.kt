package com.enthes.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

const val MIN_EVA_LEVEL = 0
const val MAX_EVA_LEVEL = 10
const val EVA_LEVEL_COUNT = 11

@Immutable
data class EvaSeverityColor(
    val container: Color,
    val onContainer: Color,
)

@Immutable
data class EvaSeverityPalette(
    val levels: List<EvaSeverityColor>,
) {
    init {
        require(levels.size == EVA_LEVEL_COUNT) {
            "EVA severity palette must contain exactly $EVA_LEVEL_COUNT levels (0..10), got ${levels.size}."
        }
    }

    fun colorFor(level: Int): EvaSeverityColor =
        levels[level.coerceIn(MIN_EVA_LEVEL, MAX_EVA_LEVEL)]
}

val LightEvaSeverityPalette = EvaSeverityPalette(
    levels = listOf(
        EvaSeverityColor(container = Color(0xFFE4F2E7), onContainer = Color(0xFF1A3D24)), // 0
        EvaSeverityColor(container = Color(0xFFEAF3E2), onContainer = Color(0xFF233E1B)), // 1
        EvaSeverityColor(container = Color(0xFFF0F4DE), onContainer = Color(0xFF2D3E16)), // 2
        EvaSeverityColor(container = Color(0xFFF5F3DA), onContainer = Color(0xFF393A12)), // 3
        EvaSeverityColor(container = Color(0xFFF9F1D6), onContainer = Color(0xFF42360F)), // 4
        EvaSeverityColor(container = Color(0xFFFBEED3), onContainer = Color(0xFF4B320D)), // 5
        EvaSeverityColor(container = Color(0xFFFCE9D1), onContainer = Color(0xFF522D0E)), // 6
        EvaSeverityColor(container = Color(0xFFFCE4D2), onContainer = Color(0xFF592813)), // 7
        EvaSeverityColor(container = Color(0xFFFBDED4), onContainer = Color(0xFF5E2218)), // 8
        EvaSeverityColor(container = Color(0xFFF9D9D7), onContainer = Color(0xFF621D1D)), // 9
        EvaSeverityColor(container = Color(0xFFF7D4D6), onContainer = Color(0xFF651920)), // 10
    ),
)

val DarkEvaSeverityPalette = EvaSeverityPalette(
    levels = listOf(
        EvaSeverityColor(container = Color(0xFF1F3626), onContainer = Color(0xFFCDE9D3)), // 0
        EvaSeverityColor(container = Color(0xFF243722), onContainer = Color(0xFFD5EAC8)), // 1
        EvaSeverityColor(container = Color(0xFF2C381F), onContainer = Color(0xFFDEEBC0)), // 2
        EvaSeverityColor(container = Color(0xFF34381E), onContainer = Color(0xFFE8E9BC)), // 3
        EvaSeverityColor(container = Color(0xFF3B361D), onContainer = Color(0xFFF1E6BA)), // 4
        EvaSeverityColor(container = Color(0xFF41331D), onContainer = Color(0xFFF7E0BA)), // 5
        EvaSeverityColor(container = Color(0xFF462F1E), onContainer = Color(0xFFFAD8BD)), // 6
        EvaSeverityColor(container = Color(0xFF492B20), onContainer = Color(0xFFFBD0C2)), // 7
        EvaSeverityColor(container = Color(0xFF4B2622), onContainer = Color(0xFFFBC8C6)), // 8
        EvaSeverityColor(container = Color(0xFF4C2224), onContainer = Color(0xFFFAC0C4)), // 9
        EvaSeverityColor(container = Color(0xFF4D1F25), onContainer = Color(0xFFF9B9C0)), // 10
    ),
)

val LocalEvaSeverityPalette = staticCompositionLocalOf { LightEvaSeverityPalette }

@Composable
@ReadOnlyComposable
fun evaSeverityColorFor(level: Int): EvaSeverityColor =
    LocalEvaSeverityPalette.current.colorFor(level)
