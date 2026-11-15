package com.example.ds.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ds.foundation.theme.AuraColorMode
import com.example.ds.foundation.theme.AuraTheme
import kotlin.math.roundToLong

private val NumberFieldCornerRadius = 10.dp
private val NumberFieldMinHeight = 48.dp

internal fun sanitizeNumericText(
    raw: String,
    allowDecimals: Boolean,
    allowNegative: Boolean,
): String {
    if (raw.isEmpty()) return ""
    val sb = StringBuilder()
    var hasDot = false
    raw.forEachIndexed { index, ch ->
        when {
            ch.isDigit() -> sb.append(ch)
            ch == '-' && allowNegative && index == 0 -> sb.append('-')
            ch == '.' && allowDecimals && !hasDot -> {
                hasDot = true
                if (sb.isEmpty() || sb.toString() == "-") sb.append('0')
                sb.append('.')
            }
        }
    }
    return sb.toString()
}

internal fun stepNumericValue(
    current: String,
    delta: Double,
    minValue: Double?,
    maxValue: Double?,
    allowDecimals: Boolean,
): String {
    val parsed = current.toDoubleOrNull() ?: 0.0
    var next = parsed + delta
    if (minValue != null && next < minValue) next = minValue
    if (maxValue != null && next > maxValue) next = maxValue

    return if (!allowDecimals) {
        next.roundToLong().toString()
    } else if (next % 1.0 == 0.0) {
        next.roundToLong().toString()
    } else {
        val roundedHundredths = (next * 100.0).roundToLong() / 100.0
        roundedHundredths.toString()
    }
}

private class ThousandsSeparatorTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val isNeg = raw.startsWith("-")
        val body = if (isNeg) raw.substring(1) else raw
        val dotIndex = body.indexOf('.')
        val intPart = if (dotIndex >= 0) body.substring(0, dotIndex) else body
        val decPart = if (dotIndex >= 0) body.substring(dotIndex) else ""

        if (intPart.length <= 3) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val formattedInt = StringBuilder()
        val origToTrans = IntArray(raw.length + 1)
        val prefixOffset = if (isNeg) 1 else 0
        if (isNeg) {
            formattedInt.append('-')
            origToTrans[0] = 0
            origToTrans[1] = 1
        } else {
            origToTrans[0] = 0
        }

        for (i in intPart.indices) {
            val remaining = intPart.length - i
            if (i > 0 && remaining % 3 == 0) {
                formattedInt.append(',')
            }
            formattedInt.append(intPart[i])
            origToTrans[prefixOffset + i + 1] = formattedInt.length
        }

        val fullFormatted = formattedInt.toString() + decPart
        val decStartOrig = prefixOffset + intPart.length
        for (j in decPart.indices) {
            origToTrans[decStartOrig + j + 1] = formattedInt.length + j + 1
        }

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val idx = offset.coerceIn(0, origToTrans.lastIndex)
                return origToTrans[idx].coerceIn(0, fullFormatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, fullFormatted.length)
                var commasBefore = 0
                for (i in 0 until clamped) {
                    if (fullFormatted[i] == ',') commasBefore++
                }
                return (clamped - commasBefore).coerceIn(0, raw.length)
            }
        }
        return TransformedText(AnnotatedString(fullFormatted), mapping)
    }
}

/**
 * Formatted numeric, currency, and measurement input field implementing `aura.component.number-field`.
 */
@Composable
fun AuraNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    variant: AuraTextFieldVariant = AuraTextFieldVariant.Outlined,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: String? = null,
    placeholder: String? = null,
    prefixText: String? = null,
    suffixText: String? = null,
    supportingText: String? = null,
    errorMessage: String? = null,
    showSteppers: Boolean = false,
    step: Double = 1.0,
    minValue: Double? = null,
    maxValue: Double? = null,
    allowDecimals: Boolean = true,
    formatThousands: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val motion = AuraTheme.motion
    val reducedMotion = rememberAuraReducedMotion()
    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isFocused by resolvedInteractionSource.collectIsFocusedAsState()
    val isError = !errorMessage.isNullOrBlank()
    val allowNegative = minValue == null || minValue < 0.0

    val activeFocus = enabled && !readOnly && isFocused
    val containerColor = when (variant) {
        AuraTextFieldVariant.Outlined -> if (!enabled) colors.interaction.disabledContainer else colors.surface.default
        AuraTextFieldVariant.Filled -> if (!enabled) colors.interaction.disabledContainer else colors.surface.sunken
        AuraTextFieldVariant.Paperless -> Color.Transparent
    }
    // Precedence: Disabled > ReadOnly > Error > Focused > Rest
    val targetBorderColor = when {
        !enabled || readOnly -> colors.border.subtle
        isError -> colors.status.error.solid
        isFocused -> colors.border.focus
        variant == AuraTextFieldVariant.Filled -> colors.border.subtle
        else -> colors.border.default
    }
    val targetBorderWidth = when (variant) {
        AuraTextFieldVariant.Outlined -> if (activeFocus) 2.dp else 1.dp
        AuraTextFieldVariant.Filled, AuraTextFieldVariant.Paperless -> if (activeFocus || isError) 2.dp else 1.dp
    }
    val targetLabelColor = when {
        !enabled -> colors.content.disabled
        isError -> colors.status.error.solid
        activeFocus -> colors.brand.primary
        else -> colors.content.secondary
    }

    val durationMillis = if (isFocused) motion.pressInDurationMillis else motion.stateChangeDurationMillis
    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = if (reducedMotion) snap() else tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
        label = "AuraNumberFieldBorderColor",
    )
    val animatedLabelColor by animateColorAsState(
        targetValue = targetLabelColor,
        animationSpec = if (reducedMotion) snap() else tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
        label = "AuraNumberFieldLabelColor",
    )
    val animatedBorderWidth by animateDpAsState(
        targetValue = targetBorderWidth,
        animationSpec = if (reducedMotion) snap() else tween(durationMillis = durationMillis),
        label = "AuraNumberFieldBorderWidth",
    )

    val numericParsed = value.toDoubleOrNull()
    val canDecrease = enabled && !readOnly && (minValue == null || (numericParsed ?: 0.0) > minValue)
    val canIncrease = enabled && !readOnly && (maxValue == null || (numericParsed ?: 0.0) < maxValue)

    val footerText = errorMessage?.takeIf { it.isNotBlank() } ?: supportingText
    val footerColor = when {
        !enabled -> colors.content.disabled
        isError -> colors.status.error.solid
        else -> colors.content.secondary
    }

    val shape = RoundedCornerShape(NumberFieldCornerRadius)
    val boxModifier = when (variant) {
        AuraTextFieldVariant.Outlined, AuraTextFieldVariant.Filled -> {
            Modifier
                .fillMaxWidth()
                .heightIn(min = NumberFieldMinHeight)
                .clip(shape)
                .background(containerColor)
                .border(animatedBorderWidth, animatedBorderColor, shape)
        }
        AuraTextFieldVariant.Paperless -> {
            Modifier
                .fillMaxWidth()
                .heightIn(min = NumberFieldMinHeight)
                .drawBehind {
                    val strokePx = animatedBorderWidth.toPx()
                    val y = size.height - strokePx / 2f
                    drawLine(
                        color = animatedBorderColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokePx,
                    )
                }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = false) {
                if (!errorMessage.isNullOrBlank()) {
                    error(errorMessage)
                }
            },
    ) {
        if (!label.isNullOrBlank()) {
            AuraText(
                text = label,
                style = typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = animatedLabelColor,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }

        Row(
            modifier = boxModifier.padding(horizontal = if (showSteppers) 4.dp else 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (showSteppers) {
                NumberStepperButton(
                    symbol = "−",
                    contentDescription = "Decrease value",
                    enabled = canDecrease,
                    onClick = {
                        onValueChange(
                            stepNumericValue(
                                current = value,
                                delta = -step,
                                minValue = minValue,
                                maxValue = maxValue,
                                allowDecimals = allowDecimals,
                            ),
                        )
                    },
                )
            }

            if (!prefixText.isNullOrBlank()) {
                AuraText(
                    text = prefixText,
                    style = typography.labelMedium,
                    color = if (enabled) colors.content.secondary else colors.content.disabled,
                )
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isEmpty() && !placeholder.isNullOrBlank()) {
                    AuraText(
                        text = placeholder,
                        style = typography.monoMedium,
                        color = if (enabled) colors.content.tertiary else colors.content.disabled,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = { incoming ->
                        val sanitized = sanitizeNumericText(incoming, allowDecimals, allowNegative)
                        onValueChange(sanitized)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    readOnly = readOnly,
                    singleLine = true,
                    textStyle = typography.monoMedium.copy(
                        color = if (enabled) colors.content.primary else colors.content.disabled,
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (allowDecimals) KeyboardType.Decimal else KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    visualTransformation = if (formatThousands) {
                        remember { ThousandsSeparatorTransformation() }
                    } else {
                        VisualTransformation.None
                    },
                    cursorBrush = SolidColor(
                        if (!enabled || readOnly) Color.Transparent
                        else if (isError) colors.status.error.solid
                        else colors.brand.primary,
                    ),
                    interactionSource = resolvedInteractionSource,
                )
            }

            if (!suffixText.isNullOrBlank()) {
                AuraText(
                    text = suffixText,
                    style = typography.labelMedium,
                    color = if (enabled) colors.content.secondary else colors.content.disabled,
                )
            }

            if (showSteppers) {
                NumberStepperButton(
                    symbol = "+",
                    contentDescription = "Increase value",
                    enabled = canIncrease,
                    onClick = {
                        onValueChange(
                            stepNumericValue(
                                current = value,
                                delta = step,
                                minValue = minValue,
                                maxValue = maxValue,
                                allowDecimals = allowDecimals,
                            ),
                        )
                    },
                )
            }
        }

        if (!footerText.isNullOrBlank()) {
            AuraText(
                text = footerText,
                style = typography.bodySmall,
                color = footerColor,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun NumberStepperButton(
    symbol: String,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val motion = AuraTheme.motion
    val reducedMotion = rememberAuraReducedMotion()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by rememberAuraTactilePressed(
        interactionSource = interactionSource,
        minPressDurationMillis = motion.minPressDurationMillis,
    )
    val scale by animateFloatAsState(
        targetValue = if (!reducedMotion && enabled && isPressed) motion.pressedScaleDefault else 1f,
        animationSpec = when {
            reducedMotion -> snap()
            isPressed -> motion.pressInSpring()
            else -> motion.pressOutSpring()
        },
        label = "NumberStepperScale",
    )

    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 40.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(8.dp))
            .background(if (enabled) colors.surface.sunken else colors.interaction.disabledContainer)
            .border(1.dp, colors.border.subtle, RoundedCornerShape(8.dp))
            .semantics { this.contentDescription = contentDescription }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        AuraText(
            text = symbol,
            style = typography.titleMedium,
            color = if (enabled) colors.content.primary else colors.content.disabled,
        )
    }
}

@Preview(name = "AuraNumberField Light", showBackground = true)
@Composable
private fun AuraNumberFieldLightPreview() {
    AuraTheme(colorMode = AuraColorMode.Light) {
        Column(
            modifier = Modifier
                .background(AuraTheme.colors.background.primary)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AuraNumberField(
                value = "15000000",
                onValueChange = {},
                label = "Portfolio Allocation",
                prefixText = "₩",
                suffixText = "원",
                formatThousands = true,
            )
            AuraNumberField(
                value = "2018",
                onValueChange = {},
                label = "Vintage Year",
                showSteppers = true,
                allowDecimals = false,
                minValue = 1950.0,
                maxValue = 2030.0,
            )
        }
    }
}
