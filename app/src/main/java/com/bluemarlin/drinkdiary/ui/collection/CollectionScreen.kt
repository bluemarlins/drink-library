package com.bluemarlin.drinkdiary.ui.collection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.component.DDRepurchaseBadge
import com.bluemarlin.drinkdiary.ui.component.DDUriImage
import com.example.ds.component.AuraBadgeSize
import com.example.ds.component.AuraCard
import com.example.ds.component.AuraCardPadding
import com.example.ds.component.AuraCardVariant
import com.example.ds.component.AuraChipButton
import com.example.ds.component.AuraChipVariant
import com.example.ds.component.AuraSearchBar
import com.example.ds.component.AuraSearchBarSurface
import com.example.ds.component.AuraSegmentedButton
import com.example.ds.component.AuraText
import com.example.ds.component.BindAdaptiveBarToLazyGridState
import com.example.ds.component.LocalAuraAdaptiveBarState
import com.example.ds.component.LocalGlassBackdropState
import com.example.ds.foundation.theme.AuraTheme
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CollectionScreen(
    state: CollectionUiState,
    onFilterChange: (DrinkType?) -> Unit,
    onStatusFilterChange: (CollectionStatus?) -> Unit,
    onQueryChange: (String) -> Unit,
    onToggleSortByRating: () -> Unit,
    onOpen: (Long) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    onScrollFractionChange: (Float) -> Unit = {},
) {
    val layout = AuraTheme.layout
    val direction = LocalLayoutDirection.current
    val density = LocalDensity.current
    val gridState = rememberLazyGridState()
    val backdropState = LocalGlassBackdropState.current
    val adaptiveBarState = LocalAuraAdaptiveBarState.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    BindAdaptiveBarToLazyGridState(gridState = gridState)

    val thresholdPx = remember(density) { with(density) { 64.dp.toPx().coerceAtLeast(1f) } }
    val scrollProgress by remember(gridState, thresholdPx) {
        derivedStateOf {
            if (gridState.firstVisibleItemIndex > 0) {
                1f
            } else {
                (gridState.firstVisibleItemScrollOffset / thresholdPx).coerceIn(0f, 1f)
            }
        }
    }

    LaunchedEffect(scrollProgress) {
        onScrollFractionChange(scrollProgress)
    }
    LaunchedEffect(gridState, backdropState) {
        if (backdropState == null) return@LaunchedEffect
        snapshotFlow {
            gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset
        }.distinctUntilChanged().collect {
            backdropState.invalidateBackdrop()
        }
    }

    val gridPadding =
        PaddingValues(
            start = contentPadding.calculateStartPadding(direction) + layout.screenMarginHorizontal,
            end = contentPadding.calculateEndPadding(direction) + layout.screenMarginHorizontal,
            top = contentPadding.calculateTopPadding() + layout.screenMarginVertical,
            bottom = contentPadding.calculateBottomPadding() + layout.screenMarginVertical,
        )
    val filterOptions = listOf(null to "전체", DrinkType.Wine to "와인", DrinkType.Whiskey to "위스키")
    val columns = layout.gridColumns.coerceAtLeast(1)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(columns),
            modifier = Modifier.widthIn(max = layout.maxContentWidth).fillMaxSize(),
            contentPadding = gridPadding,
            horizontalArrangement = Arrangement.spacedBy(layout.gridGutter),
            verticalArrangement = Arrangement.spacedBy(layout.gridGutter),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }, key = "search") {
                AuraSearchBar(
                    query = state.query,
                    onQueryChange = onQueryChange,
                    onSearch = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    },
                    placeholder = "술 이름, 구입처, 메모 검색",
                    clearContentDescription = "검색어 지우기",
                    surface = AuraSearchBarSurface.Solid,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                adaptiveBarState?.updatePinnedFlags(focused = focusState.isFocused)
                            },
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }, key = "filter") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AuraSegmentedButton(
                        options = filterOptions.map { it.second },
                        selectedIndex = filterOptions.indexOfFirst { it.first == state.filter }.coerceAtLeast(0),
                        onOptionSelected = { index -> onFilterChange(filterOptions[index].first) },
                        fullWidth = true,
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AuraChipButton(
                            label = "또 살래요",
                            variant = AuraChipVariant.Filter,
                            selected = state.statusFilter == CollectionStatus.Repurchase,
                            onClick = { onStatusFilterChange(CollectionStatus.Repurchase) },
                        )
                        AuraChipButton(
                            label = "안 맞아요",
                            variant = AuraChipVariant.Filter,
                            selected = state.statusFilter == CollectionStatus.NotForMe,
                            onClick = { onStatusFilterChange(CollectionStatus.NotForMe) },
                        )
                        AuraChipButton(
                            label = "별점 높은 순",
                            variant = AuraChipVariant.Filter,
                            selected = state.sortByRating,
                            onClick = onToggleSortByRating,
                        )
                    }
                }
            }

            if (state.loaded && state.records.isEmpty()) {
                val hasActiveFilter =
                    state.filter != null || state.statusFilter != null || state.query.isNotBlank()
                item(span = { GridItemSpan(maxLineSpan) }, key = "empty") {
                    EmptyCollection(filtered = hasActiveFilter)
                }
            } else {
                items(state.records, key = { it.id }) { record ->
                    RecordRow(record = record, onClick = { onOpen(record.id) })
                }
            }
        }
    }
}

@Composable
private fun EmptyCollection(filtered: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AuraText(
            text = if (filtered) "조건에 맞는 기록이 없어요" else "아직 기록이 없어요",
            style = AuraTheme.typography.titleMedium,
        )
        AuraText(
            text = if (filtered) "검색어나 필터를 바꾸어 보세요." else "아래 + 를 눌러 한 잔 기록해 보세요.",
            style = AuraTheme.typography.bodyMedium,
            color = AuraTheme.colors.content.secondary,
        )
    }
}

@Composable
private fun RecordRow(
    record: DrinkRecord,
    onClick: () -> Unit,
) {
    AuraCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        variant = AuraCardVariant.Outlined,
        padding = AuraCardPadding.Medium,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            record.imageUri?.let {
                DDUriImage(
                    imageUri = it,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp).clip(AuraTheme.shapes.small),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AuraText(
                        text = record.name,
                        style = AuraTheme.typography.titleMedium,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (record.collectionStatus == CollectionStatus.Repurchase) {
                        DDRepurchaseBadge(
                            text = DrinkLabels.collectionStatus(CollectionStatus.Repurchase),
                            size = AuraBadgeSize.Small,
                        )
                    }
                }
                AuraText(
                    text = DrinkLabels.subtitle(record),
                    style = AuraTheme.typography.bodySmall,
                    color = AuraTheme.colors.content.secondary,
                    maxLines = 1,
                )
            }

            AuraText(
                text = DrinkLabels.ratingText(record.rating),
                style = AuraTheme.typography.titleLarge,
            )
        }
    }
}
