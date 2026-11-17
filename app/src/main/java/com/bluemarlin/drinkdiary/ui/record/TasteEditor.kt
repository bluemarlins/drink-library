package com.bluemarlin.drinkdiary.ui.record

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.TasteInput
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.example.ds.component.AuraChipButton
import com.example.ds.component.AuraChipVariant
import com.example.ds.component.AuraText
import com.example.ds.foundation.theme.AuraTheme

// 편집 화면용 취향 입력. 주종별 6개 지표(공통 4축 + 고유 2축) 전체를 한 화면에서 바로 수정한다.
@Composable
fun TasteEditor(
    type: DrinkType,
    taste: TasteInput,
    onAnswer: (Trait, TraitAnswer) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AuraTheme.layout.sectionSpacing),
    ) {
        AuraText("그때 남긴 취향 (6개 지표)", style = AuraTheme.typography.titleMedium)

        Trait.of(type).forEach { trait ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AuraText(
                    text = DrinkLabels.trait(trait),
                    style = AuraTheme.typography.labelMedium,
                    color = AuraTheme.colors.content.secondary,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(TraitAnswer.Low, TraitAnswer.Mid, TraitAnswer.High).forEach { answer ->
                        AuraChipButton(
                            selected = taste[trait] == answer,
                            variant = AuraChipVariant.Tag,
                            onClick = { onAnswer(trait, answer) },
                            label = DrinkLabels.answer(trait, answer),
                        )
                    }
                }
            }
        }
    }
}
