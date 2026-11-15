package com.example.ds.component

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FloatingGlassNavigationTest {

    @Test
    fun `calculateScrollProgress interpolates continuously over 0dp to 64dp threshold`() {
        val thresholdPx = 192f
        assertEquals(0f, calculateScrollProgress(0, 0, thresholdPx), 0.0001f)
        assertEquals(0f, calculateScrollProgress(0, -10, thresholdPx), 0.0001f)
        assertEquals(0.5f, calculateScrollProgress(0, 96, thresholdPx), 0.0001f)
        assertEquals(1f, calculateScrollProgress(0, 192, thresholdPx), 0.0001f)
        assertEquals(1f, calculateScrollProgress(0, 400, thresholdPx), 0.0001f)
        assertEquals(1f, calculateScrollProgress(1, 0, thresholdPx), 0.0001f)
    }

    @Test
    fun `calculateBottomIslandContentPadding includes island height nav inset margin and breathing`() {
        val navInset = 24.dp
        val computed = calculateBottomIslandContentPadding(
            navBarBottomInset = navInset,
            islandHeight = 60.dp,
            bottomMargin = 16.dp,
            extraBreathing = 16.dp,
        )
        assertEquals(116.dp, computed)
    }

    @Test
    fun `calculateLiquidSquishStretch returns identity at rest`() {
        val (sx, sy) = calculateLiquidSquishStretch(
            currentIndex = 2f,
            targetIndex = 2f,
            velocity = 0f,
            isLiquid = true,
        )
        assertEquals(1f, sx, 0.0001f)
        assertEquals(1f, sy, 0.0001f)
    }

    @Test
    fun `calculateLiquidSquishStretch stretches horizontally at high mid-flight velocity`() {
        val (sx, sy) = calculateLiquidSquishStretch(
            currentIndex = 0.6f,
            targetIndex = 3f,
            velocity = 8.0f,
            isLiquid = true,
        )
        assertTrue("Expected horizontal stretch scaleX > 1f, got $sx", sx > 1.15f)
        assertTrue("Expected vertical compression scaleY < 1f, got $sy", sy < 0.92f)
    }

    @Test
    fun `calculateLiquidSquishStretch squishes elastically upon arrival deceleration`() {
        val (sx, sy) = calculateLiquidSquishStretch(
            currentIndex = 1.94f,
            targetIndex = 2f,
            velocity = 0.25f,
            isLiquid = true,
        )
        assertTrue("Expected arrival horizontal squish scaleX < 1f, got $sx", sx < 1f)
        assertTrue("Expected arrival vertical rebound scaleY > 1f, got $sy", sy > 1f)
    }

    @Test
    fun `calculateMetaballWaistRatio progresses monotonically with mergeProgress`() {
        val zero = calculateMetaballWaistRatio(0f)
        val tear = calculateMetaballWaistRatio(0.3f)
        val viscous = calculateMetaballWaistRatio(0.6f)
        val full = calculateMetaballWaistRatio(1f)

        assertEquals(0f, zero, 0.0001f)
        assertTrue(tear in 0.08f..0.86f)
        assertTrue(viscous > tear)
        assertEquals(0.86f, full, 0.0001f)
    }
}
