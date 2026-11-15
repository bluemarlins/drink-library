# 00 · Aura Design System Principles

## 1. Purpose

**Aura** is a general-purpose Android (Jetpack Compose) design system built to provide visual and behavioral consistency across multiple future applications.

## 2. Base Concept

- **Alignment:** Clean, approachable surface hierarchy and blue-led clarity inspired by Samsung One UI.
- **Differentiation:** First-class support for expressive visual depth and tactile interaction — **Blur, Glassmorphism, Liquid Glass, Spring Animation, and Physics-based motion**.
- **Scope:** Foundation tokens (`color`, `typography`, `dimension`, `material`, `motion`), theme composition (`AuraTheme`), and reusable Compose components (`:design-system`).

## 3. Core Principles

1. **Tokens first.** Components never hardcode hex colors, `dp`/`sp` literals, or raw animation durations.
2. **Three token tiers.**
   - **Primitive (`AuraPalette`, etc.):** Raw values without meaning.
   - **Semantic (`AuraColors`, etc.):** Role-based tokens consumed by components.
   - **Component (optional, later):** Per-component overrides built on semantic tokens.
3. **Code-first single source of truth.** Kotlin in `:design-system` is the authoritative source; Figma Variables mirror the Kotlin token structure.
4. **Two-axis theming.** Color mode (`Light` / `Dark`) and surface material (`Solid` → future `Glass` / `LiquidGlass`) vary independently.
5. **Separate API.** `AuraTheme` and `AuraColors` do not depend on Material 3 (`material3`), keeping the public surface clean and portable.
6. **Accessible by default.** Semantic pairs target WCAG AA contrast (4.5:1 text, 3:1 UI/large text), scalable typography (`sp`), and 48dp minimum touch targets.
7. **Degrade gracefully.** Advanced material treatments (blur/shaders) must cleanly fall back to `Solid` on older API levels or reduced-transparency settings.
8. **Usage principles before UI.** Every component category and spec must define explicit selection criteria (`When to Use`, `When NOT to Use`, decision trees, and visual hierarchy rules) before detailing visual styles.
9. **Bidirectional `Specs ↔ Code` for multi-platform reuse.** Every component is backed by a structured Markdown specification (`docs/components/`) separating the platform-agnostic contract (Intent, Anatomy, Variants, State Precedence, Semantic Token Bindings, Accessibility) from platform bindings (`Android Compose`, `iOS SwiftUI`, `Web React`).

## 4. Glossary

| Term | Meaning |
|---|---|
| `AuraPalette` | Tier 1 primitive color values |
| `AuraColors` | Tier 2 semantic color roles |
| `AuraMotion` | Tier 2 semantic interaction motion tokens |
| `AuraTypography` | Tier 2 semantic typography roles with responsive scale support |
| `AuraWindowInfo` | Device/window telemetry (`smallestWidthDp`, `widthDp`, `heightDp`, `displayRatio`) |
| `AuraLayout` | Tier 2 responsive layout & two-pane breakpoints derived from `AuraWindowInfo` |
| `AuraColorMode` | Color axis (`Light`, `Dark`) |
| `AuraMaterial` | Surface treatment axis (`Solid`, future `Glass`, `LiquidGlass`) |
| `AuraTheme` | Root `@Composable` provider exposing `AuraTheme.colors`, `typography`, `motion`, `window`, `layout`, `colorMode`, `material` |
| `Component Spec` | Bidirectional Markdown contract in `docs/components/` separating universal rules from platform API bindings |
