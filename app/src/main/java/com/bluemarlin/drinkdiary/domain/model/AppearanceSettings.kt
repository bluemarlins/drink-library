package com.bluemarlin.drinkdiary.domain.model

// 화면 재질. Aura의 AuraMaterial(Solid / Glass / LiquidGlass)에 1:1로 대응하지만,
// domain은 UI 라이브러리를 몰라야 하므로 따로 둔다.
enum class SurfaceStyle {
    Solid,
    GlassMorphism,
    LiquidGlass,
}

enum class ColorModePreference {
    System,
    Light,
    Dark,
}

data class AppearanceSettings(
    val surfaceStyle: SurfaceStyle = SurfaceStyle.LiquidGlass,
    val colorMode: ColorModePreference = ColorModePreference.System,
)
