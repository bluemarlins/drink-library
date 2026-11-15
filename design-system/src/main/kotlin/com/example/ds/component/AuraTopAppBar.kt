package com.example.ds.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.example.ds.foundation.theme.AuraTheme
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

enum class AuraTopAppBarStyle {
    FloatingCapsule,
    FloatingSplit,
    SurfaceDocked,
}

enum class AuraTopAppBarSurface {
    SolidElevated,
    TranslucentGlass,
}

enum class AuraTopAppBarScrollBehavior {
    Pinned,
    EnterAlways,
    CompactOnScroll,
}

data class AuraMoreMenuAction(
    val id: String,
    val label: String,
)

/**
 * Scroll-reactive Adaptive Floating Top Pill Bar supporting State A (transparent/hero expanded)
 * and State B (Full Capsule floating pills over `0dp..64dp` scroll progress), compact center title
 * pill with scroll-direction reveal, grouped `[Search, Bookmark, More]` capsule with continuous
 * Metaball split/merge physics, and `>= 48.dp` accessible hit bounds.
 */
@Composable
fun FloatingTopPillBar(
    title: String,
    scrollProgress: Float,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    glassStyle: GlassStyle = GlassStyle.LIQUID,
    isScrollingDown: Boolean = false,
    hideTitleOnScrollDown: Boolean = true,
    metaballSplitProgress: Float = 0f,
    onBackClick: (() -> Unit)? = null,
    onSearchClick: (() -> Unit)? = null,
    isBookmarked: Boolean = false,
    onBookmarkClick: (() -> Unit)? = null,
    moreMenuExpanded: Boolean = false,
    onMoreMenuExpandedChange: (Boolean) -> Unit = {},
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val motion = AuraTheme.motion
    val reducedMotion = rememberAuraReducedMotion()

    val fluidSpring = remember(reducedMotion) {
        if (reducedMotion) snap<Float>() else spring(dampingRatio = 0.65f, stiffness = 450f)
    }

    val clampedScroll = scrollProgress.coerceIn(0f, 1f)
    val smoothScroll by animateFloatAsState(
        targetValue = clampedScroll,
        animationSpec = if (reducedMotion) snap() else motion.pressOutSpring(),
        label = "TopPillScrollProgress",
    )
    val smoothSplit by animateFloatAsState(
        targetValue = metaballSplitProgress.coerceIn(0f, 1f),
        animationSpec = fluidSpring,
        label = "TopPillMetaballSplit",
    )

    // State A (0.32 subtle protection) -> State B (1.0 full condensed floating capsule)
    val capsuleOpacity = (0.32f + (0.68f * smoothScroll)).coerceIn(0f, 1f)
    val capsuleScale = 1.03f - (0.03f * smoothScroll)
    val showCapsuleShadow = smoothScroll > 0.08f
    val moreHandoffAlpha by animateFloatAsState(
        targetValue = if (moreMenuExpanded) 0f else 1f,
        animationSpec = fluidSpring,
        label = "TopPillMoreHandoff",
    )

    // Center Compact Title Pill rises cleanly once large hero title scrolls off; optional hide on scroll-down
    val titleTargetVisible = smoothScroll > 0.35f && !(isScrollingDown && hideTitleOnScrollDown && !moreMenuExpanded)
    val titleReveal by animateFloatAsState(
        targetValue = if (titleTargetVisible) 1f else 0f,
        animationSpec = fluidSpring,
        label = "TopPillTitleReveal",
    )

    val dispersionBoost = 1f + (smoothSplit * 0.55f) + ((1f - abs(smoothScroll * 2f - 1f)) * 0.25f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Left: 40x40dp circular capsule inside >= 48x48dp accessible hit area
        if (onBackClick != null) {
            val backInteraction = remember { MutableInteractionSource() }
            val backClick = rememberAuraDebouncedClick(onClick = onBackClick, debounceIntervalMs = 250L)
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    .fluidPressScale(backInteraction)
                    .graphicsLayer {
                        scaleX = capsuleScale
                        scaleY = capsuleScale
                    }
                    .semantics { contentDescription = "Back" }
                    .clickable(
                        interactionSource = backInteraction,
                        indication = null,
                        role = Role.Button,
                        onClick = backClick,
                    ),
                contentAlignment = Alignment.CenterStart,
            ) {
                GlassSurface(
                    modifier = Modifier.size(40.dp),
                    glassStyle = glassStyle,
                    shape = CircleShape,
                    cornerRadiusHint = 20.dp,
                    surfaceAlphaMultiplier = capsuleOpacity,
                    dispersionBoost = dispersionBoost,
                    showShadow = showCapsuleShadow,
                ) {
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        PillBackGlyph(color = colors.content.primary)
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.width(48.dp))
        }

        // Center: Compact Title Pill that emerges smoothly on scroll
        if (titleReveal > 0.03f) {
            GlassSurface(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(horizontal = 8.dp)
                    .height(40.dp)
                    .widthIn(min = 96.dp, max = 220.dp)
                    .graphicsLayer {
                        val s = 0.86f + (0.14f * titleReveal)
                        scaleX = s
                        scaleY = s
                        alpha = titleReveal.coerceIn(0f, 1f)
                        translationY = (1f - titleReveal) * -10f
                    },
                glassStyle = glassStyle,
                shape = RoundedCornerShape(50),
                cornerRadiusHint = 20.dp,
                surfaceAlphaMultiplier = titleReveal.coerceIn(0f, 1f),
                dispersionBoost = dispersionBoost,
            ) {
                Column(
                    modifier = Modifier
                        .height(40.dp)
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    AuraText(
                        text = title,
                        style = typography.labelLarge,
                        color = colors.content.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.semantics { heading() },
                    )
                    if (subtitle != null) {
                        AuraText(
                            text = subtitle,
                            style = typography.labelSmall,
                            color = colors.content.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        // Right: Unified Grouped Action Pill [Search, Bookmark, More] with Metaball split capability
        GroupedRightActionCluster(
            glassStyle = glassStyle,
            surfaceAlphaMultiplier = capsuleOpacity,
            capsuleScale = capsuleScale,
            showShadow = showCapsuleShadow,
            dispersionBoost = dispersionBoost,
            splitProgress = smoothSplit,
            moreHandoffAlpha = moreHandoffAlpha,
            onSearchClick = onSearchClick,
            isBookmarked = isBookmarked,
            onBookmarkClick = onBookmarkClick,
            moreMenuExpanded = moreMenuExpanded,
            onMoreClick = { onMoreMenuExpandedChange(!moreMenuExpanded) },
        )
    }
}

@Composable
private fun GroupedRightActionCluster(
    glassStyle: GlassStyle,
    surfaceAlphaMultiplier: Float,
    capsuleScale: Float,
    showShadow: Boolean,
    dispersionBoost: Float,
    splitProgress: Float,
    moreHandoffAlpha: Float,
    onSearchClick: (() -> Unit)?,
    isBookmarked: Boolean,
    onBookmarkClick: (() -> Unit)?,
    moreMenuExpanded: Boolean,
    onMoreClick: () -> Unit,
) {
    val colors = AuraTheme.colors
    val gapDp = 16.dp * splitProgress
    val rawMerge = (1f - ((splitProgress - 0.08f) / 0.72f)).coerceIn(0f, 1f)
    val mergeProgress = if (glassStyle == GlassStyle.LIQUID) rawMerge else 0f

    if (splitProgress <= 0.04f) {
        // Unified Single Grouped Pill (visual height 42dp, padding 12dp, spacing 10dp; 48dp hit target)
        Box(
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .height(48.dp)
                .graphicsLayer {
                    scaleX = capsuleScale
                    scaleY = capsuleScale
                },
            contentAlignment = Alignment.Center,
        ) {
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                glassStyle = glassStyle,
                shape = RoundedCornerShape(50),
                cornerRadiusHint = 21.dp,
                surfaceAlphaMultiplier = surfaceAlphaMultiplier,
                dispersionBoost = dispersionBoost,
                showShadow = showShadow,
            ) {}

            Row(
                modifier = Modifier
                    .height(48.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (onSearchClick != null) {
                    TopPillActionIconButton(
                        contentDescription = "Search",
                        onClick = onSearchClick,
                    ) {
                        PillSearchGlyph(color = colors.content.primary)
                    }
                }
                if (onBookmarkClick != null) {
                    TopPillActionIconButton(
                        contentDescription = "Bookmark",
                        onClick = onBookmarkClick,
                    ) {
                        PillBookmarkGlyph(
                            filled = isBookmarked,
                            color = if (isBookmarked) colors.brand.primary else colors.content.primary,
                        )
                    }
                }
                TopPillActionIconButton(
                    contentDescription = "More options",
                    onClick = onMoreClick,
                ) {
                    Box(modifier = Modifier.alpha(moreHandoffAlpha.coerceIn(0f, 1f))) {
                        PillMoreGlyph(
                            color = if (moreMenuExpanded) colors.brand.primary else colors.content.primary,
                        )
                    }
                }
            }
        }
    } else {
        // Metaball Splitting / Separated State: [Search, Bookmark] capsule + Viscous Bridge + [More] droplet capsule
        val moreInteraction = remember { MutableInteractionSource() }
        val moreClick = rememberAuraDebouncedClick(onClick = onMoreClick, debounceIntervalMs = 220L)

        Layout(
            modifier = Modifier.graphicsLayer {
                scaleX = capsuleScale
                scaleY = capsuleScale
            },
            content = {
                // Slot 0: [Search, Bookmark] grouped capsule inside 48dp hit target
                Box(
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .height(48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    GlassSurface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        glassStyle = glassStyle,
                        shape = RoundedCornerShape(50),
                        cornerRadiusHint = 21.dp,
                        surfaceAlphaMultiplier = surfaceAlphaMultiplier,
                        dispersionBoost = dispersionBoost,
                        showShadow = showShadow,
                    ) {}
                    Row(
                        modifier = Modifier
                            .height(48.dp)
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (onSearchClick != null) {
                            TopPillActionIconButton(
                                contentDescription = "Search",
                                onClick = onSearchClick,
                            ) {
                                PillSearchGlyph(color = colors.content.primary)
                            }
                        }
                        if (onBookmarkClick != null) {
                            TopPillActionIconButton(
                                contentDescription = "Bookmark",
                                onClick = onBookmarkClick,
                            ) {
                                PillBookmarkGlyph(
                                    filled = isBookmarked,
                                    color = if (isBookmarked) colors.brand.primary else colors.content.primary,
                                )
                            }
                        }
                    }
                }

                // Slot 1: Viscous Metaball neck bridging visual circles directly
                Box(
                    modifier = Modifier
                        .width(gapDp)
                        .height(42.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    LiquidMetaballBridge(
                        gapWidthDp = gapDp,
                        heightDp = 40.dp,
                        glassStyle = glassStyle,
                        mergeProgress = mergeProgress * moreHandoffAlpha,
                        modifier = Modifier
                            .width(gapDp)
                            .height(40.dp),
                    )
                }

                // Slot 2: 42x42dp visual droplet inside full 48x48dp touch area
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .fluidPressScale(moreInteraction)
                        .graphicsLayer { alpha = moreHandoffAlpha.coerceIn(0f, 1f) }
                        .semantics { contentDescription = "More options" }
                        .clickable(
                            interactionSource = moreInteraction,
                            indication = null,
                            role = Role.Button,
                            onClick = moreClick,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    GlassSurface(
                        modifier = Modifier.size(42.dp),
                        glassStyle = glassStyle,
                        shape = CircleShape,
                        cornerRadiusHint = 21.dp,
                        surfaceAlphaMultiplier = surfaceAlphaMultiplier,
                        dispersionBoost = dispersionBoost,
                        showShadow = showShadow,
                    ) {
                        Box(
                            modifier = Modifier.size(42.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            PillMoreGlyph(
                                color = if (moreMenuExpanded) colors.brand.primary else colors.content.primary,
                            )
                        }
                    }
                }
            },
        ) { measurables, constraints ->
            val leftPlaceable = measurables[0].measure(constraints.copy(minWidth = 0))
            val bridgePlaceable = measurables[1].measure(constraints.copy(minWidth = 0))
            val morePlaceable = measurables[2].measure(constraints.copy(minWidth = 0))

            val visualCirclePx = 42.dp.roundToPx()
            val touchInsetPx = ((morePlaceable.width - visualCirclePx) / 2f).roundToInt()
            val totalWidth = leftPlaceable.width + bridgePlaceable.width + visualCirclePx
            val totalHeight = max(leftPlaceable.height, morePlaceable.height)

            layout(totalWidth, totalHeight) {
                leftPlaceable.placeRelative(0, (totalHeight - leftPlaceable.height) / 2)
                bridgePlaceable.placeRelative(leftPlaceable.width, (totalHeight - bridgePlaceable.height) / 2)
                morePlaceable.placeRelative(
                    x = leftPlaceable.width + bridgePlaceable.width - touchInsetPx,
                    y = (totalHeight - morePlaceable.height) / 2,
                )
            }
        }
    }
}

@Composable
private fun TopPillActionIconButton(
    contentDescription: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val debouncedClick = rememberAuraDebouncedClick(onClick = onClick, debounceIntervalMs = 220L)
    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 30.dp, minHeight = 48.dp)
            .fluidPressScale(interactionSource)
            .semantics { this.contentDescription = contentDescription }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = debouncedClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        icon()
    }
}

/**
 * Layer 3 Container-Transform Popover that morphs directly out of the top-right `[More]` capsule
 * using interruptible `Spring(dampingRatio = 0.65f, stiffness = 450f)` physics.
 */
@Composable
fun MorphingMorePopover(
    expanded: Boolean,
    actions: List<AuraMoreMenuAction>,
    onActionClick: (AuraMoreMenuAction) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    glassStyle: GlassStyle = GlassStyle.LIQUID,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val reducedMotion = rememberAuraReducedMotion()

    val morphProgress by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = if (reducedMotion) snap() else spring(dampingRatio = 0.65f, stiffness = 450f),
        label = "MorphingMorePopoverProgress",
    )

    if (morphProgress <= 0.02f) return

    val clampedProgress = morphProgress.coerceIn(0f, 1f)
    val targetHeight = (actions.size * 48 + 20).dp
    val width = lerp(42.dp, 224.dp, clampedProgress)
    val height = lerp(42.dp, targetHeight, clampedProgress)
    val cornerRadius = lerp(21.dp, 24.dp, clampedProgress)
    val glyphAlpha = (1f - (clampedProgress / 0.35f)).coerceIn(0f, 1f)
    val contentAlpha = ((clampedProgress - 0.30f) / 0.70f).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .statusBarsPadding()
            .padding(top = 9.dp, end = 16.dp),
    ) {
        GlassSurface(
            modifier = Modifier
                .width(width)
                .height(height)
                .graphicsLayer {
                    alpha = clampedProgress
                },
            glassStyle = glassStyle,
            shape = RoundedCornerShape(cornerRadius),
            cornerRadiusHint = cornerRadius,
            dispersionBoost = 1f + (1f - clampedProgress) * 0.9f,
        ) {
            if (glyphAlpha > 0.02f) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .align(Alignment.TopEnd)
                        .alpha(glyphAlpha),
                    contentAlignment = Alignment.Center,
                ) {
                    PillMoreGlyph(color = colors.brand.primary)
                }
            }
            if (contentAlpha > 0.02f) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 8.dp)
                        .alpha(contentAlpha),
                ) {
                    actions.forEachIndexed { index, action ->
                        val itemInteraction = remember(action.id) { MutableInteractionSource() }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .fluidPressScale(itemInteraction, pressedScale = 0.96f)
                                .clickable(
                                    interactionSource = itemInteraction,
                                    indication = null,
                                    role = Role.Button,
                                    onClick = {
                                        onActionClick(action)
                                        onDismissRequest()
                                    },
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            MenuGlyphForIndex(index = index, color = colors.brand.primary)
                            AuraText(
                                text = action.label,
                                style = typography.bodyMedium,
                                color = colors.content.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Standard Aura Top App Bar implementing [AuraTopAppBarStyle.FloatingCapsule],
 * [AuraTopAppBarStyle.FloatingSplit], and [AuraTopAppBarStyle.SurfaceDocked] with
 * [AuraTheme.motion] transitions and semantic tokens.
 */
@Composable
fun AuraTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    style: AuraTopAppBarStyle = AuraTopAppBarStyle.FloatingCapsule,
    surface: AuraTopAppBarSurface = AuraTopAppBarSurface.SolidElevated,
    scrollBehavior: AuraTopAppBarScrollBehavior = AuraTopAppBarScrollBehavior.Pinned,
    scrollFraction: Float = 0f,
    centerTitle: Boolean = false,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    subContent: (@Composable () -> Unit)? = null,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val motion = AuraTheme.motion
    val layout = AuraTheme.layout
    val window = AuraTheme.window
    val reducedMotion = rememberAuraReducedMotion()

    val clampedScroll = scrollFraction.coerceIn(0f, 1f)
    val smoothScroll by animateFloatAsState(
        targetValue = clampedScroll,
        animationSpec = if (reducedMotion) snap() else motion.pressOutSpring(),
        label = "AuraTopAppBarScroll",
    )

    val colorSpec = remember(reducedMotion, motion.stateChangeDurationMillis) {
        if (reducedMotion) snap<Color>() else tween(durationMillis = motion.stateChangeDurationMillis)
    }

    val barHeight: Dp = when {
        scrollBehavior == AuraTopAppBarScrollBehavior.CompactOnScroll ->
            lerp(56.dp, 48.dp, smoothScroll)
        window.widthClass == com.example.ds.foundation.layout.AuraWidthClass.ExtraSmall ||
            window.displayRatio == com.example.ds.foundation.layout.AuraDisplayRatio.UltraWide -> 48.dp
        else -> 56.dp
    }

    val outerHorizontalMargin: Dp = when (style) {
        AuraTopAppBarStyle.SurfaceDocked -> 0.dp
        AuraTopAppBarStyle.FloatingCapsule, AuraTopAppBarStyle.FloatingSplit ->
            layout.screenMarginHorizontal.coerceIn(12.dp, 24.dp)
    }

    val translationYOffset by animateDpAsState(
        targetValue = if (scrollBehavior == AuraTopAppBarScrollBehavior.EnterAlways && smoothScroll > 0.7f) (-72).dp else 0.dp,
        animationSpec = if (reducedMotion) snap() else motion.pressOutSpring(),
        label = "AuraTopAppBarEnterAlways",
    )

    val barContainerColor by animateColorAsState(
        targetValue = when {
            style == AuraTopAppBarStyle.SurfaceDocked && smoothScroll <= 0.05f -> colors.surface.default
            style == AuraTopAppBarStyle.SurfaceDocked -> colors.surface.raised
            surface == AuraTopAppBarSurface.TranslucentGlass -> colors.surface.floating.copy(
                alpha = if (colors.isDark) 0.72f else 0.78f,
            )
            else -> colors.surface.floating
        },
        animationSpec = colorSpec,
        label = "AuraTopAppBarContainer",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .graphicsLayer {
                translationY = translationYOffset.toPx()
            }
            .padding(
                horizontal = outerHorizontalMargin,
                vertical = if (style == AuraTopAppBarStyle.SurfaceDocked) 0.dp else 8.dp,
            ),
    ) {
        when (style) {
            AuraTopAppBarStyle.SurfaceDocked -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(barContainerColor),
                ) {
                    TopBarSlotRow(
                        title = title,
                        subtitle = subtitle,
                        height = barHeight,
                        centerTitle = centerTitle,
                        compactTitle = smoothScroll > 0.5f,
                        navigationIcon = navigationIcon,
                        actions = actions,
                    )
                    if (smoothScroll > 0.05f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(colors.border.subtle),
                        )
                    }
                }
            }
            AuraTopAppBarStyle.FloatingCapsule -> {
                if (surface == AuraTopAppBarSurface.TranslucentGlass) {
                    GlassSurface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(barHeight),
                        glassStyle = GlassStyle.FROSTED,
                        shape = CircleShape,
                        cornerRadiusHint = barHeight / 2f,
                    ) {
                        TopBarSlotRow(
                            title = title,
                            subtitle = subtitle,
                            height = barHeight,
                            centerTitle = centerTitle,
                            compactTitle = smoothScroll > 0.5f,
                            navigationIcon = navigationIcon,
                            actions = actions,
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(barHeight)
                            .shadow(elevation = if (smoothScroll > 0.05f) 8.dp else 4.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(barContainerColor, CircleShape)
                            .border(width = 1.dp, color = colors.border.subtle, shape = CircleShape)
                            .padding(horizontal = 8.dp),
                    ) {
                        TopBarSlotRow(
                            title = title,
                            subtitle = subtitle,
                            height = barHeight,
                            centerTitle = centerTitle,
                            compactTitle = smoothScroll > 0.5f,
                            navigationIcon = navigationIcon,
                            actions = actions,
                        )
                    }
                }
            }
            AuraTopAppBarStyle.FloatingSplit -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .shadow(elevation = 4.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(barContainerColor, CircleShape)
                            .border(width = 1.dp, color = colors.border.subtle, shape = CircleShape)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            if (navigationIcon != null) {
                                CompositionLocalProvider(LocalAuraContentColor provides colors.content.primary) {
                                    navigationIcon()
                                }
                            }
                            if (title.isNotBlank()) {
                                AuraText(
                                    text = title,
                                    style = typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp)
                                        .semantics { heading() },
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .shadow(elevation = 4.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(barContainerColor, CircleShape)
                            .border(width = 1.dp, color = colors.border.subtle, shape = CircleShape)
                            .padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CompositionLocalProvider(LocalAuraContentColor provides colors.content.primary) {
                            actions()
                        }
                    }
                }
            }
        }

        if (subContent != null && !(scrollBehavior == AuraTopAppBarScrollBehavior.CompactOnScroll && smoothScroll > 0.65f)) {
            Spacer(modifier = Modifier.height(8.dp))
            subContent()
        }
    }
}

@Composable
private fun TopBarSlotRow(
    title: String,
    subtitle: String?,
    height: Dp,
    centerTitle: Boolean,
    compactTitle: Boolean,
    navigationIcon: (@Composable () -> Unit)?,
    actions: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (navigationIcon != null) {
            CompositionLocalProvider(LocalAuraContentColor provides colors.content.primary) {
                navigationIcon()
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            horizontalAlignment = if (centerTitle) Alignment.CenterHorizontally else Alignment.Start,
            verticalArrangement = Arrangement.Center,
        ) {
            AuraText(
                text = title,
                style = if (compactTitle) typography.titleMedium else typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.semantics { heading() },
            )
            if (subtitle != null && !compactTitle) {
                AuraText(
                    text = subtitle,
                    style = typography.labelMedium,
                    color = colors.content.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            CompositionLocalProvider(LocalAuraContentColor provides colors.content.primary) {
                actions()
            }
        }
    }
}

// --- Resolution-independent Canvas glyphs so :design-system stays Material-icon free ---

@Composable
private fun PillBackGlyph(color: Color) {
    Canvas(modifier = Modifier.size(18.dp)) {
        val stroke = 2.dp.toPx()
        val cy = size.height * 0.5f
        val leftX = size.width * 0.22f
        val rightX = size.width * 0.80f
        val tipY = size.height * 0.25f

        val arrow = Path().apply {
            moveTo(leftX + size.width * 0.26f, tipY)
            lineTo(leftX, cy)
            lineTo(leftX + size.width * 0.26f, size.height - tipY)
        }
        drawPath(
            path = arrow,
            color = color,
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        drawLine(
            color = color,
            start = Offset(leftX + 1.dp.toPx(), cy),
            end = Offset(rightX, cy),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun PillSearchGlyph(color: Color) {
    Canvas(modifier = Modifier.size(18.dp)) {
        val stroke = 1.9.dp.toPx()
        val radius = size.minDimension * 0.29f
        val center = Offset(size.width * 0.42f, size.height * 0.42f)
        drawCircle(
            color = color,
            radius = radius,
            center = center,
            style = Stroke(width = stroke),
        )
        val diag = radius * 0.72f
        drawLine(
            color = color,
            start = Offset(center.x + diag, center.y + diag),
            end = Offset(size.width * 0.84f, size.height * 0.84f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun PillBookmarkGlyph(filled: Boolean, color: Color) {
    Canvas(modifier = Modifier.size(18.dp)) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.24f, h * 0.16f)
            lineTo(w * 0.76f, h * 0.16f)
            lineTo(w * 0.76f, h * 0.84f)
            lineTo(w * 0.50f, h * 0.64f)
            lineTo(w * 0.24f, h * 0.84f)
            close()
        }
        if (filled) {
            drawPath(path = path, color = color)
        } else {
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 1.8.dp.toPx(), join = StrokeJoin.Round, cap = StrokeCap.Round),
            )
        }
    }
}

@Composable
private fun PillMoreGlyph(color: Color) {
    Canvas(modifier = Modifier.size(18.dp)) {
        val cx = size.width * 0.5f
        val r = 1.6.dp.toPx()
        drawCircle(color = color, radius = r, center = Offset(cx, size.height * 0.24f))
        drawCircle(color = color, radius = r, center = Offset(cx, size.height * 0.50f))
        drawCircle(color = color, radius = r, center = Offset(cx, size.height * 0.76f))
    }
}

@Composable
private fun MenuGlyphForIndex(index: Int, color: Color) {
    Canvas(modifier = Modifier.size(18.dp)) {
        val stroke = 1.7.dp.toPx()
        val w = size.width
        val h = size.height
        when (index % 4) {
            0 -> {
                // Share nodes glyph
                drawCircle(color, 2.2.dp.toPx(), Offset(w * 0.26f, h * 0.50f), style = Stroke(stroke))
                drawCircle(color, 2.2.dp.toPx(), Offset(w * 0.74f, h * 0.24f), style = Stroke(stroke))
                drawCircle(color, 2.2.dp.toPx(), Offset(w * 0.74f, h * 0.76f), style = Stroke(stroke))
                drawLine(color, Offset(w * 0.36f, h * 0.44f), Offset(w * 0.64f, h * 0.29f), stroke, StrokeCap.Round)
                drawLine(color, Offset(w * 0.36f, h * 0.56f), Offset(w * 0.64f, h * 0.71f), stroke, StrokeCap.Round)
            }
            1 -> {
                // Pin / Sparkle diamond glyph
                val p = Path().apply {
                    moveTo(w * 0.5f, h * 0.14f)
                    lineTo(w * 0.62f, h * 0.42f)
                    lineTo(w * 0.86f, h * 0.5f)
                    lineTo(w * 0.62f, h * 0.58f)
                    lineTo(w * 0.5f, h * 0.86f)
                    lineTo(w * 0.38f, h * 0.58f)
                    lineTo(w * 0.14f, h * 0.5f)
                    lineTo(w * 0.38f, h * 0.42f)
                    close()
                }
                drawPath(p, color, style = Stroke(stroke, join = StrokeJoin.Round))
            }
            2 -> {
                // Export box + arrow glyph
                drawLine(color, Offset(w * 0.5f, h * 0.62f), Offset(w * 0.5f, h * 0.20f), stroke, StrokeCap.Round)
                val head = Path().apply {
                    moveTo(w * 0.32f, h * 0.36f)
                    lineTo(w * 0.5f, h * 0.18f)
                    lineTo(w * 0.68f, h * 0.36f)
                }
                drawPath(head, color, style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
                val tray = Path().apply {
                    moveTo(w * 0.2f, h * 0.64f)
                    lineTo(w * 0.2f, h * 0.82f)
                    lineTo(w * 0.8f, h * 0.82f)
                    lineTo(w * 0.8f, h * 0.64f)
                }
                drawPath(tray, color, style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            else -> {
                // Inspector sliders glyph
                drawLine(color, Offset(w * 0.18f, h * 0.34f), Offset(w * 0.82f, h * 0.34f), stroke, StrokeCap.Round)
                drawLine(color, Offset(w * 0.18f, h * 0.66f), Offset(w * 0.82f, h * 0.66f), stroke, StrokeCap.Round)
                drawCircle(color, 2.5.dp.toPx(), Offset(w * 0.64f, h * 0.34f))
                drawCircle(color, 2.5.dp.toPx(), Offset(w * 0.36f, h * 0.66f))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AuraTopAppBarPreview() {
    AuraTheme {
        Column(
            modifier = Modifier
                .background(AuraTheme.colors.background.primary)
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FloatingTopPillBar(
                title = "Fluid Space",
                scrollProgress = 0.85f,
                onBackClick = {},
                onSearchClick = {},
                isBookmarked = true,
                onBookmarkClick = {},
            )
            AuraTopAppBar(
                title = "My Drink Diary",
                subtitle = "128 Bottles Archived",
                style = AuraTopAppBarStyle.FloatingCapsule,
            )
        }
    }
}
