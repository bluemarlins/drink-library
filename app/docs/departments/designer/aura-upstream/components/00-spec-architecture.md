# 00 · Component Specification Architecture (`Specs ↔ Code`)

## 1. Purpose

Defines the bidirectional **Markdown Specification ↔ Platform Code** contract for Aura components. For the mandatory pass/fail quality gate (`EVAL-001` – `EVAL-006`) enforced against every spec and Kotlin file, see [01-component-evaluation-ruleset.md](01-component-evaluation-ruleset.md).

Each component specification serves as an **OS-agnostic behavioral and semantic contract** so that:
1. **Usage principles come first:** Designers, engineers, and AI agents understand *when* and *why* to select a component before inspecting API props.
2. **Android (Jetpack Compose) is implemented cleanly today:** Code in `:design-system` strictly satisfies the spec's variants, sizing, state precedence, token bindings, and accessibility rules.
3. **Future platforms (`iOS SwiftUI`, `Web React`) reuse the same contract:** Platform implementations map directly to the shared Layer A contract without re-inventing component intent, state rules, or token semantics.

---

## 2. Two-Layer Specification Structure

Every component spec in `docs/components/<category>/<component>.md` is divided into **Layer A (Universal Contract)** and **Layer B (Platform Bindings)**.

```text
docs/components/<category>/<component>.md
├── YAML Frontmatter          # Machine-readable identity, status, version, platforms, token dependencies
├── Layer A: Universal Contract (OS-Agnostic, ~80%)
│   ├── 1. Intent & Usage Principles (When to Use / When NOT to Use / Do / Don't / Context Rules)
│   ├── 2. Anatomy & Canonical Part IDs
│   ├── 3. Variant, Size & Material Matrix (logical px + forbidden combinations)
│   ├── 4. Deterministic State Precedence & Behavior
│   ├── 5. Semantic Token Bindings (Part × Variant × State -> AuraColors / AuraTypography / AuraLayout)
│   ├── 6. Interaction Motion Contract (AuraMotion + Reduced Motion, or passive rationale)
│   ├── 7. Responsive Layout Behavior (Smallest Width × Display Ratio)
│   └── 8. Accessibility & Guardrail Rules (Rule IDs)
└── Layer B: Platform Bindings (OS-Specific, ~20%)
    ├── Android (Jetpack Compose)  <!-- API:ANDROID:BEGIN / END -->
    ├── iOS (SwiftUI Reference)    <!-- API:IOS:BEGIN / END -->
    └── Web (React/TS Reference)   <!-- API:WEB:BEGIN / END -->
```

### Bidirectional Sync (`Specs ↔ Code`)

- **Spec → Code (Design/Contract-First):** Author Layer A rules and Layer B target signatures before writing platform code or translating to a new OS (`iOS` / `Web`).
- **Code → Spec (Implementation Sync):** When a public Kotlin API changes in `:design-system`, update the `<!-- API:ANDROID:BEGIN --> ... <!-- API:ANDROID:END -->` block and props/behavior tables in the same change so human documentation and AI contracts never drift.

---

## 3. Canonical Anatomy & State Rules

### 3.1. Canonical Part IDs

All platforms must preserve the same part identifiers across token bindings, semantics, and tests:
- `root` — Outer container, hit target boundary, accessibility role owner
- `label` — Primary text content
- `leadingIcon` / `trailingIcon` — Optional pre/post visual cues
- `progressIndicator` — Asynchronous loading overlay or indicator
- `supportingText` / `errorText` — Secondary guidance or validation feedback (input components)

### 3.2. Universal State Precedence

Unless explicitly overridden by a component spec, concurrent interactive states resolve in the following strict order:

$$\text{Loading} \succ \text{Disabled} \succ \text{Pressed} \succ \text{Focused} \succ \text{Hovered} \succ \text{Rest}$$

---

## 4. Cross-Platform Unit & Token Mapping

Layer A specs express dimensions in **logical units (`px`)** and **semantic token paths** rather than OS-specific units. Each platform binding converts them deterministically:

| Concept | Layer A Spec | Android (Compose) | iOS (SwiftUI) | Web (CSS / React) |
|---|---|---|---|---|
| Spacing & Bounds | `16px` | `16.dp` | `16pt` (`CGFloat`) | `1rem` (`16px`) |
| Typography Size | `14px` | `14.sp` | `Font.system(size: 14)` / Dynamic Type | `0.875rem` |
| Minimum Hit Target | `48px` (mobile) | `48.dp × 48.dp` | `44pt × 44pt` | `44px × 44px` (coarse) / `36px` (fine) |
| Color Tokens | `brand.primary` | `AuraTheme.colors.brand.primary` | `auraTheme.colors.brand.primary` | `var(--aura-brand-primary)` |
| Surface Material | `Solid \| Glass` | `AuraMaterial` | `Material` / `.ultraThinMaterial` | Solid / `backdrop-filter: blur()` |

---

## 5. Naming & File Conventions

- **Component IDs:** `aura.component.<kebab-case-name>` (e.g., `aura.component.button`)
- **Rule IDs:** `<CAT>-RULE-<NNN>` for category principles (e.g., `BTN-RULE-001`) and `<COMP>-<NNN>` for component validation rules (e.g., `BTN-001`)
- **Spec File Location:** `docs/components/<category>/<kebab-case-name>.md`
- **Category Principles:** `docs/components/<category>/00-<category>-principles.md`
