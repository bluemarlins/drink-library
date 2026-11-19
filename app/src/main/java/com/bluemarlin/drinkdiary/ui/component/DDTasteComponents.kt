package com.bluemarlin.drinkdiary.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.BottleRecommendation
import com.bluemarlin.drinkdiary.domain.model.BuyRecommendationBundle
import com.bluemarlin.drinkdiary.domain.model.DashboardOverview
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.TastePreference
import com.bluemarlin.drinkdiary.domain.model.TraitPreference
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.profile.TraitStatus
import com.bluemarlin.drinkdiary.ui.profile.recordsNeeded
import com.bluemarlin.drinkdiary.ui.profile.traitStatus
import com.example.ds.component.AuraBadge
import com.example.ds.component.AuraBadgeVariant
import com.example.ds.component.AuraCard
import com.example.ds.component.AuraCardPadding
import com.example.ds.component.AuraCardVariant
import com.example.ds.component.AuraText
import com.example.ds.foundation.theme.AuraTheme

@Composable
fun DDDashboardSummaryCard(
    overview: DashboardOverview,
    onOpenCollection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AuraCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onOpenCollection,
        variant = AuraCardVariant.Elevated,
        padding = AuraCardPadding.Large,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AuraText("내 음주일기", style = AuraTheme.typography.titleMedium)
                AuraBadge(
                    label = "총 ${overview.totalCount}잔",
                    variant = AuraBadgeVariant.Brand,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SummaryStatCell(
                    label = "주종 구성",
                    value = "와인 ${overview.wineCount} · 위스키 ${overview.whiskeyCount}",
                    modifier = Modifier.weight(1f),
                )
                SummaryStatCell(
                    label = "평균 별점",
                    value = overview.averageRating?.let { DrinkLabels.averageRatingText(it) } ?: "-",
                    modifier = Modifier.weight(0.75f),
                )
                SummaryStatCell(
                    label = "또 살래요 ★",
                    value = "${overview.repurchaseCount}잔",
                    modifier = Modifier.weight(0.75f),
                )
            }
        }
    }
}

@Composable
private fun SummaryStatCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        AuraText(
            text = label,
            style = AuraTheme.typography.labelSmall,
            color = AuraTheme.colors.content.secondary,
        )
        AuraText(
            text = value,
            style = AuraTheme.typography.titleSmall,
            color = AuraTheme.colors.content.primary,
        )
    }
}

@Composable
fun DDTraitSpectrumRow(
    pref: TraitPreference,
    modifier: Modifier = Modifier,
) {
    val status = traitStatus(pref)
    val poles = DrinkLabels.poles(pref.trait)
    val codes = DrinkLabels.axisCodes(pref.trait)
    val letter = DrinkLabels.axisLetter(pref.trait, pref.preference)
    val leftLabel = if (codes != null) "${poles.first} (${codes.first})" else poles.first
    val rightLabel = if (codes != null) "${poles.second} (${codes.second})" else poles.second

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AuraText(DrinkLabels.trait(pref.trait), style = AuraTheme.typography.bodyLarge)
                if (!pref.trait.shared) {
                    AuraBadge(label = DrinkLabels.exclusiveTo(pref.trait), variant = AuraBadgeVariant.Neutral)
                }
            }

            when (status) {
                TraitStatus.Resolved -> {
                    val label =
                        if (letter != null) {
                            "$letter · ${DrinkLabels.preference(pref.trait, pref.preference!!)}"
                        } else {
                            DrinkLabels.preference(pref.trait, pref.preference!!)
                        }
                    AuraBadge(label = label, variant = AuraBadgeVariant.Brand)
                }

                TraitStatus.Neutral ->
                    AuraText(
                        text = if (letter != null) "$letter · 크게 가리지 않음" else "크게 가리지 않음",
                        style = AuraTheme.typography.bodySmall,
                        color = AuraTheme.colors.content.secondary,
                        textAlign = TextAlign.End,
                    )

                TraitStatus.NeedsRecords ->
                    AuraText(
                        text = "${recordsNeeded(pref)}잔 더 필요",
                        style = AuraTheme.typography.bodySmall,
                        color = AuraTheme.colors.content.secondary,
                        textAlign = TextAlign.End,
                    )
            }
        }

        val markerFraction: Float? =
            when (pref.preference) {
                TastePreference.High ->
                    (
                        0.5f + pref.strength
                            .coerceIn(
                                0.35,
                                1.0,
                            ).toFloat() * 0.44f
                    ).coerceIn(0.55f, 0.96f)
                TastePreference.Low ->
                    (
                        0.5f - pref.strength
                            .coerceIn(
                                0.35,
                                1.0,
                            ).toFloat() * 0.44f
                    ).coerceIn(0.04f, 0.45f)
                TastePreference.Neutral -> 0.5f
                null -> null
            }

        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth().height(16.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            val trackWidth = maxWidth
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(AuraTheme.shapes.small)
                        .background(AuraTheme.colors.surface.sunken),
            )
            // 중앙 균형 눈금
            Box(
                modifier =
                    Modifier
                        .offset(x = trackWidth * 0.5f - 1.dp)
                        .width(2.dp)
                        .height(10.dp)
                        .background(AuraTheme.colors.border.strong),
            )
            if (markerFraction != null) {
                val dotSize = 14.dp
                val dotColor =
                    if (pref.preference == TastePreference.Neutral) {
                        AuraTheme.colors.content.secondary
                    } else {
                        AuraTheme.colors.brand.primary
                    }
                Box(
                    modifier =
                        Modifier
                            .offset(x = (trackWidth - dotSize) * markerFraction)
                            .size(dotSize)
                            .clip(CircleShape)
                            .background(dotColor),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            AuraText(
                text = leftLabel,
                style = AuraTheme.typography.labelSmall,
                color =
                    if (pref.preference == TastePreference.Low) {
                        AuraTheme.colors.brand.primary
                    } else {
                        AuraTheme.colors.content.secondary
                    },
            )
            AuraText(
                text = rightLabel,
                style = AuraTheme.typography.labelSmall,
                color =
                    if (pref.preference == TastePreference.High) {
                        AuraTheme.colors.brand.primary
                    } else {
                        AuraTheme.colors.content.secondary
                    },
                textAlign = TextAlign.End,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DDBuyRecommendationSection(
    scope: TypeScope,
    bundle: BuyRecommendationBundle,
    onOpenRecord: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AuraText("앞으로 구매할 술 추천", style = AuraTheme.typography.titleMedium)

        if (!bundle.hasEnoughData) {
            AuraCard(
                modifier = Modifier.fillMaxWidth(),
                variant = AuraCardVariant.Outlined,
                padding = AuraCardPadding.Medium,
            ) {
                AuraText(
                    text = "${bundle.recordsNeeded}잔만 더 남기면 내 취향과 맞는 다음 술 스타일을 추천해 드려요.",
                    style = AuraTheme.typography.bodyMedium,
                    color = AuraTheme.colors.content.secondary,
                )
            }
        } else {
            val scopeDrinkType =
                when (scope) {
                    TypeScope.Wine -> com.bluemarlin.drinkdiary.domain.model.DrinkType.Wine
                    TypeScope.Whiskey -> com.bluemarlin.drinkdiary.domain.model.DrinkType.Whiskey
                    TypeScope.Combined -> null
                }

            val styleChips =
                bundle.preferredTraits.map {
                    "${DrinkLabels.trait(it.trait)}: ${DrinkLabels.preference(it.trait, it.preference!!)}"
                } +
                    bundle.preferredLabels.map {
                        "${DrinkLabels.tagCategory(it.category)}: ${
                            DrinkLabels.tagValue(it.category, it.value, scopeDrinkType)
                        }"
                    }

            if (styleChips.isNotEmpty()) {
                AuraCard(
                    modifier = Modifier.fillMaxWidth(),
                    variant = AuraCardVariant.Outlined,
                    padding = AuraCardPadding.Medium,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AuraText("매장에서 이런 기준을 고르세요", style = AuraTheme.typography.titleSmall)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            styleChips.forEach { chip ->
                                AuraBadge(label = chip, variant = AuraBadgeVariant.Brand)
                            }
                        }
                    }
                }
            }

            if (bundle.discoveryBottles.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AuraText("아직 안 마셔본 추천 종류", style = AuraTheme.typography.titleSmall)
                    // 이유가 모두 같으면 카드마다 되풀이하지 않고 한 번만 말한다.
                    val sharedReason =
                        bundle.discoveryBottles
                            .map { it.reason }
                            .distinct()
                            .singleOrNull()
                    sharedReason?.let {
                        AuraText(
                            text = "추천 이유: ${DrinkLabels.recommendationReason(it)}",
                            style = AuraTheme.typography.bodySmall,
                            color = AuraTheme.colors.content.secondary,
                        )
                    }
                    bundle.discoveryBottles.forEach { rec ->
                        RecommendedBottleCard(rec, showReason = sharedReason == null)
                    }
                }
            }
        }

        if (bundle.repurchasePicks.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AuraText("또 살래요라고 남긴 술", style = AuraTheme.typography.titleSmall)
                bundle.repurchasePicks.forEach { record ->
                    RepurchasePickCard(record = record, onClick = { onOpenRecord(record.id) })
                }
            }
        }
    }
}

@Composable
private fun RecommendedBottleCard(
    rec: BottleRecommendation,
    showReason: Boolean,
) {
    AuraCard(
        modifier = Modifier.fillMaxWidth(),
        variant = AuraCardVariant.Outlined,
        padding = AuraCardPadding.Medium,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AuraText(
                    text = rec.entry.displayName,
                    style = AuraTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                )
                DDDrinkBadge(drinkType = rec.entry.type)
            }
            AuraText(
                text =
                    listOfNotNull(
                        DrinkLabels.bottleFactSummary(rec.entry),
                        DrinkLabels.recommendationReason(rec.reason).takeIf { showReason },
                    ).joinToString(" · "),
                style = AuraTheme.typography.bodySmall,
                color = AuraTheme.colors.content.secondary,
            )
        }
    }
}

@Composable
private fun RepurchasePickCard(
    record: DrinkRecord,
    onClick: () -> Unit,
) {
    AuraCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        variant = AuraCardVariant.Outlined,
        padding = AuraCardPadding.Medium,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                AuraText(record.name, style = AuraTheme.typography.titleSmall, maxLines = 1)
                AuraText(
                    text = DrinkLabels.subtitle(record),
                    style = AuraTheme.typography.bodySmall,
                    color = AuraTheme.colors.content.secondary,
                    maxLines = 1,
                )
            }
            AuraBadge(
                label = DrinkLabels.ratingText(record.rating),
                variant = AuraBadgeVariant.Success,
            )
        }
    }
}
