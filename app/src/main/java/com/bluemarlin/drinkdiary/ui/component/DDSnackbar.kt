package com.bluemarlin.drinkdiary.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.ui.theme.DDShape
import com.example.ds.component.AuraText
import com.example.ds.foundation.theme.AuraTheme
import kotlinx.coroutines.delay

// Aura 1.0.0에는 Snackbar가 없어(design-system.md Non-Replaceable 목록) 기존 로컬 구현을 유지하고 Aura 토큰만 사용한다.
@Stable
class DDSnackbarState {
    var message: String? by mutableStateOf(null)
        private set

    suspend fun showSnackbar(
        text: String,
        durationMillis: Long = 3_000L,
    ) {
        message = text
        delay(durationMillis)
        message = null
    }
}

@Composable
fun DDSnackbarHost(
    state: DDSnackbarState,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 12.dp,
) {
    val text = state.message
    val duration = AuraTheme.motion.stateChangeDurationMillis
    AnimatedVisibility(
        visible = text != null,
        enter = fadeIn(animationSpec = tween(durationMillis = duration)),
        exit = fadeOut(animationSpec = tween(durationMillis = duration)),
        modifier = modifier,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = bottomPadding),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                modifier =
                    Modifier
                        .background(AuraTheme.colors.surface.inverse, DDShape.medium)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                AuraText(
                    text = text.orEmpty(),
                    style = AuraTheme.typography.bodyMedium,
                    color = AuraTheme.colors.content.inverse,
                )
            }
        }
    }
}
