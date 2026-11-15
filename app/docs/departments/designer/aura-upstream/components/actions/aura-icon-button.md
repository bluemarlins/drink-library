---
id: "aura-icon-button"
name: "AuraIconButton"
category: "actions"
status: "ready"
platforms:
  android: "com.example.ds.component.AuraIconButton"
  ios: "AuraIconButton"
  web: "AuraIconButton"
tokens:
  - "color.brand.primary"
  - "color.brand.onPrimary"
  - "color.brand.subtle"
  - "color.brand.onSubtle"
  - "color.surface.default"
  - "color.surface.raised"
  - "color.surface.sunken"
  - "color.surface.floating"
  - "color.content.primary"
  - "color.content.secondary"
  - "color.content.disabled"
  - "color.border.default"
  - "color.border.subtle"
  - "color.border.focus"
  - "color.status.error.solid"
  - "color.status.error.onSolid"
  - "color.status.error.subtle"
  - "color.status.error.onSubtle"
  - "color.interaction.selectedContainer"
  - "color.interaction.onSelected"
  - "color.interaction.disabledContainer"
  - "color.interaction.pressedOverlay"
  - "motion.pressInSpring"
  - "motion.pressOutSpring"
  - "motion.iconSpring"
  - "motion.stateChangeDurationMillis"
  - "motion.loadingCrossfadeDurationMillis"
  - "motion.spinnerRotationDurationMillis"
  - "motion.spinnerSweepDurationMillis"
  - "motion.minPressDurationMillis"
  - "motion.pressedScaleDefault"
  - "motion.pressedScaleGhost"
  - "motion.leadingIconPressedScale"
---

# AuraIconButton

Compact, icon-only action control for app bars, toolbars, list rows, inline field adornments, and floating overlays across Aura applications.

## 1. Intent & Usage Principles

`AuraIconButton` separates visual compactness (`32.dp`–`48.dp` painted frame) from ergonomics by always enforcing a minimum `48.dp × 48.dp` accessible touch target. Its emphasis follows the 5-level hierarchy established in [00-button-principles.md](00-button-principles.md) so crowded toolbars remain scannable without competing with primary `AuraButton` CTAs.

### Context Rules

- **`IBTN-RULE-001` (Self-Describing Accessibility Label)**: Every standalone `AuraIconButton` MUST convey a clear verb or toggle target via `contentDescription` (e.g., `"Filter transactions"`, `"Toggle theme"`, `"Navigate back"`). Leave `contentDescription = null` on the button only when an adjacent visual label unambiguously describes the control and is merged into the same focus node.
- **`IBTN-RULE-002` (Visual Hierarchy & Density)**:
  - **`Filled`**: Reserved for a single high-emphasis compact action inside a card or floating bar (max 1 per cluster).
  - **`Tonal`**: Medium-emphasis grouped toolbar or toggle actions (calendar mode, bookmark, sort).
  - **`Outlined`**: Crisp bounded utility button on dense or high-contrast utility panels.
  - **`Ghost`**: Default for `AuraTopAppBar` navigation/action clusters, list row affordances, and text-field trailing controls.
  - **`Floating`**: Translucent/elevated island action anchored above imagery, maps, wallpapers, tasting photos, or 3D launcher canvases.
  - **`Destructive`**: Irreversible removal or discard icon trigger inside confirmable toolbars or item rows.
- **`IBTN-RULE-003` (Touch Target Isolation)**: Visual sizes (`Small` `32.dp`, `Medium` `40.dp`, `Large` `48.dp`) MUST preserve at least `48.dp × 48.dp` hit bounds with a minimum `4.dp`–`8.dp` visual gap (`48.dp` center-to-center spacing) between adjacent icon buttons.
- **`IBTN-RULE-004` (Explicit Selection State)**: Use `selected: Boolean?` (`true` / `false`) whenever the control represents a persistent toggle (favorite, pin, lock, grid/list mode) rather than mutating icon tint imperatively without accessibility state.

### When to Use

- App bar navigation (`Back`, `Close`, `Menu`) and trailing action clusters (`Search`, `Theme`, `Filter`, `Settings`).
- Compact list row, card header, or inline `AuraTextField` trailing actions (`Visibility`, `Clear`, `Copy`).
- Overlay actions over hero imagery or canvas editors (e.g., zoom, reset camera, bookmark tasting photo) using `Floating`.

### When NOT to Use

- Primary multi-word actions where an icon alone can be misinterpreted -> use `AuraButton`.
- Attribute filters or multi-select category pills with text labels -> use `AuraChipButton`.
- Non-interactive status indicators or decorative badges -> use `AuraIcon`.

### Do / Don't

| Do | Don't |
|---|---|
| Provide an imperative action `contentDescription` on every icon-only action button. | Leave standalone icon buttons without semantic labels for screen readers. |
| Default to `Ghost` in app bars and `Floating` over photos or 3D launcher canvases. | Place multiple `Filled` icon buttons side by side in a single toolbar cluster. |
| Let `AuraIconButton` expand its transparent hit slop to `48.dp × 48.dp` around `Small`/`Medium` frames. | Wrap `Small` icon buttons inside tight `32.dp` clipping boxes that cut off touch targets or press springs. |
| Pass `selected = true / false` for persistent toggles like bookmark or pin. | Represent binary toggle state purely by swapping raw colors without semantic state. |

## 2. Variants, Shapes & Sizes

### Variants (`AuraIconButtonVariant`)

1. **`Filled`** — Solid brand fill (`brand.primary`) for high-emphasis single icon actions.
2. **`Tonal`** — Subtle brand tint (`brand.subtle`) for calm medium-emphasis toolbar controls.
3. **`Outlined`** — Transparent fill with `1.dp` hairline border (`border.default`) for bounded utility controls.
4. **`Ghost`** — Containerless hover/press surface for clean `AuraTopAppBar` and inline row clusters.
5. **`Floating`** — Elevated frosted island (`surface.floating @ 0.92 alpha`, `1.dp` `border.subtle`, soft ambient shadow) for overlays and detached Floating AppBar controls.
6. **`Destructive`** — Error-tinted container (`status.error.subtle` / `status.error.onSubtle`) for delete/discard icon triggers.

### Shapes (`AuraIconButtonShape`)

- **`Circle`** (`CircleShape`) — Default pill/circular silhouette matching capsule `AuraTopAppBar` navigation and media overlays.
- **`RoundedSquare`** (`12.dp` corner on `Small`/`Medium`, `14.dp` on `Large`) — Soft squircle matching launcher utility tiles, calculator keys, and dense inspector pads.

### Sizes (`AuraIconButtonSize`)

| Size | Visual Container | Icon Size | Min Hit Target | Corner Radius (`RoundedSquare`) |
|---|---|---|---|---|
| `Small` | `32.dp` | `18.dp` | `48.dp × 48.dp` | `10.dp` |
| `Medium` | `40.dp` | `20.dp` | `48.dp × 48.dp` | `12.dp` |
| `Large` | `48.dp` | `24.dp` | `48.dp × 48.dp` | `14.dp` |

## 3. Semantic Token Bindings

All colors resolve through `AuraTheme.colors` with zero direct Material 3 theme reads or hardcoded hex literals inside `:design-system`.

| Variant | Unselected / Default Container | Selected (`selected == true`) Container | Content (Icon / Spinner) | Border | Disabled (`enabled == false`) |
|---|---|---|---|---|---|
| `Filled` | `brand.primary` | `brand.primary` | `brand.onPrimary` | None | `interaction.disabledContainer` / `content.disabled` |
| `Tonal` | `brand.subtle` | `brand.primary` (content `brand.onPrimary`) | `brand.onSubtle` | None | `interaction.disabledContainer` / `content.disabled` |
| `Outlined` | `transparent` | `interaction.selectedContainer` | `content.primary` (`interaction.onSelected` when selected) | `1.dp` `border.default` (`brand.primary` when selected) | `transparent` / `content.disabled` / `border.subtle` |
| `Ghost` | `transparent` | `interaction.selectedContainer` | `content.primary` (`interaction.onSelected` when selected) | None | `transparent` / `content.disabled` |
| `Floating` | `surface.floating` (`0.92f` alpha) | `brand.primary` (content `brand.onPrimary`) | `content.primary` | `1.dp` `border.subtle` (`6.dp` shadow) | `interaction.disabledContainer` / `content.disabled` |
| `Destructive` | `status.error.subtle` | `status.error.solid` (content `status.error.onSolid`) | `status.error.onSubtle` | `1.dp` `border.subtle` when selected/focused | `interaction.disabledContainer` / `content.disabled` |

## 4. State Precedence & Interaction

Strict visual precedence ensures conflicting states never render ambiguously:

1. **Disabled (`!enabled`)**: Non-interactive (`clickable` disabled), `interaction.disabledContainer`, `content.disabled` icon tint, `0.dp` shadow, no press scale.
2. **Loading (`loading == true`)**: Clicks suppressed, retains active variant surface/tint colors, crossfades icon to `AuraLoadingSpinner`, announces `stateDescription = "In progress"`.
3. **Pressed**: Tactile scale spring + subtle `interaction.pressedOverlay` wash while finger/pointer is held or latched for `minPressDurationMillis`.
4. **Selected (`selected == true`)**: Active selected palette and accent border as defined in the token table.
5. **Focused / Hovered**: Keyboard focus draws `2.dp` `border.focus` focus ring; pointer hover adds subtle surface highlight.
6. **Default**: Resting variant tokens.

Rapid taps are protected by `debounceIntervalMs` (default `300L` ms).

## 5. Responsive Layout Behavior (`Smallest Width × Display Ratio`)

`AuraIconButton` adapts layout density and clustering guidance across all 5 `AuraWindowInfo` viewport profiles:

| Profile | Width / Ratio Condition | Recommended Sizing & Clustering Guidance |
|---|---|---|
| `ExtraSmall` | `sw < 360.dp` | Prefer `Small` (`32.dp` visual, `48.dp` hit) or `Medium` in toolbars; cap inline clusters to 2 visible actions and overflow extras into a `More` icon button. |
| `Compact` | `360–599.dp` | Default `Medium` (`40.dp` visual, `48.dp` hit) in headers and forms; use `Floating` `Large` (`48.dp`) for primary canvas quick triggers. |
| `Medium · Square` | `600–839.dp`, `ratio < 1.35` | Maintain `Medium`/`Large` with `>= 8.dp` cluster gap so symmetric two-hand foldable grips do not mis-tap neighboring icon buttons. |
| `Medium · UltraWide` | `ratio >= 2.1` | Use `Small` (`32.dp` visual) inside compact horizontal bars (`height` stays constrained while preserving `48.dp` touch target) to maximize vertical canvas area. |
| `Expanded` | `width >= 840.dp` | Prefer `Medium` or `Large` with pointer hover feedback and keyboard focus rings across toolbar strips, inspector headers, and two-pane detail bars. |

## 6. Motion & Reduced Motion

- **Press In**: Scales visual container cleanly using `AuraTheme.motion.pressInSpring()` to `pressedScaleDefault` (`0.96f`) for solid/outlined/floating variants or `pressedScaleGhost` (`0.95f`) for `Ghost`, while inner icon micro-compresses to `leadingIconPressedScale` (`0.92f`) via `AuraTheme.motion.iconSpring()`.
- **Minimum Press Latch**: Quick taps inside scrollable containers (`verticalScroll`, `LazyRow`) hold the pressed state for at least `AuraTheme.motion.minPressDurationMillis` (`110ms`) before releasing via `AuraTheme.motion.pressOutSpring()`, matching `AuraButton`.
- **Selection & Color Transitions**: Container, border, and icon tint animate smoothly with `tween(AuraTheme.motion.stateChangeDurationMillis)`.
- **Loading Transition**: Icon-to-spinner switch uses `AuraTheme.motion.loadingCrossfadeDurationMillis`, and the spinner drives `spinnerRotationDurationMillis` (`900ms`) + `spinnerSweepDurationMillis` (`650ms`).
- **Reduced Motion (`ANIMATOR_DURATION_SCALE == 0`)**: Disables tactile scale bounces (`1.0f`) and replaces timed color/loading transitions with `0ms` instant state swaps while preserving contrast changes.

## 7. Accessibility

- **Minimum Touch Target**: Always `>= 48.dp × 48.dp` regardless of `Small` (`32.dp`) or `Medium` (`40.dp`) visual sizing.
- **Semantics**: Exposes `Role.Button`; propagates `selected` when `selected != null`; sets `stateDescription = "In progress"` during `loading`; marks `disabled()` automatically when `!enabled || loading`.
- **Content Description**: Applies `contentDescription` on the semantic node when provided so TalkBack announces `"<action>, Selected/Not selected, Button"`.
- **Contrast**: Ensures `>= 3:1` icon/border contrast against container background and `4.5:1` / `3:1` overlay separation in `Floating` mode via semi-opaque `surface.floating` + `border.subtle`.

## 8. Platform API Contracts

<!-- API:ANDROID:BEGIN -->
```kotlin
package com.example.ds.component

enum class AuraIconButtonVariant {
    Filled,
    Tonal,
    Outlined,
    Ghost,
    Floating,
    Destructive,
}

enum class AuraIconButtonSize {
    Small,
    Medium,
    Large,
}

enum class AuraIconButtonShape {
    Circle,
    RoundedSquare,
}

@Composable
fun AuraIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AuraIconButtonVariant = AuraIconButtonVariant.Ghost,
    size: AuraIconButtonSize = AuraIconButtonSize.Medium,
    shape: AuraIconButtonShape = AuraIconButtonShape.Circle,
    enabled: Boolean = true,
    loading: Boolean = false,
    selected: Boolean? = null,
    contentDescription: String? = null,
    debounceIntervalMs: Long = 300L,
    interactionSource: MutableInteractionSource? = null,
    icon: @Composable () -> Unit,
)
```
<!-- API:ANDROID:END -->

<!-- API:IOS:BEGIN -->
```swift
public enum AuraIconButtonVariant {
    case filled, tonal, outlined, ghost, floating, destructive
}

public enum AuraIconButtonSize {
    case small, medium, large
}

public enum AuraIconButtonShape {
    case circle, roundedSquare
}

public struct AuraIconButton<Icon: View>: View {
    public init(
        variant: AuraIconButtonVariant = .ghost,
        size: AuraIconButtonSize = .medium,
        shape: AuraIconButtonShape = .circle,
        isEnabled: Bool = true,
        isLoading: Bool = false,
        isSelected: Bool? = nil,
        accessibilityLabel: String?,
        action: @escaping () -> Void,
        @ViewBuilder icon: () -> Icon
    )
}
```
<!-- API:IOS:END -->

<!-- API:WEB:BEGIN -->
```ts
export type AuraIconButtonVariant = 'filled' | 'tonal' | 'outlined' | 'ghost' | 'floating' | 'destructive';
export type AuraIconButtonSize = 'small' | 'medium' | 'large';
export type AuraIconButtonShape = 'circle' | 'roundedSquare';

export interface AuraIconButtonProps {
  onClick: () => void;
  variant?: AuraIconButtonVariant;
  size?: AuraIconButtonSize;
  shape?: AuraIconButtonShape;
  disabled?: boolean;
  loading?: boolean;
  selected?: boolean;
  'aria-label'?: string;
  debounceMs?: number;
  children: React.ReactNode;
}
```
<!-- API:WEB:END -->
