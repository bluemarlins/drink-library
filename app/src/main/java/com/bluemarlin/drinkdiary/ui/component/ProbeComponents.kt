package com.bluemarlin.drinkdiary.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import com.bluemarlin.drinkdiary.ui.record.ProbeCopy
import com.example.ds.component.AuraCard
import com.example.ds.component.AuraCardVariant
import com.example.ds.component.AuraText
import com.example.ds.foundation.theme.AuraTheme

@Composable
fun DDProbeQuestion(
    copy: ProbeCopy,
    selected: TraitAnswer?,
    onSelect: (TraitAnswer) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AuraTheme.layout.sectionSpacing),
    ) {
        AuraText(
            text = copy.question,
            style = AuraTheme.typography.headlineSmall,
            color = AuraTheme.colors.content.primary,
            modifier = Modifier.fillMaxWidth(),
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 가운데는 "보통이었어요"다(2026-08-14). 판단이 안 서는 사람에게 여전히 안전한
            // 자리를 주면서, 그 답을 판정에서 버리지 않는다 — prd.md F2.
            val options =
                listOf(
                    TraitAnswer.Low to copy.lowLabel,
                    TraitAnswer.Mid to "보통이었어요",
                    TraitAnswer.High to copy.highLabel,
                )

            options.forEach { (answer, label) ->
                AuraCard(
                    onClick = { onSelect(answer) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 56.dp),
                    variant = AuraCardVariant.Outlined,
                    selected = selected == answer,
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 32.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        AuraText(
                            text = label,
                            style = AuraTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }
    }
}
