package com.bluemarlin.drinkdiary.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.ui.theme.DDShape
import com.example.ds.component.AuraBadge
import com.example.ds.component.AuraBadgeVariant
import com.example.ds.component.AuraCard
import com.example.ds.component.AuraCardPadding
import com.example.ds.component.AuraCardVariant
import com.example.ds.component.AuraText
import com.example.ds.foundation.theme.AuraTheme

@Composable
fun DDTasteTypeBadge(
    code: String,
    name: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AuraText(
            text = code,
            style = AuraTheme.typography.displayMedium,
            color = AuraTheme.colors.brand.primary,
        )
        AuraBadge(
            label = name,
            variant = AuraBadgeVariant.Neutral,
        )
    }
}

@Composable
fun DDTasteSentenceCard(
    sentence: String,
    modifier: Modifier = Modifier,
    details: List<String> = emptyList(),
) {
    AuraCard(
        modifier = modifier.fillMaxWidth(),
        variant = AuraCardVariant.Outlined,
        padding = AuraCardPadding.Large,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AuraText(
                text = sentence,
                style = AuraTheme.typography.headlineMedium,
                color = AuraTheme.colors.content.primary,
            )

            if (details.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    details.forEach { detail ->
                        AuraText(
                            text = "• $detail",
                            style = AuraTheme.typography.bodyMedium,
                            color = AuraTheme.colors.content.secondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DDProfileProgressCard(
    title: String,
    description: String,
    currentCount: Int,
    targetCount: Int,
    modifier: Modifier = Modifier,
) {
    AuraCard(
        modifier = modifier.fillMaxWidth(),
        variant = AuraCardVariant.Outlined,
        padding = AuraCardPadding.Large,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AuraText(
                text = title,
                style = AuraTheme.typography.titleMedium,
                color = AuraTheme.colors.content.primary,
            )
            AuraText(
                text = description,
                style = AuraTheme.typography.bodyMedium,
                color = AuraTheme.colors.content.secondary,
            )

            val safeTarget = targetCount.coerceAtLeast(1)
            DDProgressBar(fraction = (currentCount.toFloat() / safeTarget).coerceIn(0f, 1f))
        }
    }
}

// Aura 1.0.0에 LinearProgressIndicator / HorizontalDivider가 없어 토큰 기반 로컬 Gap 컴포넌트를 유지한다.
@Composable
fun DDProgressBar(
    fraction: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(DDShape.small)
                .background(AuraTheme.colors.surface.sunken),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .height(6.dp)
                    .clip(DDShape.small)
                    .background(AuraTheme.colors.brand.primary),
        )
    }
}

@Composable
fun DDDivider(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(AuraTheme.colors.border.subtle),
    )
}
