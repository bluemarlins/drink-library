package com.example.ds.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ds.foundation.theme.AuraColorMode
import com.example.ds.foundation.theme.AuraTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Star icon size scale for [AuraRatingBar].
 */
enum class AuraRatingBarSize {
    Small,
    Medium,
    Large,
}

private data class AuraRatingSizeSpec(
    val starSize: Dp,
    val starSpacing: Dp,
)

private fun resolveRatingSizeSpec(size: AuraRatingBarSize): AuraRatingSizeSpec = when (size) {
    AuraRatingBarSize.Small -> AuraRatingSizeSpec(starSize = 18.dp, starSpacing = 4.dp)
    AuraRatingBarSize.Medium -> AuraRatingSizeSpec(starSize = 28.dp, starSpacing = 6.dp)
    AuraRatingBarSize.Large -> AuraRatingSizeSpec(starSize = 36.dp, starSpacing = 8.dp)
}

private fun buildStarPath(
    centerX: Float,
    centerY: Float,
    outerRadius: Float,
    innerRadius: Float,
): Path {
    val path = Path()
    val startAngle = -PI / 2.0
    val stepAngle = PI / 5.0
    for (i in 0 until 10) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val angle = startAngle + i * stepAngle
        val x = centerX + (cos(angle) * radius).toFloat()
        val y = centerY + (sin(angle) * radius).toFloat()
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
    return path
}

internal fun snapRatingValue(
    raw: Float,
    maxStars: Int,
    allowHalfRating: Boolean,
): Float {
    val clamped = raw.coerceIn(0f, maxStars.toFloat())
    return if (allowHalfRating) {
        (clamped * 2f).roundToInt().toFloat() / 2f
    } else {
        clamped.roundToInt().toFloat()
    }
}

internal fun formatRatingReadout(rating: Float): String {
    val roundedHalf = (rating * 2f).roundToInt() / 2f
    return if (roundedHalf % 1f == 0f) {
        "${roundedHalf.toInt()}.0"
    } else {
        roundedHalf.toString()
    }
}

/**
 * Interactive or read-only star & half-star sensory rating control implementing `aura.component.rating-bar`.
 */
@Composable
fun AuraRatingBar(
    rating: Float,
    onRatingChange: ((Float) -> Unit)?,
    modifier: Modifier = Modifier,
    maxStars: Int = 5,
    allowHalfRating: Boolean = true,
    size: AuraRatingBarSize = AuraRatingBarSize.Medium,
    enabled: Boolean = true,
    label: String? = null,
    showValueText: Boolean = true,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val motion = AuraTheme.motion
    val reducedMotion = rememberAuraReducedMotion()
    val density = LocalDensity.current

    val isInteractive = enabled && onRatingChange != null
    val sizeSpec = resolveRatingSizeSpec(size)
    val starSizePx = with(density) { sizeSpec.starSize.toPx() }
    val starSpacingPx = with(density) { sizeSpec.starSpacing.toPx() }
    val safeMaxStars = maxStars.coerceAtLeast(1)
    val clampedRating = snapRatingValue(rating, safeMaxStars, allowHalfRating)
    val totalRowWidth = sizeSpec.starSize * safeMaxStars + sizeSpec.starSpacing * (safeMaxStars - 1)
    val touchHeight = if (isInteractive) 48.dp else maxOf(22.dp, sizeSpec.starSize + 4.dp)

    var isScrubbing by remember { mutableStateOf(false) }
    val currentOnChange by rememberUpdatedState(onRatingChange)
    val currentAllowHalf by rememberUpdatedState(allowHalfRating)
    val currentMaxStars by rememberUpdatedState(safeMaxStars)

    val filledStarColor by animateColorAsState(
        targetValue = if (enabled) colors.brand.primary else colors.content.disabled,
        animationSpec = if (reducedMotion) snap() else tween(durationMillis = motion.stateChangeDurationMillis),
        label = "AuraRatingFilledColor",
    )
    val emptyStarColor = colors.surface.sunken
    val starOutlineColor = if (enabled) colors.border.strong else colors.border.subtle
    val activeScale by animateFloatAsState(
        targetValue = if (isScrubbing && isInteractive && !reducedMotion) 1.12f else 1f,
        animationSpec = if (reducedMotion) snap() else motion.iconSpring(),
        label = "AuraRatingActiveScale",
    )

    fun computeStarValueAt(pointerX: Float, isTap: Boolean): Float {
        val stepPx = starSizePx + starSpacingPx
        val starIndex = (pointerX / stepPx).toInt().coerceIn(0, currentMaxStars - 1)
        val localX = (pointerX - starIndex * stepPx).coerceIn(0f, starSizePx)
        val fractionInsideStar = localX / starSizePx
        return if (currentAllowHalf) {
            if (isTap && pointerX < starSizePx * 0.2f && clampedRating == 0.5f) {
                0f
            } else if (fractionInsideStar <= 0.5f) {
                starIndex + 0.5f
            } else {
                (starIndex + 1).toFloat()
            }
        } else {
            if (isTap && pointerX < starSizePx * 0.2f && clampedRating == 1f) {
                0f
            } else {
                (starIndex + 1).toFloat()
            }
        }
    }

    Column(
        modifier = modifier.semantics(mergeDescendants = false) {
            if (!enabled) disabled()
            stateDescription = "${formatRatingReadout(clampedRating)} out of $safeMaxStars stars"
            if (isInteractive) {
                setProgress { target ->
                    val next = snapRatingValue(target, currentMaxStars, currentAllowHalf)
                    currentOnChange?.invoke(next)
                    true
                }
            }
        },
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (!label.isNullOrBlank()) {
            AuraText(
                text = label,
                style = typography.labelLarge,
                color = if (enabled) colors.content.primary else colors.content.disabled,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(totalRowWidth)
                    .height(touchHeight)
                    .then(
                        if (isInteractive) {
                            Modifier
                                .pointerInput(enabled, safeMaxStars, allowHalfRating) {
                                    detectTapGestures(
                                        onTap = { offset ->
                                            val next = computeStarValueAt(offset.x, isTap = true)
                                            currentOnChange?.invoke(next)
                                        },
                                    )
                                }
                                .pointerInput(enabled, safeMaxStars, allowHalfRating) {
                                    detectHorizontalDragGestures(
                                        onDragStart = { offset ->
                                            isScrubbing = true
                                            currentOnChange?.invoke(computeStarValueAt(offset.x, isTap = false))
                                        },
                                        onHorizontalDrag = { change, _ ->
                                            change.consume()
                                            currentOnChange?.invoke(computeStarValueAt(change.position.x, isTap = false))
                                        },
                                        onDragEnd = { isScrubbing = false },
                                        onDragCancel = { isScrubbing = false },
                                    )
                                }
                        } else {
                            Modifier
                        },
                    ),
                contentAlignment = Alignment.CenterStart,
            ) {
                Canvas(
                    modifier = Modifier
                        .width(totalRowWidth)
                        .height(sizeSpec.starSize),
                ) {
                    val outerRadius = starSizePx * 0.48f
                    val innerRadius = outerRadius * 0.44f
                    val centerY = this.size.height / 2f
                    val activeIndex = floor(clampedRating - 0.01f).toInt()

                    for (index in 0 until safeMaxStars) {
                        val centerX = index * (starSizePx + starSpacingPx) + starSizePx / 2f
                        val starPath = buildStarPath(centerX, centerY, outerRadius, innerRadius)
                        val fillRatio = (clampedRating - index).coerceIn(0f, 1f)
                        val starScale = if (index == activeIndex) activeScale else 1f

                        scale(
                            scale = starScale,
                            pivot = androidx.compose.ui.geometry.Offset(centerX, centerY),
                        ) {
                            drawPath(
                                path = starPath,
                                color = emptyStarColor,
                            )
                            if (fillRatio >= 0.99f) {
                                drawPath(
                                    path = starPath,
                                    color = filledStarColor,
                                )
                            } else if (fillRatio >= 0.25f) {
                                val splitX = centerX
                                clipRect(
                                    left = centerX - starSizePx / 2f,
                                    top = 0f,
                                    right = splitX,
                                    bottom = this.size.height,
                                ) {
                                    drawPath(
                                        path = starPath,
                                        color = filledStarColor,
                                    )
                                }
                            }
                            drawPath(
                                path = starPath,
                                color = if (fillRatio > 0f) filledStarColor else starOutlineColor,
                                style = Stroke(width = 1.4.dp.toPx(), join = StrokeJoin.Round),
                            )
                        }
                    }
                }
            }

            if (showValueText) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (enabled) colors.brand.subtle else colors.interaction.disabledContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    AuraText(
                        text = "${formatRatingReadout(clampedRating)} / $safeMaxStars",
                        style = typography.monoMedium,
                        color = if (enabled) colors.brand.onSubtle else colors.content.disabled,
                    )
                }
            }
        }
    }
}

@Preview(name = "AuraRatingBar Light", showBackground = true)
@Composable
private fun AuraRatingBarLightPreview() {
    AuraTheme(colorMode = AuraColorMode.Light) {
        Column(
            modifier = Modifier
                .background(AuraTheme.colors.background.primary)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AuraRatingBar(
                rating = 4.5f,
                onRatingChange = {},
                label = "Overall Enjoyment",
                size = AuraRatingBarSize.Large,
            )
            AuraRatingBar(
                rating = 3.5f,
                onRatingChange = null,
                size = AuraRatingBarSize.Small,
            )
        }
    }
}
