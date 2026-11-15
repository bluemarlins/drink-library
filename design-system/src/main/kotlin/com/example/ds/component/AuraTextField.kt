package com.example.ds.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
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

private val FieldCornerRadius = 10.dp
private val FieldMinHeight = 48.dp

/**
 * Container presentation variants for [AuraTextField].
 */
enum class AuraTextFieldVariant {
    /** Crisp bordered field suited for structured forms and financial inputs. */
    Outlined,

    /** Recessed container fill suited for dense metadata inside raised surfaces. */
    Filled,

    /** Minimal bottom-ruled surface designed for calm diary and tasting-note writing. */
    Paperless,
}

private data class AuraTextFieldResolvedColors(
    val container: Color,
    val border: Color,
    val borderWidth: Dp,
    val label: Color,
    val text: Color,
    val placeholder: Color,
    val affix: Color,
    val footer: Color,
    val counter: Color,
    val cursor: Color,
)

private fun resolveAuraTextFieldColors(
    variant: AuraTextFieldVariant,
    enabled: Boolean,
    readOnly: Boolean,
    isError: Boolean,
    isFocused: Boolean,
    colors: AuraColors,
): AuraTextFieldResolvedColors {
    val activeFocus = enabled && !readOnly && isFocused
    val activeError = enabled && !readOnly && isError

    val container = when (variant) {
        AuraTextFieldVariant.Outlined -> if (!enabled) colors.interaction.disabledContainer else colors.surface.default
        AuraTextFieldVariant.Filled -> if (!enabled) colors.interaction.disabledContainer else colors.surface.sunken
        AuraTextFieldVariant.Paperless -> Color.Transparent
    }

    // Precedence: Disabled > ReadOnly > Error > Focused > Rest
    val border = when {
        !enabled -> colors.border.subtle
        readOnly -> colors.border.subtle
        isError -> colors.status.error.solid
        isFocused -> colors.border.focus
        variant == AuraTextFieldVariant.Filled -> colors.border.subtle
        else -> colors.border.default
    }

    val borderWidth = when (variant) {
        AuraTextFieldVariant.Outlined -> if (activeFocus) 2.dp else 1.dp
        AuraTextFieldVariant.Filled -> if (activeFocus || activeError) 1.5.dp else 1.dp
        AuraTextFieldVariant.Paperless -> if (activeFocus || activeError) 2.dp else 1.dp
    }

    val label = when {
        !enabled -> colors.content.disabled
        isError -> colors.status.error.solid
        activeFocus -> colors.brand.primary
        else -> colors.content.secondary
    }

    val text = if (!enabled) colors.content.disabled else colors.content.primary
    val placeholder = if (!enabled) colors.content.disabled else colors.content.tertiary
    val affix = if (!enabled) colors.content.disabled else colors.content.secondary
    val footer = when {
        !enabled -> colors.content.disabled
        isError -> colors.status.error.solid
        else -> colors.content.secondary
    }
    val counter = when {
        !enabled -> colors.content.disabled
        isError -> colors.status.error.solid
        else -> colors.content.tertiary
    }
    val cursor = when {
        !enabled || readOnly -> Color.Transparent
        isError -> colors.status.error.solid
        else -> colors.brand.primary
    }

    return AuraTextFieldResolvedColors(
        container = container,
        border = border,
        borderWidth = borderWidth,
        label = label,
        text = text,
        placeholder = placeholder,
        affix = affix,
        footer = footer,
        counter = counter,
        cursor = cursor,
    )
}

/**
 * Single-line and multi-line text input implementing the `aura.component.text-field` specification.
 *
 * Colors bind strictly to [AuraTheme.colors] using Compose Foundation's [BasicTextField]
 * with zero `material3` dependency.
 *
 * @param value Current text value.
 * @param onValueChange Callback invoked with the updated text (clamped when [maxLength] is set).
 * @param modifier Modifier applied to the outer field column.
 * @param variant Visual container variant (`Outlined`, `Filled`, `Paperless`).
 * @param label Persistent accessible label displayed above the field container.
 * @param placeholder Hint displayed when [value] is empty.
 * @param supportingText Helper guidance displayed below the field when [errorText] is absent.
 * @param errorText Validation error message; takes precedence over [supportingText].
 * @param prefixText Static unit or currency prefix rendered before input text (e.g., `"₩"`).
 * @param suffixText Static unit suffix rendered after input text (e.g., `"% ABV"`, `"원"`).
 * @param maxLength Optional character count limit with live footer counter.
 * @param enabled Controls whether the field accepts user interaction.
 * @param readOnly Keeps text legible and selectable while preventing edits.
 * @param singleLine Constrains input to a single line when true.
 * @param minLines Minimum visible text lines when [singleLine] is false.
 * @param maxLines Maximum visible text lines before scrolling when [singleLine] is false.
 * @param keyboardOptions Software keyboard configuration.
 * @param keyboardActions IME action callbacks.
 * @param leadingIcon Optional icon composable rendered at the start of the field.
 * @param trailingIcon Optional icon composable rendered at the end of the field.
 */
@Composable
fun AuraTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    variant: AuraTextFieldVariant = AuraTextFieldVariant.Outlined,
    label: String? = null,
    placeholder: String? = null,
    supportingText: String? = null,
    errorText: String? = null,
    prefixText: String? = null,
    suffixText: String? = null,
    maxLength: Int? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val motion = AuraTheme.motion
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isError = !errorText.isNullOrBlank()

    val baseColors = resolveAuraTextFieldColors(
        variant = variant,
        enabled = enabled,
        readOnly = readOnly,
        isError = isError,
        isFocused = isFocused,
        colors = colors,
    )
    val transitionDurationMillis = if (isFocused) {
        motion.pressInDurationMillis
    } else {
        motion.stateChangeDurationMillis
    }
    val colorAnimSpec = tween<Color>(
        durationMillis = transitionDurationMillis,
        easing = FastOutSlowInEasing,
    )
    val dpAnimSpec = tween<Dp>(
        durationMillis = transitionDurationMillis,
        easing = FastOutSlowInEasing,
    )
    val animatedBorderColor by animateColorAsState(
        targetValue = baseColors.border,
        animationSpec = colorAnimSpec,
        label = "AuraTextFieldBorderColor",
    )
    val animatedLabelColor by animateColorAsState(
        targetValue = baseColors.label,
        animationSpec = colorAnimSpec,
        label = "AuraTextFieldLabelColor",
    )
    val animatedFooterColor by animateColorAsState(
        targetValue = baseColors.footer,
        animationSpec = colorAnimSpec,
        label = "AuraTextFieldFooterColor",
    )
    val animatedBorderWidth by animateDpAsState(
        targetValue = baseColors.borderWidth,
        animationSpec = dpAnimSpec,
        label = "AuraTextFieldBorderWidth",
    )
    val resolvedColors = baseColors.copy(
        border = animatedBorderColor,
        borderWidth = animatedBorderWidth,
        label = animatedLabelColor,
        footer = animatedFooterColor,
    )

    val effectiveMinLines = if (singleLine) 1 else minLines.coerceAtLeast(1)
    val effectiveMaxLines = if (singleLine) 1 else maxLines.coerceAtLeast(effectiveMinLines)

    val sanitizedOnValueChange: (String) -> Unit = { newValue ->
        if (maxLength == null) {
            onValueChange(newValue)
        } else {
            val limit = maxLength.coerceAtLeast(0)
            when {
                newValue.length <= limit -> onValueChange(newValue)
                newValue.length < value.length -> onValueChange(newValue)
                value.length < limit -> onValueChange(newValue.take(limit))
            }
        }
    }

    val inputTextStyle = typography.bodyLarge.copy(
        color = resolvedColors.text,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    )
    val placeholderStyle = typography.bodyLarge.copy(
        color = resolvedColors.placeholder,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    )
    val affixStyle = typography.bodyMedium.copy(
        color = resolvedColors.affix,
        fontWeight = FontWeight.Medium,
    )

    Column(modifier = modifier) {
        if (!label.isNullOrBlank()) {
            AuraText(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = resolvedColors.label,
                style = typography.labelMedium,
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        BasicTextField(
            value = value,
            onValueChange = sanitizedOnValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    if (isError) {
                        error(errorText.orEmpty())
                    }
                },
            enabled = enabled,
            readOnly = readOnly,
            textStyle = inputTextStyle,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            minLines = effectiveMinLines,
            maxLines = effectiveMaxLines,
            interactionSource = interactionSource,
            cursorBrush = SolidColor(resolvedColors.cursor),
            decorationBox = { innerTextField ->
                AuraTextFieldContainer(
                    variant = variant,
                    resolvedColors = resolvedColors,
                    singleLine = singleLine,
                    effectiveMaxLines = effectiveMaxLines,
                    showPlaceholder = value.isEmpty() && !placeholder.isNullOrEmpty(),
                    placeholder = placeholder.orEmpty(),
                    placeholderStyle = placeholderStyle,
                    prefixText = prefixText,
                    suffixText = suffixText,
                    affixStyle = affixStyle,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    innerTextField = innerTextField,
                )
            },
        )

        val footerText = if (isError) errorText else supportingText
        if (!footerText.isNullOrBlank() || maxLength != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (!footerText.isNullOrBlank()) {
                        AuraText(
                            text = footerText,
                            color = resolvedColors.footer,
                            style = typography.bodySmall.copy(lineHeight = 16.sp),
                        )
                    }
                }
                if (maxLength != null) {
                    AuraText(
                        text = "${value.length}/${maxLength.coerceAtLeast(0)}",
                        color = resolvedColors.counter,
                        style = typography.monoSmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun AuraTextFieldContainer(
    variant: AuraTextFieldVariant,
    resolvedColors: AuraTextFieldResolvedColors,
    singleLine: Boolean,
    effectiveMaxLines: Int,
    showPlaceholder: Boolean,
    placeholder: String,
    placeholderStyle: TextStyle,
    prefixText: String?,
    suffixText: String?,
    affixStyle: TextStyle,
    leadingIcon: (@Composable () -> Unit)?,
    trailingIcon: (@Composable () -> Unit)?,
    innerTextField: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(FieldCornerRadius)
    val containerModifier = when (variant) {
        AuraTextFieldVariant.Outlined,
        AuraTextFieldVariant.Filled,
        -> Modifier
            .fillMaxWidth()
            .heightIn(min = FieldMinHeight)
            .clip(shape)
            .background(color = resolvedColors.container, shape = shape)
            .border(width = resolvedColors.borderWidth, color = resolvedColors.border, shape = shape)
            .padding(horizontal = 14.dp, vertical = 12.dp)

        AuraTextFieldVariant.Paperless -> Modifier
            .fillMaxWidth()
            .heightIn(min = FieldMinHeight)
            .drawBehind {
                val strokePx = resolvedColors.borderWidth.toPx()
                val y = size.height - strokePx / 2f
                drawLine(
                    color = resolvedColors.border,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokePx,
                )
            }
            .padding(horizontal = 4.dp, vertical = 10.dp)
    }

    val rowVerticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top
    val contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart

    Row(
        modifier = containerModifier,
        verticalAlignment = rowVerticalAlignment,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (leadingIcon != null) {
            CompositionLocalProvider(LocalAuraContentColor provides resolvedColors.affix) {
                Box(
                    modifier = Modifier.size(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    leadingIcon()
                }
            }
        }
        if (!prefixText.isNullOrEmpty()) {
            AuraText(
                text = prefixText,
                color = resolvedColors.affix,
                style = affixStyle,
            )
        }
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = contentAlignment,
        ) {
            if (showPlaceholder) {
                AuraText(
                    text = placeholder,
                    maxLines = if (singleLine) 1 else effectiveMaxLines,
                    overflow = TextOverflow.Ellipsis,
                    color = resolvedColors.placeholder,
                    style = placeholderStyle,
                )
            }
            innerTextField()
        }
        if (!suffixText.isNullOrEmpty()) {
            AuraText(
                text = suffixText,
                color = resolvedColors.affix,
                style = affixStyle,
            )
        }
        if (trailingIcon != null) {
            CompositionLocalProvider(LocalAuraContentColor provides resolvedColors.affix) {
                Box(
                    modifier = Modifier.size(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    trailingIcon()
                }
            }
        }
    }
}

@Preview(name = "Light Variants", showBackground = true)
@Composable
private fun AuraTextFieldLightPreview() {
    AuraTheme(colorMode = AuraColorMode.Light) {
        Column(
            modifier = Modifier
                .background(AuraTheme.colors.background.primary)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AuraTextField(
                value = "12,500,000",
                onValueChange = {},
                variant = AuraTextFieldVariant.Outlined,
                label = "Asset Value",
                prefixText = "₩",
                suffixText = "원",
                supportingText = "Enter current valuation",
            )
            AuraTextField(
                value = "46.0",
                onValueChange = {},
                variant = AuraTextFieldVariant.Filled,
                label = "ABV",
                suffixText = "% ABV",
                errorText = "ABV must be under 80%",
            )
            AuraTextField(
                value = "Warm vanilla, roasted oak, and lingering peat smoke.",
                onValueChange = {},
                variant = AuraTextFieldVariant.Paperless,
                label = "Tasting Note",
                singleLine = false,
                minLines = 3,
                maxLength = 120,
            )
            AuraTextField(
                value = "Locked Entry",
                onValueChange = {},
                variant = AuraTextFieldVariant.Outlined,
                label = "Disabled",
                enabled = false,
            )
        }
    }
}

@Preview(name = "Dark Variants", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun AuraTextFieldDarkPreview() {
    AuraTheme(colorMode = AuraColorMode.Dark) {
        Column(
            modifier = Modifier
                .background(AuraTheme.colors.background.primary)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AuraTextField(
                value = "12,500,000",
                onValueChange = {},
                variant = AuraTextFieldVariant.Outlined,
                label = "Asset Value",
                prefixText = "₩",
                suffixText = "원",
            )
            AuraTextField(
                value = "46.0",
                onValueChange = {},
                variant = AuraTextFieldVariant.Filled,
                label = "ABV",
                suffixText = "% ABV",
                errorText = "ABV must be under 80%",
            )
            AuraTextField(
                value = "",
                onValueChange = {},
                variant = AuraTextFieldVariant.Paperless,
                label = "Daily Diary",
                placeholder = "Write about today...",
                singleLine = false,
                minLines = 3,
            )
        }
    }
}
