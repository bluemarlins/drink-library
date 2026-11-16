package com.bluemarlin.drinkdiary.ui.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.TasteInput
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditUiState(
    val loaded: Boolean = false,
    val missing: Boolean = false,
    val type: DrinkType = DrinkType.Wine,
    val form: RecordForm = RecordForm(),
    val taste: TasteInput = TasteInput(),
    val saving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null,
)

fun DrinkRecord.applying(
    form: RecordForm,
    taste: TasteInput,
): DrinkRecord =
    copy(
        name = form.name.trim(),
        vintage = form.vintage.toIntOrNull(),
        servingStyle = if (type == DrinkType.Whiskey) form.servingStyle else null,
        taste = taste,
        tags = form.tags,
        rating = form.rating,
        collectionStatus = form.collectionStatus,
        imageUri = form.imageUri,
        price = form.price.toLongOrNull(),
        place = form.place.ifBlank { null },
        memo = form.memo.ifBlank { null },
    )

class EditRecordViewModel(
    private val recordId: Long,
    private val repository: DrinkRecordRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditUiState())
    val uiState: StateFlow<EditUiState> = _uiState.asStateFlow()

    private var original: DrinkRecord? = null

    init {
        viewModelScope.launch {
            val record = repository.observeRecord(recordId).first()
            if (record == null) {
                _uiState.update { it.copy(loaded = true, missing = true) }
                return@launch
            }
            original = record
            _uiState.update {
                it.copy(
                    loaded = true,
                    type = record.type,
                    form = RecordForm.of(record),
                    taste = record.taste,
                )
            }
        }
    }

    fun updateForm(form: RecordForm) = _uiState.update { it.copy(form = form) }

    fun answer(
        trait: Trait,
        answer: TraitAnswer,
    ) = _uiState.update { it.copy(taste = it.taste.with(trait, answer)) }

    fun save() {
        val source = original ?: return
        val state = _uiState.value
        if (!state.form.isSavable || state.saving) return

        _uiState.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            when (repository.save(source.applying(state.form, state.taste))) {
                is AppResult.Success -> _uiState.update { it.copy(saving = false, saved = true) }
                is AppResult.Failure ->
                    _uiState.update { it.copy(saving = false, error = "고치지 못했습니다. 다시 시도해 주세요.") }
            }
        }
    }

    fun dismissError() = _uiState.update { it.copy(error = null) }

    class Factory(
        private val recordId: Long,
        private val repository: DrinkRecordRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = EditRecordViewModel(recordId, repository) as T
    }
}
