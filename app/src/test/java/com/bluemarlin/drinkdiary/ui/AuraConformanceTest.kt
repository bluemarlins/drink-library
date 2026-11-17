package com.bluemarlin.drinkdiary.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.bluemarlin.drinkdiary.domain.model.BottleEntry
import com.bluemarlin.drinkdiary.domain.model.BottleFacts
import com.bluemarlin.drinkdiary.domain.model.BottleRecommendation
import com.bluemarlin.drinkdiary.domain.model.BuyRecommendationBundle
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DashboardOverview
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkTags
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.Origin
import com.bluemarlin.drinkdiary.domain.model.ProfileReadiness
import com.bluemarlin.drinkdiary.domain.model.RecommendationReason
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
import com.bluemarlin.drinkdiary.ui.collection.CollectionScreen
import com.bluemarlin.drinkdiary.ui.collection.CollectionUiState
import com.bluemarlin.drinkdiary.ui.collection.RecordDetailScreen
import com.bluemarlin.drinkdiary.ui.component.DDBuyRecommendationSection
import com.bluemarlin.drinkdiary.ui.component.DDDrinkBadge
import com.bluemarlin.drinkdiary.ui.component.DDRepurchaseBadge
import com.bluemarlin.drinkdiary.ui.lookup.LookupScreen
import com.bluemarlin.drinkdiary.ui.lookup.LookupUiState
import com.bluemarlin.drinkdiary.ui.navigation.DDScreenScaffold
import com.bluemarlin.drinkdiary.ui.navigation.DDScreenType
import com.bluemarlin.drinkdiary.ui.navigation.DDTopLevelTab
import com.bluemarlin.drinkdiary.ui.profile.ProfileScreen
import com.bluemarlin.drinkdiary.ui.profile.ProfileUiState
import com.bluemarlin.drinkdiary.ui.record.DrinkPicker
import com.bluemarlin.drinkdiary.ui.record.ProbeSequenceScreen
import com.bluemarlin.drinkdiary.ui.record.RecordDetailStep
import com.bluemarlin.drinkdiary.ui.record.RecordForm
import com.bluemarlin.drinkdiary.ui.settings.SettingsScreen
import com.bluemarlin.drinkdiary.ui.settings.SettingsUiState
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiaryTheme
import com.example.ds.component.AdaptiveFloatingBarPhase
import com.example.ds.component.AuraSnackbarHost
import com.example.ds.component.AuraText
import com.example.ds.component.rememberAuraAdaptiveBarState
import com.example.ds.component.rememberAuraSnackbarHostState
import com.example.ds.foundation.glass.AuraGlassDefaults
import com.example.ds.foundation.layout.AuraWindowInfo
import com.example.ds.foundation.motion.AuraMotionDefaults
import com.example.ds.foundation.motion.AuraMotionProfile
import com.example.ds.foundation.theme.AuraMaterial
import com.example.ds.foundation.theme.AuraTheme
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
            scope = TypeScope.Wine,
            overview =
                DashboardOverview(
                    totalCount = 6,
                    wineCount = 4,
                    whiskeyCount = 2,
                    averageRating = 4.5,
                    repurchaseCount = 2,
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
                    title = "테이스트 아카이브",
                    screenType = DDScreenType.TopLevel,
                    selectedTab = DDTopLevelTab.Dashboard,
                    onDashboardClick = {},
                    onCollectionClick = {},
                    onSearchClick = {},
                    onAddClick = {},
                ) {
                    innerIslandProfile = AuraTheme.motion.profile
                    AuraText("대시보드 본문")
                }
            }
        }

        // Content body stays on Standard while glass chrome degrades cleanly to Solid fallback tag.
        assertEquals(AuraMotionProfile.Standard, innerIslandProfile)
        composeRule.onNodeWithText("테이스트 아카이브").assertIsDisplayed()
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
        lateinit var snackbarHostState: com.example.ds.component.AuraSnackbarHostState
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
            composeRule.onNodeWithText("내가 마신 아카이브").assertIsDisplayed()
            composeRule.onNodeWithText((state.readiness as ProfileReadiness.Ready).type.code).assertIsDisplayed()
            val expectedTwoPane = window == squareWindow || window == expandedWindow
            assertEquals(expectedTwoPane, supportsTwoPane)
        }
    }

    @Test
    fun `CollectionScreen renders without double-padding errors or legacy chip decorations`() {
        val records = listOf(sampleRecord(1L))
        composeRule.setContent {
            DrinkDiaryTheme(windowInfo = compactWindow) {
                CollectionScreen(
                    state = CollectionUiState(allRecords = records, records = records, loaded = true),
                    onFilterChange = {},
                    onStatusFilterChange = {},
                    onQueryChange = {},
                    onToggleSortByRating = {},
                    onOpen = {},
                    contentPadding = PaddingValues(),
                )
            }
        }

        composeRule.onNodeWithText("바롤로 리제르바").assertIsDisplayed()
        // 필터 칩과 카드 배지가 같은 이름을 쓴다(branding.md 7-2 — 한 개념에 한 이름).
        assertEquals(2, composeRule.onAllNodesWithText("또 살래요").fetchSemanticsNodes().size)
    }

    @Test
    fun `LookupScreen renders semantic status badge cleanly`() {
        val records = listOf(sampleRecord(1L))
        composeRule.setContent {
            DrinkDiaryTheme(windowInfo = compactWindow) {
                LookupScreen(
                    state = LookupUiState(repurchaseCandidates = records),
                    onQueryChange = {},
                    onOpenRecord = {},
                    onRecordNew = {},
                    contentPadding = PaddingValues(),
                )
            }
        }
        composeRule.onNodeWithText("매장에서 바로 찾기").assertIsDisplayed()
        composeRule.onNodeWithText("또 살래요 · 4.5점").assertIsDisplayed()
    }

    // 매장에서 처음 본 술은 찾던 이름 그대로 기록으로 넘어가야 한다.
    @Test
    fun `LookupScreen offers to record a name it cannot find`() {
        var recorded: String? = null
        composeRule.setContent {
            DrinkDiaryTheme(windowInfo = compactWindow) {
                LookupScreen(
                    state = LookupUiState(query = " 처음 본 와인 "),
                    onQueryChange = {},
                    onOpenRecord = {},
                    onRecordNew = { recorded = it },
                    contentPadding = PaddingValues(),
                )
            }
        }

        composeRule.onNodeWithText("매장에서 바로 찾기").assertDoesNotExist()
        composeRule.onNodeWithText("'처음 본 와인' 기록하기").performScrollTo().performClick()
        assertEquals("처음 본 와인", recorded)
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

    // 같은 이유를 카드마다 되풀이하지 않는다. 이유가 하나뿐이면 섹션에 한 번만 쓴다.
    @Test
    fun `DDBuyRecommendationSection states a shared reason once`() {
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
                DDBuyRecommendationSection(
                    scope = TypeScope.Wine,
                    bundle = BuyRecommendationBundle(hasEnoughData = true, discoveryBottles = bottles),
                    onOpenRecord = {},
                )
            }
        }

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
                    onEdit = {},
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
                    onEdit = {},
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
    fun `RecordDetailStep preserves Loading semantics and reduceMotion snaps transitions`() {
        var form by mutableStateOf(RecordForm())
        var saving by mutableStateOf(false)

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
                    onSave = {},
                    saving = saving,
                )
            }
        }

        composeRule.onNodeWithText("저장").performScrollTo().assertIsNotEnabled()

        form = form.copy(name = "샤블리", rating = 4.0)
        composeRule.waitForIdle()
        composeRule.onNodeWithText("저장").performScrollTo().assertIsEnabled()

        composeRule.onNodeWithText("더 남기기 (선택)").performScrollTo().performClick()
        composeRule.onNodeWithText("빈티지").performScrollTo().assertIsDisplayed()

        saving = true
        composeRule.waitForIdle()
        composeRule
            .onNodeWithText("저장 중")
            .performScrollTo()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "In progress"))
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
                    contentPadding = PaddingValues(),
                )
            }
        }
        composeRule.onNodeWithText("내 데이터 보관함").assertIsDisplayed()
    }

    @Test
    fun `DDScreenScaffold TopLevel uses AdaptiveFloating bar transitions and focus pinning`() {
        lateinit var adaptiveState: com.example.ds.component.AuraAdaptiveBarState

        composeRule.setContent {
            DrinkDiaryTheme(windowInfo = compactWindow) {
                adaptiveState = rememberAuraAdaptiveBarState()
                DDScreenScaffold(
                    title = "테이스트 아카이브",
                    screenType = DDScreenType.TopLevel,
                    selectedTab = DDTopLevelTab.Dashboard,
                    onDashboardClick = {},
                    onCollectionClick = {},
                    onSearchClick = {},
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
                    title = "테이스트 아카이브",
                    screenType = DDScreenType.TopLevel,
                    selectedTab = DDTopLevelTab.Dashboard,
                    onDashboardClick = {},
                    onCollectionClick = {},
                    onSearchClick = {},
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
