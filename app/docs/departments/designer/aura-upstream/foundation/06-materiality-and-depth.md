# 06. Materiality & Depth Principles

> **Aura-Original Architecture:** `AuraMaterial` (`Solid`, `Glass`, `LiquidGlass`), the 4-layer surface model, and `AuraElevation` (`level0`–`level4`) are **Aura-original concepts** designed specifically for the Aura Design System. They are **not** Android Material Design (`material3`) concepts or APIs.

---

## 1. Material Axis (`AuraMaterial`)

Aura separates **Color Theme** (`AuraColorMode`: `Light` / `Dark`) from **Surface Materiality** (`AuraMaterial`):

| Material | Optical Behavior | Primary Role | Active Limit |
|---|---|---|---|
| `AuraMaterial.Solid` | Opaque tokenized surface fill (`colors.surface.*`) with crisp 1.dp subtle border | Long-lived content, forms, cards, settings, fallback states | Unlimited |
| `AuraMaterial.Glass` | Live backdrop gaussian blur (`RenderEffect`, API 31+) with tinted frost wash (`68–72%` opacity) and specular border | Floating navigation chrome, search capsules, transient sheets & dialogs | `<= 3` live-blur surfaces per viewport |
| `AuraMaterial.LiquidGlass` | Optical convex-bevel shader with peripheral chromatic dispersion + refraction (AGSL `RuntimeShader`, API 33+) and viscous metaball/squish coupling | Interactive floating hero surfaces (e.g., `AuraBottomIsland`, active pill capsule) | `<= 1` hero surface per screen (`VIS-RULE-002`, `VIS-RULE-006`) |

> **Deprecation Note:** `GlassStyle` (`FROSTED`, `LIQUID`) is deprecated in favor of `AuraMaterial`. Existing component parameters remain binary-compatible through `GlassStyle.toMaterial()`.

---

## 2. Four-Layer Surface Model (`VIS-RULE-001`)

Every Aura screen composes UI across 4 explicit Z-layers so translucent chrome never blurs sibling chrome or competes with primary content:

| Layer | Name | Allowed Materials | Typical `AuraElevation` | Representative Components |
|---|---|---|---|---|
| **Layer 0** | `canvas` | `Solid` | `level0` (`0.dp`) | `AuraScaffold`, `AuraAdaptiveTwoPane`, scrolling feed/editor background |
| **Layer 1** | `content` | `Solid` (edge scrim zone via `FloatingGlassScaffold`) | `level0`–`level1` (`0–2.dp`, scrim `0.16f`–`0.40f`) | `AuraButton`, `AuraTextField`, `AuraNumberField`, `AuraSwitch`, `AuraCheckbox`, `AuraRadio`, `AuraSlider`, `AuraRatingBar`, `AuraDateTimePicker`, `AuraSegmentedButton`, `AuraCard` (`*`) |
| **Layer 2** | `chrome` | `Solid`, `Glass`, `LiquidGlass` (`<= 1`) | `level1`–`level3` (`2–12.dp`) | `AuraTopAppBar`, `AuraBottomIsland`, `AuraSearchBar`, `AuraFab`, `AuraIconButton` (`Floating`), `AuraChipButton` (`Glass`), `AuraBadge` (`Glass`) |
| **Layer 3** | `spatial` | `Solid`, `Glass`, `LiquidGlass` (`<= 1`) | `level3`–`level4` (`12–24.dp`, scrim `0.40f`–`0.56f`) | `AuraDialog`, `MorphingMorePopover`, future `AuraBottomSheet` |

> **`*` Scoped Exception (`AuraCardVariant.Glass`):** `AuraCard` belongs to `layer: "content"` (`Layer 1`) by default and uses `Solid` variants (`Filled`, `Outlined`, `Elevated`) on inline canvases. Its `Glass` variant (`material_allowed: ["Solid", "Glass"]`) is strictly reserved for **Home Launcher** wallpaper-backed widgets or isolated overlay tiles (`CNT-RULE-003`)—never standard scrollable list items.

---

## 3. Core Visual Interaction Rules (`VIS-RULE-001`–`VIS-RULE-007`)

| Rule ID | Principle | Enforcement Requirement |
|---|---|---|
| **`VIS-RULE-001`** | **Layer Boundary Discipline** | `GlassSurface` / `Glass` / `LiquidGlass` are permitted **only** in `chrome` (Layer 2) and `spatial` (Layer 3), plus documented wallpaper-backed widget exceptions. Inline content items (`AuraScaffold` body, form fields, list items) must remain `Solid`. |
| **`VIS-RULE-002`** | **Material by Surface Lifetime** | `Solid` governs persistent workspace content; `Glass` governs persistent/transient floating navigation & overlays; `LiquidGlass` is restricted to at most **one focal hero surface per screen**. |
| **`VIS-RULE-003`** | **Contrast & Legibility on Glass** | Foreground text over any `Glass` or `LiquidGlass` surface must maintain `>= 4.5:1` contrast (WCAG AA); icons and hairline borders must maintain `>= 3:1`. Strengthen tint opacity or progressive edge scrim (`FloatingGlassScaffold`) if dynamic backdrops reduce contrast. |
| **`VIS-RULE-004`** | **Hardware & Accessibility Degradation** | Resolve requested material through the single pure pipeline `resolveEffectiveMaterial(requested, sdkInt, reduceTransparency)` matching the matrix in §4. **Reduced Transparency**, Battery Saver (`PowerManager.isPowerSaveMode`), or High-Contrast Text (`high_text_contrast_enabled`) must immediately fall back to `Solid`. |
| **`VIS-RULE-005`** | **Elevation & Scrim Ladder** | All shadows and protective scrims must read from `AuraTheme.elevation` (`level0`–`level4`). Hardcoded drop-shadow `Dp` or ad-hoc scrim alpha literals in components are forbidden. |
| **`VIS-RULE-006`** | **Live-Blur Performance Budget** | Render at most **3 simultaneous live-blur (`RenderEffect` / AGSL) surfaces** in a visible viewport and at most **1 `LiquidGlass`** surface per screen. |
| **`VIS-RULE-007`** | **State & Focus Effects** | Pressed, hovered, focused, and selected feedback must be driven exclusively through `AuraColors.interaction` state layers and `AuraTheme.motion` tokens. |

---

## 4. Material Fallback Matrix (`VIS-RULE-004`)

All components resolve effective materiality via `resolveEffectiveMaterial(requested, sdkInt, reduceTransparency)` in `AuraTheme.kt`:

| Requested Material | `reduceTransparency = true` (All APIs) | Android API 33+ (TIRAMISU+) | Android API 31–32 (S / S_V2) | Android API 26–30 (Pre-S) |
|---|---|---|---|---|
| `AuraMaterial.Solid` | `Solid` | `Solid` | `Solid` | `Solid` |
| `AuraMaterial.Glass` | `Solid` | `Glass` (`26.dp` blur `RenderEffect`) | `Glass` (`26.dp` blur `RenderEffect`) | `Solid` |
| `AuraMaterial.LiquidGlass` | `Solid` | `LiquidGlass` (AGSL shader + soft blur) | `Glass` (soft `10.dp` blur `RenderEffect`) | `Solid` |

- **Accessibility / Battery Saver (`reduceTransparency = true`):** `GlassSurface` disables blur/AGSL shaders, switches to opaque `colors.surface.floating` (`alpha = 1f`), and exposes `Modifier.testTag("aura_glass_surface_solid_fallback")` for automated verification.
- **Hardware Pre-Blur Fallback (`API 26–30` with `reduceTransparency = false`):** `resolveEffectiveMaterial` returns `Solid` (`RenderEffect` is unavailable), while `GlassSurface` preserves translucent tinting + specular 1.dp rim styling with zero runtime blur cost.

---

## 5. Depth & Elevation Ladder (`AuraElevation`)

| Token | Elevation (`Dp`) | Key Shadow Alpha | Ambient Shadow Alpha | Scrim Alpha | Intended Surface Role |
|---|---|---|---|---|---|
| `level0` | `0.dp` | `0.00f` | `0.00f` | `0.00f` | Canvas, inline inputs, outlined/filled cards |
| `level1` | `2.dp` | `0.06f` | `0.04f` | `0.16f` | Elevated/glass cards, segmented thumb, glass chips, circular action pods, edge scrim tail / popover scrim |
| `level2` | `6.dp` | `0.10f` | `0.06f` | `0.28f` | Scrolled `AuraTopAppBar`, `Floating` icon button, `Secondary`/`Glass` `AuraFab` |
| `level3` | `12.dp` | `0.14f` | `0.08f` | `0.40f` | `Primary`/`Surface` `AuraFab`, protective top/bottom edge scrim peaks in `FloatingGlassScaffold` |
| `level4` | `24.dp` | `0.20f` | `0.12f` | `0.56f` | Modal spatial overlays (`AuraDialog`, future `AuraBottomSheet`) |

---

## 6. Planned Component Governance (Principles Only)

- **`AuraBottomSheet` (`status: planned`):**
  - **Layer:** `spatial` (Layer 3).
  - **Elevation:** `AuraTheme.elevation.level4` (`24.dp`, backdrop scrim `scrimAlpha = 0.56f`).
  - **Allowed Materials:** `["Solid", "Glass"]` (must honor Reduced Transparency -> `Solid`, `>= 4.5:1` text contrast).
  - **Motion Profile:** `Expressive` default for spatial sheet presentation and drag-to-dismiss velocity continuity (`MOT-RULE-004`). Not yet implemented in `:design-system`.

---

## 7. Implementation Files

- [`AuraElevation.kt`](../../design-system/src/main/kotlin/com/example/ds/foundation/elevation/AuraElevation.kt)
- [`AuraTheme.kt`](../../design-system/src/main/kotlin/com/example/ds/foundation/theme/AuraTheme.kt)
- [`GlassSurface.kt`](../../design-system/src/main/kotlin/com/example/ds/component/GlassSurface.kt)
