package com.bluemarlin.drinkdiary.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
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
                content = content,
            )
        }

        DDScreenType.Detail -> {
            FloatingGlassScaffold(
                modifier = Modifier.fillMaxSize(),
                adaptiveBarState = adaptiveBarState,
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
                    )
                },
                hasBottomIsland = false,
                snackbarHost = snackbarHost,
                content = content,
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
        glassStyle = GlassStyle.LIQUID,
        splitActionIcon = if (onAddClick != null) addIcon else null,
        splitActionLabel = if (onAddClick != null) "기록하기" else null,
        isActionSplit = onAddClick != null,
        onSplitActionClick = onAddClick,
    )
}
