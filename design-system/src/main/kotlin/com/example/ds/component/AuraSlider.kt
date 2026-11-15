package com.example.ds.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ds.foundation.theme.AuraColorMode
import com.example.ds.foundation.theme.AuraTheme
import kotlin.math.roundToInt

private val SliderMinTouchHeight = 48.dp
private val SliderTrackHeight = 6.dp
private val SliderThumbRadius = 11.dp
private val SliderThumbHaloRadius = 20.dp

internal fun quantizeFraction(
    rawFraction: Float,
    steps: Int,
): Float {
    val clamped = rawFraction.coerceIn(0f, 1f)
    if (steps <= 0) return clamped
    val intervals = steps + 1
    return (clamped * intervals).roundToInt().toFloat() / intervals.toFloat()
}

internal fun valueToFraction(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
): Float {
    val span = valueRange.endInclusive - valueRange.start
    if (span <= 0f) return 0f
    return ((value - valueRange.start) / span).coerceIn(0f, 1f)
}

internal fun fractionToValue(
    fraction: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
): Float {
    val snapped = quantizeFraction(fraction, steps)
    val span = valueRange.endInclusive - valueRange.start
    return (valueRange.start + snapped * span).coerceIn(valueRange.start, valueRange.endInclusive)
}

/**
 * Continuous and stepped range slider implementing the `aura.component.slider` specification.
 */
@Composable
fun AuraSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    label: String? = null,
    valueLabel: String? = null,
    supportingText: String? = null,
    interactionSource: MutableInteractionSource? = null,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val motion = AuraTheme.motion
    val reducedMotion = rememberAuraReducedMotion()
    val density = LocalDensity.current
    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }

    val targetFraction = valueToFraction(value, valueRange)
    var isDragging by remember { mutableStateOf(false) }
    var trackWidthPx by remember { mutableFloatStateOf(0f) }

    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val currentOnFinished by rememberUpdatedState(onValueChangeFinished)
    val currentValueRange by rememberUpdatedState(valueRange)
    val currentSteps by rememberUpdatedState(steps)
    val currentValue by rememberUpdatedState(value)

    val animatedFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = if (reducedMotion || isDragging) snap() else motion.pressOutSpring(),
        label = "AuraSliderFraction",
    )
    val thumbScale by animateFloatAsState(
        targetValue = if (isDragging && enabled && !reducedMotion) 1.18f else 1f,
        animationSpec = when {
            reducedMotion -> snap()
            isDragging -> motion.pressInSpring()
            else -> motion.pressOutSpring()
        },
        label = "AuraSliderThumbScale",
    )
    val activeTrackColor by animateColorAsState(
        targetValue = if (enabled) colors.brand.primary else colors.content.disabled,
        animationSpec = if (reducedMotion) snap() else tween(durationMillis = motion.stateChangeDurationMillis),
        label = "AuraSliderActiveTrack",
    )
    val inactiveTrackColor by animateColorAsState(
        targetValue = if (enabled) colors.surface.sunken else colors.interaction.disabledContainer,
        animationSpec = if (reducedMotion) snap() else tween(durationMillis = motion.stateChangeDurationMillis),
        label = "AuraSliderInactiveTrack",
    )

    val thumbRadiusPx = with(density) { SliderThumbRadius.toPx() }

    fun updateFromPointerX(pointerX: Float) {
        val usableWidth = (trackWidthPx - thumbRadiusPx * 2f).coerceAtLeast(1f)
        val rawFraction = ((pointerX - thumbRadiusPx) / usableWidth).coerceIn(0f, 1f)
        val updated = fractionToValue(rawFraction, currentValueRange, currentSteps)
        if (updated != currentValue) {
            currentOnValueChange(updated)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = false) {
                if (!enabled) {
                    disabled()
                }
                if (valueLabel != null) {
                    stateDescription = valueLabel
                }
                setProgress { target ->
                    if (!enabled) return@setProgress false
                    val clampedTarget = target.coerceIn(currentValueRange.start, currentValueRange.endInclusive)
                    val snapped = fractionToValue(
                        valueToFraction(clampedTarget, currentValueRange),
                        currentValueRange,
                        currentSteps,
                    )
                    currentOnValueChange(snapped)
                    currentOnFinished?.invoke()
                    true
                }
            },
    ) {
        if (label != null || valueLabel != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (label != null) {
                    AuraText(
                        text = label,
                        style = typography.labelLarge,
                        color = if (enabled) colors.content.primary else colors.content.disabled,
                    )
                } else {
                    Spacer(modifier = Modifier)
                }
                if (valueLabel != null) {
                    AuraText(
                        text = valueLabel,
                        style = typography.monoMedium,
                        color = if (enabled) colors.brand.primary else colors.content.disabled,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(SliderMinTouchHeight)
                .onSizeChanged { trackWidthPx = it.width.toFloat() }
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    detectTapGestures(
                        onPress = { offset ->
                            val press = PressInteraction.Press(offset)
                            resolvedInteractionSource.tryEmit(press)
                            isDragging = true
                            updateFromPointerX(offset.x)
                            val released = tryAwaitRelease()
                            isDragging = false
                            if (released) {
                                resolvedInteractionSource.tryEmit(PressInteraction.Release(press))
                                currentOnFinished?.invoke()
                            } else {
                                resolvedInteractionSource.tryEmit(PressInteraction.Cancel(press))
                            }
                        },
                    )
                }
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    var activePress: PressInteraction.Press? = null
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            val press = PressInteraction.Press(offset)
                            activePress = press
                            resolvedInteractionSource.tryEmit(press)
                            isDragging = true
                            updateFromPointerX(offset.x)
                        },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            updateFromPointerX(change.position.x)
                        },
                        onDragEnd = {
                            isDragging = false
                            activePress?.let { resolvedInteractionSource.tryEmit(PressInteraction.Release(it)) }
                            activePress = null
                            currentOnFinished?.invoke()
                        },
                        onDragCancel = {
                            isDragging = false
                            activePress?.let { resolvedInteractionSource.tryEmit(PressInteraction.Cancel(it)) }
                            activePress = null
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            val haloColor = colors.brand.primary.copy(alpha = if (isDragging && enabled) 0.18f else 0f)
            val thumbFill = if (enabled) colors.surface.default else colors.interaction.disabledContainer
            val thumbRing = if (enabled) colors.brand.primary else colors.content.disabled
            val activeTickColor = colors.brand.onPrimary.copy(alpha = 0.78f)
            val inactiveTickColor = colors.content.tertiary.copy(alpha = 0.65f)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SliderMinTouchHeight),
            ) {
                val trackPx = SliderTrackHeight.toPx()
                val cornerRadius = CornerRadius(trackPx / 2f, trackPx / 2f)
                val startX = thumbRadiusPx
                val endX = (size.width - thumbRadiusPx).coerceAtLeast(startX + 1f)
                val trackSpan = endX - startX
                val centerY = size.height / 2f
                val thumbX = startX + trackSpan * animatedFraction

                // Full inactive track
                drawRoundRect(
                    color = inactiveTrackColor,
                    topLeft = Offset(startX, centerY - trackPx / 2f),
                    size = Size(trackSpan, trackPx),
                    cornerRadius = cornerRadius,
                )

                // Active brand track
                val activeWidth = (thumbX - startX).coerceAtLeast(0f)
                if (activeWidth > 0f) {
                    drawRoundRect(
                        color = activeTrackColor,
                        topLeft = Offset(startX, centerY - trackPx / 2f),
                        size = Size(activeWidth, trackPx),
                        cornerRadius = cornerRadius,
                    )
                }

                // Discrete step ticks
                if (steps > 0) {
                    val intervals = steps + 1
                    val tickRadius = 2.dp.toPx()
                    for (i in 0..intervals) {
                        val frac = i.toFloat() / intervals.toFloat()
                        val tickX = startX + trackSpan * frac
                        val isPassed = frac <= animatedFraction + 0.001f
                        drawCircle(
                            color = if (isPassed) activeTickColor else inactiveTickColor,
                            radius = tickRadius,
                            center = Offset(tickX, centerY),
                        )
                    }
                }

                // Halo + Elevated Thumb
                if (haloColor.alpha > 0f) {
                    drawCircle(
                        color = haloColor,
                        radius = SliderThumbHaloRadius.toPx(),
                        center = Offset(thumbX, centerY),
                    )
                }
                val scaledRadius = thumbRadiusPx * thumbScale
                drawCircle(
                    color = Color.Black.copy(alpha = 0.14f),
                    radius = scaledRadius + 1.dp.toPx(),
                    center = Offset(thumbX, centerY + 1.5.dp.toPx()),
                )
                drawCircle(
                    color = thumbFill,
                    radius = scaledRadius,
                    center = Offset(thumbX, centerY),
                )
                drawCircle(
                    color = thumbRing,
                    radius = scaledRadius - 1.25.dp.toPx(),
                    center = Offset(thumbX, centerY),
                    style = Stroke(width = 2.5.dp.toPx()),
                )
            }
        }

        if (!supportingText.isNullOrBlank()) {
            AuraText(
                text = supportingText,
                style = typography.bodySmall,
                color = if (enabled) colors.content.secondary else colors.content.disabled,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Preview(name = "AuraSlider Light", showBackground = true)
@Composable
private fun AuraSliderLightPreview() {
    AuraTheme(colorMode = AuraColorMode.Light) {
        Column(
            modifier = Modifier
                .background(AuraTheme.colors.background.primary)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AuraSlider(
                value = 0.72f,
                onValueChange = {},
                label = "Blur Intensity",
                valueLabel = "72%",
            )
            AuraSlider(
                value = 4f,
                onValueChange = {},
                valueRange = 1f..5f,
                steps = 3,
                label = "Body Profile",
                valueLabel = "4 / 5",
                supportingText = "Snaps across Light to Full-bodied tasting steps.",
            )
        }
    }
}
