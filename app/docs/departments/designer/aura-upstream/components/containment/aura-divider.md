---
id: "aura-divider"
name: "AuraDivider"
category: "containment"
status: "ready"
layer: "content"
material_allowed: ["Solid"]
motion_track: "Standard"
version: "1.1.0"
platforms:
  android: "com.example.ds.component.AuraDivider"
  ios: "AuraDivider"
  web: "AuraDivider"
tokens:
  - "color.border.subtle"
  - "color.border.default"
  - "color.border.strong"
  - "motion.stateChangeDurationMillis"
---

# AuraDivider

Subtle hairline separator (`Horizontal` and `Vertical`) built on `AuraTheme.colors.border` tones with zero Material 3 dependency.

## 1. Intent & Usage Principles

### When to Use

- Separating logical sections inside dense detail cards, tasting records, or settings lists (`CNT-RULE-001`).
- Separating side-by-side compact metrics or actions horizontally using `AuraDividerOrientation.Vertical`.
- Creating indented list dividers (`startIndent = 16.dp`) aligned to text content rather than leading thumbnails.

### When NOT to Use & Alternatives

- **Top-level card grouping:** Wrap grouped items in `AuraCard` (`Outlined` or `Filled`) instead of stacking dividers between unrelated blocks.
- **Generous section rhythm:** Prefer `AuraTheme.layout.sectionSpacing` whitespace over heavy horizontal rules between independent sections.

### Usage Rules

- **`DIV-RULE-001` (Border Restraint):** Default to `AuraDividerTone.Subtle` inside cards and lists; reserve `Strong` for high-contrast summary totals or structural boundaries.
- **`DIV-RULE-002` (1.dp Hairline Default):** Keep `thickness = 1.dp` for standard separation; never inflate dividers into thick decorative bars.

### Do / Don't

| Do | Don't |
|---|---|
| Use `AuraDivider(tone = AuraDividerTone.Subtle)` to separate metadata rows inside an `AuraCard`. | Hardcode `Box(Modifier.height(1.dp).background(Color(0xFF...)))` in feature screens. |
| Use `startIndent` when separating rows with leading icons or thumbnails. | Stack consecutive dividers with no spacing or content between them. |
| Bound vertical dividers inside a fixed-height or `IntrinsicSize.Min` `Row`. | Place an unbounded `Vertical` divider inside a scrollable column without height constraints. |

---

## 2. Anatomy & Variants

```text
  Horizontal (Subtle / Default / Strong):
  ────────────────────────────────────────────────

  Horizontal (Indented):
       ──────────────────────────────────────

  Vertical (inside bounded Row):
  Item A  │  Item B
```

| Variant (`AuraDividerTone`) | Semantic Token | Intended Usage |
|---|---|---|
| `Subtle` (default) | `AuraTheme.colors.border.subtle` | Quiet list and card row dividers |
| `Default` | `AuraTheme.colors.border.default` | Standard section breaks on `surface.default` |
| `Strong` | `AuraTheme.colors.border.strong` | Summary totals and primary structural dividers |

---

## 3. Semantic Token Bindings

| Element | Tone / State | Light / Dark Semantic Token |
|---|---|---|
| Hairline Stroke | `Subtle` | `AuraTheme.colors.border.subtle` |
| Hairline Stroke | `Default` | `AuraTheme.colors.border.default` |
| Hairline Stroke | `Strong` | `AuraTheme.colors.border.strong` |

---

## 4. Accessibility

- Purely decorative visual separator (`focusable = false`, no interactive click semantics) so screen readers focus on adjacent content rather than announcing hairline rules.
- Color tokens automatically adapt across Light and Dark `AuraColorMode` without obscuring adjacent text.

---

## 5. Interaction Motion Contract

Consumes `AuraTheme.motion`:

1. **Tone Transitions:** Changes between `Subtle`, `Default`, and `Strong` interpolate via `tween(durationMillis = AuraTheme.motion.stateChangeDurationMillis)`.
2. **Reduced Motion:** Resolves instantaneously with `snap()` when `AuraTheme.motion.reduceMotion` is `true`.

---

## 6. Responsive Layout Behavior

| Width / Display Profile | Divider Adaptation |
|---|---|
| `ExtraSmall` (`< 360dp`) | Full-width `fillMaxWidth()` hairline or compact `8.dp` indents to preserve narrow text columns. |
| `Compact` (`360–599dp`) | Standard `fillMaxWidth()` with optional `16.dp` content-aligned indents. |
| `Square` (`~1:1` Foldable) | Scales cleanly inside split-pane cards and form sections without fixed pixel widths. |
| `UltraWide` (`> 21:9`) | Adapts to parent container width (`maxContentWidth` rails) cleanly. |
| `Expanded` (`>= 840dp`) | Respects parent pane boundaries inside `AuraAdaptiveTwoPane` (`primaryPane` / `secondaryPane`). |

---

## 7. Cross-Platform API Contract

<!-- API:ANDROID:BEGIN -->
```kotlin
enum class AuraDividerOrientation {
    Horizontal,
    Vertical,
}

enum class AuraDividerTone {
    Subtle,
    Default,
    Strong,
}

@Composable
fun AuraDivider(
    modifier: Modifier = Modifier,
    orientation: AuraDividerOrientation = AuraDividerOrientation.Horizontal,
    tone: AuraDividerTone = AuraDividerTone.Subtle,
    thickness: Dp = 1.dp,
    startIndent: Dp = 0.dp,
    endIndent: Dp = 0.dp,
)
```
<!-- API:ANDROID:END -->

<!-- API:IOS:BEGIN -->
```swift
public enum AuraDividerOrientation: Sendable {
    case horizontal
    case vertical
}

public enum AuraDividerTone: Sendable {
    case subtle
    case `default`
    case strong
}

public struct AuraDivider: View {
    public init(
        orientation: AuraDividerOrientation = .horizontal,
        tone: AuraDividerTone = .subtle,
        thickness: CGFloat = 1,
        startIndent: CGFloat = 0,
        endIndent: CGFloat = 0
    )
}
```
<!-- API:IOS:END -->

<!-- API:WEB:BEGIN -->
```ts
export type AuraDividerOrientation = 'Horizontal' | 'Vertical';
export type AuraDividerTone = 'Subtle' | 'Default' | 'Strong';

export interface AuraDividerProps {
  orientation?: AuraDividerOrientation;
  tone?: AuraDividerTone;
  thickness?: number;
  startIndent?: number;
  endIndent?: number;
}
```
<!-- API:WEB:END -->
