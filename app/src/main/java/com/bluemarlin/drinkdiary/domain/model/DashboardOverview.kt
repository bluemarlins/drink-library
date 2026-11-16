package com.bluemarlin.drinkdiary.domain.model

data class DashboardOverview(
    val totalCount: Int = 0,
    val wineCount: Int = 0,
    val whiskeyCount: Int = 0,
    val averageRating: Double? = null,
    val repurchaseCount: Int = 0,
    val notForMeCount: Int = 0,
    val scopedCount: Int = 0,
    val scopedAverageRating: Double? = null,
    val scopedRepurchaseCount: Int = 0,
)
