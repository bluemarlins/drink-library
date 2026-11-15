package com.example.ds.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.isSpecified
import com.example.ds.foundation.theme.AuraTheme

/**
 * Adaptive two-pane container implementing the `aura.component.adaptive-two-pane` specification.
 *
 * Stacks [primaryPane] and [secondaryPane] vertically in single-pane environments, and splits
 * them into independently scrollable side-by-side panes when [AuraTheme.layout.supportsTwoPane]
 * is `true` (e.g., unfolded square foldables, landscape phones, and tablets).
 */
@Composable
fun AuraAdaptiveTwoPane(
    modifier: Modifier = Modifier,
    forceSinglePane: Boolean = false,
    primaryPaneFraction: Float = AuraTheme.layout.primaryPaneFraction,
    paneSpacing: Dp = AuraTheme.layout.gridGutter,
    primaryPane: @Composable () -> Unit,
    secondaryPane: @Composable () -> Unit,
) {
    val layout = AuraTheme.layout
    val widthConstrainedModifier = if (layout.maxContentWidth.isSpecified) {
        Modifier.widthIn(max = layout.maxContentWidth)
    } else {
        Modifier
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        if (layout.supportsTwoPane && !forceSinglePane) {
            val fraction = primaryPaneFraction.coerceIn(0.25f, 0.65f)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .then(widthConstrainedModifier)
                    .padding(horizontal = layout.screenMarginHorizontal),
                horizontalArrangement = Arrangement.spacedBy(paneSpacing),
            ) {
                Column(
                    modifier = Modifier
                        .weight(fraction)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = layout.screenMarginVertical),
                    verticalArrangement = Arrangement.spacedBy(layout.sectionSpacing),
                ) {
                    primaryPane()
                }
                Column(
                    modifier = Modifier
                        .weight(1f - fraction)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = layout.screenMarginVertical),
                    verticalArrangement = Arrangement.spacedBy(layout.sectionSpacing),
                ) {
                    secondaryPane()
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(widthConstrainedModifier)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = layout.screenMarginHorizontal,
                        vertical = layout.screenMarginVertical,
                    ),
                verticalArrangement = Arrangement.spacedBy(layout.sectionSpacing),
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    primaryPane()
                }
                Box(modifier = Modifier.fillMaxWidth()) {
                    secondaryPane()
                }
            }
        }
    }
}
