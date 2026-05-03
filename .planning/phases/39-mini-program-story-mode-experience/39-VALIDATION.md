---
phase: 39
slug: mini-program-story-mode-experience
status: draft
nyquist_compliant: true
wave_0_complete: false
created: 2026-05-03
---

# Phase 39 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | Taro 3.6.23 / React 18 / TypeScript / PowerShell local smoke |
| **Config file** | `packages/client/config/index.js` |
| **Quick run command** | `npm run build:weapp` from `packages/client` |
| **Full suite command** | `npm run build:weapp` plus `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-39-mini-program-story-mode.ps1` |
| **Estimated runtime** | ~60-180 seconds depending on Taro build and local backend availability |

## Sampling Rate

- **After every task commit:** Run the quickest relevant static check or targeted smoke for touched files.
- **After every plan wave:** Run `npm run build:weapp` from `packages/client`.
- **Before `/gsd-verify-work`:** Run full suite if local public backend is available; otherwise record backend-unavailable caveat.
- **Max feedback latency:** 180 seconds for build-only feedback.

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 39-01-01 | 01 | 1 | MP-01, MP-05 | T39-01 | No admin-only asset provenance or secrets in client DTOs | build/static | `npm run build:weapp` | ✅ | ⬜ pending |
| 39-01-02 | 01 | 1 | MP-01 | T39-02 | Unavailable media does not crash or white-screen | build/manual | `npm run build:weapp` | ✅ | ⬜ pending |
| 39-02-01 | 02 | 1 | MP-03, MP-04 | T39-03 | Anonymous stateful actions are gated; event retries are idempotent | smoke | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-39-mini-program-story-mode.ps1` | ❌ W0 | ⬜ pending |
| 39-02-02 | 02 | 1 | MP-04, MP-05 | T39-04 | Unsupported gameplay reports safe events without granting rewards locally | build/smoke | `npm run build:weapp` | ✅ | ⬜ pending |
| 39-03-01 | 03 | 2 | MP-02 | T39-05 | Route handoff does not expose unsafe params or fake geometry | build/manual | `npm run build:weapp` | ✅ | ⬜ pending |
| 39-04-01 | 04 | 2 | MP-01 to MP-05 | T39-06 | Smoke script prints no bearer token, API key, or provider/COS secret | smoke | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-39-mini-program-story-mode.ps1` | ❌ W0 | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

## Wave 0 Requirements

- [ ] `scripts/local/smoke-phase-39-mini-program-story-mode.ps1` — repeatable public runtime/session/event smoke for MP-01 through MP-05.
- [ ] Ensure the smoke can run anonymous runtime checks without auth and authenticated event checks only when `PHASE39_TRAVELER_DEV_IDENTITY` is set.
- [ ] Ensure the smoke never prints bearer tokens, API keys, COS secrets, provider secrets, local file paths, or prompt text.

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| WeChat DevTools visual story journey | MP-01 to MP-05 | WeChat canvas/media/map runtime behavior cannot be fully proven by Node build alone | Build `weapp`, open in WeChat DevTools, visit `pages/story/index?storyId=<flagshipId>`, start story mode, expand chapters, play media, tap runtime cards, and open map handoff. |
| Audio/video completion callbacks | MP-01, MP-03 | Taro media event behavior is platform/runtime dependent | In WeChat DevTools or device, play an audio/video block to completion and confirm no console crash and event path is attempted. |
| Route/current chapter visual quality | MP-02 | Visual clarity needs human inspection | Confirm current chapter, inactive chapters, and map destination context are visually distinct and Traditional Chinese copy is readable. |

## Validation Sign-Off

- [x] All tasks have `<automated>` verify or Wave 0 dependencies.
- [x] Sampling continuity: no 3 consecutive tasks without automated verify.
- [x] Wave 0 covers all missing references.
- [x] No watch-mode flags.
- [x] Feedback latency < 180s for build-only checks.
- [x] `nyquist_compliant: true` set in frontmatter.

**Approval:** approved 2026-05-03
