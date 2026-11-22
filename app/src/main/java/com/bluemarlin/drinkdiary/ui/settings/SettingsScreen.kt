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
import com.bluemarlin.drinkdiary.domain.model.AppearanceSettings
import com.bluemarlin.drinkdiary.domain.model.ColorModePreference
import com.bluemarlin.drinkdiary.domain.model.SurfaceStyle
import com.bluemarlin.drinkdiary.ui.component.DDCenteredContent
import io.github.bluemarlins.aura.component.AuraButton
import io.github.bluemarlins.aura.component.AuraButtonSize
import io.github.bluemarlins.aura.component.AuraButtonVariant
import io.github.bluemarlins.aura.component.AuraCard
import io.github.bluemarlins.aura.component.AuraCardPadding
import io.github.bluemarlins.aura.component.AuraCardVariant
import io.github.bluemarlins.aura.component.AuraDivider
import io.github.bluemarlins.aura.component.AuraSegmentedButton
import io.github.bluemarlins.aura.component.AuraSegmentedSize
import io.github.bluemarlins.aura.component.AuraText
import io.github.bluemarlins.aura.component.BindAdaptiveBarToScrollState
import io.github.bluemarlins.aura.component.rememberFloatingScrollProgress
import io.github.bluemarlins.aura.foundation.theme.AuraTheme

// 지표 커스터마이징은 열지 않는다. 로컬 데이터 보존(백업·복원)과 화면 재질·색 모드만 제공한다.
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onExportBackup: (Uri) -> Unit,
    onRestoreBackup: (Uri) -> Unit,
    onSurfaceStyleChange: (SurfaceStyle) -> Unit,
    onColorModeChange: (ColorModePreference) -> Unit,
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
                    text = "로그인이 필요 없고, 기록을 서버로 보내지 않아요. 내 드라이브나 기기에 백업 파일로 저장할 수 있어요.",
                    style = AuraTheme.typography.bodySmall,
                    color = AuraTheme.colors.content.secondary,
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AuraButton(
                text = "백업 파일 저장",
                onClick = { createBackupLauncher.launch("glass-by-glass-backup.json") },
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

        AppearanceCard(
            appearance = state.appearance,
            onSurfaceStyleChange = onSurfaceStyleChange,
            onColorModeChange = onColorModeChange,
        )

        AuraDivider()

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            AuraText("평가 기준", style = AuraTheme.typography.titleSmall)
            AuraText(
                text =
                    "와인과 위스키 모두 맛 기준 6가지로 기록해요. " +
                        "기록이 쌓이면 취향 유형과 추천이 자동으로 계산돼요.",
                style = AuraTheme.typography.bodySmall,
                color = AuraTheme.colors.content.secondary,
            )
        }
    }
}

private val SurfaceStyleOptions =
    listOf(
        SurfaceStyle.LiquidGlass to "리퀴드 글래스",
        SurfaceStyle.GlassMorphism to "글래스모피즘",
        SurfaceStyle.Solid to "솔리드",
    )

private val ColorModeOptions =
    listOf(
        ColorModePreference.System to "시스템",
        ColorModePreference.Light to "라이트",
        ColorModePreference.Dark to "다크",
    )

@Composable
private fun AppearanceCard(
    appearance: AppearanceSettings,
    onSurfaceStyleChange: (SurfaceStyle) -> Unit,
    onColorModeChange: (ColorModePreference) -> Unit,
) {
    AuraCard(
        modifier = Modifier.fillMaxWidth(),
        variant = AuraCardVariant.Outlined,
        padding = AuraCardPadding.Large,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AuraText("화면", style = AuraTheme.typography.titleMedium)

            AuraText("재질", style = AuraTheme.typography.titleSmall)
            AuraSegmentedButton(
                options = SurfaceStyleOptions.map { it.second },
                selectedIndex = SurfaceStyleOptions.indexOfFirst { it.first == appearance.surfaceStyle },
                onOptionSelected = { index -> onSurfaceStyleChange(SurfaceStyleOptions[index].first) },
                size = AuraSegmentedSize.Small,
            )
            AuraText(
                text = surfaceStyleCaption(appearance.surfaceStyle, AuraTheme.reduceTransparency),
                style = AuraTheme.typography.bodySmall,
                color = AuraTheme.colors.content.secondary,
            )

            AuraText("색 모드", style = AuraTheme.typography.titleSmall)
            AuraSegmentedButton(
                options = ColorModeOptions.map { it.second },
                selectedIndex = ColorModeOptions.indexOfFirst { it.first == appearance.colorMode },
                onOptionSelected = { index -> onColorModeChange(ColorModeOptions[index].first) },
                size = AuraSegmentedSize.Small,
            )
        }
    }
}

private fun surfaceStyleCaption(
    style: SurfaceStyle,
    reduceTransparency: Boolean,
): String {
    // 절전 모드·고대비 텍스트에서는 Aura가 유리 재질을 단색으로 낮춘다. 고른 값이 안 먹는 것처럼 보이지 않게 알린다.
    if (reduceTransparency && style != SurfaceStyle.Solid) {
        return "절전 모드나 고대비 텍스트가 켜져 있어 지금은 솔리드로 보여요."
    }
    return when (style) {
        SurfaceStyle.LiquidGlass -> "하단 탭과 상단 바가 빛을 굴절하는 유리 렌즈처럼 보여요."
        SurfaceStyle.GlassMorphism -> "뒤에 지나가는 화면을 부드럽게 흐리는 반투명 유리예요."
        SurfaceStyle.Solid -> "투명 효과 없이 또렷한 불투명 면을 써요. 배터리를 아낄 때 좋아요."
    }
}
