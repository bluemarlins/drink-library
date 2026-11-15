package com.example.ds.foundation.typography

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Tier 2 — Semantic typography roles for Aura.
 *
 * Consumed exclusively via `AuraTheme.typography`. Display, Headline, and Title styles
 * respond to `AuraTheme.layout.typographyScale`, while Body, Label, and Mono roles
 * maintain crisp baseline readability across breakpoints.
 */
@Immutable
data class AuraTypography(
    val displayLarge: TextStyle,
    val displayMedium: TextStyle,
    val headlineLarge: TextStyle,
    val headlineMedium: TextStyle,
    val headlineSmall: TextStyle,
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val titleSmall: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
    val labelLarge: TextStyle,
    val labelMedium: TextStyle,
    val labelSmall: TextStyle,
    val monoMedium: TextStyle,
    val monoSmall: TextStyle,
)

object AuraTypographyDefaults {
    val Standard: AuraTypography = scaled(1.0f)

    fun scaled(scale: Float): AuraTypography {
        val factor = scale.coerceIn(0.85f, 1.25f)
        fun TextUnit.scaleSp(): TextUnit = (this.value * factor).sp

        return AuraTypography(
            displayLarge = TextStyle(
                fontSize = 36.sp.scaleSp(),
                lineHeight = 44.sp.scaleSp(),
                fontWeight = FontWeight.Bold,
            ),
            displayMedium = TextStyle(
                fontSize = 28.sp.scaleSp(),
                lineHeight = 36.sp.scaleSp(),
                fontWeight = FontWeight.Bold,
            ),
            headlineLarge = TextStyle(
                fontSize = 24.sp.scaleSp(),
                lineHeight = 32.sp.scaleSp(),
                fontWeight = FontWeight.SemiBold,
            ),
            headlineMedium = TextStyle(
                fontSize = 20.sp.scaleSp(),
                lineHeight = 28.sp.scaleSp(),
                fontWeight = FontWeight.SemiBold,
            ),
            headlineSmall = TextStyle(
                fontSize = 18.sp.scaleSp(),
                lineHeight = 26.sp.scaleSp(),
                fontWeight = FontWeight.SemiBold,
            ),
            titleLarge = TextStyle(
                fontSize = 18.sp.scaleSp(),
                lineHeight = 24.sp.scaleSp(),
                fontWeight = FontWeight.SemiBold,
            ),
            titleMedium = TextStyle(
                fontSize = 16.sp.scaleSp(),
                lineHeight = 22.sp.scaleSp(),
                fontWeight = FontWeight.SemiBold,
            ),
            titleSmall = TextStyle(
                fontSize = 14.sp.scaleSp(),
                lineHeight = 20.sp.scaleSp(),
                fontWeight = FontWeight.SemiBold,
            ),
            bodyLarge = TextStyle(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal,
            ),
            bodyMedium = TextStyle(
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Normal,
            ),
            bodySmall = TextStyle(
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Normal,
            ),
            labelLarge = TextStyle(
                fontSize = 15.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            labelMedium = TextStyle(
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium,
            ),
            labelSmall = TextStyle(
                fontSize = 11.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
            ),
            monoMedium = TextStyle(
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                fontFeatureSettings = "tnum",
            ),
            monoSmall = TextStyle(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                fontFeatureSettings = "tnum",
            ),
        )
    }
}
