package com.bluemarlin.drinkdiary.ui.lookup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.BottleEntry
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.component.DDBuyRecommendationSection
import com.bluemarlin.drinkdiary.ui.component.DDCenteredContent
import com.bluemarlin.drinkdiary.ui.component.DDDrinkBadge
import com.example.ds.component.AuraBadge
import com.example.ds.component.AuraBadgeVariant
import com.example.ds.component.AuraButton
import com.example.ds.component.AuraButtonVariant
import com.example.ds.component.AuraCard
import com.example.ds.component.AuraCardPadding
import com.example.ds.component.AuraCardVariant
import com.example.ds.component.AuraDivider
import com.example.ds.component.AuraSearchBar
import com.example.ds.component.AuraSearchBarSurface
import com.example.ds.component.AuraText
import com.example.ds.component.BindAdaptiveBarToScrollState
import com.example.ds.component.LocalAuraAdaptiveBarState
import com.example.ds.component.rememberFloatingScrollProgress
import com.example.ds.foundation.theme.AuraTheme

// F5: 매장에서 오프라인으로 "전에 마셨나? 좋아했나?"를 15초 안에 판단하는 빠른 조회 화면.
@Composable
fun LookupScreen(
    state: LookupUiState,
    onQueryChange: (String) -> Unit,
    onOpenRecord: (Long) -> Unit,
    onRecordNew: (String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    onScrollFractionChange: (Float) -> Unit = {},
) {
    val scrollState = rememberScrollState()
    val scrollProgress = rememberFloatingScrollProgress(scrollState)
    val adaptiveBarState = LocalAuraAdaptiveBarState.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var searchFocused by remember { mutableStateOf(false) }

    BindAdaptiveBarToScrollState(scrollState = scrollState)

    LaunchedEffect(scrollProgress) {
        onScrollFractionChange(scrollProgress)
    }

    DDCenteredContent(
        modifier = modifier,
        contentPadding = contentPadding,
        scrollState = scrollState,
    ) {
        // 검색 중에는 소개 문구를 접는다. 작은 화면에서는 키보드 위에 결과가 한 건밖에 안 남는다.
        if (!searchFocused && state.query.isBlank()) {
            AuraText("매장에서 바로 찾기", style = AuraTheme.typography.headlineSmall)
            AuraText(
                text = "이름 일부만 입력해도 내가 좋아했던 술인지 바로 확인할 수 있어요.",
                style = AuraTheme.typography.bodySmall,
                color = AuraTheme.colors.content.secondary,
            )
        }

        AuraSearchBar(
            query = state.query,
            onQueryChange = onQueryChange,
            onSearch = {
                keyboardController?.hide()
                focusManager.clearFocus()
            },
            placeholder = "와인 또는 위스키 이름 검색",
            clearContentDescription = "검색어 지우기",
            surface = AuraSearchBarSurface.Solid,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        searchFocused = focusState.isFocused
                        adaptiveBarState?.updatePinnedFlags(focused = focusState.isFocused)
                    },
        )

        if (state.query.isNotBlank()) {
            SearchResultsSection(
                query = state.query.trim(),
                records = state.matchedRecords,
                catalogMatches = state.matchedCatalogBottles,
                onOpenRecord = onOpenRecord,
                onRecordNew = onRecordNew,
            )
        } else {
            if (state.repurchaseCandidates.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AuraText("또 살래요 리스트", style = AuraTheme.typography.titleMedium)
                    state.repurchaseCandidates.forEach { record ->
                        LookupRecordCard(record = record, onClick = { onOpenRecord(record.id) })
                    }
                }
                AuraDivider()
            }

            DDBuyRecommendationSection(
                scope = TypeScope.Combined,
                bundle = state.combinedRecommendations,
                onOpenRecord = onOpenRecord,
            )
        }
    }
}

@Composable
private fun SearchResultsSection(
    query: String,
    records: List<DrinkRecord>,
    catalogMatches: List<BottleEntry>,
    onOpenRecord: (Long) -> Unit,
    onRecordNew: (String) -> Unit,
) {
    if (records.isEmpty() && catalogMatches.isEmpty()) {
        AuraCard(
            modifier = Modifier.fillMaxWidth(),
            variant = AuraCardVariant.Outlined,
            padding = AuraCardPadding.Medium,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AuraText(
                    text = "일기장에도 사전에도 없어요.",
                    style = AuraTheme.typography.bodyMedium,
                    color = AuraTheme.colors.content.secondary,
                )
                // 매장에서 처음 본 술이다. 찾던 이름 그대로 바로 기록으로 넘긴다.
                AuraButton(
                    text = "'$query' 일기 쓰기",
                    onClick = { onRecordNew(query) },
                    variant = AuraButtonVariant.Primary,
                    fullWidth = true,
                )
            }
        }
        return
    }

    if (records.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AuraText("내 일기장에서 (${records.size})", style = AuraTheme.typography.titleMedium)
            records.forEach { record ->
                LookupRecordCard(record = record, onClick = { onOpenRecord(record.id) })
            }
        }
    }

    if (catalogMatches.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AuraText("아직 안 마셔본 술 (사전 참고)", style = AuraTheme.typography.titleMedium)
            catalogMatches.forEach { entry ->
                AuraCard(
                    modifier = Modifier.fillMaxWidth(),
                    variant = AuraCardVariant.Outlined,
                    padding = AuraCardPadding.Medium,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            AuraText(entry.displayName, style = AuraTheme.typography.titleSmall)
                            AuraText(
                                text = DrinkLabels.bottleFactSummary(entry),
                                style = AuraTheme.typography.bodySmall,
                                color = AuraTheme.colors.content.secondary,
                            )
                        }
                        DDDrinkBadge(drinkType = entry.type)
                    }
                }
            }
        }
    }
}

@Composable
private fun LookupRecordCard(
    record: DrinkRecord,
    onClick: () -> Unit,
) {
    val badgeVariant =
        when (record.collectionStatus) {
            CollectionStatus.Repurchase -> AuraBadgeVariant.Success
            CollectionStatus.NotForMe -> AuraBadgeVariant.Error
            CollectionStatus.Normal -> AuraBadgeVariant.Neutral
        }
    val statusText =
        "${DrinkLabels.collectionStatus(record.collectionStatus)} · ${DrinkLabels.ratingText(record.rating)}"

    AuraCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        variant = AuraCardVariant.Outlined,
        padding = AuraCardPadding.Medium,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                AuraText(record.name, style = AuraTheme.typography.titleSmall, maxLines = 1)
                AuraText(
                    text = DrinkLabels.subtitle(record),
                    style = AuraTheme.typography.bodySmall,
                    color = AuraTheme.colors.content.secondary,
                    maxLines = 1,
                )
            }
            AuraBadge(label = statusText, variant = badgeVariant)
        }
    }
}
