package com.bluemarlin.drinkdiary.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.toSize
import com.bluemarlin.drinkdiary.domain.model.BuyRecommendationBundle
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DashboardOverview
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkTags
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.LogHighlights
import com.bluemarlin.drinkdiary.domain.model.MonthSummary
import com.bluemarlin.drinkdiary.domain.model.Origin
import com.bluemarlin.drinkdiary.domain.model.ProfileReadiness
import com.bluemarlin.drinkdiary.domain.model.TasteInput
import com.bluemarlin.drinkdiary.domain.model.TastePreference
import com.bluemarlin.drinkdiary.domain.model.TasteProfile
import com.bluemarlin.drinkdiary.domain.model.TasteType
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import com.bluemarlin.drinkdiary.domain.model.TraitPreference
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.domain.model.WineColor
import com.bluemarlin.drinkdiary.ui.collection.CollectionScreen
import com.bluemarlin.drinkdiary.ui.collection.CollectionUiState
import com.bluemarlin.drinkdiary.ui.collection.RecordDetailScreen
import com.bluemarlin.drinkdiary.ui.navigation.DDScreenScaffold
import com.bluemarlin.drinkdiary.ui.navigation.DDScreenType
import com.bluemarlin.drinkdiary.ui.navigation.DDTopLevelTab
import com.bluemarlin.drinkdiary.ui.profile.ProfileScreen
import com.bluemarlin.drinkdiary.ui.profile.ProfileUiState
import com.bluemarlin.drinkdiary.ui.record.DrinkPicker
import com.bluemarlin.drinkdiary.ui.record.EditRecordScreen
import com.bluemarlin.drinkdiary.ui.record.EditUiState
import com.bluemarlin.drinkdiary.ui.record.RecordDetailStep
import com.bluemarlin.drinkdiary.ui.record.RecordForm
import com.bluemarlin.drinkdiary.ui.settings.SettingsScreen
import com.bluemarlin.drinkdiary.ui.settings.SettingsUiState
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiaryTheme
import io.github.bluemarlins.aura.component.AuraText
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.max
import kotlin.math.min

/**
 * Axis 3 of the Aura usage scorecard (`app/docs/departments/designer/aura-usage-scorecard.md`):
 * renders each main user flow in every Aura window profile and checks what a screen can break
 * even when every component is used correctly —
 *
 * - every clickable target is at least 48dp on both sides (Aura `EVAL-005`);
 * - no text is clipped vertically (`didOverflowHeight`); ellipsis on one line is allowed;
 * - no two clickable targets overlap unless one contains the other.
 *
 * Native graphics give real text metrics; legacy graphics inflate text heights.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AuraRenderContractTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "w320dp-h640dp")
    fun `main flows keep the render contract on ExtraSmall windows`() = verify("ExtraSmall")

    @Test
    @Config(qualifiers = "w390dp-h844dp")
    fun `main flows keep the render contract on Compact windows`() = verify("Compact")

    @Test
    @Config(qualifiers = "w720dp-h720dp")
    fun `main flows keep the render contract on Square windows`() = verify("Square")

    @Test
    @Config(qualifiers = "w800dp-h360dp")
    fun `main flows keep the render contract on UltraWide windows`() = verify("UltraWide")

    @Test
    @Config(qualifiers = "w1024dp-h768dp")
    fun `main flows keep the render contract on Expanded windows`() = verify("Expanded")

    private val screens: List<Pair<String, @Composable () -> Unit>> =
        listOf(
            "요약" to @Composable {
                ProfileScreen(
                    state = readyProfile(),
                    onScopeChange = {},
                    onOpenCollection = {},
                    onOpenRecord = {},
                    onShareCard = {},
                )
            },
            "일지" to @Composable {
                val records = listOf(record(1), record(2).copy(type = DrinkType.Whiskey, name = "라프로익 10년"))
                CollectionScreen(
                    state = CollectionUiState(allRecords = records, records = records, loaded = true),
                    onQueryChange = {},
                    onOpen = {},
                    onRecordNew = {},
                    contentPadding = PaddingValues(),
                )
            },
            "기록하기 · 종류" to @Composable { DrinkPicker(onPick = {}) },
            "기록하기 · 상세" to @Composable {
                RecordDetailStep(
                    type = DrinkType.Wine,
                    form = RecordForm(name = "샤블리", rating = 4.0),
                    onFormChange = {},
                    onPhotoPicked = {},
                )
            },
            "기록 상세" to @Composable {
                RecordDetailScreen(
                    record = record(1).copy(tags = DrinkTags(wineColor = WineColor.Red, origin = Origin.OldWorld)),
                    onDelete = {},
                    contentPadding = PaddingValues(),
                )
            },
            "기록 고치기" to @Composable {
                val r = record(1)
                EditRecordScreen(
                    state =
                        EditUiState(
                            loaded = true,
                            type = r.type,
                            form = RecordForm(name = r.name, rating = r.rating, memo = r.memo.orEmpty()),
                            taste = r.taste,
                        ),
                    onFormChange = {},
                    onPhotoPicked = {},
                    onAnswer = { _, _ -> },
                    contentPadding = PaddingValues(),
                )
            },
            // 위스키는 가장 긴 맛 기준 라벨("스모키하지 않음")을 쓴다. 답하지 않은 지표가 섞인 상태로 본다.
            "기록 고치기 · 위스키" to @Composable {
                EditRecordScreen(
                    state =
                        EditUiState(
                            loaded = true,
                            type = DrinkType.Whiskey,
                            form = RecordForm(name = "라프로익 10년", rating = 4.0),
                            taste = TasteInput(mapOf(Trait.Sweetness to TraitAnswer.Low)),
                        ),
                    onFormChange = {},
                    onPhotoPicked = {},
                    onAnswer = { _, _ -> },
                    contentPadding = PaddingValues(),
                )
            },
            "설정" to @Composable {
                SettingsScreen(
                    state = SettingsUiState(),
                    onExportBackup = {},
                    onRestoreBackup = {},
                    onSurfaceStyleChange = {},
                    onColorModeChange = {},
                    contentPadding = PaddingValues(),
                )
            },
            "공통 크롬" to @Composable {
                DDScreenScaffold(
                    title = "요약",
                    screenType = DDScreenType.TopLevel,
                    selectedTab = DDTopLevelTab.Dashboard,
                    onDashboardClick = {},
                    onCollectionClick = {},
                    onSettingsClick = {},
                    onAddClick = {},
                ) {
                    AuraText("본문")
                }
            },
        )

    /** Truncated text (ellipsis) is reported, not failed: long drink names are meant to ellipsize. */
    private val warnings = mutableListOf<String>()

    /**
     * Violations caused by Aura itself, tracked upstream. They are reported but do not fail;
     * once one stops occurring the test fails so the entry gets removed.
     */
    private val knownUpstream: Map<Regex, String> =
        mapOf(
            // 비어 있음. AuraSearchBar 입력 영역 문제(점수표 U1)는 Aura 2.1.0에서 고쳐졌다.
        )

    private val seenKnown = mutableSetOf<Regex>()

    private fun verify(profile: String) {
        var index by mutableIntStateOf(0)
        composeRule.setContent {
            DrinkDiaryTheme(darkTheme = false) {
                screens[index].second()
            }
        }
        val violations = mutableListOf<String>()
        screens.forEachIndexed { i, (name, _) ->
            index = i
            composeRule.waitForIdle()
            check(name).forEach { problem ->
                val entry = "$name: $problem"
                val known = knownUpstream.keys.firstOrNull { it.matches(entry) }
                if (known == null) {
                    violations += "[$profile] $entry"
                } else {
                    seenKnown += known
                    println("RENDER KNOWN [$profile] $entry — ${knownUpstream.getValue(known)}")
                }
            }
        }
        (knownUpstream.keys - seenKnown).forEach { stale ->
            violations +=
                "[$profile] known upstream issue no longer occurs, remove it: ${knownUpstream.getValue(stale)}"
        }
        violations.forEach { println("RENDER $it") }
        warnings.distinct().forEach { println("RENDER WARN [$profile] $it") }
        println(
            "RENDER [$profile] ${screens.size} screens, ${violations.size} violations, ${warnings.distinct().size} warnings",
        )
        assertTrue("Render contract violations:\n" + violations.joinToString("\n"), violations.isEmpty())
    }

    private fun check(screen: String): List<String> {
        val density = composeRule.density.density

        fun Float.dp() = this / density
        val problems = mutableListOf<String>()

        val clickables =
            composeRule
                .onAllNodes(hasClickAction())
                .fetchSemanticsNodes()
                .filter { it.size.width > 0 && it.size.height > 0 }
        clickables.forEach { node ->
            val w =
                node.size.width
                    .toFloat()
                    .dp()
            val h =
                node.size.height
                    .toFloat()
                    .dp()
            if (min(w, h) <
                47.5f
            ) {
                problems += "target ${label(node)} is ${"%.1f".format(w)}x${"%.1f".format(h)}dp (< 48dp)"
            }
        }
        for (a in clickables.indices) {
            for (b in a + 1 until clickables.size) {
                val na = clickables[a]
                val nb = clickables[b]
                if (isAncestor(na, nb) || isAncestor(nb, na)) continue
                val overlap = na.bounds().intersect(nb.bounds())
                val area = max(0f, overlap.width).dp() * max(0f, overlap.height).dp()
                if (area > 4f) problems += "targets ${label(na)} and ${label(nb)} overlap by ${"%.0f".format(area)}dp²"
            }
        }

        val root = composeRule.onNode(isRoot(), useUnmergedTree = true).fetchSemanticsNode()
        allNodes(root).forEach { node ->
            val layouts = mutableListOf<TextLayoutResult>()
            node.config
                .getOrNull(SemanticsActions.GetTextLayoutResult)
                ?.action
                ?.invoke(layouts)
            val layout = layouts.firstOrNull() ?: return@forEach
            val text = layout.layoutInput.text.take(24)
            if (layout.size.height < layout.multiParagraph.height - 0.5f) {
                problems += "text \"$text\" is clipped vertically"
            } else if (layout.multiParagraph.didExceedMaxLines) {
                warnings += "[$screen] text \"$text\" is truncated to ${layout.lineCount} line(s)"
            }
        }
        return problems.distinct()
    }

    private fun SemanticsNode.bounds() = Rect(positionInRoot, size.toSize())

    private fun allNodes(node: SemanticsNode): List<SemanticsNode> =
        listOf(node) + node.children.flatMap { allNodes(it) }

    private fun isAncestor(
        ancestor: SemanticsNode,
        node: SemanticsNode,
    ): Boolean {
        var p = node.parent
        while (p != null) {
            if (p.id == ancestor.id) return true
            p = p.parent
        }
        return false
    }

    private fun label(node: SemanticsNode): String {
        val text =
            node.config
                .getOrNull(SemanticsProperties.Text)
                ?.joinToString(" ")
                ?.takeIf { it.isNotBlank() }
        val description = node.config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString(" ")
        val field = if (node.config.getOrNull(SemanticsActions.SetText) != null) "text field " else ""
        return field + "\"${(text ?: description ?: "#${node.id}").take(24)}\""
    }

    private fun record(id: Long) =
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

    private fun readyProfile(): ProfileUiState {
        val type =
            TasteType(
                sweetness = TastePreference.Low,
                body = TastePreference.High,
                intensity = TastePreference.High,
                aftertaste = TastePreference.High,
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
                    latest = record(1),
                    thisMonth =
                        MonthSummary(
                            month = 11,
                            count = 3,
                            wineCount = 2,
                            whiskeyCount = 1,
                            weeklyCounts = listOf(1, 2, 0, 0, 0),
                        ),
                ),
            profile =
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
                ),
            readiness = ProfileReadiness.Ready(type),
            recommendations = BuyRecommendationBundle(hasEnoughData = true, repurchasePicks = listOf(record(1))),
        )
    }
}
