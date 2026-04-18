---
status: complete
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
  root_cause: ""
  artifacts: []
  missing: []
  debug_session: ""
- truth: "Open the split admin pages for game rewards, honors/titles, and reward rule center. Each route should land on its own owned page, preserve the new split IA, and show operator-friendly reward authoring/governance instead of the previous overloaded single reward drawer."
  status: failed
  reason: "User reported: 榮譽稱號那里也很多亂碼，而且要多加點某地圖逹到一定探索度就會獲得的特別地區稱號"
  severity: major
  test: 3
  root_cause: ""
  artifacts: []
  missing: []
  debug_session: ""
- truth: "Query the live public runtime reward endpoints. `GET /api/v1/redeemable-prizes`, `GET /api/v1/game-rewards`, and `GET /api/v1/reward-presentations/{id}` should all return real Phase 23 data from the split reward tables rather than empty payloads, placeholders, or legacy-only structures."
  status: failed
  reason: "User reported: 也有亂碼"
  severity: major
  test: 4
  root_cause: ""
  artifacts: []
  missing: []
  debug_session: ""
