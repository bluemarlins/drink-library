# 02 · Color

## 1. Purpose

Defines Aura's primitive palette (`AuraPalette`), semantic color roles (`AuraColors`), and Light/Dark mappings (`AuraColorSchemes`).

## 2. Base Concept & One UI Alignment

- **Neutral Temperature:** Cool Gray with subtle blue undertones to harmonize with One UI surfaces and future translucent Glass layers.
- **Light Canvas Model:** `background.primary` uses `Neutral50` (`#F2F3F7`) so crisp `Neutral0` (`#FFFFFF`) cards separate cleanly without heavy borders.
- **Dark Canvas Model:** `background.primary` uses OLED-friendly `Neutral1000` (`#000000`) with stepped charcoal surfaces (`#17191E` → `#1E2128` → `#282C35`) so elevated layers and future Glass highlights stand out.
- **Brand Hue:** One UI-inspired vivid Blue (`Blue500 = #0381FE` for focus/accents; `Blue600 = #006FE0` in Light for WCAG AA 4.5:1 button contrast; `Blue400 = #3E95FF` in Dark).
- **Info Separation:** Status `info` uses Cyan (`Cyan700` / `Cyan400`) rather than Brand Blue so informational feedback stays distinct from primary actions.

## 3. Semantic Role Groups (`AuraColors`)

| Group | Roles | Notes |
|---|---|---|
| `background` | `primary`, `secondary` | App canvas and secondary backdrop |
| `surface` | `default`, `raised`, `sunken`, `floating`, `inverse` | `floating` is the primary target for future `Glass` / `LiquidGlass` |
| `content` | `primary`, `secondary`, `tertiary`, `disabled`, `inverse` | Text and icon hierarchy |
| `brand` | `primary`, `onPrimary`, `primaryPressed`, `subtle`, `onSubtle` | Primary actions and tonal brand accents |
| `border` | `subtle`, `default`, `strong`, `focus` | Dividers, outlines, focus rings |
| `status` | `error`, `success`, `warning`, `info` (`solid`, `onSolid`, `subtle`, `onSubtle`) | Semantic feedback |
| `interaction` | `pressedOverlay`, `selectedContainer`, `onSelected`, `disabledContainer`, `scrim` | Interactive state tokens |

## 4. Accessibility Rules

- All `content.*` on `background.primary` / `surface.default` / `surface.raised` (except `disabled`) must meet ≥ 4.5:1 contrast.
- All `brand.onPrimary` on `brand.primary` and `status.*.onSolid` on `status.*.solid` must meet ≥ 4.5:1 contrast.
- Light `success.solid` uses `Green700` (`#15803D`) instead of `Green600` to keep white text ≥ 4.5:1.
- Dark `brand.primary` and `status.*.solid` use lighter 400-tones paired with dark `onPrimary`/`onSolid` text.

## 5. Implementation Files

- [`AuraPalette.kt`](../../design-system/src/main/kotlin/com/example/ds/foundation/color/AuraPalette.kt)
- [`AuraColors.kt`](../../design-system/src/main/kotlin/com/example/ds/foundation/color/AuraColors.kt)
- [`AuraColorSchemes.kt`](../../design-system/src/main/kotlin/com/example/ds/foundation/color/AuraColorSchemes.kt)
- [`AuraTheme.kt`](../../design-system/src/main/kotlin/com/example/ds/foundation/theme/AuraTheme.kt)
