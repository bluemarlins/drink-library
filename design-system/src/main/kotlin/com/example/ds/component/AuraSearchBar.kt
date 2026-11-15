package com.example.ds.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ds.foundation.theme.AuraColorMode
import com.example.ds.foundation.theme.AuraTheme

/**
 * Container material variants for [AuraSearchBar].
 */
enum class AuraSearchBarSurface {
    /** High-legibility recessed solid bar (`surface.sunken`) for content pages. */
    Solid,

    /** Translucent spatial backdrop container powered by [GlassSurface] (`FROSTED` or `LIQUID`). */
    Glass,
}

private val SearchBarMinHeight = 52.dp
private val SearchBarShape = RoundedCornerShape(50)

/**
 * Instant query search input implementing the `aura.component.search-bar` specification.
 */
@Composable
fun AuraSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSearch: ((String) -> Unit)? = null,
    placeholder: String = "Search...",
    surface: AuraSearchBarSurface = AuraSearchBarSurface.Solid,
    glassStyle: GlassStyle = GlassStyle.FROSTED,
    enabled: Boolean = true,
    activeFilterCount: Int = 0,
    onFilterClick: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val motion = AuraTheme.motion
    val reducedMotion = rememberAuraReducedMotion()
    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isFocused by resolvedInteractionSource.collectIsFocusedAsState()
    val activeFocus = enabled && isFocused

    val colorSpec = tween<Color>(
        durationMillis = if (reducedMotion) 0 else motion.stateChangeDurationMillis,
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            !enabled -> colors.border.subtle
            activeFocus -> colors.border.focus
            else -> colors.border.subtle
        },
        animationSpec = colorSpec,
        label = "AuraSearchBarBorder",
    )
    val borderWidth by animateDpAsState(
        targetValue = if (activeFocus) 2.dp else 1.dp,
        animationSpec = if (reducedMotion) snap() else tween(durationMillis = motion.pressInDurationMillis),
        label = "AuraSearchBarBorderWidth",
    )
    val leadingIconColor by animateColorAsState(
        targetValue = when {
            !enabled -> colors.content.disabled
            activeFocus -> colors.brand.primary
            else -> colors.content.secondary
        },
        animationSpec = colorSpec,
        label = "AuraSearchBarIconColor",
    )

    val rowContent: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = SearchBarMinHeight)
                .padding(start = 16.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SearchMagnifierGlyph(
                color = leadingIconColor,
                modifier = Modifier.size(20.dp),
            )

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (query.isEmpty()) {
                    AuraText(
                        text = placeholder,
                        style = typography.bodyLarge,
                        color = if (enabled) colors.content.tertiary else colors.content.disabled,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    singleLine = true,
                    textStyle = typography.bodyLarge.copy(
                        color = if (enabled) colors.content.primary else colors.content.disabled,
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { onSearch?.invoke(query) },
                    ),
                    cursorBrush = SolidColor(if (enabled) colors.brand.primary else Color.Transparent),
                    interactionSource = resolvedInteractionSource,
                )
            }

            AnimatedVisibility(
                visible = query.isNotEmpty() && enabled,
                enter = if (reducedMotion) fadeIn(snap()) else fadeIn(tween(motion.stateChangeDurationMillis)) + scaleIn(motion.iconSpring()),
                exit = if (reducedMotion) fadeOut(snap()) else fadeOut(tween(motion.pressInDurationMillis)) + scaleOut(tween(motion.pressInDurationMillis)),
            ) {
                SearchClearButton(
                    color = colors.content.secondary,
                    onClick = { onQueryChange("") },
                )
            }

            if (onFilterClick != null) {
                SearchFilterButton(
                    enabled = enabled,
                    activeFilterCount = activeFilterCount,
                    onClick = onFilterClick,
                )
            }
        }
    }

    when (surface) {
        AuraSearchBarSurface.Solid -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .heightIn(min = SearchBarMinHeight)
                    .clip(SearchBarShape)
                    .background(if (enabled) colors.surface.sunken else colors.interaction.disabledContainer)
                    .border(borderWidth, borderColor, SearchBarShape),
                contentAlignment = Alignment.Center,
            ) {
                rowContent()
            }
        }

        AuraSearchBarSurface.Glass -> {
            GlassSurface(
                modifier = modifier
                    .fillMaxWidth()
                    .heightIn(min = SearchBarMinHeight)
                    .then(
                        if (activeFocus) {
                            Modifier.border(borderWidth, borderColor, SearchBarShape)
                        } else {
                            Modifier
                        },
                    ),
                glassStyle = glassStyle,
                shape = SearchBarShape,
                cornerRadiusHint = 26.dp,
                surfaceAlphaMultiplier = if (enabled) 1f else 0.55f,
            ) {
                rowContent()
            }
        }
    }
}

@Composable
private fun SearchClearButton(
    color: Color,
    onClick: () -> Unit,
) {
    val motion = AuraTheme.motion
    val reducedMotion = rememberAuraReducedMotion()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by rememberAuraTactilePressed(
        interactionSource = interactionSource,
        minPressDurationMillis = motion.minPressDurationMillis,
    )
    val scale by animateFloatAsState(
        targetValue = if (!reducedMotion && isPressed) motion.pressedScaleDefault else 1f,
        animationSpec = when {
            reducedMotion -> snap()
            isPressed -> motion.pressInSpring()
            else -> motion.pressOutSpring()
        },
        label = "SearchClearScale",
    )

    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(50))
            .semantics { contentDescription = "Clear search" }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(16.dp)) {
            val stroke = 2.dp.toPx()
            val inset = 2.dp.toPx()
            drawLine(
                color = color,
                start = Offset(inset, inset),
                end = Offset(size.width - inset, size.height - inset),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = color,
                start = Offset(size.width - inset, inset),
                end = Offset(inset, size.height - inset),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun SearchFilterButton(
    enabled: Boolean,
    activeFilterCount: Int,
    onClick: () -> Unit,
) {
    val colors = AuraTheme.colors
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
        label = "SearchFilterScale",
    )
    val iconColor = when {
        !enabled -> colors.content.disabled
        activeFilterCount > 0 -> colors.brand.primary
        else -> colors.content.secondary
    }

    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(50))
            .semantics {
                contentDescription = if (activeFilterCount > 0) {
                    "Filter ($activeFilterCount active)"
                } else {
                    "Filter results"
                }
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (activeFilterCount > 0) {
            AuraBadgedBox(
                badge = {
                    AuraBadge(
                        count = activeFilterCount,
                        variant = AuraBadgeVariant.Brand,
                        size = AuraBadgeSize.Small,
                    )
                },
            ) {
                SearchFilterSlidersGlyph(
                    color = iconColor,
                    modifier = Modifier.size(20.dp),
                )
            }
        } else {
            SearchFilterSlidersGlyph(
                color = iconColor,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun SearchMagnifierGlyph(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val stroke = 1.9.dp.toPx()
        val circleRadius = size.minDimension * 0.31f
        val circleCenter = Offset(size.width * 0.42f, size.height * 0.42f)
        drawCircle(
            color = color,
            radius = circleRadius,
            center = circleCenter,
            style = Stroke(width = stroke),
        )
        drawLine(
            color = color,
            start = Offset(
                circleCenter.x + circleRadius * 0.72f,
                circleCenter.y + circleRadius * 0.72f,
            ),
            end = Offset(size.width * 0.86f, size.height * 0.86f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun SearchFilterSlidersGlyph(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val stroke = 1.8.dp.toPx()
        val y1 = size.height * 0.28f
        val y2 = size.height * 0.50f
        val y3 = size.height * 0.72f
        val xStart = size.width * 0.14f
        val xEnd = size.width * 0.86f

        drawLine(color = color, start = Offset(xStart, y1), end = Offset(xEnd, y1), strokeWidth = stroke, cap = StrokeCap.Round)
        drawCircle(color = color, radius = 2.4.dp.toPx(), center = Offset(size.width * 0.36f, y1))

        drawLine(color = color, start = Offset(xStart, y2), end = Offset(xEnd, y2), strokeWidth = stroke, cap = StrokeCap.Round)
        drawCircle(color = color, radius = 2.4.dp.toPx(), center = Offset(size.width * 0.68f, y2))

        drawLine(color = color, start = Offset(xStart, y3), end = Offset(xEnd, y3), strokeWidth = stroke, cap = StrokeCap.Round)
        drawCircle(color = color, radius = 2.4.dp.toPx(), center = Offset(size.width * 0.44f, y3))
    }
}

@Preview(name = "AuraSearchBar Light", showBackground = true)
@Composable
private fun AuraSearchBarLightPreview() {
    AuraTheme(colorMode = AuraColorMode.Light) {
        Column(
            modifier = Modifier
                .background(AuraTheme.colors.background.primary)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AuraSearchBar(
                query = "Barolo",
                onQueryChange = {},
                activeFilterCount = 2,
                onFilterClick = {},
            )
            AuraSearchBar(
                query = "",
                onQueryChange = {},
                placeholder = "Search bottles or tasting notes...",
                surface = AuraSearchBarSurface.Glass,
            )
        }
    }
}
