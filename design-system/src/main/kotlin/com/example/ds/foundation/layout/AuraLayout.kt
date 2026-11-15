package com.example.ds.foundation.layout

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Tier 2 — Semantic responsive layout tokens derived from [AuraWindowInfo].
 *
 * Balances physical Smallest Width (`sw`) and live Display Ratio (`width / height`) so
 * foldable cover displays, ~1:1 unfolded foldables, landscape phones, and tablets
 * receive tailored spacing, column counts, chrome heights, and two-pane splits.
 */
@Immutable
data class AuraLayout(
    val screenMarginHorizontal: Dp,
    val screenMarginVertical: Dp,
    val sectionSpacing: Dp,
    val gridColumns: Int,
    val gridGutter: Dp,
    val topBarHeight: Dp,
    val maxContentWidth: Dp,
    val supportsTwoPane: Boolean,
    val primaryPaneFraction: Float,
    val typographyScale: Float,
)

object AuraLayoutDefaults {
    val ExtraSmall = AuraLayout(
        screenMarginHorizontal = 12.dp,
        screenMarginVertical = 12.dp,
        sectionSpacing = 16.dp,
        gridColumns = 1,
        gridGutter = 10.dp,
        topBarHeight = 52.dp,
        maxContentWidth = 600.dp,
        supportsTwoPane = false,
        primaryPaneFraction = 1f,
        typographyScale = 0.92f,
    )

    val Compact = AuraLayout(
        screenMarginHorizontal = 16.dp,
        screenMarginVertical = 16.dp,
        sectionSpacing = 20.dp,
        gridColumns = 1,
        gridGutter = 12.dp,
        topBarHeight = 56.dp,
        maxContentWidth = 600.dp,
        supportsTwoPane = false,
        primaryPaneFraction = 1f,
        typographyScale = 1.00f,
    )

    val PhoneLandscape = AuraLayout(
        screenMarginHorizontal = 24.dp,
        screenMarginVertical = 12.dp,
        sectionSpacing = 16.dp,
        gridColumns = 2,
        gridGutter = 16.dp,
        topBarHeight = 48.dp,
        maxContentWidth = 960.dp,
        supportsTwoPane = true,
        primaryPaneFraction = 0.42f,
        typographyScale = 0.96f,
    )

    val FoldableSquare = AuraLayout(
        screenMarginHorizontal = 24.dp,
        screenMarginVertical = 20.dp,
        sectionSpacing = 24.dp,
        gridColumns = 2,
        gridGutter = 16.dp,
        topBarHeight = 60.dp,
        maxContentWidth = 840.dp,
        supportsTwoPane = true,
        primaryPaneFraction = 0.45f,
        typographyScale = 1.04f,
    )

    val Medium = AuraLayout(
        screenMarginHorizontal = 24.dp,
        screenMarginVertical = 20.dp,
        sectionSpacing = 24.dp,
        gridColumns = 2,
        gridGutter = 16.dp,
        topBarHeight = 60.dp,
        maxContentWidth = 840.dp,
        supportsTwoPane = true,
        primaryPaneFraction = 0.44f,
        typographyScale = 1.04f,
    )

    val Expanded = AuraLayout(
        screenMarginHorizontal = 32.dp,
        screenMarginVertical = 24.dp,
        sectionSpacing = 28.dp,
        gridColumns = 3,
        gridGutter = 20.dp,
        topBarHeight = 64.dp,
        maxContentWidth = 1200.dp,
        supportsTwoPane = true,
        primaryPaneFraction = 0.38f,
        typographyScale = 1.08f,
    )

    /**
     * Resolves semantic layout tokens using both Smallest Width and Display Ratio axes.
     */
    fun fromWindowInfo(windowInfo: AuraWindowInfo): AuraLayout = when {
        windowInfo.widthClass == AuraWidthClass.ExtraSmall -> ExtraSmall

        windowInfo.smallestWidthClass <= AuraWidthClass.Compact &&
            windowInfo.widthDp >= 600 &&
            (windowInfo.displayRatio == AuraDisplayRatio.UltraWide ||
                windowInfo.heightClass == AuraHeightClass.Compact) ->
            PhoneLandscape.copy(supportsTwoPane = windowInfo.widthDp >= 640)

        windowInfo.widthClass >= AuraWidthClass.Medium &&
            windowInfo.displayRatio == AuraDisplayRatio.Square -> FoldableSquare

        windowInfo.widthClass == AuraWidthClass.Expanded -> Expanded

        windowInfo.widthClass == AuraWidthClass.Medium -> Medium

        windowInfo.smallestWidthClass == AuraWidthClass.ExtraSmall -> ExtraSmall

        else -> Compact
    }
}
