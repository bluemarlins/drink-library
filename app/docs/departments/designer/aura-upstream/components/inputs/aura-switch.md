---
id: "aura-switch"
name: "AuraSwitch"
category: "inputs"
status: "ready"
version: "1.0.0"
platforms:
  android: "com.example.ds.component.AuraSwitch"
  ios: "AuraSwitch"
  web: "AuraSwitch"
tokens:
  - "color.brand.primary"
  - "color.brand.onPrimary"
  - "color.surface.sunken"
  - "color.surface.default"
  - "color.content.primary"
  - "color.content.secondary"
  - "color.content.disabled"
  - "color.border.default"
  - "color.border.subtle"
  - "color.border.strong"
  - "color.interaction.disabledContainer"
  - "color.interaction.pressedOverlay"
  - "typography.bodyLarge"
  - "typography.bodySmall"
  - "motion.pressInSpring"
  - "motion.pressOutSpring"
  - "motion.stateChangeDurationMillis"
  - "motion.minPressDurationMillis"
  - "motion.pressedScaleDefault"
---

# AuraSwitch

Instant boolean setting toggle representing an immediate binary state change (`On` / `Off`) without requiring an explicit form Save or Confirm button.

## 1. Overview

`AuraSwitch` provides crisp, physical pill-track toggling for immediate preferences across Aura apps (**Home Launcher** 3D wallpaper depth, **My Planner** all-day schedule mode, **My Finance** real-time price alert notifications, **My Drink Diary** cellar in-stock filter). It supports both compact standalone track toggles (`label == null`) and full-width labeled rows (`label != null`, optional `description`).

---

## 2. Intent & Usage Principles

### When to Use

- Toggling a system setting, view mode, or feature flag that takes effect **immediately** (`SW-RULE-001`).
- Binary preferences inside settings sheets, inspect/playground panels, or schedule metadata rows (`All-day event`).
- Independent boolean flags that do not belong to a multi-select batch checklist.

### When NOT to Use

- **Multi-select checklists or task completion:** Use `AuraCheckbox` instead (`My Planner` todos, `My Finance` batch asset selection).
- **Mutually exclusive 2–5 options:** Use `AuraRadio` inside forms or `AuraSegmentedButton` for compact header view switchers (`Day | Week | Month`).
- **Actions requiring Save/Submit confirmation:** A toggle whose effect stays pending until a bottom Save button is tapped violates `INP-RULE-002`; use `AuraCheckbox`.

### Usage Rules

- **`SW-RULE-001` (Immediate Commit Semantics):** Flipping `AuraSwitch` must apply and persist the change immediately. Never pair a switch with a separate confirmation dialog unless disabling a destructive safeguard.
- **`SW-RULE-002` (Affirmative Label Phrasing):** Always write `label` in the positive, active voice (e.g., `"3D Wallpaper Parallax"`, not `"Disable 3D Parallax"`) so `Checked` unequivocally means enabled.
- **`SW-RULE-003` (48dp Minimum Touch Hit Target):** Both `Medium` (`50 × 30dp`) and `Small` (`42 × 24dp`) visual tracks must live inside at least a `48dp × 48dp` interactive hit area (`44pt × 44pt` on iOS). When `label != null`, the entire row becomes the single `Role.Switch` toggle target.

### Do / Don't

| Do | Don't |
|---|---|
| Use `AuraSwitch` for immediate boolean settings like `"All-day schedule"` or `"Show tasting hints"`. | Use `AuraSwitch` inside a to-do checklist or multi-item deletion list (use `AuraCheckbox`). |
| Keep labels short and positive (`"Auto Rebalance"`, `"Compact Grid"`). | Invert label meaning (`"Hide prices off"`) that causes double-negative confusion. |
| Provide optional `description` beneath `label` when explaining side effects clearly. | Cram long multi-sentence paragraphs into inline compact controls without spacing. |
| Keep track and thumb tokens bound to `AuraTheme.colors` across Light & Dark themes. | Hardcode hex colors (`Color(0x...)`) or wrap Material `Switch` defaults. |

---

## 3. Anatomy & Variants

```text
  Standalone:                Labeled Row Mode:
  ┌───────────────────┐      ┌──────────────────────────────────────────┐
  │ ┌───────────────┐ │      │ 3D Parallax Effect            ┌────────┐ │
  │ │           (●) │ │      │ Subtle depth shift on tilt    │    (●) │ │
  │ └───────────────┘ │      │                               └────────┘ │
  └───────────────────┘      └──────────────────────────────────────────┘
       >= 48dp hit box              Full-row Role.Switch touch target
```

| Size (`AuraSwitchSize`) | Track Width | Track Height | Thumb Diameter | Min Touch Height | Recommended Usage |
|---|---|---|---|---|---|
| `Medium` (default) | `50.dp` | `30.dp` | `24.dp` | `48.dp` | Settings screens, labelled option rows, modals |
| `Small` | `42.dp` | `24.dp` | `18.dp` | `48.dp` | Dense inspector toolbars, preview parameter rows, inline headers |

---

## 4. Token Bindings

| Element | State | Light / Dark Semantic Token |
|---|---|---|
| Track Container | Checked (`Enabled`) | `AuraTheme.colors.brand.primary` |
| Track Container | Unchecked (`Enabled`) | `AuraTheme.colors.surface.sunken` |
| Track Container | `Disabled` (Checked / Unchecked) | `AuraTheme.colors.interaction.disabledContainer` |
| Track Border | Checked (`Enabled`) | `AuraTheme.colors.brand.primary` (`1.5.dp`) |
| Track Border | Unchecked (`Enabled`) | `AuraTheme.colors.border.strong` (`1.5.dp`) |
| Track Border | `Disabled` | `AuraTheme.colors.border.subtle` (`1.dp`) |
| Thumb | Checked (`Enabled`) | `AuraTheme.colors.brand.onPrimary` |
| Thumb | Unchecked (`Enabled`) | `AuraTheme.colors.content.secondary` |
| Thumb | `Disabled` | `AuraTheme.colors.content.disabled` |
| Press Overlay | `Pressed` | `AuraTheme.colors.interaction.pressedOverlay` |
| `label` Text | `Enabled` / `Disabled` | `AuraTheme.colors.content.primary` / `content.disabled` (`typography.bodyLarge`) |
| `description` Text | `Enabled` / `Disabled` | `AuraTheme.colors.content.secondary` / `content.disabled` (`typography.bodySmall`) |

---

## 5. Responsive Layout Behavior

`AuraSwitch` adapts its row footprint and typography spacing cleanly across all Aura viewport classes and aspect profiles (`AuraWindowInfo`):

| Width / Display Profile | Standalone Behavior | Labeled Row (`label != null`) Behavior |
|---|---|---|
| `ExtraSmall` (`< 360dp`) | Fixed track dimensions (`Medium` or `Small`) centered in `48.dp` touch bounds. | `label` and `description` occupy flexible `weight(1f)` with `12.dp` horizontal gap before trailing track; text wraps gracefully up to 2 lines without clipping track. |
| `Compact` (`360–599dp`) | Maintains standard track geometry aligned to grid slots or trailing edge of list rows. | Full-width settings row (`fillMaxWidth()`) keeps track trailing-aligned with `16.dp` end breathing room on phones. |
| `Square` (`~1:1` Pop-up / Split) | Preserves `48.dp` minimum touch height during multi-window split resize without collapsing track width. | Prevents crowded multi-column wrapping by stacking row vertically below sibling switches inside compact pop-up panes. |
| `UltraWide` (`> 21:9` Outer Screen) | Keeps compact inspector controls anchored cleanly inside narrow outer strips. | Label takes single line while keeping track anchored strictly on the trailing edge of the row. |
| `Expanded` (`>= 840dp` Tablet) | Sits comfortably in inspector panes or secondary split panels alongside live previews. | Constrained within parent container (`AuraCard` or pane column `maxContentWidth`) so label and switch never drift awkwardly far apart across wide monitors. |

---

## 6. State Machine & Interaction Precedence

**State Precedence:** `Disabled` > `Pressed` > `Checked` / `Unchecked` (focused ring highlights `border.focus` when keyboard/DPAD focus is active on enabled controls).

```mermaid
stateDiagram-v2
    [*] --> Unchecked
    Unchecked --> Pressed: Pointer Down (onCheckedChange != null)
    Pressed --> Checked: Pointer Up / Click
    Checked --> Pressed: Pointer Down
    Pressed --> Unchecked: Pointer Up / Click
    Unchecked --> Disabled: enabled = false
    Checked --> Disabled: enabled = false
```

---

## 7. Motion & Micro-interactions

Consumes `AuraTheme.motion` exclusively (`AuraMotion`):

1. **Thumb Translation:** Slide offset `0f -> 1f` animates with `AuraTheme.motion.pressOutSpring()`, giving crisp spring deceleration when landing on either end of the track.
2. **Tactile Press Feedback:** Uses `rememberAuraTactilePressed(interactionSource, motion.minPressDurationMillis)` (`110ms` minimum latch on fast taps). While pressed:
   - Overall visual track subtly compresses to `AuraTheme.motion.pressedScaleDefault` (`0.96f`) via `pressInSpring()` / `pressOutSpring()`.
   - Thumb gently elongates horizontally (`1.18x` width ratio) to feel physically pliable under the fingertip before snapping into its resting circle.
3. **Color Crossfade:** Track fill, border, and thumb tint transition via `tween(durationMillis = AuraTheme.motion.stateChangeDurationMillis)` (`150ms`).
4. **Reduced Motion Compliance:** When system `ANIMATOR_DURATION_SCALE == 0f` (`rememberAuraReducedMotion()`), spring/tween animations degrade to instant `snap()`.

---

## 8. Cross-Platform API Contract

<!-- API:ANDROID:BEGIN -->
```kotlin
enum class AuraSwitchSize { Medium, Small }

@Composable
fun AuraSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    size: AuraSwitchSize = AuraSwitchSize.Medium,
    enabled: Boolean = true,
    label: String? = null,
    description: String? = null,
)
```
<!-- API:ANDROID:END -->

<!-- API:IOS:BEGIN -->
```swift
public enum AuraSwitchSize: Sendable {
    case medium
    case small
}

public struct AuraSwitch: View {
    public init(
        isOn: Binding<Bool>,
        size: AuraSwitchSize = .medium,
        isEnabled: Bool = true,
        label: String? = nil,
        description: String? = nil
    )
}
```
<!-- API:IOS:END -->

<!-- API:WEB:BEGIN -->
```ts
export type AuraSwitchSize = 'medium' | 'small';

export interface AuraSwitchProps {
  checked: boolean;
  onCheckedChange?: (checked: boolean) => void;
  size?: AuraSwitchSize;
  disabled?: boolean;
  label?: string;
  description?: string;
  className?: string;
}
```
<!-- API:WEB:END -->

---

## 9. Accessibility & Test Checklist

- [ ] Interactive root registers `Role.Switch` via `Modifier.toggleable(value = checked, ...)` when `onCheckedChange != null`.
- [ ] When `label != null`, TalkBack announces `"<label>, <description>, switch, on/off"` as a single focusable node rather than separate label + toggle nodes.
- [ ] Standalone track enforces `defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)` and labeled rows enforce `heightIn(min = 48.dp)`.
- [ ] Unchecked track border uses `border.strong` to preserve `>= 3:1` non-text contrast against `surface.sunken` in both Light and Dark themes.
- [ ] Verified reduced-motion (`ANIMATOR_DURATION_SCALE = 0`) skips spring bounce without dropping state changes.
