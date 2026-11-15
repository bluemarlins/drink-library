---
id: "aura-dialog"
name: "AuraDialog"
category: "containment"
status: "ready"
version: "1.0.0"
platforms:
  android: "com.example.ds.component.AuraDialog"
  ios: "AuraDialog"
  web: "AuraDialog"
tokens:
  - "color.surface.floating"
  - "color.surface.raised"
  - "color.content.primary"
  - "color.content.secondary"
  - "color.brand.primary"
  - "color.status.error.solid"
  - "color.status.error.subtle"
  - "color.status.error.onSubtle"
  - "color.border.subtle"
  - "color.interaction.scrim"
  - "typography.headlineSmall"
  - "typography.bodyMedium"
  - "motion.pressInSpring"
  - "motion.pressOutSpring"
  - "motion.stateChangeDurationMillis"
---

# AuraDialog

Focused modal confirmation, alert, and high-consequence action dialog (`Standard`, `Destructive`, `Glass`), built on Aura tokens without Material 3 modal chrome.

## 1. Intent & Usage Principles

### When to Use

- Confirming irreversible or high-impact actions such as deleting a cellar bottle (**My Drink Diary**), clearing a schedule series (**My Planner**), or removing an asset entry (**My Finance**).
- Presenting focused, concise modal prompts that require explicit acknowledgment before returning to the underlying flow.
- Rendering a spatial fluid confirmation prompt over translucent glass backdrops (`AuraDialogVariant.Glass` with `GlassStyle.FROSTED` / `LIQUID`).

### When NOT to Use

- **Multi-field forms, long pickers, or secondary navigation:** Use a dedicated sheet, screen, or two-pane detail flow (`CNT-RULE-002`).
- **Passive inline grouped content:** Use `AuraCard`.
- **Transient non-blocking status feedback:** Use `AuraBadge` or an inline status banner.

### Usage Rules

- **`DLG-RULE-001` (Action Verb Clarity):** Always label `confirmText` with a concrete verb (`"Delete Bottle"`, `"Clear Draft"`, `"Confirm"`) rather than ambiguous `"OK"` / `"Yes"`.
- **`DLG-RULE-002` (Destructive Isolation):** Whenever a confirmation causes irreversible deletion or financial reset, set `variant = AuraDialogVariant.Destructive` so the confirm CTA renders in `AuraButtonVariant.Destructive` (`status.error.solid`) paired with an error-tinted indicator banner.
- **`DLG-RULE-003` (Action Count Cap):** Restrict dialog footer controls to at most two clear buttons (`dismissText` Outline/Ghost secondary button on the start + `confirmText` primary action button on the end) stacked vertically on ultra-narrow cover screens.

### Do / Don't

| Do | Don't |
|---|---|
| Use `variant = AuraDialogVariant.Destructive` with specific verb `"Delete Entry"` for irreversible deletion. | Style destructive delete buttons with calm primary brand blue or vague `"OK"` labels. |
| Keep dialog messages scannable (`1–3` lines) or pair with one compact inline summary slot. | Embed full scrollable multi-step forms or 10-item pickers inside an alert modal. |
| Allow dismiss via `dismissText` button, scrim tap (`dismissOnClickOutside`), and system Back unless strict compliance requires explicit choice. | Trap users inside a dialog with no cancel/back escape path. |
| Keep both dialog surfaces and buttons anchored to `AuraTheme.colors` and `AuraButton`. | Render Material `AlertDialog` defaults breaking Light/Dark surface hierarchy. |

---

## 2. Anatomy & Variants

```text
  ╭──────────────────────────────────────────╮
  │  [!]  Delete tasting note?               │  <- Optional warning pill (Destructive) + Title
  │                                          │
  │  This removes the 1998 Barolo note       │  <- Concise message + optional custom content slot
  │  and tasting score permanently.          │
  │                                          │
  │            [  Cancel  ]  [ Delete Note ] │  <- Footer AuraButtons (Outline + Primary/Destructive)
  ╰──────────────────────────────────────────╯
```

| Variant (`AuraDialogVariant`) | Surface Material | Confirm CTA Style | Accent Indicator |
|---|---|---|---|
| `Standard` (default) | `surface.floating` (`24.dp` corner radius) | `AuraButtonVariant.Primary` | None |
| `Destructive` | `surface.floating` + subtle alert pill | `AuraButtonVariant.Destructive` | `status.error.subtle` / `status.error.solid` alert emblem |
| `Glass` | `GlassSurface(glassStyle, 24.dp)` | `AuraButtonVariant.Primary` | Optical blur/refraction bevel + specular stroke |

---

## 3. Token Bindings

| Element | Variant / State | Light / Dark Semantic Token |
|---|---|---|
| Scrim Overlay | `Visible` | `AuraTheme.colors.interaction.scrim` |
| Modal Surface | `Standard` / `Destructive` / `Glass` | `surface.floating` (`border.subtle`) / `GlassSurface(glassStyle)` |
| Destructive Emblem | `Destructive` | `status.error.subtle` container + `status.error.solid` glyph |
| Title Text | `All` | `AuraTheme.colors.content.primary` (`typography.headlineSmall`) |
| Message Body | `All` | `AuraTheme.colors.content.secondary` (`typography.bodyMedium`) |
| Action Buttons | `Standard` / `Destructive` | `AuraButton(Primary/Destructive)` + `AuraButton(Outline)` |

---

## 4. State Precedence & Accessibility

### State Precedence

`Hidden` (`!visible`) > `ConfirmDisabled` (`!confirmEnabled`) > `Destructive` > `Glass` > `Standard`.

### Accessibility

- Wraps modal in Jetpack Compose `Dialog` (`DialogProperties`) so TalkBack traps focus cleanly inside the dialog and restores focus to the trigger control upon dismissal.
- Marks `title` with `semantics { heading() }` for immediate modal announcement.
- Enforces `>= 48.dp` touch height on both confirm and dismiss `AuraButton`s.

---

## 5. Motion

Consumes `AuraTheme.motion`:

1. **Modal Entrance & Exit:** Container animates subtly in scale (`0.94f -> 1.0f`) and alpha (`0f -> 1f`) using `AuraTheme.motion.pressOutSpring()`, while state transitions (`confirmEnabled`) crossfade over `AuraTheme.motion.stateChangeDurationMillis`.
2. **Action Tactile Physics:** Footer buttons inherit `AuraButton` spring press compression (`pressInSpring()` / `pressOutSpring()`).
3. **Reduced Motion:** Uses `snap()` when `rememberAuraReducedMotion()` is active.

---

## 6. Responsive Layout Behavior

| Width / Display Profile | Dialog Sizing & Action Button Layout |
|---|---|
| `ExtraSmall` (`< 360dp`) | Expands to `fillMaxWidth(0.94f)` with `20.dp` inner padding; when action labels are long, stacks footer buttons vertically (`Confirm` above `Dismiss`) so neither truncates. |
| `Compact` (`360–599dp`) | Centred modal (`widthIn(min = 280.dp, max = 380.dp)`) with horizontal end-aligned button row (`10.dp` gap). |
| `Square` (`~1:1` Pop-up / Foldable) | Centered safely above hinge/split boundaries with `widthIn(max = 400.dp)` and comfortable scrim margin. |
| `UltraWide` (`> 21:9`) | Vertical padding adjusts cleanly (`heightIn`) so title, message, and actions stay comfortably visible without clipping in short landscape frames. |
| `Expanded` (`>= 840dp` Tablet) | Remains compact and focused (`widthIn(max = 440.dp)`), never stretching across an entire tablet screen. |

---

## 7. Cross-Platform API Contract

<!-- API:ANDROID:BEGIN -->
```kotlin
enum class AuraDialogVariant {
    Standard,
    Destructive,
    Glass,
}

@Composable
fun AuraDialog(
    visible: Boolean,
    title: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AuraDialogVariant = AuraDialogVariant.Standard,
    glassStyle: GlassStyle = GlassStyle.FROSTED,
    message: String? = null,
    confirmText: String = "Confirm",
    dismissText: String? = "Cancel",
    confirmEnabled: Boolean = true,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    content: (@Composable () -> Unit)? = null,
)
```
<!-- API:ANDROID:END -->

<!-- API:IOS:BEGIN -->
```swift
public enum AuraDialogVariant: Sendable {
    case standard
    case destructive
    case glass
}

public struct AuraDialog<Content: View>: View {
    public init(
        visible: Bool,
        title: String,
        onConfirm: @escaping () -> Void,
        onDismissRequest: @escaping () -> Void,
        variant: AuraDialogVariant = .standard,
        glassStyle: GlassStyle = .frosted,
        message: String? = nil,
        confirmText: String = "Confirm",
        dismissText: String? = "Cancel",
        confirmEnabled: Bool = true,
        dismissOnBackPress: Bool = true,
        dismissOnClickOutside: Bool = true,
        @ViewBuilder content: () -> Content
    )
}
```
<!-- API:IOS:END -->

<!-- API:WEB:BEGIN -->
```ts
export type AuraDialogVariant = 'Standard' | 'Destructive' | 'Glass';

export interface AuraDialogProps {
  visible: boolean;
  title: string;
  onConfirm: () => void;
  onDismissRequest: () => void;
  variant?: AuraDialogVariant;
  glassStyle?: GlassStyle;
  message?: string;
  confirmText?: string;
  dismissText?: string | null;
  confirmEnabled?: boolean;
  dismissOnBackPress?: boolean;
  dismissOnClickOutside?: boolean;
  children?: unknown;
}
```
<!-- API:WEB:END -->
