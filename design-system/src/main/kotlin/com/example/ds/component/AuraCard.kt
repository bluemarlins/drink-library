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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ds.foundation.theme.AuraTheme

enum class AuraCardVariant {
    Elevated,
    Filled,
    Outlined,
    Glass,
}

enum class AuraCardPadding {
    None,
    Compact,
    Medium,
    Large,
}

/**
 * Grouped surface container supporting Outlined, Elevated, Filled, and Glass surface tokens,
 * standardized internal padding presets, and gentle large-surface tactile spring interaction
 * when [onClick] is supplied.
 */
@Composable
fun AuraCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    variant: AuraCardVariant = AuraCardVariant.Outlined,
    padding: AuraCardPadding = AuraCardPadding.Medium,
    enabled: Boolean = true,
    selected: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = AuraTheme.colors
    val motion = AuraTheme.motion
    val reducedMotion = rememberAuraReducedMotion()
    val interactionSource = remember { MutableInteractionSource() }
    val isInteractive = onClick != null && enabled

    val isPressed by rememberAuraTactilePressed(
        interactionSource = interactionSource,
        minPressDurationMillis = motion.minPressDurationMillis,
    )
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()

    val cornerRadius = if (variant == AuraCardVariant.Glass) 20.dp else 16.dp
    val shape = RoundedCornerShape(cornerRadius)
    val elevation = when {
        !enabled -> 0.dp
        variant == AuraCardVariant.Elevated -> 4.dp
        variant == AuraCardVariant.Glass -> 2.dp
        else -> 0.dp
    }
    val paddingDp = when (padding) {
        AuraCardPadding.None -> 0.dp
        AuraCardPadding.Compact -> 12.dp
        AuraCardPadding.Medium -> 16.dp
        AuraCardPadding.Large -> 24.dp
    }

    val colorSpec = remember(reducedMotion, motion.stateChangeDurationMillis) {
        if (reducedMotion) snap<Color>() else tween(durationMillis = motion.stateChangeDurationMillis)
    }
    val scaleSpec = remember(reducedMotion, isPressed, motion) {
        when {
            reducedMotion -> snap<Float>()
            isPressed -> motion.pressInSpring<Float>()
            else -> motion.pressOutSpring<Float>()
        }
    }

    val containerScale by animateFloatAsState(
        targetValue = if (!reducedMotion && isInteractive && isPressed) motion.pressedScaleLarge else 1f,
        animationSpec = scaleSpec,
        label = "AuraCardScale",
    )

    val baseContainerColor = when {
        !enabled -> colors.interaction.disabledContainer
        variant == AuraCardVariant.Elevated -> colors.surface.raised
        variant == AuraCardVariant.Filled -> colors.surface.sunken
        variant == AuraCardVariant.Glass -> colors.surface.floating
        else -> colors.surface.default
    }
    val targetBorderColor = when {
        !enabled -> colors.border.subtle
        isFocused && isInteractive -> colors.border.focus
        selected -> colors.brand.primary
        variant == AuraCardVariant.Outlined -> colors.border.default
        else -> colors.border.subtle
    }

    val animatedContainerColor by animateColorAsState(
        targetValue = baseContainerColor,
        animationSpec = colorSpec,
        label = "AuraCardContainerColor",
    )
    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = colorSpec,
        label = "AuraCardBorderColor",
    )

    val selectionTint = if (enabled && selected) {
        val alpha = if (variant == AuraCardVariant.Glass) 0.22f else 0.36f
        colors.interaction.selectedContainer.copy(alpha = alpha)
    } else {
        Color.Transparent
    }
    val pressOverlay = when {
        !isInteractive -> Color.Transparent
        isPressed -> colors.interaction.pressedOverlay
        isHovered -> colors.interaction.pressedOverlay.copy(alpha = 0.05f)
        else -> Color.Transparent
    }

    val borderWidth = if (selected || (isFocused && isInteractive)) 1.5.dp else 1.dp

    val clickableModifier = if (onClick != null) {
        val debouncedOnClick = rememberAuraDebouncedClick(
            onClick = onClick,
            debounceIntervalMs = 300L,
        )
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            role = Role.Button,
            onClick = debouncedOnClick,
        )
    } else {
        Modifier
    }

    val cardModifier = modifier
        .graphicsLayer {
            scaleX = containerScale
            scaleY = containerScale
        }
        .then(
            if (elevation > 0.dp) {
                Modifier.shadow(
                    elevation = elevation,
                    shape = shape,
                    clip = false,
                )
            } else {
                Modifier
            },
        )
        .clip(shape)
        .background(animatedContainerColor, shape)
        .background(selectionTint, shape)
        .background(pressOverlay, shape)
        .border(width = borderWidth, color = animatedBorderColor, shape = shape)
        .then(clickableModifier)

    Box(modifier = cardModifier) {
        CompositionLocalProvider(LocalAuraContentColor provides colors.content.primary) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingDp),
                content = content,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AuraCardPreview() {
    AuraTheme {
        Column(
            modifier = Modifier
                .background(AuraTheme.colors.background.primary)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AuraCard(variant = AuraCardVariant.Outlined, onClick = {}) {
                AuraText(text = "Outlined Interactive Card", style = AuraTheme.typography.titleSmall)
                AuraText(
                    text = "Tap to trigger large-surface tactile spring compression.",
                    style = AuraTheme.typography.bodySmall,
                    color = AuraTheme.colors.content.secondary,
                )
            }
            AuraCard(variant = AuraCardVariant.Elevated, selected = true, onClick = {}) {
                AuraText(text = "Elevated Selected Card", style = AuraTheme.typography.titleSmall)
            }
            AuraCard(variant = AuraCardVariant.Filled, padding = AuraCardPadding.Compact) {
                AuraText(text = "Filled Passive Summary Block", style = AuraTheme.typography.bodyMedium)
            }
        }
    }
}
