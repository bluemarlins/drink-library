# 01 · Token Architecture

## 1. Purpose

Defines how Aura structures, names, and exposes design tokens in Kotlin and how those tokens map to Figma Variables.

## 2. Token Tiers & Aliasing Rules

```
Tier 1: Primitive (AuraPalette)   ──▶ Raw values (e.g., Blue600, Neutral50)
   ▲
   │ referenced only by Tier 2
Tier 2: Semantic (AuraColors)     ──▶ Role-based (e.g., brand.primary, surface.floating)
   ▲
   │ referenced by Components & Tier 3
Tier 3: Component (future)        ──▶ Component-specific bindings (optional)
```

- Components and app screens must only consume **Tier 2** (or Tier 3) via `AuraTheme.*`.
- Tier 2 tokens must alias Tier 1 primitives (plus explicit alpha modifiers when needed for overlays/scrims).

## 3. Kotlin Naming & API Structure

- **Brand prefix:** `Aura` (`AuraTheme`, `AuraColors`, `AuraTypography`, `AuraMotion`, `AuraWindowInfo`, `AuraLayout`, `AuraPalette`, future `AuraSpacing`).
- **Immutability:** Token containers are `@Immutable data class` types backed by `staticCompositionLocalOf` in `AuraTheme`.
- **Naming grammar:** `{domain}.{group}.{role}` in Kotlin (`AuraTheme.colors.surface.floating`, `AuraTheme.typography.titleMedium`, `AuraTheme.layout.gridColumns`, `AuraTheme.motion.pressInSpring()`).

## 4. Figma Variable Mapping

Since Kotlin is the source of truth, Figma Variables mirror Kotlin 1:1:

| Kotlin Source | Figma Collection | Modes | Example Name |
|---|---|---|---|
| `AuraPalette` | `Primitives` | `Value` | `neutral/50`, `blue/600` |
| `AuraColors` | `Color` | `Light`, `Dark` | `surface/floating`, `brand/primary` |
| `AuraTypography` | `Typography` (Text Styles) | `Scale` (`Compact`, `Square`, `Expanded`) | `title/medium`, `mono/small` |
| `AuraLayout` | `Layout` | `ExtraSmall`, `Compact`, `Landscape`, `Square`, `Expanded` | `margin/horizontal`, `grid/columns` |
| Future `AuraMaterial` tokens | `Material` | `Solid`, `Glass`, `LiquidGlass` | `floating/blurRadius` |

## 5. Open Questions

- Export pipeline: optionally add a Gradle task later to serialize Kotlin tokens to W3C DTCG JSON for Figma import.
