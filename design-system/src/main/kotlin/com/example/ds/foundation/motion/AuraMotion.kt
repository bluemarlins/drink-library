package com.example.ds.foundation.motion

import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Tier 2 — Aura semantic interaction motion tokens.
 *
 * Defines spring physics, asymmetric press/release durations, and subtle tactile
 * micro-interaction amplitudes for in-place feedback across components.
 */
@Immutable
data class AuraMotion(
    val pressInDampingRatio: Float = 0.85f,
    val pressInStiffness: Float = 800f,
    val pressOutDampingRatio: Float = 0.65f,
    val pressOutStiffness: Float = 500f,
    val iconDampingRatio: Float = 0.75f,
    val iconStiffness: Float = 600f,
    val pressInDurationMillis: Int = 90,
    val pressOutDurationMillis: Int = 180,
    val minPressDurationMillis: Long = 110L,
    val stateChangeDurationMillis: Int = 150,
    val loadingCrossfadeDurationMillis: Int = 120,
    val spinnerRotationDurationMillis: Int = 900,
    val spinnerSweepDurationMillis: Int = 650,
    val pressedScaleDefault: Float = 0.96f,
    val pressedScaleLarge: Float = 0.975f,
    val pressedScaleGhost: Float = 0.95f,
    val leadingIconPressedScale: Float = 0.92f,
    val trailingIconNudge: Dp = 2.dp,
) {
    fun <T> pressInSpring(): SpringSpec<T> = spring(
        dampingRatio = pressInDampingRatio,
        stiffness = pressInStiffness,
    )

    fun <T> pressOutSpring(): SpringSpec<T> = spring(
        dampingRatio = pressOutDampingRatio,
        stiffness = pressOutStiffness,
    )

    fun <T> iconSpring(): SpringSpec<T> = spring(
        dampingRatio = iconDampingRatio,
        stiffness = iconStiffness,
    )
}

object AuraMotionDefaults {
    val Standard = AuraMotion()
}
