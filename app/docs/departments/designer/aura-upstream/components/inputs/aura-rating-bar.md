---
id: "aura-rating-bar"
name: "AuraRatingBar"
category: "inputs"
status: "ready"
version: "1.0.0"
platforms:
  android: "com.example.ds.component.AuraRatingBar"
  ios: "AuraRatingBar"
  web: "AuraRatingBar"
tokens:
  - "color.brand.primary"
  - "color.brand.subtle"
  - "color.surface.sunken"
  - "color.border.strong"
  - "color.content.primary"
  - "color.content.secondary"
  - "color.content.disabled"
  - "typography.labelLarge"
  - "typography.monoMedium"
  - "motion.pressInSpring"
  - "motion.pressOutSpring"
  - "motion.iconSpring"
  - "motion.stateChangeDurationMillis"
  - "motion.pressedScaleDefault"
---

# AuraRatingBar

Star and half-star rating input and display control tailored for sensory enjoyment logs (**My Drink Diary**) and summary review cards.

## 1. Intent & Usage Principles

### When to Use

- Recording tasting enjoyment scores (`0.5`..`5.0` stars) with tap or smooth horizontal scrub (**My Drink Diary**).
- Displaying read-only historical ratings on bottle cards (`onRatingChange = null`, `size = Small`).

### When NOT to Use

- **Continuous technical or multi-dimensional metrics (`0..100%`, `Body`, `Tannin`, `Serving °C`):** Use `AuraSlider`.
- **Exact numeric scores (`94 / 100` Parker Points):** Use `AuraNumberField`.
- **Single binary favorite mark:** Use `AuraIconButton` (bookmark / heart toggle).

### Usage Rules

- **`RAT-RULE-001` (Half-Star Precision Default):** Enable `allowHalfRating = true` by default in tasting journals (`4.5 / 5.0`), while supporting integer-only (`1..5`) mode for quick thumbs-up style forms.
- **`RAT-RULE-002` (Interactive vs. Read-Only Clarity):** When `onRatingChange == null`, switch into calm display mode (`Small` cards, zero drag/press feedback); when `onRatingChange != null`, enforce a minimum `48.dp` interactive height.
- **`RAT-RULE-003` (Tabular Readout Option):** Pair stars with `showValueText = true` (`"4.5 / 5"`) using monospace tabular alignment so fractional changes read crisply.

### Do / Don't

| Do | Don't |
|---|---|
| Support both single-star taps (toggle left half vs. full star) and smooth horizontal slide scrubbing. | Force users to hunt pixel-perfect half edges with only tap gestures. |
| Use `AuraRatingBarSize.Small` (`onRatingChange = null`) for compact list cards and `Large` on detail review sheets. | Cram tiny `16dp` interactive stars into primary modal review sheets. |
| Render clear stroke outlines (`border.strong`) on unfilled stars in both Light and Dark themes. | Leave unfilled stars invisible against sunken dark cards. |
| Provide descriptive `label` (`"Overall Impression"`) above or beside stars when multiple ratings coexist. | Display anonymous floating stars without explaining what aspect is being rated. |

---

## 2. Anatomy & Variants

```text
  Overall Enjoyment
   ★   ★   ★   ★½   ☆    4.5 / 5
  └─── 48dp minimum interactive row height ───┘
```

| Size (`AuraRatingBarSize`) | Visual Star Size | Star Spacing | Min Touch Height | Recommended Context |
|---|---|---|---|---|
| `Small` | `18.dp` | `4.dp` | `48.dp` (when interactive) / `20.dp` (read-only) | Bottle list rows, summary headers |
| `Medium` (default) | `28.dp` | `6.dp` | `48.dp` | Standard tasting card & archive filters |
| `Large` | `36.dp` | `8.dp` | `48.dp` | Dedicated tasting-note modal & detail review |

---

## 3. Token Bindings

| Element | State | Light / Dark Semantic Token |
|---|---|---|
| Filled / Half-Filled Star Fill | `Enabled` / `Disabled` | `brand.primary` / `content.disabled` |
| Unfilled Star Base & Stroke | `Enabled` / `Disabled` | `surface.sunken` fill + `border.strong` stroke / `border.subtle` |
| Value Readout Pill (`4.5 / 5`) | `Rest` | `brand.subtle` fill + `brand.onSubtle` or `content.primary` (`typography.monoMedium`) |
| `label` Text | `Enabled` / `Disabled` | `content.primary` / `content.disabled` (`typography.labelLarge`) |

---

## 4. State Precedence & Accessibility

### State Precedence

`Disabled` (`!enabled`) > `Dragging / Scrubbing` > `ReadOnly` (`onRatingChange == null`) > `Rest`.

### Accessibility

- Attaches `stateDescription = "${formattedRating} out of ${maxStars} stars"` and `setProgress` semantics so screen-reader users can step ratings in `0.5` or `1.0` increments without vision.
- Interactive hit zone spans at least `48.dp` vertically across the star strip.

---

## 5. Motion

Consumes `AuraTheme.motion`:

1. **Star Selection Bounce:** Newly activated stars pop gently (`1.0f -> 1.15f -> 1.0f`) using `AuraTheme.motion.iconSpring()`, and unfilled/filled transitions crossfade via `tween(durationMillis = AuraTheme.motion.stateChangeDurationMillis)`.
2. **Drag Feedback:** Active scrubbed star gently emphasizes with tactile feedback (`pressInSpring()` / `pressOutSpring()`).
3. **Reduced Motion:** Uses `snap()` under `rememberAuraReducedMotion()`.

---

## 6. Responsive Layout Behavior

| Width / Display Profile | Layout Adaptation |
|---|---|
| `ExtraSmall` (`< 360dp`) | Automatically tightens spacing between stars if `Large` `maxStars > 5` so stars and optional value readout pill never wrap or clip off-screen. |
| `Compact` (`360–599dp`) | Single-row horizontal alignment pairing optional header `label` above stars + trailing tabular `4.5 / 5` pill. |
| `Square` (`~1:1` Pop-up / Foldable) | Fits comfortably within single-column review forms without colliding with neighboring numeric inputs. |
| `UltraWide` (`> 21:9`) | Maintains natural intrinsic width anchored cleanly to start edge rather than stretching gaps between stars. |
| `Expanded` (`>= 840dp` Tablet) | Sits in bottle summary header or detail review pane with steady star proportions regardless of container width. |

---

## 7. Cross-Platform API Contract

<!-- API:ANDROID:BEGIN -->
```kotlin
enum class AuraRatingBarSize {
    Small,
    Medium,
    Large,
}

@Composable
fun AuraRatingBar(
    rating: Float,
    onRatingChange: ((Float) -> Unit)?,
    modifier: Modifier = Modifier,
    maxStars: Int = 5,
    allowHalfRating: Boolean = true,
    size: AuraRatingBarSize = AuraRatingBarSize.Medium,
    enabled: Boolean = true,
    label: String? = null,
    showValueText: Boolean = true,
)
```
<!-- API:ANDROID:END -->

<!-- API:IOS:BEGIN -->
```swift
public enum AuraRatingBarSize: Sendable {
    case small
    case medium
    case large
}

public struct AuraRatingBar: View {
    public init(
        rating: Float,
        onRatingChange: ((Float) -> Void)? = nil,
        maxStars: Int = 5,
        allowHalfRating: Bool = true,
        size: AuraRatingBarSize = .medium,
        enabled: Bool = true,
        label: String? = nil,
        showValueText: Bool = true
    )
}
```
<!-- API:IOS:END -->

<!-- API:WEB:BEGIN -->
```ts
export type AuraRatingBarSize = 'Small' | 'Medium' | 'Large';

export interface AuraRatingBarProps {
  rating: number;
  onRatingChange?: (rating: number) => void;
  maxStars?: number;
  allowHalfRating?: boolean;
  size?: AuraRatingBarSize;
  enabled?: boolean;
  label?: string;
  showValueText?: boolean;
}
```
<!-- API:WEB:END -->
