package com.example.ds.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.dp
import com.example.ds.foundation.theme.AuraTheme
import kotlin.math.max

private enum class AuraScaffoldSlot {
    TopBar,
    BottomBar,
    FloatingActionButton,
    Content,
}

private val FabMargin = 16.dp

/**
 * Root screen container implementing the `aura.component.scaffold` specification.
 *
 * Places [topBar], [bottomBar], [floatingActionButton], and [content] with zero Material 3
 * dependency while propagating safe-area [PaddingValues] and Aura content color/text defaults.
 */
@Composable
fun AuraScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    containerColor: Color = AuraTheme.colors.background.primary,
    contentColor: Color = AuraTheme.colors.content.primary,
    contentWindowInsets: WindowInsets = WindowInsets.systemBars,
    content: @Composable (PaddingValues) -> Unit,
) {
    ProvideAuraContentColorTextStyle(
        contentColor = contentColor,
        textStyle = AuraTheme.typography.bodyMedium,
    ) {
        SubcomposeLayout(
            modifier = modifier
                .fillMaxSize()
                .background(containerColor),
        ) { constraints ->
            val layoutWidth = constraints.maxWidth
            val layoutHeight = constraints.maxHeight
            val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

            val topBarPlaceables = subcompose(AuraScaffoldSlot.TopBar, topBar)
                .map { it.measure(looseConstraints) }
            val topBarHeight = topBarPlaceables.maxOfOrNull { it.height } ?: 0

            val bottomBarPlaceables = subcompose(AuraScaffoldSlot.BottomBar, bottomBar)
                .map { it.measure(looseConstraints) }
            val bottomBarHeight = bottomBarPlaceables.maxOfOrNull { it.height } ?: 0

            val fabPlaceables = subcompose(AuraScaffoldSlot.FloatingActionButton, floatingActionButton)
                .map { it.measure(looseConstraints) }
            val fabWidth = fabPlaceables.maxOfOrNull { it.width } ?: 0
            val fabHeight = fabPlaceables.maxOfOrNull { it.height } ?: 0

            val insetLeft = contentWindowInsets.getLeft(this, layoutDirection)
            val insetTop = contentWindowInsets.getTop(this)
            val insetRight = contentWindowInsets.getRight(this, layoutDirection)
            val insetBottom = contentWindowInsets.getBottom(this)

            val topPaddingPx = if (topBarHeight > 0) topBarHeight else insetTop
            val bottomPaddingPx = if (bottomBarHeight > 0) bottomBarHeight else insetBottom

            val innerPadding = PaddingValues(
                start = insetLeft.toDp(),
                top = topPaddingPx.toDp(),
                end = insetRight.toDp(),
                bottom = bottomPaddingPx.toDp(),
            )

            val contentPlaceables = subcompose(AuraScaffoldSlot.Content) {
                Box(modifier = Modifier.fillMaxSize()) {
                    content(innerPadding)
                }
            }.map { it.measure(looseConstraints) }

            val fabMarginPx = FabMargin.roundToPx()
            val fabX = (layoutWidth - insetRight - fabMarginPx - fabWidth).coerceAtLeast(0)
            val fabBottomOffset = max(bottomBarHeight, insetBottom)
            val fabY = (layoutHeight - fabBottomOffset - fabMarginPx - fabHeight).coerceAtLeast(0)

            layout(layoutWidth, layoutHeight) {
                contentPlaceables.forEach { it.place(0, 0) }
                topBarPlaceables.forEach { it.place(0, 0) }
                bottomBarPlaceables.forEach { it.place(0, layoutHeight - it.height) }
                fabPlaceables.forEach { it.place(fabX, fabY) }
            }
        }
    }
}
