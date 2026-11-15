package com.example.ds.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ds.foundation.theme.AuraTheme

enum class AuraCheckboxState {
    Unchecked,
    Checked,
    Indeterminate,
}

private val MinCheckboxTouchTarget = 48.dp
private val CheckboxVisualSize = 20.dp
private val CheckboxCornerShape = RoundedCornerShape(6.dp)

/**
 * Tri-state selection checkbox supporting checked, unchecked, indeterminate batch states,
 * error emphasis, task-completion strikethrough, and 48.dp accessible hit bounds.
 */
@Composable
fun AuraCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    indeterminate: Boolean = false,
    enabled: Boolean = true,
    isError: Boolean = false,
    label: String? = null,
    description: String? = null,
    strikethroughWhenChecked: Boolean = false,
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

    val checkboxState = when {
        indeterminate -> AuraCheckboxState.Indeterminate
        checked -> AuraCheckboxState.Checked
        else -> AuraCheckboxState.Unchecked
    }
    val toggleableState = when (checkboxState) {
        AuraCheckboxState.Checked -> ToggleableState.On
        AuraCheckboxState.Indeterminate -> ToggleableState.Indeterminate
        AuraCheckboxState.Unchecked -> ToggleableState.Off
    }
    val isMarked = checkboxState != AuraCheckboxState.Unchecked

    val colorSpec = remember(reducedMotion, motion.stateChangeDurationMillis) {
        if (reducedMotion) snap<Color>() else tween(durationMillis = motion.stateChangeDurationMillis)
    }
    val glyphSpec = remember(reducedMotion, motion) {
        if (reducedMotion) snap<Float>() else motion.iconSpring<Float>()
    }
    val scaleSpec = remember(reducedMotion, isPressed, motion) {
        when {
            reducedMotion -> snap<Float>()
            isPressed -> motion.pressInSpring<Float>()
            else -> motion.pressOutSpring<Float>()
        }
    }

    val glyphProgress by animateFloatAsState(
        targetValue = if (isMarked) 1f else 0f,
        animationSpec = glyphSpec,
        label = "AuraCheckboxGlyphProgress",
    )
    val boxScale by animateFloatAsState(
        targetValue = if (!reducedMotion && isInteractive && isPressed) motion.pressedScaleDefault else 1f,
        animationSpec = scaleSpec,
        label = "AuraCheckboxBoxScale",
    )

    // State Precedence: Disabled > Error > Pressed > Checked / Indeterminate / Unchecked
    val targetFillColor = when {
        !enabled && isMarked -> colors.interaction.disabledContainer
        !enabled -> Color.Transparent
        isError && isMarked -> colors.status.error.solid
        isMarked -> colors.brand.primary
        else -> colors.surface.default
    }
    val targetBorderColor = when {
        !enabled -> colors.content.disabled
        isFocused -> colors.border.focus
        isError -> colors.status.error.solid
        isMarked -> colors.brand.primary
        else -> colors.border.strong
    }
    val targetGlyphColor = when {
        !enabled -> colors.content.disabled
        isError -> colors.status.error.onSolid
        isMarked -> colors.brand.onPrimary
        else -> Color.Transparent
    }

    val animatedFillColor by animateColorAsState(
        targetValue = targetFillColor,
        animationSpec = colorSpec,
        label = "AuraCheckboxFillColor",
    )
    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = colorSpec,
        label = "AuraCheckboxBorderColor",
    )
    val animatedGlyphColor by animateColorAsState(
        targetValue = targetGlyphColor,
        animationSpec = colorSpec,
        label = "AuraCheckboxGlyphColor",
    )

    val pressOverlay = when {
        !isInteractive -> Color.Transparent
        isPressed -> colors.interaction.pressedOverlay
        isHovered -> colors.interaction.pressedOverlay.copy(alpha = 0.06f)
        else -> Color.Transparent
    }

    val borderWidth = if ((isFocused && enabled) || isError) 2.dp else 1.75.dp

    val toggleModifier = if (onCheckedChange != null) {
        Modifier.triStateToggleable(
            state = toggleableState,
            enabled = enabled,
            role = Role.Checkbox,
            interactionSource = interactionSource,
            indication = null,
            onClick = { onCheckedChange(!checked) },
        )
    } else {
        Modifier
    }

    val checkPath = remember { Path() }
    val partialCheckPath = remember { Path() }
    val pathMeasure = remember { PathMeasure() }

    val checkboxVisual: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .size(CheckboxVisualSize)
                .graphicsLayer {
                    scaleX = boxScale
                    scaleY = boxScale
                }
                .clip(CheckboxCornerShape)
                .background(animatedFillColor, CheckboxCornerShape)
                .background(pressOverlay, CheckboxCornerShape)
                .border(width = borderWidth, color = animatedBorderColor, shape = CheckboxCornerShape),
            contentAlignment = Alignment.Center,
        ) {
            if (glyphProgress > 0.01f) {
                Canvas(modifier = Modifier.size(CheckboxVisualSize)) {
                    val strokeWidth = 2.dp.toPx()
                    if (checkboxState == AuraCheckboxState.Indeterminate) {
                        val centerY = size.height / 2f
                        val startX = size.width * 0.28f
                        val endX = startX + (size.width * 0.44f) * glyphProgress.coerceIn(0f, 1f)
                        drawLine(
                            color = animatedGlyphColor,
                            start = Offset(startX, centerY),
                            end = Offset(endX, centerY),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round,
                        )
                    } else {
                        checkPath.reset()
                        checkPath.moveTo(size.width * 0.24f, size.height * 0.52f)
                        checkPath.lineTo(size.width * 0.43f, size.height * 0.71f)
                        checkPath.lineTo(size.width * 0.76f, size.height * 0.31f)

                        pathMeasure.setPath(checkPath, false)
                        partialCheckPath.reset()
                        val endDistance = pathMeasure.length * glyphProgress.coerceIn(0f, 1f)
                        pathMeasure.getSegment(0f, endDistance, partialCheckPath, true)

                        drawPath(
                            path = partialCheckPath,
                            color = animatedGlyphColor,
                            style = Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                            ),
                        )
                    }
                }
            }
        }
    }

    if (label == null) {
        Box(
            modifier = modifier
                .defaultMinSize(minWidth = MinCheckboxTouchTarget, minHeight = MinCheckboxTouchTarget)
                .then(toggleModifier),
            contentAlignment = Alignment.Center,
        ) {
            checkboxVisual()
        }
    } else {
        val shouldStrike = strikethroughWhenChecked && checkboxState == AuraCheckboxState.Checked
        val labelColor = when {
            !enabled -> colors.content.disabled
            isError -> colors.status.error.solid
            shouldStrike -> colors.content.secondary
            else -> colors.content.primary
        }
        Row(
            modifier = modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = MinCheckboxTouchTarget)
                .then(toggleModifier)
                .padding(vertical = 6.dp),
            verticalAlignment = if (description == null) Alignment.CenterVertically else Alignment.Top,
        ) {
            Box(
                modifier = Modifier.padding(top = if (description == null) 0.dp else 2.dp),
                contentAlignment = Alignment.Center,
            ) {
                checkboxVisual()
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                AuraText(
                    text = label,
                    style = typography.bodyLarge.copy(
                        textDecoration = if (shouldStrike) TextDecoration.LineThrough else TextDecoration.None,
                    ),
                    color = labelColor,
                )
                if (description != null) {
                    AuraText(
                        text = description,
                        style = typography.bodySmall,
                        color = if (!enabled) colors.content.disabled else colors.content.secondary,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AuraCheckboxPreview() {
    AuraTheme {
        Column(
            modifier = Modifier
                .background(AuraTheme.colors.background.primary)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AuraCheckbox(
                checked = true,
                onCheckedChange = {},
                label = "Finalize Q3 dividend rebalance",
                description = "My Planner task checklist with strikethrough",
                strikethroughWhenChecked = true,
            )
            AuraCheckbox(
                checked = false,
                indeterminate = true,
                onCheckedChange = {},
                label = "Select all brokerage accounts",
            )
            AuraCheckbox(
                checked = false,
                onCheckedChange = {},
                isError = true,
                label = "Accept risk disclosure",
            )
        }
    }
}
