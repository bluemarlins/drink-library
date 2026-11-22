---
name: aura-design-system
description: Applies the Aura Design System (io.github.bluemarlins.aura, github.com/bluemarlins/aura-design-system) to Android Jetpack Compose apps. Use when the user asks to apply, adopt, install, or migrate to Aura ("apply Aura Design System to my app"), to replace Material 3 UI with Aura, or to build or edit any Compose screen in an app that already depends on Aura. Covers Gradle setup, the AuraTheme root, Material 3 to Aura migration, semantic tokens (colors, typography, layout, motion, elevation), Solid/Glass/LiquidGlass materiality, and a compliance audit.
---

# Aura Design System — Consumer App Integration Skill

Use this skill whenever applying the **Aura Design System** (`io.github.bluemarlins.aura`) to an Android app, or building, refactoring, or "vibe-coding" Jetpack Compose UI in an app that consumes it.

> **Important Distinction:** Aura (`AuraTheme`, `AuraMotionProfile.Standard/Expressive`, `AuraMaterial.Solid/Glass/LiquidGlass`, `AuraElevation`) is an **Aura-original design system**. Never mix it with `androidx.compose.material3` (`MaterialTheme`, `Button`, `Text`, `Scaffold`, `Surface`, `Card`). Note: `androidx.compose.material.icons.*` vector glyphs are acceptable inside `AuraIcon`, `FloatingBottomIsland`, etc., as long as no `material3` widgets, themes, or tokens are used.

---

## 1. Applying Aura to an App (Start Here)

When the user asks to apply Aura to their app, work through these steps in order. If the app already depends on Aura, skip to step 4.

1. **Inspect the app.** Find the app module (usually `app/`), its `build.gradle.kts`, the root `settings.gradle.kts`, the existing root theme composable, and every file importing `androidx.compose.material3`. Confirm the requirements in §2; if one is not met (for example `minSdk < 26`), tell the user before changing anything.
2. **Add the dependency** (§2). Prefer JitPack. Sync or build once to confirm it resolves before touching UI code.
3. **Wrap the root in `AuraTheme`** (§2, "Root Theme Entry Point"), replacing the app's `MaterialTheme` wrapper.
4. **Migrate screens one at a time.** Replace each Material 3 widget with its Aura counterpart from §4, read exact signatures from [references/components-api.md](references/components-api.md), and replace hardcoded colors, font sizes, and animation specs with `AuraTheme.*` tokens. For a large app, agree on the screen order with the user first.
5. **Remove Material 3** once no file imports it: delete the `androidx.compose.material3` dependency and any leftover M3 theme files (`Color.kt`, `Type.kt`, `Theme.kt`).
6. **Verify.** Build the app (`./gradlew :app:assembleDebug`), then run the audit in §6 and fix every `ERROR`.

---

## 2. Consumer Setup

### Requirements

- `minSdk` 26+, `compileSdk` 35+, JDK 17
- Kotlin 2.0+ with the Compose compiler plugin (`org.jetbrains.kotlin.plugin.compose`) and `buildFeatures { compose = true }`. Aura itself is built with Kotlin 2.1.20 and Compose BOM `2025.02.00`.

### Option A: JitPack (Recommended)
```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

// app/build.gradle.kts
dependencies {
    implementation("com.github.bluemarlins:aura-design-system:2.1.0")
    // Only if the app uses Icons.* glyphs. Aura does not export an icon set.
    implementation("androidx.compose.material:material-icons-core")
}
```
Compose `ui`, `ui-graphics`, `foundation`, and the Compose BOM come in transitively. If the version fails to resolve, check which tags are published at <https://github.com/bluemarlins/aura-design-system/releases> and tell the user rather than guessing another version.

### Option B: Local AAR
A local AAR carries no transitive dependencies, so declare Compose explicitly:
```kotlin
// app/build.gradle.kts
dependencies {
    implementation(files("libs/aura-design-system-2.1.0.aar"))

    implementation(platform("androidx.compose:compose-bom:2025.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.compose.material:material-icons-core") // only for Icons.* glyphs
}
```
Get the AAR from the GitHub Releases page, or build and copy it from a local clone of the Aura repository (Windows PowerShell; this only works from that clone, not from an installed copy of this skill):
```powershell
powershell -ExecutionPolicy Bypass -File <aura-repo>\skills\aura-design-system\scripts\install-aura-to-app.ps1 -TargetAppDir C:\path\to\your-app
```

### Root Theme Entry Point
```kotlin
AuraTheme(
    colorMode = if (isSystemInDarkTheme()) AuraColorMode.Dark else AuraColorMode.Light,
    motionProfile = AuraMotionProfile.Standard,
) {
    // App navigation & screens
}
```

`AuraTheme(material = ...)` is a **ceiling**, not a default: no surface below it can render a richer material than the theme allows. Leave it at its default (`AuraMaterial.LiquidGlass`) so floating chrome can use `Glass` / `LiquidGlass`. Content surfaces (cards, fields, lists) stay `Solid` regardless. Set `material = AuraMaterial.Glass` or `AuraMaterial.Solid` only when the user wants to cap or disable glass app-wide; a `Solid` root silently turns every `GlassStyle.LIQUID` / `FROSTED` surface opaque.

---

## 3. Non-Negotiable Consumer Rules (`EVAL-001` – `EVAL-008`)

Every screen and custom component you write in a consumer app **must** follow these rules:

1. **Zero Material 3:** Never import `androidx.compose.material3.*` or call `MaterialTheme`. Always use `AuraText`, `AuraIcon`, `AuraScaffold` / `FloatingGlassScaffold`, `AuraCard`, `AuraButton`, etc.
2. **Tokens Only — Zero Hardcoded Literals:**
   - **Colors:** Use `AuraTheme.colors.*` (and `LocalAuraContentColor.current`). Never hardcode `Color(0x...)` in screens or components.
   - **Typography:** Use `AuraTheme.typography.*` with `AuraText`. Never hardcode `.sp` font sizes in UI.
   - **Layout & Spacing:** Use `AuraTheme.layout.*` (`screenMarginHorizontal`, `screenMarginVertical`, `sectionSpacing`, `gridColumns`, `gridGutter`, `maxContentWidth`, `supportsTwoPane`, `primaryPaneFraction`, `topBarHeight`) alongside structured `dp` component rhythm.
   - **Motion (`MOT-RULE-002`):** Use `AuraTheme.motion.*` (`pressInSpring()`, `pressOutSpring()` for press release, `selectionSpring()` for selection travel, `spatialSpring()`, `elasticStretchSpring()`, `enterSpec()`, `exitSpec()`, `staggerDelay(index)`). Never write raw numeric `tween(300)` or `spring(dampingRatio = ..., stiffness = ...)`.
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
   - Check §4 first; most common widgets already exist (including `AuraDivider`, `AuraLinearProgressBar`, `AuraSnackbar`, `AuraBadge`).
   - Only when the app needs UI that Aura does not provide (e.g., charts, bottom sheets, dropdown menus, domain-specific tiles), create an app-prefixed composable built exclusively on `AuraTheme.*`, `AuraCard`, `AuraText`, `AuraIcon`, and `AuraButton`.

---

## 4. Component Selection Quick Map (Material 3 → Aura)

All components live in `package io.github.bluemarlins.aura.component`. Choose the closest Aura component instead of writing raw Foundation boxes:

| Category | Aura Components | Replaces Material 3 |
|---|---|---|
| **Primitives** | `AuraText`, `AuraIcon`, `ProvideAuraContentColorTextStyle` | `Text`, `Icon`, `LocalContentColor` |
| **Layout & Adaptation** | `AuraScaffold`, `AuraAdaptiveTwoPane`, `AuraAdaptiveGrid` | `Scaffold`, `Surface` (as screen root) |
| **Navigation Chrome** | `AuraTopAppBar`, `FloatingTopPillBar`, `AuraBottomIsland`, `FloatingBottomIsland`, `MorphingMorePopover` | `TopAppBar`, `NavigationBar`, overflow `DropdownMenu` |
| **Spatial Glass** | `FloatingGlassScaffold`, `GlassSurface`, `rememberGlassBackdropState`, `Modifier.glassBackdropSource`, `rememberFloatingScrollProgress`, `calculateBottomIslandContentPadding`, `LiquidMetaballBridge` | — |
| **Actions** | `AuraButton`, `AuraIconButton`, `AuraFab`, `AuraSegmentedButton`, `AuraChipButton` | `Button` / `OutlinedButton` / `TextButton`, `IconButton`, `FloatingActionButton`, `SegmentedButton`, `FilterChip` / `AssistChip` |
| **Inputs & Selection** | `AuraTextField`, `AuraNumberField`, `AuraSearchBar`, `AuraDateTimePicker`, `AuraSwitch`, `AuraCheckbox`, `AuraRadio`, `AuraSlider`, `AuraRatingBar` | `TextField` / `OutlinedTextField`, `SearchBar`, `DatePicker`, `Switch`, `Checkbox`, `RadioButton`, `Slider` |
| **Containment & Feedback** | `AuraCard`, `AuraDialog`, `AuraDivider`, `AuraBadge`, `AuraBadgedBox`, `AuraLinearProgressBar`, `AuraStepProgressBar`, `AuraSnackbar`, `AuraSnackbarHost` | `Card`, `AlertDialog`, `HorizontalDivider`, `Badge` / `BadgedBox`, `LinearProgressIndicator`, `Snackbar` / `SnackbarHost` |

---

## 5. Deep-Dive Reference Files (Load On Demand)

Read only what the current screen or component task needs:

- **[Tokens, Typography, Layout, Motion, Elevation & Theme](references/tokens-and-theme.md)** — Exact `AuraTheme.*` accessors (`colors`, `typography`, `shapes`, `layout`, `motion`, `elevation`, `glass`), 16 typography roles, `Smallest Width × Display Ratio` responsive tokens, dual-track `AuraMotion` properties/methods, `AuraElevation`, and `AuraMaterial` resolution.
- **[Complete Kotlin Component API Catalog](references/components-api.md)** — Exact `@Composable` signatures, enum variants, defaults, and deprecation notes (`GlassStyle` -> `AuraMaterial`) for every Ready component and the spatial glass utilities.
- **[Motion, Layer & Visual Interaction Rules](references/motion-and-visual-rules.md)** — Z-layer boundaries (`VIS-RULE-001`–`007`), hardware/accessibility degradation matrix, `Standard` vs. `Expressive` placement, stagger choreography (`MOT-RULE-001`–`007`), and `EVAL-001`–`EVAL-008` checklist.
- **[Production Screen & Gap-Component Recipes](references/recipes-and-patterns.md)** — Copy-ready Compose templates for `FloatingGlassScaffold` home feeds, `AuraScaffold` detail/form screens with `maxContentWidth`, `AuraAdaptiveTwoPane` master-detail layouts, `AuraDialog`, and compliant app-prefixed gap components.

---

## 6. Auditing the Consumer App

After writing or editing Compose screens, audit the app for `material3` leaks, hardcoded `Color(0x...)` literals, `.sp` literals, and raw `tween`/`spring` animations.

On Windows (or anywhere `pwsh` is installed), run the script bundled with this skill. Its path is relative to this skill's directory:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/verify-aura-app.ps1 -TargetAppDir C:\path\to\your-app -FailOnViolation
```

It scans `app\src\main` by default; for other or additional modules pass `-SourceDirs "feature\home\src\main","core\ui\src\main"`.

Where PowerShell is unavailable, search the app's Kotlin sources for the same four patterns directly: `androidx.compose.material3.`, `Color(0x`, numeric `.sp` literals, and `tween(` / `spring(` calls with numeric arguments.
