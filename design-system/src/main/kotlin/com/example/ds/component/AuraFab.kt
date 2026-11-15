package com.example.ds.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ds.foundation.color.AuraColors
import com.example.ds.foundation.theme.AuraTheme

enum class AuraFabVariant {
    Primary,
    Secondary,
    Surface,
    Glass,
}

enum class AuraFabSize {
    Small,
    Regular,
    Large,
}

private val MinFabTouchTargetSize = 48.dp

private data class AuraFabPalette(
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color,
    val elevation: Dp,
)

private data class AuraFabDimensions(
    val containerHeight: Dp,
    val iconSize: Dp,
    val spinnerStrokeWidth: Dp,
    val startPaddingExtended: Dp,
    val endPaddingExtended: Dp,
    val iconLabelGap: Dp,
    val labelStyle: TextStyle,
)

/**
 * Persistent floating action button supporting compact icon-only silhouettes and
 * scroll-collapsible Extended pills with tactile spring motion and accessibility semantics.
 */
@Composable
fun AuraFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    expanded: Boolean = true,
    variant: AuraFabVariant = AuraFabVariant.Primary,
    size: AuraFabSize = AuraFabSize.Regular,
    enabled: Boolean = true,
    loading: Boolean = false,
    contentDescription: String? = null,
    debounceIntervalMs: Long = 400L,
    interactionSource: MutableInteractionSource? = null,
    icon: @Composable () -> Unit,
) {
    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val motion = AuraTheme.motion
    val reducedMotion = rememberAuraReducedMotion()
    val isInteractive = enabled && !loading
    val showExtendedLabel = !text.isNullOrEmpty() && expanded && !loading

    val isPressed by rememberAuraTactilePressed(
        interactionSource = resolvedInteractionSource,
        minPressDurationMillis = motion.minPressDurationMillis,
    )
    val isFocused by resolvedInteractionSource.collectIsFocusedAsState()
    val isHovered by resolvedInteractionSource.collectIsHoveredAsState()

    val palette = resolveAuraFabPalette(
        variant = variant,
        enabled = enabled,
        colors = colors,
    )
    val dims = resolveAuraFabDimensions(
        size = size,
        typography = typography,
    )

    val colorAnimSpec = if (reducedMotion) {
        snap<Color>()
    } else {
        tween(durationMillis = motion.stateChangeDurationMillis)
    }

    val animatedContainerColor by animateColorAsState(
        targetValue = palette.containerColor,
        animationSpec = colorAnimSpec,
        label = "AuraFabContainerColor",
    )
    val animatedContentColor by animateColorAsState(
        targetValue = palette.contentColor,
        animationSpec = colorAnimSpec,
        label = "AuraFabContentColor",
    )
    val animatedBorderColor by animateColorAsState(
        targetValue = if (isFocused && enabled) colors.border.focus else palette.borderColor,
        animationSpec = colorAnimSpec,
        label = "AuraFabBorderColor",
    )

    val pressedScaleTarget = if (size == AuraFabSize.Large || showExtendedLabel) {
        motion.pressedScaleLarge
    } else {
        motion.pressedScaleDefault
    }
    val containerScale by animateFloatAsState(
        targetValue = if (!reducedMotion && isInteractive && isPressed) pressedScaleTarget else 1f,
        animationSpec = when {
            reducedMotion -> snap()
            isPressed -> motion.pressInSpring()
            else -> motion.pressOutSpring()
        },
        label = "AuraFabScale",
    )
    val iconScale by animateFloatAsState(
        targetValue = if (!reducedMotion && isInteractive && isPressed) motion.leadingIconPressedScale else 1f,
        animationSpec = if (reducedMotion) snap() else motion.iconSpring(),
        label = "AuraFabIconScale",
    )

    val dpAnimSpec = if (reducedMotion) {
        snap<Dp>()
    } else {
        tween(durationMillis = motion.stateChangeDurationMillis)
    }
    val startPadding by animateDpAsState(
        targetValue = if (showExtendedLabel) dims.startPaddingExtended else 0.dp,
        animationSpec = dpAnimSpec,
        label = "AuraFabStartPadding",
    )
    val endPadding by animateDpAsState(
        targetValue = if (showExtendedLabel) dims.endPaddingExtended else 0.dp,
        animationSpec = dpAnimSpec,
        label = "AuraFabEndPadding",
    )

    val debouncedOnClick = rememberAuraDebouncedClick(
        onClick = onClick,
        debounceIntervalMs = debounceIntervalMs,
    )

    val overlayColor = when {
        !isInteractive -> Color.Transparent
        isPressed -> colors.interaction.pressedOverlay
        isHovered -> colors.interaction.pressedOverlay.copy(alpha = 0.06f)
        else -> Color.Transparent
    }
    val borderWidth = if (isFocused && enabled) 2.dp else 1.dp

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = MinFabTouchTargetSize, minHeight = MinFabTouchTargetSize)
            .semantics {
                if (contentDescription != null) {
                    this.contentDescription = contentDescription
                }
                if (loading) {
                    this.stateDescription = "In progress"
                }
            }
            .clickable(
                enabled = isInteractive,
                interactionSource = resolvedInteractionSource,
                indication = null,
                role = Role.Button,
                onClick = debouncedOnClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minWidth = dims.containerHeight)
                .height(dims.containerHeight)
                .graphicsLayer {
                    scaleX = containerScale
                    scaleY = containerScale
                }
                .then(
                    if (palette.elevation > 0.dp) {
                        Modifier.shadow(
                            elevation = palette.elevation,
                            shape = CircleShape,
                            clip = false,
                        )
                    } else {
                        Modifier
                    },
                )
                .clip(CircleShape)
                .background(animatedContainerColor, CircleShape)
                .background(overlayColor, CircleShape)
                .then(
                    if (animatedBorderColor.alpha > 0f) {
                        Modifier.border(
                            width = borderWidth,
                            color = animatedBorderColor,
                            shape = CircleShape,
                        )
                    } else {
                        Modifier
                    },
                )
                .padding(start = startPadding, end = endPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            CompositionLocalProvider(LocalAuraContentColor provides animatedContentColor) {
                AnimatedContent(
                    targetState = loading,
                    transitionSpec = {
                        val anim = if (reducedMotion) {
                            snap<Float>()
                        } else {
                            tween(durationMillis = motion.loadingCrossfadeDurationMillis)
                        }
                        fadeIn(animationSpec = anim) togetherWith fadeOut(animationSpec = anim)
                    },
                    label = "AuraFabLoadingCrossfade",
                ) { isLoadingState ->
                    if (isLoadingState) {
                        AuraFabSpinner(
                            color = animatedContentColor,
                            size = dims.iconSize,
                            strokeWidth = dims.spinnerStrokeWidth,
                            reducedMotion = reducedMotion,
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(dims.iconSize)
                                .graphicsLayer {
                                    scaleX = iconScale
                                    scaleY = iconScale
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            icon()
                        }
                    }
                }

                AnimatedVisibility(
                    visible = showExtendedLabel,
                    enter = if (reducedMotion) {
                        EnterTransition.None
                    } else {
                        expandHorizontally(
                            animationSpec = motion.pressOutSpring(),
                            expandFrom = Alignment.Start,
                        ) + fadeIn(tween(durationMillis = motion.stateChangeDurationMillis))
                    },
                    exit = if (reducedMotion) {
                        ExitTransition.None
                    } else {
                        shrinkHorizontally(
                            animationSpec = motion.pressOutSpring(),
                            shrinkTowards = Alignment.Start,
                        ) + fadeOut(tween(durationMillis = motion.stateChangeDurationMillis))
                    },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.width(dims.iconLabelGap))
                        AuraText(
                            text = text.orEmpty(),
                            color = animatedContentColor,
                            style = dims.labelStyle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun resolveAuraFabPalette(
    variant: AuraFabVariant,
    enabled: Boolean,
    colors: AuraColors,
): AuraFabPalette {
    if (!enabled) {
        return AuraFabPalette(
            containerColor = colors.interaction.disabledContainer,
            contentColor = colors.content.disabled,
            borderColor = Color.Transparent,
            elevation = 0.dp,
        )
    }

    return when (variant) {
        AuraFabVariant.Primary -> AuraFabPalette(
            containerColor = colors.brand.primary,
            contentColor = colors.brand.onPrimary,
            borderColor = Color.Transparent,
            elevation = 8.dp,
        )
        AuraFabVariant.Secondary -> AuraFabPalette(
            containerColor = colors.brand.subtle,
            contentColor = colors.brand.onSubtle,
            borderColor = Color.Transparent,
            elevation = 6.dp,
        )
        AuraFabVariant.Surface -> AuraFabPalette(
            containerColor = colors.surface.floating,
            contentColor = colors.content.primary,
            borderColor = colors.border.subtle,
            elevation = 8.dp,
        )
        AuraFabVariant.Glass -> AuraFabPalette(
            containerColor = colors.surface.floating.copy(alpha = 0.80f),
            contentColor = colors.content.primary,
            borderColor = colors.border.subtle,
            elevation = 6.dp,
        )
    }
}

private fun resolveAuraFabDimensions(
    size: AuraFabSize,
    typography: com.example.ds.foundation.typography.AuraTypography,
): AuraFabDimensions = when (size) {
    AuraFabSize.Small -> AuraFabDimensions(
        containerHeight = 44.dp,
        iconSize = 20.dp,
        spinnerStrokeWidth = 2.dp,
        startPaddingExtended = 14.dp,
        endPaddingExtended = 18.dp,
        iconLabelGap = 8.dp,
        labelStyle = typography.labelMedium,
    )
    AuraFabSize.Regular -> AuraFabDimensions(
        containerHeight = 56.dp,
        iconSize = 24.dp,
        spinnerStrokeWidth = 2.25.dp,
        startPaddingExtended = 16.dp,
        endPaddingExtended = 20.dp,
        iconLabelGap = 10.dp,
        labelStyle = typography.labelLarge,
    )
    AuraFabSize.Large -> AuraFabDimensions(
        containerHeight = 64.dp,
        iconSize = 28.dp,
        spinnerStrokeWidth = 2.5.dp,
        startPaddingExtended = 20.dp,
        endPaddingExtended = 24.dp,
        iconLabelGap = 12.dp,
        labelStyle = typography.titleSmall,
    )
}

@Composable
private fun AuraFabSpinner(
    color: Color,
    size: Dp,
    strokeWidth: Dp,
    reducedMotion: Boolean,
) {
    val motion = AuraTheme.motion
    if (reducedMotion) {
        Canvas(modifier = Modifier.size(size)) {
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
            )
        }
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "AuraFabSpinner")
    val baseRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = motion.spinnerRotationDurationMillis,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "AuraFabSpinnerRotation",
    )
    val sweep by infiniteTransition.animateFloat(
        initialValue = 45f,
        targetValue = 260f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = motion.spinnerSweepDurationMillis,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "AuraFabSpinnerSweep",
    )

    Canvas(modifier = Modifier.size(size)) {
        drawArc(
            color = color,
            startAngle = baseRotation,
            sweepAngle = sweep,
            useCenter = false,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AuraFabPreview() {
    AuraTheme {
        Row(
            modifier = Modifier
                .background(AuraTheme.colors.background.primary)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AuraFab(
                onClick = {},
                text = "New Entry",
                variant = AuraFabVariant.Primary,
                contentDescription = "New Entry",
            ) {
                val tint = LocalAuraContentColor.current
                Canvas(modifier = Modifier.size(20.dp)) {
                    drawLine(
                        color = tint,
                        start = Offset(size.width / 2f, 2f),
                        end = Offset(size.width / 2f, size.height - 2f),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        color = tint,
                        start = Offset(2f, size.height / 2f),
                        end = Offset(size.width - 2f, size.height / 2f),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
            }
            AuraFab(
                onClick = {},
                variant = AuraFabVariant.Surface,
                contentDescription = "Add",
            ) {
                val tint = LocalAuraContentColor.current
                Canvas(modifier = Modifier.size(20.dp)) {
                    drawCircle(color = tint, radius = size.minDimension / 4f)
                }
            }
        }
    }
}
