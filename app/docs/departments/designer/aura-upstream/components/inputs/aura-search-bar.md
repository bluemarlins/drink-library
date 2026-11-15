---
id: "aura-search-bar"
name: "AuraSearchBar"
category: "inputs"
status: "ready"
version: "1.0.0"
platforms:
  android: "com.example.ds.component.AuraSearchBar"
  ios: "AuraSearchBar"
  web: "AuraSearchBar"
tokens:
  - "color.surface.sunken"
  - "color.surface.default"
  - "color.surface.floating"
  - "color.brand.primary"
  - "color.brand.onPrimary"
  - "color.content.primary"
  - "color.content.secondary"
  - "color.content.tertiary"
  - "color.content.disabled"
  - "color.border.subtle"
  - "color.border.focus"
  - "color.interaction.disabledContainer"
  - "typography.bodyLarge"
  - "typography.labelSmall"
  - "motion.pressInSpring"
  - "motion.pressOutSpring"
  - "motion.stateChangeDurationMillis"
  - "motion.minPressDurationMillis"
  - "motion.pressedScaleDefault"
---

# AuraSearchBar

Instant query bar engineered for real-time filtering, universal search, and quick cellar/archive lookup, supporting both crisp `Solid` surfaces and dual-materiality `Glass` floating surfaces (`FROSTED` / `LIQUID`).

## 1. Intent & Usage Principles

### When to Use

- Real-time keyword filtering of lists, wine/whiskey cellars (**My Drink Diary**), stock tickers (**My Finance**), or schedule archives (**My Planner**).
- Universal app/widget launcher search over colorful wallpaper Layer 0 canvases (**Home Launcher**) using `AuraSearchBarSurface.Glass`.
- Pairing search with quick-clear (`×`) and optional active-filter count indicator (`onFilterClick`).

### When NOT to Use

- **Persistent form fields requiring a field label, error validation, and submit saving:** Use `AuraTextField`.
- **Entering purely numeric amounts or currencies:** Use `AuraNumberField`.
- **Mutually exclusive 2–5 filter pills with no typing needed:** Use `AuraChipButton` or `AuraSegmentedButton`.

### Usage Rules

- **`SRCH-RULE-001` (Instant Feedback):** Query changes update filtered results as the user types via `onQueryChange`; also wire `ImeAction.Search` (`onSearch`) for explicit keyboard confirmation.
- **`SRCH-RULE-002` (Glass Over Rich Media Only):** Use `AuraSearchBarSurface.Glass` (`GlassStyle.FROSTED` or `GlassStyle.LIQUID`) only when floating above colorful imagery or scrollable Layer 0 cards; default to `AuraSearchBarSurface.Solid` (`surface.sunken`) on standard white/dark content pages.
- **`SRCH-RULE-003` (One-Tap Clear & Filter Affordance):** Render a `>= 48dp` clear button automatically whenever `query.isNotEmpty() && enabled`, and display an active count indicator when `activeFilterCount > 0`.

### Do / Don't

| Do | Don't |
|---|---|
| Use `AuraSearchBarSurface.Solid` above flat lists (`My Drink Diary` inventory) for calm legibility. | Put `Glass` surfaces on plain uniform backgrounds where backdrop refraction is invisible. |
| Surface `activeFilterCount` on the trailing filter icon so users know hidden filters are narrowing results. | Force users to type exact filter syntax (`abv:>40`) without a filter trigger affordance. |
| Trigger `onSearch?.invoke(query)` from the keyboard Search action. | Require tapping a separate external "Search" button before filtering works. |
| Maintain `>= 48dp` interactive height across all breakpoints. | Crop clear or filter buttons below `48dp` inside dense headers. |

---

## 2. Anatomy & Variants

```text
  ┌─────────────────────────────────────────────────────────┐
  │  (Q)   Search bottles, distilleries...   (×)   [≡  2 ]  │
  └─────────────────────────────────────────────────────────┘
   Leading      Single-line query input     Clear  Filter + badge
```

- `AuraSearchBarSurface.Solid`: Pill (`50%` corner radius) backed by `surface.sunken`, elevating stroke to `border.focus` (`2.dp`) while focused.
- `AuraSearchBarSurface.Glass`: Full-capsule `GlassSurface` supporting `GlassStyle.FROSTED` and `GlassStyle.LIQUID` with live Layer 0 backdrop sampling (`LocalGlassBackdropState`).

---

## 3. Token Bindings

| Element | State | Light / Dark Semantic Token |
|---|---|---|
| Container (`Solid`) | `Rest` / `Disabled` | `surface.sunken` / `interaction.disabledContainer` |
| Container (`Glass`) | `FROSTED` / `LIQUID` | `GlassSurface(glassStyle)` with adaptive tint & specular rim |
| Stroke (`Solid`) | `Focused` / `Rest` | `border.focus` (`2.dp`) / `border.subtle` (`1.dp`) |
| Leading Magnifier Icon | `Focused` / `Rest` / `Disabled` | `brand.primary` / `content.secondary` / `content.disabled` |
| Query & Placeholder Text | `Rest` / `Placeholder` / `Disabled` | `content.primary` / `content.tertiary` / `content.disabled` (`typography.bodyLarge`) |
| Filter Badge Pill | `activeFilterCount > 0` | `brand.primary` background + `brand.onPrimary` (`typography.labelSmall`) |

---

## 4. State Precedence & Accessibility

### State Precedence

`Disabled` (`!enabled`) > `Focused` > `HasQuery` (`query.isNotEmpty()`) > `Rest`.

### Accessibility

- Uses `ImeAction.Search` (`KeyboardOptions(imeAction = ImeAction.Search)`).
- Clear (`×`) and Filter (`onFilterClick`) buttons provide `Role.Button` with descriptive `contentDescription` (`"Clear search"` and `"Filter (${activeFilterCount} active)"`) and minimum `48.dp` tap area.

---

## 5. Motion

Consumes `AuraTheme.motion`:

1. **Focus Border & Icon Accent:** Border width/color and leading magnifier icon tint transition over `AuraTheme.motion.stateChangeDurationMillis` (`150ms`).
2. **Clear Button Reveal & Tactile Press:** Clear button scales smoothly (`0f -> 1f`) with `AuraTheme.motion.iconSpring()`; interactive trailing buttons scale on press using `rememberAuraTactilePressed(..., motion.minPressDurationMillis)` and `pressInSpring()` / `pressOutSpring()`.
3. **Reduced Motion:** Uses `snap()` under `rememberAuraReducedMotion()`.

---

## 6. Responsive Layout Behavior

| Width / Display Profile | Layout & Chrome Adaptation |
|---|---|
| `ExtraSmall` (`< 360dp`) | Expands to `fillMaxWidth()`, prioritizing query text area (`weight(1f)`) with compact `12.dp` inner horizontal padding. |
| `Compact` (`360–599dp`) | Full-width `52.dp` pill bar ideal beneath `AuraTopAppBar` or docked inside `FloatingGlassScaffold`. |
| `Square` (`~1:1` Pop-up / Foldable) | Fits comfortably within single-pane search headers or master pane of `AuraAdaptiveTwoPane` without clipping trailing filter pill. |
| `UltraWide` (`> 21:9`) | Maintains compact `50.dp` height so vertical list viewport stays generous in landscape split windows. |
| `Expanded` (`>= 840dp` Tablet) | Bounded smoothly inside `AuraTheme.layout.maxContentWidth` or master pane column (typically `360–560dp`), never stretching awkwardly edge-to-edge. |

---

## 7. Cross-Platform API Contract

<!-- API:ANDROID:BEGIN -->
```kotlin
enum class AuraSearchBarSurface {
    Solid,
    Glass,
}

@Composable
fun AuraSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSearch: ((String) -> Unit)? = null,
    placeholder: String = "Search...",
    surface: AuraSearchBarSurface = AuraSearchBarSurface.Solid,
    glassStyle: GlassStyle = GlassStyle.FROSTED,
    enabled: Boolean = true,
    activeFilterCount: Int = 0,
    onFilterClick: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
)
```
<!-- API:ANDROID:END -->

<!-- API:IOS:BEGIN -->
```swift
public enum AuraSearchBarSurface: Sendable {
    case solid
    case glass
}

public struct AuraSearchBar: View {
    public init(
        query: Binding<String>,
        onSearch: ((String) -> Void)? = nil,
        placeholder: String = "Search...",
        surface: AuraSearchBarSurface = .solid,
        glassStyle: GlassStyle = .frosted,
        enabled: Bool = true,
        activeFilterCount: Int = 0,
        onFilterClick: (() -> Void)? = nil
    )
}
```
<!-- API:IOS:END -->

<!-- API:WEB:BEGIN -->
```ts
export type AuraSearchBarSurface = 'Solid' | 'Glass';

export interface AuraSearchBarProps {
  query: string;
  onQueryChange: (query: string) => void;
  onSearch?: (query: string) => void;
  placeholder?: string;
  surface?: AuraSearchBarSurface;
  glassStyle?: GlassStyle;
  enabled?: boolean;
  activeFilterCount?: number;
  onFilterClick?: () => void;
}
```
<!-- API:WEB:END -->
