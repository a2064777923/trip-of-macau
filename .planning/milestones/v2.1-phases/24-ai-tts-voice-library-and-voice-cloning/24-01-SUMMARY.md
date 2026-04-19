# Phase 24 Summary — AI TTS 音色庫與聲音復刻

## 已完成

- Admin UI 已新增 `音色與聲音工坊` 分頁，並接入：
  - 供應商與語音模型篩選
  - 官方音色同步
  - 音色列表與試聽
  - 自定義聲音復刻
  - 試聽語言與試聽文本輸入
- `創作工作台` 已接入 `AiTtsWorkbenchFields`，TTS 任務不再依賴手寫 JSON，可直接選：
  - 語音模型
  - 音色
  - 語言
  - 格式
  - 採樣率
  - 語速 / 音高 / 音量
  - 方言 / 語氣指令
- Admin backend 已新增並實裝：
  - `GET /api/admin/v1/ai/voices`
  - `POST /api/admin/v1/ai/providers/{id}/sync-voices`
  - `POST /api/admin/v1/ai/voices/preview`
  - `POST /api/admin/v1/ai/voices/clone`
  - `POST /api/admin/v1/ai/voices/{voiceId}/refresh`
  - `DELETE /api/admin/v1/ai/voices/{voiceId}`
- MySQL migration `37-phase-24-ai-voice-library.sql` 已套用到本地 `aoxiaoyou`。
- Bailian 官方音色頁 parser 已從重型跨段正則改為順序掃描 `h3 -> table -> tbody`，避免整頁 HTML 解析空結果。
- 官方音色同步已可把 `cosyvoice-v3-flash` 音色寫入 `ai_provider_inventory`。
- 自定義聲音復刻已可建立、刷新狀態並再次被試聽調用。

## 實測結果

### 8081 admin backend

- 以本地 `admin / admin123` 成功登入取得 token。
- 重新打包並重啟 `packages/admin/aoxiaoyou-admin-backend` 於 `8081`。
- `POST /api/admin/v1/ai/providers/4/sync-voices`
  - 成功同步 `cosyvoice-v3-flash`
  - 返回 30 個可用系統音色
- `POST /api/admin/v1/ai/voices/preview`
  - `longanyang + zh` 成功，音頻已上傳 COS
  - `longanyang + en` 成功，音頻已上傳 COS
  - `longanyue_v3 + zh(簡體腳本)` 成功，音頻已上傳 COS
- `POST /api/admin/v1/ai/voices/clone`
  - 使用 COS 上的預覽音頻成功建立自定義音色 `Macau Guide Voice`
- `POST /api/admin/v1/ai/voices/271/refresh`
  - 復刻狀態成功從 `submitted/provisioning` 轉為 `OK/available`
- 再次對自定義音色做 `preview`
  - 成功返回 COS 音頻 URL，證明復刻音色可直接回用

### Admin UI

- `npm run type-check` 通過
- `npm run build` 通過

## 已知限制

- 百鍊 `cosyvoice-v3-flash` 的粵語試聽目前對腳本文字較敏感：
  - `粵語音色 + 簡體中文腳本` 可成功
  - `粵語音色 + 繁體 / 粵語口語腳本` 會返回 `Engine return error code: 428`
- 這說明目前平台已能精確控制音色與語種路由，但若要讓繁體後台直接輸入粵語繁體腳本也穩定成功，下一步需要補：
  - 粵語腳本的繁轉簡預處理，或
  - 不同 TTS 模型的語種能力差異提示 / 自動降級策略

## 本輪關鍵輸出

- 系統音色同步後可在 `/ai/voices` 直接選模、選音色、輸入測試文本並生成試聽。
- 自定義復刻音色可保存為管理員私有可重用資源，並在同頁刷新狀態與再次試聽。
- 創作工作台中的 TTS 配置已與音色體系對齊。
