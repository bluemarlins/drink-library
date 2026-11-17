package com.bluemarlin.drinkdiary.ui.record

import android.os.Looper
import com.bluemarlin.drinkdiary.domain.model.AppError
import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkTags
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import com.bluemarlin.drinkdiary.domain.repository.RecordPhotoStore
import com.bluemarlin.drinkdiary.domain.repository.StoredPhoto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RecordPhotoPickTest {
    private class FakeRepository(
        private val existing: DrinkRecord? = null,
    ) : DrinkRecordRepository {
        val saved = mutableListOf<DrinkRecord>()

        override fun observeRecords(type: DrinkType?): Flow<List<DrinkRecord>> = flowOf(saved)

        override fun observeRecord(id: Long): Flow<DrinkRecord?> = flowOf(existing)

        override fun observeSearchResults(query: String): Flow<List<DrinkRecord>> = flowOf(emptyList())

        override suspend fun save(record: DrinkRecord): AppResult<Long> {
            saved += record
            return AppResult.Success(1L)
        }

        override suspend fun deleteById(id: Long): AppResult<Unit> = AppResult.Success(Unit)
    }

    private class FakeStore(
        private val result: AppResult<String>,
    ) : RecordPhotoStore {
        override suspend fun import(sourceUri: String): AppResult<String> = result

        override suspend fun listStored(): List<StoredPhoto> = emptyList()

        override suspend fun delete(uri: String) = Unit
    }

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    private val picked = "content://media/picker/0/photo/1"
    private val copy = "file:///data/files/record_photos/a.img"

    @Test
    fun `a new record keeps the app-owned copy, not the picker address`() {
        val repository = FakeRepository()
        val viewModel = RecordViewModel(repository, FakeStore(AppResult.Success(copy)))
        viewModel.pickDrink(DrinkType.Wine, DrinkTags())
        viewModel.updateForm(RecordForm(name = "Barolo", rating = 4.0))

        viewModel.pickPhoto(picked)
        idle()
        viewModel.save()
        idle()

        assertEquals(copy, repository.saved.single().imageUri)
    }

    @Test
    fun `a failed copy leaves the photo empty and says so`() {
        val viewModel = RecordViewModel(FakeRepository(), FakeStore(AppResult.Failure(AppError.Storage)))

        viewModel.pickPhoto(picked)
        idle()

        assertNull(viewModel.uiState.value.form.imageUri)
        assertEquals(PHOTO_IMPORT_ERROR, viewModel.uiState.value.error)
    }

    @Test
    fun `an edit replaces the photo with the app-owned copy`() {
        val original =
            DrinkRecord(id = 3L, type = DrinkType.Whiskey, name = "Ardbeg", rating = 4.0, recordedAtMillis = 1L)
        val repository = FakeRepository(original)
        val viewModel = EditRecordViewModel(3L, repository, FakeStore(AppResult.Success(copy)))
        idle()

        viewModel.pickPhoto(picked)
        idle()
        viewModel.save()
        idle()

        assertEquals(copy, repository.saved.single().imageUri)
    }
}
