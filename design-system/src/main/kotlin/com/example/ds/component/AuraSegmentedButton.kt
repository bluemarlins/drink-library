package com.example.ds.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ds.foundation.color.AuraColors
import com.example.ds.foundation.theme.AuraTheme
import kotlin.math.max
import kotlin.math.roundToInt

enum class AuraSegmentedVariant {
    Surface,
    Outlined,
    Glass,
}

enum class AuraSegmentedSize {
    Small,
    Medium,
    Large,
}

private val MinSegmentedTouchHeight = 48.dp

private data class AuraSegmentedPalette(
    val trackBackground: Color,
    val trackBorder: Color,
    val thumbBackground: Color,
    val activeContent: Color,
    val inactiveContent: Color,
    val thumbElevation: Dp,
)

private data class AuraSegmentedMetrics(
    val trackHeight: Dp,
    val trackPadding: Dp,
    val segmentHorizontalPadding: Dp,
    val iconSize: Dp,
    val labelStyle: TextStyle,
)

/**
 * Immediate mutually exclusive view-mode or unit switcher (`2–5` options) with a
 * spring-animated sliding thumb indicator, `>= 48.dp` vertical hit bounds, and Reduced Motion support.
 */
@Composable
fun AuraSegmentedButton(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    variant: AuraSegmentedVariant = AuraSegmentedVariant.Surface,
    size: AuraSegmentedSize = AuraSegmentedSize.Medium,
    enabled: Boolean = true,
    fullWidth: Boolean = true,
    optionIcons: List<(@Composable () -> Unit)?>? = null,
) {
    if (options.isEmpty()) return

    val safeSelectedIndex = selectedIndex.coerceIn(0, options.lastIndex)
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val motion = AuraTheme.motion
    val reducedMotion = rememberAuraReducedMotion()

    val palette = resolveAuraSegmentedPalette(
        variant = variant,
        enabled = enabled,
        colors = colors,
    )
    val metrics = resolveAuraSegmentedMetrics(
        size = size,
        typography = typography,
    )

    val colorAnimSpec = if (reducedMotion) {
        snap<Color>()
    } else {
        tween(durationMillis = motion.stateChangeDurationMillis)
    }
    val animatedTrackBg by animateColorAsState(
        targetValue = palette.trackBackground,
        animationSpec = colorAnimSpec,
        label = "AuraSegmentedTrackBg",
    )
    val animatedTrackBorder by animateColorAsState(
        targetValue = palette.trackBorder,
        animationSpec = colorAnimSpec,
        label = "AuraSegmentedTrackBorder",
    )
    val animatedThumbBg by animateColorAsState(
        targetValue = palette.thumbBackground,
        animationSpec = colorAnimSpec,
        label = "AuraSegmentedThumbBg",
    )

    val animatedThumbIndex by animateFloatAsState(
        targetValue = safeSelectedIndex.toFloat(),
        animationSpec = if (reducedMotion) snap() else motion.pressOutSpring(),
        label = "AuraSegmentedThumbIndex",
    )

    val widthModifier = if (fullWidth) Modifier.fillMaxWidth() else Modifier
    val optionCount = options.size

    Layout(
        modifier = modifier
            .then(widthModifier)
            .selectableGroup(),
        content = {
            // Child 0: Capsule track backdrop
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(animatedTrackBg, CircleShape)
                    .border(width = 1.dp, color = animatedTrackBorder, shape = CircleShape),
            )

            // Child 1: Sliding active thumb pill
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (palette.thumbElevation > 0.dp) {
                            Modifier.shadow(
                                elevation = palette.thumbElevation,
                                shape = CircleShape,
                                clip = false,
                            )
                        } else {
                            Modifier
                        },
                    )
                    .clip(CircleShape)
                    .background(animatedThumbBg, CircleShape),
            )

            // Children 2..(2 + N - 1): Equal-width accessible segment slots
            options.forEachIndexed { index, label ->
                val isSelected = index == safeSelectedIndex
                val segmentInteractionSource = remember(index) { MutableInteractionSource() }
                val isPressed by rememberAuraTactilePressed(
                    interactionSource = segmentInteractionSource,
                    minPressDurationMillis = motion.minPressDurationMillis,
                )
                val isFocused by segmentInteractionSource.collectIsFocusedAsState()
                val isHovered by segmentInteractionSource.collectIsHoveredAsState()

                val targetContentColor = when {
                    !enabled -> colors.content.disabled
                    isSelected -> palette.activeContent
                    else -> palette.inactiveContent
                }
                val animatedContentColor by animateColorAsState(
                    targetValue = targetContentColor,
                    animationSpec = colorAnimSpec,
                    label = "AuraSegmentContentColor_$index",
                )
                val contentScale by animateFloatAsState(
                    targetValue = if (!reducedMotion && enabled && isPressed) motion.pressedScaleDefault else 1f,
                    animationSpec = when {
                        reducedMotion -> snap()
                        isPressed -> motion.pressInSpring()
                        else -> motion.pressOutSpring()
                    },
                    label = "AuraSegmentScale_$index",
                )
                val iconScale by animateFloatAsState(
                    targetValue = if (!reducedMotion && enabled && isPressed) motion.leadingIconPressedScale else 1f,
                    animationSpec = if (reducedMotion) snap() else motion.iconSpring(),
                    label = "AuraSegmentIconScale_$index",
                )

                val segmentOverlay = when {
                    !enabled -> Color.Transparent
                    isPressed && !isSelected -> colors.interaction.pressedOverlay
                    isHovered && !isSelected -> colors.interaction.pressedOverlay.copy(alpha = 0.06f)
                    else -> Color.Transparent
                }

                val iconSlot = optionIcons?.getOrNull(index)

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .semantics { this.selected = isSelected }
                        .clickable(
                            enabled = enabled,
                            interactionSource = segmentInteractionSource,
                            indication = null,
                            role = Role.Tab,
                            onClick = { onOptionSelected(index) },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = maxDp((MinSegmentedTouchHeight - metrics.trackHeight) / 2, 0.dp))
                            .padding(all = metrics.trackPadding)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(segmentOverlay, CircleShape)
                            .then(
                                if (isFocused && enabled) {
                                    Modifier.border(width = 2.dp, color = colors.border.focus, shape = CircleShape)
                                } else {
                                    Modifier
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = contentScale
                                    scaleY = contentScale
                                }
                                .padding(horizontal = metrics.segmentHorizontalPadding),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            CompositionLocalProvider(LocalAuraContentColor provides animatedContentColor) {
                                if (iconSlot != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(metrics.iconSize)
                                            .graphicsLayer {
                                                scaleX = iconScale
                                                scaleY = iconScale
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        iconSlot()
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }

                                AuraText(
                                    text = label,
                                    color = animatedContentColor,
                                    style = metrics.labelStyle,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        },
    ) { measurables, constraints ->
        val trackHeightPx = metrics.trackHeight.roundToPx()
        val trackPaddingPx = metrics.trackPadding.roundToPx()
        val totalHeightPx = max(trackHeightPx, MinSegmentedTouchHeight.roundToPx())
        val thumbHeightPx = (trackHeightPx - trackPaddingPx * 2).coerceAtLeast(1)

        val segmentMeasurables = measurables.drop(2)
        val naturalSegmentMaxWidth = if (fullWidth && constraints.hasBoundedWidth) {
            0
        } else {
            segmentMeasurables.maxOfOrNull { it.maxIntrinsicWidth(totalHeightPx) } ?: 0
        }

        val totalWidthPx = if (fullWidth && constraints.hasBoundedWidth) {
            constraints.maxWidth
        } else {
            val desiredTotal = (naturalSegmentMaxWidth * optionCount) + (trackPaddingPx * 2)
            desiredTotal.coerceIn(
                minimumValue = constraints.minWidth,
                maximumValue = if (constraints.hasBoundedWidth) constraints.maxWidth else desiredTotal,
            )
        }

        val innerWidthPx = (totalWidthPx - trackPaddingPx * 2).coerceAtLeast(optionCount)
        val segmentWidthPx = (innerWidthPx / optionCount).coerceAtLeast(1)

        val trackPlaceable = measurables[0].measure(
            Constraints.fixed(width = totalWidthPx, height = trackHeightPx),
        )
        val thumbPlaceable = measurables[1].measure(
            Constraints.fixed(width = segmentWidthPx, height = thumbHeightPx),
        )
        val segmentPlaceables = segmentMeasurables.map { measurable ->
            measurable.measure(Constraints.fixed(width = segmentWidthPx, height = totalHeightPx))
        }

        val trackY = (totalHeightPx - trackHeightPx) / 2
        val thumbY = (totalHeightPx - thumbHeightPx) / 2
        val thumbX = trackPaddingPx + (animatedThumbIndex * segmentWidthPx).roundToInt()

        layout(totalWidthPx, totalHeightPx) {
            trackPlaceable.placeRelative(0, trackY)
            thumbPlaceable.placeRelative(thumbX, thumbY)
            segmentPlaceables.forEachIndexed { index, placeable ->
                placeable.placeRelative(trackPaddingPx + index * segmentWidthPx, 0)
            }
        }
    }
}

private fun maxDp(a: Dp, b: Dp): Dp = if (a > b) a else b

@Composable
private fun resolveAuraSegmentedPalette(
    variant: AuraSegmentedVariant,
    enabled: Boolean,
    colors: AuraColors,
): AuraSegmentedPalette {
    if (!enabled) {
        return AuraSegmentedPalette(
            trackBackground = colors.interaction.disabledContainer,
            trackBorder = colors.border.subtle,
            thumbBackground = colors.surface.default.copy(alpha = 0.6f),
            activeContent = colors.content.disabled,
            inactiveContent = colors.content.disabled,
            thumbElevation = 0.dp,
        )
    }

    return when (variant) {
        AuraSegmentedVariant.Surface -> AuraSegmentedPalette(
            trackBackground = colors.surface.sunken,
            trackBorder = colors.border.subtle,
            thumbBackground = colors.surface.floating,
            activeContent = colors.content.primary,
            inactiveContent = colors.content.secondary,
            thumbElevation = 2.dp,
        )
        AuraSegmentedVariant.Outlined -> AuraSegmentedPalette(
            trackBackground = colors.surface.default,
            trackBorder = colors.border.default,
            thumbBackground = colors.interaction.selectedContainer,
            activeContent = colors.interaction.onSelected,
            inactiveContent = colors.content.secondary,
            thumbElevation = 0.dp,
        )
        AuraSegmentedVariant.Glass -> AuraSegmentedPalette(
            trackBackground = colors.surface.floating.copy(alpha = 0.72f),
            trackBorder = colors.border.subtle,
            thumbBackground = colors.brand.primary,
            activeContent = colors.brand.onPrimary,
            inactiveContent = colors.content.primary,
            thumbElevation = 2.dp,
        )
    }
}

private fun resolveAuraSegmentedMetrics(
    size: AuraSegmentedSize,
    typography: com.example.ds.foundation.typography.AuraTypography,
): AuraSegmentedMetrics = when (size) {
    AuraSegmentedSize.Small -> AuraSegmentedMetrics(
        trackHeight = 32.dp,
        trackPadding = 3.dp,
        segmentHorizontalPadding = 8.dp,
        iconSize = 14.dp,
        labelStyle = typography.labelSmall,
    )
    AuraSegmentedSize.Medium -> AuraSegmentedMetrics(
        trackHeight = 40.dp,
        trackPadding = 3.dp,
        segmentHorizontalPadding = 12.dp,
        iconSize = 16.dp,
        labelStyle = typography.labelMedium,
    )
    AuraSegmentedSize.Large -> AuraSegmentedMetrics(
        trackHeight = 48.dp,
        trackPadding = 4.dp,
        segmentHorizontalPadding = 16.dp,
        iconSize = 18.dp,
        labelStyle = typography.labelLarge,
    )
}

@Preview(showBackground = true)
@Composable
private fun AuraSegmentedButtonPreview() {
    AuraTheme {
        Column(
            modifier = Modifier
                .background(AuraTheme.colors.background.primary)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AuraSegmentedButton(
                options = listOf("Day", "Week", "Month"),
                selectedIndex = 1,
                onOptionSelected = {},
                variant = AuraSegmentedVariant.Surface,
            )
            AuraSegmentedButton(
                options = listOf("All", "Wine", "Whiskey"),
                selectedIndex = 0,
                onOptionSelected = {},
                variant = AuraSegmentedVariant.Outlined,
                fullWidth = false,
            )
        }
    }
}
