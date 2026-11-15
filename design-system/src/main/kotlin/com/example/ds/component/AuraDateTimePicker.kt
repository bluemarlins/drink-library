package com.example.ds.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ds.foundation.theme.AuraColorMode
import com.example.ds.foundation.theme.AuraTheme

/**
 * Temporal selection mode for [AuraDateTimePicker].
 */
enum class AuraDateTimeMode {
    Date,
    DateTime,
    YearVintage,
}

private val MonthNames = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December",
)

private val WeekdayShortLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

internal fun isLeapYear(year: Int): Boolean =
    (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

internal fun daysInMonth(year: Int, month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if (isLeapYear(year)) 29 else 28
    else -> 30
}

internal fun firstDayOfWeekSunday(year: Int, month: Int): Int {
    val t = intArrayOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4)
    val y = if (month < 3) year - 1 else year
    return (y + y / 4 - y / 100 + y / 400 + t[(month - 1).coerceIn(0, 11)] + 1) % 7
}

private fun padTwoDigits(n: Int): String = if (n < 10) "0$n" else n.toString()

/**
 * Structured calendar date, time-slot, and vintage-year selector implementing `aura.component.date-time-picker`.
 */
@Composable
fun AuraDateTimePicker(
    selectedYear: Int,
    selectedMonth: Int,
    selectedDay: Int,
    onDateChange: (Int, Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    mode: AuraDateTimeMode = AuraDateTimeMode.Date,
    selectedHour: Int = 19,
    selectedMinute: Int = 0,
    onTimeChange: ((Int, Int) -> Unit)? = null,
    enabled: Boolean = true,
    label: String? = null,
    supportingText: String? = null,
    minYear: Int = 1950,
    maxYear: Int = 2030,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val safeMinYear = minOf(minYear, maxYear)
    val safeMaxYear = maxOf(minYear, maxYear)
    val clampedMonth = selectedMonth.coerceIn(1, 12)
    val clampedYear = selectedYear.coerceIn(safeMinYear, safeMaxYear)
    val clampedDay = selectedDay.coerceIn(1, daysInMonth(clampedYear, clampedMonth))

    var displayYear by remember(clampedYear) { mutableIntStateOf(clampedYear) }
    var displayMonth by remember(clampedMonth) { mutableIntStateOf(clampedMonth) }

    // Page base for YearVintage 12-year matrix
    val yearsPerPage = 12
    val initialPageStart = (((clampedYear - safeMinYear) / yearsPerPage) * yearsPerPage + safeMinYear)
        .coerceIn(safeMinYear, safeMaxYear)
    var vintageStartYear by remember(clampedYear, safeMinYear, safeMaxYear) {
        mutableIntStateOf(initialPageStart)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (!label.isNullOrBlank()) {
            AuraText(
                text = label,
                style = typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = if (enabled) colors.content.secondary else colors.content.disabled,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(if (enabled) colors.surface.default else colors.interaction.disabledContainer)
                .border(1.dp, colors.border.subtle, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (mode == AuraDateTimeMode.YearVintage) {
                val pageEnd = minOf(safeMaxYear, vintageStartYear + yearsPerPage - 1)
                PickerHeaderBar(
                    title = "$vintageStartYear – $pageEnd",
                    canStepBack = enabled && vintageStartYear > safeMinYear,
                    canStepForward = enabled && pageEnd < safeMaxYear,
                    prevContentDescription = "Previous years",
                    nextContentDescription = "Next years",
                    onPrevious = {
                        vintageStartYear = (vintageStartYear - yearsPerPage).coerceAtLeast(safeMinYear)
                    },
                    onNext = {
                        vintageStartYear = (vintageStartYear + yearsPerPage)
                            .coerceAtMost((safeMaxYear - yearsPerPage + 1).coerceAtLeast(safeMinYear))
                    },
                )

                val pageYears = (vintageStartYear..pageEnd).toList()
                val rows = pageYears.chunked(4)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    rows.forEach { rowYears ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            for (i in 0 until 4) {
                                val yearVal = rowYears.getOrNull(i)
                                if (yearVal != null) {
                                    PickerCellPill(
                                        text = yearVal.toString(),
                                        selected = yearVal == clampedYear,
                                        enabled = enabled,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            val maxD = daysInMonth(yearVal, clampedMonth)
                                            onDateChange(yearVal, clampedMonth, clampedDay.coerceAtMost(maxD))
                                        },
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            } else {
                val canStepPrevMonth = enabled && (displayYear > safeMinYear || displayMonth > 1)
                val canStepNextMonth = enabled && (displayYear < safeMaxYear || displayMonth < 12)

                PickerHeaderBar(
                    title = "${MonthNames[displayMonth - 1]} $displayYear",
                    canStepBack = canStepPrevMonth,
                    canStepForward = canStepNextMonth,
                    prevContentDescription = "Previous month",
                    nextContentDescription = "Next month",
                    onPrevious = {
                        if (displayMonth > 1) {
                            displayMonth -= 1
                        } else if (displayYear > safeMinYear) {
                            displayYear -= 1
                            displayMonth = 12
                        }
                    },
                    onNext = {
                        if (displayMonth < 12) {
                            displayMonth += 1
                        } else if (displayYear < safeMaxYear) {
                            displayYear += 1
                            displayMonth = 1
                        }
                    },
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    WeekdayShortLabels.forEach { dayName ->
                        AuraText(
                            text = dayName,
                            style = typography.labelSmall,
                            color = colors.content.tertiary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                val firstWeekday = firstDayOfWeekSunday(displayYear, displayMonth)
                val totalDays = daysInMonth(displayYear, displayMonth)
                val totalSlots = firstWeekday + totalDays
                val weekCount = ((totalSlots + 6) / 7).coerceAtLeast(1)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (week in 0 until weekCount) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            for (col in 0 until 7) {
                                val slotIndex = week * 7 + col
                                val dayNum = slotIndex - firstWeekday + 1
                                if (dayNum in 1..totalDays) {
                                    val isSelected = displayYear == clampedYear &&
                                        displayMonth == clampedMonth &&
                                        dayNum == clampedDay
                                    PickerCellPill(
                                        text = dayNum.toString(),
                                        selected = isSelected,
                                        enabled = enabled,
                                        isCircle = true,
                                        modifier = Modifier.weight(1f),
                                        onClick = { onDateChange(displayYear, displayMonth, dayNum) },
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                if (mode == AuraDateTimeMode.DateTime) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(colors.border.subtle),
                    )
                    val safeHour = selectedHour.coerceIn(0, 23)
                    val safeMinute = selectedMinute.coerceIn(0, 59)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AuraText(
                            text = "Time",
                            style = typography.labelLarge,
                            color = if (enabled) colors.content.primary else colors.content.disabled,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            TimeUnitStepper(
                                valueText = padTwoDigits(safeHour),
                                enabled = enabled,
                                decrementDescription = "Decrease hour",
                                incrementDescription = "Increase hour",
                                onDecrement = {
                                    val nextH = if (safeHour == 0) 23 else safeHour - 1
                                    onTimeChange?.invoke(nextH, safeMinute)
                                },
                                onIncrement = {
                                    val nextH = (safeHour + 1) % 24
                                    onTimeChange?.invoke(nextH, safeMinute)
                                },
                            )
                            AuraText(
                                text = ":",
                                style = typography.monoMedium,
                                color = colors.content.secondary,
                            )
                            TimeUnitStepper(
                                valueText = padTwoDigits(safeMinute),
                                enabled = enabled,
                                decrementDescription = "Decrease minute",
                                incrementDescription = "Increase minute",
                                onDecrement = {
                                    val nextM = (safeMinute - 5 + 60) % 60
                                    onTimeChange?.invoke(safeHour, nextM)
                                },
                                onIncrement = {
                                    val nextM = (safeMinute + 5) % 60
                                    onTimeChange?.invoke(safeHour, nextM)
                                },
                            )
                        }
                    }
                }
            }
        }

        if (!supportingText.isNullOrBlank()) {
            AuraText(
                text = supportingText,
                style = typography.bodySmall,
                color = if (enabled) colors.content.secondary else colors.content.disabled,
            )
        }
    }
}

@Composable
private fun PickerHeaderBar(
    title: String,
    canStepBack: Boolean,
    canStepForward: Boolean,
    prevContentDescription: String,
    nextContentDescription: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeaderChevronButton(
            isNext = false,
            enabled = canStepBack,
            contentDescription = prevContentDescription,
            onClick = onPrevious,
        )
        AuraText(
            text = title,
            style = typography.titleSmall,
            color = colors.content.primary,
        )
        HeaderChevronButton(
            isNext = true,
            enabled = canStepForward,
            contentDescription = nextContentDescription,
            onClick = onNext,
        )
    }
}

@Composable
private fun HeaderChevronButton(
    isNext: Boolean,
    enabled: Boolean,
    contentDescription: String,
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
        label = "PickerArrowScale",
    )
    val glyphColor = if (enabled) colors.content.primary else colors.content.disabled

    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 40.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(10.dp))
            .background(if (enabled) colors.surface.sunken else colors.interaction.disabledContainer)
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
        Canvas(modifier = Modifier.size(16.dp)) {
            val stroke = 2.dp.toPx()
            val path = Path().apply {
                if (isNext) {
                    moveTo(size.width * 0.36f, size.height * 0.22f)
                    lineTo(size.width * 0.68f, size.height * 0.50f)
                    lineTo(size.width * 0.36f, size.height * 0.78f)
                } else {
                    moveTo(size.width * 0.64f, size.height * 0.22f)
                    lineTo(size.width * 0.32f, size.height * 0.50f)
                    lineTo(size.width * 0.64f, size.height * 0.78f)
                }
            }
            drawPath(
                path = path,
                color = glyphColor,
                style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
    }
}

@Composable
private fun PickerCellPill(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCircle: Boolean = false,
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

    val containerColor by animateColorAsState(
        targetValue = when {
            !enabled && selected -> colors.content.disabled.copy(alpha = 0.25f)
            selected -> colors.brand.primary
            isPressed && enabled -> colors.surface.sunken
            else -> Color.Transparent
        },
        animationSpec = if (reducedMotion) snap() else tween(durationMillis = motion.stateChangeDurationMillis),
        label = "PickerCellBg",
    )
    val contentColor by animateColorAsState(
        targetValue = when {
            !enabled -> colors.content.disabled
            selected -> colors.brand.onPrimary
            else -> colors.content.primary
        },
        animationSpec = if (reducedMotion) snap() else tween(durationMillis = motion.stateChangeDurationMillis),
        label = "PickerCellFg",
    )
    val scale by animateFloatAsState(
        targetValue = if (!reducedMotion && enabled && isPressed) motion.pressedScaleDefault else 1f,
        animationSpec = when {
            reducedMotion -> snap()
            isPressed -> motion.pressInSpring()
            else -> motion.pressOutSpring()
        },
        label = "PickerCellScale",
    )

    val shape = if (isCircle) CircleShape else RoundedCornerShape(10.dp)

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 40.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        AuraText(
            text = text,
            style = typography.monoMedium.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            ),
            color = contentColor,
        )
    }
}

@Composable
private fun TimeUnitStepper(
    valueText: String,
    enabled: Boolean,
    decrementDescription: String,
    incrementDescription: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (enabled) colors.surface.sunken else colors.interaction.disabledContainer)
            .border(1.dp, colors.border.subtle, RoundedCornerShape(10.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .defaultMinSize(minWidth = 36.dp, minHeight = 40.dp)
                .semantics { contentDescription = decrementDescription }
                .clickable(enabled = enabled, role = Role.Button, onClick = onDecrement),
            contentAlignment = Alignment.Center,
        ) {
            AuraText(
                text = "−",
                style = typography.labelLarge,
                color = if (enabled) colors.content.secondary else colors.content.disabled,
            )
        }
        AuraText(
            text = valueText,
            style = typography.monoMedium,
            color = if (enabled) colors.content.primary else colors.content.disabled,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        Box(
            modifier = Modifier
                .defaultMinSize(minWidth = 36.dp, minHeight = 40.dp)
                .semantics { contentDescription = incrementDescription }
                .clickable(enabled = enabled, role = Role.Button, onClick = onIncrement),
            contentAlignment = Alignment.Center,
        ) {
            AuraText(
                text = "+",
                style = typography.labelLarge,
                color = if (enabled) colors.content.secondary else colors.content.disabled,
            )
        }
    }
}

@Preview(name = "AuraDateTimePicker Light", showBackground = true)
@Composable
private fun AuraDateTimePickerLightPreview() {
    AuraTheme(colorMode = AuraColorMode.Light) {
        Column(
            modifier = Modifier
                .background(AuraTheme.colors.background.primary)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AuraDateTimePicker(
                selectedYear = 2026,
                selectedMonth = 11,
                selectedDay = 15,
                onDateChange = { _, _, _ -> },
                mode = AuraDateTimeMode.DateTime,
                label = "Schedule Date & Time",
            )
        }
    }
}
