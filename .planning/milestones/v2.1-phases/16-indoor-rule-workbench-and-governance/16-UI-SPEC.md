---
phase: 16
slug: indoor-rule-workbench-and-governance
status: approved
shadcn_initialized: false
preset: not applicable
created: 2026-04-16
---

# Phase 16 — UI Design Contract

> Visual and interaction contract for the indoor rule workbench and the rule governance center.

---

## Design System

| Property | Value |
|----------|-------|
| Tool | none |
| Preset | not applicable |
| Component library | Ant Design 5 |
| Icon library | `@ant-design/icons` |
| Font | `Noto Sans TC`, `PingFang TC`, `Microsoft JhengHei`, sans-serif |

---

## Spacing Scale

Declared values (must be multiples of 4):

| Token | Value | Usage |
|-------|-------|-------|
| xs | 4px | Icon gaps, compact tags |
| sm | 8px | Inline field gaps, card meta |
| md | 16px | Default form spacing |
| lg | 24px | Panel padding, drawer sections |
| xl | 32px | Workbench column gaps |
| 2xl | 48px | Major section breaks |
| 3xl | 64px | Full-page top spacing |

Exceptions: the workbench minimap rail may use 20px inset padding to align with card gutters in Ant Design modal content.

---

## Typography

| Role | Size | Weight | Line Height |
|------|------|--------|-------------|
| Body | 14px | 400 | 1.6 |
| Label | 13px | 600 | 1.5 |
| Heading | 20px | 700 | 1.35 |
| Display | 28px | 700 | 1.2 |

---

## Color

| Role | Value | Usage |
|------|-------|-------|
| Dominant (60%) | `#F4F7FB` | Page background, low-emphasis work areas |
| Secondary (30%) | `#FFFFFF` | Cards, workbench panels, filter surfaces |
| Accent (10%) | `#0F766E` | Primary actions, active behavior state, validation-pass emphasis |
| Destructive | `#C62828` | Disable/delete/destructive confirmation only |

Accent reserved for: `編輯互動規則`, `套用規則`, active behavior chips, current conflict focus row, minimap pick mode highlight, and governance enable actions. Never use the accent color for every neutral button or secondary tab.

---

## Copywriting Contract

| Element | Copy |
|---------|------|
| Primary CTA | `編輯互動規則` / `套用至此標記` |
| Empty state heading | `尚未建立任何互動規則` |
| Empty state body | `先新增一個行為，為它設定名稱、出現條件與效果，再決定是否套用到當前標記。` |
| Error state | `規則尚未通過檢核，請先修正標示欄位後再套用。` |
| Destructive confirmation | `停用規則`: `停用後將不再於治理中心或工作台視為啟用狀態，但內容仍保留，可稍後重新啟用。` |

---

## Layout Contract

### Workbench

- Use a full-screen modal or full-width drawer instead of a small modal.
- Three-column layout on desktop:
  - left rail `280px`: behavior list, add/duplicate/reorder/status controls
  - center flexible editor: behavior details, tabs for appearance / triggers / effects
  - right rail `320px`: minimap, point picking, path tools, validation summary
- On widths below `1280px`, collapse the right rail under the center editor as a stacked section.
- The parent marker form must not show the full rule editor inline anymore. It shows:
  - rule summary chips
  - validation summary
  - `編輯互動規則` button

### Governance Center

- Top filter bar stays sticky inside the page content.
- Main body uses:
  - left: searchable filter summary + conflict counters
  - center: rule-bearing entity table
  - right: detail drawer or side panel with conflict and trigger-chain inspection
- Rows must expose enough identity to scan quickly:
  - behavior name
  - behavior code
  - parent node / marker code
  - floor / building
  - linked entity
  - status
  - conflict count

---

## Interaction Contract

- All rule editing occurs in staged local state until the operator presses `套用至此標記`.
- Closing the workbench with dirty changes must show an unsaved-changes confirmation.
- Validation is two-step:
  - immediate local field validation
  - backend rule-graph validation before final apply
- Minimap tools must clearly show current mode:
  - `點位取點`
  - `路徑取點`
  - `疊加物取點`
- Behavior list items must show human-readable names first, then codes second. Generated labels like `互動行為 1` are allowed only as defaults before the operator renames them.
- Governance actions must never hide conflicts silently. If a rule is disabled due to conflict handling, the side panel must retain the previous conflict summary for operator review.

---

## Motion Contract

- Use motion sparingly. No continuous panel animation loops.
- Allowed motion:
  - drawer/modal open and close
  - behavior list active-state slide/fade
  - minimap point append flash
  - conflict row highlight on selection
- Avoid bouncing, pulsing, or auto-scrolling animations in admin.

---

## Registry Safety

| Registry | Blocks Used | Safety Gate |
|----------|-------------|-------------|
| Ant Design official | `Modal`, `Drawer`, `Tabs`, `Table`, `Form`, `Card`, `Tag`, `Alert`, `Steps`, `Splitter`-style layout via grid/flex | not required |
| Existing project shared assets | logo and current admin layout styles | not required |

---

## Checker Sign-Off

- [x] Dimension 1 Copywriting: PASS
- [x] Dimension 2 Visuals: PASS
- [x] Dimension 3 Color: PASS
- [x] Dimension 4 Typography: PASS
- [x] Dimension 5 Spacing: PASS
- [x] Dimension 6 Registry Safety: PASS

**Approval:** approved 2026-04-16
