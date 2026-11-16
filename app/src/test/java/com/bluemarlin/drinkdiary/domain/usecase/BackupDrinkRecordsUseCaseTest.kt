package com.bluemarlin.drinkdiary.domain.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.bluemarlin.drinkdiary.data.local.DrinkDiaryDatabase
import com.bluemarlin.drinkdiary.data.local.OrgJsonBackupCodec
import com.bluemarlin.drinkdiary.data.repository.DrinkRecordRepositoryImpl
import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkTags
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.Origin
import com.bluemarlin.drinkdiary.domain.model.ServingStyle
import com.bluemarlin.drinkdiary.domain.model.TasteInput
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import com.bluemarlin.drinkdiary.domain.model.WineColor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BackupDrinkRecordsUseCaseTest {
    private lateinit var database: DrinkDiaryDatabase
    private lateinit var repository: DrinkRecordRepositoryImpl
    private lateinit var useCase: BackupDrinkRecordsUseCase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, DrinkDiaryDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        repository = DrinkRecordRepositoryImpl(database.drinkRecordDao())
        useCase = BackupDrinkRecordsUseCase(repository, OrgJsonBackupCodec { 9_999L })
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `exports all 6 traits and restores without duplicating existing records`() =
        runBlocking {
            val wine =
                DrinkRecord(
                    type = DrinkType.Wine,
                    name = "피오 체사레 바롤로",
                    vintage = 2019,
                    taste =
                        TasteInput()
                            .with(Trait.Sweetness, TraitAnswer.Low)
                            .with(Trait.Body, TraitAnswer.High)
                            .with(Trait.Intensity, TraitAnswer.High)
                            .with(Trait.Aftertaste, TraitAnswer.High)
                            .with(Trait.Acidity, TraitAnswer.High)
                            .with(Trait.Tannin, TraitAnswer.High),
                    tags = DrinkTags(wineColor = WineColor.Red, origin = Origin.OldWorld),
                    rating = 5.0,
                    collectionStatus = CollectionStatus.Repurchase,
                    price = 145_000L,
                    place = "보틀벙커",
                    memo = "묵직한 바롤로",
                    recordedAtMillis = 1_700_000_000_000L,
                )
            val whiskey =
                DrinkRecord(
                    type = DrinkType.Whiskey,
                    name = "라프로익 10",
                    servingStyle = ServingStyle.Neat,
                    taste =
                        TasteInput()
                            .with(Trait.Sweetness, TraitAnswer.Low)
                            .with(Trait.Body, TraitAnswer.High)
                            .with(Trait.Intensity, TraitAnswer.High)
                            .with(Trait.Aftertaste, TraitAnswer.High)
                            .with(Trait.Peat, TraitAnswer.High)
                            .with(Trait.AlcoholBurn, TraitAnswer.Mid),
                    rating = 4.5,
                    recordedAtMillis = 1_700_100_000_000L,
                )

            repository.save(wine)
            repository.save(whiskey)

            val exported = useCase.exportJson()
            assertTrue(exported is AppResult.Success)
            val json = (exported as AppResult.Success).value

            // 동일 DB에서 복원 시 기존 2건은 건너뜀.
            val dupRestore = useCase.restoreFromJson(json)
            assertTrue(dupRestore is AppResult.Success)
            assertEquals(0, (dupRestore as AppResult.Success).value.importedCount)
            assertEquals(2, dupRestore.value.skippedCount)

            // 새 DB에 복원 시 2건 전부 복구 및 6지표 보존.
            val freshDb =
                Room
                    .inMemoryDatabaseBuilder(
                        ApplicationProvider.getApplicationContext(),
                        DrinkDiaryDatabase::class.java,
                    ).allowMainThreadQueries()
                    .build()
            try {
                val freshRepo = DrinkRecordRepositoryImpl(freshDb.drinkRecordDao())
                val freshUseCase = BackupDrinkRecordsUseCase(freshRepo, OrgJsonBackupCodec())
                val restored = freshUseCase.restoreFromJson(json)
                assertTrue(restored is AppResult.Success)
                assertEquals(2, (restored as AppResult.Success).value.importedCount)
                assertEquals(0, restored.value.skippedCount)

                val savedList = freshRepo.observeRecords().first()
                assertEquals(2, savedList.size)
                val restoredWine = savedList.first { it.type == DrinkType.Wine }
                assertEquals(6, restoredWine.taste.answers.size)
                assertEquals(TraitAnswer.High, restoredWine.taste[Trait.Acidity])
                assertEquals(TraitAnswer.High, restoredWine.taste[Trait.Tannin])
                assertEquals(145_000L, restoredWine.price)
            } finally {
                freshDb.close()
            }
        }

    @Test
    fun `malformed json fails cleanly without modifying storage`() =
        runBlocking {
            val res = useCase.restoreFromJson("{ not valid json")
            assertTrue(res is AppResult.Failure)
        }
}
