package com.bluemarlin.drinkdiary.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.ds.component.AuraAdaptiveTwoPane
import com.example.ds.foundation.theme.AuraTheme

// AuraTheme.layout의 여백·섹션 간격·maxContentWidth를 전 화면에 일관되게 적용한다.
@Composable
fun DDCenteredContent(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val layout = AuraTheme.layout
    val outerModifier =
        modifier
            .fillMaxSize()
            .imePadding()
            .let { base ->
                if (scrollable) base.verticalScroll(rememberScrollState()) else base
            }

    Box(
        modifier = outerModifier,
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier =
                Modifier
                    .widthIn(max = layout.maxContentWidth)
                    .fillMaxWidth()
                    .padding(contentPadding)
                    .padding(
                        horizontal = layout.screenMarginHorizontal,
                        vertical = layout.screenMarginVertical,
                    ),
            verticalArrangement = Arrangement.spacedBy(layout.sectionSpacing),
            content = content,
        )
    }
}

// AuraAdaptiveTwoPane은 내부에서 양쪽 패널에 verticalScroll과 screenMargin을 적용한다.
@Composable
fun DDTwoPaneContent(
    primaryPane: @Composable () -> Unit,
    secondaryPane: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    primaryPaneFraction: Float = AuraTheme.layout.primaryPaneFraction,
) {
    val layout = AuraTheme.layout
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(contentPadding),
        contentAlignment = Alignment.TopCenter,
    ) {
        AuraAdaptiveTwoPane(
            modifier =
                Modifier
                    .widthIn(max = layout.maxContentWidth)
                    .fillMaxSize(),
            primaryPaneFraction = primaryPaneFraction,
            primaryPane = primaryPane,
            secondaryPane = secondaryPane,
        )
    }
}
