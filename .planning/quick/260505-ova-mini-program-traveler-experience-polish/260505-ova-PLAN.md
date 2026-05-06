# Quick Task 260505-ova: Mini-program Traveler Experience Polish

**Date:** 2026-05-05
**Scope:** 小程序用戶端故事線與地圖體驗展示修復。

## Goal

讓小程序首頁、故事頁與地圖頁不再暴露後台/運營配置語言，並把故事線流程以玩家可理解、可操作的方式呈現。優先處理目前 UAT 看到的問題：長標題溢出、媒體/動畫缺失時白屏或「素材準備中」體驗差、故事互動像配置清單、地圖頁誤露不該出現的切換/配置信息。

## Tasks

1. **Story page traveler presentation**
   - Files: `packages/client/src/pages/story/index.tsx`, `packages/client/src/pages/story/index.scss`
   - Action: 把章節互動流程改成「旅程行動卡」呈現，加入劇情/到達/拾取/任務/獎勵的玩家語言與降級內容，收斂診斷/配置口吻。
   - Verify: 長標題截斷或換行合理；章節展開後不出現「綁定、錨點、工作台、配置詳情」等管理語言。

2. **Map page traveler gameplay surface**
   - Files: `packages/client/src/pages/map/index.tsx`, `packages/client/src/pages/map/index.scss`
   - Action: 保留首頁大地圖切換約束，地圖頁只展示當前城市/子地圖；故事模式卡片改成玩家路線與下一步提示，探索點詳情改成 walk-in 體驗入口。
   - Verify: 地圖頁不提供跨大地圖切換，不顯示開發/運營資訊。

3. **Media and interaction fallback**
   - Files: `packages/client/src/components/StoryContentBlockRenderer/index.tsx`, `packages/client/src/components/LottieAssetPlayer/index.tsx`, related styles
   - Action: 媒體缺失、Lottie 不可播時用有設計感的故事佔位與操作提示，不讓用戶看到資源失效的技術語境。
   - Verify: `npm run build:weapp` passes; DevTools preview can run with WinNAT workaround.
