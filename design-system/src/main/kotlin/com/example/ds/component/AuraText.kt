package com.example.ds.component

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.ds.foundation.theme.AuraTheme

/**
 * Preferred content tint color propagated by Aura containers (e.g., [AuraScaffold], [AuraButton]).
 */
val LocalAuraContentColor = compositionLocalOf { Color.Unspecified }

/**
 * Preferred baseline text style propagated by Aura containers.
 */
val LocalAuraTextStyle = compositionLocalOf { TextStyle.Default }

/**
 * Scopes both [LocalAuraContentColor] and [LocalAuraTextStyle] for descendants such as
 * [AuraText] and [AuraIcon].
 */
@Composable
fun ProvideAuraContentColorTextStyle(
    contentColor: Color,
    textStyle: TextStyle = LocalAuraTextStyle.current,
    content: @Composable () -> Unit,
) {
    val mergedStyle = LocalAuraTextStyle.current.merge(textStyle)
    CompositionLocalProvider(
        LocalAuraContentColor provides contentColor,
        LocalAuraTextStyle provides mergedStyle,
        content = content,
    )
}

/**
 * Aura text primitive backed by Compose Foundation [BasicText] and [AuraTheme].
 *
 * Resolves color in precedence order: explicit [color] -> `style.color` ->
 * [LocalAuraContentColor] -> `AuraTheme.colors.content.primary`.
 */
@Composable
fun AuraText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalAuraTextStyle.current,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
) {
    val fallbackColor = AuraTheme.colors.content.primary
    val resolvedColor = color.takeOrElse {
        style.color.takeOrElse {
            LocalAuraContentColor.current.takeOrElse { fallbackColor }
        }
    }
    val baseStyle = LocalAuraTextStyle.current.merge(style)
    val mergedStyle = if (textAlign != null) {
        baseStyle.copy(color = resolvedColor, textAlign = textAlign)
    } else {
        baseStyle.copy(color = resolvedColor)
    }

    BasicText(
        text = text,
        modifier = modifier,
        style = mergedStyle,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
    )
}
