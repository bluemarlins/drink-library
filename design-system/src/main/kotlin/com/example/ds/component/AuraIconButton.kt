package com.example.ds.component

import android.os.SystemClock
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ds.foundation.theme.AuraTheme
import kotlinx.coroutines.delay

enum class AuraIconButtonVariant {
    Filled,
    Tonal,
    Outlined,
    Ghost,
    Floating,
    Destructive,
}

enum class AuraIconButtonSize {
    Small,
    Medium,
    Large,
}

enum class AuraIconButtonShape {
    Circle,
    RoundedSquare,
}

private val MinIconTouchTargetSize = 48.dp

private data class AuraIconButtonPalette(
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color,
    val elevation: Dp,
)

/**
 * Compact icon-only action control supporting 6 emphasis variants, optional toggle state,
 * 48.dp accessible hit bounds, tactile spring feedback, and reduced-motion compliance.
 */
@Composable
fun AuraIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AuraIconButtonVariant = AuraIconButtonVariant.Ghost,
    size: AuraIconButtonSize = AuraIconButtonSize.Medium,
    shape: AuraIconButtonShape = AuraIconButtonShape.Circle,
    enabled: Boolean = true,
    loading: Boolean = false,
    selected: Boolean? = null,
    contentDescription: String? = null,
    debounceIntervalMs: Long = 300L,
    interactionSource: MutableInteractionSource? = null,
    icon: @Composable () -> Unit,
) {
    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val colors = AuraTheme.colors
    val motion = AuraTheme.motion
    val reducedMotion = rememberAuraReducedMotion()
    val isInteractive = enabled && !loading
    val isSelected = selected == true

    val isPressed by rememberAuraTactilePressed(
        interactionSource = resolvedInteractionSource,
        minPressDurationMillis = motion.minPressDurationMillis,
    )
    val isFocused by resolvedInteractionSource.collectIsFocusedAsState()
    val isHovered by resolvedInteractionSource.collectIsHoveredAsState()

    val palette = resolveAuraIconButtonPalette(
        variant = variant,
        enabled = enabled,
        selected = isSelected,
        colors = colors,
    )

    val colorAnimSpec = if (reducedMotion) {
        snap<Color>()
    } else {
        tween(durationMillis = motion.stateChangeDurationMillis)
    }

    val animatedContainerColor by animateColorAsState(
        targetValue = palette.containerColor,
        animationSpec = colorAnimSpec,
        label = "AuraIconButtonContainerColor",
    )
    val animatedContentColor by animateColorAsState(
        targetValue = palette.contentColor,
        animationSpec = colorAnimSpec,
        label = "AuraIconButtonContentColor",
    )
    val animatedBorderColor by animateColorAsState(
        targetValue = if (isFocused && enabled) colors.border.focus else palette.borderColor,
        animationSpec = colorAnimSpec,
        label = "AuraIconButtonBorderColor",
    )

    val pressedScaleTarget = when (variant) {
        AuraIconButtonVariant.Ghost -> motion.pressedScaleGhost
        else -> motion.pressedScaleDefault
    }
    val containerScale by animateFloatAsState(
        targetValue = if (!reducedMotion && isInteractive && isPressed) pressedScaleTarget else 1f,
        animationSpec = when {
            reducedMotion -> snap()
            isPressed -> motion.pressInSpring()
            else -> motion.pressOutSpring()
        },
        label = "AuraIconButtonScale",
    )
    val iconScale by animateFloatAsState(
        targetValue = if (!reducedMotion && isInteractive && isPressed) motion.leadingIconPressedScale else 1f,
        animationSpec = if (reducedMotion) snap() else motion.iconSpring(),
        label = "AuraIconButtonIconScale",
    )

    val visualSize = when (size) {
        AuraIconButtonSize.Small -> 32.dp
        AuraIconButtonSize.Medium -> 40.dp
        AuraIconButtonSize.Large -> 48.dp
    }
    val iconSize = when (size) {
        AuraIconButtonSize.Small -> 18.dp
        AuraIconButtonSize.Medium -> 20.dp
        AuraIconButtonSize.Large -> 24.dp
    }
    val containerShape: Shape = when (shape) {
        AuraIconButtonShape.Circle -> CircleShape
        AuraIconButtonShape.RoundedSquare -> when (size) {
            AuraIconButtonSize.Small -> RoundedCornerShape(10.dp)
            AuraIconButtonSize.Medium -> RoundedCornerShape(12.dp)
            AuraIconButtonSize.Large -> RoundedCornerShape(14.dp)
        }
    }
    val borderWidth = if (isFocused && enabled) 2.dp else 1.dp

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

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = MinIconTouchTargetSize, minHeight = MinIconTouchTargetSize)
            .semantics {
                if (contentDescription != null) {
                    this.contentDescription = contentDescription
                }
                if (selected != null) {
                    this.selected = selected
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
        Box(
            modifier = Modifier
                .size(visualSize)
                .graphicsLayer {
                    scaleX = containerScale
                    scaleY = containerScale
                }
                .then(
                    if (palette.elevation > 0.dp) {
                        Modifier.shadow(
                            elevation = palette.elevation,
                            shape = containerShape,
                            clip = false,
                        )
                    } else {
                        Modifier
                    },
                )
                .clip(containerShape)
                .background(animatedContainerColor, containerShape)
                .background(overlayColor, containerShape)
                .then(
                    if (animatedBorderColor.alpha > 0f) {
                        Modifier.border(
                            width = borderWidth,
                            color = animatedBorderColor,
                            shape = containerShape,
                        )
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
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
                label = "AuraIconButtonLoadingCrossfade",
            ) { isLoadingState ->
                if (isLoadingState) {
                    AuraCompactSpinner(
                        color = animatedContentColor,
                        size = iconSize,
                        reducedMotion = reducedMotion,
                    )
                } else {
                    CompositionLocalProvider(LocalAuraContentColor provides animatedContentColor) {
                        Box(
                            modifier = Modifier
                                .size(iconSize)
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
            }
        }
    }
}

@Composable
private fun resolveAuraIconButtonPalette(
    variant: AuraIconButtonVariant,
    enabled: Boolean,
    selected: Boolean,
    colors: com.example.ds.foundation.color.AuraColors,
): AuraIconButtonPalette {
    if (!enabled) {
        val isGhostOrOutlined = variant == AuraIconButtonVariant.Ghost || variant == AuraIconButtonVariant.Outlined
        return AuraIconButtonPalette(
            containerColor = if (isGhostOrOutlined) Color.Transparent else colors.interaction.disabledContainer,
            contentColor = colors.content.disabled,
            borderColor = if (variant == AuraIconButtonVariant.Outlined) colors.border.subtle else Color.Transparent,
            elevation = 0.dp,
        )
    }

    return when (variant) {
        AuraIconButtonVariant.Filled -> AuraIconButtonPalette(
            containerColor = colors.brand.primary,
            contentColor = colors.brand.onPrimary,
            borderColor = Color.Transparent,
            elevation = 0.dp,
        )
        AuraIconButtonVariant.Tonal -> AuraIconButtonPalette(
            containerColor = if (selected) colors.brand.primary else colors.brand.subtle,
            contentColor = if (selected) colors.brand.onPrimary else colors.brand.onSubtle,
            borderColor = Color.Transparent,
            elevation = 0.dp,
        )
        AuraIconButtonVariant.Outlined -> AuraIconButtonPalette(
            containerColor = if (selected) colors.interaction.selectedContainer else Color.Transparent,
            contentColor = if (selected) colors.interaction.onSelected else colors.content.primary,
            borderColor = if (selected) colors.brand.primary else colors.border.default,
            elevation = 0.dp,
        )
        AuraIconButtonVariant.Ghost -> AuraIconButtonPalette(
            containerColor = if (selected) colors.interaction.selectedContainer else Color.Transparent,
            contentColor = if (selected) colors.interaction.onSelected else colors.content.primary,
            borderColor = Color.Transparent,
            elevation = 0.dp,
        )
        AuraIconButtonVariant.Floating -> AuraIconButtonPalette(
            containerColor = if (selected) colors.brand.primary else colors.surface.floating.copy(alpha = 0.92f),
            contentColor = if (selected) colors.brand.onPrimary else colors.content.primary,
            borderColor = if (selected) Color.Transparent else colors.border.subtle,
            elevation = 6.dp,
        )
        AuraIconButtonVariant.Destructive -> AuraIconButtonPalette(
            containerColor = if (selected) colors.status.error.solid else colors.status.error.subtle,
            contentColor = if (selected) colors.status.error.onSolid else colors.status.error.onSubtle,
            borderColor = if (selected) colors.status.error.solid else Color.Transparent,
            elevation = 0.dp,
        )
    }
}

@Composable
internal fun rememberAuraReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        val scale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        )
        scale == 0f
    }
}

/**
 * Keeps pressed feedback visible for at least [minPressDurationMillis] so quick taps inside
 * scrollable lists remain crisp instead of collapsing in a single frame.
 */
@Composable
internal fun rememberAuraTactilePressed(
    interactionSource: InteractionSource,
    minPressDurationMillis: Long,
): State<Boolean> {
    val pressedState = remember(interactionSource) { mutableStateOf(false) }

    LaunchedEffect(interactionSource, minPressDurationMillis) {
        var pressStartTimeMs = 0L
        var activePresses = 0
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    activePresses++
                    pressStartTimeMs = SystemClock.uptimeMillis()
                    pressedState.value = true
                }
                is PressInteraction.Release,
                is PressInteraction.Cancel,
                -> {
                    if (activePresses > 0) activePresses--
                    if (activePresses == 0) {
                        val elapsed = SystemClock.uptimeMillis() - pressStartTimeMs
                        val remaining = (minPressDurationMillis - elapsed).coerceAtLeast(0L)
                        if (remaining > 0L) delay(remaining)
                        pressedState.value = false
                    }
                }
            }
        }
    }

    return pressedState
}

@Composable
internal fun rememberAuraDebouncedClick(
    onClick: () -> Unit,
    debounceIntervalMs: Long,
): () -> Unit {
    val currentOnClick by rememberUpdatedState(onClick)
    var lastClickTimestamp by remember { mutableLongStateOf(0L) }
    return remember(debounceIntervalMs) {
        {
            val now = SystemClock.uptimeMillis()
            if (debounceIntervalMs <= 0L || now - lastClickTimestamp >= debounceIntervalMs) {
                lastClickTimestamp = now
                currentOnClick()
            }
        }
    }
}

@Composable
private fun AuraCompactSpinner(
    color: Color,
    size: Dp,
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
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
            )
        }
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "AuraIconButtonSpinner")
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
        label = "AuraIconSpinnerRotation",
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
        label = "AuraIconSpinnerSweep",
    )

    Canvas(modifier = Modifier.size(size)) {
        drawArc(
            color = color,
            startAngle = baseRotation,
            sweepAngle = sweep,
            useCenter = false,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AuraIconButtonPreview() {
    AuraTheme {
        Row(
            modifier = Modifier
                .background(AuraTheme.colors.background.primary)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AuraIconButtonVariant.entries.forEach { variant ->
                AuraIconButton(
                    onClick = {},
                    variant = variant,
                    contentDescription = variant.name,
                ) {
                    val iconTint = LocalAuraContentColor.current
                    Canvas(modifier = Modifier.size(18.dp)) {
                        drawLine(
                            color = iconTint,
                            start = Offset(size.width / 2f, 2f),
                            end = Offset(size.width / 2f, size.height - 2f),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                        drawLine(
                            color = iconTint,
                            start = Offset(2f, size.height / 2f),
                            end = Offset(size.width - 2f, size.height / 2f),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                    }
                }
            }
        }
    }
}
