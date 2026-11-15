# 05 · Responsive & Adaptive Layout (`Smallest Width` × `Display Ratio`)

## 1. Purpose

Defines Aura's **two-axis responsive and adaptive layout system (`AuraWindowInfo` + `AuraLayout`)** so screens, grids, and split panes adapt cleanly across standard bar phones, ultra-tall foldable cover displays, `~1:1` unfolded foldables (e.g., Galaxy Z Fold main display), landscape phones, and tablets.

---

## 2. Why Two Axes (`Smallest Width` × `Display Ratio`)?

Relying solely on current viewport `widthDp` misclassifies real-world mobile configurations:

1. **Unfolded Foldables (`sw >= 600dp` + `Square` ratio `0.85..1.20`):**
   - Total width and height are nearly equal (`~1:1`). A 1-column phone layout wastes horizontal space, while a 3-column tablet layout crushes cards.
   - **Strategy:** Symmetrical **2-column grid** (`gridColumns = 2`) or **45% : 55% two-pane book layout** (`supportsTwoPane = true`, `primaryPaneFraction = 0.45f`).
2. **Phone Landscape (`sw < 600dp` + `UltraWide` ratio `> 1.85` and `height < 480dp`):**
   - Current width (`widthDp >= 600dp`) looks wide, yet vertical viewport height is severely constrained (`< 480dp`). Applying spacious tablet headers steals usable vertical space.
   - **Strategy:** Compress vertical chrome (`topBarHeight = 48.dp`, `screenMarginVertical = 12.dp`) while splitting controls and content side-by-side (`supportsTwoPane = widthDp >= 640`, `primaryPaneFraction = 0.42f`).
3. **Foldable Cover Screens & Narrow Windows (`sw < 360dp` or `UltraTall` ratio `< 0.50`):**
   - `21:9`+ narrow displays cause premature wrapping with default `16dp` margins and oversized headings.
   - **Strategy:** Tighten horizontal padding (`screenMarginHorizontal = 12.dp`) and apply `typographyScale = 0.92f` to display/headline/title styles.

---

## 3. Classification Axes (`AuraWindowInfo`)

Exposed via `AuraTheme.window`.

### Axis A — Width & Smallest Width (`AuraWidthClass`)

| Class | Threshold (`dp`) | Representative Environments |
|---|---|---|
| `ExtraSmall` | `< 360dp` | Foldable cover screens, compact legacy phones, narrow split-screen windows |
| `Compact` | `360dp .. 599dp` | Standard smartphones in portrait |
| `Medium` | `600dp .. 839dp` | Unfolded foldables, small 7–8" tablets |
| `Expanded` | `>= 840dp` | Large tablets (`10"+`), desktop/DeX windows |

### Height Class (`AuraHeightClass`)

| Class | Threshold (`dp`) | Notes |
|---|---|---|
| `Compact` | `< 480dp` | Phone landscape or horizontal split-screen — triggers compact vertical chrome |
| `Medium` | `480dp .. 899dp` | Standard portrait phone & foldable inner screen |
| `Expanded` | `>= 900dp` | Large tablet portrait / tall displays |

### Axis B — Display Ratio (`AuraDisplayRatio = widthDp / heightDp`)

| Ratio Class | Aspect Ratio (`W / H`) | Representative Devices |
|---|---|---|
| `UltraTall` | `< 0.50` (`9:21` or narrower) | Foldable cover displays, 21:9 phones |
| `Portrait` | `0.50 .. 0.84` | Standard phones (`9:16`–`9:20`), portrait tablets (`3:4`) |
| `Square` | `0.85 .. 1.20` | Unfolded book-style foldables (`~1:1`), 1:1 split panes |
| `Landscape` | `1.21 .. 1.85` | Tablets in landscape (`4:3`, `16:10`, `16:9`) |
| `UltraWide` | `> 1.85` | Phones in landscape (`20:9`, `21:9`), ultrawide multi-window |

---

## 4. Semantic Responsive Token Matrix (`AuraLayout`)

Exposed via `AuraTheme.layout` and derived deterministically by `AuraLayoutDefaults.fromWindowInfo(windowInfo)`:

| Semantic Token | ExtraSmall (`sw < 360` or `w < 360`) | Phone Portrait (`Compact`) | Phone Landscape (`sw < 600` + `UltraWide` / `h < 480` + `w >= 600`) | Foldable Square (`Medium` / `Square`) | Medium General (`Medium` non-Square) | Expanded Tablet / DeX (`w >= 840`) |
|---|---|---|---|---|---|---|
| `screenMarginHorizontal` | `12.dp` | `16.dp` | `24.dp` | `24.dp` | `24.dp` | `32.dp` |
| `screenMarginVertical` | `12.dp` | `16.dp` | `12.dp` | `20.dp` | `20.dp` | `24.dp` |
| `sectionSpacing` | `16.dp` | `20.dp` | `16.dp` | `24.dp` | `24.dp` | `28.dp` |
| `gridColumns` | `1` | `1` | `2` | `2` | `2` | `3` |
| `gridGutter` | `10.dp` | `12.dp` | `16.dp` | `16.dp` | `16.dp` | `20.dp` |
| `topBarHeight` | `52.dp` | `56.dp` | `48.dp` | `60.dp` | `60.dp` | `64.dp` |
| `maxContentWidth` | `600.dp` | `600.dp` | `960.dp` | `840.dp` | `840.dp` | `1200.dp` |
| `supportsTwoPane` | `false` | `false` | `widthDp >= 640` | `true` | `true` | `true` |
| `primaryPaneFraction` | `1.0f` | `1.0f` | `0.42f` | `0.45f` | `0.44f` | `0.38f` |
| `typographyScale` | `0.92f` | `1.00f` | `0.96f` | `1.04f` | `1.04f` | `1.08f` |

### Priority Decision Order

When multiple window traits co-occur, `AuraLayoutDefaults.fromWindowInfo` evaluates conditions in this strict order:
1. `widthClass == ExtraSmall || smallestWidthClass == ExtraSmall && widthClass == ExtraSmall` → **ExtraSmall** (or if `widthDp >= 600` in landscape even with `smallestWidthDp < 360`, phone landscape wins: wait—note: on a cover screen rotated to landscape, `widthDp >= 600` and `heightDp < 480`, so evaluating `ExtraSmall` specifically when `widthClass == ExtraSmall` handles narrow portrait while allowing rotated wide mode to use Landscape Phone!). Specifically:
   1. `widthClass == ExtraSmall` → ExtraSmall Cover/Narrow
   2. `smallestWidthClass < Medium && (displayRatio == UltraWide || heightClass == Compact) && widthDp >= 600` → Phone Landscape
   3. `widthClass >= Medium && displayRatio == Square` → Unfolded Foldable Square (when `widthClass == Expanded && displayRatio == Square`, keep 2 columns & `0.45f` / `1000.dp` or `Expanded` check: let's match Square vs Expanded explicitly)
   4. `widthClass == Expanded` → Tablet / DeX Expanded
   5. `widthClass == Medium` → Medium (Tablet Portrait / Half-open)
   6. `smallestWidthClass == ExtraSmall` (cover screen rotated slightly wider or compact portrait fallback) → ExtraSmall if `widthDp < 420` else Compact
   7. Default → Standard `Compact` Phone Portrait

---

## 5. Domain-Specific Adaptations

| Application / Preview | Cover / Compact (`1` Column) | Unfolded Square Foldable (`45% : 55%` / `2` Columns) | Landscape Phone (`42% : 58%`, Compact TopBar) | Expanded Tablet (`3` Columns / `38% : 62%`) |
|---|---|---|---|---|
| **`:design-system-preview`** | Single-column catalog & stacked playgrounds | 2-column catalog/token cards; Playground left + Matrix right | `48.dp` top bar; Playground left + Matrix right | 3-column catalog & swatch groups; wide split panes |
| **My Planner** | Single day timeline or full-screen diary editor | Open paper-book spread: Schedule timeline (L) + Paperless diary (R) | Slim header + split schedule/editor | Week-at-a-glance calendar (L) + Day detail/diary (R) |
| **My Finance** | Net worth summary above asset list | Asset breakdown & allocation chart (L) + Holdings list (R) | Chart beside compact numeric rows | Portfolio overview + 3-column account cards |
| **My Drink Diary** | Stacked bottle cards & detail scroll | Cellar archive grid (2 cols) or Photo/Sensory (L) + Tasting note (R) | Photo/Score (L) + Notes (R) | 3-column bottle cellar gallery |
| **Home Launcher** | 4-column dense app grid & single widget stack | Dual-page spread (`2x` home pages) with balanced dock | Horizontal dock + wide widget grid | Tablet workspace grid with persistent quick dock |

---

## 6. Implementation Files

- [`AuraWindowInfo.kt`](../../design-system/src/main/kotlin/com/example/ds/foundation/layout/AuraWindowInfo.kt)
- [`AuraLayout.kt`](../../design-system/src/main/kotlin/com/example/ds/foundation/layout/AuraLayout.kt)
- [`AuraAdaptiveTwoPane.kt`](../../design-system/src/main/kotlin/com/example/ds/component/AuraAdaptiveTwoPane.kt)
- [`AuraAdaptiveGrid.kt`](../../design-system/src/main/kotlin/com/example/ds/component/AuraAdaptiveGrid.kt)
