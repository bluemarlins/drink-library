package com.bluemarlin.drinkdiary.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.ui.component.DDCenteredContent
import com.example.ds.component.AuraButton
import com.example.ds.component.AuraButtonSize
import com.example.ds.component.AuraButtonVariant
import com.example.ds.component.AuraCard
import com.example.ds.component.AuraCardPadding
import com.example.ds.component.AuraCardVariant
import com.example.ds.component.AuraDivider
import com.example.ds.component.AuraText
import com.example.ds.component.BindAdaptiveBarToScrollState
import com.example.ds.component.rememberFloatingScrollProgress
import com.example.ds.foundation.theme.AuraTheme

// 불필요한 지표/옵션 커스터마이징은 열지 않고, 로컬 데이터 보존(백업·복원)만 제공한다.
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onExportBackup: (Uri) -> Unit,
    onRestoreBackup: (Uri) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    onScrollFractionChange: (Float) -> Unit = {},
) {
    val scrollState = rememberScrollState()
    val scrollProgress = rememberFloatingScrollProgress(scrollState)
    BindAdaptiveBarToScrollState(scrollState = scrollState)
    LaunchedEffect(scrollProgress) {
        onScrollFractionChange(scrollProgress)
    }

    val createBackupLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            if (uri != null) onExportBackup(uri)
        }
    val openBackupLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) onRestoreBackup(uri)
        }

    DDCenteredContent(
        modifier = modifier,
        contentPadding = contentPadding,
        scrollState = scrollState,
    ) {
        AuraCard(
            modifier = Modifier.fillMaxWidth(),
            variant = AuraCardVariant.Outlined,
            padding = AuraCardPadding.Large,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AuraText("내 데이터 보관함", style = AuraTheme.typography.titleMedium)
                val summary =
                    "현재 저장된 기록 ${state.overview.totalCount}잔 " +
                        "(와인 ${state.overview.wineCount} · 위스키 ${state.overview.whiskeyCount})"
                AuraText(
                    text = summary,
                    style = AuraTheme.typography.bodyLarge,
                )
                AuraText(
                    text = "서버에 로그인하거나 기록을 보내지 않아요. 내 드라이브나 기기에 JSON 파일로 백업할 수 있습니다.",
                    style = AuraTheme.typography.bodySmall,
                    color = AuraTheme.colors.content.secondary,
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AuraButton(
                text = "백업 파일 저장 (.json)",
                onClick = { createBackupLauncher.launch("taste-archive-backup.json") },
                variant = AuraButtonVariant.Primary,
                size = AuraButtonSize.Large,
                enabled = !state.busy && state.overview.totalCount > 0,
                loading = state.busy,
                fullWidth = true,
            )

            AuraButton(
                text = "백업에서 불러오기",
                onClick = { openBackupLauncher.launch(arrayOf("application/json", "text/*")) },
                variant = AuraButtonVariant.Secondary,
                size = AuraButtonSize.Large,
                enabled = !state.busy,
                fullWidth = true,
            )
        }

        AuraDivider()

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            AuraText("평가 기준", style = AuraTheme.typography.titleSmall)
            AuraText(
                text =
                    "와인과 위스키 모두 6개의 심플한 지표로 평가하도록 고정되어 있어요. " +
                        "기록이 쌓이면 4글자 취향 유형과 추천이 자동으로 계산됩니다.",
                style = AuraTheme.typography.bodySmall,
                color = AuraTheme.colors.content.secondary,
            )
        }
    }
}
