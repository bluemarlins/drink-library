package com.bluemarlin.drinkdiary.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.ProfileReadiness
import com.bluemarlin.drinkdiary.domain.model.TagPreference
import com.bluemarlin.drinkdiary.domain.model.TasteProfile
import com.bluemarlin.drinkdiary.domain.model.TraitPreference
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.component.DDCenteredContent
import com.bluemarlin.drinkdiary.ui.component.DDProfileProgressCard
import com.bluemarlin.drinkdiary.ui.component.DDTasteSentenceCard
import com.bluemarlin.drinkdiary.ui.component.DDTasteTypeBadge
import com.bluemarlin.drinkdiary.ui.component.DDTwoPaneContent
import com.example.ds.component.AuraBadge
import com.example.ds.component.AuraBadgeVariant
import com.example.ds.component.AuraSegmentedButton
import com.example.ds.component.AuraText
import com.example.ds.foundation.theme.AuraTheme

// F3 — 문장 우선, 차트는 보조(software-architecture.md 6절).
// N개 미만이어도 화면을 비우지 않고 "아직 이르다"와 근거를 함께 보여준다(prd.md F3).
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onScopeChange: (TypeScope) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val layout = AuraTheme.layout
    val preferences = state.profile?.preferences.orEmpty()
    // 유형은 공통 축만으로 성립한다. 주종 고유 축을 같은 목록에 섞으면 유형까지의 거리가
    // 실제보다 멀어 보이고, 기본 입력 경로가 묻지도 않는 축에 "더 기록하면 된다"고
    // 약속하게 된다(실기기에서 확인된 결함).
    val (shared, specific) = preferences.partition { it.trait.shared }
    val answeredSpecific = specific.filter { it.samples > 0 }

    if (layout.supportsTwoPane) {
        DDTwoPaneContent(
            modifier = modifier,
            contentPadding = contentPadding,
            primaryPane = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(layout.sectionSpacing),
                ) {
                    ScopeSelector(selected = state.scope, onSelect = onScopeChange)
                    SummaryHeadline(readiness = state.readiness, profile = state.profile)
                }
            },
            secondaryPane = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(layout.sectionSpacing),
                ) {
                    TraitAndTagSections(
                        shared = shared,
                        answeredSpecific = answeredSpecific,
                        tagPreferences = state.tagPreferences,
                        scope = state.scope,
                    )
                }
            },
        )
    } else {
        DDCenteredContent(
            modifier = modifier,
            contentPadding = contentPadding,
        ) {
            ScopeSelector(selected = state.scope, onSelect = onScopeChange)
            SummaryHeadline(readiness = state.readiness, profile = state.profile)
            TraitAndTagSections(
                shared = shared,
                answeredSpecific = answeredSpecific,
                tagPreferences = state.tagPreferences,
                scope = state.scope,
            )
        }
    }
}

@Composable
private fun TraitAndTagSections(
    shared: List<TraitPreference>,
    answeredSpecific: List<TraitPreference>,
    tagPreferences: List<TagPreference>,
    scope: TypeScope,
) {
    if (shared.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            AuraText("유형을 만드는 축", style = AuraTheme.typography.titleMedium)
            shared.forEach { pref -> TraitStatusRow(pref) }
        }
    }

    // 고유 축은 확장 입력 경로로만 채워진다. 답이 하나도 없으면 아예 보여주지 않는다 —
    // 채울 방법이 없는 항목을 미완성으로 걸어두지 않는다.
    if (answeredSpecific.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            AuraText("추가로 기록한 축", style = AuraTheme.typography.titleMedium)
            answeredSpecific.forEach { pref -> TraitStatusRow(pref) }
        }
    }

    if (tagPreferences.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            AuraText("라벨로 본 취향", style = AuraTheme.typography.titleMedium)
            AuraText(
                // 이쪽 결과는 가져갈 수 있다는 점이 감각 축과 다르다.
                text = "매장에서 라벨만 보고도 쓸 수 있는 기준이에요.",
                style = AuraTheme.typography.bodySmall,
                color = AuraTheme.colors.content.secondary,
            )
            tagPreferences.forEach { pref ->
                TagPreferenceBlock(pref, scope)
            }
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
                    details = listOf("기록 ${recordCount}개를 바탕으로 판정했어요."),
                )
            }
        }

        is ProfileReadiness.NotReady -> {
            val description =
                if (recordCount == 0) {
                    "첫 기록을 남기면 여기서 취향이 보이기 시작해요."
                } else {
                    "지금까지 ${recordCount}개를 기록했어요. ${readiness.recordsNeeded}개만 더 남기면 유형이 나와요."
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
    // 통합 스코프에서는 주종을 특정할 수 없으므로 도수 구간을 순서로만 말한다.
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
                // 차이가 없으면 없다고 말한다. 순위만 보여주면 없는 선호가 있는 것처럼 읽힌다.
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
                    text = "%.1f점".format(value.averageRating),
                    style = AuraTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun TraitStatusRow(pref: TraitPreference) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AuraText(DrinkLabels.trait(pref.trait), style = AuraTheme.typography.bodyLarge)

        when (traitStatus(pref)) {
            TraitStatus.Resolved ->
                AuraBadge(
                    label = DrinkLabels.preference(pref.trait, pref.preference!!),
                    variant = AuraBadgeVariant.Brand,
                )

            // 결핍이 아니라 결론이다. "아직"이라고 말하지 않는다.
            TraitStatus.Neutral ->
                AuraText(
                    text = "크게 가리지 않으세요",
                    style = AuraTheme.typography.bodySmall,
                    color = AuraTheme.colors.content.secondary,
                    textAlign = TextAlign.End,
                )

            TraitStatus.NeedsRecords ->
                AuraText(
                    text = "${recordsNeeded(pref)}개 더 필요해요",
                    style = AuraTheme.typography.bodySmall,
                    color = AuraTheme.colors.content.secondary,
                    textAlign = TextAlign.End,
                )
        }
    }
}
