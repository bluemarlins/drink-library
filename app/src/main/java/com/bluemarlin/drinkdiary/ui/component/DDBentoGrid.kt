package com.bluemarlin.drinkdiary.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

// 요약 탭의 벤토 그리드 칸 하나. span은 차지하는 열 수다.
class DDBentoItem(
    val key: String,
    val span: Int,
    val content: @Composable () -> Unit,
)

// 칸을 순서대로 줄에 채운다. 한 줄의 칸은 높이를 맞춰 카드 아래 끝이 가지런하다.
// 칸 안에서는 SubcomposeLayout(BoxWithConstraints, Lazy*)를 쓰지 않는다 — 높이를 맞추려고 고유 크기를 묻는다.
@Composable
fun DDBentoGrid(
    items: List<DDBentoItem>,
    columns: Int,
    spacing: Dp,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing),
    ) {
        bentoRows(items.map { it.span.coerceIn(1, columns) }, columns).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(spacing),
            ) {
                row.forEach { index ->
                    val item = items[index]
                    Box(modifier = Modifier.weight(item.span.coerceIn(1, columns).toFloat()).fillMaxHeight()) {
                        item.content()
                    }
                }
                val used = row.sumOf { items[it].span.coerceIn(1, columns) }
                if (used < columns) Spacer(modifier = Modifier.weight((columns - used).toFloat()))
            }
        }
    }
}

// 칸 인덱스를 줄 단위로 묶는다. 다음 칸이 남은 자리에 들어가지 않으면 새 줄을 시작한다.
internal fun bentoRows(
    spans: List<Int>,
    columns: Int,
): List<List<Int>> {
    val rows = mutableListOf<MutableList<Int>>()
    var used = columns
    spans.forEachIndexed { index, span ->
        if (used + span > columns) {
            rows += mutableListOf<Int>()
            used = 0
        }
        rows.last() += index
        used += span
    }
    return rows
}
