---
id: "aura-fab"
name: "AuraFab"
category: "actions"
status: "ready"
platforms:
  android: "com.example.ds.component.AuraFab"
  ios: "AuraFab"
  web: "AuraFab"
tokens:
  - "color.brand.primary"
  - "color.brand.onPrimary"
  - "color.brand.subtle"
  - "color.brand.onSubtle"
  - "color.surface.floating"
  - "color.content.primary"
  - "color.content.disabled"
  - "color.border.subtle"
  - "color.border.focus"
  - "color.interaction.disabledContainer"
  - "color.interaction.pressedOverlay"
  - "typography.labelLarge"
  - "typography.labelMedium"
  - "typography.titleSmall"
  - "motion.pressInSpring"
  - "motion.pressOutSpring"
  - "motion.iconSpring"
  - "motion.stateChangeDurationMillis"
  - "motion.loadingCrossfadeDurationMillis"
  - "motion.spinnerRotationDurationMillis"
  - "motion.spinnerSweepDurationMillis"
  - "motion.minPressDurationMillis"
  - "motion.pressedScaleDefault"
  - "motion.pressedScaleLarge"
  - "motion.leadingIconPressedScale"
---

# AuraFab

Persistent floating action button for the single primary creation entry point on a browse, schedule, ledger, or journal screen.

## 1. Intent & Usage Principles

Aligned with [00-button-principles.md](00-button-principles.md), `AuraFab` floats at the predictable creation anchor of a screen (`AuraScaffold` bottom-end slot on handsets, or top-pane action anchor on large tablets) so users can initiate their primary creation flow (`+ New Event/Diary`, `+ Add Asset`, `+ Log Bottle`) immediately while scrolling. It supports both compact icon-only silhouettes and Extended pill labels (`text` + `expanded`).

### Context Rules

- **`FAB-RULE-001` (Single Screen Creation Anchor)**: A screen may render **at most one `AuraFab`**, reserved strictly for the primary constructive creation task—never for destructive triggers (`Delete`), secondary navigation, or multi-step form submissions (use `AuraButton`).
- **`FAB-RULE-002` (Extended-on-Rest, Compact-on-Scroll)**: Prefer `text != null` with `expanded = true` when first entering list/ledger screens so new users recognize the creation action at a glance; collapse smoothly to icon-only (`expanded = false`) as vertical scroll offset increases so underlying content stays unobstructed.
- **`FAB-RULE-003` (Surface Materiality Restraint)**: Default to `Primary` or `Secondary` over solid lists and forms. Reserve `Glass` strictly for translucent wallpaper/camera/3D launcher canvases (**Home Launcher**, **Drink Diary** hero cards) where frosted depth preserves legibility without heavy slabs.
- **`FAB-RULE-004` (Mandatory Collapsed Accessibility Label)**: Whenever `text` is null OR `expanded == false`, callers MUST pass a descriptive `contentDescription` (e.g., `"Create diary entry"`) so screen readers announce the exact creation target even when the visible pill is collapsed.

### When to Use

- Launching creation sheets or editors from a feed/grid/calendar (`+ New Diary`, `+ Add Transaction`, `+ Log Tasting Note`).
- Persistent single-thumb creation control sitting inside `AuraScaffold(floatingActionButton = ...)`.
- Scroll-responsive Extended FAB that clarifies ambiguous creation actions (`"+ Add Asset"`) before collapsing to compact `+`.

### When NOT to Use

- Form bottom bars (`Save`, `Confirm`, `Cancel`) -> use `AuraButton`.
- Repeated item cards or list row inline controls -> use `AuraIconButton` or `AuraButton(Secondary/Ghost)`.
- Filtering, tagging, or switching modes -> use `AuraChipButton` or `AuraSegmentedButton`.

### Do / Don't

| Do | Don't |
|---|---|
| Limit screens to a single `AuraFab` mapped to the core constructive creation flow. | Stack two or more FABs vertically or use `AuraFab` for delete/reset actions. |
| Supply `contentDescription` even when `text` is passed, so collapsing (`expanded = false`) preserves TalkBack clarity. | Rely only on `text` for semantics and leave `contentDescription = null` on collapsible Extended FABs. |
| Collapse extended FAB labels cleanly (`expanded = false`) when the user scrolls down a dense list. | Keep wide text-heavy Extended FABs blocking cards and bottom metrics while scrolling. |
| Use `Glass` only above rich imagery, wallpapers, or 3D overlays with sufficient edge border definition. | Place translucent `Glass` FABs over plain monochrome backgrounds where `Primary` or `Surface` reads stronger. |

## 2. Variants & Sizes

### Variants (`AuraFabVariant`)

1. **`Primary`** — Solid high-emphasis brand pill (`brand.primary` / `brand.onPrimary`), default for the primary creation CTA.
2. **`Secondary`** — Soft brand tint (`brand.subtle` / `brand.onSubtle`) with refined shadow for calm surfaces or split-pane secondary creation.
3. **`Surface`** — Elevated floating neutral pill (`surface.floating` / `content.primary`) + subtle `border.subtle` for monochrome or neutral-first editorial tools.
4. **`Glass`** — Frosted translucent overlay pill (`surface.floating @ 0.80 alpha` + luminous `border.subtle`) crafted for photography, bottle labels, and launcher canvases.

### Sizes (`AuraFabSize`)

| Size | Visual Container Height / Width | Min Hit Target | Icon Size | Label Typography (Extended) | Corner Radius |
|---|---|---|---|---|---|
| `Small` | `44.dp` | `48.dp × 48.dp` | `20.dp` | `AuraTheme.typography.labelMedium` | `CircleShape` (`50%`) |
| `Regular` | `56.dp` | `56.dp × 56.dp` | `24.dp` | `AuraTheme.typography.labelLarge` | `CircleShape` (`50%`) |
| `Large` | `64.dp` | `64.dp × 64.dp` | `28.dp` | `AuraTheme.typography.titleSmall` | `CircleShape` (`50%`) |

Extended FABs maintain the exact visual height (`44.dp` / `56.dp` / `64.dp`) while smoothly expanding horizontal padding (`start = 16.dp`, `end = 20.dp` on `Regular`) to house the label.

## 3. Semantic Token Bindings

All colors, typography, and motion resolve through `AuraTheme`:

| Variant | Container | Content (Icon / Label / Spinner) | Border | Elevation Shadow | Disabled (`enabled == false`) |
|---|---|---|---|---|---|
| `Primary` | `brand.primary` | `brand.onPrimary` | `border.focus` when focused | `8.dp` (`0.dp` disabled) | `interaction.disabledContainer` / `content.disabled` |
| `Secondary` | `brand.subtle` | `brand.onSubtle` | `border.focus` when focused | `6.dp` (`0.dp` disabled) | `interaction.disabledContainer` / `content.disabled` |
| `Surface` | `surface.floating` | `content.primary` | `1.dp` `border.subtle` (`2.dp` `border.focus` focused) | `8.dp` (`0.dp` disabled) | `interaction.disabledContainer` / `content.disabled` |
| `Glass` | `surface.floating` (`0.80f` alpha) | `content.primary` | `1.dp` `border.subtle` (`2.dp` `border.focus` focused) | `6.dp` (`0.dp` disabled) | `interaction.disabledContainer` / `content.disabled` |

## 4. State Precedence & Interaction

1. **Disabled (`!enabled`)**: Clicks suppressed, `interaction.disabledContainer`, `content.disabled`, `0.dp` shadow, no press scale.
2. **Loading (`loading == true`)**: Clicks suppressed, retains active variant surface & content colors, crossfades icon to compact spinner (`stateDescription = "In progress"`), collapses label smoothly if extended or locks footprint safely.
3. **Pressed**: Tactile scale spring (`pressedScaleDefault` / `pressedScaleLarge`) + `interaction.pressedOverlay` wash latched for `minPressDurationMillis`.
4. **Focused / Hovered**: Keyboard focus draws `2.dp` `border.focus` outline; pointer hover adds subtle `pressedOverlay` tint.
5. **Extended (`!text.isNullOrEmpty() && expanded`) vs Compact**: Smooth horizontal expansion/collapse.
6. **Default**: Resting floating elevation & variant colors.

Rapid duplicate creations are prevented by `debounceIntervalMs` (default `400L` ms).

## 5. Responsive Layout Behavior (`Smallest Width × Display Ratio`)

| Profile | Width / Ratio Condition | Placement & Extended Adaptation |
|---|---|---|
| `ExtraSmall` | `sw < 360.dp` | Prefer `Small` (`44.dp` visual, `48.dp` hit) or compact `Regular` icon-only (`expanded = false`) at `12.dp` edge margin so narrow 320–359dp feeds stay legible. |
| `Compact` | `360–599.dp` | Standard `Regular` (`56.dp`) Extended on initial load (`expanded = true`), collapsing automatically once scroll exceeds threshold (`16.dp` margin). |
| `Medium · Square` | `600–839.dp`, `ratio < 1.35` | Maintain bottom-end `Regular` or `Large` placement inside foldable square panes with `24.dp` outer clearance (`AuraScaffold` safe insets). |
| `Medium · UltraWide` | `ratio >= 2.1` | Collapse extended label (`expanded = false`) or use `Small` (`44.dp` visual / `48.dp` hit) on bottom-end so shallow landscape viewports stay unobstructed. |
| `Expanded` | `width >= 840.dp` | Anchor `Regular` / `Large` Extended FAB within the bottom-end of the primary browse pane or top-start rail header; supports pointer hover lift and keyboard focus ring. |

## 6. Motion & Reduced Motion

- **Press In / Out**: `Regular` and `Small` compress to `AuraTheme.motion.pressedScaleDefault` (`0.96f`), while `Large` compresses to `AuraTheme.motion.pressedScaleLarge` (`0.975f`) via `pressInSpring()` / `pressOutSpring()`, and icon micro-scales to `leadingIconPressedScale` (`0.92f`) via `iconSpring()`.
- **Minimum Press Latch**: Holds pressed state for at least `AuraTheme.motion.minPressDurationMillis` (`110ms`) on fast taps.
- **Extend / Collapse Transition**: Horizontal label container animates width and alpha via `expandHorizontally(pressOutSpring()) + fadeIn(tween(stateChangeDurationMillis))` / `shrinkHorizontally(...) + fadeOut(...)`.
- **Loading Transition**: Icon crossfades with `loadingCrossfadeDurationMillis` (`120ms`) into animated stroke spinner (`spinnerRotationDurationMillis`, `spinnerSweepDurationMillis`).
- **Reduced Motion (`ANIMATOR_DURATION_SCALE == 0`)**: Disables press scaling (`1.0f`) and replaces animated expand/shrink transitions with instant `EnterTransition.None` / `ExitTransition.None`.

## 7. Accessibility

- **Minimum Touch Target**: Enforces at least `48.dp × 48.dp` even when `Small` (`44.dp`) visual frame is used.
- **Semantics**: Exposes `Role.Button`; sets `contentDescription` directly on semantic node when provided; sets `stateDescription = "In progress"` during `loading`; disables interaction semantics automatically when `!enabled || loading`.
- **Contrast**: Ensures `>= 4.5:1` label contrast and `>= 3:1` icon/container separation across both Light and Dark themes.

## 8. Platform API Contracts

<!-- API:ANDROID:BEGIN -->
```kotlin
package com.example.ds.component

enum class AuraFabVariant {
    Primary,
    Secondary,
    Surface,
    Glass,
}

enum class AuraFabSize {
    Small,
    Regular,
    Large,
}

@Composable
fun AuraFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    expanded: Boolean = true,
    variant: AuraFabVariant = AuraFabVariant.Primary,
    size: AuraFabSize = AuraFabSize.Regular,
    enabled: Boolean = true,
    loading: Boolean = false,
    contentDescription: String? = null,
    debounceIntervalMs: Long = 400L,
    interactionSource: MutableInteractionSource? = null,
    icon: @Composable () -> Unit,
)
```
<!-- API:ANDROID:END -->

<!-- API:IOS:BEGIN -->
```swift
public enum AuraFabVariant {
    case primary, secondary, surface, glass
}

public enum AuraFabSize {
    case small, regular, large
}

public struct AuraFab<Icon: View>: View {
    public init(
        text: String? = nil,
        isExpanded: Bool = true,
        variant: AuraFabVariant = .primary,
        size: AuraFabSize = .regular,
        isEnabled: Bool = true,
        isLoading: Bool = false,
        accessibilityLabel: String?,
        action: @escaping () -> Void,
        @ViewBuilder icon: () -> Icon
    )
}
```
<!-- API:IOS:END -->

<!-- API:WEB:BEGIN -->
```ts
export type AuraFabVariant = 'primary' | 'secondary' | 'surface' | 'glass';
export type AuraFabSize = 'small' | 'regular' | 'large';

export interface AuraFabProps {
  onClick: () => void;
  text?: string;
  expanded?: boolean;
  variant?: AuraFabVariant;
  size?: AuraFabSize;
  disabled?: boolean;
  loading?: boolean;
  'aria-label'?: string;
  debounceMs?: number;
  icon: React.ReactNode;
}
```
<!-- API:WEB:END -->
