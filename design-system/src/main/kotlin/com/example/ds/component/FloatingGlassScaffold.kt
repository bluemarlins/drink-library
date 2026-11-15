package com.example.ds.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ds.foundation.theme.AuraTheme
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Computes the exact bottom content clearance needed for scrollable lists so the final item
 * never hides behind [FloatingBottomIsland] or the system navigation bar.
 */
fun calculateBottomIslandContentPadding(
    navBarBottomInset: Dp,
    islandHeight: Dp = 60.dp,
    bottomMargin: Dp = 16.dp,
    extraBreathing: Dp = 16.dp,
): Dp = navBarBottomInset + islandHeight + bottomMargin + extraBreathing

/**
 * Computes continuous `[0f, 1f]` scroll progress over [threshold] (`0dp..64dp`) from a [LazyListState]
 * and keeps [LocalGlassBackdropState] invalidated as items pass beneath floating glass bars.
 */
@Composable
fun rememberFloatingScrollProgress(
    listState: LazyListState,
    threshold: Dp = 64.dp,
): Float {
    val density = LocalDensity.current
    val backdropState = LocalGlassBackdropState.current
    val thresholdPx = remember(density, threshold) {
        with(density) { threshold.toPx().coerceAtLeast(1f) }
    }
    val progress by remember(listState, thresholdPx) {
        derivedStateOf {
            calculateScrollProgress(
                firstVisibleIndex = listState.firstVisibleItemIndex,
                firstVisibleOffsetPx = listState.firstVisibleItemScrollOffset,
                thresholdPx = thresholdPx,
            )
        }
    }
    LaunchedEffect(listState, backdropState) {
        if (backdropState == null) return@LaunchedEffect
        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }
            .distinctUntilChanged()
            .collect {
                backdropState.invalidateBackdrop()
            }
    }
    return progress
}

/**
 * Computes continuous `[0f, 1f]` scroll progress over [threshold] (`0dp..64dp`) from a [ScrollState]
 * and keeps [LocalGlassBackdropState] invalidated as content passes beneath floating glass bars.
 */
@Composable
fun rememberFloatingScrollProgress(
    scrollState: ScrollState,
    threshold: Dp = 64.dp,
): Float {
    val density = LocalDensity.current
    val backdropState = LocalGlassBackdropState.current
    val thresholdPx = remember(density, threshold) {
        with(density) { threshold.toPx().coerceAtLeast(1f) }
    }
    val progress by remember(scrollState, thresholdPx) {
        derivedStateOf {
            calculateScrollProgress(
                firstVisibleIndex = 0,
                firstVisibleOffsetPx = scrollState.value,
                thresholdPx = thresholdPx,
            )
        }
    }
    LaunchedEffect(scrollState, backdropState) {
        if (backdropState == null) return@LaunchedEffect
        snapshotFlow { scrollState.value }
            .distinctUntilChanged()
            .collect {
                backdropState.invalidateBackdrop()
            }
    }
    return progress
}

/**
 * Spatial 4-Layer Edge-to-Edge Scaffold:
 * - **Layer 0 (Base Canvas):** 100% full-bleed scrollable canvas recorded via [rememberGlassBackdropState]
 *   so floating bars see real scrolled content passing behind them.
 * - **Layer 1 (Edge Scrim):** Progressive protective top and bottom gradient scrims without opaque cutouts.
 * - **Layer 2 (Floating Chrome):** [topBar] and [bottomBar] floating cleanly above Layer 0/1.
 * - **Layer 3 (Transient Overlay):** Full-screen tap-to-dismiss + Morphing [overlay] slot.
 *
 * Automatically computes bottom safe `contentPadding` (`islandHeight + navigationBars + bottomMargin + breathing`)
 * so final scrollable items clear the Floating Bottom Island cleanly.
 */
@Composable
fun FloatingGlassScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable BoxScope.(GlassBackdropState) -> Unit = {},
    bottomBar: @Composable BoxScope.(GlassBackdropState) -> Unit = {},
    overlay: @Composable BoxScope.(GlassBackdropState) -> Unit = {},
    overlayVisible: Boolean = false,
    onDismissOverlay: () -> Unit = {},
    hasBottomIsland: Boolean = true,
    bottomIslandHeight: Dp = 60.dp,
    bottomIslandMargin: Dp = 16.dp,
    containerColor: Color = AuraTheme.colors.background.primary,
    contentColor: Color = AuraTheme.colors.content.primary,
    content: @Composable (PaddingValues) -> Unit,
) {
    val colors = AuraTheme.colors
    val backdropState = rememberGlassBackdropState()

    val statusTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val contentPadding = PaddingValues(
        top = statusTopPadding + 10.dp + 54.dp,
        bottom = if (hasBottomIsland) {
            calculateBottomIslandContentPadding(
                navBarBottomInset = navBottomPadding,
                islandHeight = bottomIslandHeight,
                bottomMargin = bottomIslandMargin,
                extraBreathing = 16.dp,
            )
        } else {
            navBottomPadding + 16.dp
        },
    )

    val edgeScrimBase = lerp(
        start = colors.background.primary,
        stop = colors.interaction.scrim.copy(alpha = 1f),
        fraction = 0.22f,
    )

    ProvideAuraContentColorTextStyle(
        contentColor = contentColor,
        textStyle = AuraTheme.typography.bodyMedium,
    ) {
        CompositionLocalProvider(LocalGlassBackdropState provides backdropState) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(containerColor),
            ) {
                // Layer 0: Base Edge-to-Edge Scrollable Canvas (captured for real blur/refraction)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .glassBackdropSource(backdropState),
                ) {
                    content(contentPadding)
                }

                // Layer 1: Progressive Edge Scrims (protects contrast without cutting opaque slabs)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(statusTopPadding + 76.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    edgeScrimBase.copy(alpha = 0.42f),
                                    edgeScrimBase.copy(alpha = 0.14f),
                                    Color.Transparent,
                                ),
                            ),
                        ),
                )

                if (hasBottomIsland) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(navBottomPadding + bottomIslandHeight + bottomIslandMargin + 32.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        edgeScrimBase.copy(alpha = 0.14f),
                                        edgeScrimBase.copy(alpha = 0.42f),
                                    ),
                                ),
                            ),
                    )
                }

                // Layer 2: Floating Chrome (Top Pill Bar & Bottom Island)
                topBar(backdropState)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                ) {
                    bottomBar(backdropState)
                }

                // Layer 3: Transient Overlay Scrim + Morphing Popover Layer
                if (overlayVisible) {
                    val dismissInteraction = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colors.interaction.scrim.copy(alpha = 0.14f))
                            .clickable(
                                interactionSource = dismissInteraction,
                                indication = null,
                                onClick = onDismissOverlay,
                            ),
                    )
                }
                overlay(backdropState)
            }
        }
    }
}
