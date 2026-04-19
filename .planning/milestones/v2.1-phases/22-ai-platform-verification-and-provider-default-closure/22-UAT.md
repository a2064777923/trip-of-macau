---
status: testing
phase: 22-ai-platform-verification-and-provider-default-closure
source:
  - 22-01-SUMMARY.md
  - 22-02-SUMMARY.md
  - 22-03-SUMMARY.md
started: 2026-04-19T02:41:00+08:00
updated: 2026-04-19T11:31:00+08:00
---

## Current Test

number: 5
name: Creative Studio Route
expected: |
  Open `/ai/creative-studio`.
  The page should list recent generation jobs and show finalized-candidate state clearly enough that a user can distinguish current candidate vs finalized asset state.
awaiting: user response

# Phase 22 UAT

## Preconditions

- Admin backend is running on `http://127.0.0.1:8081`
- Admin UI is reachable and logged in
- SQL repair `scripts/local/mysql/init/36-phase-22-ai-platform-verification.sql` has been applied
- Latest smoke has passed on this workstation

## Tests

### 1. Cold Start Smoke Test
expected: Kill the old admin backend, start the current local stack from scratch, and verify the witness provider defaults plus creative finalize path still work on live `8081`.
result: pass
reason: Automated verification completed locally through `mvn -Dtest=AdminAiServiceImplTest test` and `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-22-ai-platform-verification.ps1`.

### 2. AI Overview Route
expected: Open `/ai` in the admin UI. The page should render as the AI capability center overview, not a wrong redirect or blank page. You should see provider health, recent jobs, fallback / stale-provider summary, and cost wording that is clearly described as estimated rather than vendor-billed truth.
result: pass

### 3. Provider Truth Page
expected: Open `/ai/providers`. `dashscope-chat` should present as the current live witness path. `hunyuan` should show as `CREDENTIAL_MISSING`. `openai`, `minimax`, `volcengine`, and `custom` should remain `TEMPLATE_ONLY`. No visible `traveler-services` ghost route should exist.
result: issue
reported: "最近請求與最近生成作業應該要可以查看詳情，具體是哪個用戶請求的以及完整的輸入和輸出"
severity: major

### 4. Models and Capability Routes
expected: Open `/ai/models`, `/ai/capabilities`, and `/ai/capabilities/travel_qa`. Models should show inventory rows plus provider-truth semantics. Capabilities should open their own dedicated pages without redirect confusion or reused placeholder ownership.
result: pass

### 5. Creative Studio Route
expected: Open `/ai/creative-studio`. The page should list recent generation jobs and show finalized-candidate state clearly enough that a user can distinguish current candidate vs finalized asset state.
result: pending
note: 2026-04-19 local backend regression on DashScope TTS was repaired. Live evidence now shows `admin_tts_generation` completes successfully on `8081`, persists the candidate to COS, and no longer fails with CosyVoice `418`.

### 6. Observability and Settings Pages
expected: Open `/ai/observability` and `/ai/settings`. Observability should surface health, sync, latency, fallback, and estimated cost. Settings should persist governance values and keep cost wording explicitly estimated.
result: pending

### 7. POI Witness Authoring Flow
expected: In `POIManagement`, edit a POI with `coverAssetId`, `mapIconAssetId`, and `audioAssetId`. Launch the shared AI workbench from one visual slot and from `audioAssetId`, finalize a candidate, and confirm the field now holds a canonical finalized asset id instead of detached candidate-only state.
result: pending

## Summary

total: 7
passed: 3
issues: 1
pending: 3
skipped: 0
blocked: 0

## Gaps

- truth: "Recent requests and recent generation jobs expose drill-down details, including the requesting user and complete input / output payloads."
  status: failed
  reason: "User reported: 最近請求與最近生成作業應該要可以查看詳情，具體是哪個用戶請求的以及完整的輸入和輸出"
  severity: major
  test: 3
  root_cause: ""
  artifacts: []
  missing: []
  debug_session: ""
