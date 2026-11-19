package com.bluemarlin.drinkdiary.ui.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CollectionUiState(
    val filter: DrinkType? = null,
    val statusFilter: CollectionStatus? = null,
    val query: String = "",
    val sortByRating: Boolean = false,
    val allRecords: List<DrinkRecord> = emptyList(),
    val records: List<DrinkRecord> = emptyList(),
    val loaded: Boolean = false,
    val error: String? = null,
)

private data class FilterCriteria(
    val type: DrinkType? = null,
    val status: CollectionStatus? = null,
    val query: String = "",
    val sortByRating: Boolean = false,
)

class CollectionViewModel(
    private val repository: DrinkRecordRepository,
) : ViewModel() {
    private val criteria = MutableStateFlow(FilterCriteria())
    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CollectionUiState> =
        combine(
            repository.observeRecords(null),
            criteria,
            error,
        ) { allRecords, activeCriteria, message ->
            val filtered =
                filterAndSort(
                    records = allRecords,
                    criteria = activeCriteria,
                )
            CollectionUiState(
                filter = activeCriteria.type,
                statusFilter = activeCriteria.status,
                query = activeCriteria.query,
                sortByRating = activeCriteria.sortByRating,
                allRecords = allRecords,
                records = filtered,
                loaded = true,
                error = message,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CollectionUiState())

    fun selectFilter(type: DrinkType?) {
        criteria.update { it.copy(type = type) }
    }

    // 앱바 필터 메뉴는 묶음마다 하나를 고른다. 같은 항목을 다시 골라도 꺼지지 않는다 — "전체"가 따로 있다.
    fun selectStatusFilter(status: CollectionStatus?) {
        criteria.update { it.copy(status = status) }
    }

    fun updateQuery(query: String) {
        criteria.update { it.copy(query = query) }
    }

    fun selectSortByRating(byRating: Boolean) {
        criteria.update { it.copy(sortByRating = byRating) }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            when (repository.deleteById(id)) {
                is AppResult.Success -> error.value = null
                is AppResult.Failure -> error.value = "지우지 못했습니다. 다시 시도해 주세요."
            }
        }
    }

    fun dismissError() = error.update { null }

    private fun filterAndSort(
        records: List<DrinkRecord>,
        criteria: FilterCriteria,
    ): List<DrinkRecord> {
        val needle = criteria.query.trim().lowercase()
        val matched =
            records.filter { record ->
                val typeMatches = criteria.type == null || record.type == criteria.type
                val statusMatches = criteria.status == null || record.collectionStatus == criteria.status
                val queryMatches =
                    needle.isEmpty() ||
                        record.name.lowercase().contains(needle) ||
                        record.place?.lowercase()?.contains(needle) == true ||
                        record.memo?.lowercase()?.contains(needle) == true ||
                        record.vintage?.toString()?.contains(needle) == true
                typeMatches && statusMatches && queryMatches
            }

        return if (criteria.sortByRating) {
            matched.sortedWith(
                compareByDescending<DrinkRecord> { it.rating }
                    .thenByDescending { it.recordedAtMillis },
            )
        } else {
            matched
        }
    }

    class Factory(
        private val repository: DrinkRecordRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = CollectionViewModel(repository) as T
    }
}
