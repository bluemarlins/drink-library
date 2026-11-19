package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.FrequentDrink
import com.bluemarlin.drinkdiary.domain.model.LogHighlights
import com.bluemarlin.drinkdiary.domain.model.MonthSummary
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

// 요약 탭의 "이번 달", "최근에 마신 술", "가장 자주 마신 술" 카드.
// 시계와 시간대를 주입받는다 — "이번 달"은 기기 시간대의 달력 월이고, 테스트는 날짜를 고정해야 한다.
class ObserveLogHighlightsUseCase(
    private val repository: DrinkRecordRepository,
    private val now: () -> Long = System::currentTimeMillis,
    private val zone: () -> ZoneId = ZoneId::systemDefault,
) {
    operator fun invoke(): Flow<LogHighlights> =
        repository.observeRecords(null).map { records -> summarizeLog(records, now(), zone()) }
}

internal fun summarizeLog(
    records: List<DrinkRecord>,
    nowMillis: Long,
    zone: ZoneId,
): LogHighlights {
    val latest = records.maxWithOrNull(compareBy<DrinkRecord> { it.recordedAtMillis }.thenBy { it.id })
    return LogHighlights(
        latest = latest,
        mostFrequent = mostFrequent(records),
        thisMonth = monthSummary(records, nowMillis, zone),
    )
}

private fun mostFrequent(records: List<DrinkRecord>): FrequentDrink? =
    records
        .filter { it.name.isNotBlank() }
        .groupBy { it.name.trim().lowercase() }
        .values
        .filter { it.size >= 2 }
        .map { group -> group.size to group.maxBy { it.recordedAtMillis } }
        // 횟수가 같으면 더 최근에 마신 술이 앞선다.
        .maxWithOrNull(compareBy<Pair<Int, DrinkRecord>> { it.first }.thenBy { it.second.recordedAtMillis })
        ?.let { (count, newest) ->
            FrequentDrink(name = newest.name.trim(), count = count, latestRecordId = newest.id)
        }

private fun monthSummary(
    records: List<DrinkRecord>,
    nowMillis: Long,
    zone: ZoneId,
): MonthSummary {
    val month = YearMonth.from(Instant.ofEpochMilli(nowMillis).atZone(zone))
    val inMonth =
        records.filter {
            YearMonth.from(Instant.ofEpochMilli(it.recordedAtMillis).atZone(zone)) == month
        }
    val weeks = (month.lengthOfMonth() + 6) / 7
    val weekly = MutableList(weeks) { 0 }
    inMonth.forEach {
        val day = Instant.ofEpochMilli(it.recordedAtMillis).atZone(zone).dayOfMonth
        weekly[(day - 1) / 7]++
    }
    return MonthSummary(
        month = month.monthValue,
        count = inMonth.size,
        wineCount = inMonth.count { it.type == DrinkType.Wine },
        whiskeyCount = inMonth.count { it.type == DrinkType.Whiskey },
        averageRating = inMonth.takeIf { it.isNotEmpty() }?.map { it.rating }?.average(),
        weeklyCounts = weekly,
    )
}
