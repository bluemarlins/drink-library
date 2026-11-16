package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DashboardOverview
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveDashboardOverviewUseCase(
    private val repository: DrinkRecordRepository,
) {
    operator fun invoke(scope: TypeScope): Flow<DashboardOverview> =
        repository.observeRecords(null).map { allRecords ->
            val scoped =
                when (scope) {
                    TypeScope.Wine -> allRecords.filter { it.type == DrinkType.Wine }
                    TypeScope.Whiskey -> allRecords.filter { it.type == DrinkType.Whiskey }
                    TypeScope.Combined -> allRecords
                }
            DashboardOverview(
                totalCount = allRecords.size,
                wineCount = allRecords.count { it.type == DrinkType.Wine },
                whiskeyCount = allRecords.count { it.type == DrinkType.Whiskey },
                averageRating = allRecords.takeIf { it.isNotEmpty() }?.map { it.rating }?.average(),
                repurchaseCount = allRecords.count { it.collectionStatus == CollectionStatus.Repurchase },
                notForMeCount = allRecords.count { it.collectionStatus == CollectionStatus.NotForMe },
                scopedCount = scoped.size,
                scopedAverageRating = scoped.takeIf { it.isNotEmpty() }?.map { it.rating }?.average(),
                scopedRepurchaseCount = scoped.count { it.collectionStatus == CollectionStatus.Repurchase },
            )
        }
}
