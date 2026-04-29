# Admin Control-Plane IA Audit

**日期：** 2026-04-29
**範圍：** 後台管理系統側欄、路由入口、死碼頁面與本地驗證產物清理。

## 保留的主分欄

- `首頁儀表盤`：保留作為系統狀態、服務健康與近期操作總覽。
- `地圖與空間管理`：保留城市與子地圖、POI、POI 地點體驗、室內建築與小地圖，以及室內互動治理中心。
- `AI 能力中心`：保留為獨立大分欄，內含供應商、模型、聲音、能力配置、創作、監控與治理設定。
- `故事與內容管理`：保留故事線、故事模式、章節、內容積木、體驗流程、模板、治理、探索元素、任務活動與媒體資源。
- `收集物與獎勵`：保留兌換獎勵物品、遊戲內獎勵、榮譽稱號、獎勵規則與收集物。
- `用戶與進度管理`：收斂為 `用戶與進度工作台`，由列表與詳情承接進度、軌跡、故事 session、事件與修復操作。
- `測試與營運管理`：保留測試控制台、營運活動、測試資源與沙盒。
- `系統與權限管理`：保留管理員、角色、系統設定、審計與日誌。

## 已清理的冗餘入口

- 移除側欄中重複的 `用戶進度與軌跡` placeholder。舊路由 `/users/story-progress` 仍保留兼容 redirect 到 `/users/progress`。
- `地圖與空間管理` 下的 `互動規則治理中心` 改名為 `室內互動治理中心`，避免與故事/POI/獎勵共用的 `體驗規則治理中心` 混淆。
- 刪除舊版未路由頁面：
  - `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/AiCapabilityCenter.tsx`
  - `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/IndoorSpaceManagement.tsx`
  - `packages/admin/aoxiaoyou-admin-ui/src/pages/AiCapabilityCenter/TravelerServicesPage.tsx`
  - `packages/admin/aoxiaoyou-admin-ui/src/pages/StoryChapterManagement/index.tsx`

## 保留但仍屬後續完善的入口

- `瓦片地圖`：保留為大地圖瓦片覆蓋圖層入口，明確與 `室內建築與小地圖` 分開。
- `任務與活動`：保留為後續營運活動與任務主資料入口。
- `測試資源與沙盒`：保留為測試資料、沙盒快照與批量工具入口。
- `審計與日誌`：保留為管理操作、內容版本與登入安全事件入口。

這些入口目前不應錯誤跳轉到其他控制台，也不應混用不相關頁面；未完成時必須維持繁體中文專用 placeholder。

## 本次清理的本地產物類型

- Playwright 生成的臨時快照、截圖與 console log。
- `output/` 下的本地驗證截圖與後端輸出 log。
- 臨時登入 JSON、COS probe 輸出與一次性舊指南。
- 重複的未追蹤修復摘要文檔；canonical 狀態改以 README、`.planning` 與本文件為準。

## 後續可優化候選

- `故事與內容管理` 的體驗流程、模板、治理與探索元素可以再做視覺分組，降低側欄密度。
- `測試與營運管理` 仍需要 Phase 34 後的真實營運儀表盤與批量工具承接，避免長期 placeholder。
- `系統與權限管理` 的審計中心應接入真實操作日誌與內容版本差異。
