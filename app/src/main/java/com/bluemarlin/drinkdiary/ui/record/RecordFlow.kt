package com.bluemarlin.drinkdiary.ui.record

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bluemarlin.drinkdiary.DrinkDiaryApplication
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.TagCategory
import com.bluemarlin.drinkdiary.ui.component.DDDivider
import com.example.ds.component.AuraButton
import com.example.ds.component.AuraButtonSize
import com.example.ds.component.AuraButtonVariant
import com.example.ds.component.AuraText
import com.example.ds.foundation.theme.AuraTheme

private enum class Step { PickDrink, Probes, Detail, Saved }

@Composable
fun RecordFlow(modifier: Modifier = Modifier) {
    val appContainer = (LocalContext.current.applicationContext as DrinkDiaryApplication).appContainer
    val viewModel: RecordViewModel =
        viewModel(
            factory =
                RecordViewModel.Factory(
                    appContainer.drinkRecordRepository,
                    appContainer.userPreferencesRepository,
                ),
        )
    val state by viewModel.uiState.collectAsState()
    var step by remember { mutableStateOf(Step.PickDrink) }

    if (state.savedId != null && step != Step.Saved) step = Step.Saved

    Crossfade(targetState = step, label = "record-step") { current ->
        when (current) {
            Step.PickDrink ->
                DrinkPicker(
                    onPick = { choice ->
                        viewModel.pickDrink(choice.type, choice.tags)
                        step = Step.Probes
                    },
                    modifier = modifier,
                )

            Step.Probes ->
                ProbeSequenceScreen(
                    type = state.type ?: DrinkType.Wine,
                    answers = state.taste,
                    onAnswer = viewModel::answer,
                    onComplete = { step = Step.Detail },
                    modifier = modifier,
                )

            Step.Detail ->
                RecordDetailStep(
                    type = state.type ?: DrinkType.Wine,
                    form = state.form,
                    alwaysAskTags = state.alwaysAskTags,
                    onFormChange = viewModel::updateForm,
                    onSave = viewModel::save,
                    saving = state.saving,
                    modifier = modifier,
                )

            Step.Saved ->
                RecordSaved(
                    taps = state.taps,
                    leaning = state.taste.leaningCount,
                    askTagPreference = state.askTagPreference,
                    onChooseTags = viewModel::chooseAlwaysAskTags,
                    onRestart = {
                        viewModel.startOver()
                        step = Step.PickDrink
                    },
                    modifier = modifier,
                )
        }
    }

    state.error?.let {
        // 저장 실패는 조용히 넘기지 않는다(harness.md §7).
        val layout = AuraTheme.layout
        AuraText(
            it,
            color = AuraTheme.colors.status.error.solid,
            modifier =
                Modifier.padding(
                    horizontal = layout.screenMarginHorizontal,
                    vertical = layout.screenMarginVertical,
                ),
        )
    }
}

@Composable
private fun RecordSaved(
    taps: Int,
    leaning: Int,
    askTagPreference: Boolean,
    onChooseTags: (Set<TagCategory>) -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val layout = AuraTheme.layout
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = layout.screenMarginHorizontal,
                    vertical = layout.screenMarginVertical,
                ),
        verticalArrangement = Arrangement.spacedBy(layout.gridGutter),
    ) {
        AuraText("기록했습니다.", style = AuraTheme.typography.headlineSmall)
        AuraText("취향 입력은 탭 ${taps}번으로 끝났습니다.", style = AuraTheme.typography.bodyLarge)
        AuraText(
            // '보통'을 뺀 개수를 말하되 그것이 버려졌다는 인상을 주지 않는다 — 판정에는 다 쓰인다.
            text = "뚜렷한 인상을 남긴 축 ${leaning}개.",
            style = AuraTheme.typography.bodyMedium,
            color = AuraTheme.colors.content.secondary,
        )
        AuraText(
            text = "유형은 한 잔으로 나오지 않습니다. 기록이 쌓여 대비가 생겨야 판정됩니다.",
            style = AuraTheme.typography.bodySmall,
            color = AuraTheme.colors.content.secondary,
        )
        // 첫 기록 직후 한 번만. 이미 가치를 받은 뒤라 마찰로 느껴지지 않고,
        // 이 시점의 사용자는 방금 흐름을 겪어서 무엇을 묻는 건지 안다(prd.md S1).
        if (askTagPreference) {
            DDDivider(modifier = Modifier.padding(vertical = 8.dp))
            TagPreferencePrompt(onConfirm = onChooseTags)
        } else {
            AuraButton(
                text = "한 잔 더 기록하기",
                onClick = onRestart,
                variant = AuraButtonVariant.Secondary,
                size = AuraButtonSize.Large,
                fullWidth = true,
            )
        }
    }
}
