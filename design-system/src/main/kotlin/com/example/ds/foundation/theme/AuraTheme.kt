package com.example.ds.foundation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.ds.component.LocalAuraContentColor
import com.example.ds.component.LocalAuraTextStyle
import com.example.ds.foundation.color.AuraColorSchemes
import com.example.ds.foundation.color.AuraColors
import com.example.ds.foundation.layout.AuraLayout
import com.example.ds.foundation.layout.AuraLayoutDefaults
import com.example.ds.foundation.layout.AuraWindowInfo
import com.example.ds.foundation.layout.rememberAuraWindowInfo
import com.example.ds.foundation.motion.AuraMotion
import com.example.ds.foundation.motion.AuraMotionDefaults
import com.example.ds.foundation.typography.AuraTypography
import com.example.ds.foundation.typography.AuraTypographyDefaults

/**
 * Color axis for [AuraTheme].
 */
enum class AuraColorMode {
    Light,
    Dark,
}

/**
 * Surface material axis for [AuraTheme].
 *
 * Skeleton starts with [Solid]. Future releases will extend this axis with
 * `Glass` (blur + tint) and `LiquidGlass` (blur + refraction + physics) without
 * altering semantic color roles.
 */
enum class AuraMaterial {
    Solid,
}

private val LocalAuraColors = staticCompositionLocalOf { AuraColorSchemes.Light }
private val LocalAuraMotion = staticCompositionLocalOf { AuraMotionDefaults.Standard }
private val LocalAuraColorMode = staticCompositionLocalOf { AuraColorMode.Light }
private val LocalAuraMaterial = staticCompositionLocalOf { AuraMaterial.Solid }
private val LocalAuraWindowInfo = staticCompositionLocalOf {
    AuraWindowInfo.fromDimensions(smallestWidthDp = 360, widthDp = 360, heightDp = 800)
}
private val LocalAuraLayout = staticCompositionLocalOf { AuraLayoutDefaults.Compact }
private val LocalAuraTypography = staticCompositionLocalOf { AuraTypographyDefaults.Standard }

/**
 * Root theme provider for the Aura design system.
 *
 * Uses two independent styling axes ([colorMode] and [material]) alongside responsive
 * [windowInfo]/[layout] breakpoints, [typography] roles, and [motion] interaction physics.
 */
@Composable
fun AuraTheme(
    colorMode: AuraColorMode = if (isSystemInDarkTheme()) AuraColorMode.Dark else AuraColorMode.Light,
    material: AuraMaterial = AuraMaterial.Solid,
    motion: AuraMotion = AuraMotionDefaults.Standard,
    windowInfo: AuraWindowInfo = rememberAuraWindowInfo(),
    layout: AuraLayout = AuraLayoutDefaults.fromWindowInfo(windowInfo),
    typography: AuraTypography = AuraTypographyDefaults.scaled(layout.typographyScale),
    content: @Composable () -> Unit,
) {
    val colors = when (colorMode) {
        AuraColorMode.Light -> AuraColorSchemes.Light
        AuraColorMode.Dark -> AuraColorSchemes.Dark
    }

    CompositionLocalProvider(
        LocalAuraColors provides colors,
        LocalAuraMotion provides motion,
        LocalAuraColorMode provides colorMode,
        LocalAuraMaterial provides material,
        LocalAuraWindowInfo provides windowInfo,
        LocalAuraLayout provides layout,
        LocalAuraTypography provides typography,
        LocalAuraContentColor provides colors.content.primary,
        LocalAuraTextStyle provides typography.bodyMedium,
        content = content,
    )
}

object AuraTheme {
    val colors: AuraColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAuraColors.current

    val typography: AuraTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalAuraTypography.current

    val motion: AuraMotion
        @Composable
        @ReadOnlyComposable
        get() = LocalAuraMotion.current

    val window: AuraWindowInfo
        @Composable
        @ReadOnlyComposable
        get() = LocalAuraWindowInfo.current

    val layout: AuraLayout
        @Composable
        @ReadOnlyComposable
        get() = LocalAuraLayout.current

    val colorMode: AuraColorMode
        @Composable
        @ReadOnlyComposable
        get() = LocalAuraColorMode.current

    val material: AuraMaterial
        @Composable
        @ReadOnlyComposable
        get() = LocalAuraMaterial.current
}
