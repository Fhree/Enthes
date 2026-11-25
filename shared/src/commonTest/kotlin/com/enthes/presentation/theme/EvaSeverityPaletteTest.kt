package com.enthes.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class EvaSeverityPaletteTest {

    @Test
    fun lightAndDarkPalettesContainElevenSteps() {
        assertEquals(EVA_LEVEL_COUNT, LightEvaSeverityPalette.levels.size)
        assertEquals(EVA_LEVEL_COUNT, DarkEvaSeverityPalette.levels.size)
    }

    @Test
    fun colorForClampsOutOfRangeLevels() {
        assertEquals(LightEvaSeverityPalette.colorFor(0), LightEvaSeverityPalette.colorFor(-5))
        assertEquals(LightEvaSeverityPalette.colorFor(10), LightEvaSeverityPalette.colorFor(15))
        assertEquals(DarkEvaSeverityPalette.colorFor(0), DarkEvaSeverityPalette.colorFor(-1))
        assertEquals(DarkEvaSeverityPalette.colorFor(10), DarkEvaSeverityPalette.colorFor(42))
    }

    @Test
    fun paletteRejectsInvalidStepCount() {
        assertFailsWith<IllegalArgumentException> {
            EvaSeverityPalette(levels = emptyList())
        }
    }

    @Test
    fun lightPaletteMeetsWcagContrastForAllLevels() {
        for (level in MIN_EVA_LEVEL..MAX_EVA_LEVEL) {
            val colors = LightEvaSeverityPalette.colorFor(level)
            val ratio = contrastRatio(colors.container, colors.onContainer)
            assertTrue(
                actual = ratio >= 7.0,
                message = "Light EVA level $level contrast ratio was $ratio (expected >= 7.0)",
            )
        }
    }

    @Test
    fun darkPaletteMeetsWcagContrastForAllLevels() {
        for (level in MIN_EVA_LEVEL..MAX_EVA_LEVEL) {
            val colors = DarkEvaSeverityPalette.colorFor(level)
            val ratio = contrastRatio(colors.container, colors.onContainer)
            assertTrue(
                actual = ratio >= 7.0,
                message = "Dark EVA level $level contrast ratio was $ratio (expected >= 7.0)",
            )
        }
    }

    private fun contrastRatio(first: Color, second: Color): Double {
        val l1 = first.luminance().toDouble()
        val l2 = second.luminance().toDouble()
        val lighter = max(l1, l2)
        val darker = min(l1, l2)
        return (lighter + 0.05) / (darker + 0.05)
    }
}
