package com.bluemarlin.drinkdiary.ui.record

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.ServingStyle
import com.bluemarlin.drinkdiary.domain.model.TagCategory
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.component.DDCenteredContent
import com.bluemarlin.drinkdiary.ui.component.DDUriImage
import com.bluemarlin.drinkdiary.ui.theme.DDShape
import com.example.ds.component.AuraButton
import com.example.ds.component.AuraButtonSize
import com.example.ds.component.AuraButtonVariant
import com.example.ds.component.AuraChipButton
import com.example.ds.component.AuraNumberField
import com.example.ds.component.AuraRatingBar
import com.example.ds.component.AuraRatingBarSize
import com.example.ds.component.AuraSegmentedButton
import com.example.ds.component.AuraText
import com.example.ds.component.AuraTextField
import com.example.ds.component.AuraTextFieldVariant
import com.example.ds.foundation.theme.AuraTheme

// 취향 입력 이후 단계. 이름과 만족도만 필수이고 나머지는 접어둔다 —
// 기본 경로를 무겁게 만들면 F2에서 지킨 마찰 예산이 여기서 무너진다.
@Composable
fun RecordDetailStep(
    type: DrinkType,
    form: RecordForm,
    alwaysAskTags: Set<TagCategory>,
    onFormChange: (RecordForm) -> Unit,
    onSave: () -> Unit,
    saving: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    // 편집 화면도 이 폼을 그대로 쓴다. 폼을 복제하면 저장 규칙이 한쪽에서만 지켜진다.
    saveLabel: String = "저장",
    // 편집에서만 채워지는 자리. 작성 경로는 마법사가 이미 취향을 물었으므로 비어 있다 —
    // 같은 것을 두 번 물으면 F2의 탭 예산이 무너진다.
    tasteSection: @Composable () -> Unit = {},
) {
    // 사용자가 "매번 물어봐 달라"고 고른 것은 접지 않고 늘 보인다.
    val (always, folded) = remainingTags(type).partition { it in alwaysAskTags }
    var expanded by remember { mutableStateOf(false) }
    val photoPicker =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) onFormChange(form.copy(imageUri = uri.toString()))
        }
    val layout = AuraTheme.layout

    DDCenteredContent(
        modifier = modifier,
        contentPadding = contentPadding,
    ) {
        AuraText("무엇이었나요?", style = AuraTheme.typography.headlineSmall)

        AuraTextField(
            value = form.name,
            onValueChange = { onFormChange(form.copy(name = it)) },
            label = "이름",
            placeholder = "술 이름을 입력하세요",
            supportingText = if (form.name.isBlank()) "이름을 입력하면 저장할 수 있어요" else null,
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
                    modifier = Modifier.size(64.dp).clip(DDShape.small),
                )
            }
        }

        TagPicker(
            type = type,
            tags = form.tags,
            categories = always,
            onTagsChange = { onFormChange(form.copy(tags = it)) },
            title = null,
        )

        AuraButton(
            text = if (expanded) "덜 남기기" else "더 남기기 (선택)",
            onClick = { expanded = !expanded },
            variant = AuraButtonVariant.Ghost,
            size = AuraButtonSize.Small,
        )

        if (expanded) {
            OptionalFields(
                type = type,
                form = form,
                foldedTags = folded,
                onFormChange = onFormChange,
            )
        }

        AuraButton(
            text = if (saving) "저장 중" else saveLabel,
            onClick = onSave,
            variant = AuraButtonVariant.Primary,
            size = AuraButtonSize.Large,
            enabled = form.isSavable && !saving,
            loading = saving,
            fullWidth = true,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OptionalFields(
    type: DrinkType,
    form: RecordForm,
    foldedTags: List<TagCategory>,
    onFormChange: (RecordForm) -> Unit,
) {
    val layout = AuraTheme.layout

    Column(verticalArrangement = Arrangement.spacedBy(layout.sectionSpacing)) {
        TagPicker(
            type = type,
            tags = form.tags,
            categories = foldedTags,
            onTagsChange = { onFormChange(form.copy(tags = it)) },
        )

        // P4 — 같은 이름이 같은 맛을 보장하지 않게 만드는 변수들
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

// 라벨은 ui.DrinkLabels 한 곳에서 온다 — 기록 화면과 컬렉션 화면이 같은 것을 다르게 부르지 않도록.
