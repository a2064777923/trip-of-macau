---
status: testing
phase: 11-story-activity-and-collection-composition
source:
  - 11-01-SUMMARY.md
  - 11-02-SUMMARY.md
  - 11-03-SUMMARY.md
started: 2026-04-15T02:56:54.5315981Z
updated: 2026-04-15T03:05:00.0000000Z
---

## Current Test
<!-- OVERWRITE each test - shows where we are -->

number: 1
name: 冷啟動冒煙驗證
expected: |
  停掉目前執行中的相關服務後，從乾淨狀態重新啟動系統。
  管理後台應可在 `http://127.0.0.1:8081` 正常提供 API，既有種子資料可被讀取，
  且故事線 `macau_fire_route`、章節 `311001`、活動 `macau_fortress_night_walk`、
  獎勵 `reward_historic_archive` 相關讀寫或查詢流程不會因重新啟動而失效。
awaiting: user response

## Tests

### 1. 冷啟動冒煙驗證
expected: 從乾淨狀態重新啟動後，8081 管理後端與 Phase 11 既有種子資料可正常工作。
result: pending

### 2. 故事線與章節編排入口
expected: 在管理後台中，故事線可編輯多個城市與子地圖綁定、附件排序，且章節編排有獨立頁面，不會再錯誤跳回故事線列表。
result: pending

### 3. 章節結點與條件配置
expected: 章節可編輯 anchor 類型與目標、前置條件、完成條件、完成獎勵等結構化欄位，保存後再次打開仍能正確回顯。
result: pending

### 4. 任務與活動編排
expected: 任務與活動頁可用繁體中文管理活動或全局任務，編輯主辦方、報名與上下線時間、置頂與多語內容後，公開活動列表與發現頁可讀到對應已發佈資料。
result: pending

### 5. 收集物與獎勵關聯配置
expected: 獎勵管理可設定故事線、城市、子地圖與資源關聯；保存後，公開獎勵或收集物讀取結果會帶出這些關聯與封面資源。
result: issue
reported: "收集物與奬勵那裏的新增表單功能和內容不夠完善，也沒有幾個示例的內容，收集物和奬勵物也是要綁定地圖或子地圖的，也可以綁定室內地圖。設定的觸發方式也要足夠豐富和易於配置。這部分是未做好還是不在當前milestone?"
severity: major

## Summary

total: 5
passed: 0
issues: 1
pending: 4
skipped: 0
blocked: 0

## Gaps

- truth: "獎勵與收集物的新增/編輯流程應可支援更完整的示例內容、地圖/子地圖/室內地圖綁定，以及足夠豐富且易配置的觸發方式。"
  status: failed
  reason: "User reported: 收集物與奬勵那裏的新增表單功能和內容不夠完善，也沒有幾個示例的內容，收集物和奬勵物也是要綁定地圖或子地圖的，也可以綁定室內地圖。設定的觸發方式也要足夠豐富和易於配置。這部分是未做好還是不在當前milestone?"
  severity: major
  test: 5
  root_cause: ""
  artifacts: []
  missing: []
  debug_session: ""
