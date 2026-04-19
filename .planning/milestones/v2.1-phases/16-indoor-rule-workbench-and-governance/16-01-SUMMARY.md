---
phase: 16-indoor-rule-workbench-and-governance
plan: 01
completed: 2026-04-16
---

# Phase 16.01 Summary

Wave 1 建立了室內互動規則治理的後端基礎。

- 新增 `IndoorRuleGovernanceService`，從 Phase 15 的室內節點與行為資料投影出治理中心所需的總覽、詳情與狀態更新結果。
- `AdminIndoorController` 新增治理接口：
  - `GET /api/admin/v1/map/indoor/rules/overview`
  - `GET /api/admin/v1/map/indoor/rules/conflicts`
  - `GET /api/admin/v1/map/indoor/rules/behaviors/{behaviorId}`
  - `PATCH /api/admin/v1/map/indoor/rules/behaviors/{behaviorId}/status`
- 後端衝突分類已落地為四個明確代碼：
  - `MISSING_PREREQUISITE`
  - `SCHEDULE_OVERLAP`
  - `ENTITY_COLLISION`
  - `STATUS_MISMATCH`
- 狀態切換會回傳 `parentNodeStatus` 與 `warnings`，避免靜默啟用不合規規則。

## Verification

- `mvn test -q` 通過於 `packages/admin/aoxiaoyou-admin-backend`
