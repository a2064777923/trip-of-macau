---
status: complete
phase: 24-ai-tts-voice-library-and-voice-cloning
source:
  - 24-01-SUMMARY.md
started: 2026-04-19T13:46:58+08:00
updated: 2026-04-19T13:56:23+08:00
---

## Current Test

[testing complete]

# Phase 24 UAT

## Preconditions

- Admin backend 正在 `http://127.0.0.1:8081` 運行
- Admin UI 可正常打開並已登入
- AI 供應商已配置可用的語音模型
- 本機已完成本輪 Phase 24 的最新前後端代碼部署

## Tests

### 1. 聲音與音色工作台入口
expected: 打開 `/ai/voices`。頁面應正常載入「聲音與音色工作台」，而不是空白頁、錯誤頁或錯誤跳轉。你應能看到供應商 / 模型選擇、系統音色與自定義音色區塊，以及同步音色、試聽、聲音複刻等入口。
result: pass

### 2. 系統音色同步與語言試聽
expected: 在工作台選擇可用 TTS 供應商與模型後，同步官方音色列表應成功。選中例如 `longanyue_v3` 這類系統音色時，語言下拉至少應能準確顯示普通話 / 粵語 / 英文等該音色支持的語言。選定語言並點擊試聽後，應能成功生成可播放音頻，而不是 400 / provider error。
result: pass

### 3. 聲音複刻素材上傳入口
expected: 打開「聲音複刻」流程時，來源音頻不應只剩純下拉框。你應可像其他媒體欄位一樣直接拖入音頻、打開本地檔案上傳，或改用已有資源。選中音頻資源後，表單會接受該資源，不再強制你同時填寫公開 `sourceUrl`。
result: pass

### 4. 自定義音色建立與回用
expected: 使用一份來源音頻建立自定義音色後，該音色應出現在自定義音色列表，可刷新狀態至可用，並可再次試聽或被後續 TTS 生成流程選用。
result: pass

### 5. 創作工作台 TTS 結構化配置
expected: 打開 AI 創作工作台的語音合成任務時，應看到結構化欄位，而不是手寫 JSON。你應可直接配置模型、音色、輸出語言、輸出格式、語速、音調、音量等欄位。
result: pass

### 6. 生成提交鎖定與加載狀態
expected: 在創作工作台點擊開始生成後，提交按鈕應立即進入 loading / disabled 狀態，避免重複提交；任務完成或失敗前，不應讓使用者誤以為沒有送出請求。
result: pass

## Summary

total: 6
passed: 6
issues: 0
pending: 0
skipped: 0
blocked: 0

## Gaps

[]
