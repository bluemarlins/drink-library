package com.bluemarlin.drinkdiary.data

import android.content.Context
import android.graphics.BitmapFactory
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.bluemarlin.drinkdiary.data.local.AssetBottleDictionary
import com.bluemarlin.drinkdiary.data.local.DrinkDiaryDatabase
import com.bluemarlin.drinkdiary.data.local.LocalSeedLoader
import com.bluemarlin.drinkdiary.data.repository.DrinkRecordRepositoryImpl
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.ProfileReadiness
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.domain.repository.BottleMatcher
import com.bluemarlin.drinkdiary.domain.usecase.ObserveTagPreferenceUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveTasteProfileUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ResolveProfileReadinessUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LocalSeedLoaderTest {
    private lateinit var context: Context
    private lateinit var database: DrinkDiaryDatabase
    private lateinit var repository: DrinkRecordRepositoryImpl
    private val markerName = ".test_local_seed_marker"

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        File(context.filesDir, markerName).delete()
        database =
            Room
                .inMemoryDatabaseBuilder(context, DrinkDiaryDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        repository = DrinkRecordRepositoryImpl(database.drinkRecordDao())
    }

    @After
    fun tearDown() {
        File(context.filesDir, markerName).delete()
        database.close()
    }

    @Test
    fun `parses valid in-memory seed json and skips malformed rows`() {
        val loader = LocalSeedLoader(context, repository, markerFileName = markerName)
        val json =
            """
            [
              {
                "type": "Wine",
                "name": "피오 체사레 바롤로",
                "vintage": 2019,
                "rating": 5.0,
                "collectionStatus": "Repurchase",
                "imageUri": "file:///android_asset/local_seed/images/barolo.jpg",
                "price": 145000,
                "place": "와인샵",
                "memo": "체리와 말린 장미 향, 묵직한 탄닌",
                "recordedAtMillis": 1700000000000,
                "taste": {
                  "Sweetness": "Low",
                  "Body": "High",
                  "Intensity": "High",
                  "Aftertaste": "High",
                  "Acidity": "High",
                  "Tannin": "High"
                },
                "tags": {
                  "WineColor": "Red",
                  "AbvBand": "Mid",
                  "Origin": "OldWorld"
                }
              },
              {
                "type": "Unknown",
                "name": "무효"
              }
            ]
            """.trimIndent()

        val parsed = loader.parseJson(json)
        assertEquals(1, parsed.size)
        val rec = parsed.first()
        assertEquals(DrinkType.Wine, rec.type)
        assertEquals("피오 체사레 바롤로", rec.name)
        assertEquals(2019, rec.vintage)
        assertEquals(5.0, rec.rating, 0.0)
        assertEquals(CollectionStatus.Repurchase, rec.collectionStatus)
        assertEquals(6, rec.taste.answers.size)
        assertEquals("OldWorld", rec.tags[com.bluemarlin.drinkdiary.domain.model.TagCategory.Origin])
    }

    @Test
    fun `missing seed asset is a safe no-op`() =
        runBlocking {
            val loader =
                LocalSeedLoader(
                    context = context,
                    repository = repository,
                    assetPath = "local_seed/does_not_exist.json",
                    markerFileName = markerName,
                )

            loader.seedIfNeeded()

            assertTrue(repository.observeRecords().first().isEmpty())
            assertTrue(!File(context.filesDir, markerName).exists())
        }

    @Test
    fun `seedIfNeeded is idempotent and does not duplicate existing records`() =
        runBlocking {
            val hasAsset = runCatching { context.assets.open("local_seed/seed_records.json").close() }.isSuccess
            if (!hasAsset) return@runBlocking

            // 사전 기록 1건을 먼저 저장해도 동일 이름은 중복 삽입하지 않고 나머지만 채운다.
            repository.save(
                DrinkRecord(
                    type = DrinkType.Whiskey,
                    name = "글렌피딕 12",
                    rating = 4.0,
                    recordedAtMillis = 12345L,
                ),
            )

            val loader = LocalSeedLoader(context, repository, markerFileName = markerName)
            loader.seedIfNeeded()
            val afterFirst = repository.observeRecords().first()
            assertEquals(50, afterFirst.size)
            assertTrue(File(context.filesDir, markerName).exists())

            loader.seedIfNeeded()
            val afterSecond = repository.observeRecords().first()
            assertEquals(50, afterSecond.size)
        }

    @Test
    fun `local seed dataset has 50 complete items with valid images, dictionary matches, and ready taste profiles`() =
        runBlocking {
            val hasAsset = runCatching { context.assets.open("local_seed/seed_records.json").close() }.isSuccess
            if (!hasAsset) return@runBlocking

            val loader = LocalSeedLoader(context, repository, markerFileName = markerName)
            val records = loader.parse()

            assertEquals(50, records.size)
            val wines = records.filter { it.type == DrinkType.Wine }
            val whiskies = records.filter { it.type == DrinkType.Whiskey }
            assertEquals(25, wines.size)
            assertEquals(25, whiskies.size)
            assertEquals(50, records.map { it.name }.toSet().size)

            // 와인 <= $150 (원화 환산 상한 기준 여유 포함), 위스키 <= $400 검증 및 필수 필드 확인
            val dictionary = BottleMatcher { AssetBottleDictionary(context).entries }
            records.forEach { rec ->
                assertTrue("${rec.name}: 테이스팅 노트(memo)가 비어 있음", !rec.memo.isNullOrBlank())
                assertTrue("${rec.name}: 이미지 URI 누락", !rec.imageUri.isNullOrBlank())
                assertTrue("${rec.name}: 가격 누락", (rec.price ?: 0L) > 0L)
                assertTrue("${rec.name}: 6개 감각 축 미충족", rec.taste.answers.size == Trait.of(rec.type).size)
                assertTrue("${rec.name}: 태그 누락", !rec.tags.isEmpty)

                val price = rec.price!!
                if (rec.type == DrinkType.Wine) {
                    assertTrue("${rec.name}: 와인 가격 상한 초과 ($price)", price <= 230_000L)
                } else {
                    assertTrue("${rec.name}: 위스키 가격 상한 초과 ($price)", price <= 600_000L)
                }

                // 모든 이름이 bottles.json 사전에 매칭되어 캐스크/와인스타일 사실을 반환해야 함
                val facts = dictionary.lookup(rec.type, rec.name)
                assertNotNull("${rec.name}: bottles.json 사전 매칭 실패", facts)

                // 이미지 asset 파일이 열리고 디코딩 가능한 실제 이미지인지 확인
                val assetPath = rec.imageUri!!.removePrefix("file:///android_asset/")
                val bitmap =
                    context.assets.open(assetPath).use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                assertNotNull("${rec.name}: 이미지 디코딩 실패 ($assetPath)", bitmap)
                assertTrue(
                    "${rec.name}: 이미지 크기 비정상 (${bitmap.width}x${bitmap.height})",
                    bitmap.width > 0 && bitmap.height > 0,
                )
            }

            // 적재 후 Wine, Whiskey, Combined 모두 ProfileReadiness.Ready 및 태그 선호 요약 확인
            loader.seedIfNeeded()
            val observeTasteProfile = ObserveTasteProfileUseCase(repository)
            val resolveReadiness = ResolveProfileReadinessUseCase()
            val observeTagPreference = ObserveTagPreferenceUseCase(repository, dictionary)

            for (scope in TypeScope.entries) {
                val profile = observeTasteProfile(scope).first()
                val readiness = resolveReadiness(profile)
                assertTrue("$scope 취향 유형이 Ready가 아님: $readiness", readiness is ProfileReadiness.Ready)
                val tagPrefs = observeTagPreference(scope).first()
                assertTrue("$scope 태그 선호 결과 비어 있음", tagPrefs.isNotEmpty())
            }
        }
}
