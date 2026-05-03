---
phase: 38-public-runtime-asset-consumption
plan: 03
subsystem: testing
tags: [powershell, smoke-test, public-runtime, story-assets, event-ingestion]
requires:
  - phase: 38-01
    provides: traveler-safe runtime asset sanitation
  - phase: 38-02
    provides: allowlisted idempotent public story events
provides:
  - Repeatable Phase 38 public runtime asset smoke
  - Authenticated local/dev event/session smoke path
  - Phase 38 verification evidence and requirements traceability
affects: [phase-39-mini-program-story-consumption, phase-40-acceptance]
tech-stack:
  added: []
  patterns: [utf8mb4-smoke-import, dev-bypass-authenticated-smoke, runtime-privacy-assertions]
key-files:
  created:
    - scripts/local/smoke-phase-38-public-runtime-assets.ps1
    - .planning/phases/38-public-runtime-asset-consumption/38-VERIFICATION.md
  modified:
    - scripts/local/mysql/init/49-phase-33-east-west-flagship-story.sql
    - .planning/REQUIREMENTS.md
key-decisions:
  - "Phase 38 smoke supports either PHASE38_TRAVELER_BEARER_TOKEN or a local/dev PHASE38_TRAVELER_DEV_IDENTITY without printing bearer tokens."
  - "The flagship seed now links a finale video content block so public runtime can verify video-kind unsupported/fallback behavior, not only image/audio/Lottie paths."
  - "RUN-03 is marked complete only after authenticated local smoke plus focused unit tests pass."
patterns-established:
  - "Public runtime smoke imports only known UTF-8/utf8mb4 seed files and asserts banned public DTO field names."
  - "Lifecycle filtering is verified through media objects with availability metadata instead of raw admin fields."
requirements-completed: [RUN-01, RUN-02, RUN-03, RUN-04]
duration: 70 min
completed: 2026-05-03
---

# Phase 38 Plan 03: Public Runtime Smoke and Traceability Summary

**Repeatable smoke now proves the flagship story runtime exposes safe media, hides admin provenance, and records baseline story events idempotently.**

## Performance

- **Duration:** 70 min
- **Started:** 2026-05-03T06:55:00Z
- **Completed:** 2026-05-03T14:10:00+08:00
- **Tasks:** 3
- **Files modified:** 4

## Accomplishments

- Added `scripts/local/smoke-phase-38-public-runtime-assets.ps1` with UTF-8 output, MySQL seed import, flagship story runtime checks, banned-field assertions, media-kind checks, and optional authenticated event/session checks.
- Restarted the local public backend on port `8080` with the latest classes after detecting an old Java process from April 30.
- Added local/dev authenticated smoke support through `PHASE38_TRAVELER_DEV_IDENTITY`, while still accepting `PHASE38_TRAVELER_BEARER_TOKEN` for external bearer-token runs.
- Added a final recap video content block to the Phase 33 flagship seed so Phase 38 verifies public video media as an unsupported/fallback-safe DTO, not as a raw unpublished URL.
- Created `38-VERIFICATION.md` and updated `.planning/REQUIREMENTS.md` to mark `RUN-01` through `RUN-04` complete with evidence.

## Task Commits

Pending in this plan commit.

## Files Created/Modified

- `scripts/local/smoke-phase-38-public-runtime-assets.ps1` - Phase 38 public runtime asset/privacy/event smoke.
- `scripts/local/mysql/init/49-phase-33-east-west-flagship-story.sql` - adds `ch05_finale_recap_video` and links it to the flagship finale chapter.
- `.planning/phases/38-public-runtime-asset-consumption/38-VERIFICATION.md` - exact verification commands and caveats.
- `.planning/REQUIREMENTS.md` - marks `RUN-01` through `RUN-04` complete after verification.

## Decisions Made

- Used local/dev dev-bypass for authenticated smoke because no traveler bearer token was available; this verifies public event/session behavior without storing or printing a token.
- Kept the video smoke strict instead of weakening acceptance; the seed now includes a real runtime video block so Phase 39 can consume the same media-kind contract.
- Recorded WeChat/device and mini-program rendering as caveats because those remain Phase 39/40 scope.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Restarted stale public backend before trusting runtime JSON**
- **Found during:** Task 38-03-01
- **Issue:** Port `8080` was served by a Java process started on 2026-04-30, before Phase 38 code existed, causing misleading runtime asset results.
- **Fix:** Restarted `packages/server` with profile `local` and `-Dmaven.test.skip=true` so the smoke exercised the current compiled classes.
- **Files modified:** None.
- **Verification:** Phase 38 smoke advanced beyond the initial missing asset failure.

**2. [Rule 1 - Bug] Fixed PowerShell asset collection typing**
- **Found during:** Task 38-03-01
- **Issue:** `System.Collections.Generic.List[object]` collection returned `Argument types do not match` under this Windows PowerShell runtime.
- **Fix:** Switched `Collect-MediaAssets` to ordinary PowerShell array accumulation.
- **Files modified:** `scripts/local/smoke-phase-38-public-runtime-assets.ps1`.
- **Verification:** Smoke progressed to semantic media-kind validation.

**3. [Rule 3 - Blocking] Added a public runtime video block to the flagship seed**
- **Found during:** Task 38-03-01
- **Issue:** Video assets existed in `content_assets` and material package versions, but the flagship `east_west_war_and_coexistence` story did not link any video asset into runtime content blocks or compiled steps.
- **Fix:** Added `ch05_finale_recap_video`, linked it to chapter 5, and pointed it at existing placeholder video asset `333008` so public runtime verifies unsupported video delivery safely.
- **Files modified:** `scripts/local/mysql/init/49-phase-33-east-west-flagship-story.sql`.
- **Verification:** Authenticated Phase 38 smoke passed with image/audio/video/Lottie coverage.

---

**Total deviations:** 3 auto-fixed (1 bug, 2 blocking).
**Impact on plan:** All fixes were required to make the planned smoke truthful and repeatable; no mini-program Phase 39 scope was implemented.

## Issues Encountered

- `rg` was unavailable in this Codex desktop runtime due to a WindowsApps access error; PowerShell and `git grep` were used instead.
- Authenticated smoke needed `WECHAT_DEV_BYPASS_ENABLED=true` on the local public backend. The verification uses dev-bypass only for local/dev proof and does not claim production WeChat login UAT.

## User Setup Required

None for anonymous runtime smoke. To run authenticated checks locally, start the public backend with `WECHAT_DEV_BYPASS_ENABLED=true` under `local` or `dev`, then set `PHASE38_TRAVELER_DEV_IDENTITY` before running the smoke. Alternatively set `PHASE38_TRAVELER_BEARER_TOKEN`.

## Next Phase Readiness

Phase 39 can consume public runtime DTOs knowing the backend now exposes safe media availability/fallback state, content block media for image/audio/video/Lottie, idempotent event writes, dynamic exploration queries, and retry-safe session exit.

---
*Phase: 38-public-runtime-asset-consumption*
*Completed: 2026-05-03*
