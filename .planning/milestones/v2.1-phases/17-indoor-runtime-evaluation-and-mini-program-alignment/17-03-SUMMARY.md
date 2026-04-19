---
phase: 17-indoor-runtime-evaluation-and-mini-program-alignment
plan: 03
subsystem: testing
tags: [powershell, smoke-test, wechat-build, uat, indoor-runtime]
requires:
  - phase: 17-indoor-runtime-evaluation-and-mini-program-alignment
    provides: live public indoor runtime contract and mini-program integration
provides:
  - phase 17 smoke harness
  - resumable UAT checklist
  - client script alias for runtime smoke
  - live proof against ports 8080 and 8081
affects: [future indoor regression checks, manual UAT, local QA workflows]
tech-stack:
  added: [powershell smoke harness]
  patterns: [live fixture discovery, fail-fast runtime assertions, build-in-the-loop verification]
key-files:
  created:
    - scripts/local/smoke-phase-17-indoor-runtime.ps1
    - .planning/phases/17-indoor-runtime-evaluation-and-mini-program-alignment/17-UAT.md
  modified:
    - packages/client/package.json
key-decisions:
  - "The smoke harness discovers Lisboa `1F` through admin APIs instead of hardcoding a floor id."
  - "Smoke verifies both a supported anonymous interaction and an auth-gated blocked interaction to prove both halves of the contract."
  - "Mini-program build remains part of the smoke path so phase completion is tied to an actual WeChat build artifact."
patterns-established:
  - "Indoor smoke scripts should fail fast on missing runtime fields, missing showcase behaviors, or downgraded fixture support levels."
  - "Manual UAT is tracked separately from automated smoke so experiential checks remain resumable."
requirements-completed: [RULE-03]
duration: multi-session
completed: 2026-04-17
---

# Phase 17 Plan 03 Summary

**Phase 17 now has repeatable live proof: a Lisboa runtime smoke harness, a WeChat build-in-the-loop check, and a manual UAT checklist for authored indoor behavior quality.**

## Accomplishments

- Added `scripts/local/smoke-phase-17-indoor-runtime.ps1` to validate live runtime data on `8080` and fixture discovery through `8081`.
- Added `.planning/.../17-UAT.md` for manual WeChat DevTools runtime checks.
- Added `npm run smoke:phase17:indoor-runtime` in `packages/client` for repeatable local validation from the mini-program workspace.
- Closed the local schema gap that originally caused runtime `500` responses.

## Files Created Or Modified

- `scripts/local/smoke-phase-17-indoor-runtime.ps1`
- `.planning/phases/17-indoor-runtime-evaluation-and-mini-program-alignment/17-UAT.md`
- `packages/client/package.json`

## Verification

- `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-17-indoor-runtime.ps1`
- `npm run smoke:phase17:indoor-runtime -- -SkipServerTests -SkipBuild` in `packages/client`
- `mvn -q "-Dtest=PublicIndoorRuntimeServiceTest,PublicIndoorRuntimeInteractionServiceTest" test` in `packages/server`
- `npm run build:weapp` in `packages/client`

## Live Outcomes

- Runtime snapshot on `http://127.0.0.1:8080/api/v1/indoor/floors/12/runtime?locale=zh-Hant` returned successfully.
- Smoke found the canonical Lisboa showcase behaviors:
  - `night-market-schedule-overlay`
  - `royal-palace-dwell-reveal`
  - `zipcity-guiding-path`
- Anonymous `zipcity-guiding-path` tap interaction was accepted and returned `path_motion` plus `bubble`.
- Anonymous `royal-palace-dwell-reveal` dwell interaction was blocked with `auth_required`.

## Notes

- The first live run failed because local MySQL had not applied the authored overlay-geometry migration; smoke and runtime are now aligned after applying the missing scripts.
- Manual experiential checks still belong in DevTools; the UAT file captures the remaining visual and interaction-quality checkpoints.
