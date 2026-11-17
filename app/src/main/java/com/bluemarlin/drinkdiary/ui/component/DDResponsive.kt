package com.bluemarlin.drinkdiary.ui.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.example.ds.component.AuraAdaptiveTwoPane
import com.example.ds.foundation.theme.AuraTheme

// AuraTheme.layout의 여백·섹션 간격·maxContentWidth를 전 화면에 일관되게 적용한다.
@Composable
fun DDCenteredContent(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    scrollable: Boolean = true,
    scrollState: ScrollState = rememberScrollState(),
    content: @Composable ColumnScope.() -> Unit,
) {
    val layout = AuraTheme.layout
    val outerModifier =
        modifier
            .fillMaxSize()
            .imePadding()
            .let { base ->
                if (scrollable) base.verticalScroll(scrollState) else base
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

// AuraAdaptiveTwoPane은 내부에서 양쪽 패널에 독립 verticalScroll을 제공한다.
// FloatingGlassScaffold 뒤로 콘텐츠가 흐르도록 상·하 contentPadding은 각 패널 내부 Spacer로 전달한다.
@Composable
fun DDTwoPaneContent(
    primaryPane: @Composable () -> Unit,
    secondaryPane: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    primaryPaneFraction: Float = AuraTheme.layout.primaryPaneFraction,
) {
    val layout = AuraTheme.layout
    val direction = LocalLayoutDirection.current
    val topInset = contentPadding.calculateTopPadding()
    val bottomInset = contentPadding.calculateBottomPadding()
    val isTwoPane = layout.supportsTwoPane

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(
                    start = contentPadding.calculateStartPadding(direction),
                    end = contentPadding.calculateEndPadding(direction),
                ),
        contentAlignment = Alignment.TopCenter,
    ) {
        AuraAdaptiveTwoPane(
            modifier = Modifier.fillMaxSize(),
            primaryPaneFraction = primaryPaneFraction,
            primaryPane = {
                if (topInset > 0.dp) Spacer(Modifier.height(topInset))
                primaryPane()
                if (isTwoPane && bottomInset > 0.dp) Spacer(Modifier.height(bottomInset))
            },
            secondaryPane = {
                if (isTwoPane && topInset > 0.dp) Spacer(Modifier.height(topInset))
                secondaryPane()
                if (bottomInset > 0.dp) Spacer(Modifier.height(bottomInset))
            },
        )
    }
}
