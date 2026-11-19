package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class SummarizeLogTest {
    private val seoul = ZoneId.of("Asia/Seoul")

    private fun at(date: String): Long =
        LocalDate
            .parse(date)
            .atTime(20, 0)
            .atZone(seoul)
            .toInstant()
            .toEpochMilli()

    private fun record(
        id: Long,
        name: String,
        date: String,
        type: DrinkType = DrinkType.Wine,
        rating: Double = 4.0,
    ) = DrinkRecord(id = id, type = type, name = name, rating = rating, recordedAtMillis = at(date))

    private val now = at("2026-11-19")

    @Test
    fun `this month counts only the current calendar month and buckets by week`() {
        val records =
            listOf(
                record(1, "샤블리", "2026-10-31"),
                record(2, "샤블리", "2026-11-01", rating = 3.0),
                record(3, "글렌피딕 12", "2026-11-08", type = DrinkType.Whiskey, rating = 5.0),
                record(4, "바롤로", "2026-11-19", rating = 4.0),
            )

        val month = summarizeLog(records, now, seoul).thisMonth

        assertEquals(11, month.month)
        assertEquals(3, month.count)
        assertEquals(2, month.wineCount)
        assertEquals(1, month.whiskeyCount)
        assertEquals(4.0, month.averageRating!!, 0.0001)
        // 11월은 30일이라 1~7 · 8~14 · 15~21 · 22~28 · 29~30의 다섯 칸.
        assertEquals(listOf(1, 1, 1, 0, 0), month.weeklyCounts)
    }

    @Test
    fun `an empty month has no average rather than zero`() {
        val month = summarizeLog(listOf(record(1, "샤블리", "2026-10-02")), now, seoul).thisMonth

        assertEquals(0, month.count)
        assertNull(month.averageRating)
    }

    @Test
    fun `most frequent ignores case and spacing and needs at least two glasses`() {
        val records =
            listOf(
                record(1, "Glenfiddich 12", "2026-09-01"),
                record(2, " glenfiddich 12 ", "2026-10-01"),
                record(3, "샤블리", "2026-11-01"),
            )

        val frequent = summarizeLog(records, now, seoul).mostFrequent!!

        assertEquals("glenfiddich 12", frequent.name)
        assertEquals(2, frequent.count)
        assertEquals(2L, frequent.latestRecordId)
    }

    @Test
    fun `nothing drunk twice means no most frequent drink`() {
        val records = listOf(record(1, "샤블리", "2026-11-01"), record(2, "바롤로", "2026-11-02"))

        assertNull(summarizeLog(records, now, seoul).mostFrequent)
    }

    @Test
    fun `a tie goes to the drink had most recently`() {
        val records =
            listOf(
                record(1, "샤블리", "2026-09-01"),
                record(2, "샤블리", "2026-09-02"),
                record(3, "바롤로", "2026-10-01"),
                record(4, "바롤로", "2026-11-01"),
            )

        assertEquals("바롤로", summarizeLog(records, now, seoul).mostFrequent!!.name)
    }

    @Test
    fun `latest is the most recently drunk record`() {
        val records = listOf(record(1, "샤블리", "2026-11-10"), record(2, "바롤로", "2026-11-02"))

        assertEquals(1L, summarizeLog(records, now, seoul).latest!!.id)
    }
}
