# 04 · Typography System

## 1. Purpose

Defines Aura's **Tier 2 semantic typography scale (`AuraTypography`)**, responsive scale adaptation, and usage principles across all four target applications and the `:design-system-preview` catalog.

Exposed via `AuraTheme.typography` and rendered with `AuraText` without depending on Material 3 `Typography`.

---

## 2. Usage Principles (`When to Use`)

| Role | Token | Default Scale (Size / LineHeight / Weight) | When to Use | When NOT to Use | Target App & Preview Examples |
|---|---|---|---|---|---|
| **Display** | `displayLarge` | `36sp / 44sp / Bold` | Single hero metric or oversized clock/asset headline per screen | Paragraph copy, section headers, card titles | **My Finance** net worth figure, **Home Launcher** hero clock |
| **Display** | `displayMedium` | `28sp / 36sp / Bold` | Prominent secondary metric or summary banner figure | Repeated list rows or form labels | **My Finance** portfolio delta, **My Drink Diary** cellar total |
| **Headline** | `headlineLarge` | `24sp / 32sp / SemiBold` | Top-level editorial page title (when no compact top bar is used) | App bar titles (`titleLarge`) | **My Planner** daily cover title, editorial summaries |
| **Headline** | `headlineMedium` | `20sp / 28sp / SemiBold` | Modal/sheet hero titles or bottle/entry detail header | Dense card headers | **My Drink Diary** bottle detail title, modal hero |
| **Headline** | `headlineSmall` | `18sp / 26sp / SemiBold` | Empty/placeholder page headlines or multi-pane summary titles | Multi-line body text | **Preview** `PlannedComponentScreen` title |
| **Title** | `titleLarge` | `18sp / 24sp / SemiBold` | App bar titles and primary sheet headers | Inline list body copy | **Preview** `AuraTopAppBar` / `TopAppBar` titles |
| **Title** | `titleMedium` | `16sp / 22sp / SemiBold` | Section headers (`sectionSpacing` headings) and card titles | Input helper text or tags | **Preview** `CatalogItemCard` name, section headings |
| **Title** | `titleSmall` | `14sp / 20sp / SemiBold` | Compact sub-section titles or nested card headers | Long-form reading text | Nested `surface.raised` headers, widget group labels |
| **Body** | `bodyLarge` | `16sp / 24sp / Normal` | Long-form reading and reflective writing (`Paperless` diary, tasting notes) | Compact table cells or badges | **My Planner** diary body, **Preview** toggle row titles |
| **Body** | `bodyMedium` | `14sp / 20sp / Normal` | Default body copy, card descriptions, matrix labels | Interactive buttons (`label*`) | **Preview** card description, matrix labels |
| **Body** | `bodySmall` | `12sp / 18sp / Normal` | Captions, timestamps, helper/error footnotes, telemetry notes | Main reading paragraphs | **Preview** debounce counter caption, helper text |
| **Label** | `labelLarge` | `15sp / 20sp / SemiBold` | Large interactive controls (`Large` CTA buttons), group control headers | Multi-line descriptions | **Preview** playground option headers, Large `AuraButton` |
| **Label** | `labelMedium` | `13sp / 18sp / Medium` | Default buttons/chips, field labels, category tags | Long explanatory prose | `AuraTextField` label, category pills, Medium chips |
| **Label** | `labelSmall` | `11sp / 16sp / Medium` | Compact status badges (`Ready`/`Planned`), micro metadata tags | Primary action buttons | **Preview** `StatusBadge`, chart axis ticks |
| **Code / Numeric** | `monoMedium` | `14sp / 20sp / Medium (Monospace, tnum)` | Aligned financial numbers, stopwatch/timecodes, multi-pane metrics | Editorial diary prose | **My Finance** asset tables, telemetry figures |
| **Code / Numeric** | `monoSmall` | `12sp / 16sp / Medium (Monospace, tnum)` | Hex color codes (`#FF2F6DD8`), design token keys, API debug metadata | User-facing narrative copy | **Preview** `ColorSchemePreviewScreen` swatches & telemetry |

---

## 3. Responsive Typography Scaling (`typographyScale`)

Display, Headline, and Title roles scale multiplicatively using `AuraTheme.layout.typographyScale` so headings stay crisp on ultra-tall foldable cover screens without overflowing, while expanding gracefully on unfolded foldables and tablets.

| Window Context | `typographyScale` | Scaled Roles | Preserved Roles (Unscaled for Readability) |
|---|---|---|---|
| Cover / Narrow (`sw < 360dp` or `width < 360dp`) | `0.92f` | `display*`, `headline*`, `title*` | `body*`, `label*`, `mono*` |
| Landscape Phone (`sw < 600dp` + `UltraWide` / `height < 480dp`) | `0.96f` | `display*`, `headline*`, `title*` | `body*`, `label*`, `mono*` |
| Standard Phone (`Compact` + `Portrait`) | `1.00f` | Baseline (`1.00x`) | `body*`, `label*`, `mono*` |
| Unfolded Foldable (`Medium` / `Square`) | `1.04f` | `display*`, `headline*`, `title*` | `body*`, `label*`, `mono*` |
| Tablet / DeX (`Expanded`) | `1.08f` | `display*`, `headline*`, `title*` | `body*`, `label*`, `mono*` |

- Body, Label, and Monospace roles remain unscaled across breakpoints so minimum touch-control legibility and user `sp` font-scale accessibility settings are always respected.

---

## 4. Cross-Platform Token Bindings

| Aura Token | Android (`Compose TextStyle`) | iOS (`SwiftUI Font`) | Web (`CSS`) |
|---|---|---|---|
| `displayLarge` | `36.sp / 44.sp / SemiBold..Bold (Bold)` | `.system(size: 36, weight: .bold)` | `2.25rem / 2.75rem (700)` |
| `headlineLarge` | `24.sp / 32.sp / SemiBold` | `.system(size: 24, weight: .semibold)` | `1.5rem / 2rem (600)` |
| `titleLarge` | `18.sp / 24.sp / SemiBold` | `.system(size: 18, weight: .semibold)` | `1.125rem / 1.5rem (600)` |
| `bodyMedium` | `14.sp / 20.sp / Normal` | `.system(size: 14, weight: .regular)` | `0.875rem / 1.25rem (400)` |
| `labelMedium` | `13.sp / 18.sp / Medium` | `.system(size: 13, weight: .medium)` | `0.8125rem / 1.125rem (500)` |
| `monoSmall` | `12.sp / 16.sp / Medium / Monospace / tnum` | `.system(size: 12, weight: .medium, design: .monospaced)` | `0.75rem / 1rem; font-variant-numeric: tabular-nums;` |

---

## 5. Implementation Files

- [`AuraTypography.kt`](../../design-system/src/main/kotlin/com/example/ds/foundation/typography/AuraTypography.kt)
- [`AuraText.kt`](../../design-system/src/main/kotlin/com/example/ds/component/AuraText.kt)
- [`AuraTheme.kt`](../../design-system/src/main/kotlin/com/example/ds/foundation/theme/AuraTheme.kt)
