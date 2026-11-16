package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ObserveDashboardOverviewUseCaseTest {
    private class FakeRepository(
        private val records: List<DrinkRecord>,
    ) : DrinkRecordRepository {
        override fun observeRecords(type: DrinkType?): Flow<List<DrinkRecord>> =
            flowOf(if (type == null) records else records.filter { it.type == type })

        override fun observeRecord(id: Long): Flow<DrinkRecord?> = flowOf(records.firstOrNull { it.id == id })

        override fun observeSearchResults(query: String): Flow<List<DrinkRecord>> = flowOf(records)

        override suspend fun save(record: DrinkRecord): AppResult<Long> = AppResult.Success(1L)

        override suspend fun deleteById(id: Long): AppResult<Unit> = AppResult.Success(Unit)
    }

    private fun rec(
        id: Long,
        type: DrinkType,
        rating: Double,
        status: CollectionStatus = CollectionStatus.Normal,
    ) = DrinkRecord(
        id = id,
        type = type,
        name = "Drink $id",
        rating = rating,
        collectionStatus = status,
        recordedAtMillis = id * 1000L,
    )

    @Test
    fun `empty repository yields zeroes and null averages`() =
        runBlocking {
            val overview = ObserveDashboardOverviewUseCase(FakeRepository(emptyList())).invoke(TypeScope.Wine).first()

            assertEquals(0, overview.totalCount)
            assertEquals(0, overview.wineCount)
            assertEquals(0, overview.whiskeyCount)
            assertNull(overview.averageRating)
            assertNull(overview.scopedAverageRating)
        }

    @Test
    fun `summarizes totals and scoped stats accurately`() =
        runBlocking {
            val records =
                listOf(
                    rec(1, DrinkType.Wine, 4.5, CollectionStatus.Repurchase),
                    rec(2, DrinkType.Wine, 3.5, CollectionStatus.Normal),
                    rec(3, DrinkType.Whiskey, 2.0, CollectionStatus.NotForMe),
                )
            val useCase = ObserveDashboardOverviewUseCase(FakeRepository(records))

            val wineOverview = useCase(TypeScope.Wine).first()
            assertEquals(3, wineOverview.totalCount)
            assertEquals(2, wineOverview.wineCount)
            assertEquals(1, wineOverview.whiskeyCount)
            assertEquals(1, wineOverview.repurchaseCount)
            assertEquals(1, wineOverview.notForMeCount)
            assertEquals(2, wineOverview.scopedCount)
            assertEquals(4.0, wineOverview.scopedAverageRating ?: 0.0, 0.001)
            assertEquals(1, wineOverview.scopedRepurchaseCount)

            val combinedOverview = useCase(TypeScope.Combined).first()
            assertEquals(3, combinedOverview.scopedCount)
            assertEquals(10.0 / 3.0, combinedOverview.averageRating ?: 0.0, 0.001)
        }
}
