---
name: aura-design-system
description: Integrates the Aura Design System (com.example.ds) into Android Jetpack Compose consumer apps with zero Material 3 usage, semantic tokens (AuraColors, AuraTypography, AuraLayout, AuraMotion, AuraElevation), dual-track Standard/Expressive physics, and Solid/Glass/LiquidGlass materiality with accessibility fallbacks.
---

# Aura Design System — Consumer App Integration Skill

Use this skill whenever building, refactoring, or "vibe-coding" Android Jetpack Compose UI in an app that consumes the **Aura Design System** (`com.example.ds`).

> **Important Distinction:** Aura (`AuraTheme`, `AuraMotionProfile.Standard/Expressive`, `AuraMaterial.Solid/Glass/LiquidGlass`, `AuraElevation`) is an **Aura-original design system**. Never mix it with `androidx.compose.material3` (`MaterialTheme`, `Button`, `Text`, `Scaffold`, `Surface`, `Card`). Note: `androidx.compose.material.icons.*` vector glyphs are acceptable inside `AuraIcon`, `FloatingBottomIsland`, etc., as long as no `material3` widgets, themes, or tokens are used.

---

## 1. Non-Negotiable Consumer Rules (`EVAL-001` – `EVAL-008`)

Every screen and custom component you write in a consumer app **must** follow these rules:

1. **Zero Material 3:** Never import `androidx.compose.material3.*` or call `MaterialTheme`. Always use `AuraText`, `AuraIcon`, `AuraScaffold` / `FloatingGlassScaffold`, `AuraCard`, `AuraButton`, etc.
2. **Tokens Only — Zero Hardcoded Literals:**
   - **Colors:** Use `AuraTheme.colors.*` (and `LocalAuraContentColor.current`). Never hardcode `Color(0x...)` in screens or components.
   - **Typography:** Use `AuraTheme.typography.*` with `AuraText`. Never hardcode `.sp` font sizes in UI.
   - **Layout & Spacing:** Use `AuraTheme.layout.*` (`screenMarginHorizontal`, `screenMarginVertical`, `sectionSpacing`, `gridColumns`, `gridGutter`, `maxContentWidth`, `supportsTwoPane`, `primaryPaneFraction`, `topBarHeight`) alongside structured `dp` component rhythm.
   - **Motion (`MOT-RULE-002`):** Use `AuraTheme.motion.*` (`pressInSpring()`, `pressOutSpring()`, `spatialSpring()`, `elasticStretchSpring()`, `enterSpec()`, `exitSpec()`, `staggerDelay(index)`). Never write raw numeric `tween(300)` or `spring(dampingRatio = ..., stiffness = ...)`.
   - **Depth & Scrims (`VIS-RULE-005`):** Use `AuraTheme.elevation.level0`–`level4` (`elevation`, `keyShadowAlpha`, `ambientShadowAlpha`, `scrimAlpha`).
3. **4-Layer Visual & Material Discipline (`VIS-RULE-001` – `VIS-RULE-006`):**
   - **Layer 0 (`canvas`):** `Solid` — `AuraScaffold`, `AuraAdaptiveTwoPane`, `FloatingGlassScaffold` scroll canvas.
   - **Layer 1 (`content`):** `Solid` — cards, list rows, forms, switches, text fields (`Glass` is forbidden on inline list/form items).
   - **Layer 2 (`chrome`):** `Solid`, `Glass`, or `LiquidGlass` (`<= 1` hero) — `AuraTopAppBar`, `FloatingTopPillBar`, `AuraBottomIsland`, `FloatingBottomIsland`, `AuraSearchBar`, `AuraFab`, floating icon buttons.
   - **Layer 3 (`spatial`):** `Solid`, `Glass`, or `LiquidGlass` (`<= 1` hero) — `AuraDialog`, `MorphingMorePopover`.
   - Respect performance & fallback budgets: **`<= 3` live-blur surfaces per viewport**, **`<= 1` `LiquidGlass` hero per screen**, and never fight `AuraTheme.reduceTransparency`.
4. **Dual-Track Motion (`MOT-RULE-003`, `MOT-RULE-007`):**
   - Keep `AuraTheme(motionProfile = AuraMotionProfile.Standard)` at your app root for forms, inputs, and lists.
   - Wrap only focal spatial chrome (`FloatingBottomIsland`, `AuraDialog`, `AuraFab`) in a nested `AuraTheme(motionProfile = AuraMotionProfile.Expressive)` when desired (**at most 1 Expressive hero per screen**).
   - Always honor `AuraTheme.motion.reduceMotion` / `rememberAuraReducedMotion()` in any custom animation.
5. **App-Specific Gap Components:**
   - When the app needs UI not yet in Aura (e.g., Snackbar, Divider, ProgressBar, Charts, Domain Badges), create an app-prefixed composable built exclusively on `AuraTheme.*`, `AuraCard`, `AuraText`, `AuraIcon`, and `AuraButton`.

---

## 2. Quick Consumer Setup

### Option A: Local AAR (Recommended for Workspace Sync)
1. Place `aura-design-system-1.0.0.aar` in `<app-module>/libs/`.
2. In `<app-module>/build.gradle.kts`:
   ```kotlin
   dependencies {
       implementation(files("libs/aura-design-system-1.0.0.aar"))
   }
   ```
3. Or run the helper script from `aura-design-system` to build & sync both the AAR and this skill kit:
   ```powershell
   powershell -ExecutionPolicy Bypass -File scripts/install-aura-to-app.ps1 -TargetAppDir C:\path\to\your-app
   ```

### Option B: JitPack
```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}

// app/build.gradle.kts
dependencies {
    implementation("com.github.bluemarlins:aura-design-system:1.0.0")
}
```

### Root Theme Entry Point
```kotlin
AuraTheme(
    colorMode = if (isSystemInDarkTheme()) AuraColorMode.Dark else AuraColorMode.Light,
    material = AuraMaterial.Solid,
    motionProfile = AuraMotionProfile.Standard,
) {
    // App navigation & screens
}
```

---

## 3. Component Selection Quick Map

All components live in `package com.example.ds.component`. Choose the closest Aura component instead of writing raw Foundation boxes:

| Category | Aura Components |
|---|---|
| **Primitives** | `AuraText`, `AuraIcon`, `ProvideAuraContentColorTextStyle` |
| **Layout & Adaptation** | `AuraScaffold`, `AuraAdaptiveTwoPane`, `AuraAdaptiveGrid` |
| **Navigation Chrome** | `AuraTopAppBar`, `FloatingTopPillBar`, `AuraBottomIsland`, `FloatingBottomIsland`, `MorphingMorePopover` |
| **Spatial Glass** | `FloatingGlassScaffold`, `GlassSurface`, `rememberGlassBackdropState`, `Modifier.glassBackdropSource`, `rememberFloatingScrollProgress`, `calculateBottomIslandContentPadding`, `LiquidMetaballBridge` |
| **Actions** | `AuraButton`, `AuraIconButton`, `AuraFab`, `AuraSegmentedButton`, `AuraChipButton` |
| **Inputs & Selection** | `AuraTextField`, `AuraNumberField`, `AuraSearchBar`, `AuraDateTimePicker`, `AuraSwitch`, `AuraCheckbox`, `AuraRadio`, `AuraSlider`, `AuraRatingBar` |
| **Containment & Feedback** | `AuraCard`, `AuraDialog`, `AuraBadge`, `AuraBadgedBox` |

---

## 4. Deep-Dive Reference Files (Load On Demand)

Read only what the current screen or component task needs:

- **[Tokens, Typography, Layout, Motion, Elevation & Theme](references/tokens-and-theme.md)** — Exact `AuraTheme.*` accessors, all 16 typography roles, `Smallest Width × Display Ratio` responsive tokens, dual-track `AuraMotion` properties/methods, `AuraElevation`, and `AuraMaterial` resolution.
- **[Complete Kotlin Component API Catalog](references/components-api.md)** — Exact `@Composable` signatures, enum variants, defaults, and deprecation notes (`GlassStyle` -> `AuraMaterial`) for all 21 Ready components and spatial glass utilities.
- **[Motion, Layer & Visual Interaction Rules](references/motion-and-visual-rules.md)** — Z-layer boundaries (`VIS-RULE-001`–`007`), hardware/accessibility degradation matrix, `Standard` vs. `Expressive` placement, stagger choreography (`MOT-RULE-001`–`007`), and `EVAL-001`–`EVAL-008` checklist.
- **[Production Screen & Gap-Component Recipes](references/recipes-and-patterns.md)** — Copy-ready Compose templates for `FloatingGlassScaffold` home feeds, `AuraScaffold` detail/form screens with `maxContentWidth`, `AuraAdaptiveTwoPane` master-detail layouts, `AuraDialog`, and compliant app-prefixed gap components.

---

## 5. Auditing Your Consumer App

After writing or editing Compose screens, run the verification script against your app repository to catch `material3` leaks, hardcoded `Color(0x...)` literals, `.sp` literals, or raw `tween`/`spring` animations:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/verify-aura-app.ps1 -TargetAppDir C:\path\to\your-app -FailOnViolation
```

See [scripts/verify-aura-app.ps1](scripts/verify-aura-app.ps1) and [scripts/install-aura-to-app.ps1](scripts/install-aura-to-app.ps1) for options.
