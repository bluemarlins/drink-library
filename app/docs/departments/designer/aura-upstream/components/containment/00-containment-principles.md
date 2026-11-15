# 00 · Containment & Surface Principles

## 1. Purpose

Defines the **Containment Component Taxonomy** and the **Surface Separation Principles** governing how Aura groups related information, establishes visual hierarchy without clutter, and adapts content density across its four flagship applications:

- **My Planner:** Daily timeline blocks, checklist cards, and calm paper-like reflection sheets
- **My Finance:** Portfolio summary cards, holdings rows, and modal confirmation dialogs
- **My Drink Diary:** Bottle tasting note cards, sensory radar containers, and cellar filter sheets
- **Home Launcher:** Translucent `Glass` widget cards floating over custom wallpapers

---

## 2. Containment Component Taxonomy

| Component | Role | Representative Usage Across Target Apps |
|---|---|---|
| `AuraCard` | Grouped surface container (`Elevated`, `Filled`, `Outlined`, `Glass`) with optional interactive click | Holdings summary (**Finance**), Tasting log entry (**Drink Diary**), Agenda block (**Planner**), Glass widget (**Launcher**) |
| `AuraListItem` | Structured single/multi-line row inside lists or settings sheets *(Planned)* | Transaction rows (**Finance**), Preference toggles (**Settings**) |
| `AuraBottomSheet` | Modal or docked bottom overlay sheet *(Planned)* | Quick log entry (**Drink Diary**), Account filter sheet (**Finance**) |
| `AuraDialog` | High-priority modal decision / alert container *(Planned)* | Destructive delete confirmation, Quick edit form |
| `AuraDivider` | Subtle `1.dp` hairline separator (`border.subtle`) *(Planned)* | Section breaks inside dense cards or lists |

---

## 3. Core Containment Rules

- **`CNT-RULE-001` (Tonal & Border Restraint Over Drop-Shadow Soup):** Aura prioritizes clean semantic surface stepping (`surface.default`, `surface.raised`, `surface.sunken`, `surface.floating`) paired with `1.dp` semantic borders (`border.subtle`, `border.default`) over heavy blurry shadows that dirty light themes and vanish in dark themes.
- **`CNT-RULE-002` (No Nested Interactive Card Traps):** Avoid nesting clickable `AuraCard` elements inside another clickable `AuraCard`. When an internal action (e.g., an `AuraIconButton` bookmark) lives inside a clickable card, ensure independent hit bounds (`>= 48.dp`) or use a passive card with explicit action buttons.
- **`CNT-RULE-003` (Surface Contrast Pairing):**
  - Place `Outlined` (`surface.default` + `border.default`) or `Elevated` (`surface.raised`) cards on `background.primary`.
  - Use `Filled` (`surface.sunken`) cards on `surface.default` sheets or secondary callout blocks where softer grouping is desired.
  - Reserve `Glass` (`surface.floating`) exclusively for overlays and wallpaper-backed widgets (**Home Launcher**).
- **`CNT-RULE-004` (Flat Canvas Protection for Writing):** Do not trap long-form `Paperless` diary writing (`My Planner`) inside small padded boxes; reserve cards for discrete summarized records and grouped controls.
