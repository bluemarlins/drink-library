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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.ServingStyle
import com.bluemarlin.drinkdiary.domain.model.TagCategory
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.component.DDCenteredContent
import com.bluemarlin.drinkdiary.ui.component.DDUriImage
import com.example.ds.component.AuraButton
import com.example.ds.component.AuraButtonSize
import com.example.ds.component.AuraButtonVariant
import com.example.ds.component.AuraChipButton
import com.example.ds.component.AuraChipVariant
import com.example.ds.component.AuraNumberField
import com.example.ds.component.AuraRatingBar
import com.example.ds.component.AuraRatingBarSize
import com.example.ds.component.AuraSegmentedButton
import com.example.ds.component.AuraText
import com.example.ds.component.AuraTextField
import com.example.ds.component.AuraTextFieldVariant
import com.example.ds.component.rememberFloatingScrollProgress
import com.example.ds.foundation.theme.AuraTheme

// 취향 입력 이후 단계. 필수 항목(이름·만족도·다시 살까) 외 선택 항목은 고정된 접힘 영역으로 유지해
// 불필요한 커스터마이징과 입력 마찰을 방지한다.
@Composable
fun RecordDetailStep(
    type: DrinkType,
    form: RecordForm,
    onFormChange: (RecordForm) -> Unit,
    onPhotoPicked: (String) -> Unit,
    onSave: () -> Unit,
    saving: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    saveLabel: String = "저장",
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

        AuraRatingBar(
            rating = form.rating.toFloat(),
            onRatingChange = { onFormChange(form.copy(rating = it.toDouble())) },
            maxStars = 5,
            allowHalfRating = true,
            size = AuraRatingBarSize.Large,
            label = "얼마나 좋았나요?",
            showValueText = true,
        )

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

        AuraButton(
            text = if (expanded) "덜 남기기" else "더 남기기 (선택)",
            onClick = { expanded = !expanded },
            variant = AuraButtonVariant.Ghost,
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

        AuraButton(
            text = if (saving) "저장 중" else saveLabel,
            onClick = onSave,
            variant = AuraButtonVariant.Primary,
            size = AuraButtonSize.Large,
            enabled = form.isSavable,
            loading = saving,
            fullWidth = true,
        )

        form.missingForSave?.let {
            AuraText(
                text = it,
                style = AuraTheme.typography.bodySmall,
                color = AuraTheme.colors.content.secondary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
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
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ServingStyle.entries.forEach { style ->
                        AuraChipButton(
                            label = DrinkLabels.servingStyle(style),
                            variant = AuraChipVariant.Tag,
                            onClick = { onFormChange(form.copy(servingStyle = style)) },
                            selected = form.servingStyle == style,
                        )
                    }
                }
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
