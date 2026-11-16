# Motion, Materiality & Visual Interaction Rules

Aura separates **Color Mode** (`Light` / `Dark`) from **Surface Materiality** (`Solid` / `Glass` / `LiquidGlass`) and **Motion Track** (`Standard` / `Expressive`). Follow these rules when designing or coding consumer screens.

---

## 1. Four-Layer Surface Architecture (`VIS-RULE-001` – `VIS-RULE-006`)

Every Aura screen stacks across 4 explicit Z-axis layers so translucent chrome never blurs sibling chrome or obscures content:

| Layer | Name | Allowed Materials | Typical `AuraElevation` | Consumer Screen Usage |
|---|---|---|---|---|
| **Layer 0** | `canvas` | `Solid` | `level0` (`0.dp`) | `AuraScaffold`, `AuraAdaptiveTwoPane`, `FloatingGlassScaffold` scroll canvas |
| **Layer 1** | `content` | `Solid` | `level0`–`level1` (`0–2.dp`) | `AuraCard` (list/detail items), `AuraTextField`, `AuraButton`, `AuraSwitch`, `AuraSlider`, `AuraRatingBar` |
| **Layer 2** | `chrome` | `Solid`, `Glass`, `LiquidGlass` (`<= 1`) | `level1`–`level3` (`2–12.dp`) | `AuraTopAppBar`, `FloatingTopPillBar`, `AuraBottomIsland`, `FloatingBottomIsland`, `AuraSearchBar`, `AuraFab`, `AuraIconButton(Floating)`, `AuraChipButton(Glass)` |
| **Layer 3** | `spatial` | `Solid`, `Glass`, `LiquidGlass` (`<= 1`) | `level3`–`level4` (`12–24.dp`) | `AuraDialog`, `MorphingMorePopover` |

### Strict Placement Guardrails
1. **No Glass on List Cards (`VIS-RULE-001`):** `AuraCardVariant.Glass` is strictly scoped to wallpaper-backed Home Launcher widgets or isolated spatial tiles—never standard scrollable feed or catalog items. Use `AuraCardVariant.Outlined`, `Filled`, or `Elevated` inside lists/forms.
2. **Blur & Hero Budgets (`VIS-RULE-002`, `VIS-RULE-006`):** Keep `<= 3` simultaneous live-blur (`Glass` / `LiquidGlass`) panels in a visible viewport and **`<= 1` `LiquidGlass` focal hero** per screen (e.g., `FloatingBottomIsland`). If your screen already uses `FloatingBottomIsland(glassStyle = GlassStyle.LIQUID)` and `FloatingTopPillBar`, switch the top bar or overlay cards to `FROSTED` (`Glass`) or `Solid`.
3. **Always Protect Edge Contrast (`VIS-RULE-003`):** When floating glass chrome sits over scrollable items, use `FloatingGlassScaffold` (which draws Layer 1 protective vertical gradient scrims from `AuraTheme.elevation.level3.scrimAlpha` down to `Transparent` and attaches `.glassBackdropSource(backdropState)`). Ensure foreground text on glass keeps `>= 4.5:1` contrast and borders/icons keep `>= 3:1`.
4. **Hardware & Accessibility Fallback (`VIS-RULE-004`):** Never bypass `AuraTheme.reduceTransparency` or write device-specific `Build.VERSION` checks manually around glass panels. `GlassSurface` and Aura chrome components call `AuraTheme.effectiveMaterial(...)` automatically:
   - API 26–30 -> degrades blur/refraction cleanly to `Solid`.
   - API 31–32 -> `LiquidGlass` degrades gracefully to `Glass` `RenderEffect` blur.
   - Battery Saver or High-Contrast Text -> forces opaque `Solid` (`aura_glass_surface_solid_fallback` testTag).
5. **Elevation Tokens (`VIS-RULE-005`):** Read shadows and dim scrims exclusively from `AuraTheme.elevation.level0`–`level4`.

---

## 2. Dual-Track Motion System (`MOT-RULE-001` – `MOT-RULE-007`)

Aura replaces Material's radial clipped ripple with **Tactile Spring Cushion** + smooth `AuraColors.interaction` interpolation because radial ripples clip awkwardly across translucent `Glass` and `LiquidGlass` capsules.

| Rule | Principle | What Consumer App Code Must Do |
|---|---|---|
| **`MOT-RULE-001`** | **Purposeful Motion** | Every animation serves `Orient` (screen/pane entrance), `Focus` (selection/dialog/active tab), `Express` (liquid hero capsule), or `Connect` (scroll-linked header transitions). Skip unmotivated decorative loops. |
| **`MOT-RULE-002`** | **Tokens Only** | Zero numeric `tween(300)` or `spring(dampingRatio = ..., stiffness = ...)` in app UI code. Call `AuraTheme.motion.pressInSpring()`, `pressOutSpring()`, `spatialSpring()`, `elasticStretchSpring()`, `enterSpec()`, `exitSpec()`. |
| **`MOT-RULE-003`** | **Dual-Track Separation** | Keep root `AuraTheme(motionProfile = AuraMotionProfile.Standard)` (`0–3%` overshoot) for forms, catalogs, and settings. Apply `AuraMotionProfile.Expressive` only via nested `AuraTheme(motionProfile = AuraMotionProfile.Expressive)` on `<= 1` hero chrome/modal per screen. |
| **`MOT-RULE-004`** | **Interruptible Springs** | Drive stateful expands, active pill indicators, and press compressions with spring specs (`animateFloatAsState`, `Animatable`) so rapid re-taps reverse velocity smoothly without snapping. |
| **`MOT-RULE-005`** | **Timing Asymmetry** | Entrances stay `30–50%` longer than exits (`enterSpec()` vs `exitSpec()`), and press-in (`pressInDurationMillis = 90ms`) is faster than release rebound (`pressOutDurationMillis = 180ms`). |
| **`MOT-RULE-006`** | **Choreography Budget** | Assign transition roles (`Outgoing`, `Incoming`, `Persistent`). For staggered list/card reveals, pass `AuraTheme.motion.staggerDelay(index)` (automatically capped at `staggerBudgetMillis = 400ms`), and animate `<= 1/3` of visible items simultaneously. |
| **`MOT-RULE-007`** | **Reduced Motion** | Always check `AuraTheme.motion.reduceMotion` (or `rememberAuraReducedMotion()`) in custom app animations: snap spatial movement (`scale`, `translation`, `staggerDelay` already returns `0`) immediately while keeping opacity, color tint, and loading semantics legible. |

---

## 3. Writing Custom Motion in App-Specific Components

When building an app-specific interactive component (e.g., an expanding tasting-note panel or custom card action) outside `:design-system`:

```kotlin
val motion = AuraTheme.motion
val reducedMotion = rememberAuraReducedMotion() || motion.reduceMotion

val contentAlpha by animateFloatAsState(
    targetValue = if (expanded) 1f else 0f,
    animationSpec = if (expanded) motion.enterSpec() else motion.exitSpec(),
    label = "PanelAlpha",
)
```

For press compression on custom clickable cards that cannot use `AuraCard(onClick = ...)` directly, reuse `Modifier.fluidPressScale(interactionSource, enabled, pressedScale = motion.pressedScaleDefault)` from `com.example.ds.component`.

---

## 4. Consumer Review Checklist (`EVAL-001` – `EVAL-008` Adapted for Apps)

Before finishing any vibe-coded screen, verify:
- [ ] Zero `androidx.compose.material3.*` imports (`EVAL-002`).
- [ ] Zero hardcoded `Color(0x...)` in composables; colors read `AuraTheme.colors.*` (`EVAL-002`).
- [ ] Zero hardcoded `.sp` sizes; typography uses `AuraTheme.typography.*` + `AuraText` (`EVAL-002`).
- [ ] Long forms/feeds constrain width with `Modifier.widthIn(max = AuraTheme.layout.maxContentWidth)` or adapt via `AuraAdaptiveGrid` / `AuraAdaptiveTwoPane` (`EVAL-003`).
- [ ] Zero numeric `tween(...)` / `spring(...)` literals; custom motion uses `AuraTheme.motion.*` and honors `reduceMotion` (`EVAL-004`, `EVAL-008`).
- [ ] Floating glass is limited to Layer 2 chrome & Layer 3 modals (`<= 3` blur surfaces, `<= 1` `LiquidGlass` hero), leaving Layer 1 lists/forms `Solid` (`EVAL-007`).
- [ ] Run `scripts/verify-aura-app.ps1 -TargetAppDir <path> -FailOnViolation`.
