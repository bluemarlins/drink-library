package com.bluemarlin.drinkdiary.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.ui.component.DDBentoGrid
import com.bluemarlin.drinkdiary.ui.component.DDBentoItem
import com.bluemarlin.drinkdiary.ui.component.DDCenteredContent
import io.github.bluemarlins.aura.component.AuraSegmentedButton
import io.github.bluemarlins.aura.component.AuraSkeleton
import io.github.bluemarlins.aura.component.AuraSkeletonText
import io.github.bluemarlins.aura.component.AuraText
import io.github.bluemarlins.aura.component.BindAdaptiveBarToScrollState
import io.github.bluemarlins.aura.component.rememberFloatingScrollProgress
import io.github.bluemarlins.aura.foundation.theme.AuraTheme

// 요약 탭: 그간 마신 술을 한눈에 보는 벤토 그리드.
// 위쪽은 주종을 가리지 않는 기록의 사실(이번 달·최근·자주·누적), 아래쪽은 고른 주종의 취향이다.
// 커버 화면은 2열, 펼친 화면은 4열이다.
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
    val scrollState = rememberScrollState()
    val scrollProgress = rememberFloatingScrollProgress(scrollState)
    val columns = if (layout.supportsTwoPane) 4 else 2
    val gap = layout.gridGutter

    BindAdaptiveBarToScrollState(scrollState = scrollState)

    LaunchedEffect(scrollProgress) {
        onScrollFractionChange(scrollProgress)
    }

    DDCenteredContent(
        modifier = modifier,
        contentPadding = contentPadding,
        scrollState = scrollState,
    ) {
        if (!state.loaded) {
            ProfileSkeleton(columns = columns)
            return@DDCenteredContent
        }

        DDBentoGrid(
            columns = columns,
            spacing = gap,
            items =
                listOf(
                    DDBentoItem("month", span = 2) { MonthCard(state.highlights.thisMonth) },
                    DDBentoItem("latest", span = 1) { LatestCard(state.highlights.latest, onOpenRecord) },
                    DDBentoItem("frequent", span = 1) { FrequentCard(state.highlights.mostFrequent, onOpenRecord) },
                    DDBentoItem("total", span = 2) { TotalCard(state.overview, onOpenCollection) },
                ),
        )

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AuraText("내 취향", style = AuraTheme.typography.titleMedium)
            ScopeSelector(selected = state.scope, onSelect = onScopeChange)
        }

        DDBentoGrid(
            columns = columns,
            spacing = gap,
            items =
                listOf(
                    DDBentoItem("type", span = 2) {
                        TasteTypeCard(
                            readiness = state.readiness,
                            recordCount = state.profile?.recordCount ?: 0,
                            onShare = onShareCard,
                        )
                    },
                    DDBentoItem("traits", span = 1) { TraitCard(state.profile?.preferences.orEmpty()) },
                    DDBentoItem("labels", span = 1) { LabelCard(state.tagPreferences, state.scope) },
                    DDBentoItem("next", span = 2) { NextBottleCard(state.recommendations, onOpenRecord) },
                ),
        )
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
private fun ProfileSkeleton(columns: Int) {
    val layout = AuraTheme.layout
    val card = AuraTheme.shapes.extraLarge
    DDBentoGrid(
        columns = columns,
        spacing = layout.gridGutter,
        items =
            listOf(
                DDBentoItem("month", span = 2) {
                    AuraSkeleton(
                        Modifier.fillMaxWidth().height(144.dp),
                        shape = card,
                        contentDescription = "요약을 불러오는 중",
                    )
                },
                DDBentoItem("latest", span = 1) { AuraSkeleton(Modifier.fillMaxWidth().height(160.dp), shape = card) },
                DDBentoItem(
                    "frequent",
                    span = 1,
                ) { AuraSkeleton(Modifier.fillMaxWidth().height(160.dp), shape = card) },
                DDBentoItem("total", span = 2) { AuraSkeleton(Modifier.fillMaxWidth().height(112.dp), shape = card) },
            ),
    )
    AuraSkeletonText(lines = 1, lineHeight = 20.dp, modifier = Modifier.fillMaxWidth(0.3f))
    AuraSkeleton(Modifier.fillMaxWidth().height(48.dp), shape = AuraTheme.shapes.full)
}
