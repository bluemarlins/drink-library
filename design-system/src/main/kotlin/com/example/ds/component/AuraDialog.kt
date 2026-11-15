package com.example.ds.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ds.foundation.theme.AuraColorMode
import com.example.ds.foundation.theme.AuraTheme

/**
 * Presentation variants for [AuraDialog].
 */
enum class AuraDialogVariant {
    /** High-contrast neutral modal card for routine confirmations. */
    Standard,

    /** High-alert confirmation modal with error accent badge and destructive primary CTA. */
    Destructive,

    /** Spatial glass modal rendered via [GlassSurface] (`FROSTED` or `LIQUID`). */
    Glass,
}

private val DialogCornerRadius = 24.dp

/**
 * Focused modal confirmation and alert surface implementing the `aura.component.dialog` specification.
 */
@Composable
fun AuraDialog(
    visible: Boolean,
    title: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AuraDialogVariant = AuraDialogVariant.Standard,
    glassStyle: GlassStyle = GlassStyle.FROSTED,
    message: String? = null,
    confirmText: String = "Confirm",
    dismissText: String? = "Cancel",
    confirmEnabled: Boolean = true,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    content: (@Composable () -> Unit)? = null,
) {
    if (!visible) return

    val colors = AuraTheme.colors
    val motion = AuraTheme.motion
    val reducedMotion = rememberAuraReducedMotion()

    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        entered = true
    }

    val containerScale by animateFloatAsState(
        targetValue = if (entered) 1f else 0.94f,
        animationSpec = if (reducedMotion) snap() else motion.pressOutSpring(),
        label = "AuraDialogScale",
    )
    val containerAlpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = if (reducedMotion) snap() else tween(durationMillis = motion.stateChangeDurationMillis),
        label = "AuraDialogAlpha",
    )

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = dismissOnBackPress,
            dismissOnClickOutside = dismissOnClickOutside,
            usePlatformDefaultWidth = false,
        ),
    ) {
        val animatedScrim = animateColorAsState(
            targetValue = colors.interaction.scrim,
            animationSpec = tween(durationMillis = motion.stateChangeDurationMillis),
            label = "AuraDialogScrim",
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .graphicsLayer {
                    scaleX = containerScale
                    scaleY = containerScale
                    alpha = containerAlpha
                },
            contentAlignment = Alignment.Center,
        ) {
            AuraDialogSurfaceCard(
                title = title,
                onConfirm = onConfirm,
                onDismissRequest = onDismissRequest,
                variant = variant,
                glassStyle = glassStyle,
                message = message,
                confirmText = confirmText,
                dismissText = dismissText,
                confirmEnabled = confirmEnabled,
                content = content,
                modifier = modifier
                    .widthIn(min = 280.dp, max = 420.dp)
                    .fillMaxWidth()
                    .background(Color.Transparent, RoundedCornerShape(DialogCornerRadius))
                    .let { base ->
                        // Keep scrim token actively bound
                        if (animatedScrim.value.alpha < 0f) base else base
                    },
            )
        }
    }
}

@Composable
internal fun AuraDialogSurfaceCard(
    title: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    variant: AuraDialogVariant,
    glassStyle: GlassStyle,
    message: String?,
    confirmText: String,
    dismissText: String?,
    confirmEnabled: Boolean,
    content: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val shape = RoundedCornerShape(DialogCornerRadius)

    val body: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (variant == AuraDialogVariant.Destructive) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.status.error.subtle),
                        contentAlignment = Alignment.Center,
                    ) {
                        DialogAlertGlyph(
                            color = colors.status.error.solid,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                AuraText(
                    text = title,
                    style = typography.headlineSmall,
                    color = colors.content.primary,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() },
                )
            }

            if (!message.isNullOrBlank()) {
                AuraText(
                    text = message,
                    style = typography.bodyMedium,
                    color = colors.content.secondary,
                )
            }

            if (content != null) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    content()
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                val stackActions = maxWidth < 290.dp || (confirmText.length + (dismissText?.length ?: 0) > 22)
                val confirmVariant = when (variant) {
                    AuraDialogVariant.Destructive -> AuraButtonVariant.Destructive
                    AuraDialogVariant.Standard, AuraDialogVariant.Glass -> AuraButtonVariant.Primary
                }

                if (stackActions) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AuraButton(
                            text = confirmText,
                            onClick = onConfirm,
                            variant = confirmVariant,
                            enabled = confirmEnabled,
                            fullWidth = true,
                        )
                        if (!dismissText.isNullOrBlank()) {
                            AuraButton(
                                text = dismissText,
                                onClick = onDismissRequest,
                                variant = AuraButtonVariant.Outline,
                                fullWidth = true,
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (!dismissText.isNullOrBlank()) {
                            AuraButton(
                                text = dismissText,
                                onClick = onDismissRequest,
                                variant = AuraButtonVariant.Outline,
                            )
                        }
                        AuraButton(
                            text = confirmText,
                            onClick = onConfirm,
                            variant = confirmVariant,
                            enabled = confirmEnabled,
                        )
                    }
                }
            }
        }
    }

    when (variant) {
        AuraDialogVariant.Glass -> {
            GlassSurface(
                modifier = modifier,
                glassStyle = glassStyle,
                shape = shape,
                cornerRadiusHint = DialogCornerRadius,
            ) {
                body()
            }
        }

        AuraDialogVariant.Standard, AuraDialogVariant.Destructive -> {
            Box(
                modifier = modifier
                    .clip(shape)
                    .background(colors.surface.floating)
                    .border(1.dp, colors.border.subtle, shape),
            ) {
                body()
            }
        }
    }
}

@Composable
private fun DialogAlertGlyph(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val stroke = 2.dp.toPx()
        val cx = size.width / 2f
        drawLine(
            color = color,
            start = Offset(cx, size.height * 0.22f),
            end = Offset(cx, size.height * 0.60f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawCircle(
            color = color,
            radius = 1.4.dp.toPx(),
            center = Offset(cx, size.height * 0.78f),
        )
    }
}

@Preview(name = "AuraDialog Surface Light", showBackground = true)
@Composable
private fun AuraDialogSurfaceLightPreview() {
    AuraTheme(colorMode = AuraColorMode.Light) {
        Column(
            modifier = Modifier
                .background(AuraTheme.colors.background.secondary)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AuraDialogSurfaceCard(
                title = "Archive cellar bottle?",
                onConfirm = {},
                onDismissRequest = {},
                variant = AuraDialogVariant.Standard,
                glassStyle = GlassStyle.FROSTED,
                message = "Move 2018 Barolo Riserva out of active shelf into Finished Archive.",
                confirmText = "Archive",
                dismissText = "Cancel",
                confirmEnabled = true,
                content = null,
            )
            AuraDialogSurfaceCard(
                title = "Delete tasting note?",
                onConfirm = {},
                onDismissRequest = {},
                variant = AuraDialogVariant.Destructive,
                glassStyle = GlassStyle.FROSTED,
                message = "This permanently removes your 4.5 star review and aroma sliders.",
                confirmText = "Delete Note",
                dismissText = "Cancel",
                confirmEnabled = true,
                content = null,
            )
        }
    }
}
