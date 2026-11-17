package com.bluemarlin.drinkdiary.ui.record

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.snap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.TasteInput
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.component.DDCenteredContent
import com.bluemarlin.drinkdiary.ui.component.DDProbeQuestion
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiaryTheme
import com.example.ds.component.AuraButton
import com.example.ds.component.AuraButtonSize
import com.example.ds.component.AuraButtonVariant
import com.example.ds.component.AuraStepProgressBar
import com.example.ds.component.AuraText
import com.example.ds.foundation.theme.AuraTheme

@Composable
fun ProbeSequenceScreen(
    type: DrinkType,
    answers: TasteInput,
    currentIndex: Int,
    onIndexChange: (Int) -> Unit,
    onAnswer: (Trait, TraitAnswer) -> Unit,
    onComplete: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    // 주종별 6개 지표(공통 4축 + 주종 고유 2축)를 1문항 1탭으로 평가한다.
    val traits = Trait.of(type)
    if (traits.isEmpty()) return

    val safeIndex = currentIndex.coerceIn(0, traits.lastIndex)
    val currentTrait = traits[safeIndex]

    DDCenteredContent(
        modifier = modifier,
        contentPadding = contentPadding,
    ) {
        AuraStepProgressBar(
            currentStep = safeIndex + 1,
            totalSteps = traits.size,
            modifier = Modifier.fillMaxWidth(),
            contentDescription = "${safeIndex + 1} / ${traits.size}",
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AuraText(
                text = "${safeIndex + 1} / ${traits.size} · ${DrinkLabels.trait(currentTrait)}",
                style = AuraTheme.typography.labelLarge,
                color = AuraTheme.colors.content.secondary,
            )
            AuraButton(
                text = if (safeIndex == 0) "주종 다시 선택" else "이전 질문",
                onClick = onBack,
                variant = AuraButtonVariant.Ghost,
                size = AuraButtonSize.Small,
            )
        }

        val motion = AuraTheme.motion
        Crossfade(
            targetState = safeIndex,
            animationSpec = if (motion.reduceMotion) snap() else motion.enterSpec(),
            label = "ProbeSequenceAnimation",
            modifier = Modifier.fillMaxWidth(),
        ) { index ->
            val trait = traits[index]
            val copy = probeCopy(type = type, trait = trait)

            DDProbeQuestion(
                copy = copy,
                selected = answers[trait],
                onSelect = { answer ->
                    onAnswer(trait, answer)
                    if (index < traits.lastIndex) {
                        onIndexChange(index + 1)
                    } else {
                        onComplete()
                    }
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProbeSequenceScreenPreview() {
    DrinkDiaryTheme {
        ProbeSequenceScreen(
            type = DrinkType.Wine,
            answers = TasteInput(),
            currentIndex = 0,
            onIndexChange = {},
            onAnswer = { _, _ -> },
            onComplete = {},
            onBack = {},
        )
    }
}
