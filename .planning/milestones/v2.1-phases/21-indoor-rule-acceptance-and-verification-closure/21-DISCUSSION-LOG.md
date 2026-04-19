# Phase 21: Indoor Rule Acceptance and Verification Closure - Discussion Log

> **Audit trail only.** Do not use as input to planning or execution agents.
> Canonical decisions live in `21-CONTEXT.md`.

**Date:** 2026-04-18
**Phase:** 21-indoor-rule-acceptance-and-verification-closure
**Mode:** Auto-discuss triggered by `/gsd-next`
**Areas discussed:** 驗收邊界, 驗證證據, 執行時與登入覆蓋, 小程序驗收邊界

---

## 驗收邊界

| Option | Description | Selected |
|--------|-------------|----------|
| 關閉式驗收 | 以驗證既有 15-17 承諾為主，只修補阻塞驗證的缺陷，不新增功能 | ✓ |
| 邊驗收邊擴功能 | 把新規則類型與新治理功能一起併入 | |
| 只做文件整理 | 不做實機與 live stack 驗證，只補文檔 | |

**Auto choice:** 關閉式驗收
**Reasoning:** Phase 21 在 roadmap 中明確是 acceptance / verification closure，而不是功能擴建 phase。

---

## 驗證證據

| Option | Description | Selected |
|--------|-------------|----------|
| 分層證據鏈 | 結合 fixture、admin smoke、public smoke、測試、client build、WeChat UAT | ✓ |
| 全手工驗收 | 主要依賴人工操作與肉眼確認 | |
| 全自動 smoke | 以腳本為主，弱化 DevTools 與人工體驗驗收 | |

**Auto choice:** 分層證據鏈
**Reasoning:** 這最符合現有 Phase 15/16/17 的驗證模式，也能對應里程碑收口所需的可追溯性。

---

## 執行時與登入覆蓋

| Option | Description | Selected |
|--------|-------------|----------|
| 同時驗證匿名與登入 | 驗證匿名可見、受保護互動需登入，以及登入後重試路徑 | ✓ |
| 只驗證登入後流程 | 忽略匿名阻擋與 auth wall 路徑 | |
| 只驗證公開互動 | 不處理受保護互動 | |

**Auto choice:** 同時驗證匿名與登入
**Reasoning:** Phase 17 既有 smoke 和 UAT 已經把這兩半合同分開，Phase 21 必須把它完整收口。

---

## 小程序驗收邊界

| Option | Description | Selected |
|--------|-------------|----------|
| 驗證現有合同 | 在 DevTools 驗證現有 indoor runtime 合同與視覺/互動表現，不擴 scope | ✓ |
| 同步重做體驗設計 | 連帶重構小程序室內體驗與特殊聯動 | |
| 暫停小程序驗收 | 只驗 admin 與 backend，之後再說 | |

**Auto choice:** 驗證現有合同
**Reasoning:** 這符合 Phase 21 的里程碑收口目標，也尊重使用者先前將更深的小程序聯動設計延後到後續 milestone 的方向。

---

## The Agent's Discretion

- 是否將 Phase 21 的自動驗證整合為一個 wrapper script，或延用多個既有 smoke script 串接。
- Phase 21 `UAT` / `VERIFICATION` / `SUMMARY` 工件的細部命名與切分方式。

## Deferred Ideas

- 更深入的小程序互動體驗、劇情化聯動、特殊導航或遊戲化強化。
- 新 indoor rule 類型與更廣泛的跨域治理能力。

