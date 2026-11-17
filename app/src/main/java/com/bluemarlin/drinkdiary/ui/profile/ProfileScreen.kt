package com.bluemarlin.drinkdiary.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.ProfileReadiness
import com.bluemarlin.drinkdiary.domain.model.TagPreference
import com.bluemarlin.drinkdiary.domain.model.TasteProfile
import com.bluemarlin.drinkdiary.domain.model.TraitPreference
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.component.DDBuyRecommendationSection
import com.bluemarlin.drinkdiary.ui.component.DDCenteredContent
import com.bluemarlin.drinkdiary.ui.component.DDDashboardSummaryCard
import com.bluemarlin.drinkdiary.ui.component.DDProfileProgressCard
import com.bluemarlin.drinkdiary.ui.component.DDTasteSentenceCard
import com.bluemarlin.drinkdiary.ui.component.DDTasteTypeBadge
import com.bluemarlin.drinkdiary.ui.component.DDTraitSpectrumRow
import com.bluemarlin.drinkdiary.ui.component.DDTwoPaneContent
import com.example.ds.component.AuraButton
import com.example.ds.component.AuraButtonSize
import com.example.ds.component.AuraButtonVariant
import com.example.ds.component.AuraDivider
import com.example.ds.component.AuraSegmentedButton
import com.example.ds.component.AuraText
import com.example.ds.component.BindAdaptiveBarToScrollState
import com.example.ds.component.rememberFloatingScrollProgress
import com.example.ds.foundation.theme.AuraTheme

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onScopeChange: (TypeScope) -> Unit,
    onOpenCollection: () -> Unit,
    onOpenRecord: (Long) -> Unit,
    onShareCard: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    onScrollFractionChange: (Float) -> Unit = {},
) {
    val layout = AuraTheme.layout
    val preferences = state.profile?.preferences.orEmpty()
    val scrollState = rememberScrollState()
    val scrollProgress = rememberFloatingScrollProgress(scrollState)

    if (!layout.supportsTwoPane) {
        BindAdaptiveBarToScrollState(scrollState = scrollState)
    }

    LaunchedEffect(scrollProgress, layout.supportsTwoPane) {
        onScrollFractionChange(if (layout.supportsTwoPane) 0f else scrollProgress)
    }

    if (layout.supportsTwoPane) {
        DDTwoPaneContent(
            modifier = modifier,
            contentPadding = contentPadding,
            primaryPane = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(layout.sectionSpacing),
                ) {
                    DDDashboardSummaryCard(
                        overview = state.overview,
                        onOpenCollection = onOpenCollection,
                    )
                    ScopeSelector(selected = state.scope, onSelect = onScopeChange)
                    SummaryHeadline(
                        readiness = state.readiness,
                        profile = state.profile,
                        onShareCard = onShareCard,
                    )
                    TraitSpectrumSection(preferences = preferences, scope = state.scope)
                }
            },
            secondaryPane = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(layout.sectionSpacing),
                ) {
                    DDBuyRecommendationSection(
                        scope = state.scope,
                        bundle = state.recommendations,
                        onOpenRecord = onOpenRecord,
                    )
                    TagPreferencesSection(tagPreferences = state.tagPreferences, scope = state.scope)
                }
            },
        )
    } else {
        DDCenteredContent(
            modifier = modifier,
            contentPadding = contentPadding,
            scrollState = scrollState,
        ) {
            DDDashboardSummaryCard(
                overview = state.overview,
                onOpenCollection = onOpenCollection,
            )
            ScopeSelector(selected = state.scope, onSelect = onScopeChange)
            SummaryHeadline(
                readiness = state.readiness,
                profile = state.profile,
                onShareCard = onShareCard,
            )
            TraitSpectrumSection(preferences = preferences, scope = state.scope)
            AuraDivider()
            DDBuyRecommendationSection(
                scope = state.scope,
                bundle = state.recommendations,
                onOpenRecord = onOpenRecord,
            )
            TagPreferencesSection(tagPreferences = state.tagPreferences, scope = state.scope)
        }
    }
}

@Composable
private fun TraitSpectrumSection(
    preferences: List<TraitPreference>,
    scope: TypeScope,
) {
    if (preferences.isEmpty()) return

    val title =
        when (scope) {
            TypeScope.Wine -> "와인 맛 기준 6가지"
            TypeScope.Whiskey -> "위스키 맛 기준 6가지"
            TypeScope.Combined -> "와인·위스키 공통 맛 기준 4가지"
        }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AuraText(title, style = AuraTheme.typography.titleMedium)
        preferences.forEach { pref ->
            DDTraitSpectrumRow(pref = pref)
        }
    }
}

@Composable
private fun TagPreferencesSection(
    tagPreferences: List<TagPreference>,
    scope: TypeScope,
) {
    if (tagPreferences.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AuraText("라벨로 본 취향", style = AuraTheme.typography.titleMedium)
        AuraText(
            text = "매장에서 라벨만 보고도 쓸 수 있는 기준이에요.",
            style = AuraTheme.typography.bodySmall,
            color = AuraTheme.colors.content.secondary,
        )
        tagPreferences.forEach { pref ->
            TagPreferenceBlock(pref, scope)
        }
    }
}

@Composable
private fun ScopeSelector(
    selected: TypeScope,
    onSelect: (TypeScope) -> Unit,
) {
    val scopes =
        listOf(
            TypeScope.Wine to "와인",
            TypeScope.Whiskey to "위스키",
            TypeScope.Combined to "통합",
        )
    AuraSegmentedButton(
        options = scopes.map { it.second },
        selectedIndex = scopes.indexOfFirst { it.first == selected }.coerceAtLeast(0),
        onOptionSelected = { index -> onSelect(scopes[index].first) },
        fullWidth = true,
    )
}

@Composable
private fun SummaryHeadline(
    readiness: ProfileReadiness,
    profile: TasteProfile?,
    onShareCard: () -> Unit,
) {
    val recordCount = profile?.recordCount ?: 0

    when (readiness) {
        is ProfileReadiness.Ready -> {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DDTasteTypeBadge(
                    code = readiness.type.code,
                    name = TasteTypeCopy.shortName(readiness.type),
                )
                DDTasteSentenceCard(
                    sentence = TasteTypeCopy.sentence(readiness.type),
                    details = listOf("${recordCount}잔을 바탕으로 판정했어요.", TasteTypeCopy.legend(readiness.type)),
                )
                AuraButton(
                    text = "내 취향 카드 공유하기",
                    onClick = onShareCard,
                    variant = AuraButtonVariant.Outline,
                    size = AuraButtonSize.Medium,
                    fullWidth = true,
                )
            }
        }

        is ProfileReadiness.NotReady -> {
            val description =
                if (recordCount == 0) {
                    "첫 기록을 남기면 여기서 취향이 보이기 시작해요."
                } else {
                    "지금까지 ${recordCount}잔을 기록했어요. ${readiness.recordsNeeded}잔만 더 남기면 취향 유형이 나와요."
                }
            val target = recordCount + readiness.recordsNeeded
            DDProfileProgressCard(
                title = "아직 취향을 판단하기엔 일러요",
                description = description,
                currentCount = recordCount,
                targetCount = target,
            )
        }
    }
}

@Composable
private fun TagPreferenceBlock(
    pref: TagPreference,
    scope: TypeScope,
) {
    val drinkType =
        when (scope) {
            TypeScope.Wine -> DrinkType.Wine
            TypeScope.Whiskey -> DrinkType.Whiskey
            TypeScope.Combined -> null
        }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AuraText(DrinkLabels.tagCategory(pref.category), style = AuraTheme.typography.bodyLarge)
            if (!pref.meaningfulGap) {
                AuraText(
                    text = "아직 차이가 뚜렷하지 않아요",
                    style = AuraTheme.typography.bodySmall,
                    color = AuraTheme.colors.content.secondary,
                )
            }
        }

        pref.values.forEach { value ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                AuraText(
                    text = "${DrinkLabels.tagValue(pref.category, value.value, drinkType)} · ${value.samples}잔",
                    style = AuraTheme.typography.bodyMedium,
                    color = AuraTheme.colors.content.secondary,
                )
                AuraText(
                    text = DrinkLabels.averageRatingText(value.averageRating),
                    style = AuraTheme.typography.bodyMedium,
                )
            }
        }
    }
}
