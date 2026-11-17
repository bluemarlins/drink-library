package com.bluemarlin.drinkdiary.ui.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkTags
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.ServingStyle
import com.bluemarlin.drinkdiary.domain.model.TasteInput
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import com.bluemarlin.drinkdiary.domain.repository.RecordPhotoStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecordForm(
    val name: String = "",
    val rating: Double = 0.0,
    val collectionStatus: CollectionStatus = CollectionStatus.Normal,
    val vintage: String = "",
    val servingStyle: ServingStyle? = null,
    val price: String = "",
    val place: String = "",
    val memo: String = "",
    val imageUri: String? = null,
    val tags: DrinkTags = DrinkTags(),
) {
    // 만족도는 선택이 아니다 — 선호 판정의 종속 변수라 없으면 그 기록은 유형에 기여하지 못한다.
    val isSavable: Boolean get() = missingForSave == null

    // 저장 버튼이 꺼져 있을 때 무엇이 빠졌는지 그대로 말해 준다.
    val missingForSave: String?
        get() =
            when {
                name.isBlank() && rating <= 0.0 -> "이름과 별점을 남기면 저장할 수 있어요"
                name.isBlank() -> "이름을 입력하면 저장할 수 있어요"
                rating <= 0.0 -> "별점을 남기면 저장할 수 있어요"
                else -> null
            }

    companion object {
        fun of(record: DrinkRecord): RecordForm =
            RecordForm(
                name = record.name,
                rating = record.rating,
                collectionStatus = record.collectionStatus,
                vintage = record.vintage?.toString().orEmpty(),
                servingStyle = record.servingStyle,
                price = record.price?.toString().orEmpty(),
                place = record.place.orEmpty(),
                memo = record.memo.orEmpty(),
                imageUri = record.imageUri,
                tags = record.tags,
            )
    }
}

data class RecordUiState(
    val type: DrinkType? = null,
    val taste: TasteInput = TasteInput(),
    val form: RecordForm = RecordForm(),
    val taps: Int = 0,
    val saving: Boolean = false,
    val savedId: Long? = null,
    val error: String? = null,
)

class RecordViewModel(
    private val repository: DrinkRecordRepository,
    private val photoStore: RecordPhotoStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    private var photoJob: Job? = null

    fun pickDrink(
        type: DrinkType,
        tags: DrinkTags,
    ) = _uiState.update {
        val resetTaste = if (it.type == type) it.taste else TasteInput()
        it.copy(
            type = type,
            taste = resetTaste,
            form = it.form.copy(tags = tags),
            taps = it.taps + 1,
        )
    }

    fun answer(
        trait: Trait,
        answer: TraitAnswer,
    ) = _uiState.update {
        it.copy(taste = it.taste.with(trait, answer), taps = it.taps + 1)
    }

    fun updateForm(form: RecordForm) = _uiState.update { it.copy(form = form) }

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
        val current = _uiState.value
        if (current.type == null || !current.form.isSavable || current.saving) return

        _uiState.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            // 사진 복사가 끝나기 전에 저장하면 사진이 빠진 채 저장된다.
            photoJob?.join()
            val state = _uiState.value
            val type = state.type ?: return@launch
            val record =
                DrinkRecord(
                    type = type,
                    name = state.form.name.trim(),
                    vintage = state.form.vintage.toIntOrNull(),
                    servingStyle = if (type == DrinkType.Whiskey) state.form.servingStyle else null,
                    taste = state.taste,
                    tags = state.form.tags,
                    rating = state.form.rating,
                    collectionStatus = state.form.collectionStatus,
                    imageUri = state.form.imageUri,
                    price = state.form.price.toLongOrNull(),
                    place = state.form.place.ifBlank { null },
                    memo = state.form.memo.ifBlank { null },
                    recordedAtMillis = System.currentTimeMillis(),
                )
            when (val result = repository.save(record)) {
                is AppResult.Success ->
                    _uiState.update { it.copy(saving = false, savedId = result.value) }
                is AppResult.Failure ->
                    _uiState.update { it.copy(saving = false, error = "저장하지 못했습니다. 다시 시도해 주세요.") }
            }
        }
    }

    fun startOver() = _uiState.update { RecordUiState() }

    class Factory(
        private val repository: DrinkRecordRepository,
        private val photoStore: RecordPhotoStore,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = RecordViewModel(repository, photoStore) as T
    }
}

internal const val PHOTO_IMPORT_ERROR = "사진을 가져오지 못했어요. 다른 사진으로 다시 시도해 주세요."
