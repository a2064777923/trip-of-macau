---
phase: 19
slug: ai-capability-platform-redesign-and-provider-model-orchestra
status: verified
threats_open: 0
asvs_level: 1
created: 2026-04-17
---

# Phase 19 - Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| Admin 瀏覽器 <-> Admin Backend API | 管理員在 AI 能力中心配置供應商、模型、策略與生成任務 | API Key / API Secret、模型設定、提示詞、生成任務資料 |
| Admin Backend <-> 外部 AI 供應商 | 後端按供應商設定發送模型請求、同步模型庫、輪詢任務與下載生成資源 | 供應商密鑰、提示詞、模型輸出、遠端資源 URL |
| Admin Backend <-> Tencent COS | 後端把 AI 生成的圖片與音頻落盤到 COS 並回填資源庫 | 二進位媒體、對象鍵、公開 URL |
| Admin Backend <-> MySQL | 儲存供應商配置、模型庫、生成任務、候選結果與審計日誌 | 加密後密鑰、遮罩欄位、工作歷史、摘要日誌 |

---

## Threat Register

| Threat ID | Category | Component | Disposition | Mitigation | Status |
|-----------|----------|-----------|-------------|------------|--------|
| P19-T01 | Information Disclosure | AI 供應商憑證儲存與讀取 | mitigate | `AiSecretCryptoService` 以環境變數密鑰材料加密儲存 API Key / Secret；後台回傳與表單更新只暴露遮罩值，且需顯式勾選替換密鑰才會覆寫。 | closed |
| P19-T02 | Elevation of Privilege | AI 生成任務、候選結果與歷史查閱 | mitigate | 任務列表、詳情、刷新、定稿與恢復都以 `ownerAdminId` 與 `canAccessJob(...)` 約束，只允許超級管理員或任務擁有者訪問。 | closed |
| P19-T03 | SSRF | 自定義供應商 Base URL 與供應商回傳的資源下載 URL | mitigate | 新增 `AiOutboundUrlGuard`，在供應商保存時校驗 Base URL；在 API 請求與資源下載前再次驗證 `https`、官方平台域名策略、DNS 解析結果不得落到本機 / 私網 / 保留地址；下載鏈路禁用 redirect，並加入大小上限控制。`custom` 供應商放寬為可用任意公開 HTTPS 主機，但仍禁止本機、私網與特殊內網域名。 | closed |
| P19-T04 | Sensitive Data Exposure | AI 請求日誌與生成結果摘要 | mitigate | 日誌只記錄 `hash(prompt)` 到 `inputDataHash`，`userOpenid` 會遮罩，`outputSummary` 截斷至 1024 字，未發現原始提示詞直接落入審計日誌。 | closed |

*Status: open / closed*  
*Disposition: mitigate (implementation required) / accept (documented risk) / transfer (third-party)*

---

## Accepted Risks Log

No accepted risks.

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-04-17 | 4 | 3 | 1 | Codex |
| 2026-04-17 | 4 | 4 | 0 | Codex |

補充說明：

- Phase 19 原始計劃文件未提供顯式 `<threat_model>` 區塊，因此本次威脅登記是依實作範圍與代碼鏈路補建。
- `P19-T03` 的修補已覆蓋供應商保存、外呼請求、圖片 / 音頻下載三個入口，並以定向單元測試驗證。

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-04-17
