package com.bluemarlin.drinkdiary.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import io.github.bluemarlins.aura.component.AuraBadge
import io.github.bluemarlins.aura.component.AuraBadgeSize
import io.github.bluemarlins.aura.component.AuraBadgeVariant

@Composable
fun DDDrinkBadge(
    drinkType: DrinkType,
    label: String? = null,
    modifier: Modifier = Modifier,
) {
    // 주종 분류는 상태(Error/Warning)가 아닌 카테고리이므로 FBK-RULE-001에 따라 Brand/Neutral을 사용한다.
    val (variant, defaultLabel) =
        when (drinkType) {
            DrinkType.Wine -> AuraBadgeVariant.Brand to "와인"
            DrinkType.Whiskey -> AuraBadgeVariant.Neutral to "위스키"
        }

    val displayLabel = if (label.isNullOrBlank()) defaultLabel else "$defaultLabel · $label"

    AuraBadge(
        modifier = modifier,
        label = displayLabel,
        variant = variant,
    )
}

@Composable
fun DDRepurchaseBadge(
    modifier: Modifier = Modifier,
    text: String = "또 살래요",
    size: AuraBadgeSize = AuraBadgeSize.Medium,
) {
    AuraBadge(
        modifier = modifier,
        label = text,
        variant = AuraBadgeVariant.Success,
        size = size,
    )
}
