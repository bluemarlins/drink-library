package com.bluemarlin.drinkdiary.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.R
import io.github.bluemarlins.aura.component.AuraAdaptiveBarState
import io.github.bluemarlins.aura.component.AuraBottomIslandItem
import io.github.bluemarlins.aura.component.AuraFloatingActionBarDefaults
import io.github.bluemarlins.aura.component.AuraIcon
import io.github.bluemarlins.aura.component.AuraIconButton
import io.github.bluemarlins.aura.component.AuraIconButtonVariant
import io.github.bluemarlins.aura.component.AuraTopAppBar
import io.github.bluemarlins.aura.component.AuraTopAppBarScrollBehavior
import io.github.bluemarlins.aura.component.AuraTopAppBarStyle
import io.github.bluemarlins.aura.component.AuraTopAppBarSurface
import io.github.bluemarlins.aura.component.FloatingBottomIsland
import io.github.bluemarlins.aura.component.FloatingGlassScaffold
import io.github.bluemarlins.aura.component.GlassStyle
import io.github.bluemarlins.aura.component.rememberAuraAdaptiveBarState
import io.github.bluemarlins.aura.foundation.layout.AuraHeightClass
import io.github.bluemarlins.aura.foundation.motion.AuraMotionProfile
import io.github.bluemarlins.aura.foundation.theme.AuraTheme

enum class DDScreenType {
    TopLevel,
    Detail,
    Editor,
}

// 하단 탭 = 화면 제목: 요약 · 일지 · 설정 (branding.md 7-2). 매장 찾기는 2026-11-19 제거(prd.md F5).
enum class DDTopLevelTab {
    Dashboard,
    Collection,
    Settings,
}

// Aura Navigation 원칙(NAV-RULE-001): 모든 화면이 FloatingGlassScaffold + FloatingCapsule을 쓴다.
// TopLevel은 하단 내비게이션 아일랜드, Detail·Editor는 선택적 하단 액션 독(AuraFloatingActionBar)을 둔다.
@Composable
fun DDScreenScaffold(
    title: String,
    screenType: DDScreenType,
    subtitle: String? = null,
    selectedTab: DDTopLevelTab? = null,
    onDashboardClick: (() -> Unit)? = null,
    onCollectionClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    onAddClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    scrollFraction: Float = 0f,
    adaptiveBarState: AuraAdaptiveBarState = rememberAuraAdaptiveBarState(minimizeOnScrollDown = true),
    toolbarActions: @Composable RowScope.() -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    bottomAction: (@Composable () -> Unit)? = null,
    bottomActionHeight: Dp = AuraFloatingActionBarDefaults.Height,
    content: @Composable (PaddingValues) -> Unit,
) {
    val backLabel = stringResource(R.string.back)
    val navIcon: (@Composable () -> Unit)? =
        onBackClick?.let { onBack ->
            {
                AuraIconButton(
                    onClick = onBack,
                    contentDescription = backLabel,
                    variant = AuraIconButtonVariant.Ghost,
                    icon = {
                        AuraIcon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_back),
                            contentDescription = null,
                        )
                    },
                )
            }
        }

    when (screenType) {
        DDScreenType.TopLevel -> {
            FloatingGlassScaffold(
                modifier = Modifier.fillMaxSize(),
                adaptiveBarState = adaptiveBarState,
                // 큰 제목이 액션 줄에 들어가 앱바가 한 줄(56dp)이다. 기본값 112dp는 두 줄 기준이라 빈 띠가 남는다.
                topBarClearance = oneLineTopBarClearance(),
                topBar = { _ ->
                    // 큰 제목을 액션 줄에 둔다. 기본 배치는 빈 액션 줄 아래로 제목을 한 줄 내려 그린다.
                    AuraTopAppBar(
                        title = title,
                        style = AuraTopAppBarStyle.FloatingCapsule,
                        surface = AuraTopAppBarSurface.TranslucentGlass,
                        scrollBehavior = AuraTopAppBarScrollBehavior.AdaptiveFloating,
                        scrollFraction = scrollFraction,
                        adaptiveState = adaptiveBarState,
                        navigationIcon = navIcon,
                        actions = toolbarActions,
                        expandedSubtitle = subtitle,
                        inlineExpandedTitle = true,
                    )
                },
                bottomBar = { _ ->
                    // MOT-RULE-003: 화면당 최대 1개의 focal spatial chrome에만 Expressive 트랙을 중첩 적용한다.
                    AuraTheme(
                        colorMode = AuraTheme.colorMode,
                        material = AuraTheme.material,
                        motionProfile = AuraMotionProfile.Expressive,
                        reduceTransparency = AuraTheme.reduceTransparency,
                        glass = AuraTheme.glass,
                        windowInfo = AuraTheme.window,
                    ) {
                        DDBottomNavigation(
                            selectedTab = selectedTab,
                            onDashboardClick = onDashboardClick,
                            onCollectionClick = onCollectionClick,
                            onSettingsClick = onSettingsClick,
                            onAddClick = onAddClick,
                        )
                    }
                },
                hasBottomIsland = true,
                snackbarHost = snackbarHost,
                content = content,
            )
        }

        // 상세·편집은 같은 floating 크롬을 쓴다(Aura NAV-RULE-001: 짧은 작업 화면도 floating 유지).
        // 저장·고치기 같은 주 액션은 폼 끝 인라인 버튼 대신 하단 AuraFloatingActionBar에 둔다.
        DDScreenType.Detail, DDScreenType.Editor -> {
            FloatingGlassScaffold(
                modifier = Modifier.fillMaxSize(),
                adaptiveBarState = adaptiveBarState,
                topBarClearance = oneLineTopBarClearance(),
                topBar = { _ ->
                    AuraTopAppBar(
                        title = title,
                        style = AuraTopAppBarStyle.FloatingCapsule,
                        surface = AuraTopAppBarSurface.TranslucentGlass,
                        scrollBehavior = AuraTopAppBarScrollBehavior.AdaptiveFloating,
                        scrollFraction = scrollFraction,
                        adaptiveState = adaptiveBarState,
                        navigationIcon = navIcon,
                        actions = toolbarActions,
                        // 상세는 본문 첫 줄이 이미 술 이름이라 큰 제목을 두 번 쓰지 않는다. 스크롤하면 캡슐 제목으로 나타난다.
                        expandedTitle = if (screenType == DDScreenType.Detail) "" else null,
                        inlineExpandedTitle = screenType == DDScreenType.Editor,
                    )
                },
                bottomBar = { _ -> bottomAction?.invoke() },
                hasBottomIsland = bottomAction != null,
                bottomIslandHeight = bottomActionHeight,
                bottomIslandMargin = AuraFloatingActionBarDefaults.BottomMargin,
                snackbarHost = snackbarHost,
                content = content,
            )
        }
    }
}

// 핸들러가 없는 탭은 그리지 않는다. 눌러도 아무 일이 없는 탭을 노출하면 사용자는 앱이
// 고장난 것으로 읽는다(실기기에서 확인된 결함) — 화면이 생기면 핸들러와 함께 나타난다.
@Suppress("DEPRECATION")
@Composable
private fun DDBottomNavigation(
    selectedTab: DDTopLevelTab?,
    onDashboardClick: (() -> Unit)?,
    onCollectionClick: (() -> Unit)?,
    onSettingsClick: (() -> Unit)?,
    onAddClick: (() -> Unit)?,
) {
    val home = ImageVector.vectorResource(R.drawable.ic_home)
    val collection = ImageVector.vectorResource(R.drawable.ic_collection)
    val settings = ImageVector.vectorResource(R.drawable.ic_settings)
    val addIcon = ImageVector.vectorResource(R.drawable.ic_add)
    val labels =
        Triple(
            stringResource(R.string.nav_dashboard),
            stringResource(R.string.nav_collection),
            stringResource(R.string.nav_settings),
        )

    val entries =
        buildList {
            onDashboardClick?.let {
                add(Triple(DDTopLevelTab.Dashboard, AuraBottomIslandItem("dashboard", labels.first, home), it))
            }
            onCollectionClick?.let {
                add(Triple(DDTopLevelTab.Collection, AuraBottomIslandItem("collection", labels.second, collection), it))
            }
            onSettingsClick?.let {
                add(Triple(DDTopLevelTab.Settings, AuraBottomIslandItem("settings", labels.third, settings), it))
            }
        }
    if (entries.isEmpty()) return

    FloatingBottomIsland(
        items = entries.map { it.second },
        selectedIndex = entries.indexOfFirst { it.first == selectedTab }.coerceAtLeast(0),
        onItemSelected = { index -> entries[index].third() },
        glassStyle = GlassStyle.LIQUID,
        splitActionIcon = if (onAddClick != null) addIcon else null,
        splitActionLabel = if (onAddClick != null) "기록하기" else null,
        isActionSplit = onAddClick != null,
        onSplitActionClick = onAddClick,
    )
}

@Composable
private fun oneLineTopBarClearance(): Dp = if (AuraTheme.window.heightClass == AuraHeightClass.Compact) 48.dp else 64.dp
