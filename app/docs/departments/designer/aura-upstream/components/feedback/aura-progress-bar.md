---
id: "aura-progress-bar"
name: "AuraProgressBar"
category: "feedback"
status: "ready"
layer: "content"
material_allowed: ["Solid"]
motion_track: "Standard"
version: "1.1.0"
platforms:
  android: "com.example.ds.component.AuraProgressBar"
  ios: "AuraProgressBar"
  web: "AuraProgressBar"
tokens:
  - "color.brand.primary"
  - "color.surface.sunken"
  - "color.status.success.solid"
  - "color.status.warning.solid"
  - "color.status.error.solid"
  - "color.status.info.solid"
  - "shapes.extraSmall"
  - "shapes.small"
  - "motion.stateChangeDurationMillis"
---

# AuraProgressBar

Deterministic progress indicators (`AuraLinearProgressBar` and `AuraStepProgressBar`) backed by `AuraTheme.colors`, `AuraTheme.shapes`, and `AuraTheme.motion`.

## 1. Intent & Usage Principles

### When to Use

- **`AuraLinearProgressBar`:** Communicating continuous completion fraction (`0f..1f`), readiness thresholds, or quota usage inside content cards.
- **`AuraStepProgressBar`:** Communicating discrete multi-step wizard or onboarding progress (`currentStep` out of `totalSteps`).

### When NOT to Use & Alternatives

- **Interactive value scrubbing:** Use `AuraSlider` when the user drags to adjust a value.
- **Inline status labels or unread counts:** Use `AuraBadge`.
- **Transient operation results:** Use `AuraSnackbar`.

### Usage Rules

- **`PRG-RULE-001` (Bounded Progress):** Clamp continuous `progress` to `0f..1f` and discrete `currentStep` to `0..totalSteps` so layout widths never overflow.
- **`PRG-RULE-002` (Semantic Color Alignment):** Use `Brand` for neutral goal completion and `Success` / `Warning` / `Error` / `Info` only when communicating explicit semantic status (`FBK-RULE-001`).

### Do / Don't

| Do | Don't |
|---|---|
| Pair `AuraLinearProgressBar` with clear text context (e.g., `"4 / 6 entries"`). | Render a bare progress bar with no explanation of what is progressing. |
| Use `AuraStepProgressBar` for 2–8 discrete questionnaire or onboarding steps. | Render `AuraStepProgressBar` with 30+ tiny slivers; switch to `AuraLinearProgressBar`. |
| Consume `AuraTheme.shapes` and `AuraTheme.colors.surface.sunken` track tokens. | Hardcode corner radii or raw hex colors inside app-local progress boxes. |

---

## 2. Anatomy & Variants

```text
  AuraLinearProgressBar:
  [████████████░░░░░░░░░░░░░]   6.dp height, AuraTheme.shapes.small

  AuraStepProgressBar (currentStep = 3, totalSteps = 5):
  [████] [████] [████] [░░░░] [░░░░]   4.dp height, 4.dp gap, AuraTheme.shapes.extraSmall
```

| Variant (`AuraProgressVariant`) | Active Indicator Token | Track Token |
|---|---|---|
| `Brand` (default) | `AuraTheme.colors.brand.primary` | `AuraTheme.colors.surface.sunken` |
| `Success` | `AuraTheme.colors.status.success.solid` | `AuraTheme.colors.surface.sunken` |
| `Warning` | `AuraTheme.colors.status.warning.solid` | `AuraTheme.colors.surface.sunken` |
| `Error` | `AuraTheme.colors.status.error.solid` | `AuraTheme.colors.surface.sunken` |
| `Info` | `AuraTheme.colors.status.info.solid` | `AuraTheme.colors.surface.sunken` |

---

## 3. Semantic Token Bindings

| Element | Variant / State | Light / Dark Semantic Token |
|---|---|---|
| Inactive Track | `Rest` | `AuraTheme.colors.surface.sunken` |
| Active Indicator | `By Variant` | `brand.primary` / `status.{success,warning,error,info}.solid` |
| Corner Shape | `Linear` / `Step` | `AuraTheme.shapes.small` / `AuraTheme.shapes.extraSmall` |

---

## 4. Accessibility

- Exposes `ProgressBarRangeInfo` via `Modifier.semantics(mergeDescendants = true)` so TalkBack announces completion ratio accurately.
- Supports optional `contentDescription` for meaningful spoken summaries (e.g., `"Step 2 of 4"`).
- Track (`surface.sunken`) and active indicator (`brand.primary` / `status.*.solid`) maintain crisp visual separation in both Light and Dark themes.

---

## 5. Interaction Motion Contract

Consumes `AuraTheme.motion`:

1. **Progress & Step Transitions:** `AuraLinearProgressBar` animates fraction changes and `AuraStepProgressBar` animates segment activation colors via `tween(durationMillis = AuraTheme.motion.stateChangeDurationMillis)`.
2. **Reduced Motion:** Snaps immediately (`snap()`) when `AuraTheme.motion.reduceMotion` is `true` while preserving semantic `ProgressBarRangeInfo`.

---

## 6. Responsive Layout Behavior

| Width / Display Profile | Progress Bar Adaptation |
|---|---|
| `ExtraSmall` (`< 360dp`) | Expands `fillMaxWidth()` within narrow margins while keeping `4.dp` segment gaps legible. |
| `Compact` (`360–599dp`) | Fills container or card inner width cleanly. |
| `Square` (`~1:1` Foldable) | Equal-weight step segments (`Modifier.weight(1f)`) distribute evenly across wider viewports. |
| `UltraWide` (`> 21:9`) | Bounded by parent `maxContentWidth` form/card container to avoid excessively wide bars. |
| `Expanded` (`>= 840dp`) | Scales cleanly inside multi-column cards and detail panes. |

---

## 7. Cross-Platform API Contract

<!-- API:ANDROID:BEGIN -->
```kotlin
enum class AuraProgressVariant {
    Brand,
    Success,
    Warning,
    Error,
    Info,
}

@Composable
fun AuraLinearProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    variant: AuraProgressVariant = AuraProgressVariant.Brand,
    height: Dp = 6.dp,
    shape: Shape = AuraTheme.shapes.small,
    trackColor: Color = AuraTheme.colors.surface.sunken,
    indicatorColor: Color = Color.Unspecified,
    contentDescription: String? = null,
)

@Composable
fun AuraStepProgressBar(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
    variant: AuraProgressVariant = AuraProgressVariant.Brand,
    height: Dp = 4.dp,
    segmentSpacing: Dp = 4.dp,
    shape: Shape = AuraTheme.shapes.extraSmall,
    activeColor: Color = Color.Unspecified,
    inactiveColor: Color = AuraTheme.colors.surface.sunken,
    contentDescription: String? = null,
)
```
<!-- API:ANDROID:END -->

<!-- API:IOS:BEGIN -->
```swift
public enum AuraProgressVariant: Sendable {
    case brand
    case success
    case warning
    case error
    case info
}

public struct AuraLinearProgressBar: View {
    public init(
        progress: Double,
        variant: AuraProgressVariant = .brand,
        height: CGFloat = 6,
        contentDescription: String? = nil
    )
}

public struct AuraStepProgressBar: View {
    public init(
        currentStep: Int,
        totalSteps: Int,
        variant: AuraProgressVariant = .brand,
        height: CGFloat = 4,
        segmentSpacing: CGFloat = 4,
        contentDescription: String? = nil
    )
}
```
<!-- API:IOS:END -->

<!-- API:WEB:BEGIN -->
```ts
export type AuraProgressVariant = 'Brand' | 'Success' | 'Warning' | 'Error' | 'Info';

export interface AuraLinearProgressBarProps {
  progress: number;
  variant?: AuraProgressVariant;
  height?: number;
  contentDescription?: string;
}

export interface AuraStepProgressBarProps {
  currentStep: number;
  totalSteps: number;
  variant?: AuraProgressVariant;
  height?: number;
  segmentSpacing?: number;
  contentDescription?: string;
}
```
<!-- API:WEB:END -->
