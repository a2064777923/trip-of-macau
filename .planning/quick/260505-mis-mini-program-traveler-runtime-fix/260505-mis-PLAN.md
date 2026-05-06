# Quick Task 260505-mis: Mini-program Traveler Runtime Fix

## Goal
修復小程序故事與地圖頁暴露管理者/配置型信息、地圖頁錯誤展示大地圖切換提示、媒體不可用時反覆上報造成狀態/緩存膨脹的問題。

## Tasks
1. 地圖頁只保留首頁大地圖切換入口，不在地圖頁展示大地圖切換/提示條；故事路線文字改為遊客語境。
2. 故事頁清理同步、錨點、配置等管理者語境，長標題做 clamp，互動流程以遊玩行動卡展示。
3. 媒體與 Lottie 降級策略改為遊客友好顯示，避免缺失媒體在只讀瀏覽下反覆上報。

## Verification
- `npm run build:weapp`
- public backend `/api/v1/health` 和 `/api/v1/story-lines?locale=zh-Hant`
- WeChat MCP 若仍被 3799 端口阻塞，記錄阻塞原因與修復命令。
