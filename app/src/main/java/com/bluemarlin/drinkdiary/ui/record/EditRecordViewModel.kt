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
import com.bluemarlin.drinkdiary.domain.repository.RecordPhotoStore
import kotlinx.coroutines.Job
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
    val initialForm: RecordForm = RecordForm(),
    val initialTaste: TasteInput = TasteInput(),
) {
    // 고친 게 있는데 저장 없이 나가려는지 판단한다. 불러오기 전과 저장 후에는 버릴 것이 없다.
    val dirty: Boolean
        get() = loaded && !missing && !saved && (form != initialForm || taste != initialTaste)
}

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
    private val photoStore: RecordPhotoStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditUiState())
    val uiState: StateFlow<EditUiState> = _uiState.asStateFlow()

    private var original: DrinkRecord? = null
    private var photoJob: Job? = null

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
                    initialForm = RecordForm.of(record),
                    initialTaste = record.taste,
                )
            }
        }
    }

    fun updateForm(form: RecordForm) = _uiState.update { it.copy(form = form) }

    fun answer(
        trait: Trait,
        answer: TraitAnswer,
    ) = _uiState.update { it.copy(taste = it.taste.with(trait, answer)) }

    fun pickPhoto(sourceUri: String) {
        photoJob =
            viewModelScope.launch {
                when (val imported = photoStore.import(sourceUri)) {
                    is AppResult.Success ->
                        _uiState.update { it.copy(form = it.form.copy(imageUri = imported.value)) }
                    is AppResult.Failure ->
                        _uiState.update { it.copy(error = PHOTO_IMPORT_ERROR) }
                }
            }
    }

    fun save() {
        val source = original ?: return
        if (!_uiState.value.form.isSavable || _uiState.value.saving) return

        _uiState.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            photoJob?.join()
            val state = _uiState.value
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
        private val photoStore: RecordPhotoStore,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            EditRecordViewModel(recordId, repository, photoStore) as T
    }
}
