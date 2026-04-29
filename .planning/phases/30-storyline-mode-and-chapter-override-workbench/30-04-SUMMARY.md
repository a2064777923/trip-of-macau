---
phase: 30-storyline-mode-and-chapter-override-workbench
plan: 04
subsystem: verification
tags: [mysql, utf8mb4, smoke-test, storylines, runtime]
requires:
  - phase: 30-storyline-mode-and-chapter-override-workbench
    provides: admin and public runtime implementation
provides:
  - Phase 30 UTF-8 seed
  - Phase 30 live smoke script
  - Phase 30 handoff and verification evidence
affects: [phase-31-governance, phase-32-progress, phase-33-content-package, phase-34-mini-program-runtime]
tech-stack:
  added: []
  patterns: [UTF-8 SQL seed, HttpClient smoke, public no-status assertion]
key-files:
  created:
    - scripts/local/mysql/init/41-phase-30-storyline-mode-overrides.sql
    - scripts/local/smoke-phase-30-storyline-mode.ps1
    - .planning/phases/30-storyline-mode-and-chapter-override-workbench/30-HANDOFF.md
    - .planning/phases/30-storyline-mode-and-chapter-override-workbench/30-VERIFICATION.md
  modified: []
key-decisions:
  - "Seed demonstrates chapter 1 inheritance from A-Ma POI default flow and replacement/append story overrides."
  - "Smoke resolves admin auth from env or ignored local credentials and does not track secrets."
patterns-established:
  - "Live smoke validates admin snapshot, public runtime, compiled story steps, and Phase 29 POI regression."
requirements-completed: [STORY-02, STORY-04, LINK-02]
duration: 1h
completed: 2026-04-29
---

# Phase 30-04: Seed And Verification Summary

**UTF-8 seeded first story-mode slice with live admin/public smoke coverage**

## Performance

- **Duration:** Approximately 1 hour in this continuation pass.
- **Completed:** 2026-04-29T10:02:20+08:00
- **Tasks:** 3
- **Files modified:** 4

## Accomplishments

- Added `41-phase-30-storyline-mode-overrides.sql` with `東西方文明的戰火與共生` and chapter 1 `鏡海初戰：中葡首次海防對峙`.
- Seeded `story_east_west_ch01_flow`, replacement `arrival_intro_media`, append story steps, overrides, relation links, and exploration elements.
- Added `smoke-phase-30-storyline-mode.ps1` with UTF-8 `HttpClient`, env/file auth resolution, public no-status assertions, and A-Ma POI regression probe.
- Imported the seed into local MySQL and verified against live 8081/8080 services.

## Task Commits

No atomic commits were created in this dirty brownfield worktree. Files were verified through live local commands listed in `30-VERIFICATION.md`.

## Files Created/Modified

- `scripts/local/mysql/init/41-phase-30-storyline-mode-overrides.sql` - Idempotent Phase 30 seed.
- `scripts/local/smoke-phase-30-storyline-mode.ps1` - Live smoke for admin workbench and public runtime.
- `.planning/phases/30-storyline-mode-and-chapter-override-workbench/30-HANDOFF.md` - Follow-on boundary handoff.
- `.planning/phases/30-storyline-mode-and-chapter-override-workbench/30-VERIFICATION.md` - Exact verification evidence.

## Decisions Made

- Kept Chinese seed content in a UTF-8 SQL file and imported through mysql client with `--default-character-set=utf8mb4`.
- Used character-code construction for the smoke title assertion to avoid Windows PowerShell source-encoding false failures.

## Deviations from Plan

- The smoke initially parsed the admin list primary key as `id`; the real DTO exposes `storylineId`. The script now uses `storylineId` with `id` fallback.
- Initial backend restart used unquoted `cmd set` variables and produced `root ` as the username. Restart was corrected to `set "KEY=value"`.

## Issues Encountered

- Stale 8081/8080 processes were running old code. They were killed and restarted from the current worktree.
- Windows PowerShell displayed raw Chinese script literals incorrectly when the script had no BOM. The script now avoids Chinese source literals for the assertion.

## User Setup Required

None for current local verification. Future runs can provide `PHASE30_ADMIN_BEARER`, `PHASE30_ADMIN_USERNAME`/`PHASE30_ADMIN_PASSWORD`, or ignored `tmp-admin-login.json`.

## Next Phase Readiness

Phase 31 can inspect seeded override rows and compiled steps as concrete governance fixtures. Phase 32 can consume seeded exploration elements. Phase 33 can expand the one-chapter slice into the full flagship content package. Phase 34 can consume the public runtime DTO from the mini-program.

---
*Phase: 30-storyline-mode-and-chapter-override-workbench*
*Completed: 2026-04-29*
