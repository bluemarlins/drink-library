package com.bluemarlin.drinkdiary.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.bluemarlin.drinkdiary.domain.model.BottleEntry
import com.bluemarlin.drinkdiary.domain.model.BottleFacts
import com.bluemarlin.drinkdiary.domain.model.BottleRecommendation
import com.bluemarlin.drinkdiary.domain.model.BuyRecommendationBundle
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.ColorModePreference
import com.bluemarlin.drinkdiary.domain.model.DashboardOverview
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkTags
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.LogHighlights
import com.bluemarlin.drinkdiary.domain.model.MonthSummary
import com.bluemarlin.drinkdiary.domain.model.Origin
import com.bluemarlin.drinkdiary.domain.model.ProfileReadiness
import com.bluemarlin.drinkdiary.domain.model.RecommendationReason
import com.bluemarlin.drinkdiary.domain.model.SurfaceStyle
import com.bluemarlin.drinkdiary.domain.model.TasteInput
import com.bluemarlin.drinkdiary.domain.model.TastePreference
import com.bluemarlin.drinkdiary.domain.model.TasteProfile
import com.bluemarlin.drinkdiary.domain.model.TasteType
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import com.bluemarlin.drinkdiary.domain.model.TraitPreference
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.domain.model.WineColor
import com.bluemarlin.drinkdiary.domain.model.WineStyle
import com.bluemarlin.drinkdiary.ui.collection.CollectionFilterAction
import com.bluemarlin.drinkdiary.ui.collection.CollectionScreen
import com.bluemarlin.drinkdiary.ui.collection.CollectionUiState
import com.bluemarlin.drinkdiary.ui.collection.RecordDetailScreen
import com.bluemarlin.drinkdiary.ui.collection.applyFilterItem
import com.bluemarlin.drinkdiary.ui.collection.collectionFilterSections
import com.bluemarlin.drinkdiary.ui.collection.collectionFilterSummary
import com.bluemarlin.drinkdiary.ui.component.DDDrinkBadge
import com.bluemarlin.drinkdiary.ui.component.DDRepurchaseBadge
import com.bluemarlin.drinkdiary.ui.navigation.DDScreenScaffold
import com.bluemarlin.drinkdiary.ui.navigation.DDScreenType
import com.bluemarlin.drinkdiary.ui.navigation.DDTopLevelTab
import com.bluemarlin.drinkdiary.ui.profile.NextBottleCard
import com.bluemarlin.drinkdiary.ui.profile.ProfileScreen
import com.bluemarlin.drinkdiary.ui.profile.ProfileUiState
import com.bluemarlin.drinkdiary.ui.profile.tasteHighlights
import com.bluemarlin.drinkdiary.ui.record.DrinkPicker
import com.bluemarlin.drinkdiary.ui.record.ProbeSequenceScreen
import com.bluemarlin.drinkdiary.ui.record.RecordDetailStep
import com.bluemarlin.drinkdiary.ui.record.RecordForm
import com.bluemarlin.drinkdiary.ui.record.RecordSaveBar
import com.bluemarlin.drinkdiary.ui.settings.SettingsScreen
import com.bluemarlin.drinkdiary.ui.settings.SettingsUiState
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiaryTheme
import io.github.bluemarlins.aura.component.AdaptiveFloatingBarPhase
import io.github.bluemarlins.aura.component.AuraFloatingActionBarTestTag
import io.github.bluemarlins.aura.component.AuraSnackbarHost
import io.github.bluemarlins.aura.component.AuraText
import io.github.bluemarlins.aura.component.rememberAuraAdaptiveBarState
import io.github.bluemarlins.aura.component.rememberAuraSnackbarHostState
import io.github.bluemarlins.aura.foundation.glass.AuraGlassDefaults
import io.github.bluemarlins.aura.foundation.layout.AuraWindowInfo
import io.github.bluemarlins.aura.foundation.motion.AuraMotionDefaults
import io.github.bluemarlins.aura.foundation.motion.AuraMotionProfile
import io.github.bluemarlins.aura.foundation.theme.AuraMaterial
import io.github.bluemarlins.aura.foundation.theme.AuraTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSystemClock
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AuraConformanceTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val extraSmallWindow = AuraWindowInfo.fromDimensions(320, 320, 640)
    private val compactWindow = AuraWindowInfo.fromDimensions(390, 390, 844)
    private val squareWindow = AuraWindowInfo.fromDimensions(720, 720, 720)
    private val expandedWindow = AuraWindowInfo.fromDimensions(900, 1024, 768)

    private fun sampleRecord(id: Long = 1L) =
        DrinkRecord(
            id = id,
            type = DrinkType.Wine,
            name = "바롤로 리제르바",
            vintage = 2018,
            taste =
                TasteInput(
                    mapOf(
                        Trait.Sweetness to TraitAnswer.Low,
                        Trait.Body to TraitAnswer.High,
                        Trait.Intensity to TraitAnswer.High,
                        Trait.Aftertaste to TraitAnswer.High,
                    ),
                ),
            rating = 4.5,
            collectionStatus = CollectionStatus.Repurchase,
            price = 85000L,
            place = "동네 와인샵",
            memo = "탄닌이 단단하고 여운이 길다.",
            recordedAtMillis = 1_700_000_000_000L,
        )

    private fun sampleReadyProfile(): ProfileUiState {
        val type =
            TasteType(
                sweetness = TastePreference.Low,
                body = TastePreference.High,
                intensity = TastePreference.High,
                aftertaste = TastePreference.High,
            )
        val profile =
            TasteProfile(
                scope = TypeScope.Wine,
                recordCount = 6,
                preferences =
                    listOf(
                        TraitPreference(Trait.Sweetness, TastePreference.Low, 6, 0.8, 1),
                        TraitPreference(Trait.Body, TastePreference.High, 6, 0.75, 1),
                        TraitPreference(Trait.Intensity, TastePreference.High, 6, 0.7, 1),
                        TraitPreference(Trait.Aftertaste, TastePreference.High, 6, 0.65, 1),
                    ),
            )
        return ProfileUiState(
            loaded = true,
            scope = TypeScope.Wine,
            overview =
                DashboardOverview(
                    totalCount = 6,
                    wineCount = 4,
                    whiskeyCount = 2,
                    averageRating = 4.5,
                    repurchaseCount = 2,
                ),
            highlights =
                LogHighlights(
                    latest = sampleRecord(),
                    thisMonth =
                        MonthSummary(
                            month = 11,
                            count = 3,
                            wineCount = 2,
                            whiskeyCount = 1,
                            weeklyCounts = listOf(1, 2, 0, 0, 0),
                        ),
                ),
            profile = profile,
            readiness = ProfileReadiness.Ready(type),
            recommendations = BuyRecommendationBundle(hasEnoughData = true, repurchasePicks = listOf(sampleRecord())),
        )
    }

    @Test
    fun `DrinkDiaryTheme provides LiquidGlass ceiling, AuraGlass tokens, and Standard root motion track`() {
        var lightMaterial: AuraMaterial? = null
        var lightProfile: AuraMotionProfile? = null
        var lightFallbackAlpha = 0f
        var darkIsDark: Boolean? = null
        var darkLiquidAlpha = 0f

        composeRule.setContent {
            DrinkDiaryTheme(darkTheme = false) {
                lightMaterial = AuraTheme.material
                lightProfile = AuraTheme.motion.profile
                lightFallbackAlpha = AuraTheme.glass.solidFallbackAlpha
            }
            DrinkDiaryTheme(darkTheme = true) {
                darkIsDark = AuraTheme.colors.isDark
                darkLiquidAlpha = AuraTheme.glass.liquidTint.alpha
            }
        }

        assertEquals(AuraMaterial.LiquidGlass, lightMaterial)
        assertEquals(AuraMotionProfile.Standard, lightProfile)
        assertEquals(0.98f, lightFallbackAlpha, 0.001f)
        assertEquals(true, darkIsDark)
        assertEquals(AuraGlassDefaults.Dark.liquidTint.alpha, darkLiquidAlpha, 0.001f)
        assertTrue(darkLiquidAlpha >= 0.65f)
    }

    @Test
    fun `TopLevel scaffold uses Expressive island and falls back to Solid under reduceTransparency`() {
        var innerIslandProfile: AuraMotionProfile? = null

        composeRule.setContent {
            DrinkDiaryTheme(reduceTransparency = true) {
                DDScreenScaffold(
                    title = "요약",
                    screenType = DDScreenType.TopLevel,
                    selectedTab = DDTopLevelTab.Dashboard,
                    onDashboardClick = {},
                    onCollectionClick = {},
                    onSettingsClick = {},
                    onAddClick = {},
                ) {
                    innerIslandProfile = AuraTheme.motion.profile
                    AuraText("대시보드 본문")
                }
            }
        }

        // Content body stays on Standard while glass chrome degrades cleanly to Solid fallback tag.
        assertEquals(AuraMotionProfile.Standard, innerIslandProfile)
        composeRule.onAllNodesWithText("요약").onFirst().assertIsDisplayed()
        // 하단 탭은 요약 · 일지 · 설정이다. 매장 찾기는 없다(prd.md F5, 2026-11-19).
        composeRule.onNodeWithText("일지").assertIsDisplayed()
        composeRule.onNodeWithText("설정").assertIsDisplayed()
        composeRule.onNodeWithText("매장 찾기").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("기록하기").assertIsDisplayed()
        val fallbackMatcher =
            SemanticsMatcher.expectValue(
                SemanticsProperties.TestTag,
                "aura_glass_surface_solid_fallback",
            )
        assertTrue(
            composeRule
                .onAllNodes(fallbackMatcher)
                .fetchSemanticsNodes()
                .isNotEmpty(),
        )
    }

    @Test
    fun `Detail scaffold hosts FloatingCapsule top bar without bottom island and displays snackbar`() {
        lateinit var snackbarHostState: io.github.bluemarlins.aura.component.AuraSnackbarHostState
        lateinit var scope: CoroutineScope

        composeRule.setContent {
            DrinkDiaryTheme {
                snackbarHostState = rememberAuraSnackbarHostState()
                scope = rememberCoroutineScope()
                DDScreenScaffold(
                    title = "와인",
                    screenType = DDScreenType.Detail,
                    onBackClick = {},
                    snackbarHost = { AuraSnackbarHost(hostState = snackbarHostState) },
                ) {
                    AuraText("상세 본문")
                }
            }
        }

        composeRule.onNodeWithText("와인").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("뒤로").assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithText("기록하기").fetchSemanticsNodes().isEmpty())

        composeRule.runOnIdle {
            scope.launch {
                snackbarHostState.showSnackbar("저장되었습니다")
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("저장되었습니다").assertIsDisplayed()
    }

    @Test
    fun `ProfileScreen adapts cleanly across ExtraSmall, Compact, Square, and Expanded windows`() {
        val state = sampleReadyProfile()
        var currentWindow by mutableStateOf(extraSmallWindow)
        var supportsTwoPane: Boolean? = null

        composeRule.setContent {
            DrinkDiaryTheme(windowInfo = currentWindow) {
                supportsTwoPane = AuraTheme.layout.supportsTwoPane
                ProfileScreen(
                    state = state,
                    onScopeChange = {},
                    onOpenCollection = {},
                    onOpenRecord = {},
                    onShareCard = {},
                )
            }
        }

        listOf(extraSmallWindow, compactWindow, squareWindow, expandedWindow).forEach { window ->
            currentWindow = window
            composeRule.waitForIdle()
            // 요약은 벤토 카드다(branding.md 7-2 카드 이름). 커버 화면 2열, 펼친 화면 4열.
            // 앞 창 크기에서 아래로 스크롤해 둔 상태가 남으므로 카드마다 스크롤해서 확인한다.
            composeRule.onNodeWithText("이번 달 · 11월").performScrollTo().assertIsDisplayed()
            composeRule.onNodeWithText("최근에 마신 술", substring = true).performScrollTo().assertIsDisplayed()
            composeRule.onNodeWithText("누적").performScrollTo().assertIsDisplayed()
            composeRule
                .onNodeWithText((state.readiness as ProfileReadiness.Ready).type.code)
                .performScrollTo()
                .assertIsDisplayed()
            composeRule.onNodeWithText("맛 기준").performScrollTo().assertIsDisplayed()
            composeRule.onNodeWithText("다음 한 병").performScrollTo().assertIsDisplayed()
            val expectedTwoPane = window == squareWindow || window == expandedWindow
            assertEquals(expectedTwoPane, supportsTwoPane)
        }
    }

    // 맛 기준 카드는 뚜렷한 방향부터 보여준다. 중립은 문장 카드가 말한다.
    @Test
    fun `taste highlights keep the clearest directions and skip neutral`() {
        val picked =
            tasteHighlights(
                listOf(
                    TraitPreference(Trait.Sweetness, TastePreference.Low, 6, 0.5, 1),
                    TraitPreference(Trait.Body, TastePreference.Neutral, 6, 0.9, 1),
                    TraitPreference(Trait.Intensity, TastePreference.High, 6, 0.8, 1),
                    TraitPreference(Trait.Aftertaste, null, 2, 0.0, 0),
                ),
            )

        assertEquals(listOf(Trait.Intensity, Trait.Sweetness), picked.map { it.trait })
    }

    @Test
    fun `CollectionScreen renders without double-padding errors or legacy chip decorations`() {
        val records = listOf(sampleRecord(1L))
        composeRule.setContent {
            DrinkDiaryTheme(windowInfo = compactWindow) {
                CollectionScreen(
                    state = CollectionUiState(allRecords = records, records = records, loaded = true),
                    onQueryChange = {},
                    onOpen = {},
                    onRecordNew = {},
                    contentPadding = PaddingValues(),
                )
            }
        }

        composeRule.onNodeWithText("바롤로 리제르바").assertIsDisplayed()
        // 필터는 앱바 메뉴로 옮겼다. 목록에는 카드 배지의 "또 살래요"만 남는다.
        assertEquals(1, composeRule.onAllNodesWithText("또 살래요").fetchSemanticsNodes().size)
        composeRule.onNodeWithText("별점 높은 순").assertDoesNotExist()
    }

    // 일지에서 못 찾은 술은 찾던 이름 그대로 기록으로 넘어가야 한다(매장 찾기 화면이 하던 일).
    @Test
    fun `CollectionScreen offers to record a name it cannot find`() {
        var recorded: String? = null
        composeRule.setContent {
            DrinkDiaryTheme(windowInfo = compactWindow) {
                CollectionScreen(
                    state = CollectionUiState(query = " 처음 본 와인 ", loaded = true),
                    onQueryChange = {},
                    onOpen = {},
                    onRecordNew = { recorded = it },
                    contentPadding = PaddingValues(),
                )
            }
        }

        // 화면 언어는 한국어다 — 스크린리더가 읽는 설명도 마찬가지(branding.md 7-1 R1).
        composeRule.onNodeWithContentDescription("검색어 지우기").assertExists()
        composeRule.onNodeWithText("'처음 본 와인' 기록하기").performScrollTo().performClick()
        assertEquals("처음 본 와인", recorded)
    }

    @Test
    fun `journal filter menu changes the list from the app bar and the subtitle names the active filter`() {
        var state by mutableStateOf(CollectionUiState(loaded = true))
        composeRule.setContent {
            DrinkDiaryTheme(windowInfo = compactWindow) {
                Box {
                    CollectionFilterAction(
                        state = state,
                        onTypeChange = { state = state.copy(filter = it) },
                        onStatusChange = { state = state.copy(statusFilter = it) },
                        onSortByRatingChange = { state = state.copy(sortByRating = it) },
                    )
                }
            }
        }

        assertEquals(null, collectionFilterSummary(state))
        // Aura 아이콘 버튼은 300ms 연타를 막는다. 시계를 넘겨 첫 탭이 들어가게 한다.
        val openMenu: (String) -> Unit = { description ->
            ShadowSystemClock.advanceBy(Duration.ofMillis(500))
            composeRule.mainClock.advanceTimeBy(500L)
            composeRule.onNodeWithContentDescription(description).performClick()
            composeRule.waitForIdle()
        }
        openMenu("필터")
        composeRule.onNodeWithText("주종").assertIsDisplayed()
        composeRule.onNodeWithText("다시 살까").assertIsDisplayed()
        composeRule.onNodeWithText("와인").performClick()
        composeRule.waitForIdle()
        assertEquals(DrinkType.Wine, state.filter)

        // 걸린 필터는 아이콘 설명과 부제로 드러난다.
        composeRule.onNodeWithContentDescription("필터, 걸려 있음").assertExists()
        assertEquals("와인", collectionFilterSummary(state))
    }

    // 메뉴 항목 → 상태 변경과 부제 문구. 묶음마다 하나만 고르고, 기본값은 부제에서 빠진다.
    @Test
    fun `journal filter items map to state and the subtitle lists only non-default choices`() {
        var state = CollectionUiState(loaded = true)
        val pick: (String) -> Unit = { label ->
            val item = collectionFilterSections(state).flatMap { it.items }.single { it.label == label }
            applyFilterItem(
                item.id,
                onTypeChange = { state = state.copy(filter = it) },
                onStatusChange = { state = state.copy(statusFilter = it) },
                onSortByRatingChange = { state = state.copy(sortByRating = it) },
            )
        }

        assertEquals(listOf("주종", "다시 살까", "정렬"), collectionFilterSections(state).map { it.title })
        assertEquals(3, collectionFilterSections(state).sumOf { section -> section.items.count { it.selected } })

        pick("위스키")
        pick("별점 높은 순")
        pick("또 살래요")
        assertEquals("위스키 · 또 살래요 · 별점 높은 순", collectionFilterSummary(state))

        pick("최근 순")
        assertEquals("위스키 · 또 살래요", collectionFilterSummary(state))
        assertEquals(
            listOf("위스키", "또 살래요", "최근 순"),
            collectionFilterSections(state).flatMap { it.items }.filter { it.selected }.map { it.label },
        )
    }

    @Test
    fun `DDDrinkBadge and DDRepurchaseBadge render semantic labels cleanly`() {
        composeRule.setContent {
            DrinkDiaryTheme {
                DDDrinkBadge(drinkType = DrinkType.Wine)
                DDDrinkBadge(drinkType = DrinkType.Whiskey)
                DDRepurchaseBadge()
            }
        }

        composeRule.onNodeWithText("와인").assertIsDisplayed()
        composeRule.onNodeWithText("위스키").assertIsDisplayed()
        composeRule.onNodeWithText("또 살래요").assertIsDisplayed()
    }

    // 다음 한 병 카드는 추천 하나와 그 이유 한 줄만 말한다.
    @Test
    fun `NextBottleCard names one bottle with its reason`() {
        val reason = RecommendationReason.WineStyleMatch(WineStyle.FullRed, averageRating = 4.25)
        val bottles =
            listOf("바롤로", "리오하").map { name ->
                BottleRecommendation(
                    entry = BottleEntry(DrinkType.Wine, name, emptySet(), BottleFacts(wineStyle = WineStyle.FullRed)),
                    reason = reason,
                )
            }
        composeRule.setContent {
            DrinkDiaryTheme(windowInfo = compactWindow) {
                NextBottleCard(
                    bundle = BuyRecommendationBundle(hasEnoughData = true, discoveryBottles = bottles),
                    onOpenRecord = {},
                )
            }
        }

        composeRule.onNodeWithText("바롤로").assertIsDisplayed()
        composeRule.onNodeWithText("리오하").assertDoesNotExist()
        assertEquals(
            1,
            composeRule.onAllNodesWithText("같은 스타일에 준 내 평균 4.3점", substring = true).fetchSemanticsNodes().size,
        )
    }

    // 기록할 때 고른 색·산지 같은 태그는 상세 화면에서 다시 확인할 수 있어야 한다.
    @Test
    fun `RecordDetailScreen lists the label tags of the record`() {
        composeRule.setContent {
            DrinkDiaryTheme(windowInfo = compactWindow) {
                RecordDetailScreen(
                    record = sampleRecord().copy(tags = DrinkTags(wineColor = WineColor.Red, origin = Origin.OldWorld)),
                    onDelete = {},
                    contentPadding = PaddingValues(),
                )
            }
        }

        composeRule.onNodeWithText("색").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("레드").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("산지").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("구대륙").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `RecordDetailScreen uses Outline delete entry trigger and AuraDialog visible state transition`() {
        var deleted = false

        composeRule.setContent {
            DrinkDiaryTheme(windowInfo = compactWindow) {
                RecordDetailScreen(
                    record = sampleRecord(),
                    onDelete = { deleted = true },
                    contentPadding = PaddingValues(),
                )
            }
        }

        composeRule.onNodeWithText("이 기록 지우기").performScrollTo().performClick()
        composeRule.onNodeWithText("바롤로 리제르바 기록을 지울까요?").assertIsDisplayed()
        composeRule.onNodeWithText("그대로 두기").performClick()
        composeRule.waitForIdle()
        assertTrue(composeRule.onAllNodesWithText("바롤로 리제르바 기록을 지울까요?").fetchSemanticsNodes().isEmpty())

        ShadowSystemClock.advanceBy(Duration.ofMillis(500))
        composeRule.mainClock.advanceTimeBy(500L)
        composeRule.onNodeWithText("이 기록 지우기").performScrollTo().performClick()
        composeRule.onNodeWithText("지우기").performClick()
        assertTrue(deleted)
    }

    @Test
    fun `RecordDetailStep keeps the rating readout in the app's words and expands optional fields`() {
        var form by mutableStateOf(RecordForm())

        composeRule.setContent {
            DrinkDiaryTheme(
                motionProfile = AuraMotionProfile.Standard,
                motion = AuraMotionDefaults.Standard.copy(reduceMotion = true),
            ) {
                RecordDetailStep(
                    type = DrinkType.Wine,
                    form = form,
                    onFormChange = { form = it },
                    onPhotoPicked = {},
                )
            }
        }

        // 저장 버튼은 폼이 아니라 하단 독(RecordSaveBar)에 있다.
        composeRule.onNodeWithText("저장").assertDoesNotExist()
        // 고르기 전에는 점수를 보이지 않고, 용어집에 없는 "N / 5" 형식도 쓰지 않는다.
        composeRule.onNodeWithText("0점").assertDoesNotExist()
        composeRule.onNodeWithText("/ 5", substring = true).assertDoesNotExist()

        form = form.copy(name = "샤블리", rating = 3.5)
        composeRule.waitForIdle()
        composeRule.onNodeWithText("3.5점").assertIsDisplayed()
        composeRule.onNodeWithText("/ 5", substring = true).assertDoesNotExist()

        composeRule.onNodeWithText("더 남기기 (선택)").performScrollTo().performClick()
        composeRule.onNodeWithText("빈티지").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `RecordSaveBar explains why saving is blocked and shows Loading semantics while saving`() {
        var form by mutableStateOf(RecordForm())
        var saving by mutableStateOf(false)

        composeRule.setContent {
            DrinkDiaryTheme(motion = AuraMotionDefaults.Standard.copy(reduceMotion = true)) {
                Box(Modifier.fillMaxSize()) {
                    RecordSaveBar(form = form, saving = saving, onSave = {})
                }
            }
        }

        composeRule.onNodeWithText("저장").assertIsNotEnabled()
        composeRule.onNodeWithText(RecordForm().missingForSave!!).assertIsDisplayed()

        form = form.copy(name = "샤블리", rating = 3.5)
        composeRule.waitForIdle()
        composeRule.onNodeWithText("저장").assertIsEnabled()

        saving = true
        composeRule.waitForIdle()
        composeRule
            .onNodeWithText("저장 중")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "In progress"))
    }

    @Test
    fun `Editor scaffold floats its top bar and hosts the save dock`() {
        composeRule.setContent {
            DrinkDiaryTheme(windowInfo = compactWindow) {
                DDScreenScaffold(
                    title = "기록 고치기",
                    screenType = DDScreenType.Editor,
                    onBackClick = {},
                    bottomAction = {
                        RecordSaveBar(form = RecordForm(name = "샤블리", rating = 4.0), saving = false, onSave = {})
                    },
                ) { _ -> }
            }
        }

        composeRule.onAllNodesWithText("기록 고치기").onFirst().assertExists()
        composeRule.onNodeWithTag(AuraFloatingActionBarTestTag).assertIsDisplayed()
        composeRule.onNodeWithText("저장").assertIsEnabled()
    }

    @Test
    fun `ProfileScreen shows skeletons instead of an empty month before the first load`() {
        composeRule.setContent {
            DrinkDiaryTheme(windowInfo = compactWindow) {
                ProfileScreen(
                    state = ProfileUiState(),
                    onScopeChange = {},
                    onOpenCollection = {},
                    onOpenRecord = {},
                    onShareCard = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("요약을 불러오는 중").assertExists()
        composeRule.onNodeWithText("이번 달은 아직 기록이 없어요.").assertDoesNotExist()
    }

    @Test
    fun `DrinkPicker renders two-pane groups on Square foldable window`() {
        composeRule.setContent {
            DrinkDiaryTheme(windowInfo = squareWindow) {
                DrinkPicker(onPick = {})
            }
        }
        composeRule.onNodeWithText("무엇을 마셨나요?").assertIsDisplayed()
        composeRule.onNodeWithText("싱글몰트").assertIsDisplayed()
    }

    @Test
    fun `ProbeSequenceScreen renders step progress under reduceMotion`() {
        composeRule.setContent {
            DrinkDiaryTheme(
                windowInfo = compactWindow,
                motion = AuraMotionDefaults.Standard.copy(reduceMotion = true),
            ) {
                ProbeSequenceScreen(
                    type = DrinkType.Wine,
                    answers = TasteInput(),
                    currentIndex = 0,
                    onIndexChange = {},
                    onAnswer = { _, _ -> },
                    onComplete = {},
                    onBack = {},
                )
            }
        }
        composeRule.onNodeWithContentDescription("1 / 6").assertIsDisplayed()
    }

    @Test
    fun `SettingsScreen renders storage card on Expanded window`() {
        val overview =
            DashboardOverview(
                totalCount = 2,
                wineCount = 1,
                whiskeyCount = 1,
            )
        composeRule.setContent {
            DrinkDiaryTheme(windowInfo = expandedWindow) {
                SettingsScreen(
                    state = SettingsUiState(overview = overview),
                    onExportBackup = {},
                    onRestoreBackup = {},
                    onSurfaceStyleChange = {},
                    onColorModeChange = {},
                    contentPadding = PaddingValues(),
                )
            }
        }
        composeRule.onNodeWithText("내 데이터 보관함").assertIsDisplayed()
    }

    @Test
    fun `SettingsScreen offers surface material and color mode choices`() {
        var style: SurfaceStyle? = null
        var mode: ColorModePreference? = null
        composeRule.setContent {
            DrinkDiaryTheme(windowInfo = compactWindow) {
                SettingsScreen(
                    state = SettingsUiState(),
                    onExportBackup = {},
                    onRestoreBackup = {},
                    onSurfaceStyleChange = { style = it },
                    onColorModeChange = { mode = it },
                    contentPadding = PaddingValues(),
                )
            }
        }

        composeRule.onNodeWithText("글래스모피즘").performScrollTo().performClick()
        composeRule.onNodeWithText("다크").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals(SurfaceStyle.GlassMorphism, style)
        assertEquals(ColorModePreference.Dark, mode)
    }

    @Test
    fun `DDScreenScaffold TopLevel uses AdaptiveFloating bar transitions and focus pinning`() {
        lateinit var adaptiveState: io.github.bluemarlins.aura.component.AuraAdaptiveBarState

        composeRule.setContent {
            DrinkDiaryTheme(windowInfo = compactWindow) {
                adaptiveState = rememberAuraAdaptiveBarState()
                DDScreenScaffold(
                    title = "요약",
                    screenType = DDScreenType.TopLevel,
                    selectedTab = DDTopLevelTab.Dashboard,
                    onDashboardClick = {},
                    onCollectionClick = {},
                    onSettingsClick = {},
                    onAddClick = {},
                    adaptiveBarState = adaptiveState,
                ) {
                    AuraText("대시보드 본문")
                }
            }
        }

        assertEquals(AdaptiveFloatingBarPhase.DockedExpanded, adaptiveState.phase)

        composeRule.runOnIdle {
            adaptiveState.updateScrollBounds(scrollOffsetPx = 700f, maxScrollOffsetPx = 3000f)
        }
        composeRule.waitForIdle()
        assertEquals(AdaptiveFloatingBarPhase.FloatingVisible, adaptiveState.phase)

        composeRule.runOnIdle {
            adaptiveState.updatePinnedFlags(focused = true)
        }
        composeRule.waitForIdle()
        assertEquals(AdaptiveFloatingBarPhase.FloatingPinned, adaptiveState.phase)
    }

    @Test
    fun `Solid theme material ceiling caps nested TopLevel chrome to Solid fallback`() {
        var resolvedInsideTheme: AuraMaterial? = null

        composeRule.setContent {
            DrinkDiaryTheme(material = AuraMaterial.Solid, reduceTransparency = false) {
                resolvedInsideTheme = AuraTheme.effectiveMaterial(AuraMaterial.LiquidGlass)
                DDScreenScaffold(
                    title = "요약",
                    screenType = DDScreenType.TopLevel,
                    selectedTab = DDTopLevelTab.Dashboard,
                    onDashboardClick = {},
                    onCollectionClick = {},
                    onSettingsClick = {},
                    onAddClick = {},
                ) {
                    AuraText("대시보드 본문")
                }
            }
        }

        assertEquals(AuraMaterial.Solid, resolvedInsideTheme)
        val fallbackMatcher =
            SemanticsMatcher.expectValue(
                SemanticsProperties.TestTag,
                "aura_glass_surface_solid_fallback",
            )
        assertTrue(
            composeRule
                .onAllNodes(fallbackMatcher, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty(),
        )
    }
}
