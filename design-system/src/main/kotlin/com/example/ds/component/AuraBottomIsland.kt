package com.example.ds.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.ds.foundation.theme.AuraColorMode
import com.example.ds.foundation.theme.AuraTheme
import kotlin.math.abs
import kotlin.math.roundToInt

data class AuraBottomIslandItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon,
)

private val IslandHeight = 60.dp

/**
 * Computes velocity-coupled Elastic Squish & Stretch scale factors `(scaleX, scaleY)` for
 * the sliding active indicator pill in [FloatingBottomIsland].
 *
 * - At rest (`velocity == 0` and `currentIndex == targetIndex`): returns `1f to 1f`.
 * - Mid-flight high velocity: stretches horizontally up to `1.28f` and compresses vertically down to `0.85f`.
 * - Arrival / deceleration near target: rebounds with a brief elastic squish (`scaleX < 1f`, `scaleY > 1f`).
 */
internal fun calculateLiquidSquishStretch(
    currentIndex: Float,
    targetIndex: Float,
    velocity: Float,
    isLiquid: Boolean,
): Pair<Float, Float> {
    val distance = abs(targetIndex - currentIndex)
    val speed = abs(velocity)
    if (speed < 0.01f && distance < 0.005f) {
        return 1f to 1f
    }
    if (!isLiquid) {
        val subtle = (speed * 0.008f).coerceIn(0f, 0.04f)
        return (1f + subtle) to (1f - subtle * 0.4f)
    }

    val normalizedSpeed = (speed / 7.5f).coerceIn(0f, 1f)
    val arrivalWindow = if (distance < 0.28f && speed > 0.05f) {
        (1f - (distance / 0.28f)) * (1f - normalizedSpeed * 0.7f).coerceIn(0f, 1f)
    } else {
        0f
    }

    val stretchDelta = normalizedSpeed * 0.28f
    val squishDelta = arrivalWindow * 0.08f
    val scaleX = (1f + stretchDelta - squishDelta).coerceIn(0.92f, 1.28f)
    val scaleY = (1f - (stretchDelta * 0.54f) + (squishDelta * 0.75f)).coerceIn(0.85f, 1.08f)
    return scaleX to scaleY
}

/**
 * Detached Floating Bottom Island Navigation with:
 * - Stadium capsule geometry (`60.dp` height, `20.dp` horizontal / `16.dp` bottom margin)
 * - Optional Split Island auxiliary circular action (`10.dp` separation) with viscous Metaball merge/split
 * - Spring-driven sliding Active Indicator pill + velocity-coupled **Elastic Squish & Stretch** in [GlassStyle.LIQUID]
 * - Dual optical materiality ([GlassStyle.FROSTED] / [GlassStyle.LIQUID]) sampling live Layer 0 pixels
 */
@Composable
fun FloatingBottomIsland(
    items: List<AuraBottomIslandItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    glassStyle: GlassStyle = GlassStyle.LIQUID,
    splitActionIcon: ImageVector? = null,
    splitActionLabel: String? = null,
    isActionSplit: Boolean = true,
    onSplitActionClick: (() -> Unit)? = null,
) {
    AuraBottomIsland(
        items = items,
        selectedIndex = selectedIndex,
        onItemSelected = onItemSelected,
        modifier = modifier,
        glassStyle = glassStyle,
        splitActionIcon = splitActionIcon,
        splitActionLabel = splitActionLabel,
        isActionSplit = isActionSplit,
        onSplitActionClick = onSplitActionClick,
    )
}

/**
 * Aura Navigation Bottom Island component implementing the `aura-bottom-island` contract.
 */
@Composable
fun AuraBottomIsland(
    items: List<AuraBottomIslandItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    glassStyle: GlassStyle = GlassStyle.FROSTED,
    splitActionIcon: ImageVector? = null,
    splitActionLabel: String? = null,
    isActionSplit: Boolean = true,
    onSplitActionClick: (() -> Unit)? = null,
) {
    if (items.isEmpty()) return

    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val motion = AuraTheme.motion
    val reducedMotion = rememberAuraReducedMotion()

    val clampedSelected = selectedIndex.coerceIn(0, items.lastIndex)
    val indicatorAnimatable = remember { Animatable(clampedSelected.toFloat()) }
    LaunchedEffect(clampedSelected, reducedMotion) {
        if (reducedMotion) {
            indicatorAnimatable.snapTo(clampedSelected.toFloat())
        } else {
            indicatorAnimatable.animateTo(
                targetValue = clampedSelected.toFloat(),
                animationSpec = spring(dampingRatio = 0.65f, stiffness = 450f),
            )
        }
    }

    val splitTarget = if (isActionSplit && splitActionIcon != null) 1f else 0f
    val splitAnimatable = remember { Animatable(splitTarget) }
    LaunchedEffect(splitTarget, reducedMotion) {
        if (reducedMotion) {
            splitAnimatable.snapTo(splitTarget)
        } else {
            splitAnimatable.animateTo(
                targetValue = splitTarget,
                animationSpec = spring(dampingRatio = 0.58f, stiffness = 320f),
            )
        }
    }

    val splitFraction = splitAnimatable.value.coerceIn(0f, 1f)
    val islandElasticStretch = if (!reducedMotion && glassStyle == GlassStyle.LIQUID) {
        (abs(splitAnimatable.velocity) * 0.015f).coerceIn(0f, 0.06f)
    } else {
        0f
    }

    val travelDelta = abs(indicatorAnimatable.value - clampedSelected.toFloat())
    val dispersionBoost = 1f + (travelDelta.coerceIn(0f, 1.4f) * 0.65f) + (islandElasticStretch * 4f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Row(
            modifier = Modifier.widthIn(max = 560.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            // Main Floating Navigation Island
            GlassSurface(
                modifier = Modifier
                    .weight(1f)
                    .height(IslandHeight)
                    .graphicsLayer {
                        scaleX = 1f + islandElasticStretch
                        scaleY = 1f - (islandElasticStretch * 0.55f)
                    },
                glassStyle = glassStyle,
                shape = RoundedCornerShape(50),
                cornerRadiusHint = IslandHeight / 2f,
                dispersionBoost = dispersionBoost,
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp, vertical = 5.dp),
                ) {
                    val slotWidthDp = maxWidth / items.size.coerceAtLeast(1)
                    val indicatorHeight = IslandHeight - 10.dp

                    val indicatorTint = if (glassStyle == GlassStyle.LIQUID) {
                        colors.brand.primary.copy(alpha = if (colors.isDark) 0.28f else 0.16f)
                    } else {
                        colors.interaction.selectedContainer.copy(alpha = if (colors.isDark) 0.55f else 0.75f)
                    }
                    val indicatorBorder = if (glassStyle == GlassStyle.LIQUID) {
                        Color.White.copy(alpha = if (colors.isDark) 0.28f else 0.62f)
                    } else {
                        colors.brand.primary.copy(alpha = 0.22f)
                    }

                    // Sliding Active Indicator Pill driven by instantaneous spring velocity
                    Box(
                        modifier = Modifier
                            .offset {
                                val slotPx = slotWidthDp.toPx()
                                IntOffset(x = (slotPx * indicatorAnimatable.value).roundToInt(), y = 0)
                            }
                            .width(slotWidthDp)
                            .height(indicatorHeight)
                            .graphicsLayer {
                                if (!reducedMotion) {
                                    val (sx, sy) = calculateLiquidSquishStretch(
                                        currentIndex = indicatorAnimatable.value,
                                        targetIndex = clampedSelected.toFloat(),
                                        velocity = indicatorAnimatable.velocity,
                                        isLiquid = glassStyle == GlassStyle.LIQUID,
                                    )
                                    scaleX = sx
                                    scaleY = sy
                                }
                            }
                            .clip(RoundedCornerShape(50))
                            .background(indicatorTint, RoundedCornerShape(50))
                            .border(width = 1.dp, color = indicatorBorder, shape = RoundedCornerShape(50)),
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .selectableGroup(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        items.forEachIndexed { index, item ->
                            val isSelected = index == clampedSelected
                            val tabInteraction = remember(item.id) { MutableInteractionSource() }
                            val colorSpec = remember(reducedMotion, motion.stateChangeDurationMillis) {
                                if (reducedMotion) snap<Color>() else tween(motion.stateChangeDurationMillis)
                            }
                            val tintColor by animateColorAsState(
                                targetValue = if (isSelected) colors.brand.primary else colors.content.secondary,
                                animationSpec = colorSpec,
                                label = "BottomIslandTabTint",
                            )
                            val contentAlpha by animateFloatAsState(
                                targetValue = if (isSelected) 1f else 0.60f,
                                animationSpec = if (reducedMotion) snap() else tween(motion.stateChangeDurationMillis),
                                label = "BottomIslandTabAlpha",
                            )
                            val iconScale by animateFloatAsState(
                                targetValue = if (isSelected) 1.08f else 1f,
                                animationSpec = if (reducedMotion) snap() else motion.iconSpring(),
                                label = "BottomIslandIconScale",
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                    .fluidPressScale(tabInteraction)
                                    .clip(RoundedCornerShape(50))
                                    .selectable(
                                        selected = isSelected,
                                        interactionSource = tabInteraction,
                                        indication = null,
                                        role = Role.Tab,
                                        onClick = { onItemSelected(index) },
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .alpha(contentAlpha)
                                        .padding(horizontal = 4.dp),
                                ) {
                                    val vector = if (isSelected) item.selectedIcon else item.icon
                                    val painter = rememberVectorPainter(image = vector)
                                    CompositionLocalProvider(LocalAuraContentColor provides tintColor) {
                                        Image(
                                            painter = painter,
                                            contentDescription = item.label,
                                            colorFilter = ColorFilter.tint(tintColor),
                                            modifier = Modifier
                                                .size(22.dp)
                                                .graphicsLayer {
                                                    scaleX = iconScale
                                                    scaleY = iconScale
                                                },
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    AuraText(
                                        text = item.label,
                                        style = typography.labelSmall,
                                        color = tintColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Split Island Auxiliary Action Button (60x60dp circle + 10dp spacing + Metaball liquid bridge)
            if (splitActionIcon != null && onSplitActionClick != null && splitFraction > 0.04f) {
                val gapWidth = 10.dp * splitFraction
                val actionDiameter = IslandHeight * splitFraction
                val mergeBridgeStrength = if (glassStyle == GlassStyle.LIQUID) {
                    (1f - ((splitFraction - 0.18f) / 0.70f)).coerceIn(0f, 1f)
                } else {
                    0f
                }

                Box(
                    modifier = Modifier
                        .width(gapWidth)
                        .height(IslandHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    LiquidMetaballBridge(
                        gapWidthDp = gapWidth,
                        heightDp = IslandHeight - 4.dp,
                        glassStyle = glassStyle,
                        mergeProgress = mergeBridgeStrength,
                        modifier = Modifier
                            .width(gapWidth)
                            .height(IslandHeight - 4.dp),
                    )
                }

                val actionInteraction = remember { MutableInteractionSource() }
                val debouncedActionClick = rememberAuraDebouncedClick(
                    onClick = onSplitActionClick,
                    debounceIntervalMs = 250L,
                )

                Box(
                    modifier = Modifier
                        .size(width = actionDiameter, height = IslandHeight)
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .graphicsLayer {
                            alpha = splitFraction
                            scaleX = 0.72f + 0.28f * splitFraction
                            scaleY = 0.72f + 0.28f * splitFraction
                        }
                        .fluidPressScale(actionInteraction)
                        .semantics {
                            if (splitActionLabel != null) {
                                contentDescription = splitActionLabel
                            }
                        }
                        .clickable(
                            interactionSource = actionInteraction,
                            indication = null,
                            role = Role.Button,
                            onClick = debouncedActionClick,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    GlassSurface(
                        modifier = Modifier.size(IslandHeight),
                        glassStyle = glassStyle,
                        shape = CircleShape,
                        cornerRadiusHint = IslandHeight / 2f,
                        surfaceAlphaMultiplier = splitFraction,
                        dispersionBoost = dispersionBoost,
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            val actionPainter = rememberVectorPainter(image = splitActionIcon)
                            Image(
                                painter = actionPainter,
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colors.brand.primary),
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Floating Bottom Island · Light Liquid", showBackground = true)
@Composable
private fun FloatingBottomIslandLiquidPreview() {
    val previewDot = rememberPreviewDotVector()
    val previewPlus = rememberPreviewPlusVector()
    val items = listOf(
        AuraBottomIslandItem(id = "home", label = "Home", icon = previewDot),
        AuraBottomIslandItem(id = "diary", label = "Diary", icon = previewDot),
        AuraBottomIslandItem(id = "finance", label = "Finance", icon = previewDot),
        AuraBottomIslandItem(id = "planner", label = "Planner", icon = previewDot),
    )
    AuraTheme(colorMode = AuraColorMode.Light) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .background(AuraTheme.colors.background.primary),
            contentAlignment = Alignment.BottomCenter,
        ) {
            FloatingBottomIsland(
                items = items,
                selectedIndex = 1,
                onItemSelected = {},
                glassStyle = GlassStyle.LIQUID,
                splitActionIcon = previewPlus,
                splitActionLabel = "Create",
                isActionSplit = true,
                onSplitActionClick = {},
            )
        }
    }
}

@Preview(name = "Floating Bottom Island · Dark Frosted", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun FloatingBottomIslandFrostedDarkPreview() {
    val previewDot = rememberPreviewDotVector()
    val items = listOf(
        AuraBottomIslandItem(id = "home", label = "Home", icon = previewDot),
        AuraBottomIslandItem(id = "diary", label = "Diary", icon = previewDot),
        AuraBottomIslandItem(id = "finance", label = "Finance", icon = previewDot),
    )
    AuraTheme(colorMode = AuraColorMode.Dark) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .background(AuraTheme.colors.background.primary),
            contentAlignment = Alignment.BottomCenter,
        ) {
            FloatingBottomIsland(
                items = items,
                selectedIndex = 0,
                onItemSelected = {},
                glassStyle = GlassStyle.FROSTED,
                isActionSplit = false,
            )
        }
    }
}

private fun rememberPreviewDotVector(): ImageVector = ImageVector.Builder(
    name = "PreviewDot",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).apply {
    path(fill = SolidColor(Color.Black)) {
        moveTo(12f, 5f)
        arcTo(7f, 7f, 0f, true, true, 11.9f, 5f)
        close()
    }
}.build()

private fun rememberPreviewPlusVector(): ImageVector = ImageVector.Builder(
    name = "PreviewPlus",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).apply {
    path(fill = SolidColor(Color.Black)) {
        moveTo(11f, 5f)
        horizontalLineTo(13f)
        verticalLineTo(11f)
        horizontalLineTo(19f)
        verticalLineTo(13f)
        horizontalLineTo(13f)
        verticalLineTo(19f)
        horizontalLineTo(11f)
        verticalLineTo(13f)
        horizontalLineTo(5f)
        verticalLineTo(11f)
        horizontalLineTo(11f)
        close()
    }
}.build()
