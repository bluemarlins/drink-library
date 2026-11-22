package com.bluemarlin.drinkdiary.ui.record

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.ServingStyle
import com.bluemarlin.drinkdiary.domain.model.TagCategory
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.component.DDCenteredContent
import com.bluemarlin.drinkdiary.ui.component.DDUriImage
import io.github.bluemarlins.aura.component.AuraButton
import io.github.bluemarlins.aura.component.AuraButtonSize
import io.github.bluemarlins.aura.component.AuraButtonVariant
import io.github.bluemarlins.aura.component.AuraFloatingActionBar
import io.github.bluemarlins.aura.component.AuraFloatingActionBarDefaults
import io.github.bluemarlins.aura.component.AuraNumberField
import io.github.bluemarlins.aura.component.AuraRatingBar
import io.github.bluemarlins.aura.component.AuraRatingBarSize
import io.github.bluemarlins.aura.component.AuraSegmentedButton
import io.github.bluemarlins.aura.component.AuraText
import io.github.bluemarlins.aura.component.AuraTextField
import io.github.bluemarlins.aura.component.AuraTextFieldVariant
import io.github.bluemarlins.aura.component.rememberFloatingScrollProgress
import io.github.bluemarlins.aura.foundation.theme.AuraTheme

// 취향 입력 이후 단계. 필수 항목(이름·만족도·다시 살까) 외 선택 항목은 고정된 접힘 영역으로 유지해
// 불필요한 커스터마이징과 입력 마찰을 방지한다.
@Composable
fun RecordDetailStep(
    type: DrinkType,
    form: RecordForm,
    onFormChange: (RecordForm) -> Unit,
    onPhotoPicked: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    onScrollFractionChange: (Float) -> Unit = {},
    tasteSection: @Composable () -> Unit = {},
) {
    val optionalTags = remember(type) { remainingTags(type) }
    var expanded by remember { mutableStateOf(false) }
    val photoPicker =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) onPhotoPicked(uri.toString())
        }
    val layout = AuraTheme.layout
    val motion = AuraTheme.motion
    val scrollState = rememberScrollState()
    val scrollProgress = rememberFloatingScrollProgress(scrollState)

    LaunchedEffect(scrollProgress) {
        onScrollFractionChange(scrollProgress)
    }

    DDCenteredContent(
        modifier = modifier,
        contentPadding = contentPadding,
        scrollState = scrollState,
    ) {
        AuraText("무엇이었나요?", style = AuraTheme.typography.headlineSmall)

        AuraTextField(
            value = form.name,
            onValueChange = { onFormChange(form.copy(name = it)) },
            label = "이름",
            placeholder = "술 이름을 입력하세요",
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        // Aura 기본 표시는 "3.5 / 5"라 용어집(branding.md 7절)과 어긋난다. 앱 문구로 대신 쓴다.
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            AuraText("얼마나 좋았나요?", style = AuraTheme.typography.labelLarge)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AuraRatingBar(
                    rating = form.rating.toFloat(),
                    onRatingChange = { onFormChange(form.copy(rating = it.toDouble())) },
                    maxStars = 5,
                    allowHalfRating = true,
                    size = AuraRatingBarSize.Large,
                    showValueText = false,
                )
                // 0점은 아직 고르지 않은 상태다. "0점"이라고 매긴 것처럼 보이지 않게 숫자를 감춘다.
                if (form.rating > 0.0) {
                    AuraText(DrinkLabels.ratingText(form.rating), style = AuraTheme.typography.bodyLarge)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AuraText("다시 살 건가요?", style = AuraTheme.typography.titleMedium)
            val statuses = CollectionStatus.entries
            AuraSegmentedButton(
                options = statuses.map(DrinkLabels::collectionStatus),
                selectedIndex = statuses.indexOf(form.collectionStatus).coerceAtLeast(0),
                onOptionSelected = { index ->
                    onFormChange(form.copy(collectionStatus = statuses[index]))
                },
                fullWidth = true,
            )
        }

        tasteSection()

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(layout.gridGutter),
        ) {
            AuraButton(
                text = if (form.imageUri == null) "사진 넣기" else "사진 바꾸기",
                onClick = {
                    photoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
                variant = AuraButtonVariant.Outline,
                size = AuraButtonSize.Small,
            )

            form.imageUri?.let {
                DDUriImage(
                    imageUri = it,
                    contentDescription = "선택한 사진",
                    modifier = Modifier.size(64.dp).clip(AuraTheme.shapes.small),
                )
                AuraButton(
                    text = "사진 빼기",
                    onClick = { onFormChange(form.copy(imageUri = null)) },
                    variant = AuraButtonVariant.Ghost,
                    size = AuraButtonSize.Small,
                )
            }
        }

        // Ghost 버튼은 안쪽 여백 때문에 글자가 다른 항목보다 들여 써져 보인다. 사진 버튼과 같은 Outline으로 맞춘다.
        AuraButton(
            text = if (expanded) "덜 남기기" else "더 남기기 (선택)",
            onClick = { expanded = !expanded },
            variant = AuraButtonVariant.Outline,
            size = AuraButtonSize.Small,
        )

        AnimatedVisibility(
            visible = expanded,
            enter =
                if (motion.reduceMotion) {
                    fadeIn(motion.enterSpec())
                } else {
                    fadeIn(motion.enterSpec()) + expandVertically(motion.spatialSpring())
                },
            exit =
                if (motion.reduceMotion) {
                    fadeOut(motion.exitSpec())
                } else {
                    fadeOut(motion.exitSpec()) + shrinkVertically(motion.spatialSpring())
                },
        ) {
            OptionalFields(
                type = type,
                form = form,
                optionalTags = optionalTags,
                onFormChange = onFormChange,
            )
        }
    }
}

// 기록하기·기록 고치기 공용 저장 독. 저장할 수 없는 이유는 버튼 위에 함께 보여 준다.
@Composable
fun RecordSaveBar(
    form: RecordForm,
    saving: Boolean,
    onSave: () -> Unit,
    label: String = "저장",
) {
    AuraFloatingActionBar(supportingText = form.missingForSave) {
        AuraButton(
            text = if (saving) "저장 중" else label,
            onClick = onSave,
            variant = AuraButtonVariant.Primary,
            size = AuraButtonSize.Large,
            enabled = form.isSavable,
            loading = saving,
            fullWidth = true,
            modifier = Modifier.weight(1f),
        )
    }
}

fun recordSaveBarHeight(form: RecordForm): Dp =
    AuraFloatingActionBarDefaults.Height +
        if (form.missingForSave != null) AuraFloatingActionBarDefaults.SupportingTextHeight else 0.dp

@Composable
private fun OptionalFields(
    type: DrinkType,
    form: RecordForm,
    optionalTags: List<TagCategory>,
    onFormChange: (RecordForm) -> Unit,
) {
    val layout = AuraTheme.layout

    Column(verticalArrangement = Arrangement.spacedBy(layout.sectionSpacing)) {
        TagPicker(
            type = type,
            tags = form.tags,
            categories = optionalTags,
            onTagsChange = { onFormChange(form.copy(tags = it)) },
        )

        if (type == DrinkType.Wine) {
            AuraNumberField(
                value = form.vintage,
                onValueChange = { onFormChange(form.copy(vintage = it.filter(Char::isDigit).take(4))) },
                label = "빈티지",
                placeholder = "예: 2021",
                allowDecimals = false,
                formatThousands = false,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AuraText("어떻게 마셨나요?", style = AuraTheme.typography.titleSmall)
                // 고르기 전에는 아무것도 선택되지 않은 채로 보인다(selectedIndex = -1).
                AuraSegmentedButton(
                    options = ServingStyle.entries.map(DrinkLabels::servingStyle),
                    selectedIndex = ServingStyle.entries.indexOf(form.servingStyle),
                    onOptionSelected = { index -> onFormChange(form.copy(servingStyle = ServingStyle.entries[index])) },
                )
            }
        }

        AuraNumberField(
            value = form.price,
            onValueChange = { onFormChange(form.copy(price = it.filter(Char::isDigit))) },
            label = "가격",
            suffixText = "원",
            allowDecimals = false,
            formatThousands = true,
            modifier = Modifier.fillMaxWidth(),
        )
        AuraTextField(
            value = form.place,
            onValueChange = { onFormChange(form.copy(place = it)) },
            label = "어디에서",
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        AuraTextField(
            value = form.memo,
            onValueChange = { onFormChange(form.copy(memo = it)) },
            variant = AuraTextFieldVariant.Paperless,
            label = "메모",
            singleLine = false,
            minLines = 3,
            maxLines = 6,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
