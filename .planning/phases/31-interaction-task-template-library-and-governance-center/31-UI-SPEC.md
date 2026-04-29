---
phase: 31
slug: interaction-task-template-library-and-governance-center
status: ready
created: 2026-04-29
---

# Phase 31 UI Spec - 互動與任務模板庫、體驗規則治理中心

## Design Intent

Phase 31 的 UI 要從「資料表管理」提升為「編排與治理工作台」。操作員打開頁面後，應立即理解三件事：

- 有哪些可重用模板可直接套用。
- 某個模板或互動規則被哪些 POI、故事章節、室內點、獎勵規則使用。
- 系統目前偵測到哪些衝突、重複發放、高風險或缺配置問題。

## Visual Language

- 保持現有 admin 的淺色卡片系統與紫色主色，但避免大片單調表格。
- 用「左側分類 / 中間卡片或列表 / 右側詳情」的工作台節奏。
- Preset cards 使用明確圖標、類型標籤、風險標籤、用途摘要。
- Conflict severity 使用一致顏色：
  - `error`: red
  - `warning`: gold
  - `info`: blue
- Long codes use ellipsis plus tooltip/title, never overflow.

## Template Library Layout

- Header:
  - title: `互動與任務模板庫`
  - subtitle: `展示、出現條件、觸發條件、觸發效果、任務玩法與獎勵演出共用模板`
- Left:
  - grouped preset catalog by template type.
  - preset card actions: `套用為新模板`, `查看 JSON`.
- Main:
  - filter bar with labelled controls.
  - template table or cards with usage count and risk level.
- Drawer:
  - structured editor first.
  - advanced JSON folded under `進階 JSON`.
  - usage section accessible by `查看使用處`.

## Governance Center Layout

- Header:
  - title: `體驗規則治理中心`
  - subtitle: `跨 POI、故事、室內、獎勵與活動檢查模板使用、觸發鏈與衝突`
- Summary:
  - cards: 模板, 流程, 綁定, 覆寫, 衝突, 高風險.
- Filters:
  - every control has visible Traditional Chinese label.
  - boolean filters are switches, not unlabeled dropdowns.
- Results:
  - table/card list with source, owner, template/step, trigger/effect, risk, conflict count, status.
- Detail drawer:
  - usage refs.
  - conflicts.
  - raw summary collapsed.

## Motion and Feedback

- Loading state uses skeleton or spinner inside the active panel, not blank pages.
- Preset apply and conflict check buttons show loading and are disabled while pending.
- Successful clone/check actions show concise Traditional Chinese message.
- Form validation keeps the existing auto-scroll/focus/shake behavior.

## Responsive Behavior

- Desktop: 3-column workbench where useful.
- Medium width: filters wrap to two rows, details open in drawer.
- Narrow width: preset catalog becomes horizontal or single column; no table content should become vertical one-character text.

## Acceptance

- `/admin/#/content/experience/templates` is usable without raw JSON-first editing.
- `/admin/#/content/experience/governance` has labelled filters and non-empty conflict/detail areas with seeded data.
- Playwright screenshots show readable cards/tables at 1280px width.
