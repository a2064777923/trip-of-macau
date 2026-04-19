---
phase: 16-indoor-rule-workbench-and-governance
plan: 04
completed: 2026-04-16
---

# Phase 16.04 Summary

Wave 4 補齊了本地驗證與交付工件。

- 新增 `scripts/local/smoke-phase-16-indoor-rule-governance.ps1`，可直接對 `http://127.0.0.1:8081` 的 admin backend 執行登入、治理總覽、衝突、詳情、狀態切換，以及作者頁回寫後再讀治理結果的 smoke 驗證。
- `packages/admin/aoxiaoyou-admin-ui/package.json` 新增 `smoke:phase16:indoor-rules` 指令。
- 新增 `16-UAT.md`，覆蓋工作台命名、排序、取消/套用、縮略圖取點、治理中心篩選、衝突檢視、狀態切換與深鏈回作者頁等人工驗收流程。
- 本地 `8081` 已確認為本專案 `aoxiaoyou-admin-backend` 的 Java 進程，`/swagger-ui.html` 可正常回應。

## Verification

- `npm run build` 通過於 `packages/admin/aoxiaoyou-admin-ui`
- `mvn test -q` 通過於 `packages/admin/aoxiaoyou-admin-backend`
- `http://127.0.0.1:8081/swagger-ui.html` 回應 `200`
