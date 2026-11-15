package com.example.ds.foundation.layout

import androidx.compose.ui.unit.dp
import com.example.ds.foundation.typography.AuraTypographyDefaults
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuraLayoutTest {

    @Test
    fun `foldable cover display triggers ExtraSmall responsive tokens`() {
        val window = AuraWindowInfo.fromDimensions(
            smallestWidthDp = 344,
            widthDp = 344,
            heightDp = 820,
        )

        assertEquals(AuraWidthClass.ExtraSmall, window.smallestWidthClass)
        assertEquals(AuraWidthClass.ExtraSmall, window.widthClass)
        assertEquals(AuraDisplayRatio.UltraTall, window.displayRatio)

        val layout = AuraLayoutDefaults.fromWindowInfo(window)
        assertEquals(1, layout.gridColumns)
        assertFalse(layout.supportsTwoPane)
        assertEquals(12.dp, layout.screenMarginHorizontal)
        assertEquals(0.92f, layout.typographyScale)
    }

    @Test
    fun `standard compact portrait phone uses baseline Compact layout even on 20 by 9 aspect`() {
        val window = AuraWindowInfo.fromDimensions(
            smallestWidthDp = 412,
            widthDp = 412,
            heightDp = 915,
        )

        assertEquals(AuraWidthClass.Compact, window.smallestWidthClass)
        assertEquals(AuraWidthClass.Compact, window.widthClass)

        val layout = AuraLayoutDefaults.fromWindowInfo(window)
        assertEquals(1, layout.gridColumns)
        assertFalse(layout.supportsTwoPane)
        assertEquals(16.dp, layout.screenMarginHorizontal)
        assertEquals(1.00f, layout.typographyScale)
    }

    @Test
    fun `landscape phone uses compact vertical chrome and two-pane split`() {
        val window = AuraWindowInfo.fromDimensions(
            smallestWidthDp = 412,
            widthDp = 915,
            heightDp = 412,
        )

        assertEquals(AuraWidthClass.Compact, window.smallestWidthClass)
        assertEquals(AuraHeightClass.Compact, window.heightClass)
        assertEquals(AuraDisplayRatio.UltraWide, window.displayRatio)

        val layout = AuraLayoutDefaults.fromWindowInfo(window)
        assertEquals(2, layout.gridColumns)
        assertTrue(layout.supportsTwoPane)
        assertEquals(48.dp, layout.topBarHeight)
        assertEquals(0.42f, layout.primaryPaneFraction)
        assertEquals(0.96f, layout.typographyScale)
    }

    @Test
    fun `unfolded foldable with square aspect ratio uses symmetrical two-pane layout`() {
        val window = AuraWindowInfo.fromDimensions(
            smallestWidthDp = 690,
            widthDp = 710,
            heightDp = 760,
        )

        assertEquals(AuraWidthClass.Medium, window.widthClass)
        assertEquals(AuraDisplayRatio.Square, window.displayRatio)

        val layout = AuraLayoutDefaults.fromWindowInfo(window)
        assertEquals(2, layout.gridColumns)
        assertTrue(layout.supportsTwoPane)
        assertEquals(0.45f, layout.primaryPaneFraction)
        assertEquals(1.04f, layout.typographyScale)
    }

    @Test
    fun `expanded landscape tablet expands to 3 columns and 38-62 split`() {
        val window = AuraWindowInfo.fromDimensions(
            smallestWidthDp = 840,
            widthDp = 1280,
            heightDp = 800,
        )

        assertEquals(AuraWidthClass.Expanded, window.widthClass)
        assertEquals(AuraDisplayRatio.Landscape, window.displayRatio)

        val layout = AuraLayoutDefaults.fromWindowInfo(window)
        assertEquals(3, layout.gridColumns)
        assertTrue(layout.supportsTwoPane)
        assertEquals(0.38f, layout.primaryPaneFraction)
        assertEquals(1.08f, layout.typographyScale)
    }

    @Test
    fun `scaled typography adjusts headings while keeping body readable`() {
        val standard = AuraTypographyDefaults.Standard
        val compactScaled = AuraTypographyDefaults.scaled(0.92f)
        val expandedScaled = AuraTypographyDefaults.scaled(1.08f)

        assertTrue(compactScaled.displayLarge.fontSize < standard.displayLarge.fontSize)
        assertTrue(expandedScaled.headlineLarge.fontSize > standard.headlineLarge.fontSize)
        assertEquals(standard.bodyMedium.fontSize, compactScaled.bodyMedium.fontSize)
        assertEquals(standard.bodyMedium.fontSize, expandedScaled.bodyMedium.fontSize)
    }
}
