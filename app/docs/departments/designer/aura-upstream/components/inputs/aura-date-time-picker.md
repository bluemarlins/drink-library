---
id: "aura-date-time-picker"
name: "AuraDateTimePicker"
category: "inputs"
status: "ready"
version: "1.0.0"
platforms:
  android: "com.example.ds.component.AuraDateTimePicker"
  ios: "AuraDateTimePicker"
  web: "AuraDateTimePicker"
tokens:
  - "color.surface.default"
  - "color.surface.sunken"
  - "color.brand.primary"
  - "color.brand.onPrimary"
  - "color.brand.subtle"
  - "color.brand.onSubtle"
  - "color.content.primary"
  - "color.content.secondary"
  - "color.content.tertiary"
  - "color.content.disabled"
  - "color.border.default"
  - "color.border.subtle"
  - "typography.titleSmall"
  - "typography.bodyMedium"
  - "typography.labelMedium"
  - "typography.monoMedium"
  - "motion.pressInSpring"
  - "motion.pressOutSpring"
  - "motion.stateChangeDurationMillis"
  - "motion.minPressDurationMillis"
  - "motion.pressedScaleDefault"
---

# AuraDateTimePicker

Structured calendar date, time-slot, and vintage-year selector built exclusively on Aura tokens for **My Planner**, **My Finance**, and **My Drink Diary**.

## 1. Intent & Usage Principles

### When to Use

- Picking a calendar day for schedules (**My Planner**), dividend/transaction dates (**My Finance**), or tasting logs (**My Drink Diary**).
- Selecting combined date + hour/minute schedule slots (`AuraDateTimeMode.DateTime`).
- Rapidly choosing a wine/whiskey bottle vintage year (`AuraDateTimeMode.YearVintage`) within `minYear..maxYear`.

### When NOT to Use

- **Typing raw numbers unrelated to calendar dates (currency, ABV):** Use `AuraNumberField`.
- **Relative duration / temperature scales:** Use `AuraSlider`.
- **Single-line view scope switching (`Day | Week | Month`):** Use `AuraSegmentedButton`.

### Usage Rules

- **`DTP-RULE-001` (48dp Calendar Cell Accessibility):** Day cells, `<` / `>` month arrows, and hour/minute steppers maintain comfortable hit bounds (`>= 40–48dp`) with clear visual selected pill highlighting.
- **`DTP-RULE-002` (Fast Vintage Selection):** In `YearVintage` mode, prioritize an intuitive scannable grid/strip of years instead of forcing users to tap "previous month" hundreds of times.
- **`DTP-RULE-003` (Tabular Dates & Times):** All day numbers, years, hours, and minutes use monospace tabular styling (`typography.monoMedium`) so numbers align cleanly across columns.

### Do / Don't

| Do | Don't |
|---|---|
| Use `AuraDateTimeMode.YearVintage` for selecting a 1998 wine vintage in **My Drink Diary**. | Force users to navigate a monthly calendar 28 years backward just to pick a vintage year. |
| Use `AuraDateTimeMode.DateTime` inside **My Planner** event sheets when start/end time matters. | Split date and time into unaligned free-form text inputs without validation. |
| Clamp displayed years cleanly between `minYear` and `maxYear`. | Show invalid out-of-range dates or crash on leap-year February 29 transitions. |
| Pair selected cells with high-contrast `brand.primary` + `brand.onPrimary` tokens. | Rely on low-opacity tint alone without clear foreground contrast on selected days. |

---

## 2. Anatomy & Variants

```text
  Label (optional)
  ╭──────────────────────────────────────────────────────╮
  │  [<]          November 2026                     [>]  │
  │   Sun   Mon   Tue   Wed   Thu   Fri   Sat            │
  │          1     2     3     4     5     6             │
  │    7     8     9   (10)   11    12    13             │
  │  ──────────────────────────────────────────────────  │
  │   Time   [ -  19  + ]  :  [ -  30  + ]               │
  ╰──────────────────────────────────────────────────────╯
```

`AuraDateTimeMode`:
- `Date`: Month header + 7-column calendar grid.
- `DateTime`: Calendar grid + interactive Hour (`00..23`) and Minute (`00..55` 5-min steppers).
- `YearVintage`: Dedicated 4-column selectable year matrix with quick decade/range jumps.

---

## 3. Token Bindings

| Element | State | Light / Dark Semantic Token |
|---|---|---|
| Picker Card Surface | `Default` / `Disabled` | `surface.default` / `interaction.disabledContainer` |
| Header Month/Year & Arrows | `Rest` | `surface.sunken` icon pills + `content.primary` (`typography.titleSmall`) |
| Day / Year Cell Container | `Selected` / `Rest` | `brand.primary` / `Transparent` |
| Day / Year Cell Text | `Selected` / `Rest` / `Disabled` | `brand.onPrimary` / `content.primary` / `content.disabled` (`typography.monoMedium`) |
| Weekday Header Row | `Rest` | `content.tertiary` (`typography.labelSmall`) |
| Time Stepper Container | `Rest` | `surface.sunken` fill + `border.subtle` border |

---

## 4. State Precedence & Accessibility

### State Precedence

`Disabled` (`!enabled`) > `Selected` > `Pressed` > `Rest`.

### Accessibility

- Month navigation `<` / `>`, day cells, year buttons, and hour/minute steppers all expose `Role.Button` / `Role.RadioButton` semantics with complete screen-reader descriptions (e.g., `"2026-11-15"`, `"Previous month"`, `"Increase hour"`).
- Keyboard and TalkBack traversal follows logical calendar order (header navigation -> weekday headers -> day cells -> optional time steppers).

---

## 5. Motion

Consumes `AuraTheme.motion`:

1. **Selected Cell Crossfade & Tactile Tap:** Active cell container/content colors transition via `tween(durationMillis = AuraTheme.motion.stateChangeDurationMillis)`; pressed interactive cells/steppers scale cleanly with `pressInSpring()` / `pressOutSpring()` and `rememberAuraTactilePressed(..., motion.minPressDurationMillis)`.
2. **Reduced Motion:** Uses `snap()` when `rememberAuraReducedMotion()` is active.

---

## 6. Responsive Layout Behavior

| Width / Display Profile | Calendar & Selector Adaptation |
|---|---|
| `ExtraSmall` (`< 360dp`) | Compact `12.dp` outer card padding; 7 calendar columns share equal `weight(1f)` so all weekdays remain visible without horizontal scroll. |
| `Compact` (`360–599dp`) | Full-width `40–44dp` square-friendly day cells and side-by-side Hour/Minute steppers. |
| `Square` (`~1:1` Pop-up / Foldable) | Fits comfortably in a single fold pane or pop-up sheet (`~340dp`–`420dp` width) with tight vertical rhythm. |
| `UltraWide` (`> 21:9`) | Centered or pane-aligned inside `AuraAdaptiveTwoPane` so picker never stretches uncomfortably thin vertically. |
| `Expanded` (`>= 840dp` Tablet) | Sits in `AuraAdaptiveTwoPane` detail pane or modal dialog bounded to comfortable `<= 420dp` width. |

---

## 7. Cross-Platform API Contract

<!-- API:ANDROID:BEGIN -->
```kotlin
enum class AuraDateTimeMode {
    Date,
    DateTime,
    YearVintage,
}

@Composable
fun AuraDateTimePicker(
    selectedYear: Int,
    selectedMonth: Int,
    selectedDay: Int,
    onDateChange: (Int, Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    mode: AuraDateTimeMode = AuraDateTimeMode.Date,
    selectedHour: Int = 19,
    selectedMinute: Int = 0,
    onTimeChange: ((Int, Int) -> Unit)? = null,
    enabled: Boolean = true,
    label: String? = null,
    supportingText: String? = null,
    minYear: Int = 1950,
    maxYear: Int = 2030,
)
```
<!-- API:ANDROID:END -->

<!-- API:IOS:BEGIN -->
```swift
public enum AuraDateTimeMode: Sendable {
    case date
    case dateTime
    case yearVintage
}

public struct AuraDateTimePicker: View {
    public init(
        selectedYear: Int,
        selectedMonth: Int,
        selectedDay: Int,
        onDateChange: @escaping (Int, Int, Int) -> Void,
        mode: AuraDateTimeMode = .date,
        selectedHour: Int = 19,
        selectedMinute: Int = 0,
        onTimeChange: ((Int, Int) -> Void)? = nil,
        enabled: Bool = true,
        label: String? = nil,
        supportingText: String? = nil,
        minYear: Int = 1950,
        maxYear: Int = 2030
    )
}
```
<!-- API:IOS:END -->

<!-- API:WEB:BEGIN -->
```ts
export type AuraDateTimeMode = 'Date' | 'DateTime' | 'YearVintage';

export interface AuraDateTimePickerProps {
  selectedYear: number;
  selectedMonth: number;
  selectedDay: number;
  onDateChange: (year: number, month: number, day: number) => void;
  mode?: AuraDateTimeMode;
  selectedHour?: number;
  selectedMinute?: number;
  onTimeChange?: (hour: number, minute: number) => void;
  enabled?: boolean;
  label?: string;
  supportingText?: string;
  minYear?: number;
  maxYear?: number;
}
```
<!-- API:WEB:END -->
