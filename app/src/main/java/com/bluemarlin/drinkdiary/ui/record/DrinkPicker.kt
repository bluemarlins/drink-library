package com.bluemarlin.drinkdiary.ui.record

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.DrinkTags
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.TagCategory
import com.bluemarlin.drinkdiary.domain.model.WhiskyStyle
import com.bluemarlin.drinkdiary.domain.model.WineColor
import com.bluemarlin.drinkdiary.ui.component.DDCenteredContent
import com.bluemarlin.drinkdiary.ui.component.DDTwoPaneContent
import com.example.ds.component.AuraAdaptiveGrid
import com.example.ds.component.AuraCard
import com.example.ds.component.AuraCardPadding
import com.example.ds.component.AuraCardVariant
import com.example.ds.component.AuraText
import com.example.ds.foundation.theme.AuraTheme

// 첫 질문 하나로 주종과 분류를 함께 받는다.
//
// 이 분류는 감각이 아니라 라벨·잔에서 바로 나오는 사실이고, 우리 측정에서 감각 축보다 술을
// 잘 나눴다(departments/planner/axis-validation-2026-08.md). 그래서 선택 태그가 아니라
// 기본 경로에 둔다.
//
// **탭은 늘지 않는다.** 원래도 주종을 고르는 화면이었고, 선택지만 세분화한 것이다 —
// F2의 5탭 예산(주종 1 + 공통 축 4)이 그대로 유지된다.
data class DrinkChoice(
    val type: DrinkType,
    val label: String,
    val tags: DrinkTags,
)

// 첫 화면이 이미 물은 태그. TagPicker는 이걸 빼고 보여준다 — 아니면 같은 것을 두 번 묻게 된다.
fun promotedTags(type: DrinkType): Set<TagCategory> =
    when (type) {
        DrinkType.Wine -> setOf(TagCategory.WineColor)
        DrinkType.Whiskey -> setOf(TagCategory.WhiskyStyle)
    }

private val wineChoices =
    WineColor.entries.map { color ->
        DrinkChoice(
            type = DrinkType.Wine,
            label =
                when (color) {
                    WineColor.Red -> "레드"
                    WineColor.White -> "화이트"
                    WineColor.Sparkling -> "스파클링"
                    WineColor.Other -> "그 외"
                },
            tags = DrinkTags(wineColor = color),
        )
    }

private val whiskeyChoices =
    WhiskyStyle.entries.map { style ->
        DrinkChoice(
            type = DrinkType.Whiskey,
            label =
                when (style) {
                    WhiskyStyle.SingleMalt -> "싱글몰트"
                    WhiskyStyle.Blended -> "블렌디드"
                    WhiskyStyle.Bourbon -> "버번"
                },
            tags = DrinkTags(whiskyStyle = style),
        )
        // "잘 모름"은 값이 아니라 태그 없음이다. 모르는 것을 사실로 저장하지 않는다.
    } + DrinkChoice(DrinkType.Whiskey, "잘 모름", DrinkTags())

@Composable
fun DrinkPicker(
    onPick: (DrinkChoice) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val layout = AuraTheme.layout
    if (layout.supportsTwoPane) {
        val groupColumns = if (layout.gridColumns >= 3) 2 else 1
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier.widthIn(max = layout.maxContentWidth).fillMaxSize(),
            ) {
                AuraText(
                    text = "무엇을 마셨나요?",
                    style = AuraTheme.typography.headlineSmall,
                    modifier =
                        Modifier
                            .padding(contentPadding)
                            .padding(
                                start = layout.screenMarginHorizontal,
                                end = layout.screenMarginHorizontal,
                                top = layout.screenMarginVertical,
                            ),
                )
                DDTwoPaneContent(
                    modifier = Modifier.weight(1f),
                    primaryPaneFraction = 0.5f,
                    primaryPane = {
                        ChoiceGroup(
                            title = "와인",
                            choices = wineChoices,
                            columns = groupColumns,
                            onPick = onPick,
                        )
                    },
                    secondaryPane = {
                        ChoiceGroup(
                            title = "위스키",
                            choices = whiskeyChoices,
                            columns = groupColumns,
                            onPick = onPick,
                        )
                    },
                )
            }
        }
    } else {
        DDCenteredContent(
            modifier = modifier,
            contentPadding = contentPadding,
        ) {
            AuraText("무엇을 마셨나요?", style = AuraTheme.typography.headlineSmall)

            ChoiceGroup(
                title = "와인",
                choices = wineChoices,
                columns = layout.gridColumns,
                onPick = onPick,
            )
            ChoiceGroup(
                title = "위스키",
                choices = whiskeyChoices,
                columns = layout.gridColumns,
                onPick = onPick,
            )
        }
    }
}

@Composable
private fun ChoiceGroup(
    title: String,
    choices: List<DrinkChoice>,
    columns: Int,
    onPick: (DrinkChoice) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AuraText(
            text = title,
            style = AuraTheme.typography.titleSmall,
            color = AuraTheme.colors.content.secondary,
        )
        AuraAdaptiveGrid(
            items = choices,
            modifier = Modifier.fillMaxWidth(),
            columns = columns,
        ) { choice ->
            AuraCard(
                onClick = { onPick(choice) },
                variant = AuraCardVariant.Outlined,
                padding = AuraCardPadding.None,
            ) {
                AuraText(
                    text = choice.label,
                    style = AuraTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp),
                )
            }
        }
    }
}
