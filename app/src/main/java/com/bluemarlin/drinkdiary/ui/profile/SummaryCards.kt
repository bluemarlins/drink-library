package com.bluemarlin.drinkdiary.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.BuyRecommendationBundle
import com.bluemarlin.drinkdiary.domain.model.DashboardOverview
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.FrequentDrink
import com.bluemarlin.drinkdiary.domain.model.MonthSummary
import com.bluemarlin.drinkdiary.domain.model.ProfileReadiness
import com.bluemarlin.drinkdiary.domain.model.TagPreference
import com.bluemarlin.drinkdiary.domain.model.TastePreference
import com.bluemarlin.drinkdiary.domain.model.TraitPreference
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.component.DDUriImage
import io.github.bluemarlins.aura.component.AuraButton
import io.github.bluemarlins.aura.component.AuraButtonSize
import io.github.bluemarlins.aura.component.AuraButtonVariant
import io.github.bluemarlins.aura.component.AuraCard
import io.github.bluemarlins.aura.component.AuraCardPadding
import io.github.bluemarlins.aura.component.AuraCardVariant
import io.github.bluemarlins.aura.component.AuraLinearProgressBar
import io.github.bluemarlins.aura.component.AuraText
import io.github.bluemarlins.aura.foundation.theme.AuraTheme

// 요약 탭의 벤토 카드들. 카드마다 한 가지만 말하고, 자세한 건 누르면 열리는 화면이 말한다.
// 카드 이름은 branding.md 7-2 용어집을 따른다.

@Composable
private fun SummaryCard(
    label: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    variant: AuraCardVariant = AuraCardVariant.Outlined,
    content: @Composable () -> Unit,
) {
    AuraCard(
        modifier = modifier.fillMaxSize(),
        onClick = onClick,
        variant = variant,
        padding = AuraCardPadding.Medium,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AuraText(label, style = AuraTheme.typography.labelLarge, color = AuraTheme.colors.content.secondary)
            content()
        }
    }
}

@Composable
private fun EmptyLine(text: String) {
    AuraText(text, style = AuraTheme.typography.bodyMedium, color = AuraTheme.colors.content.secondary)
}

@Composable
fun MonthCard(month: MonthSummary) {
    SummaryCard(label = "이번 달 · ${month.month}월", variant = AuraCardVariant.Elevated) {
        if (month.count == 0) {
            EmptyLine("이번 달은 아직 기록이 없어요.")
            return@SummaryCard
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AuraText("${month.count}잔", style = AuraTheme.typography.headlineLarge)
                AuraText(
                    text =
                        listOfNotNull(
                            "와인 ${month.wineCount} · 위스키 ${month.whiskeyCount}",
                            month.averageRating?.let { "평균 별점 ${DrinkLabels.averageRatingText(it)}" },
                        ).joinToString("\n"),
                    style = AuraTheme.typography.bodySmall,
                    color = AuraTheme.colors.content.secondary,
                )
            }
            WeeklyBars(counts = month.weeklyCounts)
        }
    }
}

// 주별 막대. 가장 많이 마신 주가 가장 높다. 숫자 대신 "1주…5주" 라벨만 단다.
@Composable
private fun WeeklyBars(counts: List<Int>) {
    val peak = counts.maxOrNull()?.coerceAtLeast(1) ?: 1
    val barMaxHeight = 56.dp
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
        counts.forEachIndexed { index, count ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .width(14.dp)
                            .height((barMaxHeight * (count.toFloat() / peak)).coerceAtLeast(4.dp))
                            .clip(AuraTheme.shapes.small)
                            .background(
                                if (count == peak && count > 0) {
                                    AuraTheme.colors.brand.primary
                                } else {
                                    AuraTheme.colors.brand.subtle
                                },
                            ),
                )
                AuraText(
                    "${index + 1}주",
                    style = AuraTheme.typography.labelSmall,
                    color = AuraTheme.colors.content.secondary,
                )
            }
        }
    }
}

@Composable
fun LatestCard(
    record: DrinkRecord?,
    onOpen: (Long) -> Unit,
) {
    SummaryCard(label = "최근에 마신 술", onClick = record?.let { { onOpen(it.id) } }) {
        if (record == null) {
            EmptyLine("첫 잔을 기록해 보세요.")
            return@SummaryCard
        }
        record.imageUri?.let {
            DDUriImage(
                imageUri = it,
                contentDescription = null,
                modifier = Modifier.size(48.dp).clip(AuraTheme.shapes.small),
            )
        }
        AuraText(record.name, style = AuraTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        AuraText(
            "${DrinkLabels.date(record.recordedAtMillis)} · ${DrinkLabels.ratingText(record.rating)}",
            style = AuraTheme.typography.bodySmall,
            color = AuraTheme.colors.content.secondary,
        )
    }
}

@Composable
fun FrequentCard(
    frequent: FrequentDrink?,
    onOpen: (Long) -> Unit,
) {
    SummaryCard(label = "가장 자주 마신 술", onClick = frequent?.let { { onOpen(it.latestRecordId) } }) {
        if (frequent == null) {
            EmptyLine("두 번 마신 술이 아직 없어요.")
            return@SummaryCard
        }
        AuraText(
            frequent.name,
            style = AuraTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        AuraText(
            "${frequent.count}번 마셨어요",
            style = AuraTheme.typography.bodySmall,
            color = AuraTheme.colors.content.secondary,
        )
    }
}

@Composable
fun TotalCard(
    overview: DashboardOverview,
    onOpenLog: () -> Unit,
) {
    SummaryCard(label = "누적", onClick = onOpenLog) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Stat("모두", "${overview.totalCount}잔", Modifier.weight(1f))
            Stat("평균 별점", overview.averageRating?.let { DrinkLabels.averageRatingText(it) } ?: "-", Modifier.weight(1f))
            Stat("또 살래요", "${overview.repurchaseCount}잔", Modifier.weight(1f))
        }
    }
}

@Composable
private fun Stat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        AuraText(value, style = AuraTheme.typography.titleLarge)
        AuraText(label, style = AuraTheme.typography.labelSmall, color = AuraTheme.colors.content.secondary)
    }
}

@Composable
fun TasteTypeCard(
    readiness: ProfileReadiness,
    recordCount: Int,
    onShare: () -> Unit,
) {
    when (readiness) {
        is ProfileReadiness.Ready ->
            SummaryCard(label = "취향 유형", variant = AuraCardVariant.Elevated) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AuraText(
                        readiness.type.code,
                        style = AuraTheme.typography.headlineLarge,
                        color = AuraTheme.colors.brand.primary,
                    )
                    AuraText(TasteTypeCopy.shortName(readiness.type), style = AuraTheme.typography.titleMedium)
                }
                AuraText(TasteTypeCopy.sentence(readiness.type), style = AuraTheme.typography.bodyMedium)
                AuraText(
                    "${TasteTypeCopy.legend(readiness.type)}\n${recordCount}잔을 바탕으로 판정했어요.",
                    style = AuraTheme.typography.bodySmall,
                    color = AuraTheme.colors.content.secondary,
                )
                AuraButton(
                    text = "취향 카드 공유하기",
                    onClick = onShare,
                    variant = AuraButtonVariant.Outline,
                    size = AuraButtonSize.Medium,
                    fullWidth = true,
                )
            }

        is ProfileReadiness.NotReady -> {
            val target = recordCount + readiness.recordsNeeded
            SummaryCard(label = "취향 유형") {
                AuraText(
                    text =
                        if (recordCount == 0) {
                            "첫 기록을 남기면 여기서 취향이 보이기 시작해요."
                        } else {
                            "${readiness.recordsNeeded}잔만 더 기록하면 취향 유형이 나와요."
                        },
                    style = AuraTheme.typography.bodyMedium,
                )
                AuraLinearProgressBar(
                    progress = if (target == 0) 0f else recordCount.toFloat() / target,
                    modifier = Modifier.fillMaxWidth(),
                )
                AuraText(
                    "$recordCount / ${target}잔",
                    style = AuraTheme.typography.labelSmall,
                    color = AuraTheme.colors.content.secondary,
                )
            }
        }
    }
}

// 방향이 판정된 맛 기준 중 뚜렷한 것부터 셋. 중립은 "가리지 않음"이라 여기서는 빼고 문장 카드에 맡긴다.
internal fun tasteHighlights(preferences: List<TraitPreference>): List<TraitPreference> =
    preferences
        .filter { it.preference == TastePreference.High || it.preference == TastePreference.Low }
        .sortedByDescending { it.strength }
        .take(3)

@Composable
fun TraitCard(preferences: List<TraitPreference>) {
    SummaryCard(label = "맛 기준") {
        val picked = tasteHighlights(preferences)
        if (picked.isEmpty()) {
            EmptyLine(
                if (preferences.any { it.evaluated }) "크게 가리는 맛이 없어요." else "기록이 쌓이면 보여요.",
            )
            return@SummaryCard
        }
        picked.forEach { pref ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AuraText(
                    "${DrinkLabels.trait(pref.trait)} · ${DrinkLabels.preference(pref.trait, pref.preference!!)}",
                    style = AuraTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                PoleBar(pref)
            }
        }
    }
}

// 가운데에서 선호 쪽으로 뻗는 짧은 막대. 뚜렷할수록 길다.
@Composable
private fun PoleBar(pref: TraitPreference) {
    val reach = pref.strength.coerceIn(0.2, 1.0).toFloat()
    val toHigh = pref.preference == TastePreference.High
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(AuraTheme.shapes.small)
                .background(AuraTheme.colors.surface.sunken),
    ) {
        val filled: @Composable () -> Unit = {
            Box(
                modifier =
                    Modifier
                        .weight(reach)
                        .fillMaxHeight()
                        .background(AuraTheme.colors.brand.primary),
            )
        }
        val empty: @Composable () -> Unit = { Spacer(modifier = Modifier.weight(1f - reach + 0.0001f)) }
        val half: @Composable () -> Unit = { Spacer(modifier = Modifier.weight(1f)) }
        // 왼쪽 절반은 낮은 극, 오른쪽 절반은 높은 극이다. 막대는 가운데에서 선호 쪽으로만 뻗는다.
        if (toHigh) {
            half()
            filled()
            empty()
        } else {
            empty()
            filled()
            half()
        }
    }
}

// 별점 차이가 말할 만한 라벨 중 가장 별점이 높은 값. 차이가 없으면 단정하지 않는다.
internal fun bestLabel(tagPreferences: List<TagPreference>): TagPreference? =
    tagPreferences
        .filter { it.meaningfulGap && it.best != null }
        .maxByOrNull { it.best!!.averageRating }

@Composable
fun LabelCard(
    tagPreferences: List<TagPreference>,
    scope: TypeScope,
) {
    SummaryCard(label = "라벨로 본 취향") {
        val best = bestLabel(tagPreferences)
        if (best == null) {
            EmptyLine(if (tagPreferences.isEmpty()) "라벨 정보를 남기면 보여요." else "아직 차이가 뚜렷하지 않아요.")
            return@SummaryCard
        }
        val value = best.best!!
        val drinkType =
            when (scope) {
                TypeScope.Wine -> DrinkType.Wine
                TypeScope.Whiskey -> DrinkType.Whiskey
                TypeScope.Combined -> null
            }
        AuraText(
            DrinkLabels.tagValue(best.category, value.value, drinkType),
            style = AuraTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        AuraText(
            "${DrinkLabels.tagCategory(
                best.category,
            )} · 평균 ${DrinkLabels.averageRatingText(value.averageRating)} · ${value.samples}잔",
            style = AuraTheme.typography.bodySmall,
            color = AuraTheme.colors.content.secondary,
        )
    }
}

@Composable
fun NextBottleCard(
    bundle: BuyRecommendationBundle,
    onOpenRecord: (Long) -> Unit,
) {
    val discovery = bundle.discoveryBottles.firstOrNull()
    val repurchase = bundle.repurchasePicks.firstOrNull()
    SummaryCard(
        label = "다음 한 병",
        onClick = if (discovery == null && repurchase != null) ({ onOpenRecord(repurchase.id) }) else null,
    ) {
        when {
            !bundle.hasEnoughData ->
                EmptyLine("${bundle.recordsNeeded}잔만 더 기록하면 내 취향에 맞는 술을 추천해 드려요.")

            discovery != null -> {
                AuraText(discovery.entry.displayName, style = AuraTheme.typography.titleMedium)
                AuraText(
                    "${DrinkLabels.bottleFactSummary(
                        discovery.entry,
                    )}\n${DrinkLabels.recommendationReason(discovery.reason)}",
                    style = AuraTheme.typography.bodySmall,
                    color = AuraTheme.colors.content.secondary,
                )
            }

            repurchase != null -> {
                AuraText(repurchase.name, style = AuraTheme.typography.titleMedium)
                AuraText(
                    "또 살래요라고 남긴 술 · ${DrinkLabels.ratingText(repurchase.rating)}",
                    style = AuraTheme.typography.bodySmall,
                    color = AuraTheme.colors.content.secondary,
                )
            }

            else -> EmptyLine("지금은 추천할 술이 없어요.")
        }
    }
}
