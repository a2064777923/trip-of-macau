---
status: diagnosed
phase: 23-reward-domain-split-and-acquisition-presentation-system
source:
  - 23-01-SUMMARY.md
  - 23-02-SUMMARY.md
  - 23-03-SUMMARY.md
started: 2026-04-18T18:56:11.3851075+08:00
updated: 2026-04-18T21:33:10.0332697+08:00
---

## Current Test

[testing complete]

## Tests

### 1. Cold Start Smoke Test
expected: Kill any running local services for the reward domain, then start the current live stack again. The admin backend on `8081` and the public backend on `8080` should boot without schema or seed errors, and the Phase 23 reward smoke should still pass against live data.
result: pass

### 2. 兌換獎勵物品管理資訊架構
expected: Open `/admin/#/collection/redeemable-prizes` on the live admin stack. The page should be a dedicated Traditional Chinese reward workspace for redeemable prizes, with the split reward-domain layout in place instead of an old placeholder, wrong redirect, or reused console.
result: issue
reported: "很多字段都又變成ASCII那種亂碼了哦"
severity: major

### 3. 遊戲內獎勵與榮譽頁面拆分
expected: Open the split admin pages for game rewards, honors/titles, and reward rule center. Each route should land on its own owned page, preserve the new split IA, and show operator-friendly reward authoring/governance instead of the previous overloaded single reward drawer.
result: issue
reported: "榮譽稱號那里也很多亂碼，而且要多加點某地圖逹到一定探索度就會獲得的特別地區稱號"
severity: major

### 4. 公開獎勵接口可讀
expected: Query the live public runtime reward endpoints. `GET /api/v1/redeemable-prizes`, `GET /api/v1/game-rewards`, and `GET /api/v1/reward-presentations/{id}` should all return real Phase 23 data from the split reward tables rather than empty payloads, placeholders, or legacy-only structures.
result: issue
reported: "也有亂碼"
severity: major

### 5. 室內互動與獎勵聯動回顯
expected: In the live admin reward governance view, indoor interaction rules should be able to surface linked reward rules and linked rewards, so operators can inspect the connection between indoor behavior triggers and the new reward domain without runtime crashes.
result: pass

## Summary

total: 5
passed: 2
issues: 3
pending: 0
skipped: 0
blocked: 0

## Gaps

- truth: "Open `/admin/#/collection/redeemable-prizes` on the live admin stack. The page should be a dedicated Traditional Chinese reward workspace for redeemable prizes, with the split reward-domain layout in place instead of an old placeholder, wrong redirect, or reused console."
  status: failed
  reason: "User reported: 很多字段都又變成ASCII那種亂碼了哦"
  severity: major
  test: 2
  root_cause: "Phase 23 reward seed SQL already contains mojibake in reward names, summaries, descriptions, and presentation copy, and both admin/public projections read those localized fields directly from the database."
  artifacts:
    - path: "scripts/local/mysql/init/24-phase-23-reward-seed.sql"
      issue: "Seed literals for reward presentations, rules, prizes, and rewards are already corrupted."
    - path: "packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicCatalogServiceImpl.java"
      issue: "Public reward payloads project localized DB fields directly via resolveText(...)."
    - path: "packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminRewardDomainServiceImpl.java"
      issue: "Admin reward-domain responses and validation messages use the same corrupted text source."
  missing:
    - "Repair Phase 23 reward seed literals to valid UTF-8 Chinese/Traditional Chinese text."
    - "Replay repaired reward seed into local MySQL."
    - "Add anti-mojibake smoke or integration assertions for reward-domain payloads."
  debug_session: ".planning/phases/23-reward-domain-split-and-acquisition-presentation-system/23-DIAGNOSIS.md"
- truth: "Open the split admin pages for game rewards, honors/titles, and reward rule center. Each route should land on its own owned page, preserve the new split IA, and show operator-friendly reward authoring/governance instead of the previous overloaded single reward drawer."
  status: failed
  reason: "User reported: 榮譽稱號那里也很多亂碼，而且要多加點某地圖逹到一定探索度就會獲得的特別地區稱號"
  severity: major
  test: 3
  root_cause: "The honors page header copy is corrupted in source, and the Phase 23 fixture set only seeds one title reward plus one badge, so the page lacks the requested regional exploration-title examples."
  artifacts:
    - path: "packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/HonorManagement.tsx"
      issue: "Honor page title and subtitle literals are mojibake in source."
    - path: "scripts/local/mysql/init/24-phase-23-reward-seed.sql"
      issue: "Only one title reward fixture exists, which is not enough to demonstrate exploration-threshold titles by map or sub-map."
  missing:
    - "Restore HonorManagement page copy to Traditional Chinese."
    - "Add multiple city/sub-map exploration title fixtures."
    - "Bind at least one title rule to map exploration thresholds so the honors page has meaningful showcase data."
  debug_session: ".planning/phases/23-reward-domain-split-and-acquisition-presentation-system/23-DIAGNOSIS.md"
- truth: "Query the live public runtime reward endpoints. `GET /api/v1/redeemable-prizes`, `GET /api/v1/game-rewards`, and `GET /api/v1/reward-presentations/{id}` should all return real Phase 23 data from the split reward tables rather than empty payloads, placeholders, or legacy-only structures."
  status: failed
  reason: "User reported: 也有亂碼"
  severity: major
  test: 4
  root_cause: "The public reward endpoints are not corrupting text at serialization time; they are returning already-corrupted localized reward records from the Phase 23 seed and live database."
  artifacts:
    - path: "packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicCatalogServiceImpl.java"
      issue: "Redeemable prizes, game rewards, and reward presentations all return localized DB fields directly."
    - path: "scripts/local/mysql/init/24-phase-23-reward-seed.sql"
      issue: "The seeded reward-domain data contains mojibake in zh/zh-Hant-facing fields."
  missing:
    - "Reseed the repaired reward-domain fixtures into local MySQL."
    - "Rerun live `8080` reward endpoint smoke after reseed."
    - "Add a payload text-health assertion so public reward APIs cannot silently ship mojibake again."
  debug_session: ".planning/phases/23-reward-domain-split-and-acquisition-presentation-system/23-DIAGNOSIS.md"
