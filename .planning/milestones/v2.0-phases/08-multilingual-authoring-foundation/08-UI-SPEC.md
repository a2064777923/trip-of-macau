---
phase: 08
slug: multilingual-authoring-foundation
status: approved
shadcn_initialized: false
preset: not-applicable
created: 2026-04-13
---

# Phase 8 - UI Design Contract

> Visual and interaction contract for the admin-side multilingual authoring foundation. This phase extends the existing Ant Design Pro shell rather than inventing a new design system.

---

## Design System

| Property | Value |
|----------|-------|
| Tool | none |
| Preset | not applicable |
| Component library | Ant Design 5 + Ant Design Pro shell |
| Icon library | `@ant-design/icons` |
| Font | existing admin system stack; no new type system in Phase 8 |

---

## Spacing Scale

Declared values (must be multiples of 4):

| Token | Value | Usage |
|-------|-------|-------|
| xs | 4px | Status dots, inline badge spacing |
| sm | 8px | Inline actions, locale chips, compact form padding |
| md | 16px | Default form control spacing |
| lg | 24px | Card and panel padding |
| xl | 32px | Section gaps between field groups |
| 2xl | 48px | Page-level module separation |
| 3xl | 64px | Reserved for large empty states only |

Exceptions: none

---

## Typography

| Role | Size | Weight | Line Height |
|------|------|--------|-------------|
| Body | 14px | 400 | 1.6 |
| Label | 13px | 500 | 1.5 |
| Heading | 20px | 600 | 1.4 |
| Display | 28px | 700 | 1.25 |

---

## Color

| Role | Value | Usage |
|------|-------|-------|
| Dominant (60%) | `#f5f7fb` | Page background, grouped form canvas |
| Secondary (30%) | `#ffffff` | Cards, locale panels, settings drawers |
| Accent (10%) | `#7c5cff` | Primary save, translate action, active locale indicator |
| Destructive | `#d14343` | Delete, clear translation, overwrite confirmation |

Accent reserved for:
- primary submit buttons
- translate actions
- active locale tab and completeness status
- never for every border or every secondary action

---

## Copywriting Contract

| Element | Copy |
|---------|------|
| Primary CTA | `儲存多語內容` |
| Empty state heading | `尚未建立多語內容` |
| Empty state body | `先輸入主欄位語言，之後可一鍵補齊其他語言版本。` |
| Error state | `翻譯未完成，可先手動編輯或稍後重試。` |
| Destructive confirmation | `覆蓋翻譯`: `這會覆蓋目標語言現有內容，是否繼續？` |

---

## Registry Safety

| Registry | Blocks Used | Safety Gate |
|----------|-------------|-------------|
| Ant Design official | Form, Tabs, Segmented, Collapse, Alert, Tag, Tooltip, Drawer, Result, Modal | not required |
| Existing project shell | PageContainer, Pro layout wrappers | not required |

---

## Core Interaction Contract

### Page-level layout

- Phase 8 pages keep the current admin shell and route structure from Phase 7.
- Multilingual authoring lives inside white cards/panels within the existing page container.
- System translation settings and domain authoring forms must look like part of the same control plane, not separate tools.

### Multilingual field group contract

Each localized field block must render in this order:
1. Field label and required marker
2. Primary authoring locale banner
3. Quick actions row
4. Four locale editors
5. Inline validation and translation status

Required behaviors:
- primary locale editor is always expanded and visually pinned first
- remaining locales may use tabs or collapsible sections, but completeness must be visible without opening every panel
- a locale with human-edited content gets a `手動` state
- a locale filled by machine translation gets a `機器翻譯` state with engine info
- an empty locale gets an `未填寫` state
- translation failure is shown inline per locale; it must not block the form submit button

### Quick actions row

The shared action row may contain only these primary interactions in Phase 8:
- `以主欄位翻譯未填語言`
- `全部重新翻譯`
- `複製主欄位到其他語言`
- `清除機器翻譯標記`

Rules:
- actions must be explicit; no automatic translation on blur or save
- destructive overwrite requires confirmation
- while a translate action is running, the row shows per-locale progress, not a global page spinner

### Translation settings panel contract

System settings must expose:
- primary authoring language selector
- translation engine priority list
- default overwrite behavior
- request timeout/fallback status display

Interaction rules:
- settings save independently from content forms
- form pages read the saved defaults on open
- authors can still choose a different source locale for the current action without changing the global default

### Preview contract

Admin preview surfaces must provide:
- locale switcher with `繁中 / 简中 / English / Português`
- visible indication when the displayed value came from fallback rather than a filled locale slot
- no mixed-locale panel without a visible reason

### Validation contract

Validation must be localized in Traditional Chinese and must distinguish:
- `必填但未填寫`
- `尚未有翻譯，可稍後補齊`
- `翻譯服務暫時不可用`

Only the primary authoring locale is hard-required for save in Phase 8. The other three locales are required for milestone completion workflows, but their absence must not hard-fail ordinary content draft saves.

---

## Phase 8 Screen Contracts

### Domain CRUD forms

Applies to:
- cities
- POIs
- storylines
- chapters
- rewards
- stamps
- tip articles
- notifications
- runtime-setting copy editors

Rules:
- replace raw `ZH / EN / ZHT` field repetition with a shared multilingual field block
- Portuguese must be first-class, never hidden in an advanced drawer
- related metadata fields that are not display copy stay outside the multilingual block

### System translation settings

Rules:
- lives under `system/configs`
- uses grouped settings cards, not raw key-value rows
- includes engine ordering and fallback explanation in plain Traditional Chinese
- exposes bridge/service health at summary level only; raw stack traces stay in logs, not in the form

### Mobile/runtime alignment hints

Rules:
- public/admin preview locale labels must match mini-program locale labels exactly
- use `pt` for canonical code and `Português` for admin-facing display
- do not use mixed abbreviations like `ZHT`

---

## Checker Sign-Off

- [x] Dimension 1 Copywriting: PASS
- [x] Dimension 2 Visuals: PASS
- [x] Dimension 3 Color: PASS
- [x] Dimension 4 Typography: PASS
- [x] Dimension 5 Spacing: PASS
- [x] Dimension 6 Registry Safety: PASS

**Approval:** approved 2026-04-13
