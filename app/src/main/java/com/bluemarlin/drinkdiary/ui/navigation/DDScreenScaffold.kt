package com.bluemarlin.drinkdiary.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.R
import com.example.ds.component.AuraAdaptiveBarState
import com.example.ds.component.AuraBottomIslandItem
import com.example.ds.component.AuraIcon
import com.example.ds.component.AuraIconButton
import com.example.ds.component.AuraIconButtonVariant
import com.example.ds.component.AuraScaffold
import com.example.ds.component.AuraTopAppBar
import com.example.ds.component.AuraTopAppBarScrollBehavior
import com.example.ds.component.AuraTopAppBarStyle
import com.example.ds.component.AuraTopAppBarSurface
import com.example.ds.component.FloatingBottomIsland
import com.example.ds.component.FloatingGlassScaffold
import com.example.ds.component.GlassStyle
import com.example.ds.component.rememberAuraAdaptiveBarState
import com.example.ds.foundation.motion.AuraMotionProfile
import com.example.ds.foundation.theme.AuraTheme

enum class DDScreenType {
    TopLevel,
    Detail,
    Editor,
}

enum class DDTopLevelTab {
    Dashboard,
    Collection,
    Search,
}

// Aura 1.1.0의 Navigation 원칙(NAV-RULE-001): TopLevel·Detail은 FloatingGlassScaffold + FloatingCapsule,
// Editor(고밀도 입력 폼)는 AuraScaffold + SurfaceDocked를 사용한다.
@Composable
fun DDScreenScaffold(
    title: String,
    screenType: DDScreenType,
    selectedTab: DDTopLevelTab? = null,
    onDashboardClick: (() -> Unit)? = null,
    onCollectionClick: (() -> Unit)? = null,
    onSearchClick: (() -> Unit)? = null,
    onAddClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    scrollFraction: Float = 0f,
    adaptiveBarState: AuraAdaptiveBarState = rememberAuraAdaptiveBarState(),
    toolbarActions: @Composable RowScope.() -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
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
                topBar = { _ ->
                    StatusBarScrim(visibility = scrollFraction)
                    AuraTopAppBar(
                        title = title,
                        style = AuraTopAppBarStyle.FloatingCapsule,
                        surface = AuraTopAppBarSurface.TranslucentGlass,
                        scrollBehavior = AuraTopAppBarScrollBehavior.AdaptiveFloating,
                        scrollFraction = scrollFraction,
                        adaptiveState = adaptiveBarState,
                        navigationIcon = navIcon,
                        actions = toolbarActions,
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
                            onSearchClick = onSearchClick,
                            onAddClick = onAddClick,
                        )
                    }
                },
                hasBottomIsland = true,
                snackbarHost = snackbarHost,
                content = { padding -> OpaqueBackdrop { content(padding) } },
            )
        }

        DDScreenType.Detail -> {
            FloatingGlassScaffold(
                modifier = Modifier.fillMaxSize(),
                adaptiveBarState = adaptiveBarState,
                topBar = { _ ->
                    StatusBarScrim(visibility = scrollFraction)
                    AuraTopAppBar(
                        title = title,
                        style = AuraTopAppBarStyle.FloatingCapsule,
                        surface = AuraTopAppBarSurface.TranslucentGlass,
                        scrollBehavior = AuraTopAppBarScrollBehavior.AdaptiveFloating,
                        scrollFraction = scrollFraction,
                        adaptiveState = adaptiveBarState,
                        navigationIcon = navIcon,
                        actions = toolbarActions,
                    )
                },
                hasBottomIsland = false,
                snackbarHost = snackbarHost,
                content = { padding -> OpaqueBackdrop { content(padding) } },
            )
        }

        DDScreenType.Editor -> {
            AuraScaffold(
                topBar = {
                    AuraTopAppBar(
                        title = title,
                        style = AuraTopAppBarStyle.SurfaceDocked,
                        surface = AuraTopAppBarSurface.SolidElevated,
                        scrollFraction = scrollFraction,
                        navigationIcon = navIcon,
                        actions = toolbarActions,
                    )
                },
                snackbarHost = snackbarHost,
                content = content,
            )
        }
    }
}

// Aura의 FloatingGlassScaffold는 adaptiveBarState를 받으면 상단 스크림을 끈다. 그러면 스크롤된
// 본문이 상태바 아이콘과 떠 있는 버튼 아래로 그대로 지나가 글자가 겹친다(실기기 점검 P1-4).
// 상태바 영역은 배경색으로 덮고, 떠 있는 버튼 높이만큼은 서서히 걷어 낸다.
@Composable
private fun BoxScope.StatusBarScrim(visibility: Float) {
    val alpha = visibility.coerceIn(0f, 1f)
    if (alpha <= 0f) return
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val background = AuraTheme.colors.background.primary
    val density = LocalDensity.current
    val solidUntil = with(density) { statusBar.toPx() }
    val fadeTo = with(density) { (statusBar + STATUS_SCRIM_FADE).toPx() }
    Box(
        modifier =
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(statusBar + STATUS_SCRIM_FADE)
                .graphicsLayer { this.alpha = alpha }
                .background(
                    Brush.verticalGradient(
                        0f to background,
                        (solidUntil / fadeTo) to background,
                        1f to background.copy(alpha = 0f),
                        startY = 0f,
                        endY = fadeTo,
                    ),
                ),
    )
}

private val STATUS_SCRIM_FADE = 72.dp

// FloatingGlassScaffold는 배경색을 유리가 흐리게 베끼는 콘텐츠 레이어 바깥에 칠한다. 그러면 흐린 사본이
// 투명한 바탕 위에 그려져, 그 아래 선명한 원본이 하단 바 안으로 그대로 비친다(실기기 점검 P1-5).
// 배경을 콘텐츠 레이어 안에 깔아 사본이 원본을 덮게 한다.
@Composable
private fun OpaqueBackdrop(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(AuraTheme.colors.background.primary),
    ) {
        content()
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
    onSearchClick: (() -> Unit)?,
    onAddClick: (() -> Unit)?,
) {
    val home = ImageVector.vectorResource(R.drawable.ic_home)
    val collection = ImageVector.vectorResource(R.drawable.ic_collection)
    val search = ImageVector.vectorResource(R.drawable.ic_search)
    val addIcon = ImageVector.vectorResource(R.drawable.ic_add)
    val labels =
        Triple(
            stringResource(R.string.nav_dashboard),
            stringResource(R.string.nav_collection),
            stringResource(R.string.nav_search),
        )

    val entries =
        buildList {
            onDashboardClick?.let {
                add(Triple(DDTopLevelTab.Dashboard, AuraBottomIslandItem("dashboard", labels.first, home), it))
            }
            onCollectionClick?.let {
                add(Triple(DDTopLevelTab.Collection, AuraBottomIslandItem("collection", labels.second, collection), it))
            }
            onSearchClick?.let {
                add(Triple(DDTopLevelTab.Search, AuraBottomIslandItem("search", labels.third, search), it))
            }
        }
    if (entries.isEmpty()) return

    FloatingBottomIsland(
        items = entries.map { it.second },
        selectedIndex = entries.indexOfFirst { it.first == selectedTab }.coerceAtLeast(0),
        onItemSelected = { index -> entries[index].third() },
        glassStyle = GlassStyle.FROSTED,
        splitActionIcon = if (onAddClick != null) addIcon else null,
        splitActionLabel = if (onAddClick != null) "기록하기" else null,
        isActionSplit = onAddClick != null,
        onSplitActionClick = onAddClick,
    )
}
