# Aura Tokens, Theme & Accessors Reference

All theme tokens live under `com.example.ds.foundation.*` and are accessed through `AuraTheme` (`com.example.ds.foundation.theme.AuraTheme`).

---

## 1. Root Theme (`AuraTheme`)

```kotlin
package com.example.ds.foundation.theme

enum class AuraColorMode { Light, Dark }

enum class AuraMaterial {
    Solid,       // Opaque high-legibility surfaces (Layer 0/1 default + accessibility fallback)
    Glass,       // Live RenderEffect gaussian backdrop blur + tinted frost + specular rim (API 31+)
    LiquidGlass, // AGSL edge bevel refraction + chromatic dispersion + metaball physics (API 33+)
}

@Composable
fun AuraTheme(
    colorMode: AuraColorMode = if (isSystemInDarkTheme()) AuraColorMode.Dark else AuraColorMode.Light,
    material: AuraMaterial = AuraMaterial.Solid,
    motionProfile: AuraMotionProfile = AuraMotionProfile.Standard,
    reduceTransparency: Boolean = rememberAuraReduceTransparency(),
    motion: AuraMotion = AuraMotionDefaults.forProfile(motionProfile),
    elevation: AuraElevation = AuraElevationDefaults.Standard,
    windowInfo: AuraWindowInfo = rememberAuraWindowInfo(),
    layout: AuraLayout = AuraLayoutDefaults.fromWindowInfo(windowInfo),
    typography: AuraTypography = AuraTypographyDefaults.scaled(layout.typographyScale),
    shapes: AuraShapes = AuraShapesDefaults.Standard,
    content: @Composable () -> Unit,
)
```

### Read-Only Accessors (`object AuraTheme`)
- `AuraTheme.colors: AuraColors`
- `AuraTheme.typography: AuraTypography`
- `AuraTheme.shapes: AuraShapes` (`extraSmall = 4.dp`, `small = 8.dp`, `medium = 12.dp`, `large = 16.dp`, `extraLarge = 24.dp`, `full = CircleShape`)
- `AuraTheme.motion: AuraMotion`
- `AuraTheme.elevation: AuraElevation`
- `AuraTheme.window: AuraWindowInfo`
- `AuraTheme.layout: AuraLayout`
- `AuraTheme.colorMode: AuraColorMode`
- `AuraTheme.material: AuraMaterial`
- `AuraTheme.reduceTransparency: Boolean`
- `AuraTheme.effectiveMaterial(requested: AuraMaterial = material): AuraMaterial`

### Pure Material Resolution & Accessibility Helpers
```kotlin
fun resolveEffectiveMaterial(
    requested: AuraMaterial,
    sdkInt: Int = Build.VERSION.SDK_INT,
    reduceTransparency: Boolean = false,
): AuraMaterial

@Composable
fun rememberAuraReduceTransparency(): Boolean
// Returns true if PowerManager.isPowerSaveMode == true OR system high_text_contrast_enabled == 1
```

| Requested | `reduceTransparency = true` | API 33+ | API 31–32 | API 26–30 |
|---|---|---|---|---|
| `Solid` | `Solid` | `Solid` | `Solid` | `Solid` |
| `Glass` | `Solid` | `Glass` | `Glass` | `Solid` |
| `LiquidGlass` | `Solid` | `LiquidGlass` | `Glass` | `Solid` |

---

## 2. Color Tokens (`AuraTheme.colors`)

`AuraColors` (`com.example.ds.foundation.color`) adapts automatically to `Light` / `Dark`:

| Group | Fields (`Color`) | Purpose |
|---|---|---|
| `isDark` | `Boolean` | Quick dark-mode check when adjusting translucent overlays |
| `background` | `.primary`, `.secondary` | Layer 0 page/window canvas backgrounds |
| `surface` | `.default`, `.raised`, `.sunken`, `.floating`, `.inverse`, `.floatingGlass` | Cards, wells, elevated pods, floating bar tints, adaptive glass tint (`#CCF7F7F9` / `#CC1C1C1E`), dark/inverse banners |
| `content` | `.primary`, `.secondary`, `.tertiary`, `.disabled`, `.inverse` | Headings, body copy, captions/hints, disabled labels, inverse text |
| `brand` | `.primary`, `.onPrimary`, `.primaryPressed`, `.subtle`, `.onSubtle` | Primary CTAs, active tabs/pills, light tinted highlight containers |
| `border` | `.subtle`, `.default`, `.strong`, `.focus`, `.glassSpecular` | Dividers, outlined containers, hovered/high-contrast strokes, focus rings, glass specular rim |
| `status` | `.error`, `.success`, `.warning`, `.info` -> each has `.solid`, `.onSolid`, `.subtle`, `.onSubtle` | Validation states, badges, semantic banners, status chips |
| `interaction` | `.pressedOverlay`, `.selectedContainer`, `.onSelected`, `.disabledContainer`, `.scrim` | Touch compression tint, active selection highlights, disabled fills, edge/modal scrims |

**Content Color Propagation:** Containers (`AuraScaffold`, `AuraButton`, `AuraCard`, etc.) propagate `LocalAuraContentColor` and `LocalAuraTextStyle`. Use `AuraText` (with optional `color = AuraTheme.colors.content.secondary`) and `AuraIcon` instead of raw Compose `BasicText`/`Image` so inheritance works automatically.

---

## 3. Typography Tokens (`AuraTheme.typography`)

`AuraTypography` (`com.example.ds.foundation.typography`) provides **16 semantic roles**. `display*`, `headline*`, and `title*` scale dynamically via `AuraTheme.layout.typographyScale` (`0.85f..1.25f`). `body*`, `label*`, and `mono*` hold fixed baseline readability across breakpoints.

| Role | Baseline Size / LineHeight | Weight | Notes |
|---|---|---|---|
| `displayLarge` | `36.sp` / `44.sp` (scaled) | `Bold` | Hero numbers / landing banners |
| `displayMedium` | `28.sp` / `36.sp` (scaled) | `Bold` | Large feature headers |
| `headlineLarge` | `24.sp` / `32.sp` (scaled) | `SemiBold` | Top screen titles |
| `headlineMedium` | `20.sp` / `28.sp` (scaled) | `SemiBold` | Section headers |
| `headlineSmall` | `18.sp` / `26.sp` (scaled) | `SemiBold` | Subsection headers |
| `titleLarge` | `18.sp` / `24.sp` (scaled) | `SemiBold` | Dialog titles, top app bar titles |
| `titleMedium` | `16.sp` / `22.sp` (scaled) | `SemiBold` | Card titles, bottom sheet titles |
| `titleSmall` | `14.sp` / `20.sp` (scaled) | `SemiBold` | Dense row headers, group titles |
| `bodyLarge` | `16.sp` / `24.sp` | `Normal` | Primary readable body copy |
| `bodyMedium` | `14.sp` / `20.sp` | `Normal` | Standard list & card copy (`LocalAuraTextStyle` default) |
| `bodySmall` | `12.sp` / `18.sp` | `Normal` | Supporting text, captions, metadata |
| `labelLarge` | `15.sp` / `20.sp` | `SemiBold` | Primary buttons, prominent tabs |
| `labelMedium` | `13.sp` / `18.sp` | `Medium` | Chips, compact tabs, field labels |
| `labelSmall` | `11.sp` / `16.sp` | `Medium` | Bottom nav captions, micro-badges |
| `monoMedium` | `14.sp` / `20.sp` | `Medium` | `FontFamily.Monospace`, `tnum` (tabular financial/ABV/date figures) |
| `monoSmall` | `12.sp` / `16.sp` | `Medium` | `FontFamily.Monospace`, `tnum` (dense numeric readouts) |

---

## 4. Responsive Layout & Window Info (`AuraTheme.layout`, `AuraTheme.window`)

Aura classifies the viewport along **two axes**:
- **Primary (`smallestWidthDp` & `widthDp` -> `AuraWidthClass`):** `ExtraSmall` (`< 360dp`), `Compact` (`360–599dp`), `Medium` (`600–839dp`), `Expanded` (`>= 840dp`)
- **Height (`AuraHeightClass`):** `Compact` (`< 480dp`), `Medium` (`480–899dp`), `Expanded` (`>= 900dp`)
- **Secondary (`widthDp / heightDp` -> `AuraDisplayRatio`):** `UltraTall` (`< 0.50`), `Portrait` (`0.50–0.85`), `Square` (`0.85–1.20` — unfolded foldables), `Landscape` (`1.20–1.85`), `UltraWide` (`> 1.85` — phone landscape)

### Resolved Presets (`AuraLayoutDefaults.fromWindowInfo`)

| Preset | Trigger | `screenMarginHorizontal` | `screenMarginVertical` | `sectionSpacing` | `gridColumns` | `gridGutter` | `topBarHeight` | `maxContentWidth` | `supportsTwoPane` | `primaryPaneFraction` | `typographyScale` |
|---|---|---|---|---|---|---|---|---|---|---|---|
| `ExtraSmall` | `sw/w < 360dp` | `12.dp` | `12.dp` | `16.dp` | `1` | `10.dp` | `52.dp` | `600.dp` | `false` | `1f` | `0.92f` |
| `Compact` | Default portrait | `16.dp` | `16.dp` | `20.dp` | `1` | `12.dp` | `56.dp` | `600.dp` | `false` | `1f` | `1.00f` |
| `PhoneLandscape` | `sw < 600 && w >= 600` (Compact height / UltraWide) | `24.dp` | `12.dp` | `16.dp` | `2` | `16.dp` | `48.dp` | `960.dp` | `w >= 640` | `0.42f` | `0.96f` |
| `FoldableSquare` | `w >= 600 && ratio in Square` | `24.dp` | `20.dp` | `24.dp` | `2` | `16.dp` | `60.dp` | `840.dp` | `true` | `0.45f` | `1.04f` |
| `Medium` | `w 600–839dp` | `24.dp` | `20.dp` | `24.dp` | `2` | `16.dp` | `60.dp` | `840.dp` | `true` | `0.44f` | `1.04f` |
| `Expanded` | `w >= 840dp` | `32.dp` | `24.dp` | `28.dp` | `3` | `20.dp` | `64.dp` | `1200.dp` | `true` | `0.38f` | `1.08f` |

---

## 5. Interaction Motion Tokens (`AuraTheme.motion`)

`AuraMotion` (`com.example.ds.foundation.motion`) provides dual-track motion specs (`AuraMotionProfile.Standard` vs `AuraMotionProfile.Expressive`) plus system accessibility helpers.

### Properties & Defaults

| Token | `Standard` Default | `Expressive` Override | Purpose |
|---|---|---|---|
| `pressInDampingRatio` / `pressInStiffness` | `0.85f` / `800f` | Same | Fast, firm press compression (`pressInSpring()`) |
| `pressOutDampingRatio` / `pressOutStiffness` | `0.65f` / `500f` | Same | Subtle tactile release rebound (`pressOutSpring()`) |
| `iconDampingRatio` / `iconStiffness` | `0.75f` / `600f` | Same | Secondary icon micro-nudge (`iconSpring()`) |
| `pressInDurationMillis` / `pressOutDurationMillis` | `90` / `180` | Same | Asymmetric color/border press/release interpolation |
| `minPressDurationMillis` | `110L` | Same | Minimum tactile latch so quick taps inside scroll views still render |
| `stateChangeDurationMillis` / `loadingCrossfadeDurationMillis` | `150` / `120` | Same | Enabled/disabled and loading crossfade transitions |
| `spinnerRotationDurationMillis` / `spinnerSweepDurationMillis` | `900` / `650` | Same | Indeterminate spinner cycle |
| `pressedScaleDefault` / `Large` / `Ghost` | `0.96f` / `0.975f` / `0.95f` | Same | Surface press scales |
| `leadingIconPressedScale` / `trailingIconNudge` | `0.92f` / `2.dp` | Same | Button icon depth cues |
| `profile` | `Standard` | `Expressive` | Active `AuraMotionProfile` track |
| `enterDurationMillis` / `exitDurationMillis` | `280` / `200` | `380` / `260` | Spatial container enter > exit (`30–50%` asymmetry) |
| `spatialDampingRatio` / `spatialStiffness` | `0.90f` / `400f` | `0.75f` / `350f` | Dialogs, popovers, FAB expansion, bottom island motion |
| `elasticDampingRatio` / `elasticStiffness` | `0.58f` / `320f` | `0.58f` / `320f` | `LiquidGlass` squish/metaball impulse (`elasticStretchSpring()`) |
| `staggerDelayMillis` / `staggerBudgetMillis` | `40` / `400` | `60` / `400` | Incremental item reveal capped at `400ms` (`staggerDelay(index)`) |
| `scrimFadeDurationMillis` | `180` | `220` | Modal backdrop scrim fade |
| `adaptiveBarDampingRatio` / `adaptiveBarStiffness` | `0.85f` / `380f` | Same | Non-overshooting spring for Docked <-> Floating morph (`adaptiveBarMorphSpring()`) |
| `floatingBarAppearDurationMillis` / `Disappear` | `250` / `180` | Same | Asymmetric bezier enter/exit specs (`floatingBarAppearSpec()`, `floatingBarDisappearSpec()`) |
| `reduceMotion` | `false` (auto `true` if system scale = `0f`) | Same | Controlled automatically via `rememberAuraReducedMotion()` |

### Helper Methods on `AuraMotion`
- `pressInSpring<T>()`, `pressOutSpring<T>()`, `iconSpring<T>()`
- `spatialSpring<T>()`, `elasticStretchSpring<T>()`
- `enterSpec<T>(): FiniteAnimationSpec<T>` — returns `snap()` when `reduceMotion == true`, else `tween(enterDurationMillis, FastOutSlowInEasing)`
- `exitSpec<T>(): FiniteAnimationSpec<T>` — returns `snap()` when `reduceMotion == true`, else `tween(exitDurationMillis, FastOutSlowInEasing)`
- `staggerDelay(index: Int): Int` — returns `0` when `reduceMotion == true` or `index <= 0`, else `minOf(index * staggerDelayMillis, staggerBudgetMillis)`
- Top-level composable helper: `com.example.ds.foundation.motion.rememberAuraReducedMotion(): Boolean`

---

## 6. Elevation & Scrim Ladder (`AuraTheme.elevation`)

`AuraElevation` (`com.example.ds.foundation.elevation.AuraElevationDefaults.Standard`) defines 5 Z-axis levels (`AuraElevationLevel(elevation, keyShadowAlpha, ambientShadowAlpha, scrimAlpha)`):

| Token | `elevation` | `keyShadowAlpha` | `ambientShadowAlpha` | `scrimAlpha` | Typical Surface Role |
|---|---|---|---|---|---|
| `level0` | `0.dp` | `0.00f` | `0.00f` | `0.00f` | Layer 0 canvas, flat inputs, outlined/filled cards |
| `level1` | `2.dp` | `0.06f` | `0.04f` | `0.16f` | Elevated/glass cards, segmented thumb, edge scrim tail |
| `level2` | `6.dp` | `0.14f` | `0.08f` | `0.28f` | Scrolled `AuraTopAppBar`, `Floating` icon button, `Secondary`/`Glass` FAB |
| `level3` | `12.dp` | `0.14f` | `0.08f` | `0.40f` | `Primary`/`Surface` FAB, `FloatingGlassScaffold` edge scrim peak |
| `level4` | `24.dp` | `0.20f` | `0.12f` | `0.56f` | Modal overlays (`AuraDialog`, popovers, full-screen dim scrims) |
