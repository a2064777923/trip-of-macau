---
phase: 16-indoor-rule-workbench-and-governance
plan: 03
completed: 2026-04-16
---

# Phase 16.03 Summary

Wave 3 建立了室內互動規則治理中心。

- 新增 `IndoorRuleCenter.tsx` 獨立路由頁，提供跨建築、跨樓層的規則治理視角。
- 新增 `IndoorRuleConflictPanel.tsx` 詳情抽屜，用於查看規則數量、衝突與提醒、父標記與樓層資訊。
- 治理中心支援按關鍵字、建築、樓層、POI、綁定類型、狀態與支援層級進行過濾。
- 治理中心可直接切換規則狀態，並深鏈回對應的室內作者頁上下文。
- Phase 16 所屬治理頁與工作台新增文案已統一為繁體中文。

## Verification

- `npm run build` 通過於 `packages/admin/aoxiaoyou-admin-ui`
