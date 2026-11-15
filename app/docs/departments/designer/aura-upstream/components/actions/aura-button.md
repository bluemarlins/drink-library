---
component_id: "aura.component.button"
name: "AuraButton"
category: "actions"
status: "ready"
version: "0.2.0"
platforms:
  android: "com.example.ds.component.AuraButton"
  ios: "AuraDS.AuraButton (planned)"
  web: "@aura-ds/react.AuraButton (planned)"
tokens:
  - "brand.*"
  - "content.*"
  - "border.*"
  - "status.error.*"
  - "interaction.*"
  - "motion.*"
  - "typography.*"
  - "layout.*"
---

# AuraButton

> Cross-platform specification for Aura's labeled action button. See [00-button-principles.md](00-button-principles.md) for category-level selection rules and [00-spec-architecture.md](../00-spec-architecture.md) for the bidirectional `Specs ↔ Code` contract.

---

## 1. Intent & Usage Principles

### Problem

Users need an unmistakable, accessible control with an explicit verb label to trigger primary, secondary, cancel, or destructive actions.

### When to Use

- Submitting forms or saving records (e.g., **My Planner** save schedule, **My Finance** record transaction, **My Drink Diary** save tasting note).
- Confirming or dismissing modal dialogs and bottom sheets.
- Primary or secondary navigation steps inside guided workflows.

### When NOT to Use & Alternatives

| Situation | Alternative | Reason |
|---|---|---|
| Icon-only toolbar or media overlay action | `AuraIconButton` | Compact icon hit region with mandatory `contentDescription` |
| Persistent screen-wide `+ Create` shortcut | `AuraFab` | Floating entry point independent of scroll position |
| Switching view modes (`Day \| Week \| Month`) | `AuraSegmentedButton` | Immediate mutually exclusive toggle without submit |
| Multi-select tags or inline filters | `AuraChipButton` | Lightweight attribute/filter capsule |

### Context & Hierarchy Rules

- **`BTN-RULE-001`:** Maximum 1 `Primary` button per screen or modal dialog.
- **`BTN-RULE-002`:** Do not place `Primary` inside repeated cards, list rows, or home launcher widgets; use `Secondary`, `Outline`, or `Ghost`.
- **`BTN-RULE-003`:** Reserve `Destructive` for final confirmation steps; use `Outline` or `Ghost` on first-level surfaces.

### Do / Don't

| Do | Don't |
|---|---|
| Use a concise action verb (`"Save Entry"`, `"Add Asset"`) with `Primary` limited to the single key CTA per view. | Place two or more `Primary` buttons competing side-by-side on the same screen. |
| Pass `loading = true` during async saves to preserve measured width and block double taps. | Replace the button with a standalone spinner that causes layout jump, or wrap it in manual `if (!loading)` clicks. |
| Use `Outline` or `Ghost` inside repeated cards (**My Drink Diary** cellar list) and compact rows. | Put `Primary` or `Large` `fullWidth` CTAs inside every card of a list or home widget. |
| Rely on `AuraButtonSize` (`Small`/`Medium`/`Large`) to keep the `>= 48dp` outer touch target automatically. | Override `Modifier.height(32.dp)` directly on the outer button box, shrinking touch accessibility. |

---

## 2. Anatomy & Canonical Part IDs

```text
+----------------------------------------------------------------------+
|  touchTarget (min 48x48 logical px, invisible hit-box if Small)      |
|  +----------------------------------------------------------------+  |
|  |  root (visual container: background, border, shape)            |  |
|  |                                                                |  |
|  |   [leadingIcon]  --gap--  [label]  --gap--  [trailingIcon]     |  |
|  |                                                                |  |
|  |   [progressIndicator] (centered overlay when loading == true)  |  |
|  +----------------------------------------------------------------+  |
+----------------------------------------------------------------------+
```

| Part ID | Responsibility | Required | Semantics Owner |
|---|---|---|---|
| `touchTarget` | Guarantees minimum interactive hit box (`48×48px` mobile) | Yes | `root` (`Role.Button`) |
| `root` | Visual container fill, border stroke, corner radius, horizontal/vertical padding | Yes | Inherited |
| `leadingIcon` | Optional visual cue before text label | No | Decorative (merged) |
| `label` | Single-line action verb with ellipsis overflow | Yes | Accessible name |
| `trailingIcon` | Optional directional/disclosure cue after text label | No | Decorative (merged) |
| `progressIndicator` | Centered spinner visible when `loading == true` | Conditional | `stateDescription = "In progress"` |

---

## 3. Variant, Size & Material Matrix

### Variants (`AuraButtonVariant`)

| Variant | Visual Weight | Intended Usage |
|---|---|---|
| `Primary` | Highest | Single main CTA per screen or modal (`Save`, `Confirm`, `Apply`) |
| `Secondary` | Medium-High (Tonal) | Complementary high-value action (`Scan Label`, `Analyze Portfolio`) |
| `Outline` | Medium-Low (Bordered) | Neutral alternative or cancel action (`Cancel`, `Reset Filter`) |
| `Ghost` | Lowest (Borderless) | Low-emphasis inline or header action (`Jump to Today`, `See All`) |
| `Destructive` | High Alert | Irreversible delete/reset confirmation (`Delete Note`, `Remove Asset`) |

### Sizes (`AuraButtonSize`) — Logical Units

| Size | Visual Height | Min Touch Target | Horizontal Padding | Vertical Padding | Corner Radius | Label Size / Line Height | Icon / Spinner Size | Icon Gap |
|---|---|---|---|---|---|---|---|---|
| `Small` | `36px` | `48px` (Android) / `44pt` (iOS) | `12px` | `6px` | `8px` | `13px / 18px` SemiBold | `16px` | `6px` |
| `Medium` (Default) | `48px` | `48px` / `44pt` | `16px` | `10px` | `10px` | `14px / 20px` SemiBold | `18px` | `8px` |
| `Large` | `56px` | `56px` | `20px` | `12px` | `12px` | `16px / 22px` SemiBold | `20px` | `8px` |

### Valid & Invalid Combinations

- **`BTN-001`:** Callers must never override height below `minTouchTarget` or inject raw hex colors; use `variant` and `size`.
- **`BTN-002`:** `text` must be non-blank. Icon-only actions must use `AuraIconButton`.
- **`BTN-003`:** Full-width bottom CTAs should use `size = Large` and `fullWidth = true`.

---

## 4. State Precedence & Behavior

Concurrent states resolve in strict deterministic order:

$$\text{Loading} \succ \text{Disabled} \succ \text{Pressed} \succ \text{Focused} \succ \text{Hovered} \succ \text{Rest}$$

1. **`Loading` (`loading == true`):**
   - Retains the active variant's rest container color and exact measured button width/height (prevents layout shift).
   - Sets content (`leadingIcon`, `label`, `trailingIcon`) opacity to `0.0f` and renders centered `progressIndicator`.
   - Blocks all pointer/click events and sets accessibility state to `"In progress"`.
2. **`Disabled` (`enabled == false` and `!loading`):**
   - Ignores all clicks and press visual feedback.
   - Uses `interaction.disabledContainer` (or `Transparent` for `Outline`/`Ghost`) and `content.disabled`.
3. **`Pressed`:**
   - Applies `brand.primaryPressed` for `Primary` or composites `interaction.pressedOverlay` over the rest container for other variants.
4. **Duplicate-Click Prevention (`debounceIntervalMs`):**
   - Defaults to `400ms` monotonic uptime debounce so rapid double taps cannot trigger duplicate saves or transactions. Passing `0L` opts out when immediate repeat taps are intentional.

---

## 5. Semantic Token Bindings (`AuraColors` & `AuraTypography`)

| Part | Variant | State | `AuraColors` / `AuraTypography` Token Path |
|---|---|---|---|
| `root` (bg) | `Primary` | `Rest` / `Loading` | `colors.brand.primary` |
| `root` (bg) | `Primary` | `Pressed` | `colors.brand.primaryPressed` |
| `label` / `progressIndicator` | `Primary` | `Rest` / `Pressed` / `Loading` | `colors.brand.onPrimary` |
| `root` (bg) | `Secondary` | `Rest` / `Loading` | `colors.brand.subtle` |
| `root` (bg) | `Secondary` | `Pressed` | `colors.interaction.pressedOverlay` over `colors.brand.subtle` |
| `label` / `progressIndicator` | `Secondary` | `Rest` / `Pressed` / `Loading` | `colors.brand.onSubtle` |
| `root` (bg) | `Outline` | `Rest` / `Loading` | `Transparent` |
| `root` (bg) | `Outline` | `Pressed` | `colors.interaction.pressedOverlay` |
| `root` (border) | `Outline` | `Rest` / `Loading` | `1px` `colors.border.default` |
| `root` (border) | `Outline` | `Pressed` | `1px` `colors.border.strong` |
| `root` (border) | `Outline` | `Disabled` | `1px` `colors.border.subtle` |
| `label` / `progressIndicator` | `Outline` | `Rest` / `Pressed` / `Loading` | `colors.content.primary` |
| `root` (bg) | `Ghost` | `Rest` / `Loading` | `Transparent` |
| `root` (bg) | `Ghost` | `Pressed` | `colors.interaction.pressedOverlay` |
| `label` / `progressIndicator` | `Ghost` | `Rest` / `Pressed` / `Loading` | `colors.brand.primary` |
| `root` (bg) | `Destructive` | `Rest` / `Loading` | `colors.status.error.solid` |
| `root` (bg) | `Destructive` | `Pressed` | `colors.interaction.pressedOverlay` over `colors.status.error.solid` |
| `label` / `progressIndicator` | `Destructive` | `Rest` / `Pressed` / `Loading` | `colors.status.error.onSolid` |
| `root` (bg) | `Primary` / `Secondary` / `Destructive` | `Disabled` | `colors.interaction.disabledContainer` |
| `root` (bg) | `Outline` / `Ghost` | `Disabled` | `Transparent` |
| `label` | `*` | `Disabled` | `colors.content.disabled` |
| `label` (type) | `Small` | `*` | `typography.labelMedium` (`13sp / 18sp`, `SemiBold`) |
| `label` (type) | `Medium` / `Large` | `*` | `typography.labelLarge` adapted (`14sp/20sp` Medium, `16sp/22sp` Large, `SemiBold`) |

---

## 6. Interaction Motion Contract

Aura uses **Tactile Spring Compression + Smooth State-Layer Interpolation** instead of radial Material ripples (see [03-motion-interaction.md](../../foundation/03-motion-interaction.md)).

1. **Primary Layer — Tactile Spring Scale (`root`):**
   - `Small` / `Medium`: compresses to `motion.pressedScaleDefault` (`0.96f`)
   - `Large` / `fullWidth`: compresses to `motion.pressedScaleLarge` (`0.975f`)
   - `Ghost`: compresses to `motion.pressedScaleGhost` (`0.95f`)
   - **Asymmetric Spring:** Press in uses `motion.pressInSpring` (`dampingRatio = 0.85f, stiffness = 800f`); release out rebounds via `motion.pressOutSpring` (`dampingRatio = 0.65f, stiffness = 500f`).
2. **Secondary Layer — Asymmetric Color & Border Interpolation:**
   - Container, content, and border colors interpolate over `motion.pressInDurationMillis` (`90ms`, `FastOutSlowInEasing`) on press and `motion.pressOutDurationMillis` (`180ms`, `FastOutSlowInEasing`) on release.
   - `Outline` border strengthens from `border.default` to `border.strong` while pressed.
   - **Minimum Press Latch (`motion.minPressDurationMillis = 110ms`):** Inside scrollable containers, Compose delays press emission until tap release; latching the pressed state for at least `110ms` ensures quick clicks always render full compression and tint before rebounding.
3. **Craft Layer — Icon Micro-Nudge:**
   - `leadingIcon`: scales to `motion.leadingIconPressedScale` (`0.92f`) via `motion.iconSpring`.
   - `trailingIcon`: translates horizontally forward by `motion.trailingIconNudge` (`2px` / `2.dp`) via `motion.iconSpring`.
4. **In-Place State Layer — `Rest ↔ Loading` Crossfade & Breathing Spinner:**
   - Label/icon row fades (`alpha 1f ↔ 0f` over `120ms`) and contracts slightly (`scale 1f ↔ 0.92f`) while preserving measured width.
   - `progressIndicator` crossfades and pops in (`scale 0.75f ↔ 1.0f`) and combines continuous rotation (`900ms` linear) with breathing arc modulation (`45° ↔ 240°`, `650ms` reverse `FastOutSlowInEasing`).
5. **Reduced Motion:**
   - When system animations are disabled (`ANIMATOR_DURATION_SCALE == 0`), scale and icon translation resolve directly to rest (`1.0f`, `0px`) while color and loading state feedback remain active.

---

## 7. Responsive Layout Behavior (`Smallest Width` × `Display Ratio`)

| Window / Ratio Profile | Sizing & Placement Guideline |
|---|---|
| **`ExtraSmall` (`sw < 360dp`, Foldable Cover / `UltraTall`)** | Keep action labels short (`1–2` words); use `fullWidth = true` for bottom confirm CTAs and wrap dual actions vertically (`Column`) if combined width exceeds container bounds. |
| **`Compact` (`360..599dp`, Phone Portrait)** | Use `Large` (`fullWidth = true`) for primary form submission CTAs and equal-weight split rows (`weight(1f)`) for modal Confirm/Cancel pairs. |
| **`Medium` + `Square` (Unfolded Foldable `~1:1`)** | Place `fullWidth` CTAs only inside bounded panes (`primaryPaneFraction = 0.45f`) or dialogs (`<= 480dp`); never stretch a `Primary` button across the entire `700dp+` canvas. |
| **`PhoneLandscape` + `UltraWide` (`width >= 600dp`, `height < 480dp`)** | Prefer `Small` or `Medium` inside the `48dp` compact chrome or right-aligned action rows to preserve vertical viewport height. |
| **`Expanded` (`>= 840dp`, Tablet / DeX)** | Use intrinsic-width or bounded (`<= 320dp`) inline actions positioned near the form footer (`Arrangement.End`) rather than full-bleed `fullWidth` bars. |

---

## 8. Accessibility & Guardrails

- **Role:** `Role.Button` (Android), `.accessibilityAddTraits(.isButton)` (iOS), `<button type="button">` (Web).
- **Accessible Name:** Derived from `text` (`maxLines = 1`, `TextOverflow.Ellipsis`). Leading/trailing icons are decorative and merged with the button semantics.
- **Loading State:** Announces `stateDescription = "In progress"` (Android) / `aria-busy="true"` (Web) / loading accessibility value (iOS).
- **Contrast:** Every variant's enabled content/container pair satisfies WCAG AA (≥ 4.5:1) via `AuraColorSchemes`.
- **Touch Target (`BTN-RULE-005`):** Outer hit box enforces `>= 48dp` minimum width and height even for `Small (36dp)`.

---

## 9. Platform Bindings

### 9.1. Android (Jetpack Compose)

- **Source:** [`AuraButton.kt`](../../../design-system/src/main/kotlin/com/example/ds/component/AuraButton.kt)
- **Dependency Policy:** Uses Compose Foundation/UI/Animation primitives only (`material3` independent).

<!-- API:ANDROID:BEGIN -->
```kotlin
package com.example.ds.component

enum class AuraButtonVariant {
    Primary,
    Secondary,
    Outline,
    Ghost,
    Destructive,
}

enum class AuraButtonSize {
    Small,
    Medium,
    Large,
}

@Composable
fun AuraButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AuraButtonVariant = AuraButtonVariant.Primary,
    size: AuraButtonSize = AuraButtonSize.Medium,
    enabled: Boolean = true,
    loading: Boolean = false,
    fullWidth: Boolean = false,
    debounceIntervalMs: Long = 400L,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
)
```
<!-- API:ANDROID:END -->

### 9.2. iOS (SwiftUI Reference Contract — Planned)

<!-- API:IOS:BEGIN -->
```swift
public enum AuraButtonVariant {
    case primary, secondary, outline, ghost, destructive
}

public enum AuraButtonSize {
    case small, medium, large
}

public struct AuraButton<LeadingIcon: View, TrailingIcon: View>: View {
    public init(
        _ text: String,
        variant: AuraButtonVariant = .primary,
        size: AuraButtonSize = .medium,
        isEnabled: Bool = true,
        isLoading: Bool = false,
        fullWidth: Bool = false,
        debounceInterval: TimeInterval = 0.4,
        @ViewBuilder leadingIcon: () -> LeadingIcon = { EmptyView() },
        @ViewBuilder trailingIcon: () -> TrailingIcon = { EmptyView() },
        action: @escaping () -> Void
    )
}
```
<!-- API:IOS:END -->

### 9.3. Web (React / TypeScript Reference Contract — Planned)

<!-- API:WEB:BEGIN -->
```ts
export type AuraButtonVariant =
  | "primary"
  | "secondary"
  | "outline"
  | "ghost"
  | "destructive";

export type AuraButtonSize = "small" | "medium" | "large";

export interface AuraButtonProps {
  text: string;
  onClick: () => void;
  variant?: AuraButtonVariant;
  size?: AuraButtonSize;
  disabled?: boolean;
  loading?: boolean;
  fullWidth?: boolean;
  debounceIntervalMs?: number;
  leadingIcon?: React.ReactNode;
  trailingIcon?: React.ReactNode;
  className?: string;
}
```
<!-- API:WEB:END -->
