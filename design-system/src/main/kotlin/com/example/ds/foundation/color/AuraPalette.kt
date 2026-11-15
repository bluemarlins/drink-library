package com.example.ds.foundation.color

import androidx.compose.ui.graphics.Color

/**
 * Tier 1 — Aura primitive color palette.
 *
 * Raw color values without semantic meaning. Components must not reference
 * [AuraPalette] directly; use semantic roles from [AuraColors] via `AuraTheme.colors`.
 *
 * Figma mapping: Variable collection `Primitives` (e.g., `neutral/50`, `blue/600`).
 */
object AuraPalette {
    // Neutral (Cool Gray — One UI-aligned canvas & OLED-friendly dark scale)
    val Neutral0 = Color(0xFFFFFFFF)
    val Neutral50 = Color(0xFFF2F3F7)
    val Neutral100 = Color(0xFFE8EAEF)
    val Neutral200 = Color(0xFFD4D8E0)
    val Neutral300 = Color(0xFFB4BAC6)
    val Neutral400 = Color(0xFF8E95A2)
    val Neutral500 = Color(0xFF636B78)
    val Neutral600 = Color(0xFF4A515C)
    val Neutral700 = Color(0xFF31363F)
    val Neutral800 = Color(0xFF282C35)
    val Neutral850 = Color(0xFF1E2128)
    val Neutral900 = Color(0xFF17191E)
    val Neutral950 = Color(0xFF0E1014)
    val Neutral1000 = Color(0xFF000000)

    // Brand Blue (One UI Blue aligned)
    val Blue50 = Color(0xFFEDF5FF)
    val Blue100 = Color(0xFFD6E9FF)
    val Blue200 = Color(0xFFADCFFF)
    val Blue300 = Color(0xFF7AB4FF)
    val Blue400 = Color(0xFF3E95FF)
    val Blue500 = Color(0xFF0381FE)
    val Blue600 = Color(0xFF006FE0)
    val Blue700 = Color(0xFF0058B8)
    val Blue800 = Color(0xFF003F87)
    val Blue900 = Color(0xFF0B2545)
    val Blue950 = Color(0xFF041326)

    // Red (Error)
    val Red50 = Color(0xFFFEF2F2)
    val Red100 = Color(0xFFFEE2E2)
    val Red400 = Color(0xFFF87171)
    val Red600 = Color(0xFFDC2626)
    val Red800 = Color(0xFF991B1B)
    val Red900 = Color(0xFF450F12)

    // Green (Success)
    val Green50 = Color(0xFFF0FDF4)
    val Green100 = Color(0xFFDCFCE7)
    val Green400 = Color(0xFF4ADE80)
    val Green700 = Color(0xFF15803D)
    val Green800 = Color(0xFF166534)
    val Green900 = Color(0xFF0C2E1B)

    // Amber (Warning)
    val Amber50 = Color(0xFFFFFBEB)
    val Amber100 = Color(0xFFFEF3C7)
    val Amber400 = Color(0xFFFBBF24)
    val Amber500 = Color(0xFFF59E0B)
    val Amber800 = Color(0xFF92400E)
    val Amber900 = Color(0xFF3D2607)

    // Cyan (Info — visually separated from Brand Blue)
    val Cyan50 = Color(0xFFECFEFF)
    val Cyan100 = Color(0xFFCFFAFE)
    val Cyan400 = Color(0xFF22D3EE)
    val Cyan700 = Color(0xFF0E7490)
    val Cyan800 = Color(0xFF155E75)
    val Cyan900 = Color(0xFF0B2C38)
}
