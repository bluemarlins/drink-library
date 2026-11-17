package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.BottleEntry
import com.bluemarlin.drinkdiary.domain.model.BottleFacts
import com.bluemarlin.drinkdiary.domain.model.CaskGroup
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkTags
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.RecommendationReason
import com.bluemarlin.drinkdiary.domain.model.TasteInput
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.domain.model.WineColor
import com.bluemarlin.drinkdiary.domain.model.WineStyle
import com.bluemarlin.drinkdiary.domain.repository.BottleMatcher
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveBuyRecommendationsUseCaseTest {
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

    private val catalog =
        listOf(
            BottleEntry(
                type = DrinkType.Whiskey,
                displayName = "맥캘란 12 셰리오크",
                aliases = setOf("맥캘란 12 셰리오크", "Macallan 12 Sherry Oak"),
                facts = BottleFacts(cask = CaskGroup.Sherry),
            ),
            BottleEntry(
                type = DrinkType.Whiskey,
                displayName = "글렌드로낙 12",
                aliases = setOf("글렌드로낙 12", "Glendronach 12"),
                facts = BottleFacts(cask = CaskGroup.Sherry),
            ),
            BottleEntry(
                type = DrinkType.Whiskey,
                displayName = "글렌리벳 12",
                aliases = setOf("글렌리벳 12", "Glenlivet 12"),
                facts = BottleFacts(cask = CaskGroup.Bourbon),
            ),
            BottleEntry(
                type = DrinkType.Wine,
                displayName = "피오 체사레 바롤로",
                aliases = setOf("피오 체사레 바롤로", "Pio Cesare Barolo"),
                facts = BottleFacts(wineStyle = WineStyle.FullRed),
            ),
            BottleEntry(
                type = DrinkType.Wine,
                displayName = "클라우디 베이 소비뇽 블랑",
                aliases = setOf("클라우디 베이 소비뇽 블랑"),
                facts = BottleFacts(wineStyle = WineStyle.DryWhite),
            ),
        )

    private fun useCase(records: List<DrinkRecord>): ObserveBuyRecommendationsUseCase {
        val repo = FakeRepository(records)
        val dict = BottleMatcher { catalog }
        val tasteUseCase = ObserveTasteProfileUseCase(repo)
        val tagUseCase = ObserveTagPreferenceUseCase(repo, dict)
        return ObserveBuyRecommendationsUseCase(
            repository = repo,
            dictionary = dict,
            observeTasteProfile = tasteUseCase,
            observeTagPreference = tagUseCase,
            resolveReadiness = ResolveProfileReadinessUseCase(),
        )
    }

    private fun taste(
        sweet: TraitAnswer,
        body: TraitAnswer,
        intensity: TraitAnswer,
        after: TraitAnswer,
    ) = TasteInput()
        .with(Trait.Sweetness, sweet)
        .with(Trait.Body, body)
        .with(Trait.Intensity, intensity)
        .with(Trait.Aftertaste, after)

    @Test
    fun `below minimum samples holds style recommendations but surfaces repurchase picks`() =
        runBlocking {
            val records =
                listOf(
                    DrinkRecord(
                        id = 1,
                        type = DrinkType.Wine,
                        name = "피오 체사레 바롤로",
                        taste = taste(TraitAnswer.Low, TraitAnswer.High, TraitAnswer.High, TraitAnswer.High),
                        rating = 5.0,
                        collectionStatus = CollectionStatus.Repurchase,
                        recordedAtMillis = 100L,
                    ),
                )

            val bundle = useCase(records).invoke(TypeScope.Wine).first()

            assertFalse(bundle.hasEnoughData)
            assertEquals(TasteThresholds.MIN_SAMPLES - 1, bundle.recordsNeeded)
            assertTrue(bundle.discoveryBottles.isEmpty())
            assertEquals(listOf("피오 체사레 바롤로"), bundle.repurchasePicks.map { it.name })
        }

    @Test
    fun `recommends un-tried sherry whiskey matching high cask preference`() =
        runBlocking {
            // 맥캘란 12 셰리오크(Sherry)만 마셔 높은 점수를 주었고, 글렌드로낙 12(Sherry)는 미음용.
            val records =
                listOf(
                    DrinkRecord(
                        id = 1,
                        type = DrinkType.Whiskey,
                        name = "맥캘란 12 셰리오크",
                        taste = taste(TraitAnswer.High, TraitAnswer.High, TraitAnswer.High, TraitAnswer.High),
                        rating = 5.0,
                        collectionStatus = CollectionStatus.Repurchase,
                        recordedAtMillis = 1L,
                    ),
                    DrinkRecord(
                        id = 2,
                        type = DrinkType.Whiskey,
                        name = "맥캘란 12 셰리오크 2",
                        taste = taste(TraitAnswer.High, TraitAnswer.High, TraitAnswer.High, TraitAnswer.High),
                        rating = 4.5,
                        recordedAtMillis = 2L,
                    ),
                    DrinkRecord(
                        id = 3,
                        type = DrinkType.Whiskey,
                        name = "맥캘란 12 셰리오크 3",
                        taste = taste(TraitAnswer.Mid, TraitAnswer.High, TraitAnswer.High, TraitAnswer.High),
                        rating = 4.5,
                        recordedAtMillis = 3L,
                    ),
                    DrinkRecord(
                        id = 4,
                        type = DrinkType.Whiskey,
                        name = "글렌리벳 12",
                        taste = taste(TraitAnswer.Low, TraitAnswer.Low, TraitAnswer.Low, TraitAnswer.Low),
                        rating = 2.0,
                        recordedAtMillis = 4L,
                    ),
                    DrinkRecord(
                        id = 5,
                        type = DrinkType.Whiskey,
                        name = "글렌리벳 12 2",
                        taste = taste(TraitAnswer.Low, TraitAnswer.Low, TraitAnswer.Low, TraitAnswer.Low),
                        rating = 2.5,
                        recordedAtMillis = 5L,
                    ),
                    DrinkRecord(
                        id = 6,
                        type = DrinkType.Whiskey,
                        name = "글렌리벳 12 3",
                        taste = taste(TraitAnswer.Low, TraitAnswer.Low, TraitAnswer.Low, TraitAnswer.Low),
                        rating = 2.0,
                        recordedAtMillis = 6L,
                    ),
                )

            val bundle = useCase(records).invoke(TypeScope.Whiskey).first()

            assertTrue(bundle.hasEnoughData)
            assertEquals(1, bundle.discoveryBottles.size)
            val pick = bundle.discoveryBottles.first()
            assertEquals("글렌드로낙 12", pick.entry.displayName)
            assertTrue(pick.reason is RecommendationReason.CaskMatch)
        }

    // 캐스크별 평점 차이가 없을 때 단맛 선호로 셰리를 고른 것은 "고루 즐기는 성향" 추천이 아니다.
    @Test
    fun `sherry picked from a sweet preference says so in its reason`() =
        runBlocking {
            val records =
                (1..6).map { index ->
                    val liked = index <= 3
                    val answer = if (liked) TraitAnswer.High else TraitAnswer.Low
                    DrinkRecord(
                        id = index.toLong(),
                        type = DrinkType.Whiskey,
                        // 사전에 없는 이름이라 캐스크 선호는 성립하지 않는다.
                        name = "처음 보는 위스키 $index",
                        taste = taste(answer, answer, answer, answer),
                        rating = if (liked) 5.0 else 2.0,
                        recordedAtMillis = index.toLong(),
                    )
                }

            val bundle = useCase(records).invoke(TypeScope.Whiskey).first()

            assertEquals(
                listOf("맥캘란 12 셰리오크", "글렌드로낙 12"),
                bundle.discoveryBottles.map { it.entry.displayName },
            )
            assertTrue(
                bundle.discoveryBottles.all {
                    it.reason == RecommendationReason.TasteAlignedCask(CaskGroup.Sherry)
                },
            )
        }

    @Test
    fun `wine recommendation aligns with full body and excludes dry white`() =
        runBlocking {
            val records =
                listOf(
                    DrinkRecord(
                        id = 1,
                        type = DrinkType.Wine,
                        name = "아무 레드 1",
                        taste = taste(TraitAnswer.Low, TraitAnswer.High, TraitAnswer.High, TraitAnswer.High),
                        tags = DrinkTags(wineColor = WineColor.Red),
                        rating = 5.0,
                        recordedAtMillis = 1L,
                    ),
                    DrinkRecord(
                        id = 2,
                        type = DrinkType.Wine,
                        name = "아무 레드 2",
                        taste = taste(TraitAnswer.Low, TraitAnswer.High, TraitAnswer.High, TraitAnswer.High),
                        tags = DrinkTags(wineColor = WineColor.Red),
                        rating = 4.5,
                        recordedAtMillis = 2L,
                    ),
                    DrinkRecord(
                        id = 3,
                        type = DrinkType.Wine,
                        name = "아무 레드 3",
                        taste = taste(TraitAnswer.Low, TraitAnswer.High, TraitAnswer.High, TraitAnswer.High),
                        tags = DrinkTags(wineColor = WineColor.Red),
                        rating = 4.5,
                        recordedAtMillis = 3L,
                    ),
                    DrinkRecord(
                        id = 4,
                        type = DrinkType.Wine,
                        name = "아무 화이트 1",
                        taste = taste(TraitAnswer.Low, TraitAnswer.Low, TraitAnswer.Low, TraitAnswer.Low),
                        tags = DrinkTags(wineColor = WineColor.White),
                        rating = 2.0,
                        recordedAtMillis = 4L,
                    ),
                    DrinkRecord(
                        id = 5,
                        type = DrinkType.Wine,
                        name = "아무 화이트 2",
                        taste = taste(TraitAnswer.Low, TraitAnswer.Low, TraitAnswer.Low, TraitAnswer.Low),
                        tags = DrinkTags(wineColor = WineColor.White),
                        rating = 2.0,
                        recordedAtMillis = 5L,
                    ),
                    DrinkRecord(
                        id = 6,
                        type = DrinkType.Wine,
                        name = "아무 화이트 3",
                        taste = taste(TraitAnswer.Low, TraitAnswer.Low, TraitAnswer.Low, TraitAnswer.Low),
                        tags = DrinkTags(wineColor = WineColor.White),
                        rating = 2.5,
                        recordedAtMillis = 6L,
                    ),
                )

            val bundle = useCase(records).invoke(TypeScope.Wine).first()

            assertTrue(bundle.hasEnoughData)
            assertEquals(listOf("피오 체사레 바롤로"), bundle.discoveryBottles.map { it.entry.displayName })
        }
}
