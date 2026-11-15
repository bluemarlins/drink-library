package com.example.ds.foundation.layout

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.platform.LocalConfiguration

/**
 * Width classification along Aura's primary size axis (Smallest Width & Viewport Width).
 */
enum class AuraWidthClass {
    ExtraSmall,
    Compact,
    Medium,
    Expanded,
    ;

    companion object {
        fun fromDp(dp: Int): AuraWidthClass = when {
            dp < 360 -> ExtraSmall
            dp < 600 -> Compact
            dp < 840 -> Medium
            else -> Expanded
        }
    }
}

/**
 * Vertical height classification used to protect usable viewport space on landscape phones.
 */
enum class AuraHeightClass {
    Compact,
    Medium,
    Expanded,
    ;

    companion object {
        fun fromDp(dp: Int): AuraHeightClass = when {
            dp < 480 -> Compact
            dp < 900 -> Medium
            else -> Expanded
        }
    }
}

/**
 * Display aspect ratio classification (`widthDp / heightDp`) along Aura's secondary axis.
 */
enum class AuraDisplayRatio {
    /** Narrow elongated displays (`ratio < 0.50`, e.g., foldable cover displays). */
    UltraTall,

    /** Standard upright portrait (`0.50 <= ratio < 0.85`). */
    Portrait,

    /** Nearly square aspect ratio (`0.85 <= ratio <= 1.20`, e.g., unfolded foldables). */
    Square,

    /** Standard landscape (`1.20 < ratio <= 1.85`, e.g., tablets). */
    Landscape,

    /** Wide elongated horizontal (`ratio > 1.85`, e.g., landscape phones). */
    UltraWide,
    ;

    companion object {
        fun fromRatio(ratio: Float): AuraDisplayRatio = when {
            ratio < 0.50f -> UltraTall
            ratio < 0.85f -> Portrait
            ratio <= 1.20f -> Square
            ratio <= 1.85f -> Landscape
            else -> UltraWide
        }
    }
}

/**
 * Immutable snapshot of physical and current window dimensions for responsive adaptation.
 */
@Immutable
data class AuraWindowInfo(
    val smallestWidthDp: Int,
    val widthDp: Int,
    val heightDp: Int,
    val smallestWidthClass: AuraWidthClass,
    val widthClass: AuraWidthClass,
    val heightClass: AuraHeightClass,
    val aspectRatio: Float,
    val displayRatio: AuraDisplayRatio,
) {
    companion object {
        fun fromDimensions(
            smallestWidthDp: Int,
            widthDp: Int,
            heightDp: Int,
        ): AuraWindowInfo {
            val safeSw = smallestWidthDp.coerceAtLeast(1)
            val safeW = widthDp.coerceAtLeast(1)
            val safeH = heightDp.coerceAtLeast(1)
            val ratio = safeW.toFloat() / safeH.toFloat()
            return AuraWindowInfo(
                smallestWidthDp = safeSw,
                widthDp = safeW,
                heightDp = safeH,
                smallestWidthClass = AuraWidthClass.fromDp(safeSw),
                widthClass = AuraWidthClass.fromDp(safeW),
                heightClass = AuraHeightClass.fromDp(safeH),
                aspectRatio = ratio,
                displayRatio = AuraDisplayRatio.fromRatio(ratio),
            )
        }
    }
}

/**
 * Computes [AuraWindowInfo] from the active Compose [LocalConfiguration].
 */
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun rememberAuraWindowInfo(): AuraWindowInfo {
    val configuration = LocalConfiguration.current
    val windowMinDp = minOf(configuration.screenWidthDp, configuration.screenHeightDp)
    val smallestWidthDp = if (configuration.smallestScreenWidthDp > 0) {
        minOf(configuration.smallestScreenWidthDp, windowMinDp)
    } else {
        windowMinDp
    }
    return AuraWindowInfo.fromDimensions(
        smallestWidthDp = smallestWidthDp,
        widthDp = configuration.screenWidthDp,
        heightDp = configuration.screenHeightDp,
    )
}
