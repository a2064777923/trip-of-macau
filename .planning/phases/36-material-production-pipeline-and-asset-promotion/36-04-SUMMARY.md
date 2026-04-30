---
phase: 36-material-production-pipeline-and-asset-promotion
plan: 36-04
subsystem: verification
tags: [smoke, cos, ffmpeg, material-production, gsd]

requires:
  - phase: 36-01
    provides: package production APIs and version history
  - phase: 36-02
    provides: production and board-slicing tooling
  - phase: 36-03
    provides: package-scoped admin UI controls
  - phase: 36-05
    provides: ffmpeg-gated video job tooling
provides:
  - repeatable Phase 36 smoke script
  - Phase 36 verification evidence
  - operator handoff and live dependency blocker record
affects: [phase-36, phase-37, material-qa, runtime-assets]

tech-stack:
  added: []
  patterns:
    - validate-only smoke mode for local contract checks
    - fail-closed live dependency gates for provider/COS/ffmpeg/auth readiness

key-files:
  created:
    - scripts/local/smoke-phase-36-material-production.ps1
    - .planning/phases/36-material-production-pipeline-and-asset-promotion/36-HANDOFF.md
    - .planning/phases/36-material-production-pipeline-and-asset-promotion/36-VERIFICATION.md
  modified:
    - .planning/phases/36-material-production-pipeline-and-asset-promotion/36-VALIDATION.md
    - .planning/ROADMAP.md
    - .planning/STATE.md

key-decisions:
  - "Phase 36 plans are executed but MAT-01 through MAT-05 stay pending until live smoke proves generated/imported assets, rollback, COS, provider, and ffmpeg evidence."
  - "The smoke script exposes dependency readiness without printing secret values."
  - "MAT-04 remains blocked on ffmpeg subtitle support instead of being downgraded to placeholder video."

patterns-established:
  - "Validation-only smoke can run safely without external secrets and still catch local contract drift."
  - "Live smoke success line is reserved for complete package/API/version/COS/rollback evidence."

requirements-completed: [MAT-01, MAT-02, MAT-03, MAT-04, MAT-05]

duration: 35 min
completed: 2026-04-30
---

# Phase 36 Plan 04: Smoke Verification and Handoff Summary

**Phase 36 now has repeatable smoke verification and handoff evidence, with live material production explicitly blocked rather than falsely completed.**

## Performance

- **Duration:** 35 min
- **Started:** 2026-04-30T07:47:00Z
- **Completed:** 2026-04-30T08:05:00Z
- **Tasks:** 3
- **Files modified:** 7

## Accomplishments

- Added `smoke-phase-36-material-production.ps1` with local validate-only checks, admin auth gate, `/production/preflight`, version-history checks, COS `HEAD` checks, reversible `/production/rollback`, and optional ffmpeg video branch.
- Captured the exact automated evidence: backend service test PASS, admin UI build PASS, local preflight PASS, board slicing dry-run PASS, smoke validate-only PASS, video/live smoke blocked.
- Updated validation, roadmap, and state to show all Phase 36 plans executed while leaving MAT requirements pending due to live external dependency blockers.

## Task Commits

Each task was committed atomically:

1. **Task 1: Create the Phase 36 smoke script with provider, COS, and ffmpeg preflight gates** - `94d1877` (test)
2. **Task 2: Run compile/build/smoke, capture evidence, and keep blockers honest** - same docs commit
3. **Task 3: Update roadmap, requirements, and state from verified evidence only** - same docs commit

**Plan metadata:** same docs commit

## Files Created/Modified

- `scripts/local/smoke-phase-36-material-production.ps1` - Repeatable Phase 36 smoke and dependency gate.
- `.planning/phases/36-material-production-pipeline-and-asset-promotion/36-HANDOFF.md` - Operator handoff, live commands, and blocker list.
- `.planning/phases/36-material-production-pipeline-and-asset-promotion/36-VERIFICATION.md` - Evidence table and requirement status.
- `.planning/phases/36-material-production-pipeline-and-asset-promotion/36-VALIDATION.md` - Updated per-task validation map.
- `.planning/ROADMAP.md` - Marked all Phase 36 plans executed but kept active milestone blocked.
- `.planning/STATE.md` - Recorded live dependency blockers.

## Decisions Made

- `REQUIREMENTS.md` remains pending for MAT-01 through MAT-05 because live generated/imported assets, COS URLs, rollback, and ffmpeg video output are not proven in this shell.
- `STATE.md` uses `status: blocked` so `/gsd-next` will not accidentally advance to Phase 37 before the live dependency gap is resolved.
- Validate-only smoke is allowed to pass without secrets; live smoke is not.

## Deviations from Plan

None - plan executed exactly as written, with blockers preserved in evidence rather than hidden.

## Issues Encountered

- Live smoke is blocked by missing `PHASE36_ADMIN_BEARER_TOKEN`.
- Provider/COS readiness is false in this shell: `OPENAI_API_KEY=False`, `PHASE36_COS_READY=False`.
- MAT-04 is blocked by missing `ffmpeg` subtitles support: `FFMPEG_SUBTITLES_UNAVAILABLE`.
- No live `phase36-production-report.json` with imported/published rows exists yet, so representative item/version/COS evidence remains pending.

## User Setup Required

Before closing Phase 36, load local runtime dependencies without committing secrets:

- Set `PHASE36_ADMIN_BEARER_TOKEN`.
- Set provider credentials such as `OPENAI_API_KEY` only in local environment/runtime config.
- Ensure backend/COS runtime is configured and set `PHASE36_COS_READY=true` after local COS smoke is valid.
- Install or expose `ffmpeg` and confirm `ffmpeg -filters` includes `subtitles`.
- Run `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-36-material-production.ps1`.

## Next Phase Readiness

Not ready to advance automatically. Phase 36 implementation plans are complete, but live requirement verification remains blocked. Resolve the blockers and rerun smoke before Phase 37 planning/execution.

---
*Phase: 36-material-production-pipeline-and-asset-promotion*
*Completed: 2026-04-30*
