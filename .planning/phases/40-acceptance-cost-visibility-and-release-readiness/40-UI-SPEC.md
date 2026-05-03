# Phase 40 UI Spec: Acceptance, Cost Visibility, and Release Readiness

**Created:** 2026-05-03
**Status:** Ready for planning
**Surfaces:** Admin AI capability center, material package QA/history views, release/UAT planning artifacts

## Design Intent

Phase 40 UI work is a closure polish pass, not a new product module. The operator should be able to answer:

1. Which AI/material generation jobs ran recently?
2. Who triggered them, with which provider/model/capability?
3. Did they succeed, fail, retry, or produce assets?
4. What did they cost or estimate, without exposing secrets?
5. What evidence is still automated, manual, skipped, or deferred before v3.1 closure?

## Admin Placement

Use the existing AI capability center rather than creating a duplicate top-level module.

Primary route:

- `/ai/observability`
- Sidebar label: `監控與成本`

Related surfaces may deep-link to it:

- `故事素材包管理`
- `創作工作台`
- `音色與聲音工坊`

The actual admin UI path is `packages/admin/aoxiaoyou-admin-ui/src/pages/AiCapabilityCenter/*`; older references to `src/pages/AI/AIManagement.tsx` are stale and must not be used.

## Observability Page Requirements

### Header

Required copy:

- Title: `監控與成本`
- Subtitle: `查看生成請求、模型用量、素材產出、錯誤與成本估算，協助完成 v3.1 發布驗收。`

Header actions:

- `重新整理`
- `匯出安全摘要`
- `查看驗收清單`

### Summary Cards

Show at least five cards:

- `24h 估算成本`
- `24h 請求數`
- `24h 失敗`
- `待處理生成作業`
- `已產出素材`
- `最近一次錯誤`

Cost values must be labeled:

- `估算`
- `實際`
- `供應商未返回`

### Filters

Every filter must have a visible label. Required filters:

- `能力`
- `供應商`
- `模型`
- `狀態`
- `作業類型`
- `操作人`
- `時間範圍`
- `只看失敗`
- `只看有素材`

### Recent Jobs Table

Required columns:

- `時間`
- `操作人`
- `能力`
- `作業類型`
- `供應商`
- `模型`
- `狀態`
- `素材包 / 目標`
- `輸出素材`
- `耗時`
- `成本`
- `操作`

Long values such as asset URL, object key, provider request id, material item key, and error text must use ellipsis plus tooltip or drawer detail. They must not overflow into adjacent columns.

### Request Log Table

Required columns:

- `時間`
- `操作人`
- `能力`
- `供應商`
- `模型`
- `成功`
- `耗時`
- `成本`
- `摘要`
- `操作`

Rows must allow opening a detail drawer. The drawer may show sanitized JSON in an advanced collapsible section, but the default view must be readable cards and description rows.

### Detail Drawer

Required sections:

- `基本資訊`
- `供應商與模型`
- `輸入摘要`
- `輸出摘要`
- `成本與用量`
- `素材與版本`
- `錯誤與重試`
- `安全檢查`

Do not render provider API keys, encrypted credentials, bearer tokens, COS secrets, full prompt text, full script text, local file paths, or admin-only provenance in default detail sections.

If full provenance already exists behind an authorized detail API, place it under `進階診斷` and clearly label it as restricted. Phase 40 should prefer prompt/script summaries and asset links.

## UAT And Release Artifacts

Planning artifacts should be usable by an operator, not only by engineers.

Required document sections:

- `前置條件`
- `自動化驗證`
- `微信開發者工具 / 真機驗收`
- `故事線旅程驗收`
- `媒體與 Lottie 驗收`
- `互動、拾取與獎勵驗收`
- `退出與重啟驗收`
- `已接受限制`
- `延期功能`
- `發布阻擋項`

Each checklist item must have:

- `ID`
- `步驟`
- `預期結果`
- `證據`
- `結果`
- `備註`

## Visual And Interaction Requirements

- Use grouped cards, tabs, tables, and drawers; do not show a single dense raw log table as the whole page.
- Keep visible copy in Traditional Chinese.
- Loading states must disable repeat submit/refresh buttons where the action is not idempotent.
- Tables must support horizontal scroll where needed and never let long URLs or object keys overflow outside their cell.
- Empty states must explain whether data is unavailable because of missing env/config, no jobs, or failed loading.
- Error states must show the provider/model/job id if available, without exposing secrets.
- Release readiness documents should use concise evidence tables instead of raw command logs.

## Required Copy

- `監控與成本`
- `生成作業`
- `請求日誌`
- `成本與用量`
- `估算`
- `實際`
- `供應商未返回`
- `查看詳情`
- `安全摘要`
- `輸入摘要`
- `輸出摘要`
- `素材與版本`
- `錯誤與重試`
- `進階診斷`
- `自動化驗證`
- `真機驗收`
- `已接受限制`
- `延期功能`

## Non-goals

- No new AI provider onboarding UI.
- No new material production workbench.
- No public mini-program UI redesign.
- No full approval workflow.
- No raw JSON-first observability page.
