package com.example.ds.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RemainingComponentsTest {

    @Test
    fun `sanitizeNumericText preserves valid digits optional decimal and leading sign`() {
        assertEquals("12345", sanitizeNumericText("12a34b5", allowDecimals = false, allowNegative = false))
        assertEquals("0.52", sanitizeNumericText(".5.2", allowDecimals = true, allowNegative = false))
        assertEquals("-42.75", sanitizeNumericText("-42.7.5-", allowDecimals = true, allowNegative = true))
        assertEquals("-42.759", sanitizeNumericText("-42.7.5-9", allowDecimals = true, allowNegative = true))
        assertEquals("42", sanitizeNumericText("-42", allowDecimals = false, allowNegative = false))
    }

    @Test
    fun `stepNumericValue increments decrements and clamps to bounds`() {
        assertEquals(
            "2019",
            stepNumericValue("2018", delta = 1.0, minValue = 1950.0, maxValue = 2030.0, allowDecimals = false),
        )
        assertEquals(
            "2030",
            stepNumericValue("2030", delta = 1.0, minValue = 1950.0, maxValue = 2030.0, allowDecimals = false),
        )
        assertEquals(
            "4.5",
            stepNumericValue("4.25", delta = 0.25, minValue = 0.0, maxValue = 25.0, allowDecimals = true),
        )
        assertEquals(
            "0",
            stepNumericValue("0.2", delta = -0.5, minValue = 0.0, maxValue = 10.0, allowDecimals = true),
        )
    }

    @Test
    fun `calendar helpers compute leap years month lengths and weekdays accurately`() {
        assertTrue(isLeapYear(2024))
        assertFalse(isLeapYear(1900))
        assertTrue(isLeapYear(2000))
        assertEquals(29, daysInMonth(2024, 2))
        assertEquals(28, daysInMonth(2026, 2))
        assertEquals(30, daysInMonth(2026, 11))
        // 2026-11-01 is Sunday (0); 2024-03-01 is Friday (5)
        assertEquals(0, firstDayOfWeekSunday(2026, 11))
        assertEquals(5, firstDayOfWeekSunday(2024, 3))
    }

    @Test
    fun `slider helpers map and quantize stepped ranges deterministically`() {
        assertEquals(0f, valueToFraction(1f, 1f..5f), 0.0001f)
        assertEquals(0.5f, valueToFraction(3f, 1f..5f), 0.0001f)
        assertEquals(1f, valueToFraction(5f, 1f..5f), 0.0001f)

        // 1f..5f with steps = 3 has intervals = 4 (values 1, 2, 3, 4, 5)
        assertEquals(4f, fractionToValue(0.70f, 1f..5f, steps = 3), 0.0001f)
        assertEquals(3f, fractionToValue(0.49f, 1f..5f, steps = 3), 0.0001f)
    }

    @Test
    fun `rating helpers snap half stars and format readouts cleanly`() {
        assertEquals(4.5f, snapRatingValue(4.38f, maxStars = 5, allowHalfRating = true), 0.0001f)
        assertEquals(4.0f, snapRatingValue(4.38f, maxStars = 5, allowHalfRating = false), 0.0001f)
        assertEquals(5.0f, snapRatingValue(7.2f, maxStars = 5, allowHalfRating = true), 0.0001f)

        assertEquals("4.0", formatRatingReadout(4f))
        assertEquals("4.5", formatRatingReadout(4.5f))
    }
}
