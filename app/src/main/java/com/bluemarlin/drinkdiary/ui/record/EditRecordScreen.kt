package com.bluemarlin.drinkdiary.ui.record

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import com.bluemarlin.drinkdiary.ui.component.DDCenteredContent
import com.example.ds.component.AuraDivider
import com.example.ds.component.AuraText
import com.example.ds.foundation.theme.AuraTheme

// 기록 작성과 **같은 폼**을 쓴다. 취향 편집만 슬롯으로 끼워 넣는다 —
// 작성 경로는 마법사가 이미 물었으므로 그 자리를 비워 둔다.
@Composable
fun EditRecordScreen(
    state: EditUiState,
    onFormChange: (RecordForm) -> Unit,
    onAnswer: (Trait, TraitAnswer) -> Unit,
    onSave: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    if (!state.loaded) {
        DDCenteredContent(
            modifier = modifier,
            contentPadding = contentPadding,
            scrollable = false,
        ) {
            AuraText("기록을 불러오는 중이에요.", style = AuraTheme.typography.bodyLarge)
        }
        return
    }

    if (state.missing) {
        // 목록에서 이미 지워졌을 수 있다. 빈 폼을 보여주면 저장할 때 **새 기록이 생긴다.**
        DDCenteredContent(
            modifier = modifier,
            contentPadding = contentPadding,
            scrollable = false,
        ) {
            AuraText("이 기록을 찾을 수 없어요.", style = AuraTheme.typography.bodyLarge)
        }
        return
    }

    RecordDetailStep(
        type = state.type,
        form = state.form,
        onFormChange = onFormChange,
        onSave = onSave,
        saving = state.saving,
        modifier = modifier,
        contentPadding = contentPadding,
        saveLabel = "고친 내용 저장",
        tasteSection = {
            AuraDivider()
            TasteEditor(type = state.type, taste = state.taste, onAnswer = onAnswer)
        },
    )
}
