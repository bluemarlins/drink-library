package com.bluemarlin.drinkdiary.ui.lookup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bluemarlin.drinkdiary.domain.model.BottleEntry
import com.bluemarlin.drinkdiary.domain.model.BuyRecommendationBundle
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.domain.repository.BottleDictionary
import com.bluemarlin.drinkdiary.domain.repository.BottleMatcher
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import com.bluemarlin.drinkdiary.domain.usecase.ObserveBuyRecommendationsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class LookupUiState(
    val query: String = "",
    val matchedRecords: List<DrinkRecord> = emptyList(),
    val matchedCatalogBottles: List<BottleEntry> = emptyList(),
    val repurchaseCandidates: List<DrinkRecord> = emptyList(),
    val combinedRecommendations: BuyRecommendationBundle = BuyRecommendationBundle(),
)

class LookupViewModel(
    private val repository: DrinkRecordRepository,
    private val dictionary: BottleDictionary,
    private val observeRecommendations: ObserveBuyRecommendationsUseCase,
) : ViewModel() {
    private val query = MutableStateFlow("")

    val uiState: StateFlow<LookupUiState> =
        combine(
            query,
            repository.observeRecords(null),
            observeRecommendations(TypeScope.Combined),
        ) { q, records, bundle ->
            val trimmed = q.trim()
            val repurchases =
                records
                    .filter { it.collectionStatus == CollectionStatus.Repurchase }
                    .sortedWith(
                        compareByDescending<DrinkRecord> { it.rating }
                            .thenByDescending { it.recordedAtMillis },
                    )

            if (trimmed.isEmpty()) {
                LookupUiState(
                    query = q,
                    matchedRecords = emptyList(),
                    matchedCatalogBottles = emptyList(),
                    repurchaseCandidates = repurchases,
                    combinedRecommendations = bundle,
                )
            } else {
                val lower = trimmed.lowercase()
                val matchedRecords =
                    records
                        .filter { record ->
                            record.name.lowercase().contains(lower) ||
                                record.place?.lowercase()?.contains(lower) == true ||
                                record.memo?.lowercase()?.contains(lower) == true ||
                                record.vintage?.toString()?.contains(lower) == true
                        }.sortedWith(
                            compareByDescending<DrinkRecord> { it.collectionStatus == CollectionStatus.Repurchase }
                                .thenByDescending { it.rating }
                                .thenByDescending { it.recordedAtMillis },
                        )

                val normalizedNeedle = BottleMatcher.normalize(trimmed)
                val existingNormalized = records.map { BottleMatcher.normalize(it.name) }
                val catalogMatches =
                    if (normalizedNeedle.length < 2) {
                        emptyList()
                    } else {
                        dictionary
                            .entries()
                            .filter { entry ->
                                val aliases = entry.aliases.map(BottleMatcher::normalize)
                                val hitsQuery =
                                    aliases.any {
                                        it.contains(normalizedNeedle) ||
                                            normalizedNeedle.contains(it)
                                    }
                                val notRecorded =
                                    existingNormalized.none { drunk ->
                                        aliases.any { alias -> drunk == alias || drunk.contains(alias) }
                                    }
                                hitsQuery && notRecorded
                            }.take(6)
                    }

                LookupUiState(
                    query = q,
                    matchedRecords = matchedRecords,
                    matchedCatalogBottles = catalogMatches,
                    repurchaseCandidates = repurchases,
                    combinedRecommendations = bundle,
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LookupUiState())

    fun updateQuery(newQuery: String) {
        query.value = newQuery
    }

    class Factory(
        private val repository: DrinkRecordRepository,
        private val dictionary: BottleDictionary,
        private val observeRecommendations: ObserveBuyRecommendationsUseCase,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LookupViewModel(repository, dictionary, observeRecommendations) as T
    }
}
