package com.bluemarlin.drinkdiary.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.TagCategory
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.component.DDDivider
import com.bluemarlin.drinkdiary.ui.record.promotableTags
import com.example.ds.component.AuraSwitch
import com.example.ds.component.AuraText
import com.example.ds.foundation.theme.AuraTheme

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onToggle: (TagCategory) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val layout = AuraTheme.layout

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(
                    horizontal = layout.screenMarginHorizontal,
                    vertical = layout.screenMarginVertical,
                ),
        verticalArrangement = Arrangement.spacedBy(layout.sectionSpacing),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            AuraText("기록할 때 물어볼 것", style = AuraTheme.typography.titleMedium)
            AuraText(
                text = "켜면 기록할 때마다 바로 보이고, 끄면 '더 남기기' 안으로 들어가요.",
                style = AuraTheme.typography.bodySmall,
                color = AuraTheme.colors.content.secondary,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(layout.gridGutter)) {
            promotableTags.forEach { (category, hint) ->
                AuraSwitch(
                    checked = category in state.alwaysAskTags,
                    onCheckedChange = { onToggle(category) },
                    label = DrinkLabels.tagCategory(category),
                    description = hint,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        DDDivider()

        AuraText(
            // 정직하게 말한다. 지금 켜도 **이미 저장된 기록에는 그 답이 없다.**
            // 이 항목의 취향 판정은 앞으로 쌓이는 기록부터 시작한다 — 숨기면 사용자는
            // "왜 아직 아무것도 안 나오지"에서 앱이 고장난 줄 안다.
            text = "지금 켜도 이미 저장한 기록에는 그 답이 없어요. 이 항목의 취향은 앞으로 남기는 기록부터 쌓여요.",
            style = AuraTheme.typography.bodySmall,
            color = AuraTheme.colors.content.secondary,
        )
    }
}
