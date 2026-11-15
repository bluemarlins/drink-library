---
component_id: "aura.component.adaptive-two-pane"
name: "AuraAdaptiveTwoPane"
category: "layout"
status: "ready"
version: "0.1.0"
platforms:
  android: "com.example.ds.component.AuraAdaptiveTwoPane"
  ios: "AuraDS.AuraAdaptiveTwoPane (planned)"
  web: "@aura-ds/react.AuraAdaptiveTwoPane (planned)"
tokens:
  - "layout.supportsTwoPane"
  - "layout.primaryPaneFraction"
  - "layout.screenMarginHorizontal"
  - "layout.screenMarginVertical"
  - "layout.sectionSpacing"
  - "layout.gridGutter"
---

# AuraAdaptiveTwoPane

> Cross-platform specification for Aura's `Smallest Width × Display Ratio`-aware two-pane adaptive layout container. See [05-responsive-layout.md](../../foundation/05-responsive-layout.md) and [00-spec-architecture.md](../00-spec-architecture.md).

---

## 1. Intent & Usage Principles

### Problem

Screens with both an interactive control/master pane and a detailed showcase/matrix/editor pane become either uncomfortably wide in single-column mode on unfolded foldables/tablets or require endless vertical scrolling in landscape phones.

### When to Use

- **Interactive Playgrounds + Reference Matrices:** `:design-system-preview` (`ButtonPreviewScreen`, `TextFieldPreviewScreen`).
- **Two-Page Book Spread:** **My Planner** daily schedule (`primaryPane`) alongside reflective paper diary (`secondaryPane`) on unfolded foldables.
- **List-Detail & Portfolio Split:** **My Finance** overview + holdings detail; **My Drink Diary** bottle sensory summary + tasting notes.

### When NOT to Use & Alternatives

| Situation | Alternative | Reason |
|---|---|---|
| Homogeneous collection of equal-priority cards | `AuraAdaptiveGrid` | Uniform multi-column grid scales symmetrically (`1 / 2 / 3` columns) |
| Narrow modal dialog or single-step sheet | Single `Column` | Modals have bounded max width (`maxContentWidth`) rather than internal two-pane split |

### Context Rules

- **`LAY-RULE-003`:** Place the higher-frequency control or summary/master list in `primaryPane` (left pane in LTR), and the detailed reference, editor, or matrix in `secondaryPane` (right pane in LTR).
- **`LAY-RULE-004`:** In Two-Pane mode, each pane must own an independent scroll container so pinning/configuring controls in `primaryPane` never scrolls the reference list out of view, and vice versa.

### Do / Don't

| Do | Don't |
|---|---|
| Put live controls/filters/master index in `primaryPane` and detailed reference/editor in `secondaryPane`. | Nest scrollable `LazyColumn` without bounded height directly inside `primaryPane` or `secondaryPane`. |
| Pass `innerPadding` from `AuraScaffold` into `AuraAdaptiveTwoPane(modifier = Modifier.fillMaxSize().padding(innerPadding))`. | Wrap `AuraAdaptiveTwoPane` in an outer `.verticalScroll()` container (it breaks independent pane scrolling). |
| Let `AuraTheme.layout` decide `supportsTwoPane` from `Smallest Width × Display Ratio`. | Hardcode tablet-only `Row` splits using raw `screenWidthDp > 600` checks. |

---

## 2. Anatomy & Canonical Part IDs

```text
Single-Pane (supportsTwoPane == false)        Two-Pane (supportsTwoPane == true)
+-----------------------------------+        +-------------------+------------------------+
| scrollable Column                 |        | Row                                        |
|  [primaryPane]                    |        | [primaryPane]     | [secondaryPane]        |
|  --- sectionSpacing ---           |        | (weight: pFrac)   | (weight: 1 - pFrac)    |
|  [secondaryPane]                  |        | own ScrollState   | own ScrollState        |
+-----------------------------------+        +-------------------+------------------------+
```

| Part ID | Responsibility | Required | Semantics |
|---|---|---|---|
| `root` | Switches between stacked `Column` and split `Row` based on `AuraTheme.layout.supportsTwoPane` | Yes | Layout container |
| `primaryPane` | Primary interactive/summary region (`primaryPaneFraction` width when split) | Yes | Independent scroll in split mode |
| `secondaryPane` | Secondary reference/detail/editor region (`1 - primaryPaneFraction` width when split) | Yes | Independent scroll in split mode |

---

## 3. Responsive Layout Behavior (`Adaptive Behavior Matrix`)

| `AuraLayout` Profile | `supportsTwoPane` | `primaryPaneFraction` | `paneSpacing` / Stack Spacing | Pane Scroll Behavior |
|---|---|---|---|---|
| **`ExtraSmall`** (Cover `sw < 360dp` / `UltraTall`) | `false` | Stacked (`100%`) | Vertical `16px` (`sectionSpacing`) | Unified single scroll |
| **`Compact`** (Phone Portrait) | `false` | Stacked (`100%`) | Vertical `20px` (`sectionSpacing`) | Unified single scroll |
| **`PhoneLandscape`** (`UltraWide`, `width >= 640dp`) | `true` | `42% : 58%` | Horizontal `16px` (`gridGutter`) | Independent dual vertical scroll |
| **`Medium` + `Square`** (Foldable `~1:1`) | `true` | `45% : 55%` | Horizontal `16px` (`gridGutter`) | Independent dual vertical scroll |
| **`Expanded`** (Tablet / DeX `>= 840dp`) | `true` | `38% : 62%` | Horizontal `20px` (`gridGutter`) | Independent dual vertical scroll |

### Guardrail Rules

- **`PAN-001`:** `primaryPaneFraction` must be clamped within `(0.25f .. 0.65f)` to prevent unusable micro-panes on custom overrides.
- **`PAN-002`:** Callers may pass `forceSinglePane = true` for debug simulation or explicitly linear flows without modifying `AuraTheme.layout`.

---

## 4. Semantic Token Bindings

| Part | Token Binding |
|---|---|
| `root` split switch | `AuraTheme.layout.supportsTwoPane`, `AuraTheme.layout.primaryPaneFraction` |
| `root` max content rail | `AuraTheme.layout.maxContentWidth` |
| `primaryPane` / `secondaryPane` gutters | `AuraTheme.layout.screenMarginHorizontal`, `screenMarginVertical`, `gridGutter` |
| Single-pane stacked vertical rhythm | `AuraTheme.layout.sectionSpacing` |

---

## 5. Interaction Motion & Accessibility Contract

- **Interaction Motion (`interaction_motion: "none"`):** `AuraAdaptiveTwoPane` is a passive responsive structural layout container with no clickable press target of its own. Scroll physics use native platform inertia while interactive child components own their `AuraTheme.motion` transitions.
- **Accessibility & Focus Order:**
  - Keyboard/TalkBack focus traversal progresses linearly from `primaryPane` (top/start) into `secondaryPane` (bottom/end), preserving predictable reading order in both Single-Pane and Two-Pane modes.
  - Preserves composable identity inside in-place configuration changes (`configChanges` / Popup Window resizing) when callers hoist screen state outside `AuraAdaptiveTwoPane`.

---

## 6. Platform Bindings (Layer B)

### 6.1 Android (Jetpack Compose)

<!-- API:ANDROID:BEGIN -->
```kotlin
@Composable
fun AuraAdaptiveTwoPane(
    modifier: Modifier = Modifier,
    forceSinglePane: Boolean = false,
    primaryPaneFraction: Float = AuraTheme.layout.primaryPaneFraction,
    paneSpacing: Dp = AuraTheme.layout.gridGutter,
    primaryPane: @Composable () -> Unit,
    secondaryPane: @Composable () -> Unit,
)
```
<!-- API:ANDROID:END -->

### 6.2 iOS (SwiftUI Reference)

<!-- API:IOS:BEGIN -->
```swift
struct AuraAdaptiveTwoPane<Primary: View, Secondary: View>: View {
    var forceSinglePane: Bool = false
    var primaryPaneFraction: CGFloat? = nil
    @ViewBuilder var primaryPane: () -> Primary
    @ViewBuilder var secondaryPane: () -> Secondary
}
```
<!-- API:IOS:END -->

### 6.3 Web (React / TypeScript Reference)

<!-- API:WEB:BEGIN -->
```tsx
export interface AuraAdaptiveTwoPaneProps {
  forceSinglePane?: boolean;
  primaryPaneFraction?: number;
  primaryPane: React.ReactNode;
  secondaryPane: React.ReactNode;
}
```
<!-- API:WEB:END -->
