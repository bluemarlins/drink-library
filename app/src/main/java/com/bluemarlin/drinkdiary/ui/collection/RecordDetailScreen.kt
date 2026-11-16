package com.bluemarlin.drinkdiary.ui.collection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.component.DDCenteredContent
import com.bluemarlin.drinkdiary.ui.component.DDDrinkBadge
import com.bluemarlin.drinkdiary.ui.component.DDRepurchaseBadge
import com.bluemarlin.drinkdiary.ui.component.DDTwoPaneContent
import com.bluemarlin.drinkdiary.ui.component.DDUriImage
import com.example.ds.component.AuraBadge
import com.example.ds.component.AuraBadgeVariant
import com.example.ds.component.AuraButton
import com.example.ds.component.AuraButtonSize
import com.example.ds.component.AuraButtonVariant
import com.example.ds.component.AuraDialog
import com.example.ds.component.AuraDialogVariant
import com.example.ds.component.AuraDivider
import com.example.ds.component.AuraRatingBar
import com.example.ds.component.AuraRatingBarSize
import com.example.ds.component.AuraText
import com.example.ds.foundation.theme.AuraTheme

@Composable
fun RecordDetailScreen(
    record: DrinkRecord?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val layout = AuraTheme.layout

    if (record == null) {
        // 삭제 직후에도 잠깐 지나가는 상태다. "없어졌다"고 단정하지 않는다.
        DDCenteredContent(
            modifier = modifier,
            contentPadding = contentPadding,
            scrollable = false,
        ) {
            AuraText("기록을 불러오는 중이에요.", style = AuraTheme.typography.bodyLarge)
        }
        return
    }

    var confirming by remember { mutableStateOf(false) }

    if (layout.supportsTwoPane) {
        DDTwoPaneContent(
            modifier = modifier,
            contentPadding = contentPadding,
            primaryPane = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(layout.sectionSpacing),
                ) {
                    RecordOverviewSection(record)
                }
            },
            secondaryPane = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(layout.sectionSpacing),
                ) {
                    RecordTasteAndActionsSection(
                        record = record,
                        onEdit = onEdit,
                        onRequestDelete = { confirming = true },
                    )
                }
            },
        )
    } else {
        DDCenteredContent(
            modifier = modifier,
            contentPadding = contentPadding,
        ) {
            RecordOverviewSection(record)
            AuraDivider()
            RecordTasteAndActionsSection(
                record = record,
                onEdit = onEdit,
                onRequestDelete = { confirming = true },
            )
        }
    }

    if (confirming) {
        AuraDialog(
            visible = true,
            title = "${record.name} 기록을 지울까요?",
            // 되돌릴 수 없다는 사실을 누르기 전에 말한다.
            message = "취향 답까지 함께 지워지고, 되돌릴 수 없어요. 취향 유형도 이 기록만큼 다시 계산됩니다.",
            variant = AuraDialogVariant.Destructive,
            confirmText = "지우기",
            dismissText = "그대로 두기",
            onConfirm = {
                confirming = false
                onDelete()
            },
            onDismissRequest = { confirming = false },
        )
    }
}

@Composable
private fun RecordOverviewSection(record: DrinkRecord) {
    record.imageUri?.let {
        DDUriImage(
            imageUri = it,
            contentDescription = "${record.name} 사진",
            modifier = Modifier.fillMaxWidth().height(220.dp).clip(AuraTheme.shapes.medium),
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DDDrinkBadge(drinkType = record.type)
            if (record.collectionStatus == CollectionStatus.Repurchase) {
                DDRepurchaseBadge()
            }
        }
        AuraText(record.name, style = AuraTheme.typography.headlineSmall)
        AuraText(
            text = DrinkLabels.subtitle(record),
            style = AuraTheme.typography.bodyMedium,
            color = AuraTheme.colors.content.secondary,
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AuraText("만족도", style = AuraTheme.typography.bodyLarge, color = AuraTheme.colors.content.secondary)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AuraRatingBar(
                rating = record.rating.toFloat(),
                onRatingChange = null,
                allowHalfRating = true,
                size = AuraRatingBarSize.Small,
                showValueText = false,
            )
            AuraText("${DrinkLabels.rating(record.rating)} / 5", style = AuraTheme.typography.bodyLarge)
        }
    }

    DetailRow("다시 살까", DrinkLabels.collectionStatus(record.collectionStatus))
    record.price?.let { DetailRow("가격", DrinkLabels.price(it)) }
    record.place?.let { DetailRow("어디에서", it) }

    record.memo?.let {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            AuraText("메모", style = AuraTheme.typography.labelLarge)
            AuraText(it, style = AuraTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun RecordTasteAndActionsSection(
    record: DrinkRecord,
    onEdit: () -> Unit,
    onRequestDelete: () -> Unit,
) {
    AuraText("그때 남긴 취향", style = AuraTheme.typography.titleMedium)
    if (record.taste.answers.isEmpty()) {
        AuraText(
            text = "취향 입력이 없는 기록이에요.",
            style = AuraTheme.typography.bodyMedium,
            color = AuraTheme.colors.content.secondary,
        )
    } else {
        // 저장된 순서가 아니라 축 순서로 보여준다 — 기록마다 순서가 달라지면 비교가 안 된다.
        Trait.entries.forEach { trait ->
            record.taste[trait]?.let { answer ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AuraText(DrinkLabels.trait(trait), style = AuraTheme.typography.bodyLarge)
                    AuraBadge(
                        label = DrinkLabels.answer(trait, answer),
                        variant = AuraBadgeVariant.Neutral,
                    )
                }
            }
        }
    }

    AuraDivider()

    // 고치기가 지우기보다 위에 있고 채워진 버튼이다. 오타 하나 때문에 지우고 다시 쓰게
    // 만들면 그 기록의 날짜가 바뀌고, 취향 판정의 표본도 한 번 흔들린다.
    AuraButton(
        text = "고치기",
        onClick = onEdit,
        variant = AuraButtonVariant.Primary,
        size = AuraButtonSize.Large,
        fullWidth = true,
    )

    AuraButton(
        text = "이 기록 지우기",
        onClick = onRequestDelete,
        variant = AuraButtonVariant.Destructive,
        size = AuraButtonSize.Large,
        fullWidth = true,
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AuraText(label, style = AuraTheme.typography.bodyLarge, color = AuraTheme.colors.content.secondary)
        AuraText(
            value,
            style = AuraTheme.typography.bodyLarge,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}
