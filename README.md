# Trip of Macau

Trip of Macau 是一個以故事化城市探索為核心的微信小程序與後台管理平台。當前代碼庫的重點是讓 `/admin` 成為小程序內容、地圖、室內空間、獎勵、AI 能力、用戶進度與營運工具的統一控制台，並由 `packages/server` 提供小程序可消費的真實公開 API。

## 主要模組

- `packages/client`：Taro / React 微信小程序前端。
- `packages/server`：小程序公開 API 後端，端口預設為 `8080`。
- `packages/admin/aoxiaoyou-admin-backend`：後台管理 API，端口預設為 `8081`。
- `packages/admin/aoxiaoyou-admin-ui`：後台管理系統前端，Vite 開發端口預設為 `5173`，路徑為 `/admin/`。
- `scripts/local/mysql/init`：本地 MySQL schema 與階段種子資料。
- `.planning`：GSD milestone、phase、驗證與狀態文檔。

## 本地啟動

先啟動 Docker Desktop，確保 MySQL / MongoDB / Redis 依賴可用，然後按需要啟動服務：

```powershell
scripts\local\start-admin-backend.cmd
```

```powershell
scripts\local\start-admin-ui.cmd
```

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\local\start-public-backend.ps1
```

```powershell
npm run dev:weapp --prefix packages\client
```

常用入口：

- 公開 API Swagger：`http://localhost:8080/swagger-ui.html`
- 管理 API Swagger：`http://localhost:8081/swagger-ui.html`
- 管理系統：`http://localhost:5173/admin/`

## 驗證

常用驗證命令：

```powershell
mvn -q -DskipTests compile -f packages\server\pom.xml
mvn -q -DskipTests compile -f packages\admin\aoxiaoyou-admin-backend\pom.xml
npm run build --prefix packages\admin\aoxiaoyou-admin-ui
```

階段 smoke 腳本位於 `scripts/local/smoke-phase-*.ps1`。執行前確認本地服務已啟動，且需要的本地環境變數與 COS / AI provider 設定已放在本機配置中，不要提交 secrets。

## 當前規劃狀態

目前 active milestone 是 `v3.0 Admin Core Domain Completion and Control-Plane Linkage`。Phase 28-32 已完成，下一個正式規劃入口是：

```text
/gsd-plan-phase 33
```

以 `.planning/PROJECT.md`、`.planning/ROADMAP.md`、`.planning/STATE.md` 為當前真實來源。舊的臨時啟動指南、一次性修復摘要與本地測試輸出不再作為 canonical docs。

## 文檔入口

- 項目狀態：`.planning/PROJECT.md`
- Roadmap：`.planning/ROADMAP.md`
- 當前 GSD 狀態：`.planning/STATE.md`
- Mini-program / admin / public contract：`docs/integration/miniapp-admin-public-contract.md`
- Admin IA 清理與保留策略：`docs/admin-control-plane-audit.md`

## 重要約束

- 所有中文、SQL、CSV、JSON 與請求 payload 必須使用 UTF-8 / utf8mb4。
- 不要在 tracked files 中提交 COS、微信、AI provider 或資料庫密碼。
- 後台界面文案使用繁體中文。
- 清理 dirty worktree 時只刪除本地生成物或明確死碼，不要回退未確認的 phase 實作。
