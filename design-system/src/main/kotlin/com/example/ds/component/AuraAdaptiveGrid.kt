package com.example.ds.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.example.ds.foundation.theme.AuraTheme

/**
 * Responsive multi-column grid driven by `AuraTheme.layout.gridColumns` and `gridGutter`.
 *
 * Renders equal-weight columns inside standard or scrollable vertical columns without requiring
 * nested scroll containers.
 */
@Composable
fun <T> AuraAdaptiveGrid(
    items: List<T>,
    modifier: Modifier = Modifier,
    columns: Int = AuraTheme.layout.gridColumns,
    horizontalSpacing: Dp = AuraTheme.layout.gridGutter,
    verticalSpacing: Dp = AuraTheme.layout.gridGutter,
    itemContent: @Composable (T) -> Unit,
) {
    val safeColumns = columns.coerceAtLeast(1)
    if (items.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
    ) {
        if (safeColumns == 1) {
            items.forEach { item ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    itemContent(item)
                }
            }
        } else {
            items.chunked(safeColumns).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
                ) {
                    rowItems.forEach { item ->
                        Box(modifier = Modifier.weight(1f)) {
                            itemContent(item)
                        }
                    }
                    repeat(safeColumns - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
