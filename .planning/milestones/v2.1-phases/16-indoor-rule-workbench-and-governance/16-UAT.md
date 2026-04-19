---
status: complete
phase: 16-indoor-rule-workbench-and-governance
source:
  - 16-01-SUMMARY.md
  - 16-02-SUMMARY.md
  - 16-03-SUMMARY.md
  - 16-04-SUMMARY.md
started: 2026-04-16T22:00:00+08:00
updated: 2026-04-17T00:28:30+08:00
---

## Current Test

[testing complete]

## Tests

### 1. 室內作者頁打開互動規則工作台
expected: 在「室內建築與小地圖 > 圖資、標記與 CSV」編輯任一標記時，應先看到互動規則摘要卡片，點擊「編輯互動規則」後，右側要打開全屏工作台，並自動帶入目前標記已有的互動行為資料。
result: pass

### 2. 互動行為命名、排序與套用
expected: 在工作台內新增或重命名互動行為、調整順序後按「校驗成功後套用」，主表單中的互動規則摘要應立即同步；重新打開工作台時，名稱與順序應保持一致。
result: pass

### 3. 工作台取消不污染主表單
expected: 在工作台中修改任一互動行為後按「取消」，回到主表單時不應帶入未套用的變更；重新打開工作台時，仍應看到上次正式套用前的資料。
result: pass

### 4. 當前行為專屬的路徑與疊加物取點
expected: 在工作台內對某個互動行為新增路徑點或疊加物點後，切換到另一個互動行為時，不應再顯示上一個行為的路徑或疊加物；切回原行為時才恢復顯示。
result: pass

### 5. 路徑與疊加物可以清空後重選
expected: 在工作台中已取點的路徑與疊加物，都應可以透過「清空路徑點」或「清空疊加物」刪除；清空後再次取點，應按新選擇重新建立，不殘留舊幾何。
result: pass

### 6. 治理中心承接建築與樓層上下文
expected: 從作者頁點「打開治理中心」後，治理中心應自動帶入當前建築與樓層上下文；列表應只顯示相符範圍內的互動規則資料，而不是跳到空白或錯誤頁面。
result: pass

### 7. 治理中心詳情與狀態切換
expected: 在治理中心打開任一規則詳情時，應能看到規則數量、衝突/提醒與父標記/樓層資訊；切換規則狀態後，列表與詳情要同步更新。
result: pass

## Summary

total: 7
passed: 7
issues: 0
pending: 0
skipped: 0
blocked: 0

## Gaps

[]
