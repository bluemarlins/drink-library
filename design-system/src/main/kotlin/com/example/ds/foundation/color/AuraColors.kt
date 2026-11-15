package com.example.ds.foundation.color

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Tier 2 — Aura semantic color roles.
 *
 * Components consume colors exclusively through [AuraColors] via `AuraTheme.colors`.
 * Figma mapping: Variable collection `Color` with `Light` / `Dark` modes
 * (e.g., `surface/floating`, `brand/primary`).
 */
@Immutable
data class AuraColors(
    val isDark: Boolean,
    val background: Background,
    val surface: Surface,
    val content: Content,
    val brand: Brand,
    val border: Border,
    val status: Status,
    val interaction: Interaction,
) {
    @Immutable
    data class Background(
        val primary: Color,
        val secondary: Color,
    )

    @Immutable
    data class Surface(
        val default: Color,
        val raised: Color,
        val sunken: Color,
        /** Elevated overlay layer (bottom bars, sheets, popovers). Primary target for future Glass / LiquidGlass materials. */
        val floating: Color,
        val inverse: Color,
    )

    @Immutable
    data class Content(
        val primary: Color,
        val secondary: Color,
        val tertiary: Color,
        val disabled: Color,
        val inverse: Color,
    )

    @Immutable
    data class Brand(
        val primary: Color,
        val onPrimary: Color,
        val primaryPressed: Color,
        val subtle: Color,
        val onSubtle: Color,
    )

    @Immutable
    data class Border(
        val subtle: Color,
        val default: Color,
        val strong: Color,
        val focus: Color,
    )

    @Immutable
    data class StatusRole(
        val solid: Color,
        val onSolid: Color,
        val subtle: Color,
        val onSubtle: Color,
    )

    @Immutable
    data class Status(
        val error: StatusRole,
        val success: StatusRole,
        val warning: StatusRole,
        val info: StatusRole,
    )

    @Immutable
    data class Interaction(
        val pressedOverlay: Color,
        val selectedContainer: Color,
        val onSelected: Color,
        val disabledContainer: Color,
        val scrim: Color,
    )
}
