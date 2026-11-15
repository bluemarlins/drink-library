---
component_id: "aura.component.scaffold"
name: "AuraScaffold"
category: "layout"
status: "ready"
version: "0.1.0"
platforms:
  android: "com.example.ds.component.AuraScaffold"
  ios: "AuraDS.AuraScaffold (planned)"
  web: "@aura-ds/react.AuraScaffold (planned)"
tokens:
  - "background.primary"
  - "content.primary"
  - "layout.*"
  - "typography.bodyMedium"
---

# AuraScaffold

> Cross-platform specification for Aura's root screen layout scaffold. Replaces Material 3 `Scaffold` with zero `material3` dependency and native `AuraTheme` token bindings. See [05-responsive-layout.md](../../foundation/05-responsive-layout.md) and [00-spec-architecture.md](../00-spec-architecture.md).

---

## 1. Intent & Usage Principles

### Problem

Every screen across **My Planner**, **My Finance**, **My Drink Diary**, and **`:design-system-preview`** needs consistent background polarity, top/bottom bar slot placement, floating action placement, and edge-to-edge system inset propagation without importing Material 3.

### When to Use

- Root layout container for top-level navigation destinations and detail screens.
- Screens combining a top header (`topBar`), main scrollable/adaptive body (`content`), and optional `bottomBar` or `floatingActionButton`.

### When NOT to Use & Alternatives

| Situation | Alternative | Reason |
|---|---|---|
| Desktop/home wallpaper full-bleed canvas without system chrome | `Box` + direct inset modifiers | **Home Launcher** manages wallpaper/dock layers directly |
| Nested split panels inside an existing screen | `AuraAdaptiveTwoPane` | Do not nest multiple `AuraScaffold` instances inside a single viewport |

### Context Rules

- **`LAY-RULE-001`:** Screens must pass `innerPadding` received from `content(PaddingValues)` directly to the outer scrollable or two-pane container so content never hides behind `topBar`, `bottomBar`, or system bars.
- **`LAY-RULE-002`:** Never nest `AuraScaffold` inside another `AuraScaffold` on the same screen.

### Do / Don't

| Do | Don't |
|---|---|
| Wrap every standard screen destination in `AuraScaffold` and pass `innerPadding` to the outer body modifier. | Hardcode status bar or navigation bar heights (`padding(top = 24.dp)`) instead of consuming `innerPadding`. |
| Provide `AuraTheme.colors.background.primary` (or `background.secondary` for grouped layouts) via `containerColor`. | Inject `MaterialTheme.colorScheme` or opaque white/black hex colors on screen backgrounds. |
| Combine `AuraScaffold` with `AuraAdaptiveTwoPane` or `AuraAdaptiveGrid` inside `content(innerPadding)`. | Nest a second `AuraScaffold` inside `primaryPane` or `secondaryPane`. |

---

## 2. Anatomy & Canonical Part IDs

```text
+-------------------------------------------------------+
| root (containerColor = background.primary)            |
|  +-------------------------------------------------+  |
|  | topBar (respects top system bar / cutout)       |  |
|  +-------------------------------------------------+  |
|  | content(innerPadding)                           |  |
|  |   - Receives combined top/bottom bar &          |  |
|  |     safe-area inset padding values              |  |
|  |                              [floatingActionBtn]|  |
|  +-------------------------------------------------+  |
|  | bottomBar (respects bottom navigation bar)      |  |
|  +-------------------------------------------------+  |
+-------------------------------------------------------+
```

| Part ID | Responsibility | Required | Semantics / Providers |
|---|---|---|---|
| `root` | Full-size backdrop surface setting base background & default `AuraText`/`AuraIcon` color & text style | Yes | `LocalAuraContentColor`, `LocalAuraTextStyle` |
| `topBar` | Optional top header chrome anchored at top edge | No | Measured height added to `content` top padding |
| `content` | Screen body receiving safe `PaddingValues` | Yes | Consumes insets cleanly |
| `bottomBar` | Optional bottom navigation/action bar anchored at bottom edge | No | Measured height added to `content` bottom padding |
| `floatingActionButton` | Optional FAB positioned at bottom-end above `bottomBar` + `16px` margin | No | Overlay slot |

---

## 3. Slot & Parameter Matrix

| Parameter | Default | Contract |
|---|---|---|
| `topBar` | Empty composable | Rendered at top; its height sets top inset if present (with window top inset fallback when absent) |
| `bottomBar` | Empty composable | Rendered at bottom; its height sets bottom inset if present |
| `floatingActionButton` | Empty composable | Placed at `Alignment.BottomEnd`, `16px` margin from `bottomBar` top or bottom window inset |
| `containerColor` | `AuraTheme.colors.background.primary` | Screen canvas color (`background.secondary` allowed for grouped sheet-style screens) |
| `contentColor` | `AuraTheme.colors.content.primary` | Provided via `LocalAuraContentColor` to descendants |
| `contentWindowInsets` | `WindowInsets.systemBars` | Safe area insets propagated to `innerPadding` when bars are absent |

### Guardrail Rules

- **`SCF-001`:** When `topBar` is present and consumes the status-bar inset (e.g., `AuraTopAppBar` or system `TopAppBar`), `innerPadding.calculateTopPadding()` reflects the measured `topBar` height (including its top inset) rather than double-counting `contentWindowInsets.top`.
- **`SCF-002`:** When `topBar` is empty (`0px`), `innerPadding.calculateTopPadding()` preserves `contentWindowInsets`'s top padding so content clears the status bar.

---

## 4. Semantic Token Bindings

| Part | Token Binding |
|---|---|
| `root` background | `AuraTheme.colors.background.primary` |
| `root` default content color | `AuraTheme.colors.content.primary` |
| `root` default text style | `AuraTheme.typography.bodyMedium` |
| FAB spacing offset | `16px` (`screenMarginHorizontal` aligned or fixed `16.dp` standard offset) |

---

## 5. Responsive Layout Behavior (`Smallest Width` × `Display Ratio`)

| Window / Ratio Profile | Scaffold Chrome & Inset Behavior |
|---|---|
| **`ExtraSmall` (`sw < 360dp`, Foldable Cover / `UltraTall`)** | Keeps `topBarHeight = 52dp` and narrow `12dp` inner margins so vertical scrolling viewport maximizes content visibility. |
| **`Compact` (`360..599dp`, Phone Portrait)** | Standard `56dp` top bar clearance and `WindowInsets.systemBars` edge-to-edge propagation. |
| **`Medium` + `Square` (Unfolded Foldable `~1:1`)** | Supplies full screen-width top/bottom chrome while inner `AuraAdaptiveTwoPane` splits body content `45% : 55%` inside `maxContentWidth = 840dp`. |
| **`PhoneLandscape` + `UltraWide` (`width >= 600dp`, `height < 480dp`)** | Drops `topBarHeight` to `48dp` and preserves horizontal navigation-bar insets cleanly in `innerPadding.calculateLeft/Right`. |
| **`Expanded` (`>= 840dp`, Tablet / DeX)** | Supports full-width background canvas (`background.primary`) with `64dp` header clearance and `1200dp` max centered content rail. |

---

## 6. Interaction Motion & Accessibility Contract

- **Interaction Motion (`interaction_motion: "none"`):** `AuraScaffold` is a passive layout structural chrome slot container with no direct clickable/pressed state of its own; interactive feedback is owned by child components (`topBar` actions, `floatingActionButton`, `content`).
- **Accessibility & Edge-to-Edge:**
  - Always passes platform-safe `PaddingValues` derived from `WindowInsets.systemBars` (and freeform caption bars in tablet Popup Window mode) so interactive controls never overlap system gesture areas or camera cutouts.
  - Propagates `LocalAuraContentColor = colors.content.primary` and `LocalAuraTextStyle = typography.bodyMedium` so descendants preserve WCAG AA legibility by default.

---

## 7. Platform Bindings (Layer B)

### 7.1 Android (Jetpack Compose)

<!-- API:ANDROID:BEGIN -->
```kotlin
@Composable
fun AuraScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    containerColor: Color = AuraTheme.colors.background.primary,
    contentColor: Color = AuraTheme.colors.content.primary,
    contentWindowInsets: WindowInsets = WindowInsets.systemBars,
    content: @Composable (PaddingValues) -> Unit,
)
```
<!-- API:ANDROID:END -->

### 7.2 iOS (SwiftUI Reference)

<!-- API:IOS:BEGIN -->
```swift
struct AuraScaffold<Content: View, TopBar: View, BottomBar: View, FAB: View>: View {
    var containerColor: Color = auraTheme.colors.background.primary
    @ViewBuilder var topBar: () -> TopBar
    @ViewBuilder var bottomBar: () -> BottomBar
    @ViewBuilder var floatingActionButton: () -> FAB
    @ViewBuilder var content: () -> Content
}
```
<!-- API:IOS:END -->

### 7.3 Web (React / TypeScript Reference)

<!-- API:WEB:BEGIN -->
```tsx
export interface AuraScaffoldProps {
  topBar?: React.ReactNode;
  bottomBar?: React.ReactNode;
  floatingActionButton?: React.ReactNode;
  containerColor?: string;
  children: React.ReactNode;
}
```
<!-- API:WEB:END -->
