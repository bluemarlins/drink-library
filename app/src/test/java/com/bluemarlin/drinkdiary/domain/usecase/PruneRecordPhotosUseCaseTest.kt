package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import com.bluemarlin.drinkdiary.domain.repository.RecordPhotoStore
import com.bluemarlin.drinkdiary.domain.repository.StoredPhoto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class PruneRecordPhotosUseCaseTest {
    private val now = 10_000_000L
    private val hour = 60L * 60L * 1000L

    private class FakeRepository(
        private val records: List<DrinkRecord>,
    ) : DrinkRecordRepository {
        override fun observeRecords(type: DrinkType?): Flow<List<DrinkRecord>> = flowOf(records)

        override fun observeRecord(id: Long): Flow<DrinkRecord?> = flowOf(records.firstOrNull { it.id == id })

        override fun observeSearchResults(query: String): Flow<List<DrinkRecord>> = flowOf(emptyList())

        override suspend fun save(record: DrinkRecord): AppResult<Long> = AppResult.Success(record.id)

        override suspend fun deleteById(id: Long): AppResult<Unit> = AppResult.Success(Unit)
    }

    private class FakeStore(
        var stored: List<StoredPhoto>,
    ) : RecordPhotoStore {
        val deleted = mutableListOf<String>()

        override suspend fun import(sourceUri: String): AppResult<String> = AppResult.Success(sourceUri)

        override suspend fun listStored(): List<StoredPhoto> = stored

        override suspend fun delete(uri: String) {
            deleted += uri
        }
    }

    private fun record(
        id: Long,
        imageUri: String?,
    ) = DrinkRecord(
        id = id,
        type = DrinkType.Wine,
        name = "w$id",
        rating = 4.0,
        imageUri = imageUri,
        recordedAtMillis = id,
    )

    @Test
    fun `deletes old photos no record points to and keeps the rest`() =
        runBlocking {
            val store =
                FakeStore(
                    listOf(
                        StoredPhoto("file:///p/kept.img", now - 2 * hour),
                        StoredPhoto("file:///p/orphan.img", now - 2 * hour),
                    ),
                )
            val useCase =
                PruneRecordPhotosUseCase(
                    repository = FakeRepository(listOf(record(1, "file:///p/kept.img"), record(2, null))),
                    photoStore = store,
                    now = { now },
                )

            assertEquals(1, useCase())
            assertEquals(listOf("file:///p/orphan.img"), store.deleted)
        }

    // 입력 화면에서 방금 고른 사진은 아직 어떤 기록에도 없다. 이것을 지우면 저장할 때 사진이 깨진다.
    @Test
    fun `keeps a just-picked photo that is not saved yet`() =
        runBlocking {
            val store = FakeStore(listOf(StoredPhoto("file:///p/pending.img", now - 5 * 60 * 1000L)))
            val useCase = PruneRecordPhotosUseCase(FakeRepository(emptyList()), store, now = { now })

            assertEquals(0, useCase())
            assertEquals(emptyList<String>(), store.deleted)
        }
}
