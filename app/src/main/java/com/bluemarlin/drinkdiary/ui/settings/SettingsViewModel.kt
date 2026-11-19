package com.bluemarlin.drinkdiary.ui.settings

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.DashboardOverview
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.domain.usecase.BackupDrinkRecordsUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveDashboardOverviewUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SettingsUiState(
    val overview: DashboardOverview = DashboardOverview(),
    val busy: Boolean = false,
    val message: String? = null,
)

class SettingsViewModel(
    observeOverview: ObserveDashboardOverviewUseCase,
    private val backupUseCase: BackupDrinkRecordsUseCase,
) : ViewModel() {
    private val busy = MutableStateFlow(false)
    private val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> =
        combine(
            observeOverview(TypeScope.Combined),
            busy,
            message,
        ) { overview, isBusy, msg ->
            SettingsUiState(
                overview = overview,
                busy = isBusy,
                message = msg,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun exportToUri(
        contentResolver: ContentResolver,
        uri: Uri,
    ) {
        if (busy.value) return
        busy.value = true
        viewModelScope.launch {
            val outcome =
                when (val encoded = backupUseCase.exportJson()) {
                    is AppResult.Failure -> "백업 파일을 만들지 못했습니다."
                    is AppResult.Success ->
                        withContext(Dispatchers.IO) {
                            runCatching {
                                contentResolver.openOutputStream(uri)?.use { stream ->
                                    stream.write(encoded.value.toByteArray(Charsets.UTF_8))
                                } ?: error("cannot open stream")
                            }.fold(
                                onSuccess = { "기록 ${uiState.value.overview.totalCount}잔을 백업 파일로 저장했어요." },
                                onFailure = { "백업 파일을 저장하지 못했습니다." },
                            )
                        }
                }
            busy.value = false
            message.value = outcome
        }
    }

    fun restoreFromUri(
        contentResolver: ContentResolver,
        uri: Uri,
    ) {
        if (busy.value) return
        busy.value = true
        viewModelScope.launch {
            val rawJson =
                withContext(Dispatchers.IO) {
                    runCatching {
                        contentResolver.openInputStream(uri)?.use { stream ->
                            stream.readBytes().toString(Charsets.UTF_8)
                        } ?: error("cannot open stream")
                    }.getOrNull()
                }

            val outcome =
                if (rawJson == null) {
                    "백업 파일을 읽지 못했습니다."
                } else {
                    when (val result = backupUseCase.restoreFromJson(rawJson)) {
                        is AppResult.Failure -> "한잔의 기록 백업 파일이 아니에요."
                        is AppResult.Success -> {
                            val summary = result.value
                            if (summary.skippedCount > 0) {
                                "기록 ${summary.importedCount}잔을 불러왔고, 이미 있는 ${summary.skippedCount}잔은 건너뛰었어요."
                            } else {
                                "기록 ${summary.importedCount}잔을 불러왔어요."
                            }
                        }
                    }
                }
            busy.value = false
            message.value = outcome
        }
    }

    fun dismissMessage() = message.update { null }

    class Factory(
        private val observeOverview: ObserveDashboardOverviewUseCase,
        private val backupUseCase: BackupDrinkRecordsUseCase,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel(observeOverview, backupUseCase) as T
    }
}
