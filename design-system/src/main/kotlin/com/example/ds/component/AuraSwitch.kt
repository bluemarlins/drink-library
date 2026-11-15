package com.example.ds.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.ds.foundation.theme.AuraTheme
import kotlin.math.roundToInt

enum class AuraSwitchSize {
    Medium,
    Small,
}

private val MinSwitchTouchTarget = 48.dp

private data class SwitchDimensions(
    val trackWidth: Dp,
    val trackHeight: Dp,
    val thumbDiameter: Dp,
    val innerPadding: Dp,
)

/**
 * Immediate boolean setting toggle compliant with Aura semantic tokens, 48.dp hit bounds,
 * physical thumb slide + stretch micro-interaction, and reduced-motion settings.
 */
@Composable
fun AuraSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    size: AuraSwitchSize = AuraSwitchSize.Medium,
    enabled: Boolean = true,
    label: String? = null,
    description: String? = null,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val motion = AuraTheme.motion
    val reducedMotion = rememberAuraReducedMotion()
    val interactionSource = remember { MutableInteractionSource() }
    val isInteractive = enabled && onCheckedChange != null

    val isPressed by rememberAuraTactilePressed(
        interactionSource = interactionSource,
        minPressDurationMillis = motion.minPressDurationMillis,
    )
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()

    val dims = remember(size) {
        when (size) {
            AuraSwitchSize.Medium -> SwitchDimensions(
                trackWidth = 50.dp,
                trackHeight = 30.dp,
                thumbDiameter = 24.dp,
                innerPadding = 3.dp,
            )
            AuraSwitchSize.Small -> SwitchDimensions(
                trackWidth = 42.dp,
                trackHeight = 24.dp,
                thumbDiameter = 18.dp,
                innerPadding = 3.dp,
            )
        }
    }

    val colorSpec = remember(reducedMotion, motion.stateChangeDurationMillis) {
        if (reducedMotion) snap<Color>() else tween(durationMillis = motion.stateChangeDurationMillis)
    }
    val thumbProgressSpec = remember(reducedMotion, motion) {
        if (reducedMotion) snap<Float>() else motion.pressOutSpring<Float>()
    }
    val scaleSpec = remember(reducedMotion, isPressed, motion) {
        when {
            reducedMotion -> snap<Float>()
            isPressed -> motion.pressInSpring<Float>()
            else -> motion.pressOutSpring<Float>()
        }
    }
    val dpSpec = remember(reducedMotion, motion) {
        if (reducedMotion) snap<Dp>() else motion.pressOutSpring<Dp>()
    }

    val thumbProgress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = thumbProgressSpec,
        label = "AuraSwitchThumbProgress",
    )
    val trackScale by animateFloatAsState(
        targetValue = if (!reducedMotion && isInteractive && isPressed) motion.pressedScaleDefault else 1f,
        animationSpec = scaleSpec,
        label = "AuraSwitchTrackScale",
    )
    val targetThumbWidth = if (!reducedMotion && isInteractive && isPressed) {
        dims.thumbDiameter * 1.18f
    } else {
        dims.thumbDiameter
    }
    val animatedThumbWidth by animateDpAsState(
        targetValue = targetThumbWidth,
        animationSpec = dpSpec,
        label = "AuraSwitchThumbWidth",
    )

    val targetTrackColor = when {
        !enabled -> colors.interaction.disabledContainer
        checked -> colors.brand.primary
        else -> colors.surface.sunken
    }
    val targetBorderColor = when {
        !enabled -> colors.border.subtle
        isFocused -> colors.border.focus
        checked -> colors.brand.primary
        else -> colors.border.strong
    }
    val targetThumbColor = when {
        !enabled -> colors.content.disabled
        checked -> colors.brand.onPrimary
        else -> colors.content.secondary
    }

    val animatedTrackColor by animateColorAsState(
        targetValue = targetTrackColor,
        animationSpec = colorSpec,
        label = "AuraSwitchTrackColor",
    )
    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = colorSpec,
        label = "AuraSwitchBorderColor",
    )
    val animatedThumbColor by animateColorAsState(
        targetValue = targetThumbColor,
        animationSpec = colorSpec,
        label = "AuraSwitchThumbColor",
    )

    val pressOverlay = when {
        !isInteractive -> Color.Transparent
        isPressed -> colors.interaction.pressedOverlay
        isHovered -> colors.interaction.pressedOverlay.copy(alpha = 0.06f)
        else -> Color.Transparent
    }

    val toggleModifier = if (onCheckedChange != null) {
        Modifier.toggleable(
            value = checked,
            enabled = enabled,
            role = Role.Switch,
            interactionSource = interactionSource,
            indication = null,
            onValueChange = onCheckedChange,
        )
    } else {
        Modifier
    }

    val borderWidth = if (isFocused && enabled) 2.dp else 1.5.dp
    val trackShape = RoundedCornerShape(dims.trackHeight / 2f)

    val trackVisual: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .size(width = dims.trackWidth, height = dims.trackHeight)
                .graphicsLayer {
                    scaleX = trackScale
                    scaleY = trackScale
                }
                .clip(trackShape)
                .background(animatedTrackColor, trackShape)
                .background(pressOverlay, trackShape)
                .border(width = borderWidth, color = animatedBorderColor, shape = trackShape),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier
                    .offset {
                        val trackWidthPx = dims.trackWidth.toPx()
                        val paddingPx = dims.innerPadding.toPx()
                        val thumbWidthPx = animatedThumbWidth.toPx()
                        val maxTravelPx = (trackWidthPx - (paddingPx * 2f) - thumbWidthPx).coerceAtLeast(0f)
                        IntOffset(
                            x = (paddingPx + maxTravelPx * thumbProgress).roundToInt(),
                            y = 0,
                        )
                    }
                    .size(width = animatedThumbWidth, height = dims.thumbDiameter)
                    .clip(CircleShape)
                    .background(animatedThumbColor, CircleShape),
            )
        }
    }

    if (label == null) {
        Box(
            modifier = modifier
                .defaultMinSize(minWidth = MinSwitchTouchTarget, minHeight = MinSwitchTouchTarget)
                .then(toggleModifier),
            contentAlignment = Alignment.Center,
        ) {
            trackVisual()
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = MinSwitchTouchTarget)
                .then(toggleModifier)
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
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
            Spacer(modifier = Modifier.width(12.dp))
            trackVisual()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AuraSwitchPreview() {
    AuraTheme {
        Column(
            modifier = Modifier
                .background(AuraTheme.colors.background.primary)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AuraSwitch(
                checked = true,
                onCheckedChange = {},
                label = "3D Parallax Wallpaper",
                description = "Subtle depth shift on Home Launcher motion.",
            )
            AuraSwitch(
                checked = false,
                onCheckedChange = {},
                size = AuraSwitchSize.Small,
                label = "Compact Grid Mode",
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AuraSwitch(checked = true, onCheckedChange = {})
                AuraSwitch(checked = false, onCheckedChange = {})
                AuraSwitch(checked = true, onCheckedChange = {}, enabled = false)
            }
        }
    }
}
