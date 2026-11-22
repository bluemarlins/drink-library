package com.bluemarlin.drinkdiary.ui.record

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.TasteInput
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import io.github.bluemarlins.aura.component.AuraSegmentedButton
import io.github.bluemarlins.aura.component.AuraText
import io.github.bluemarlins.aura.foundation.theme.AuraTheme

private val TasteAnswers = listOf(TraitAnswer.Low, TraitAnswer.Mid, TraitAnswer.High)

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
        AuraText("그때 남긴 취향 (맛 기준 6가지)", style = AuraTheme.typography.titleMedium)

        Trait.of(type).forEach { trait ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AuraText(
                    text = DrinkLabels.trait(trait),
                    style = AuraTheme.typography.labelMedium,
                    color = AuraTheme.colors.content.secondary,
                )
                // 아직 답하지 않은 지표는 아무것도 고르지 않은 채로 보인다(selectedIndex = -1).
                AuraSegmentedButton(
                    options = TasteAnswers.map { DrinkLabels.answer(trait, it) },
                    selectedIndex = TasteAnswers.indexOf(taste[trait]),
                    onOptionSelected = { index -> onAnswer(trait, TasteAnswers[index]) },
                )
            }
        }
    }
}
