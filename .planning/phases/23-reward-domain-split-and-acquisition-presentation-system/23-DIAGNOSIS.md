---
phase: 23-reward-domain-split-and-acquisition-presentation-system
status: diagnosed
updated: 2026-04-18T21:33:10.0332697+08:00
---

# Phase 23 Diagnosis

## Issue 1

- UAT test: 2
- Symptom: `兌換獎勵物品管理` 中多個欄位顯示 ASCII/亂碼。
- Root cause:
  - [24-phase-23-reward-seed.sql](/D:/Archive/trip-of-macau/scripts/local/mysql/init/24-phase-23-reward-seed.sql) 直接寫入了大量已經被污染的獎勵名稱、摘要、描述、highlight、presentation step 標題等字串。
  - 管理後台與公開接口都直接讀取這批資料；投影層沒有重新轉碼，實際上是「錯資料被正常投影」。
- Evidence:
  - [24-phase-23-reward-seed.sql](/D:/Archive/trip-of-macau/scripts/local/mysql/init/24-phase-23-reward-seed.sql) 中 `reward_presentations`、`reward_rules`、`redeemable_prizes`、`game_rewards` 的多處中文 literal 已是 mojibake。
  - [PublicCatalogServiceImpl.java](/D:/Archive/trip-of-macau/packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicCatalogServiceImpl.java) 直接以 `resolveText(...)` 投影 `nameZh/nameZht/descriptionZht/...`，不會修正已污染資料。
  - [AdminRewardDomainServiceImpl.java](/D:/Archive/trip-of-macau/packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminRewardDomainServiceImpl.java) 同樣直接讀寫 reward domain 欄位。
- Missing:
  - 修復 Phase 23 reward seed 全部中文/繁中文案為有效 UTF-8。
  - 重播 reward seed 到本地 MySQL，覆蓋目前已污染資料。
  - 為 reward smoke / integration test 增加 anti-mojibake 斷言，避免之後再把亂碼 seed 進庫。

## Issue 2

- UAT test: 3
- Symptom: `榮譽與稱號` 頁面有亂碼，且缺少「某地圖探索度達標即獲得的特別地區稱號」示例。
- Root cause:
  - [HonorManagement.tsx](/D:/Archive/trip-of-macau/packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/HonorManagement.tsx) 的 `title` / `subTitle` 已在源碼中變成亂碼，這是頁面框架層問題，不是資料投影問題。
  - [24-phase-23-reward-seed.sql](/D:/Archive/trip-of-macau/scripts/local/mysql/init/24-phase-23-reward-seed.sql) 目前只提供單一 badge、單一 title 和單一 fragment 展示，沒有足夠的地圖探索進度型稱號樣例，導致榮譽頁無法展示用戶要求的內容深度。
- Evidence:
  - [HonorManagement.tsx](/D:/Archive/trip-of-macau/packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/HonorManagement.tsx) 中 `title` / `subTitle` literal 已是 mojibake。
  - [24-phase-23-reward-seed.sql](/D:/Archive/trip-of-macau/scripts/local/mysql/init/24-phase-23-reward-seed.sql) 只有 `reward_title_mirror_harbor_chronicler` 一條 title 類型獎勵。
- Missing:
  - 將榮譽頁 header 文案恢復為繁體中文。
  - 補充多條基於城市或子地圖探索度門檻的 `title` 類型 reward seed。
  - 讓榮譽頁的展示資料能覆蓋 badge / title / region-progress title 三類典型案例。

## Issue 3

- UAT test: 4
- Symptom: 公開 reward endpoints 返回內容也有亂碼。
- Root cause:
  - 公開接口不是在輸出層毀壞字元，而是把已污染的 reward domain 資料原樣投出。
  - [PublicCatalogServiceImpl.java](/D:/Archive/trip-of-macau/packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicCatalogServiceImpl.java) 對 `redeemable_prizes`、`game_rewards`、`reward_presentations` 的 `name/description/highlight/summaryText` 等欄位都是直接用 `resolveText(...)` 返回。
  - 因為 seed 與 live DB 值已亂，公開接口自然同樣亂。
- Evidence:
  - [PublicCatalogServiceImpl.java](/D:/Archive/trip-of-macau/packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicCatalogServiceImpl.java):302-305、380-383、998-1009 直接投影 reward text。
  - [24-phase-23-reward-seed.sql](/D:/Archive/trip-of-macau/scripts/local/mysql/init/24-phase-23-reward-seed.sql) 中對應欄位 literal 已污染。
- Missing:
  - 修復 seed 後重播資料並重新驗證 `GET /api/v1/redeemable-prizes`、`GET /api/v1/game-rewards`、`GET /api/v1/reward-presentations/{id}`。
  - 增加 public reward smoke / test 的文本健全性檢查，至少針對 zh-Hant payload 做亂碼檢測。
