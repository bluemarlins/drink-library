package com.example.ds.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.ds.foundation.theme.AuraTheme

private val DefaultIconSize = 24.dp

/**
 * Aura icon primitive for [ImageVector] glyphs backed by [AuraTheme].
 *
 * Defaults [tint] to [LocalAuraContentColor], falling back to `AuraTheme.colors.content.primary`.
 * Pass `Color.Unspecified` only for multi-color assets.
 */
@Composable
fun AuraIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalAuraContentColor.current.takeOrElse { AuraTheme.colors.content.primary },
) {
    AuraIcon(
        painter = rememberVectorPainter(imageVector),
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint,
    )
}

/**
 * Aura icon primitive for [Painter] assets backed by [AuraTheme].
 */
@Composable
fun AuraIcon(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalAuraContentColor.current.takeOrElse { AuraTheme.colors.content.primary },
) {
    val colorFilter = if (tint == Color.Unspecified) null else ColorFilter.tint(tint)
    val sizingModifier = if (
        painter.intrinsicSize == Size.Unspecified ||
        painter.intrinsicSize.width.isInfinite() ||
        painter.intrinsicSize.height.isInfinite()
    ) {
        modifier.size(DefaultIconSize)
    } else {
        modifier
    }

    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = sizingModifier,
        contentScale = ContentScale.Fit,
        colorFilter = colorFilter,
    )
}
