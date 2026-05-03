# Phase 40 WeChat UAT Checklist

## Preconditions

- 使用微信開發者工具或已安裝測試版小程序的真機。
- 本地 public backend、admin backend、MySQL、Redis 可用，且故事線 public runtime 可讀取 `東西方文明的戰火與共生`。
- 若要測試登入後事件上報，使用本地/開發環境允許的測試身份；不要在證據欄貼出憑證、完整請求、完整回應或本機絕對路徑。
- 先閱讀 `40-SMOKE-REPORT.md`，確認自動化 smoke 未出現阻擋項，再進行人工驗收。

## Automated Evidence To Review First

| Evidence | Scope | Current Result |
| --- | --- | --- |
| `40-SMOKE-REPORT.md` | Phase 36-39 smoke bundle、AI 監控 API shape、quick release readiness | PASS_WITH_SKIPS_ALLOWED |
| `36-VERIFICATION.md` | 生成圖片、切圖、音頻、視頻、COS 發佈、素材包版本 | PASS |
| `38-VERIFICATION.md` | public runtime asset DTO、fallback、事件、探索度接口 | PASS |
| `39-VERIFICATION.md` | 小程序故事頁 build、runtime smoke、路線/事件 baseline | PASS |

## WeChat DevTools / Device Checklist

| ID | Step | Expected Result | Evidence | Result | Notes |
| --- | --- | --- | --- | --- | --- |
| UAT-40-01 | 在微信開發者工具或真機打開小程序，進入故事頁並選擇 `東西方文明的戰火與共生`。 | 故事封面、標題、簡介、章節入口可見；若媒體不可用，顯示繁體中文 fallback。 | 待填：截圖或錄屏編號。 | Pending | open flagship story |
| UAT-40-02 | 點擊開始故事模式。 | 建立臨時故事模式 session；頁面提示目前章節與目的地。 | 待填：session 狀態截圖。 | Pending | start story mode |
| UAT-40-03 | 切換到地圖頁查看故事模式路線。 | 當前章節路線高亮，非當前路線灰色或弱化，目的地資訊可讀。 | 待填：地圖截圖。 | Pending | route and current chapter display |
| UAT-40-04 | 回到故事頁，逐一展開五個章節。 | 五章都可查看名稱、綁定地點、內容積木、互動/任務/獎勵摘要。 | 待填：章節截圖。 | Pending | inspect all five chapters |
| UAT-40-05 | 查看任一圖片內容積木。 | 圖片正常渲染；若資源不可用，顯示 poster/fallback 或明確不可用狀態。 | 待填：圖片渲染證據。 | Pending | image block renders or fallback appears |
| UAT-40-06 | 查看任一 Lottie 內容積木。 | Lottie 正常播放；若網絡地址或 canvas 不可用，顯示 poster/fallback，不白屏。 | 待填：Lottie 或 fallback 證據。 | Pending | Lottie block renders or poster/fallback appears |
| UAT-40-07 | 試播章節旁白或音效。 | 音頻可播放；若不可播放，顯示繁體中文不可用狀態。 | 待填：播放截圖或錄屏。 | Pending | audio block plays or unavailable state appears |
| UAT-40-08 | 試播章節劇情視頻。 | 視頻可播放；若不可播放，顯示 poster/fallback 或明確不可用狀態。 | 待填：視頻截圖或錄屏。 | Pending | video block plays or unavailable state appears |
| UAT-40-09 | 點擊拾取物或互動 action card。 | 卡片可點擊並有狀態反饋；重複點擊不造成破壞性錯誤。 | 待填：點擊前後截圖。 | Pending | pickup/action card can be tapped |
| UAT-40-10 | 執行 baseline 任務操作。 | 可完成已支持的 baseline 任務；未支持玩法用繁體中文 placeholder 解釋限制。 | 待填：任務反饋證據。 | Pending | task baseline action can be completed or placeholder explains limitation |
| UAT-40-11 | 觸發獎勵或稱號反饋。 | 顯示獎勵、稱號、收集物或 fallback 說明；不暴露管理端資料。 | 待填：獎勵反饋截圖。 | Pending | reward/title feedback appears or fallback explains limitation |
| UAT-40-12 | 從章節目的地點擊前往地圖。 | 地圖 tab 打開並聚焦目前目的地，路線上下文未遺失。 | 待填：地圖 handoff 截圖。 | Pending | map handoff opens current destination |
| UAT-40-13 | 退出故事模式。 | 清除臨時故事模式路線/session 狀態；不清除永久拾取、獎勵、探索記錄。 | 待填：退出前後證據。 | Pending | exit story mode clears temporary session route state |
| UAT-40-14 | 重新開始故事模式。 | 重新建立臨時故事流程；已獲得的永久拾取物、獎勵、探索記錄仍保留。 | 待填：重新開始證據。 | Pending | restart story mode does not erase permanent pickups/rewards/exploration |
| UAT-40-15 | 遇到 AR、拍照識別、語音輸入、拼圖等未完成玩法。 | unsupported AR/photo/speech/puzzle gameplay 顯示清楚的繁體中文 placeholder，不阻斷故事主流程。 | 待填：placeholder 截圖。 | Pending | unsupported AR/photo/speech/puzzle gameplay shows Traditional Chinese placeholder |

## Flagship Story Journey

驗收重點是「故事介紹 -> 開始故事模式 -> 查看章節 -> 路線 handoff -> 媒體/互動 baseline -> 退出/重啟」是否順暢。五章包括媽閣廟、亞婆井前地、崗頂前地、大炮台、議事亭前地；本期不要求在真機完成所有複雜地理玩法。

## Media And Lottie Checks

圖片、音頻、視頻、Lottie 均可接受三種結果：正常播放、poster/fallback 顯示、繁體中文不可用狀態。不可接受白屏、跨格溢出、英文裸錯誤或管理端資料外洩。

## Interaction, Pickup, Task, And Reward Checks

本期驗收 baseline action card、事件上報、拾取/任務/獎勵反饋與 fallback。完整 AR/photo recognition、語音 NPC、拼圖、炮台佈防、路線覆蓋小遊戲為延期功能。

## Exit And Restart Checks

退出故事模式只清理臨時 session/路線上下文；永久探索事件、已拾取物、已獲得獎勵與稱號不得被清除。重啟故事模式後應能從 public runtime 重新取得路線與章節資料。

## Expected v3.1 Placeholder Behavior

當遇到 AR、拍照識別、語音輸入、拼圖、炮台佈防、室內視覺定位等複雜玩法時，v3.1 的預期行為是以繁體中文 placeholder 或降級卡片保留故事流程，不是完整玩法引擎。

## Result Summary

| Category | Total | Passed | Issues | Pending | Skipped | Blocked |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| WeChat DevTools / Device Checklist | 15 | 0 | 0 | 15 | 0 | 0 |
