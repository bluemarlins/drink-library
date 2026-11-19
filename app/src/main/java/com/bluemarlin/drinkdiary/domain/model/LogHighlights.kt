package com.bluemarlin.drinkdiary.domain.model

// 요약 탭 위쪽 카드들의 재료. 취향 판정과 달리 주종을 가리지 않는 기록 전체의 사실이다.
data class LogHighlights(
    val latest: DrinkRecord? = null,
    // 두 번 이상 마신 술 중 가장 많이 마신 것. 한 번씩만 마셨다면 "자주"라고 부를 수 없어 null이다.
    val mostFrequent: FrequentDrink? = null,
    val thisMonth: MonthSummary = MonthSummary(),
)

data class FrequentDrink(
    // 가장 최근 기록에 적힌 그대로의 이름. 묶을 때는 앞뒤 공백과 대소문자를 무시한다.
    val name: String,
    val count: Int,
    val latestRecordId: Long,
)

data class MonthSummary(
    val month: Int = 1,
    val count: Int = 0,
    val wineCount: Int = 0,
    val whiskeyCount: Int = 0,
    val averageRating: Double? = null,
    // 이번 달 1~7일, 8~14일 … 순서의 잔 수. 달의 길이에 따라 4칸 또는 5칸이다.
    val weeklyCounts: List<Int> = emptyList(),
)
