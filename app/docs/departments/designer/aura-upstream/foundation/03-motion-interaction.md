# 03 · Interaction Motion

## 1. Purpose & Scope

Defines Aura's **Tier 2 semantic interaction motion tokens (`AuraMotion`)** and cross-platform timing/physics rules for **in-place interactive feedback** (press, release, focus, state transitions, and loading).

Spatial navigation and visibility transitions (`Show`, `Hide`, `Move`) are intentionally out of scope here and will be specified separately.

---

## 2. Design Philosophy: Tactile Spring Cushion over Material Ripple

Instead of Material Design's radial clipped ripple, Aura uses **Tactile Spring Compression + Smooth State-Layer Interpolation**:

1. **Harmonizes with `Solid` and `Glass` materials:** Radial ripples clip awkwardly on translucent `Glass` and `LiquidGlass` layers; GPU-accelerated scale compression and smooth color tinting feel cohesive across all material treatments.
2. **Unifies 4 application personalities:** Delivers a physical stamp/cushion feel in **My Planner**, crisp confirmation feedback in **My Finance**, refined tactility in **My Drink Diary**, and responsive 3D elasticity in **Home Launcher**.
3. **Cross-platform portable:** Scale + spring physics + state-layer color interpolation translate cleanly across Jetpack Compose, SwiftUI, and Web React without platform-specific ripple shaders.

---

## 3. Interaction Motion Token Architecture (`AuraMotion`)

Exposed via `AuraTheme.motion` (`AuraMotionDefaults.Standard`).

### 3.1. Spring Physics Tokens

| Token | Compose (`SpringSpec`) | iOS (`SwiftUI`) | Web (`Motion`) | Purpose |
|---|---|---|---|---|
| `pressInSpring` | `dampingRatio = 0.85f, stiffness = 800f` | `.spring(response: 0.16, dampingFraction: 0.85)` | `stiffness: 800, damping: 35` | Fast, firm press compression without wobble |
| `pressOutSpring` | `dampingRatio = 0.65f, stiffness = 500f` | `.spring(response: 0.26, dampingFraction: 0.65)` | `stiffness: 500, damping: 22` | Subtle elastic rebound on release |
| `iconSpring` | `dampingRatio = 0.75f, stiffness = 600f` | `.spring(response: 0.21, dampingFraction: 0.75)` | `stiffness: 600, damping: 28` | Secondary icon micro-nudge and spinner pop |

### 3.2. Asymmetric Duration Tokens

Interactive transitions use **fast attack (`pressIn`)** and **gradual settle (`pressOut`)**:

| Token | Value | Easing | Purpose |
|---|---|---|---|
| `pressInDurationMillis` | `90ms` | `FastOutSlowInEasing` | Container/border press tint |
| `pressOutDurationMillis` | `180ms` | `FastOutSlowInEasing` | Container/border release fade-back |
| `minPressDurationMillis` | `110ms` | — | Minimum pressed visual latch so quick taps inside scrollable containers render complete press-and-release motion |
| `stateChangeDurationMillis` | `150ms` | `FastOutSlowInEasing` | Enabled ↔ Disabled transitions |
| `loadingCrossfadeDurationMillis` | `120ms` | `FastOutSlowInEasing` | In-place `Rest ↔ Loading` content crossfade |
| `spinnerRotationDurationMillis` | `900ms` | `LinearEasing` | Continuous spinner rotation |
| `spinnerSweepDurationMillis` | `650ms` | `FastOutSlowInEasing` (Reverse) | Breathing spinner arc modulation (`45° ↔ 240°`) |

### 3.3. Interactive Scale & Micro-Nudge Tokens

| Token | Value | Target |
|---|---|---|
| `pressedScaleDefault` | `0.96f` | `Small` / `Medium` interactive controls |
| `pressedScaleLarge` | `0.975f` | `Large` / `fullWidth` controls (prevents excessive shrinkage on wide CTAs) |
| `pressedScaleGhost` | `0.95f` | Borderless `Ghost` buttons where scale carries primary tactile feedback |
| `leadingIconPressedScale` | `0.92f` | Subtle icon depth compression on press |
| `trailingIconNudge` | `2.dp` (`2px`) | Directional forward nudge on press |

---

## 4. Accessibility: Reduced Motion Policy

- **System Animation Scale (`ANIMATOR_DURATION_SCALE == 0f`) / Reduced Motion:**
  - Spatial transformations (`scale`, `translationX` icon nudge) must resolve directly to rest (`1.0f`, `0.dp`) to avoid vestibular discomfort.
  - Color/border/state-layer transitions and explicit loading state semantics remain active so interactive feedback is never lost.

---

## 5. Implementation Files

- [`AuraMotion.kt`](../../design-system/src/main/kotlin/com/example/ds/foundation/motion/AuraMotion.kt)
- [`AuraTheme.kt`](../../design-system/src/main/kotlin/com/example/ds/foundation/theme/AuraTheme.kt)
