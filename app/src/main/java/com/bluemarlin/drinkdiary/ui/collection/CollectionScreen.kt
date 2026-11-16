package com.bluemarlin.drinkdiary.ui.collection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.component.DDUriImage
import com.bluemarlin.drinkdiary.ui.theme.DDShape
import com.example.ds.component.AuraCard
import com.example.ds.component.AuraCardVariant
import com.example.ds.component.AuraSegmentedButton
import com.example.ds.component.AuraText
import com.example.ds.foundation.theme.AuraTheme

@Composable
fun CollectionScreen(
    state: CollectionUiState,
    onFilterChange: (DrinkType?) -> Unit,
    onOpen: (Long) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val layout = AuraTheme.layout
    val direction = LocalLayoutDirection.current
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
            columns = GridCells.Fixed(columns),
            modifier = Modifier.widthIn(max = layout.maxContentWidth).fillMaxSize(),
            contentPadding = gridPadding,
            horizontalArrangement = Arrangement.spacedBy(layout.gridGutter),
            verticalArrangement = Arrangement.spacedBy(layout.gridGutter),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }, key = "filter") {
                AuraSegmentedButton(
                    options = filterOptions.map { it.second },
                    selectedIndex = filterOptions.indexOfFirst { it.first == state.filter }.coerceAtLeast(0),
                    onOptionSelected = { index -> onFilterChange(filterOptions[index].first) },
                    fullWidth = true,
                )
            }

            // 아직 한 번도 읽지 않은 상태를 "기록이 없다"로 말하지 않는다 — 로딩과 빈 목록은 다르다.
            if (state.loaded && state.records.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "empty") {
                    EmptyCollection(filtered = state.filter != null)
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
            text = if (filtered) "이 주종으로 남긴 기록이 없어요" else "아직 기록이 없어요",
            style = AuraTheme.typography.titleMedium,
        )
        AuraText(
            text = if (filtered) "위에서 '전체'를 눌러보세요." else "아래 + 를 눌러 한 잔 기록해 보세요.",
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
    AuraCard(onClick = onClick, modifier = Modifier.fillMaxWidth(), variant = AuraCardVariant.Outlined) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            record.imageUri?.let {
                DDUriImage(
                    imageUri = it,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp).clip(DDShape.small),
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
                        AuraText(
                            text = "★",
                            style = AuraTheme.typography.labelSmall,
                            color = AuraTheme.colors.brand.primary,
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
                text = DrinkLabels.rating(record.rating),
                style = AuraTheme.typography.titleLarge,
                color = AuraTheme.colors.brand.primary,
            )
        }
    }
}
