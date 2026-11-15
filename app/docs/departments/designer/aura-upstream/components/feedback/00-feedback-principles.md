# 00 · Feedback & Status Principles

## 1. Purpose

Defines the **Feedback & Progress Component Taxonomy** governing non-intrusive status badges, numeric unread counters, and state indicators across Aura applications (**My Planner**, **My Finance**, **My Drink Diary**, **Home Launcher**, and the **Design System Catalog**).

---

## 2. Feedback Taxonomy

| Component | Role | Representative Usage |
|---|---|---|
| `AuraBadge` | Compact inline status pill, semantic count tag, or minimal presence dot | Catalog `"Ready"` / `"Planned"` status pills, unread notifications (`3`, `99+`), portfolio alert dot, bottle stock level (`In Cellar`) |
| `AuraBadgedBox` | Anchored overlay container placing an `AuraBadge` at the top-end corner of icons/actions | Filter button active count badge, notification bell dot |

---

## 3. Core Rules

- **`FBK-RULE-001` (Semantic Role Clarity):** Never decorate semantic statuses (`Success`, `Warning`, `Error`, `Info`) with brand colors when communicating gain/loss, deadline alert, or error conditions; consume `AuraTheme.colors.status.*`.
- **`FBK-RULE-002` (Overflow Formatting):** Notification counters exceeding `maxCount` (default `99`) must compact cleanly to `"${maxCount}+"` so capsule width stays predictable.
- **`FBK-RULE-003` (Passive Affordance):** `AuraBadge` is strictly a read-only visual status cue and must never impersonate a clickable `AuraChipButton`.
