---
phase: 34-public-runtime-and-mini-program-consumption-baseline
plan: 34-02
subsystem: client
tags: [taro, runtime, storylines, events, exploration]

requires:
  - phase: 34-01
    provides: Public runtime metadata on `GET /api/v1/storylines/{id}/runtime`
provides:
  - Typed mini-program API helpers for public storyline runtime, sessions, events, and user exploration
  - Runtime-to-story mapping helpers with live/fallback status
  - Anonymous-safe passive story event reporting helper
affects: [phase-34, mini-program-story-runtime, public-runtime]

tech-stack:
  added: []
  patterns:
    - Live runtime is merged into the existing public content cache with explicit `runtimeSource`
    - Passive runtime events no-op for anonymous users instead of creating synthetic identities

key-files:
  created: []
  modified:
    - packages/client/src/services/api.ts
    - packages/client/src/services/gameService.ts
    - packages/client/src/types/game.ts

key-decisions:
  - "Public runtime data is preferred when available, but story browsing keeps existing cached content when runtime sync fails."
  - "Story runtime event reporting is best-effort and anonymous-safe: no bearer token means no-op, not a visible traveler error."

requirements-completed:
  - LINK-02
  - VER-01

duration: 35min
completed: 2026-04-30
---

# Phase 34 Plan 34-02: Client Runtime Mapping Summary

**Mini-program story runtime contracts now map public compiled flows into story state with live/fallback status and auth-gated event helpers.**

## Performance

- **Duration:** 35 min
- **Started:** 2026-04-29T23:33:10Z
- **Completed:** 2026-04-30T00:08:00Z
- **Tasks:** 2
- **Files modified:** 3

## Accomplishments

- Added typed DTOs and API helpers for storyline runtime, sessions, experience events, session events, and user exploration.
- Added story runtime UI types for compiled steps, chapter runtime state, storyline runtime metadata, and runtime sessions.
- Added runtime mapping in `gameService.ts`, including live sync status `即時故事資料已同步`, fallback status `使用本機快取`, and stable `clientEventId` generation.

## Task Commits

1. **Tasks 34-02-01 and 34-02-02: Client runtime DTOs and mapping** - `15adffa` (`feat`)

## Files Created/Modified

- `packages/client/src/services/api.ts` - Added runtime/session/event/exploration DTOs and public API helpers.
- `packages/client/src/services/gameService.ts` - Added runtime mapping, cache merge, session helpers, and anonymous-safe event reporting.
- `packages/client/src/types/game.ts` - Added story runtime, runtime step, chapter runtime, and session types.

## Decisions Made

- Public runtime is merged into existing story state rather than introducing a parallel store, so the story page can continue to use `getStorylines()`.
- Event reporting remains best-effort and no-ops without auth to preserve anonymous read-only story browsing.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

- `rg` was blocked by WindowsApps permissions in this desktop runtime, so searches used PowerShell `Select-String`.

## User Setup Required

None - no external service configuration required for this plan.

## Verification

- `npm run build:weapp --prefix packages/client` exited `0`.
- Acceptance strings and helper names were verified with `Select-String`.

## Next Phase Readiness

Plan 34-03 can consume `refreshStorylineRuntime`, `recordStoryRuntimeEvent`, and `runtimeSteps` on story chapters.

---
*Phase: 34-public-runtime-and-mini-program-consumption-baseline*
*Completed: 2026-04-30*
