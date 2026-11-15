---
id: "aura-slider"
name: "AuraSlider"
category: "inputs"
status: "ready"
version: "1.0.0"
platforms:
  android: "com.example.ds.component.AuraSlider"
  ios: "AuraSlider"
  web: "AuraSlider"
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
  - "typography.labelLarge"
  - "typography.labelMedium"
  - "typography.bodySmall"
  - "typography.monoMedium"
  - "motion.pressInSpring"
  - "motion.pressOutSpring"
  - "motion.stateChangeDurationMillis"
  - "motion.pressedScaleDefault"
---

# AuraSlider

Continuous and stepped range slider designed for sensory flavor profiling (**My Drink Diary** Body/Tannin/Acidity/Peat), wallpaper blur/3D parallax depth (**Home Launcher**), and portfolio allocation percentages (**My Finance**).

## 1. Intent & Usage Principles

### When to Use

- Selecting a continuous or quantized numeric value inside a fixed visual range (`0f..1f`, `1..5`, `6°C..20°C`, `0%..100%`).
- Adjusting tasting intensity notes where relative position along a spectrum is meaningful (**My Drink Diary**).
- Tuning live visual controls such as blur strength or haptic amplitude (**Home Launcher**).

### When NOT to Use

- **Precise monetary amounts (`₩15,430,200`) or ABV decimals (`46.3%`):** Use `AuraNumberField` for exact keyboard entry.
- **5-star satisfaction rating:** Use `AuraRatingBar`.
- **Binary On/Off toggle:** Use `AuraSwitch`.

### Usage Rules

- **`SLD-RULE-001` (Paired Header Readout):** Supply `label` and live formatted `valueLabel` whenever exact numeric feedback helps comprehension so users see the active value above the thumb without covering it with their finger.
- **`SLD-RULE-002` (Stepped Snap When Discrete):** Use `steps > 0` for discrete scales (e.g., `steps = 3` for a `1..5` sensory rubric -> 5 discrete stops: `1, 2, 3, 4, 5`) with crisp track tick markers.
- **`SLD-RULE-003` (48dp Minimum Interactive Track Box):** Keep the visual track sleek (`6.dp`) while preserving a full `48.dp` height touch box for effortless dragging.

### Do / Don't

| Do | Don't |
|---|---|
| Pair `label = "Body"` and `valueLabel = "Full (4 / 5)"` with `steps = 3` for tasting notes. | Leave users guessing what numerical value a bare slider represents. |
| Provide `onValueChangeFinished` for expensive saves/network calls rather than mutating storage on every drag frame. | Trigger heavy database writes on every single pixel drag delta. |
| Snap discrete values deterministically across both tap and drag gestures. | Allow stepped sliders to visually park halfway between ticks. |
| Render a visible ring around the thumb and high-contrast active fill. | Rely solely on hairline track ticks with poor contrast in Dark theme. |

---

## 2. Anatomy & Variants

```text
  Body                                         4 / 5
  ───────●═══════════════════════════════════════════   (48dp touch height)
  Light-bodied                                 Full
```

- **Continuous (`steps == 0`):** Smooth floating-point scrub from `valueRange.start` to `valueRange.endInclusive`.
- **Stepped (`steps > 0`):** Divides `valueRange` into `steps + 1` equal intervals with tick dots rendered directly along the track.

---

## 3. Token Bindings

| Element | State | Light / Dark Semantic Token |
|---|---|---|
| Inactive Track | `Enabled` / `Disabled` | `surface.sunken` / `interaction.disabledContainer` |
| Active Track Fill | `Enabled` / `Disabled` | `brand.primary` / `content.disabled` |
| Step Tick Markers | `Active` / `Inactive` | `brand.onPrimary` (`75%`) / `content.tertiary` (`65%`) |
| Thumb Core | `Enabled` / `Disabled` | `surface.default` fill + `brand.primary` (`2.5.dp`) ring / `content.disabled` |
| Thumb Glow Halo | `Dragging / Pressed` | `brand.primary` (`18%` alpha) |
| `label` & `valueLabel` | `Enabled` / `Disabled` | `content.primary` (`typography.labelLarge`) & `brand.primary` (`typography.monoMedium`) |

---

## 4. State Precedence & Accessibility

### State Precedence

`Disabled` (`!enabled`) > `Dragging / Pressed` > `Focused` > `Rest`.

### Accessibility

- Exposes Compose `semantics { setProgress { target -> ... } }` so TalkBack volume-key increments adjust `value` cleanly across `valueRange` (respecting `steps`).
- Tracks and thumb ensure at least `48.dp` interactive touch height.

---

## 5. Motion

Consumes `AuraTheme.motion`:

1. **Thumb Drag / Press Expansion:** Thumb expands gently (`1.0f -> 1.18f`) and reveals a translucent halo ring via `AuraTheme.motion.pressInSpring()` / `pressOutSpring()` while active.
2. **Tap Jump Smoothness:** Tapping directly on the track animates the fraction cleanly using `pressOutSpring()` (or snaps cleanly during live continuous drags so thumb never lags behind the finger).
3. **Reduced Motion:** Uses `snap()` under `rememberAuraReducedMotion()`.

---

## 6. Responsive Layout Behavior

| Width / Display Profile | Track & Header Adaptation |
|---|---|
| `ExtraSmall` (`< 360dp`) | Track expands across full card/pane width minus `12.dp` thumb edge inset so endpoints remain easily reachable near bezels. |
| `Compact` (`360–599dp`) | Full-width control (`fillMaxWidth()`) with clear spacing between `label` and `valueLabel` above the thumb. |
| `Square` (`~1:1` Pop-up / Foldable) | Adapts seamlessly inside modal inspector sheets without shrinking thumb radius. |
| `UltraWide` (`> 21:9`) | Maintains compact `48.dp` track row footprint in horizontal landscape panels. |
| `Expanded` (`>= 840dp` Tablet) | Sits cleanly inside `AuraAdaptiveTwoPane` inspector panes bounded by `maxContentWidth` (`<= 520dp`) so drags never require overly long arm travel. |

---

## 7. Cross-Platform API Contract

<!-- API:ANDROID:BEGIN -->
```kotlin
@Composable
fun AuraSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    label: String? = null,
    valueLabel: String? = null,
    supportingText: String? = null,
    interactionSource: MutableInteractionSource? = null,
)
```
<!-- API:ANDROID:END -->

<!-- API:IOS:BEGIN -->
```swift
public struct AuraSlider: View {
    public init(
        value: Binding<Float>,
        enabled: Bool = true,
        valueRange: ClosedRange<Float> = 0...1,
        steps: Int = 0,
        onValueChangeFinished: (() -> Void)? = nil,
        label: String? = nil,
        valueLabel: String? = nil,
        supportingText: String? = nil
    )
}
```
<!-- API:IOS:END -->

<!-- API:WEB:BEGIN -->
```ts
export interface AuraSliderProps {
  value: number;
  onValueChange: (value: number) => void;
  enabled?: boolean;
  valueRange?: [number, number];
  steps?: number;
  onValueChangeFinished?: () => void;
  label?: string;
  valueLabel?: string;
  supportingText?: string;
}
```
<!-- API:WEB:END -->
