package com.example.ds.component

import android.os.SystemClock
import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ds.foundation.color.AuraColors
import com.example.ds.foundation.theme.AuraColorMode
import com.example.ds.foundation.theme.AuraTheme
import com.example.ds.foundation.typography.AuraTypography
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val MinTouchTargetWidth = 48.dp

/**
 * Visual hierarchy variants for [AuraButton].
 */
enum class AuraButtonVariant {
    /** Single highest-emphasis CTA per screen or modal context. */
    Primary,

    /** Tonal accent button for complementary actions or inside cards. */
    Secondary,

    /** Bordered neutral button for cancel or alternative actions. */
    Outline,

    /** Borderless low-emphasis action for toolbars and inline headers. */
    Ghost,

    /** High-alert confirmation button reserved for irreversible destructive actions. */
    Destructive,
}

/**
 * Visual size scale for [AuraButton].
 *
 * Physical touch targets always enforce a minimum of 48dp even for [Small].
 */
enum class AuraButtonSize {
    Small,
    Medium,
    Large,
}

private data class AuraButtonSizeSpec(
    val visualHeight: Dp,
    val minTouchHeight: Dp,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val cornerRadius: Dp,
    val iconSize: Dp,
    val iconGap: Dp,
    val spinnerSize: Dp,
    val spinnerStrokeWidth: Dp,
) {
    fun textStyle(size: AuraButtonSize, typography: AuraTypography): TextStyle = when (size) {
        AuraButtonSize.Small -> typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        AuraButtonSize.Medium -> typography.labelLarge.copy(fontSize = 14.sp, lineHeight = 20.sp)
        AuraButtonSize.Large -> typography.labelLarge.copy(fontSize = 16.sp, lineHeight = 22.sp)
    }
}

private fun AuraButtonSize.toSpec(): AuraButtonSizeSpec = when (this) {
    AuraButtonSize.Small -> AuraButtonSizeSpec(
        visualHeight = 36.dp,
        minTouchHeight = 48.dp,
        horizontalPadding = 12.dp,
        verticalPadding = 6.dp,
        cornerRadius = 8.dp,
        iconSize = 16.dp,
        iconGap = 6.dp,
        spinnerSize = 16.dp,
        spinnerStrokeWidth = 2.dp,
    )
    AuraButtonSize.Medium -> AuraButtonSizeSpec(
        visualHeight = 48.dp,
        minTouchHeight = 48.dp,
        horizontalPadding = 16.dp,
        verticalPadding = 10.dp,
        cornerRadius = 10.dp,
        iconSize = 18.dp,
        iconGap = 8.dp,
        spinnerSize = 18.dp,
        spinnerStrokeWidth = 2.dp,
    )
    AuraButtonSize.Large -> AuraButtonSizeSpec(
        visualHeight = 56.dp,
        minTouchHeight = 56.dp,
        horizontalPadding = 20.dp,
        verticalPadding = 12.dp,
        cornerRadius = 12.dp,
        iconSize = 20.dp,
        iconGap = 8.dp,
        spinnerSize = 20.dp,
        spinnerStrokeWidth = 2.5.dp,
    )
}

private data class AuraButtonResolvedColors(
    val container: Color,
    val content: Color,
    val border: Color?,
)

private fun resolveAuraButtonColors(
    variant: AuraButtonVariant,
    enabled: Boolean,
    isPressed: Boolean,
    loading: Boolean,
    colors: AuraColors,
): AuraButtonResolvedColors {
    // Precedence: Loading > Disabled > Pressed > Rest
    if (!enabled && !loading) {
        val container = when (variant) {
            AuraButtonVariant.Outline,
            AuraButtonVariant.Ghost,
            -> Color.Transparent

            AuraButtonVariant.Primary,
            AuraButtonVariant.Secondary,
            AuraButtonVariant.Destructive,
            -> colors.interaction.disabledContainer
        }
        val border = if (variant == AuraButtonVariant.Outline) colors.border.subtle else null
        return AuraButtonResolvedColors(
            container = container,
            content = colors.content.disabled,
            border = border,
        )
    }

    val pressedActive = isPressed && !loading
    return when (variant) {
        AuraButtonVariant.Primary -> AuraButtonResolvedColors(
            container = if (pressedActive) colors.brand.primaryPressed else colors.brand.primary,
            content = colors.brand.onPrimary,
            border = null,
        )

        AuraButtonVariant.Secondary -> AuraButtonResolvedColors(
            container = if (pressedActive) {
                colors.interaction.pressedOverlay.compositeOver(colors.brand.subtle)
            } else {
                colors.brand.subtle
            },
            content = colors.brand.onSubtle,
            border = null,
        )

        AuraButtonVariant.Outline -> AuraButtonResolvedColors(
            container = if (pressedActive) colors.interaction.pressedOverlay else Color.Transparent,
            content = colors.content.primary,
            border = if (pressedActive) colors.border.strong else colors.border.default,
        )

        AuraButtonVariant.Ghost -> AuraButtonResolvedColors(
            container = if (pressedActive) colors.interaction.pressedOverlay else Color.Transparent,
            content = colors.brand.primary,
            border = null,
        )

        AuraButtonVariant.Destructive -> AuraButtonResolvedColors(
            container = if (pressedActive) {
                colors.interaction.pressedOverlay.compositeOver(colors.status.error.solid)
            } else {
                colors.status.error.solid
            },
            content = colors.status.error.onSolid,
            border = null,
        )
    }
}

@Composable
private fun rememberTactilePressedState(
    interactionSource: InteractionSource,
    minPressDurationMillis: Long,
): State<Boolean> {
    val isVisuallyPressed = remember(interactionSource) { mutableStateOf(false) }
    LaunchedEffect(interactionSource, minPressDurationMillis) {
        val activePresses = mutableListOf<PressInteraction.Press>()
        var pressStartUptimeMs = 0L
        var releaseJob: Job? = null

        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    releaseJob?.cancel()
                    releaseJob = null
                    if (activePresses.isEmpty()) {
                        pressStartUptimeMs = SystemClock.uptimeMillis()
                    }
                    activePresses.add(interaction)
                    isVisuallyPressed.value = true
                }

                is PressInteraction.Release -> {
                    activePresses.remove(interaction.press)
                    if (activePresses.isEmpty()) {
                        val elapsedMs = SystemClock.uptimeMillis() - pressStartUptimeMs
                        val remainingMs = minPressDurationMillis - elapsedMs
                        if (remainingMs > 0L) {
                            releaseJob?.cancel()
                            releaseJob = launch {
                                delay(remainingMs)
                                isVisuallyPressed.value = false
                            }
                        } else {
                            isVisuallyPressed.value = false
                        }
                    }
                }

                is PressInteraction.Cancel -> {
                    activePresses.remove(interaction.press)
                    if (activePresses.isEmpty()) {
                        releaseJob?.cancel()
                        releaseJob = null
                        isVisuallyPressed.value = false
                    }
                }
            }
        }
    }
    return isVisuallyPressed
}

@Composable
private fun rememberReducedMotion(): Boolean {
    if (LocalInspectionMode.current) return false
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f,
            ) == 0f
        }.getOrDefault(false)
    }
}

/**
 * Labeled action button implementing the `aura.component.button` specification.
 *
 * Uses Aura's tactile spring compression and asymmetric color interpolation bound to
 * [AuraTheme.colors] and [AuraTheme.motion] with zero `material3` dependency.
 *
 * @param text Action verb label displayed inside the button.
 * @param onClick Callback invoked when clicked (throttled by [debounceIntervalMs]).
 * @param modifier Modifier applied to the outer touch-target container.
 * @param variant Visual hierarchy variant (`Primary`, `Secondary`, `Outline`, `Ghost`, `Destructive`).
 * @param size Visual height and typography scale (`Small`, `Medium`, `Large`).
 * @param enabled Controls whether the button accepts user input.
 * @param loading Displays a centered breathing spinner while preserving button width and blocking clicks.
 * @param fullWidth Expands the button to fill available horizontal width.
 * @param debounceIntervalMs Minimum interval in milliseconds between accepted clicks (`0L` disables).
 * @param leadingIcon Optional icon composable rendered before [text].
 * @param trailingIcon Optional icon composable rendered after [text].
 */
@Composable
fun AuraButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AuraButtonVariant = AuraButtonVariant.Primary,
    size: AuraButtonSize = AuraButtonSize.Medium,
    enabled: Boolean = true,
    loading: Boolean = false,
    fullWidth: Boolean = false,
    debounceIntervalMs: Long = 400L,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val colors = AuraTheme.colors
    val motion = AuraTheme.motion
    val typography = AuraTheme.typography
    val reducedMotion = rememberReducedMotion()
    val sizeSpec = size.toSpec()
    val labelTextStyle = sizeSpec.textStyle(size, typography)
    val shape = RoundedCornerShape(sizeSpec.cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by rememberTactilePressedState(interactionSource, motion.minPressDurationMillis)
    val pressedActive = enabled && !loading && isPressed

    val resolvedColors = resolveAuraButtonColors(
        variant = variant,
        enabled = enabled,
        isPressed = isPressed,
        loading = loading,
        colors = colors,
    )

    val targetScale = when {
        reducedMotion || !pressedActive -> 1f
        variant == AuraButtonVariant.Ghost -> motion.pressedScaleGhost
        size == AuraButtonSize.Large || fullWidth -> motion.pressedScaleLarge
        else -> motion.pressedScaleDefault
    }
    val buttonScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = if (pressedActive) motion.pressInSpring() else motion.pressOutSpring(),
        label = "AuraButtonScale",
    )

    val colorDuration = when {
        pressedActive -> motion.pressInDurationMillis
        !enabled && !loading -> motion.stateChangeDurationMillis
        else -> motion.pressOutDurationMillis
    }
    val colorSpec = tween<Color>(
        durationMillis = colorDuration,
        easing = FastOutSlowInEasing,
    )
    val containerColor by animateColorAsState(
        targetValue = resolvedColors.container,
        animationSpec = colorSpec,
        label = "AuraButtonContainerColor",
    )
    val contentColor by animateColorAsState(
        targetValue = resolvedColors.content,
        animationSpec = colorSpec,
        label = "AuraButtonContentColor",
    )
    val borderColor by animateColorAsState(
        targetValue = resolvedColors.border ?: Color.Transparent,
        animationSpec = colorSpec,
        label = "AuraButtonBorderColor",
    )

    val leadingIconScale by animateFloatAsState(
        targetValue = if (pressedActive && !reducedMotion) motion.leadingIconPressedScale else 1f,
        animationSpec = motion.iconSpring(),
        label = "AuraButtonLeadingIconScale",
    )
    val trailingIconOffsetX by animateDpAsState(
        targetValue = if (pressedActive && !reducedMotion) motion.trailingIconNudge else 0.dp,
        animationSpec = motion.iconSpring(),
        label = "AuraButtonTrailingIconOffset",
    )

    val crossfadeSpec = tween<Float>(
        durationMillis = motion.loadingCrossfadeDurationMillis,
        easing = FastOutSlowInEasing,
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (loading) 0f else 1f,
        animationSpec = crossfadeSpec,
        label = "AuraButtonContentAlpha",
    )
    val contentScale by animateFloatAsState(
        targetValue = if (loading && !reducedMotion) 0.92f else 1f,
        animationSpec = motion.iconSpring(),
        label = "AuraButtonContentScale",
    )
    val spinnerAlpha by animateFloatAsState(
        targetValue = if (loading) 1f else 0f,
        animationSpec = crossfadeSpec,
        label = "AuraButtonSpinnerAlpha",
    )
    val spinnerScale by animateFloatAsState(
        targetValue = if (loading || reducedMotion) 1f else 0.75f,
        animationSpec = motion.iconSpring(),
        label = "AuraButtonSpinnerScale",
    )

    var lastClickUptimeMs by remember { mutableLongStateOf(-1L) }
    val debouncedOnClick: () -> Unit = {
        if (debounceIntervalMs <= 0L) {
            onClick()
        } else {
            val now = SystemClock.uptimeMillis()
            if (lastClickUptimeMs < 0L || now - lastClickUptimeMs >= debounceIntervalMs) {
                lastClickUptimeMs = now
                onClick()
            }
        }
    }

    val widthModifier = if (fullWidth) Modifier.fillMaxWidth() else Modifier

    Box(
        modifier = modifier
            .then(widthModifier)
            .defaultMinSize(minWidth = MinTouchTargetWidth, minHeight = sizeSpec.minTouchHeight)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !loading,
                role = Role.Button,
                onClick = debouncedOnClick,
            )
            .semantics {
                role = Role.Button
                if (loading) {
                    stateDescription = "In progress"
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        val borderModifier = if (variant == AuraButtonVariant.Outline) {
            Modifier.border(width = 1.dp, color = borderColor, shape = shape)
        } else {
            Modifier
        }

        Box(
            modifier = Modifier
                .then(widthModifier)
                .heightIn(min = sizeSpec.visualHeight)
                .graphicsLayer {
                    scaleX = buttonScale
                    scaleY = buttonScale
                }
                .clip(shape)
                .background(color = containerColor, shape = shape)
                .then(borderModifier)
                .padding(
                    horizontal = sizeSpec.horizontalPadding,
                    vertical = sizeSpec.verticalPadding,
                ),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalAuraContentColor provides contentColor) {
                Row(
                    // Hide content visually while loading so measured width stays fixed.
                    modifier = Modifier.graphicsLayer {
                        alpha = contentAlpha
                        scaleX = contentScale
                        scaleY = contentScale
                    },
                    horizontalArrangement = Arrangement.spacedBy(sizeSpec.iconGap, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (leadingIcon != null) {
                        Box(
                            modifier = Modifier
                                .size(sizeSpec.iconSize)
                                .graphicsLayer {
                                    scaleX = leadingIconScale
                                    scaleY = leadingIconScale
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            leadingIcon()
                        }
                    }
                    AuraText(
                        text = text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = contentColor,
                        style = labelTextStyle,
                    )
                    if (trailingIcon != null) {
                        Box(
                            modifier = Modifier
                                .size(sizeSpec.iconSize)
                                .graphicsLayer {
                                    translationX = trailingIconOffsetX.toPx()
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            trailingIcon()
                        }
                    }
                }
            }

            if (loading || spinnerAlpha > 0f) {
                AuraLoadingSpinner(
                    color = contentColor,
                    size = sizeSpec.spinnerSize,
                    strokeWidth = sizeSpec.spinnerStrokeWidth,
                    rotationDurationMillis = motion.spinnerRotationDurationMillis,
                    sweepDurationMillis = motion.spinnerSweepDurationMillis,
                    modifier = Modifier.graphicsLayer {
                        alpha = spinnerAlpha
                        scaleX = spinnerScale
                        scaleY = spinnerScale
                    },
                )
            }
        }
    }
}

@Composable
private fun AuraLoadingSpinner(
    color: Color,
    size: Dp,
    strokeWidth: Dp,
    rotationDurationMillis: Int,
    sweepDurationMillis: Int,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "AuraButtonSpinner")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = rotationDurationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "SpinnerRotation",
    )
    val sweepAngle by transition.animateFloat(
        initialValue = 45f,
        targetValue = 240f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = sweepDurationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "SpinnerSweep",
    )

    Canvas(modifier = modifier.size(size)) {
        val strokePx = strokeWidth.toPx()
        val inset = strokePx / 2f
        val arcSize = Size(
            width = this.size.width - strokePx,
            height = this.size.height - strokePx,
        )
        drawCircle(
            color = color.copy(alpha = 0.24f),
            radius = (this.size.minDimension - strokePx) / 2f,
            style = Stroke(width = strokePx),
        )
        drawArc(
            color = color,
            startAngle = angle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(width = strokePx, cap = StrokeCap.Round),
        )
    }
}

@Preview(name = "Light Variants", showBackground = true)
@Composable
private fun AuraButtonLightPreview() {
    AuraTheme(colorMode = AuraColorMode.Light) {
        Column(
            modifier = Modifier
                .background(AuraTheme.colors.background.primary)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AuraButton(text = "Primary", onClick = {}, variant = AuraButtonVariant.Primary)
            AuraButton(text = "Secondary", onClick = {}, variant = AuraButtonVariant.Secondary)
            AuraButton(text = "Outline", onClick = {}, variant = AuraButtonVariant.Outline)
            AuraButton(text = "Ghost", onClick = {}, variant = AuraButtonVariant.Ghost)
            AuraButton(text = "Destructive", onClick = {}, variant = AuraButtonVariant.Destructive)
            AuraButton(text = "Loading", onClick = {}, loading = true)
            AuraButton(text = "Disabled", onClick = {}, enabled = false)
        }
    }
}

@Preview(name = "Dark Variants", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun AuraButtonDarkPreview() {
    AuraTheme(colorMode = AuraColorMode.Dark) {
        Column(
            modifier = Modifier
                .background(AuraTheme.colors.background.primary)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AuraButton(text = "Primary", onClick = {}, variant = AuraButtonVariant.Primary)
            AuraButton(text = "Secondary", onClick = {}, variant = AuraButtonVariant.Secondary)
            AuraButton(text = "Outline", onClick = {}, variant = AuraButtonVariant.Outline)
            AuraButton(text = "Ghost", onClick = {}, variant = AuraButtonVariant.Ghost)
            AuraButton(text = "Destructive", onClick = {}, variant = AuraButtonVariant.Destructive)
            AuraButton(text = "Loading", onClick = {}, loading = true)
            AuraButton(text = "Disabled", onClick = {}, enabled = false)
        }
    }
}
