package com.bluemarlin.drinkdiary.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.example.ds.component.AuraBadge
import com.example.ds.component.AuraBadgeVariant

@Composable
fun DDDrinkBadge(
    drinkType: DrinkType,
    label: String? = null,
    modifier: Modifier = Modifier,
) {
    val (variant, defaultLabel) =
        when (drinkType) {
            DrinkType.Wine -> AuraBadgeVariant.Error to "와인"
            DrinkType.Whiskey -> AuraBadgeVariant.Warning to "위스키"
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
    text: String = "★ 다시 살래요",
) {
    AuraBadge(
        modifier = modifier,
        label = text,
        variant = AuraBadgeVariant.Brand,
    )
}
