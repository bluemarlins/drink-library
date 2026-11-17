package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.BottleEntry
import com.bluemarlin.drinkdiary.domain.model.BottleRecommendation
import com.bluemarlin.drinkdiary.domain.model.BuyRecommendationBundle
import com.bluemarlin.drinkdiary.domain.model.CaskGroup
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.LabelRecommendation
import com.bluemarlin.drinkdiary.domain.model.ProfileReadiness
import com.bluemarlin.drinkdiary.domain.model.RecommendationReason
import com.bluemarlin.drinkdiary.domain.model.TagCategory
import com.bluemarlin.drinkdiary.domain.model.TagPreference
import com.bluemarlin.drinkdiary.domain.model.TastePreference
import com.bluemarlin.drinkdiary.domain.model.TasteProfile
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.domain.model.WineColor
import com.bluemarlin.drinkdiary.domain.model.WineStyle
import com.bluemarlin.drinkdiary.domain.repository.BottleDictionary
import com.bluemarlin.drinkdiary.domain.repository.BottleMatcher
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveBuyRecommendationsUseCase(
    private val repository: DrinkRecordRepository,
    private val dictionary: BottleDictionary,
    private val observeTasteProfile: ObserveTasteProfileUseCase,
    private val observeTagPreference: ObserveTagPreferenceUseCase,
    private val resolveReadiness: ResolveProfileReadinessUseCase,
) {
    operator fun invoke(scope: TypeScope): Flow<BuyRecommendationBundle> {
        val drinkType =
            when (scope) {
                TypeScope.Wine -> DrinkType.Wine
                TypeScope.Whiskey -> DrinkType.Whiskey
                TypeScope.Combined -> null
            }

        return combine(
            repository.observeRecords(drinkType),
            observeTasteProfile(scope),
            observeTagPreference(scope),
        ) { records, profile, tagPrefs ->
            buildBundle(scope, records, profile, tagPrefs)
        }
    }

    private fun buildBundle(
        scope: TypeScope,
        records: List<DrinkRecord>,
        profile: TasteProfile,
        tagPrefs: List<TagPreference>,
    ): BuyRecommendationBundle {
        val repurchasePicks =
            records
                .asSequence()
                .filter { it.collectionStatus == CollectionStatus.Repurchase }
                .sortedWith(
                    compareByDescending<DrinkRecord> { it.rating }
                        .thenByDescending { it.recordedAtMillis },
                ).take(MAX_PICKS)
                .toList()

        val readiness = resolveReadiness(profile)
        if (readiness is ProfileReadiness.NotReady) {
            return BuyRecommendationBundle(
                hasEnoughData = false,
                recordsNeeded = readiness.recordsNeeded,
                repurchasePicks = repurchasePicks,
            )
        }

        val preferredTraits =
            profile.preferences.filter { pref ->
                pref.preference != null && pref.preference != TastePreference.Neutral
            }

        val preferredLabels =
            tagPrefs
                .filter { it.meaningfulGap }
                .mapNotNull { tagPref ->
                    val top =
                        tagPref.values.firstOrNull {
                            it.samples >= TagThresholds.MIN_SAMPLES_PER_VALUE
                        } ?: return@mapNotNull null
                    LabelRecommendation(
                        category = tagPref.category,
                        value = top.value,
                        averageRating = top.averageRating,
                        samples = top.samples,
                    )
                }

        val discoveryBottles =
            pickDiscoveryBottles(
                scope = scope,
                records = records,
                profile = profile,
                tagPrefs = tagPrefs,
            )

        return BuyRecommendationBundle(
            hasEnoughData = true,
            recordsNeeded = 0,
            preferredTraits = preferredTraits,
            preferredLabels = preferredLabels,
            discoveryBottles = discoveryBottles,
            repurchasePicks = repurchasePicks,
        )
    }

    private fun pickDiscoveryBottles(
        scope: TypeScope,
        records: List<DrinkRecord>,
        profile: TasteProfile,
        tagPrefs: List<TagPreference>,
    ): List<BottleRecommendation> {
        val normalizedDrunk =
            records
                .groupBy { it.type }
                .mapValues { (_, list) -> list.map { BottleMatcher.normalize(it.name) }.filter { it.isNotEmpty() } }

        val targetTypes =
            when (scope) {
                TypeScope.Wine -> setOf(DrinkType.Wine)
                TypeScope.Whiskey -> setOf(DrinkType.Whiskey)
                TypeScope.Combined -> setOf(DrinkType.Wine, DrinkType.Whiskey)
            }

        val caskPref = topTagFor(tagPrefs, TagCategory.Cask)
        val wineStylePref = topTagFor(tagPrefs, TagCategory.WineStyle)
        val wineColorPref = topTagFor(tagPrefs, TagCategory.WineColor)

        val candidates =
            dictionary
                .entries()
                .asSequence()
                .filter { it.type in targetTypes }
                .filterNot { entry -> alreadyDrunk(entry, normalizedDrunk[entry.type].orEmpty()) }
                .mapNotNull { entry ->
                    scoreBottle(
                        entry = entry,
                        profile = profile,
                        caskPref = caskPref,
                        wineStylePref = wineStylePref,
                        wineColorPref = wineColorPref,
                    )
                }.sortedByDescending { it.first }
                .map { it.second }
                .toList()

        return if (scope == TypeScope.Combined) {
            val wines = candidates.filter { it.entry.type == DrinkType.Wine }.take(2)
            val whiskeys = candidates.filter { it.entry.type == DrinkType.Whiskey }.take(2)
            (wines + whiskeys).take(MAX_PICKS)
        } else {
            candidates.take(MAX_PICKS)
        }
    }

    private fun alreadyDrunk(
        entry: BottleEntry,
        drunkNames: List<String>,
    ): Boolean {
        if (drunkNames.isEmpty()) return false
        val normalizedAliases = entry.aliases.map(BottleMatcher::normalize).filter { it.isNotEmpty() }
        return drunkNames.any { recordNorm ->
            normalizedAliases.any { alias ->
                recordNorm == alias || recordNorm.contains(alias) || alias.contains(recordNorm)
            }
        }
    }

    private fun topTagFor(
        tagPrefs: List<TagPreference>,
        category: TagCategory,
    ): Pair<String, Double>? {
        val pref = tagPrefs.firstOrNull { it.category == category && it.meaningfulGap } ?: return null
        val top = pref.values.firstOrNull { it.samples >= TagThresholds.MIN_SAMPLES_PER_VALUE } ?: return null
        return top.value to top.averageRating
    }

    private fun scoreBottle(
        entry: BottleEntry,
        profile: TasteProfile,
        caskPref: Pair<String, Double>?,
        wineStylePref: Pair<String, Double>?,
        wineColorPref: Pair<String, Double>?,
    ): Pair<Int, BottleRecommendation>? {
        val sweetness = profile.preference(Trait.Sweetness)?.preference
        val body = profile.preference(Trait.Body)?.preference
        val intensity = profile.preference(Trait.Intensity)?.preference
        val acidity = profile.preference(Trait.Acidity)?.preference
        val tannin = profile.preference(Trait.Tannin)?.preference

        if (entry.type == DrinkType.Whiskey) {
            val cask = entry.facts.cask
            if (cask != null && caskPref != null && cask.name == caskPref.first) {
                return 100 to
                    BottleRecommendation(
                        entry = entry,
                        reason = RecommendationReason.CaskMatch(cask, caskPref.second),
                    )
            }
            if (cask == CaskGroup.Sherry && sweetness == TastePreference.High) {
                return 60 to
                    BottleRecommendation(
                        entry = entry,
                        reason = RecommendationReason.TasteAlignedCask(cask),
                    )
            }
            if (cask == CaskGroup.Bourbon && body == TastePreference.Low) {
                return 50 to
                    BottleRecommendation(
                        entry = entry,
                        reason = RecommendationReason.TasteAlignedCask(cask),
                    )
            }
            if (sweetness == TastePreference.Neutral && body == TastePreference.Neutral && caskPref == null) {
                return 10 to BottleRecommendation(entry = entry, reason = RecommendationReason.GeneralCatalogMatch)
            }
            return null
        }

        val style = entry.facts.wineStyle ?: return null
        if (wineStylePref != null && style.name == wineStylePref.first) {
            return 100 to
                BottleRecommendation(
                    entry = entry,
                    reason = RecommendationReason.WineStyleMatch(style, wineStylePref.second),
                )
        }

        val prefersRed = wineColorPref?.first == WineColor.Red.name
        val prefersWhite = wineColorPref?.first == WineColor.White.name

        val score =
            when (style) {
                WineStyle.FullRed ->
                    when {
                        prefersWhite -> 0
                        body == TastePreference.High &&
                            (tannin == TastePreference.High || sweetness != TastePreference.High) -> 80
                        body == TastePreference.High || intensity == TastePreference.High -> 65
                        else -> 0
                    }

                WineStyle.LightRed ->
                    when {
                        prefersWhite -> 0
                        body == TastePreference.Low && (tannin == TastePreference.Low || prefersRed) -> 80
                        body == TastePreference.Low && acidity == TastePreference.High -> 70
                        else -> 0
                    }

                WineStyle.DryWhite ->
                    when {
                        prefersRed -> 0
                        sweetness == TastePreference.Low && (acidity == TastePreference.High || prefersWhite) -> 80
                        sweetness == TastePreference.Low && body == TastePreference.Low -> 65
                        else -> 0
                    }

                WineStyle.AromaticWhite ->
                    when {
                        prefersRed -> 0
                        sweetness == TastePreference.High && (intensity == TastePreference.High || prefersWhite) -> 80
                        sweetness == TastePreference.High -> 65
                        else -> 0
                    }
            }

        if (score > 0) {
            return score to
                BottleRecommendation(
                    entry = entry,
                    reason = RecommendationReason.TasteAlignedStyle(style),
                )
        }

        val allNeutral =
            listOf(sweetness, body, intensity).all { it == TastePreference.Neutral } && wineColorPref == null
        return if (allNeutral) {
            10 to BottleRecommendation(entry = entry, reason = RecommendationReason.GeneralCatalogMatch)
        } else {
            null
        }
    }

    companion object {
        private const val MAX_PICKS = 4
    }
}
