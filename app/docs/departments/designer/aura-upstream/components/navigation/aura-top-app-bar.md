---
id: "aura-top-app-bar"
name: "AuraTopAppBar"
category: "navigation"
status: "ready"
platforms:
  android: "com.example.ds.component.AuraTopAppBar"
  ios: "AuraTopAppBar"
  web: "AuraTopAppBar"
tokens:
  - "color.surface.default"
  - "color.surface.raised"
  - "color.surface.floating"
  - "color.content.primary"
  - "color.content.secondary"
  - "color.border.subtle"
  - "color.border.default"
  - "color.interaction.scrim"
  - "typography.titleLarge"
  - "typography.titleMedium"
  - "typography.labelMedium"
  - "motion.pressInSpring"
  - "motion.pressOutSpring"
  - "motion.stateChangeDurationMillis"
  - "motion.minPressDurationMillis"
---

# AuraTopAppBar & FloatingTopPillBar

Canvas-first top navigation system combining an Adaptive Scroll-Responsive **Floating Top Pill Bar** (`FloatingTopPillBar` with `GlassStyle.FROSTED` / `GlassStyle.LIQUID`, Metaball capsule merging, and Layer 3 Morphing `[More]` Popover) with standardized **Floating Capsule / Split / Docked** headers (`AuraTopAppBar`).

## 1. Intent & Usage Principles

Traditional Material top bars span edge-to-edge as opaque rectangles, cutting off vertical canvas continuity. `AuraTopAppBar` applies the Aura **Floating Concept** (see [00-navigation-principles.md](00-navigation-principles.md)): navigation sits inside sculpted floating pills inset from screen edges and below status bars, allowing My Planner journals, My Finance charts, My Drink Diary tasting photos, and Home Launcher canvases to breathe beneath.

### Context Rules

- **`TBAR-RULE-001` (Style Selection by Canvas)**:
  - **`FloatingCapsule`**: Default across catalog, dashboard, diary, and analytics screens. Wraps navigation icon, title (+ optional subtitle), and up to `2–3` trailing actions in a unified pill container floating above scrolled content.
  - **`FloatingSplit`**: Detaches the leading back/title island from trailing `AuraIconButton` / `AuraChipButton` action islands so the upper-center viewport stays unobstructed over photography, wine labels, calendar hero art, or 3D launcher canvases.
  - **`SurfaceDocked`**: Classic full-bleed docked surface with a crisp bottom `1.dp` divider; used strictly for dense tabular entry forms or split-pane secondary editors where floating elevation would clash with top-aligned input fields.
- **`TBAR-RULE-002` (Slot Purity)**: Leading navigation and trailing utility slots MUST be populated with `AuraIconButton` (`Ghost` inside `FloatingCapsule`/`SurfaceDocked`, `Floating` inside `FloatingSplit`) or compact `AuraChipButton` (`Filter`/`Glass`/`Assist`) — never raw `Material3 IconButton` or oversized `AuraButton`s.
- **`TBAR-RULE-003` (Underlap Scroll Scrim & Content Padding)**: Screens using floating bars MUST pass `AuraTopAppBar` total top clearance (status bar inset + floating outer margin + capsule height + optional chip ribbon) into content top padding so initial items align cleanly below the bar, while scrolled items glide visibly underneath.
- **`TBAR-RULE-004` (Single-Screen Chrome Budget)**: Never stack two full floating bars vertically. When quick filtering belongs to the bar (e.g., My Finance `"Week | Month | Year"` or Catalog category strip), render an integrated `subContent` ribbon of `AuraChipButton`s spaced `8.dp` below the capsule (collapsing gracefully under `CompactOnScroll` / `EnterAlways`).
- **`TBAR-RULE-005` (Accessibility Contrast Guarantee)**: When `TranslucentGlass` is selected, text/icon contrast MUST hold `>= 4.5:1` / `>= 3:1` over scrolled high-variance photos by combining a subtle backdrop scrim (`color.interaction.scrim`) with an elevated frosted container tint; degrade automatically to `SolidElevated` under Reduced Transparency or unsupported GPU blur tiers.

### When to Use

- Top-level navigation headers and detail screen bars in every Aura application and `:design-system-preview`.
- Media-forward detail headers (Drink Diary bottles, Planner daily reflections) needing minimal obstruction (`FloatingSplit`).
- Filterable dashboards combining title context with an inline `AuraChipButton` sub-ribbon.

### When NOT to Use

- Multi-destination bottom switching -> use `AuraNavigationBar` (planned).
- In-page section headers inside a scrollable body -> use `AuraText` typography tokens.
- Floating bottom contextual toolbars / composer bars -> use dedicated floating bottom bar patterns.

### Do / Don't

| Do | Don't |
|---|---|
| Use `FloatingCapsule` on standard list/catalog/finance screens and `FloatingSplit` over photography or 3D canvases. | Stack opaque docked bars on top of dark/light edge-to-edge photos where floating pills preserve depth. |
| Compose leading/trailing slots exclusively from `AuraIconButton` and compact `AuraChipButton`. | Place wide `AuraButton` CTAs inside the top app bar trailing slot. |
| Provide top content padding equal to the resting bar footprint so the first list item never starts hidden beneath the capsule. | Overlap interactive first-row controls under the floating capsule at initial scroll position `0`. |
| Cap trailing visible actions at `2–3` and fold overflow items into a `More` icon menu. | Pack `4+` crowded icons into `FloatingCapsule`, truncating the screen title to 3 letters. |

## 2. Floating Architecture & Variants

### Layout Styles (`AuraTopAppBarStyle`)

1. **`FloatingCapsule`**
   - Continuous pill (`CircleShape` / `28.dp` radius), `56.dp` height (`48.dp` compact), horizontal outer margin `16.dp` (`Compact`) / `24.dp` (`Expanded`), `8.dp` clearance below `statusBars` inset.
   - Leading slot (`AuraIconButton`), center/start-aligned title (`typography.titleMedium` / `titleLarge`) + optional single-line subtitle (`typography.labelMedium`), trailing action cluster (`AuraIconButton` + optional 1 `AuraChipButton`).
2. **`FloatingSplit`**
   - Independent left island (Circular `AuraIconButton`, or compact Back + Title capsule) and right island (Floating `AuraIconButton` / `AuraChipButton` cluster), leaving the horizontal middle completely open to underlying artwork.
   - Every standalone island enforces `>= 48.dp` height and matching radius/elevation.
3. **`SurfaceDocked`**
   - Full-width flush surface anchored to the top viewport edge with `statusBarsPadding`, `56.dp` content row, and `1.dp` `border.subtle` bottom rule once scrolled (`scrollOffset > 0`).

### Surface Materiality (`AuraTopAppBarSurface`)

- **`SolidElevated`**: `surface.floating` fill + `1.dp` `border.subtle` hairline + `8.dp` ambient shadow (`0.dp` flat blend on `SurfaceDocked` when unscrolled). Default fallback across all API levels.
- **`TranslucentGlass` (`GlassStyle.FROSTED` / `GlassStyle.LIQUID`)**:
  - **Layer 0 -> Layer 1 -> Layer 2 Spatial Pass-Through:** Scrolls live Layer 0 canvas content directly behind the floating capsules through a progressive Layer 1 top edge scrim (`interaction.scrim`).
  - **`GlassStyle.FROSTED`:** `28.dp` uniform backdrop blur + `1.3x` saturation boost (API 31+), Light `White 72%` / Dark `#1E1E22 68%` tint, `1.dp` diagonal specular stroke (`White 55% -> 8%`), dual shadow (`Y-offset 10.dp, Blur 24.dp, Alpha 15%`).
  - **`GlassStyle.LIQUID`:** `8.dp` convex edge bevel AGSL `RuntimeShader` refraction + `~1.2.dp` chromatic dispersion + `6.dp` Fresnel inner glow (`White 35%`) over a soft Safe-Zone blur (`>= 48.dp` touch target on `40×40dp` Back & `42dp` grouped right action pill), plus viscous Metaball merging/splitting (`metaballSplitProgress`) and spring-driven Layer 3 **Morphing Popover** for `[More]`.
  - Falls back cleanly without crash on API levels without AGSL (`RuntimeShader`) or hardware backdrop blur.

### Scroll Behaviors (`AuraTopAppBarScrollBehavior`)

- **`Pinned`**: Stays anchored at top while content scrolls underneath; transitions resting separation (`SolidElevated` border/shadow opacity and blur intensity) smoothly as `firstVisibleItemScrollOffset > 0`.
- **`EnterAlways`**: Translates upward smoothly into negative offset (clipped safely below system status bar region or faded cleanly) on upward scroll, snapping right back into view on downward scroll.
- **`CompactOnScroll`**: Starts expanded (`titleLarge`, optional rich subtitle or attached `subContent` `AuraChipButton` ribbon) and compresses smoothly toward a compact `48.dp` capsule (`titleMedium`, condensed actions) as scroll progresses.

## 3. Semantic Token Bindings

| Element | `FloatingCapsule` / `FloatingSplit` (`SolidElevated`) | `TranslucentGlass` Mode | `SurfaceDocked` |
|---|---|---|---|
| Container | `surface.floating` | `surface.floating @ 0.78 / 0.72 alpha` + blur | `surface.default` (unscrolled) -> `surface.raised` (scrolled) |
| Border / Divider | `1.dp` `border.subtle` | `1.dp` luminous `border.subtle` | Bottom hairline `border.subtle` |
| Title Text | `content.primary` (`typography.titleMedium` / `titleLarge`) | `content.primary` | `content.primary` |
| Subtitle Text | `content.secondary` (`typography.labelMedium`) | `content.secondary` | `content.secondary` |
| Navigation & Actions | `AuraIconButton` (`Ghost` inside capsule, `Floating` in split) | `AuraIconButton` (`Ghost` / `Floating`) | `AuraIconButton` (`Ghost`) |
| Sub-ribbon Chips | `AuraChipButton` (`Filter` / `Tag`) | `AuraChipButton` (`Glass`) | `AuraChipButton` (`Filter`) |
| Underlap Edge Scrim | Optional `0.06f` fade | `interaction.scrim` progressive top fade | None |

## 4. State Precedence

1. **Scrolled / Elevated (`scrollFraction > 0f`)**: Stronger shadow/border definition (or frost blur in `TranslucentGlass`) and compacted header height under `CompactOnScroll`.
2. **Collapsed / Hidden (`EnterAlways` hidden)**: Translated cleanly out of content lane while preserving layout stability; returns on minimal downward scroll delta.
3. **Modal / Menu Open**: Associated overflow menu or sheet locks bar visibility (`Pinned`) and suppresses auto-collapse.
4. **Resting (`scrollFraction == 0f`)**: Maximum transparency / minimal shadow weight so content canvas feels unified with the bar.

## 5. Responsive Layout Behavior (`Smallest Width × Display Ratio`)

| Profile | Width / Ratio Condition | Floating Geometry Adaptation |
|---|---|---|
| `ExtraSmall` | `sw < 360.dp` | `FloatingCapsule` uses `12.dp` horizontal margin, `48.dp` capsule height, max `1` visible action + overflow `AuraIconButton`. Avoid `FloatingSplit` unless title is omitted (icon-only islands). |
| `Compact` | `360–599.dp` | `FloatingCapsule` (`16.dp` margin, `56.dp` height) or `FloatingSplit` for media headers; max `2–3` actions. Sub-ribbon chips scroll horizontally (`LazyRow`). |
| `Medium · Square` | `600–839.dp`, `ratio < 1.35` | Constrain `FloatingCapsule` `maxWidth = 680.dp` (centered) or use `FloatingSplit` with `20.dp` outer margins so the pill maintains sculptural proportions across a square foldable. |
| `Medium · UltraWide` | `ratio >= 2.1` | Switch to compact height (`48.dp` capsule) and prefer `FloatingSplit` or `EnterAlways` scroll collapse so shallow vertical windows (`~350–420.dp` tall) retain comfortable reading area. |
| `Expanded` | `width >= 840.dp` | Pane-aware integration with `AuraAdaptiveTwoPane`: avoid spanning a single floating pill across both panes; cap standalone bar at `AuraLayout.maxContentWidth` (`24.dp` outer margin) or mount pane-local `FloatingSplit` islands. |

## 6. Motion & Reduced Motion

- **Collapse / Reveal (`EnterAlways` & `CompactOnScroll`)**: Driven by spring physics (`AuraTheme.motion.pressOutSpring()`) on scroll release so partial collapses settle cleanly onto either `0%` or `100%` states without sticking halfway.
- **Surface Material Morph**: Transition between resting and scrolled elevation/border/frost uses `tween(AuraTheme.motion.stateChangeDurationMillis)`.
- **Reduced Motion (`ANIMATOR_DURATION_SCALE == 0`)**: Replaces sliding translation and height morphing with immediate visibility / opacity thresholds and crisp elevation border toggles.

## 7. Accessibility

- **Heading Landmark**: Title node MUST expose `semantics { heading() }` so screen-reader navigation jumps straight to screen headings inside floating pills.
- **Touch Bounds**: Both `FloatingCapsule` slot buttons and detached `FloatingSplit` islands enforce `>= 48.dp × 48.dp` hit targets; `FloatingSplit` maintains at least `16.dp` unobstructed center clearance.
- **Focus Order**: Keyboard `Tab` traverses Leading Navigation -> Trailing Actions -> Optional Sub-ribbon `AuraChipButton`s -> Main Screen Content (skipping scrolled-underneath obscured items outside the visible viewport).
- **Reduced Transparency & High Contrast**: Automatically bypasses translucent blur layers in favor of opaque `SolidElevated` surfaces (`>= 7:1` headline contrast when High Contrast is active).

## 8. Cross-Platform API Contracts

<!-- API:ANDROID:BEGIN -->
```kotlin
enum class AuraTopAppBarStyle {
    FloatingCapsule,
    FloatingSplit,
    SurfaceDocked,
}

enum class AuraTopAppBarSurface {
    SolidElevated,
    TranslucentGlass,
}

enum class AuraTopAppBarScrollBehavior {
    Pinned,
    EnterAlways,
    CompactOnScroll,
}

@Composable
fun FloatingTopPillBar(
    title: String,
    scrollProgress: Float,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    glassStyle: GlassStyle = GlassStyle.LIQUID,
    isScrollingDown: Boolean = false,
    hideTitleOnScrollDown: Boolean = true,
    metaballSplitProgress: Float = 0f,
    onBackClick: (() -> Unit)? = null,
    onSearchClick: (() -> Unit)? = null,
    isBookmarked: Boolean = false,
    onBookmarkClick: (() -> Unit)? = null,
    moreMenuExpanded: Boolean = false,
    onMoreMenuExpandedChange: (Boolean) -> Unit = {},
)

@Composable
fun AuraTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    style: AuraTopAppBarStyle = AuraTopAppBarStyle.FloatingCapsule,
    surface: AuraTopAppBarSurface = AuraTopAppBarSurface.SolidElevated,
    scrollBehavior: AuraTopAppBarScrollBehavior = AuraTopAppBarScrollBehavior.Pinned,
    scrollFraction: Float = 0f,
    centerTitle: Boolean = false,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    subContent: (@Composable () -> Unit)? = null,
)
```
<!-- API:ANDROID:END -->

<!-- API:IOS:BEGIN -->
```swift
public enum AuraTopAppBarStyle {
    case floatingCapsule, floatingSplit, surfaceDocked
}

public enum AuraTopAppBarSurface {
    case solidElevated, translucentGlass
}

public enum AuraTopAppBarScrollBehavior {
    case pinned, enterAlways, compactOnScroll
}

public struct AuraTopAppBar<NavigationIcon: View, Actions: View, SubContent: View>: View {
    public init(
        title: String,
        subtitle: String? = nil,
        style: AuraTopAppBarStyle = .floatingCapsule,
        surface: AuraTopAppBarSurface = .solidElevated,
        scrollBehavior: AuraTopAppBarScrollBehavior = .pinned,
        scrollFraction: CGFloat = 0,
        centerTitle: Bool = false,
        @ViewBuilder navigationIcon: () -> NavigationIcon,
        @ViewBuilder actions: () -> Actions,
        @ViewBuilder subContent: () -> SubContent
    )
}
```
<!-- API:IOS:END -->

<!-- API:WEB:BEGIN -->
```ts
export type AuraTopAppBarStyle = 'floatingCapsule' | 'floatingSplit' | 'surfaceDocked';
export type AuraTopAppBarSurface = 'solidElevated' | 'translucentGlass';
export type AuraTopAppBarScrollBehavior = 'pinned' | 'enterAlways' | 'compactOnScroll';

export interface AuraTopAppBarProps {
  title: string;
  subtitle?: string;
  style?: AuraTopAppBarStyle;
  surface?: AuraTopAppBarSurface;
  scrollBehavior?: AuraTopAppBarScrollBehavior;
  scrollFraction?: number;
  centerTitle?: boolean;
  navigationIcon?: React.ReactNode;
  actions?: React.ReactNode;
  subContent?: React.ReactNode;
}
```
<!-- API:WEB:END -->
