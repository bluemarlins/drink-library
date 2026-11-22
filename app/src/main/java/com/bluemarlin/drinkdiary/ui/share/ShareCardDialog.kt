package com.bluemarlin.drinkdiary.ui.share

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.bluemarlins.aura.component.AuraDialog
import io.github.bluemarlins.aura.component.AuraDialogVariant
import io.github.bluemarlins.aura.component.AuraText
import io.github.bluemarlins.aura.foundation.theme.AuraTheme
import kotlinx.coroutines.launch

@Composable
fun ShareCardDialog(
    content: ShareCardContent?,
    onDismiss: () -> Unit,
) {
    var cachedContent by remember { mutableStateOf<ShareCardContent?>(null) }
    if (content != null) {
        cachedContent = content
    }
    val activeContent = content ?: cachedContent ?: return

    val colors = AuraTheme.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val bitmap = remember(activeContent, colors) { ShareCardRenderer.render(activeContent, colors) }

    AuraDialog(
        visible = content != null,
        title = "내 취향 카드",
        variant = AuraDialogVariant.Glass,
        confirmText = "이미지 공유하기",
        dismissText = "닫기",
        onConfirm = {
            scope.launch {
                ShareCardRenderer.shareBitmap(context, bitmap)
                onDismiss()
            }
        },
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "${activeContent.type.code} 취향 공유 카드 미리보기",
                contentScale = ContentScale.Fit,
                modifier =
                    Modifier
                        .heightIn(max = 320.dp)
                        .aspectRatio(9f / 16f)
                        .clip(AuraTheme.shapes.medium),
            )
            AuraText(
                text = "스토리에 올리기 좋은 세로 이미지로 공유해요. 이미지는 이 기기에서만 만들어져요.",
                style = AuraTheme.typography.bodySmall,
                color = AuraTheme.colors.content.secondary,
            )
        }
    }
}
