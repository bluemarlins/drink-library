package com.example.ds.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ds.foundation.color.AuraColors
import com.example.ds.foundation.theme.AuraColorMode
import com.example.ds.foundation.theme.AuraTheme

/**
 * Semantic tone variants for [AuraBadge].
 */
enum class AuraBadgeVariant {
    Brand,
    Neutral,
    Success,
    Warning,
    Error,
    Info,
}

/**
 * Physical size tiers for [AuraBadge].
 */
enum class AuraBadgeSize {
    Dot,
    Small,
    Medium,
}

private data class AuraBadgePalette(
    val container: Color,
    val content: Color,
    val cutoutStroke: Color,
)

private fun resolveAuraBadgePalette(
    variant: AuraBadgeVariant,
    solidEmphasis: Boolean,
    colors: AuraColors,
): AuraBadgePalette {
    val (container, content) = when (variant) {
        AuraBadgeVariant.Brand -> if (solidEmphasis) {
            colors.brand.primary to colors.brand.onPrimary
        } else {
            colors.brand.subtle to colors.brand.onSubtle
        }
        AuraBadgeVariant.Neutral -> colors.surface.sunken to colors.content.secondary
        AuraBadgeVariant.Success -> if (solidEmphasis) {
            colors.status.success.solid to colors.status.success.onSolid
        } else {
            colors.status.success.subtle to colors.status.success.onSubtle
        }
        AuraBadgeVariant.Warning -> if (solidEmphasis) {
            colors.status.warning.solid to colors.status.warning.onSolid
        } else {
            colors.status.warning.subtle to colors.status.warning.onSubtle
        }
        AuraBadgeVariant.Error -> if (solidEmphasis) {
            colors.status.error.solid to colors.status.error.onSolid
        } else {
            colors.status.error.subtle to colors.status.error.onSubtle
        }
        AuraBadgeVariant.Info -> if (solidEmphasis) {
            colors.status.info.solid to colors.status.info.onSolid
        } else {
            colors.status.info.subtle to colors.status.info.onSubtle
        }
    }
    return AuraBadgePalette(
        container = container,
        content = content,
        cutoutStroke = colors.surface.default,
    )
}

/**
 * Compact semantic status pill, numeric counter (`"99+"`), or presence dot built on [AuraTheme].
 */
@Composable
fun AuraBadge(
    modifier: Modifier = Modifier,
    label: String? = null,
    count: Int? = null,
    maxCount: Int = 99,
    variant: AuraBadgeVariant = AuraBadgeVariant.Brand,
    size: AuraBadgeSize = AuraBadgeSize.Medium,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val motion = AuraTheme.motion
    val reducedMotion = rememberAuraReducedMotion()

    val displayText = when {
        size == AuraBadgeSize.Dot -> null
        count != null -> if (count > maxCount) "$maxCount+" else count.coerceAtLeast(0).toString()
        !label.isNullOrBlank() -> label
        else -> null
    }
    val solidEmphasis = size == AuraBadgeSize.Dot || (count != null && label == null)
    val targetPalette = resolveAuraBadgePalette(
        variant = variant,
        solidEmphasis = solidEmphasis,
        colors = colors,
    )

    val containerColor by animateColorAsState(
        targetValue = targetPalette.container,
        animationSpec = if (reducedMotion) snap() else tween(durationMillis = motion.stateChangeDurationMillis),
        label = "AuraBadgeContainer",
    )
    val contentColor by animateColorAsState(
        targetValue = targetPalette.content,
        animationSpec = if (reducedMotion) snap() else tween(durationMillis = motion.stateChangeDurationMillis),
        label = "AuraBadgeContent",
    )
    val presenceScale by animateFloatAsState(
        targetValue = if (size == AuraBadgeSize.Dot || displayText != null) 1f else 0.9f,
        animationSpec = if (reducedMotion) snap() else motion.pressOutSpring(),
        label = "AuraBadgeScale",
    )

    if (size == AuraBadgeSize.Dot || displayText == null) {
        Box(
            modifier = modifier
                .size(10.dp)
                .graphicsLayer {
                    scaleX = presenceScale
                    scaleY = presenceScale
                }
                .clip(CircleShape)
                .background(containerColor)
                .border(1.5.dp, targetPalette.cutoutStroke, CircleShape)
                .semantics {
                    contentDescription = "Unread status indicator"
                },
        )
        return
    }

    val minWidth = when (size) {
        AuraBadgeSize.Dot -> 10.dp
        AuraBadgeSize.Small -> 18.dp
        AuraBadgeSize.Medium -> 24.dp
    }
    val minHeight = when (size) {
        AuraBadgeSize.Dot -> 10.dp
        AuraBadgeSize.Small -> 18.dp
        AuraBadgeSize.Medium -> 24.dp
    }
    val horizontalPad = when (size) {
        AuraBadgeSize.Dot -> 0.dp
        AuraBadgeSize.Small -> 6.dp
        AuraBadgeSize.Medium -> 10.dp
    }
    val verticalPad = when (size) {
        AuraBadgeSize.Dot -> 0.dp
        AuraBadgeSize.Small -> 2.dp
        AuraBadgeSize.Medium -> 4.dp
    }
    val shape = when (size) {
        AuraBadgeSize.Dot -> CircleShape
        AuraBadgeSize.Small -> CircleShape
        AuraBadgeSize.Medium -> RoundedCornerShape(8.dp)
    }
    val textStyle = when (size) {
        AuraBadgeSize.Dot, AuraBadgeSize.Small -> typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
        AuraBadgeSize.Medium -> typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
    }

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = minWidth, minHeight = minHeight)
            .graphicsLayer {
                scaleX = presenceScale
                scaleY = presenceScale
            }
            .clip(shape)
            .background(containerColor)
            .then(
                if (solidEmphasis) {
                    Modifier.border(1.dp, targetPalette.cutoutStroke, shape)
                } else {
                    Modifier
                },
            )
            .padding(horizontal = horizontalPad, vertical = verticalPad),
        contentAlignment = Alignment.Center,
    ) {
        AuraText(
            text = displayText,
            style = textStyle,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Anchors a badge cleanly at the top-end corner of an icon or control without obscuring its center glyph.
 */
@Composable
fun AuraBadgedBox(
    badge: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier) {
        content()
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = (-4).dp),
            contentAlignment = Alignment.Center,
            content = badge,
        )
    }
}

@Preview(name = "AuraBadge Light", showBackground = true)
@Composable
private fun AuraBadgeLightPreview() {
    AuraTheme(colorMode = AuraColorMode.Light) {
        Column(
            modifier = Modifier
                .background(AuraTheme.colors.background.primary)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AuraBadge(label = "Ready", variant = AuraBadgeVariant.Success)
                AuraBadge(label = "Planned", variant = AuraBadgeVariant.Neutral)
                AuraBadge(label = "Featured", variant = AuraBadgeVariant.Brand)
                AuraBadge(label = "Low Stock", variant = AuraBadgeVariant.Warning)
                AuraBadge(label = "Alert", variant = AuraBadgeVariant.Error)
                AuraBadge(label = "Sync", variant = AuraBadgeVariant.Info)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                AuraBadge(count = 4, variant = AuraBadgeVariant.Error, size = AuraBadgeSize.Small)
                AuraBadge(count = 128, variant = AuraBadgeVariant.Brand, size = AuraBadgeSize.Small)
                AuraBadge(size = AuraBadgeSize.Dot, variant = AuraBadgeVariant.Success)
            }
        }
    }
}
