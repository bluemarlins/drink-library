package com.bluemarlin.drinkdiary.ui.component

import android.graphics.ImageDecoder
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.example.ds.component.AuraText
import com.example.ds.foundation.theme.AuraTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.roundToInt

private const val ASSET_URI_PREFIX = "file:///android_asset/"
private const val MAX_IMAGE_DIMENSION_PX = 1024

// 이미지 라이브러리를 새로 들이지 않기 위해 플랫폼 ImageDecoder로 직접 디코드한다
// (harness.md §10 — 공식 대안으로 충분하면 서드파티를 추가하지 않는다).
@Composable
fun DDUriImage(
    imageUri: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var bitmap by remember(imageUri) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(imageUri) {
        bitmap =
            withContext(Dispatchers.IO) {
                runCatching {
                    val source =
                        if (imageUri.startsWith(ASSET_URI_PREFIX)) {
                            ImageDecoder.createSource(
                                context.assets,
                                imageUri.removePrefix(ASSET_URI_PREFIX),
                            )
                        } else {
                            ImageDecoder.createSource(context.contentResolver, Uri.parse(imageUri))
                        }
                    ImageDecoder
                        .decodeBitmap(source) { decoder, info, _ ->
                            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                            val width = info.size.width
                            val height = info.size.height
                            val longest = max(width, height)
                            if (longest > MAX_IMAGE_DIMENSION_PX && width > 0 && height > 0) {
                                val scale = MAX_IMAGE_DIMENSION_PX.toFloat() / longest.toFloat()
                                decoder.setTargetSize(
                                    (width * scale).roundToInt().coerceAtLeast(1),
                                    (height * scale).roundToInt().coerceAtLeast(1),
                                )
                            }
                        }.asImageBitmap()
                }.getOrNull()
            }
    }

    val loaded = bitmap
    if (loaded == null) {
        Box(
            modifier = modifier.background(AuraTheme.colors.surface.sunken),
            contentAlignment = Alignment.Center,
        ) {
            AuraText("사진", style = AuraTheme.typography.labelSmall)
        }
    } else {
        Image(
            bitmap = loaded,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
        )
    }
}
