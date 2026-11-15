---
id: "aura-card"
name: "AuraCard"
category: "containment"
status: "ready"
version: "1.0.0"
platforms:
  android: "com.example.ds.component.AuraCard"
  ios: "AuraCard"
  web: "AuraCard"
tokens:
  - "color.surface.default"
  - "color.surface.raised"
  - "color.surface.sunken"
  - "color.surface.floating"
  - "color.content.primary"
  - "color.border.subtle"
  - "color.border.default"
  - "color.border.strong"
  - "color.brand.primary"
  - "color.interaction.selectedContainer"
  - "color.interaction.pressedOverlay"
  - "color.interaction.disabledContainer"
  - "motion.pressInSpring"
  - "motion.pressOutSpring"
  - "motion.stateChangeDurationMillis"
  - "motion.minPressDurationMillis"
  - "motion.pressedScaleLarge"
---

# AuraCard

Grouped surface container for structuring related metadata, summaries, interactive catalog entries, and **Home Launcher** `Glass` widgets cleanly across Light and Dark themes.

## 1. Overview

`AuraCard` replaces generic Material cards with Aura's semantic surface stepping (`Elevated`, `Filled`, `Outlined`, `Glass`) and standardized internal padding presets (`None`, `Compact`, `Medium`, `Large`). When `onClick != null`, `AuraCard` automatically gains `Role.Button` accessibility, debounced click protection, and subtle large-surface tactile spring compression (`pressedScaleLarge = 0.975f`); when `onClick == null`, it behaves as a calm, passive container.

---

## 2. Intent & Usage Principles

### When to Use

- Grouping cohesive domain records (e.g., **My Finance** portfolio summary, **My Drink Diary** bottle card, **My Planner** daily block).
- Presenting interactive grid tiles in responsive catalogs (`CatalogHomeScreen` grid cards).
- Grouping preview matrices, inspector panels, or preference sections without visual clutter.

### When NOT to Use

- **Wrapping an entire full-screen page in one giant card:** Use `AuraScaffold` and `background.primary` instead (`CNT-RULE-004`).
- **Simple single-line actions or buttons:** Use `AuraButton` or `AuraChipButton`.
- **Deeply nested card-inside-card hierarchies:** Avoid placing `Outlined` or `Elevated` cards inside other `Outlined`/`Elevated` cards; use spacing or a `Filled` (`surface.sunken`) nested callout block at most 1 level deep.

### Usage Rules

- **`CARD-RULE-001` (Choose Emphasis by Surface Context):** Default to `Outlined` for scannable lists and catalog grids, `Elevated` for hero summaries (**My Finance** net worth), `Filled` for quiet nested callout info panels, and `Glass` only over rich backdrops (**Home Launcher**).
- **`CARD-RULE-002` (Interactive vs. Passive Honesty):** Only supply `onClick != null` when the entire card performs an action (navigation, selection toggle). Never make decorative summary cards artificially clickable.
- **`CARD-RULE-003` (Standardized Internal Padding):** Prefer `AuraCardPadding` (`Compact` 12dp, `Medium` 16dp, `Large` 24dp) rather than ad-hoc padding on child columns; use `None` (0dp) only when hosting edge-to-edge media headers.
- **`CARD-RULE-004` (Gentle Tactile Scale on Large Surfaces):** Interactive cards must scale at `motion.pressedScaleLarge` (`0.975f`), never aggressive small-button scales (`0.95f`), so large containers feel refined rather than jittery.

### Do / Don't

| Do | Don't |
|---|---|
| Use `AuraCard(onClick = ...)` for tappable list/grid items such as tasting logs or catalog entries. | Wrap a passive Material `Card` inside `Modifier.clickable(...)` bypassing Aura tactile motion. |
| Use `AuraCard(variant = Filled)` with `Compact` padding for rule readouts and subtle summaries. | Nest `Elevated` cards three levels deep with overlapping drop shadows. |
| Use `selected = true` to highlight an active card choice with `brand.primary` border & tint. | Rely on color alone without structural border thickness change on selected cards. |
| Pass `padding = AuraCardPadding.None` for full-bleed banner/image headers paired with inner text padding. | Stack double `16.dp` padding inside both `AuraCard` and its root child `Column`. |

---

## 3. Anatomy & Variants

```text
  Outlined (Default)        Elevated                  Filled                    Glass
  ╭──────────────────╮      ╭──────────────────╮      ╭──────────────────╮      ╭──────────────────╮
  │ surface.default  │      │ surface.raised   │      │ surface.sunken   │      │ surface.floating │
  │ 1dp border.def.  │      │ 1dp border.subtle│      │ 1dp border.subtle│      │ 1dp border.subtle│
  ╰──────────────────╯      │ + 4dp elevation  │      ╰──────────────────╯      │ 20dp corner rad. │
                            ╰──────────────────╯                                ╰──────────────────╯
```

| Variant (`AuraCardVariant`) | Container Token | Border Token (`Default` -> `Selected`) | Elevation | Corner Radius |
|---|---|---|---|---|
| `Outlined` (default) | `surface.default` | `border.default` (`1.dp`) -> `brand.primary` (`1.5.dp`) | `0.dp` | `16.dp` |
| `Elevated` | `surface.raised` | `border.subtle` (`1.dp`) -> `brand.primary` (`1.5.dp`) | `4.dp` | `16.dp` |
| `Filled` | `surface.sunken` | `border.subtle` (`1.dp`) -> `brand.primary` (`1.5.dp`) | `0.dp` | `16.dp` |
| `Glass` | `surface.floating` | `border.subtle` (`1.dp`) -> `brand.primary` (`1.5.dp`) | `2.dp` | `20.dp` |

| Padding (`AuraCardPadding`) | Value | Intended Use |
|---|---|---|
| `None` | `0.dp` | Full-bleed imagery, map previews, custom child row layouts |
| `Compact` | `12.dp` | Dense metadata blocks, narrow split panes, `ExtraSmall` screens |
| `Medium` (default) | `16.dp` | Standard catalog cards, domain record cards, preview matrices |
| `Large` | `24.dp` | Featured hero summary cards on tablets (`Expanded`) |

---

## 4. Token Bindings

| Element | State | Light / Dark Semantic Token |
|---|---|---|
| Container Surface | `Default` | Mapped by `AuraCardVariant` (`surface.default` / `raised` / `sunken` / `floating`) |
| Container Surface | `Selected` | `interaction.selectedContainer` blended gently or `surface.default` + `brand.primary` border (`Glass` stays `surface.floating`) |
| Container Surface | `Disabled` | `AuraTheme.colors.interaction.disabledContainer` |
| Press Overlay | `Pressed` (`onClick != null`) | `AuraTheme.colors.interaction.pressedOverlay` |
| Border | `Default` / `Selected` / `Disabled` | `border.default` or `border.subtle` / `brand.primary` (`1.5.dp`) / `border.subtle` |
| Default Content Tint | `All` | Provided via `LocalAuraContentColor` -> `AuraTheme.colors.content.primary` |

---

## 5. Responsive Layout Behavior

`AuraCard` integrates cleanly with `AuraAdaptiveGrid` and `AuraAdaptiveTwoPane` across every `AuraWindowInfo` profile:

| Width / Display Profile | Grid & Width Adaptation | Internal Padding & Layout Guidance |
|---|---|---|
| `ExtraSmall` (`< 360dp`) | Single-column (`1 col`) spanning available pane width cleanly without horizontal overflow. | Prefer `Compact` (`12.dp`) or `Medium` (`16.dp`) padding to maximize text line measure. |
| `Compact` (`360–599dp`) | Full-width card lists or 1-column feeds with `16.dp` screen margins. | Standard `Medium` (`16.dp`) padding; `16.dp` corner radius balances content framing and height. |
| `Square` (`~1:1` Pop-up / Foldable) | Flexes into `2-col` (`AuraAdaptiveGrid`) when width >= `600dp`, or stacks cleanly inside split pop-ups. | Keep internal child rows responsive (`FlowRow` or flexible weights) so metadata never truncates awkwardly. |
| `UltraWide` (`> 21:9`) | Remains crisp in single-column cover strips or expands cleanly in wide landscape multi-column feeds. | Bound hero cards with parent `maxContentWidth` (`600dp–1200dp`) so typography lines never become excessively long. |
| `Expanded` (`>= 840dp` Tablet) | Displays inside `2–3` column adaptive grids (`AuraAdaptiveGrid`) and secondary inspector panes (`AuraAdaptiveTwoPane`). | Ideal home for `Elevated` summary cards with `Large` (`24.dp`) padding alongside detail forms. |

---

## 6. State Machine & Interaction Precedence

**State Precedence:** `Disabled` > `Pressed` (`onClick != null`) > `Selected` > `Default` (`Passive` when `onClick == null`).

```mermaid
stateDiagram-v2
    [*] --> Default
    Default --> Pressed: Pointer Down (onClick != null)
    Pressed --> Default: Pointer Up / Cancel
    Default --> Selected: selected = true
    Selected --> Pressed: Pointer Down (onClick != null)
    Default --> Disabled: enabled = false
    Selected --> Disabled: enabled = false
```

---

## 7. Motion & Micro-interactions

Consumes `AuraTheme.motion`:

1. **Interactive Tactile Compression (`onClick != null`):** Tracks pointer press via `rememberAuraTactilePressed(interactionSource, motion.minPressDurationMillis)` and animates card scale to `AuraTheme.motion.pressedScaleLarge` (`0.975f`) using `pressInSpring()` / `pressOutSpring()`, plus `interaction.pressedOverlay` tint.
2. **Passive Stability (`onClick == null`):** Remains completely static (`1.0f` scale, zero hover/press tint) so non-interactive containers never trick users into tapping them.
3. **Selection & State Color Transitions:** Border and container colors crossfade smoothly over `AuraTheme.motion.stateChangeDurationMillis` (`150ms`).
4. **Reduced Motion:** Uses `snap()` when system `ANIMATOR_DURATION_SCALE == 0f`.

---

## 8. Cross-Platform API Contract

<!-- API:ANDROID:BEGIN -->
```kotlin
enum class AuraCardVariant { Elevated, Filled, Outlined, Glass }
enum class AuraCardPadding { None, Compact, Medium, Large }

@Composable
fun AuraCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    variant: AuraCardVariant = AuraCardVariant.Outlined,
    padding: AuraCardPadding = AuraCardPadding.Medium,
    enabled: Boolean = true,
    selected: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
)
```
<!-- API:ANDROID:END -->

<!-- API:IOS:BEGIN -->
```swift
public enum AuraCardVariant: Sendable {
    case elevated
    case filled
    case outlined
    case glass
}

public enum AuraCardPadding: Sendable {
    case none
    case compact
    case medium
    case large
}

public struct AuraCard<Content: View>: View {
    public init(
        variant: AuraCardVariant = .outlined,
        padding: AuraCardPadding = .medium,
        isEnabled: Bool = true,
        isSelected: Bool = false,
        onTap: (() -> Void)? = nil,
        @ViewBuilder content: () -> Content
    )
}
```
<!-- API:IOS:END -->

<!-- API:WEB:BEGIN -->
```ts
export type AuraCardVariant = 'elevated' | 'filled' | 'outlined' | 'glass';
export type AuraCardPadding = 'none' | 'compact' | 'medium' | 'large';

export interface AuraCardProps {
  variant?: AuraCardVariant;
  padding?: AuraCardPadding;
  disabled?: boolean;
  selected?: boolean;
  onClick?: () => void;
  className?: string;
  children?: React.ReactNode;
}
```
<!-- API:WEB:END -->

---

## 9. Accessibility & Test Checklist

- [ ] Interactive `AuraCard` (`onClick != null`) registers `Role.Button`, debounces rapid taps via `rememberAuraDebouncedClick`, and sets `enabled` semantics.
- [ ] Passive `AuraCard` (`onClick == null`) does NOT attach clickable semantics or intercept child focus/accessibility navigation.
- [ ] `selected = true` updates border thickness (`1.5.dp`) and `brand.primary` color cleanly in both Light and Dark themes.
- [ ] Child typography defaults cleanly via `LocalAuraContentColor` (`content.primary`) without inheriting Material surface defaults.
