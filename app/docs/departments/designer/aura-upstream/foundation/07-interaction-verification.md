# 07. Interaction & Visual Verification Strategy

Aura Design System verifies Motion Interaction (`MOT-RULE-001`–`MOT-RULE-007`) and Visual Materiality (`VIS-RULE-001`–`VIS-RULE-007`) through a **3-tier automated test pyramid** backed by a manual emulator inspection checklist. Screenshot pixel-diff regression (Paparazzi/Roborazzi) and Macrobenchmark frame-timing harnesses are intentionally out of scope.

---

## 1. Three-Tier Verification Architecture

| Tier | Mechanism | Execution Environment | Rules Verified |
|---|---|---|---|
| **Tier 1: Static Contract (`EVAL-004`, `EVAL-007`, `EVAL-008`)** | [`SpecCodeConsistencyTest.kt`](../../design-system/src/test/kotlin/com/example/ds/contract/SpecCodeConsistencyTest.kt) parses Markdown frontmatter/bodies and production Kotlin source (stripping `@Preview`) | Pure JVM (`:design-system:testDebugUnitTest`) | Zero numeric `tween`/`spring` literals (`MOT-RULE-002`); `layer` & `material_allowed` frontmatter; no `GlassSurface` in `content` layer (`VIS-RULE-001`); `"Reduced Transparency"` & `"4.5:1"` contrast contracts (`VIS-RULE-003`, `VIS-RULE-004`); `motion_track` & test coverage (`MOT-RULE-003`) |
| **Tier 2: Token & Material Matrix Invariants** | [`MotionTokenInvariantTest.kt`](../../design-system/src/test/kotlin/com/example/ds/contract/MotionTokenInvariantTest.kt), [`MaterialResolutionTest.kt`](../../design-system/src/test/kotlin/com/example/ds/foundation/theme/MaterialResolutionTest.kt) | Pure JVM (`:design-system:testDebugUnitTest`) | Enter > Exit duration asymmetry (`MOT-RULE-005`); Press-in < Press-out; stagger budget bounds (`MOT-RULE-006`); Standard vs Expressive damping separation; full SDK x Material x `reduceTransparency` truth table (`VIS-RULE-004`) |
| **Tier 3: Deterministic `mainClock` Behavior Tests** | [`ComponentMotionBehaviorTest.kt`](../../design-system/src/test/kotlin/com/example/ds/motion/ComponentMotionBehaviorTest.kt) using Robolectric (`@Config(sdk = [33])`) + Compose `mainClock.autoAdvance = false` | JVM + Robolectric (`:design-system:testDebugUnitTest`) | Mid-flight spring scales, monotonic progress, Reduced Motion instant snap + semantic preservation (`MOT-RULE-007`), mid-flight interruption continuity (`MOT-RULE-004`), Reduced Transparency fallback path (`aura_glass_surface_solid_fallback`) |

---

## 2. Behavior Test Instrumentation Design

To keep verification deterministic and free of pixel-rendering flakiness while preserving clean public APIs:

1. **`AuraMotionSemantics.kt` (`internal`):** Defines `AuraMotionScaleKey` and `AuraMotionProgressKey` (`SemanticsPropertyKey<Float>`) attached directly to root semantics modifiers of instrumented components.
2. **Solid Fallback Probe (`GlassSurface.kt`):** When `AuraTheme.reduceTransparency == true`, the opaque fallback box exposes `Modifier.testTag("aura_glass_surface_solid_fallback")`.
3. **`reduceMotion` Injection:** Because Robolectric defaults `Settings.Global.ANIMATOR_DURATION_SCALE` to `1f`, tests inject `AuraTheme(motion = AuraMotionDefaults.Standard.copy(reduceMotion = true))` to deterministically assert Reduced Motion behavior.

---

## 3. Component Motion & Visual Test Matrix (`EVAL-008`)

Every component below has dedicated deterministic `mainClock` assertions in `ComponentMotionBehaviorTest.kt` (referenced by `EVAL-008`):

| Component | Test Cases Verified | Rules Covered |
|---|---|---|
| `AuraButton` | Press (`pressedScaleDefault`), Release settle (`1.0f`), Mid-flight press->release continuity, `reduceMotion = true` instant snap with `stateDescription = "In progress"` preserved | `MOT-RULE-002`, `MOT-RULE-004`, `MOT-RULE-005`, `MOT-RULE-007` |
| `AuraSwitch` | Unchecked (`0f`) -> Checked (`1f`) monotonic spring progress and settle; `reduceMotion = true` immediate snap | `MOT-RULE-002`, `MOT-RULE-004`, `MOT-RULE-007` |
| `AuraFab` | Extended collapse/expand progress transition; press scale settle | `MOT-RULE-002`, `MOT-RULE-003`, `MOT-RULE-004` |
| `AuraBottomIsland` | Active indicator transition between index `0` and index `2` under `Expressive` profile, velocity-driven elastic stretch + settle (`1f`), `reduceMotion = true` snap | `MOT-RULE-003`, `MOT-RULE-004`, `MOT-RULE-007` |
| `AuraDialog` | `visible = true` spatial entry scale/alpha (`Expressive` profile), `reduceMotion = true` instant open (`progress == 1f`) | `MOT-RULE-003`, `MOT-RULE-005`, `MOT-RULE-007` |
| `GlassSurface` | `reduceTransparency = true` renders `"aura_glass_surface_solid_fallback"`; `reduceTransparency = false` omits solid fallback tag | `VIS-RULE-004` |

> **Why RenderEffect/AGSL Pixels Are Not Asserted in Robolectric:** Robolectric runs on host JVM Skia software rendering without Android 12/13 HardwareRenderer AGSL/RenderEffect GPU shaders. Therefore, automated tests verify material resolution decisions (`MaterialResolutionTest`), solid fallback branching (`ComponentMotionBehaviorTest`), and token compliance (`SpecCodeConsistencyTest`), while optical blur/refraction appearance is verified via manual inspection.

---

## 4. Verification Commands & Manual Checklist

```powershell
.\gradlew.bat :design-system:testDebugUnitTest
.\gradlew.bat :design-system-preview:testDebugUnitTest
.\gradlew.bat assembleDebug
```

**Manual Device Checklist (`:design-system-preview` -> Interaction Principles screen):**
- Verify `Glass` (`RenderEffect` blur) on Android API 31–32 and `LiquidGlass` (AGSL refractive bevel) on API 33+ emulators.
- Toggle system **Remove animations** ON -> verify spatial motion snaps immediately while loading/color states remain legible.
- Toggle system **Battery Saver** or **High-contrast text** ON -> verify `Glass` and `LiquidGlass` degrade cleanly to crisp `Solid` surfaces.
- Inspect Light and Dark `AuraColorMode` contrast (`>= 4.5:1` text, `>= 3:1` borders/icons) over busy scrolling backdrops.
