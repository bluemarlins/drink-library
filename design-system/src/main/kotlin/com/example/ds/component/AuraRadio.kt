package com.example.ds.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ds.foundation.theme.AuraTheme

private val MinRadioTouchTarget = 48.dp
private val RadioRingSize = 20.dp
private val RadioDotSize = 10.dp

/**
 * Mutually exclusive single-choice radio control with animated inner dot spring,
 * semantic token bindings, and 48.dp minimum interactive touch area.
 */
@Composable
fun AuraRadio(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null,
    description: String? = null,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val motion = AuraTheme.motion
    val reducedMotion = rememberAuraReducedMotion()
    val interactionSource = remember { MutableInteractionSource() }
    val isInteractive = enabled && onClick != null

    val isPressed by rememberAuraTactilePressed(
        interactionSource = interactionSource,
        minPressDurationMillis = motion.minPressDurationMillis,
    )
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()

    val colorSpec = remember(reducedMotion, motion.stateChangeDurationMillis) {
        if (reducedMotion) snap<Color>() else tween(durationMillis = motion.stateChangeDurationMillis)
    }
    val dotSpec = remember(reducedMotion, motion) {
        if (reducedMotion) snap<Float>() else motion.iconSpring<Float>()
    }
    val scaleSpec = remember(reducedMotion, isPressed, motion) {
        when {
            reducedMotion -> snap<Float>()
            isPressed -> motion.pressInSpring<Float>()
            else -> motion.pressOutSpring<Float>()
        }
    }

    val dotScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = dotSpec,
        label = "AuraRadioDotScale",
    )
    val ringScale by animateFloatAsState(
        targetValue = if (!reducedMotion && isInteractive && isPressed) motion.pressedScaleDefault else 1f,
        animationSpec = scaleSpec,
        label = "AuraRadioRingScale",
    )

    val targetRingFill = if (!enabled && !selected) {
        colors.interaction.disabledContainer
    } else {
        colors.surface.default
    }
    val targetBorderColor = when {
        !enabled -> colors.content.disabled
        isFocused -> colors.border.focus
        selected -> colors.brand.primary
        else -> colors.border.strong
    }
    val targetDotColor = when {
        !enabled -> colors.content.disabled
        else -> colors.brand.primary
    }

    val animatedRingFill by animateColorAsState(
        targetValue = targetRingFill,
        animationSpec = colorSpec,
        label = "AuraRadioRingFill",
    )
    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = colorSpec,
        label = "AuraRadioBorderColor",
    )
    val animatedDotColor by animateColorAsState(
        targetValue = targetDotColor,
        animationSpec = colorSpec,
        label = "AuraRadioDotColor",
    )

    val pressOverlay = when {
        !isInteractive -> Color.Transparent
        isPressed -> colors.interaction.pressedOverlay
        isHovered -> colors.interaction.pressedOverlay.copy(alpha = 0.06f)
        else -> Color.Transparent
    }

    val borderWidth = if ((isFocused && enabled) || selected) 2.dp else 1.75.dp

    val selectModifier = if (onClick != null) {
        Modifier.selectable(
            selected = selected,
            enabled = enabled,
            role = Role.RadioButton,
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
        )
    } else {
        Modifier
    }

    val radioVisual: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .size(RadioRingSize)
                .graphicsLayer {
                    scaleX = ringScale
                    scaleY = ringScale
                }
                .clip(CircleShape)
                .background(animatedRingFill, CircleShape)
                .background(pressOverlay, CircleShape)
                .border(width = borderWidth, color = animatedBorderColor, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (dotScale > 0.01f) {
                Box(
                    modifier = Modifier
                        .size(RadioDotSize)
                        .graphicsLayer {
                            scaleX = dotScale
                            scaleY = dotScale
                        }
                        .clip(CircleShape)
                        .background(animatedDotColor, CircleShape),
                )
            }
        }
    }

    if (label == null) {
        Box(
            modifier = modifier
                .defaultMinSize(minWidth = MinRadioTouchTarget, minHeight = MinRadioTouchTarget)
                .then(selectModifier),
            contentAlignment = Alignment.Center,
        ) {
            radioVisual()
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = MinRadioTouchTarget)
                .then(selectModifier)
                .padding(vertical = 6.dp),
            verticalAlignment = if (description == null) Alignment.CenterVertically else Alignment.Top,
        ) {
            Box(
                modifier = Modifier.padding(top = if (description == null) 0.dp else 2.dp),
                contentAlignment = Alignment.Center,
            ) {
                radioVisual()
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                AuraText(
                    text = label,
                    style = typography.bodyLarge,
                    color = if (enabled) colors.content.primary else colors.content.disabled,
                )
                if (description != null) {
                    AuraText(
                        text = description,
                        style = typography.bodySmall,
                        color = if (enabled) colors.content.secondary else colors.content.disabled,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AuraRadioPreview() {
    AuraTheme {
        Column(
            modifier = Modifier
                .background(AuraTheme.colors.background.primary)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AuraRadio(
                selected = true,
                onClick = {},
                label = "Korean Won (KRW)",
                description = "Format balances with ₩ symbol and whole units.",
            )
            AuraRadio(
                selected = false,
                onClick = {},
                label = "US Dollar (USD)",
                description = "Format balances with $ and 2 decimal places.",
            )
            AuraRadio(
                selected = false,
                onClick = {},
                enabled = false,
                label = "Japanese Yen (JPY · Unavailable)",
            )
        }
    }
}
