# Phase 41 Research: WeChat Runtime UAT Harness and Story Entry Hardening

**Researched:** 2026-05-04  
**Scope:** Local codebase and existing smoke/UAT artifacts  
**Status:** Ready for planning

## Phase Goal

Make the real mini-program story route runnable against the local public backend and generated media assets before deeper gameplay engines are added.

## Current Implementation Facts

- `packages/client/package.json` already exposes `build:weapp`, `smoke:phase39:story-mode`, and `smoke:phase40:release-readiness`.
- `packages/client/config/dev.js` hardcodes local API base `http://127.0.0.1:8080/api/v1`, `USE_MOCK=false`, and dev bypass enabled by default.
- `packages/client/config/prod.js` disables dev bypass and reads `API_BASE_URL`, `CDN_BASE_URL`, and `USE_MOCK` from environment defaults.
- `packages/client/src/app.config.ts` registers `pages/story/index` and required location permissions.
- `packages/client/src/pages/story/index.tsx` already consumes public story runtime through `refreshStorylineRuntime`, renders chapter route context, starts/exits story sessions, records runtime events, renders content blocks, and displays runtime status/fallback copy.
- `packages/client/src/components/StoryContentBlockRenderer/index.tsx` already handles `image`, `gallery`, `audio`, `video`, `lottie`, `attachment_list`, missing-media notices, fallback notices, audio completion, video completion, and unavailable-media reporting callbacks.
- `packages/client/src/components/LottieAssetPlayer/index.tsx` uses `lottie-miniprogram`, fetches Lottie JSON through `Taro.request`, renders to `Canvas type='2d'`, destroys animation on unmount, and falls back to poster/fallback images or Traditional Chinese error text.
- `scripts/local/smoke-phase-39-mini-program-story-mode.ps1` already validates the public story list, finds flagship story `east_west_war_and_coexistence`, loads runtime, checks chapters/content/media/fallback metadata, and can optionally run authenticated story event/session checks with `PHASE39_TRAVELER_DEV_IDENTITY`.
- `scripts/local/smoke-phase-40-release-readiness.ps1` wraps Phase 36-39 smoke, optional builds, admin AI observability, and report generation, but its planning-artifact assumptions point to archived Phase 40 paths and it is not focused on WeChat DevTools launch evidence.

## Planning Implications

### What Phase 41 Should Build

- A new Phase 41 UAT harness script that checks public backend health, flagship runtime payload, generated media asset shapes, mini-program build output, generated `project.config.json`, and WeChat DevTools CLI availability/launch outcome.
- A client-side environment diagnostics surface or lightweight runtime helper that makes `API_BASE_URL`, `USE_MOCK`, `WECHAT_DEV_BYPASS_ENABLED`, runtime source, and media fallback state visible during story-page troubleshooting without exposing secrets.
- Focused story-page/media fallback hardening only where evidence shows blanks or ambiguous states; avoid rewriting the Phase 39 story runtime engine.
- A `41-UAT.md` or smoke report that separates automated checks, DevTools launch checks, physical-device checks, known workstation blockers, and accepted caveats.

### What Phase 41 Should Not Build

- Full gameplay event interpreter for pickups/tasks/rewards beyond existing baseline cards. That belongs to Phase 42.
- Admin support/repair workflows. Those belong to Phase 43.
- Full admin IA cleanup. That belongs to Phase 44.
- Production AR/photo recognition, speech gameplay, route-coverage games, puzzle/cannon-defense engines, or production indoor visual positioning.

## Technical Risks

- WeChat DevTools CLI may fail to bind or automate on this workstation, as v3.1 noted `listen EACCES: permission denied 127.0.0.1:3799`. The harness must record `BLOCKED` rather than pretending DevTools visual UAT passed.
- Devtools and physical devices cannot reach `127.0.0.1` in the same way as the host browser. The plan should expose a configurable mini-program API base URL and document when LAN IP or tunnel is required.
- Lottie support requires network JSON URL, 2D canvas, and destroy-on-exit behavior. The current component covers the basics; planning should preserve it and verify fallback states.
- Story page already reports some events on content view and media completion. Phase 41 should avoid adding new duplicate event semantics that conflict with Phase 42.
- Existing dirty worktree changes are unrelated. Plans must instruct executor to read current files and avoid reverting unrelated admin UI/SQL changes.

## Validation Architecture

Phase 41 validation should have three layers:

1. **Backend/runtime smoke:** call public health and `GET /api/v1/storylines/{id}/runtime?localeCode=zh-Hant` or existing compatible route; assert flagship runtime has 5 chapters, compiled steps, and media assets with availability/fallback metadata.
2. **Mini-program build smoke:** run `npm run build:weapp` in `packages/client`; assert `dist/app.js`, `dist/app.json`, `dist/pages/story/index.js`, and `dist/project.config.json` exist.
3. **DevTools/UAT evidence:** detect `D:/Software/微信web开发者工具/cli.bat` or configured `WECHAT_DEVTOOLS_CLI`; attempt `cli.bat open --project <dist>` when enabled; record PASS/BLOCKED/SKIP with safe evidence. Manual physical-device UAT remains a checklist item unless actually executed.

## Recommended Plan Shape

- **Plan 41-01:** UAT harness and report writer, reusing Phase 39 runtime checks and adding build/project/DevTools readiness.
- **Plan 41-02:** Story entry and media fallback diagnostics/hardening in client code.
- **Plan 41-03:** Documentation, npm aliases, and verification evidence for local/devtools/experience configuration.

## Verification Commands

- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-41-wechat-runtime-uat.ps1 -Quick`
- `npm run build:weapp` in `packages/client`
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-39-mini-program-story-mode.ps1`

---

## RESEARCH COMPLETE
