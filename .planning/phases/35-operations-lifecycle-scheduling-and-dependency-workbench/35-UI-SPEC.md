# Phase 35 UI Spec: Operations Lifecycle Scheduling Workbench

**Created:** 2026-04-30
**Status:** Ready for planning
**Surface:** Admin UI `/ops/lifecycle`

## Design Intent

The lifecycle workbench is an operations console for high-risk content visibility changes. It must help admins answer four questions before acting:

1. What content am I changing?
2. What is its current lifecycle state?
3. What will break, disappear, or change for travelers and progress calculations?
4. When and by whom was the action applied or scheduled?

The UI must be practical and visually structured. Avoid a generic CRUD table or a raw JSON admin panel.

## Route And Sidebar

- Route: `/ops/lifecycle`
- Sidebar group: `測試與營運管理`
- Sidebar label: `生命週期與發布排程`

## Page Structure

### Header

Required copy:

- Title: `生命週期與發布排程`
- Subtitle: `跨地圖、故事、室內、獎勵與體驗流程檢查依賴後再發布、下線或移除。`

Header actions:

- `重新載入`
- `查看排程`
- `執行到期排程`

### Summary Cards

Show four to six cards:

- `待執行排程`
- `今日已套用`
- `高風險操作`
- `失敗操作`
- `已發布內容`
- `未發布內容`

### Target Filters

Every filter must have a visible label above or beside the control:

- `關鍵字`
- `主體類型`
- `目前狀態`
- `城市`
- `子地圖`
- `故事線`
- `只看已發布`
- `只看有依賴`

The target list should show:

- target name
- target type label
- target code/id
- current status label
- public visibility marker
- dependency count if known
- last updated time

### Preview Drawer

Opened by `預覽影響`.

Required sections:

- `操作摘要`
- `狀態轉換`
- `依賴影響`
- `小程序公開內容影響`
- `探索度與用戶進度影響`
- `下游子內容`
- `被其他內容綁定`
- `風險與阻擋`

Impact cards must use severity states:

- `blocking` -> `阻擋`
- `warning` -> `警告`
- `info` -> `提示`

The preview drawer must expose enough detail for confidence without overwhelming the operator:

- grouped impact counts
- top representative rows
- expandable metadata or detail area
- no raw JSON as the default view

### Action Panel

Required fields:

- `操作`: `發布`, `下線`, `移除`
- `執行方式`: `立即執行`, `排程執行`
- `排程時間`
- `操作原因`
- `確認已閱讀影響`

Rules:

- `排程時間` is required only for scheduled execution.
- `操作原因` is required for `下線` and `移除`.
- Apply/schedule buttons remain disabled until preview is loaded and confirmation is checked.
- If backend returns blocking impacts, destructive action should be disabled unless the backend explicitly allows force mode. Phase 35 does not require force mode.

Required buttons:

- `預覽影響`
- `立即套用`
- `加入排程`
- `取消排程`

### History View

Required columns:

- `時間`
- `主體`
- `操作`
- `狀態變更`
- `排程狀態`
- `操作人`
- `影響數`
- `結果`
- `操作`

Detail drawer:

- operation request
- impact groups
- audit/result message
- error message if failed
- target links where possible

## Copy Requirements

All visible copy must be Traditional Chinese.

Required strings:

- `生命週期與發布排程`
- `預覽影響`
- `依賴影響`
- `小程序公開內容影響`
- `探索度與用戶進度影響`
- `被其他內容綁定`
- `下游子內容`
- `立即套用`
- `加入排程`
- `執行到期排程`
- `確認已閱讀影響`
- `操作原因`
- `阻擋`
- `警告`
- `提示`

## Visual And Interaction Requirements

- Use cards and grouped panels, not a single full-width dense form.
- Target and history tables should remain readable at desktop width and degrade into horizontal scroll or card layout on narrower screens.
- Long codes, relation keys, COS keys, paths, and object identifiers must use ellipsis plus tooltip/title.
- Preview loading must show a clear loading state so operators do not repeatedly click.
- After validation failure, the page should scroll to and highlight the problematic field where feasible, following existing form UX expectations.
- Destructive copy should be clear and calm. Avoid sensational warning language, but make consequences explicit.

## Non-goals

- No full approval workflow UI.
- No role-permission matrix redesign.
- No public mini-program lifecycle UI.
- No raw JSON-first lifecycle editor.

