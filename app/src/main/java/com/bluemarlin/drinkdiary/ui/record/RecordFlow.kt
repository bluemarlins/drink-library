package com.bluemarlin.drinkdiary.ui.record

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.snap
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bluemarlin.drinkdiary.DrinkDiaryApplication
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.ui.component.DDCenteredContent
import com.bluemarlin.drinkdiary.ui.navigation.DDScreenScaffold
import com.bluemarlin.drinkdiary.ui.navigation.DDScreenType
import com.example.ds.component.AuraButton
import com.example.ds.component.AuraButtonSize
import com.example.ds.component.AuraButtonVariant
import com.example.ds.component.AuraDialog
import com.example.ds.component.AuraDialogVariant
import com.example.ds.component.AuraText
import com.example.ds.foundation.theme.AuraTheme

private enum class Step { PickDrink, Probes, Detail, Saved }

@Composable
fun RecordFlow(
    onExit: () -> Unit,
    prefillName: String,
    onShareTasteCard: (DrinkType) -> Unit,
    snackbarHost: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val appContainer = (LocalContext.current.applicationContext as DrinkDiaryApplication).appContainer
    val viewModel: RecordViewModel =
        viewModel(
            factory = RecordViewModel.Factory(appContainer.drinkRecordRepository, appContainer.recordPhotoStore),
        )
    val state by viewModel.uiState.collectAsState()

    // 이 ViewModel은 화면을 떠나도 남는다(Activity 범위). 비우지 않고 나가면 다음 진입이
    // 저장 완료 화면이나 지난번 입력에서 시작한다.
    val exit: () -> Unit = {
        viewModel.startOver()
        onExit()
    }
    LaunchedEffect(Unit) {
        if (prefillName.isNotBlank()) viewModel.updateForm(RecordForm(name = prefillName))
    }
    var step by remember { mutableStateOf(Step.PickDrink) }
    var probeIndex by remember { mutableIntStateOf(0) }
    var showDiscardConfirm by remember { mutableStateOf(false) }
    var scrollFraction by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(step) {
        if (step != Step.Detail) scrollFraction = 0f
    }

    if (state.savedId != null && step != Step.Saved) step = Step.Saved

    val activeType = state.type ?: DrinkType.Wine
    val traits = Trait.of(activeType)

    val handleBack: () -> Unit = {
        when (step) {
            Step.PickDrink ->
                if (state.form.name.isNotBlank() || state.form.rating > 0.0 || state.taste.answers.isNotEmpty()) {
                    showDiscardConfirm = true
                } else {
                    exit()
                }
            Step.Probes ->
                if (probeIndex > 0) {
                    probeIndex--
                } else {
                    step = Step.PickDrink
                }
            Step.Detail -> {
                probeIndex = traits.lastIndex.coerceAtLeast(0)
                step = Step.Probes
            }
            Step.Saved -> exit()
        }
    }

    BackHandler(onBack = handleBack)

    DDScreenScaffold(
        title = "기록하기",
        screenType = DDScreenType.Editor,
        onBackClick = handleBack,
        scrollFraction = scrollFraction,
        snackbarHost = snackbarHost,
    ) { contentPadding ->
        val motion = AuraTheme.motion
        Crossfade(
            targetState = step,
            animationSpec = if (motion.reduceMotion) snap() else motion.enterSpec(),
            label = "record-step",
        ) { current ->
            when (current) {
                Step.PickDrink ->
                    DrinkPicker(
                        onPick = { choice ->
                            viewModel.pickDrink(choice.type, choice.tags)
                            probeIndex = 0
                            step = Step.Probes
                        },
                        modifier = modifier,
                        contentPadding = contentPadding,
                    )

                Step.Probes ->
                    ProbeSequenceScreen(
                        type = activeType,
                        answers = state.taste,
                        currentIndex = probeIndex,
                        onIndexChange = { probeIndex = it },
                        onAnswer = viewModel::answer,
                        onComplete = { step = Step.Detail },
                        onBack = {
                            if (probeIndex > 0) {
                                probeIndex--
                            } else {
                                step = Step.PickDrink
                            }
                        },
                        modifier = modifier,
                        contentPadding = contentPadding,
                    )

                Step.Detail ->
                    RecordDetailStep(
                        type = activeType,
                        form = state.form,
                        onFormChange = viewModel::updateForm,
                        onPhotoPicked = viewModel::pickPhoto,
                        onSave = viewModel::save,
                        saving = state.saving,
                        modifier = modifier,
                        contentPadding = contentPadding,
                        onScrollFractionChange = { scrollFraction = it },
                    )

                Step.Saved ->
                    RecordSaved(
                        type = activeType,
                        onShare = { onShareTasteCard(activeType) },
                        onRestart = {
                            viewModel.startOver()
                            probeIndex = 0
                            step = Step.PickDrink
                        },
                        onDone = exit,
                        modifier = modifier,
                        contentPadding = contentPadding,
                    )
            }
        }

        state.error?.let {
            val layout = AuraTheme.layout
            AuraText(
                it,
                color = AuraTheme.colors.status.error.solid,
                modifier =
                    Modifier
                        .padding(contentPadding)
                        .padding(
                            horizontal = layout.screenMarginHorizontal,
                            vertical = layout.screenMarginVertical,
                        ),
            )
        }
    }

    AuraDialog(
        visible = showDiscardConfirm,
        title = "기록을 그만할까요?",
        message = "지금 입력한 내용은 저장되지 않아요.",
        variant = AuraDialogVariant.Destructive,
        confirmText = "나가기",
        dismissText = "계속 기록하기",
        onConfirm = {
            showDiscardConfirm = false
            exit()
        },
        onDismissRequest = { showDiscardConfirm = false },
    )
}

@Composable
private fun RecordSaved(
    type: DrinkType,
    onShare: () -> Unit,
    onRestart: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    DDCenteredContent(
        modifier = modifier,
        contentPadding = contentPadding,
    ) {
        AuraText("기록했습니다.", style = AuraTheme.typography.headlineSmall)
        AuraText(
            text = "이번 잔까지 내 취향에 반영했어요.",
            style = AuraTheme.typography.bodyLarge,
        )
        AuraText(
            text = "기록이 쌓일수록 취향 유형과 다음 한 병 추천이 더 정확해져요.",
            style = AuraTheme.typography.bodySmall,
            color = AuraTheme.colors.content.secondary,
        )
        AuraButton(
            text = "${if (type == DrinkType.Wine) "와인" else "위스키"} 취향 카드 공유하기",
            onClick = onShare,
            variant = AuraButtonVariant.Primary,
            size = AuraButtonSize.Large,
            fullWidth = true,
        )
        AuraButton(
            text = "한 잔 더 기록하기",
            onClick = onRestart,
            variant = AuraButtonVariant.Secondary,
            size = AuraButtonSize.Large,
            fullWidth = true,
        )
        AuraButton(
            text = "내 취향으로 돌아가기",
            onClick = onDone,
            variant = AuraButtonVariant.Ghost,
            size = AuraButtonSize.Medium,
            fullWidth = true,
        )
    }
}
