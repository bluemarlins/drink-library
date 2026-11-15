---
component_id: "aura.component.text-field"
name: "AuraTextField"
category: "inputs"
status: "ready"
version: "0.1.0"
platforms:
  android: "com.example.ds.component.AuraTextField"
  ios: "AuraDS.AuraTextField (planned)"
  web: "@aura-ds/react.AuraTextField (planned)"
tokens:
  - "surface.*"
  - "content.*"
  - "border.*"
  - "brand.*"
  - "status.error.*"
  - "interaction.*"
  - "typography.*"
  - "motion.*"
  - "layout.*"
---

# AuraTextField

> Cross-platform specification for Aura's single-line and multi-line text input component. See [00-input-principles.md](00-input-principles.md) for category-level selection principles and [00-spec-architecture.md](../00-spec-architecture.md) for the bidirectional `Specs ↔ Code` contract.

---

## 1. Intent & Usage Principles

### Problem

Users need a clear, accessible text input surface that scales from structured single-line form fields to calm multi-line diary and tasting-note writing across all target apps.

### When to Use

- Short single-line metadata: schedule title (**My Planner**), asset name (**My Finance**), bottle name/region (**My Drink Diary**).
- Multi-line reflective writing: paper-style daily diary (**My Planner**), wine/whiskey tasting notes (**My Drink Diary**).
- Constrained text inputs needing prefix/suffix units, character limits, or inline validation feedback.

### When NOT to Use & Alternatives

| Situation | Alternative | Reason |
|---|---|---|
| Instant search / live archive filtering | `AuraSearchBar` | Requires clear button, search affordance, and `Glass` overlay support |
| Formatted currency or stepper numeric entry | `AuraNumberField` | Needs thousands separators, decimal clamping, and numeric keypad patterns |
| Selecting from a fixed list of options | `AuraRadio` / `AuraChipButton` | Prevents typing errors and speeds up selection |
| Picking dates, times, or vintage years | `AuraDateTimePicker` | Provides structured calendar/wheel pickers |

### Context Rules

- **`INP-RULE-001`:** Use `Outlined` or `Filled` for structured forms; use `Paperless` for calm long-form writing (`singleLine = false`, `minLines >= 3`).
- **`INP-RULE-004`:** Always supply a non-empty `label` for form fields; do not rely solely on `placeholder`.
- **`INP-RULE-005`:** Pair error borders with explicit `errorText` so validation is clear without relying solely on color.

### Do / Don't

| Do | Don't |
|---|---|
| Always provide an accessible `label` above the field (`INP-RULE-004`). | Use `placeholder` alone as the field title—once typed into, context disappears. |
| Use `Paperless` (`singleLine = false`, `minLines >= 3`) for **My Planner** diary and **My Drink Diary** tasting notes. | Wrap long reflective writing in heavy `Outlined` borders that distract from prose. |
| Use `prefixText` / `suffixText` (`₩`, `원`, `% ABV`) for units rather than appending symbols to user input values. | Pollute bound text strings with unit suffixes that break numeric editing or validation. |
| Pass `errorText` so border tint, helper text, and accessibility `semantics.error` update together. | Highlight errors by color alone without explaining how to resolve the error. |

---

## 2. Anatomy & Canonical Part IDs

```text
+-----------------------------------------------------------------------+
|  root                                                                 |
|  [label]                                                              |
|  +-----------------------------------------------------------------+  |
|  |  fieldContainer (min 48px height, border / fill / underline)    |  |
|  |                                                                 |  |
|  |  [leadingIcon] [prefix] [inputText / placeholder] [suffix]      |  |
|  |                                                  [trailingIcon] |  |
|  +-----------------------------------------------------------------+  |
|  footerRow: [supportingText / errorText]          [characterCounter]  |
+-----------------------------------------------------------------------+
```

| Part ID | Responsibility | Required | Semantics Owner |
|---|---|---|---|
| `root` | Outer vertical container grouping label, field, and footer | Yes | Container |
| `label` | Persistent descriptive field title | Recommended | Label text |
| `fieldContainer` | Visual box (`Outlined`/`Filled`) or bottom-ruled line (`Paperless`), `>= 48px` hit height | Yes | `BasicTextField` |
| `leadingIcon` | Optional leading visual cue | No | Decorative |
| `prefix` | Optional static unit/currency prefix (`₩`, `$`) | No | Text |
| `inputText` | Editable text value | Yes | Input value |
| `placeholder` | Hint displayed only when `value` is empty | No | Hint |
| `suffix` | Optional static unit suffix (`원`, `% ABV`, `ml`) | No | Text |
| `trailingIcon` | Optional trailing action/status icon | No | Child / Action |
| `footerRow` | Bottom feedback row | Conditional | Container |
| `supportingText` | Helper guidance shown when no error is active | No | Helper text |
| `errorText` | Validation error message taking priority over `supportingText` | Conditional | `semantics.error` |
| `characterCounter` | Displays `"{length}/{maxLength}"` when `maxLength != null` | Conditional | Counter text |

---

## 3. Variant & Line Mode Matrix

### Visual Variants (`AuraTextFieldVariant`)

| Variant | Visual Treatment | Recommended Usage |
|---|---|---|
| `Outlined` (Default) | Rounded `10px` box (`surface.default`) with full border (`border.default` -> `border.focus`) | Structured forms in **My Finance** & **My Planner** |
| `Filled` | Rounded `10px` recessed fill (`surface.sunken`) with subtle border | Compact card metadata in **My Drink Diary** & settings |
| `Paperless` | Transparent background with bottom horizontal rule (`1px` -> `2px` active) | Calm multi-line diary pages (**Planner**) and tasting notes (**Drink Diary**) |

### Line Modes

| Mode | Configuration | Behavior |
|---|---|---|
| **Single-Line** | `singleLine = true` | `effectiveMinLines = 1`, `effectiveMaxLines = 1`, vertical center alignment, min height `48px` |
| **Multi-Line** | `singleLine = false`, `minLines >= 3` | Top-aligned icons/affixes, grows vertically up to `maxLines` |

### Validation & Input Constraints

- **`TF-001` (Single-Line Precedence):** When `singleLine = true`, `effectiveMinLines` and `effectiveMaxLines` clamp to `1` to prevent invalid line range exceptions.
- **`TF-002` (Character Limit Enforcement):** When `maxLength != null`, additions exceeding `maxLength` are truncated or blocked while deletions are always permitted even if initial text exceeded `maxLength`.
- **`TF-003` (Error Overrides Supporting Text):** When `errorText` is non-blank, `isError = true` and `errorText` renders in place of `supportingText`.
- **`TF-004` (Cursor Suppression on ReadOnly):** Read-only fields keep readable `content.primary` text without showing an active focus ring or blinking cursor.

---

## 4. State Precedence & Behavior

Concurrent states resolve in strict deterministic order:

$$\text{Disabled} \succ \text{ReadOnly} \succ \text{Error} \succ \text{Focused} \succ \text{Rest}$$

1. **`Disabled` (`enabled == false`):** Ignores input/focus, applies `interaction.disabledContainer` (`Outlined`/`Filled`) and `content.disabled` text.
2. **`ReadOnly` (`readOnly == true`):** Preserves `content.primary` legibility, suppresses active focus border thickening and cursor.
3. **`Error` (`!errorText.isNullOrBlank()`):** Applies `status.error.solid` to border/underline, label, error text, and accessibility error semantics.
4. **`Focused`:** Thickens border/underline (`2px` for `Outlined`/`Paperless`, `1.5px` for `Filled`) with `border.focus` and tints label with `brand.primary`.
5. **`Rest`:** Default neutral border and `content.secondary` label.

---

## 5. Semantic Token Bindings (`AuraColors` & `AuraTypography`)

| Part | Variant | State | `AuraColors` / `AuraTypography` Token Path |
|---|---|---|---|
| `fieldContainer` (bg) | `Outlined` | `Enabled` / `ReadOnly` | `colors.surface.default` |
| `fieldContainer` (bg) | `Filled` | `Enabled` / `ReadOnly` | `colors.surface.sunken` |
| `fieldContainer` (bg) | `Outlined` / `Filled` | `Disabled` | `colors.interaction.disabledContainer` |
| `fieldContainer` (bg) | `Paperless` | `*` | `Transparent` |
| `fieldContainer` (border/line) | `*` | `Disabled` / `ReadOnly` | `colors.border.subtle` |
| `fieldContainer` (border/line) | `*` | `Error` | `colors.status.error.solid` |
| `fieldContainer` (border/line) | `*` | `Focused` | `colors.border.focus` |
| `fieldContainer` (border/line) | `Outlined` / `Paperless` | `Rest` | `colors.border.default` |
| `fieldContainer` (border/line) | `Filled` | `Rest` | `colors.border.subtle` |
| `label` | `*` | `Disabled` | `colors.content.disabled` |
| `label` | `*` | `Error` | `colors.status.error.solid` |
| `label` | `*` | `Focused` | `colors.brand.primary` |
| `label` | `*` | `Rest` / `ReadOnly` | `colors.content.secondary` |
| `label` (type) | `*` | `*` | `typography.labelMedium` (`13sp / 18sp`, `Medium`) |
| `inputText` | `*` | `Enabled` / `ReadOnly` | `colors.content.primary` + `typography.bodyLarge` (`15sp / 22sp`) |
| `inputText` | `*` | `Disabled` | `colors.content.disabled` |
| `placeholder` | `*` | `Enabled` / `ReadOnly` | `colors.content.tertiary` + `typography.bodyLarge` (`15sp / 22sp`) |
| `prefix` / `suffix` | `*` | `Enabled` / `ReadOnly` | `colors.content.secondary` + `typography.bodyMedium` (`Medium`) |
| `errorText` | `*` | `Error` | `colors.status.error.solid` + `typography.bodySmall` |
| `supportingText` | `*` | `Non-Error` | `colors.content.secondary` + `typography.bodySmall` |
| `characterCounter` | `*` | `Rest` / `Error` | `colors.content.tertiary` / `colors.status.error.solid` + `typography.monoSmall` (`tnum`) |

---

## 6. Interaction & Focus Motion Contract

`AuraTextField` drives focus and validation state transitions through `AuraTheme.motion` (see [03-motion-interaction.md](../../foundation/03-motion-interaction.md)):

1. **Focus-In / Focus-Out Transition:**
   - Border/underline color, label color, footer color, and stroke thickness (`1dp ↔ 2dp` for `Outlined`/`Paperless`, `1dp ↔ 1.5dp` for `Filled`) interpolate via `animateColorAsState` and `animateDpAsState` using `FastOutSlowInEasing`.
   - **Focus Attack:** `motion.pressInDurationMillis` (`90ms`) gives crisp immediate focus acquisition.
   - **Blur / Error State Transition:** `motion.stateChangeDurationMillis` (`150ms`) smoothly settles back to rest or validation styling.
2. **Reduced Motion Compatibility:**
   - Color and non-spatial border thickness cues remain active so focus and error states remain unmistakable even when system animator scale is `0`.

---

## 7. Responsive Layout Behavior (`Smallest Width` × `Display Ratio`)

| Window / Ratio Profile | Form & Field Layout Adaptation |
|---|---|
| **`ExtraSmall` (`sw < 360dp`, Foldable Cover / `UltraTall`)** | Render fields 1 per row (`fillMaxWidth`); keep `prefixText`/`suffixText` concise so editable area stays generous inside tight margins (`12dp`). |
| **`Compact` (`360..599dp`, Phone Portrait)** | Full-width stacked form inputs with `16dp` outer screen margins. |
| **`Medium` + `Square` (Unfolded Foldable `~1:1`)** | Pair short metadata fields side-by-side in 2 columns (`Row` + `weight(1f)` + `gridGutter`); place `Paperless` diary/tasting notes in `secondaryPane` (`55%`) via `AuraAdaptiveTwoPane`. |
| **`PhoneLandscape` + `UltraWide` (`width >= 600dp`, `height < 480dp`)** | Avoid stacking multiple `minLines >= 4` editors above keyboards; show controls in `primaryPane` (`42%`) and active input in `secondaryPane` (`58%`). |
| **`Expanded` (`>= 840dp`, Tablet / DeX)** | Constrain standalone single-column form width (`<= 600dp` via `maxContentWidth`) or pair metadata fields side-by-side (`weight(1f)`) to preserve comfortable reading line length. |

---

## 8. Accessibility & Guardrails

- **Touch Target:** `fieldContainer` enforces minimum `48dp` height (`44pt` on iOS).
- **Error Semantics:** Sets Compose `Modifier.semantics { if (isError) error(errorText!!) }`, iOS accessibility hint/label, and Web `aria-invalid="true"` + `aria-describedby`.
- **Contrast:** All enabled text/container pairs comply with WCAG AA (≥ 4.5:1) via `AuraColorSchemes`.

---

## 9. Platform Bindings

### 9.1. Android (Jetpack Compose)

- **Source:** [`AuraTextField.kt`](../../../design-system/src/main/kotlin/com/example/ds/component/AuraTextField.kt)
- **Dependency Policy:** Built strictly on Compose Foundation `BasicTextField` without `material3`.

<!-- API:ANDROID:BEGIN -->
```kotlin
package com.example.ds.component

enum class AuraTextFieldVariant {
    Outlined,
    Filled,
    Paperless,
}

@Composable
fun AuraTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    variant: AuraTextFieldVariant = AuraTextFieldVariant.Outlined,
    label: String? = null,
    placeholder: String? = null,
    supportingText: String? = null,
    errorText: String? = null,
    prefixText: String? = null,
    suffixText: String? = null,
    maxLength: Int? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
)
```
<!-- API:ANDROID:END -->

### 9.2. iOS (SwiftUI Reference Contract — Planned)

<!-- API:IOS:BEGIN -->
```swift
public enum AuraTextFieldVariant {
    case outlined, filled, paperless
}

public struct AuraTextField<LeadingIcon: View, TrailingIcon: View>: View {
    public init(
        text: Binding<String>,
        variant: AuraTextFieldVariant = .outlined,
        label: String? = nil,
        placeholder: String? = nil,
        supportingText: String? = nil,
        errorText: String? = nil,
        prefixText: String? = nil,
        suffixText: String? = nil,
        maxLength: Int? = nil,
        isEnabled: Bool = true,
        isReadOnly: Bool = false,
        lineLimit: ClosedRange<Int>? = 1...1,
        @ViewBuilder leadingIcon: () -> LeadingIcon = { EmptyView() },
        @ViewBuilder trailingIcon: () -> TrailingIcon = { EmptyView() }
    )
}
```
<!-- API:IOS:END -->

### 9.3. Web (React / TypeScript Reference Contract — Planned)

<!-- API:WEB:BEGIN -->
```ts
export type AuraTextFieldVariant = "outlined" | "filled" | "paperless";

export interface AuraTextFieldProps {
  value: string;
  onValueChange: (value: string) => void;
  variant?: AuraTextFieldVariant;
  label?: string;
  placeholder?: string;
  supportingText?: string;
  errorText?: string;
  prefixText?: string;
  suffixText?: string;
  maxLength?: number;
  disabled?: boolean;
  readOnly?: boolean;
  multiline?: boolean;
  minRows?: number;
  maxRows?: number;
  leadingIcon?: React.ReactNode;
  trailingIcon?: React.ReactNode;
  className?: string;
}
```
<!-- API:WEB:END -->
