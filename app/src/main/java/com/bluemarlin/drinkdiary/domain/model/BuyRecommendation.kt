package com.bluemarlin.drinkdiary.domain.model

data class BottleRecommendation(
    val entry: BottleEntry,
    val reason: RecommendationReason,
)

sealed interface RecommendationReason {
    data class CaskMatch(
        val cask: CaskGroup,
        val averageRating: Double,
    ) : RecommendationReason

    data class WineStyleMatch(
        val style: WineStyle,
        val averageRating: Double,
    ) : RecommendationReason

    data class TasteAlignedStyle(
        val style: WineStyle,
    ) : RecommendationReason

    data object GeneralCatalogMatch : RecommendationReason
}

data class LabelRecommendation(
    val category: TagCategory,
    val value: String,
    val averageRating: Double,
    val samples: Int,
)

data class BuyRecommendationBundle(
    val hasEnoughData: Boolean = false,
    val recordsNeeded: Int = 0,
    val preferredTraits: List<TraitPreference> = emptyList(),
    val preferredLabels: List<LabelRecommendation> = emptyList(),
    val discoveryBottles: List<BottleRecommendation> = emptyList(),
    val repurchasePicks: List<DrinkRecord> = emptyList(),
)
