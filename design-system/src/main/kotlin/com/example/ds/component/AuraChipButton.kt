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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ds.foundation.theme.AuraTheme

enum class AuraChipVariant {
    Filter,
    Tag,
    Assist,
    Glass,
}

enum class AuraChipSize {
    Small,
    Medium,
}

private val MinChipTouchTargetHeight = 48.dp

private data class AuraChipPalette(
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color,
    val elevation: Dp,
)

/**
 * Compact capsule control for dataset filtering, attribute tagging, contextual assist actions,
 * and translucent Floating AppBar sub-ribbons.
 */
@Composable
fun AuraChipButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    variant: AuraChipVariant = AuraChipVariant.Filter,
    size: AuraChipSize = AuraChipSize.Medium,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    debounceIntervalMs: Long = 200L,
    interactionSource: MutableInteractionSource? = null,
) {
    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val motion = AuraTheme.motion
    val reducedMotion = rememberAuraReducedMotion()

    val isPressed by rememberAuraTactilePressed(
        interactionSource = resolvedInteractionSource,
        minPressDurationMillis = motion.minPressDurationMillis,
    )
    val isFocused by resolvedInteractionSource.collectIsFocusedAsState()
    val isHovered by resolvedInteractionSource.collectIsHoveredAsState()

    val palette = resolveAuraChipPalette(
        variant = variant,
        enabled = enabled,
        selected = selected,
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
        label = "AuraChipContainerColor",
    )
    val animatedContentColor by animateColorAsState(
        targetValue = palette.contentColor,
        animationSpec = colorAnimSpec,
        label = "AuraChipContentColor",
    )
    val animatedBorderColor by animateColorAsState(
        targetValue = if (isFocused && enabled) colors.border.focus else palette.borderColor,
        animationSpec = colorAnimSpec,
        label = "AuraChipBorderColor",
    )

    val pressedScaleTarget = when (variant) {
        AuraChipVariant.Tag -> motion.pressedScaleGhost
        else -> motion.pressedScaleDefault
    }
    val containerScale by animateFloatAsState(
        targetValue = if (!reducedMotion && enabled && isPressed) pressedScaleTarget else 1f,
        animationSpec = when {
            reducedMotion -> snap()
            isPressed -> motion.pressInSpring()
            else -> motion.pressOutSpring()
        },
        label = "AuraChipScale",
    )
    val iconScale by animateFloatAsState(
        targetValue = if (!reducedMotion && enabled && isPressed) motion.leadingIconPressedScale else 1f,
        animationSpec = if (reducedMotion) snap() else motion.iconSpring(),
        label = "AuraChipIconScale",
    )

    val visualHeight: Dp
    val horizontalPadding: Dp
    val iconSize: Dp
    val labelStyle = when (size) {
        AuraChipSize.Small -> {
            visualHeight = 28.dp
            horizontalPadding = 10.dp
            iconSize = 14.dp
            typography.labelSmall
        }
        AuraChipSize.Medium -> {
            visualHeight = 34.dp
            horizontalPadding = 14.dp
            iconSize = 16.dp
            typography.labelMedium
        }
    }

    val debouncedOnClick = rememberAuraDebouncedClick(
        onClick = onClick,
        debounceIntervalMs = debounceIntervalMs,
    )

    val overlayColor = when {
        !enabled -> Color.Transparent
        isPressed -> colors.interaction.pressedOverlay
        isHovered -> colors.interaction.pressedOverlay.copy(alpha = 0.06f)
        else -> Color.Transparent
    }

    val borderWidth = if (isFocused && enabled) 2.dp else 1.dp
    val semanticRole = if (variant == AuraChipVariant.Filter) Role.Checkbox else Role.Button

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = MinChipTouchTargetHeight)
            .semantics {
                when (variant) {
                    AuraChipVariant.Filter -> {
                        this.toggleableState = if (selected) ToggleableState.On else ToggleableState.Off
                    }
                    AuraChipVariant.Tag -> {
                        this.selected = selected
                    }
                    AuraChipVariant.Assist,
                    AuraChipVariant.Glass,
                    -> {
                        if (selected) this.selected = true
                    }
                }
            }
            .clickable(
                enabled = enabled,
                interactionSource = resolvedInteractionSource,
                indication = null,
                role = semanticRole,
                onClick = debouncedOnClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .height(visualHeight)
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
                .padding(horizontal = horizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            CompositionLocalProvider(LocalAuraContentColor provides animatedContentColor) {
                if (leadingIcon != null) {
                    Box(
                        modifier = Modifier
                            .size(iconSize)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        leadingIcon()
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                AuraText(
                    text = label,
                    color = animatedContentColor,
                    style = labelStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier.size(iconSize),
                        contentAlignment = Alignment.Center,
                    ) {
                        trailingIcon()
                    }
                }
            }
        }
    }
}

@Composable
private fun resolveAuraChipPalette(
    variant: AuraChipVariant,
    enabled: Boolean,
    selected: Boolean,
    colors: com.example.ds.foundation.color.AuraColors,
): AuraChipPalette {
    if (!enabled) {
        return AuraChipPalette(
            containerColor = colors.interaction.disabledContainer,
            contentColor = colors.content.disabled,
            borderColor = colors.border.subtle,
            elevation = 0.dp,
        )
    }

    return when (variant) {
        AuraChipVariant.Filter -> AuraChipPalette(
            containerColor = if (selected) colors.interaction.selectedContainer else colors.surface.sunken,
            contentColor = if (selected) colors.interaction.onSelected else colors.content.secondary,
            borderColor = if (selected) colors.brand.primary else colors.border.subtle,
            elevation = 0.dp,
        )
        AuraChipVariant.Tag -> AuraChipPalette(
            containerColor = if (selected) colors.brand.subtle else colors.surface.default,
            contentColor = if (selected) colors.brand.onSubtle else colors.content.primary,
            borderColor = if (selected) colors.brand.primary else colors.border.default,
            elevation = 0.dp,
        )
        AuraChipVariant.Assist -> AuraChipPalette(
            containerColor = if (selected) colors.interaction.selectedContainer else colors.surface.raised,
            contentColor = if (selected) colors.interaction.onSelected else colors.content.primary,
            borderColor = if (selected) colors.brand.primary else colors.border.default,
            elevation = 0.dp,
        )
        AuraChipVariant.Glass -> AuraChipPalette(
            containerColor = if (selected) {
                colors.brand.primary.copy(alpha = 0.92f)
            } else {
                colors.surface.floating.copy(alpha = 0.78f)
            },
            contentColor = if (selected) colors.brand.onPrimary else colors.content.primary,
            borderColor = colors.border.subtle,
            elevation = 2.dp,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AuraChipButtonPreview() {
    AuraTheme {
        Row(
            modifier = Modifier
                .background(AuraTheme.colors.background.primary)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AuraChipButton(
                label = "All",
                onClick = {},
                selected = true,
                variant = AuraChipVariant.Filter,
            )
            AuraChipButton(
                label = "Smoky",
                onClick = {},
                selected = false,
                variant = AuraChipVariant.Tag,
            )
            AuraChipButton(
                label = "Today",
                onClick = {},
                variant = AuraChipVariant.Assist,
            )
        }
    }
}
